package com.roosafeed.home_cloud.auth.bootstrap;

import com.roosafeed.home_cloud.auth.dto.request.CreateUserRequest;
import com.roosafeed.home_cloud.auth.service.UserService;
import com.roosafeed.home_cloud.common.enums.UserRole;
import com.roosafeed.home_cloud.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {
    private final AppProperties appProperties;
    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var adminConfig = appProperties.getSecurity().getAdmin();

        if (adminConfig.getEmail() == null || adminConfig.getPassword() == null) {
            log.warn("Admin bootstrap skipped: admin credentials not configured");
            return;
        }

        // check if admin exists
        if (userService.userExists(adminConfig.getEmail())) {
            log.info("Admin bootstrap skipped: admin exists");
            return;
        }

        // create a new admin
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setEmail(adminConfig.getEmail());
        userRequest.setPassword(adminConfig.getPassword());
        userRequest.setDisplayName("ADMIN");

        userService.saveUser(userRequest, UserRole.ADMIN);
        log.info("Admin user created with configured credentials (email={})", adminConfig.getEmail());
    }
}

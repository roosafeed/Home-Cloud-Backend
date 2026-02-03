package com.roosafeed.home_cloud.common.dto;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class BaseDto {
    private UUID id;
    private Instant createdAt;
    private Instant updatedAt;
}

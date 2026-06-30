package com.convergentmemory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private Long userId;
    private String username;
    private String displayName;
    private String apiToken;
    private String message;
    private String homepageUrl;
}

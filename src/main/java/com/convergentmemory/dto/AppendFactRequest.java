package com.convergentmemory.dto;

import lombok.Data;

@Data
public class AppendFactRequest {
    private String targetFile;
    private String fact;
    private String userUtterance;
    private String sourceAgent;
}

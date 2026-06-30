package com.convergentmemory.dto;

import lombok.Data;

@Data
public class ConvergeRequest {
    private String mode = "rule";
    private String category = "INBOX";
    private Integer limit = 50;
}

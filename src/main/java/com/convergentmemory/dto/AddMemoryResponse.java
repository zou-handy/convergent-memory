package com.convergentmemory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddMemoryResponse {
    private Long id;
    private String filePath;
    private String message;
}

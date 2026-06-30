package com.convergentmemory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConvergeApplyResponse {
    private Long batchId;
    private int appliedClusters;
    private String message;
}

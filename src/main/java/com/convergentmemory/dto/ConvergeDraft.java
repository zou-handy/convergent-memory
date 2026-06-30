package com.convergentmemory.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ConvergeDraft {
    private String mode;
    private List<Cluster> clusters = new ArrayList<>();
    private int inputCount;
    private String summary;

    @Data
    public static class Cluster {
        private String suggestedTitle;
        private String suggestedCategory;
        private String suggestedFilePath;
        private List<Long> sourceIds = new ArrayList<>();
        private List<String> sourceCueTags = new ArrayList<>();
        private String draftContent;
        private double confidence;
    }
}

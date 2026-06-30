package com.convergentmemory.dto;

import lombok.Data;
import java.util.List;

@Data
public class AddMemoryRequest {
    private String title;
    private String content;
    private String sourceAgent;
    private List<String> tags;
    private List<String> cueTags;
}

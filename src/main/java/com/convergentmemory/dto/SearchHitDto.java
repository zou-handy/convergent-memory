package com.convergentmemory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchHitDto {
    private Long id;
    private String title;
    private String filePath;
    private String category;
    private String summary;
    private String tags;
    private String cueTags;
    private int score;
    private String hitOn;
}

package com.convergentmemory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "convergence_batch")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConvergenceBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String triggeredBy;

    @Column(length = 20)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String inputIds;

    @Column(length = 512)
    private String outputPath;

    @Column(columnDefinition = "TEXT")
    private String diffText;

    @Column(columnDefinition = "TEXT")
    private String humanDiff;

    @Column(nullable = false)
    private Boolean llmUsed;

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}

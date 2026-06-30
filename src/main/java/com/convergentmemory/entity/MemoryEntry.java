package com.convergentmemory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "memory_entry", indexes = {
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_updated", columnList = "updatedAt"),
    @Index(name = "idx_owner", columnList = "ownerId")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MemoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 512)
    private String filePath;

    @Column(nullable = false, length = 20)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(length = 512)
    private String tags;

    @Column(length = 512)
    private String cueTags;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

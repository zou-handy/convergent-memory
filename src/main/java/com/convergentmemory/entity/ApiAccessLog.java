package com.convergentmemory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_access_log", indexes = {
    @Index(name = "idx_log_created", columnList = "createdAt")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApiAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String endpoint;

    @Column(length = 512)
    private String query;

    @Column(length = 100)
    private String sourceAgent;

    private Boolean success;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}

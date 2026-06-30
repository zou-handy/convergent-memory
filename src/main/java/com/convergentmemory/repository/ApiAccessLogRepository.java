package com.convergentmemory.repository;

import com.convergentmemory.entity.ApiAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiAccessLogRepository extends JpaRepository<ApiAccessLog, Long> {}

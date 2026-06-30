package com.convergentmemory.repository;

import com.convergentmemory.entity.MemoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MemoryEntryRepository extends JpaRepository<MemoryEntry, Long> {
    Optional<MemoryEntry> findByFilePath(String filePath);
    Optional<MemoryEntry> findByFilePathAndOwnerId(String filePath, Long ownerId);
    List<MemoryEntry> findByOwnerIdOrderByUpdatedAtDesc(Long ownerId);
    List<MemoryEntry> findByOwnerIdAndCategoryOrderByUpdatedAtDesc(Long ownerId, String category);

    @Query("SELECT m FROM MemoryEntry m WHERE m.category = :category ORDER BY m.updatedAt DESC")
    List<MemoryEntry> findByCategoryOrderByUpdatedAtDesc(@Param("category") String category);

    @Query("SELECT m FROM MemoryEntry m " +
           "WHERE (:ownerId IS NULL OR m.ownerId = :ownerId) " +
           "  AND (LOWER(m.title) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "    OR LOWER(m.summary) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "    OR LOWER(m.tags) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "    OR LOWER(m.cueTags) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "  AND (:category IS NULL OR m.category = :category) " +
           "ORDER BY m.updatedAt DESC")
    List<MemoryEntry> search(@Param("q") String q,
                             @Param("category") String category,
                             @Param("ownerId") Long ownerId);
}

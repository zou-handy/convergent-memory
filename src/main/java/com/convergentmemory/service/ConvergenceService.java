package com.convergentmemory.service;

import com.convergentmemory.agent.ConvergerAgent;
import com.convergentmemory.dto.ConvergeDraft;
import com.convergentmemory.entity.ConvergenceBatch;
import com.convergentmemory.entity.MemoryEntry;
import com.convergentmemory.repository.ConvergenceBatchRepository;
import com.convergentmemory.repository.MemoryEntryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConvergenceService {

    private final ConvergerAgent converger;
    private final MemoryEntryRepository entryRepo;
    private final ConvergenceBatchRepository batchRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${memory.vault.path}")
    private String vaultPath;

    @Transactional
    public PreviewResult preview(String mode, String category, int limit) throws IOException {
        List<MemoryEntry> entries = entryRepo.findByCategoryOrderByUpdatedAtDesc(
                category == null || category.isBlank() ? "INBOX" : category.toUpperCase());
        if (entries.size() > limit) entries = entries.subList(0, limit);

        ConvergeDraft draft = "llm".equalsIgnoreCase(mode)
                ? converger.llmConverge(entries)
                : converger.ruleBasedConverge(entries);

        ConvergenceBatch batch = ConvergenceBatch.builder()
                .triggeredBy("api")
                .status("PREVIEW")
                .inputIds(entries.stream().map(e -> String.valueOf(e.getId())).collect(Collectors.joining(",")))
                .diffText(objectMapper.writeValueAsString(draft))
                .llmUsed("llm".equalsIgnoreCase(mode))
                .startedAt(LocalDateTime.now())
                .build();
        batch = batchRepo.save(batch);

        log.info("preview batch id={} mode={} input={} clusters={}",
                batch.getId(), draft.getMode(), entries.size(), draft.getClusters().size());
        return new PreviewResult(batch.getId(), draft);
    }

    @Transactional
    public ApplyResult apply(Long batchId) throws IOException {
        ConvergenceBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("batch not found: " + batchId));
        if (!"PREVIEW".equals(batch.getStatus())) {
            throw new IllegalStateException("batch status is " + batch.getStatus() + ", expected PREVIEW");
        }

        ConvergeDraft draft = objectMapper.readValue(batch.getDiffText(), ConvergeDraft.class);
        Path vault = Paths.get(vaultPath);
        int applied = 0;

        for (ConvergeDraft.Cluster cluster : draft.getClusters()) {
            Path target = vault.resolve(cluster.getSuggestedFilePath());
            Files.createDirectories(target.getParent());

            String content = cluster.getDraftContent();
            int suffix = 1;
            Path finalTarget = target;
            while (Files.exists(finalTarget)) {
                String name = target.getFileName().toString().replace(".md", "");
                finalTarget = target.getParent().resolve(name + "-" + suffix + ".md");
                suffix++;
            }
            Files.writeString(finalTarget, content, StandardCharsets.UTF_8);

            String relPath = vault.relativize(finalTarget).toString().replace(java.io.File.separatorChar, '/');
            MemoryEntry entry = MemoryEntry.builder()
                    .title(cluster.getSuggestedTitle())
                    .filePath(relPath)
                    .category(cluster.getSuggestedCategory())
                    .summary(cluster.getDraftContent().length() > 200
                            ? cluster.getDraftContent().substring(0, 200) + "..."
                            : cluster.getDraftContent())
                    .tags("")
                    .cueTags(String.join(",", cluster.getSourceCueTags()))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            entryRepo.save(entry);
            applied++;
            log.info("applied cluster -> {}", relPath);
        }

        batch.setStatus("APPLIED");
        batch.setFinishedAt(LocalDateTime.now());
        batchRepo.save(batch);
        return new ApplyResult(batch.getId(), applied);
    }

    public record PreviewResult(Long batchId, ConvergeDraft draft) {}
    public record ApplyResult(Long batchId, int applied) {}
}

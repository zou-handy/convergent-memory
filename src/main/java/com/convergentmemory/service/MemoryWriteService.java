package com.convergentmemory.service;

import com.convergentmemory.dto.AddMemoryRequest;
import com.convergentmemory.dto.AppendFactRequest;
import com.convergentmemory.entity.ApiAccessLog;
import com.convergentmemory.entity.MemoryEntry;
import com.convergentmemory.entity.User;
import com.convergentmemory.repository.ApiAccessLogRepository;
import com.convergentmemory.repository.MemoryEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemoryWriteService {

    private final MemoryEntryRepository entryRepo;
    private final ApiAccessLogRepository logRepo;

    @Value("${memory.vault.path}")
    private String vaultPath;

    private Path userVault(User user) {
        if (user == null) return Paths.get(vaultPath);
        return Paths.get(vaultPath).resolve("u").resolve(user.getUsername());
    }

    @Transactional
    public MemoryEntry addInboxNote(AddMemoryRequest req, User owner) throws IOException {
        Path vault = userVault(owner);
        Files.createDirectories(vault.resolve("inbox"));

        String date = LocalDate.now().toString();
        String slug = slugify(req.getTitle());
        Path filePath = vault.resolve("inbox").resolve(date + "-" + slug + ".md");
        int suffix = 1;
        while (Files.exists(filePath)) {
            filePath = vault.resolve("inbox").resolve(date + "-" + slug + "-" + suffix + ".md");
            suffix++;
        }

        Files.writeString(filePath, buildInboxMarkdown(req), StandardCharsets.UTF_8);
        log.info("Wrote inbox note: {} (owner={})", filePath, owner == null ? "null" : owner.getUsername());

        String relPath = Paths.get(vaultPath).relativize(filePath).toString().replace(java.io.File.separatorChar, '/');
        MemoryEntry entry = MemoryEntry.builder()
                .ownerId(owner == null ? null : owner.getId())
                .title(req.getTitle())
                .filePath(relPath)
                .category("INBOX")
                .summary(buildSummary(req.getContent()))
                .tags(req.getTags() == null ? "" : String.join(",", req.getTags()))
                .cueTags(req.getCueTags() == null ? "" : String.join(",", req.getCueTags()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        entry = entryRepo.save(entry);

        logRepo.save(ApiAccessLog.builder()
                .endpoint("/api/memory/add")
                .sourceAgent(req.getSourceAgent())
                .success(true)
                .createdAt(LocalDateTime.now())
                .build());
        return entry;
    }

    @Transactional
    public MemoryEntry appendFact(AppendFactRequest req, User owner) throws IOException {
        Path vault = userVault(owner);
        Path filePath = vault.resolve(req.getTargetFile());
        Files.createDirectories(filePath.getParent());

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String source = req.getSourceAgent() == null ? "unknown" : req.getSourceAgent();
        String appendBlock = "\n\n<!-- agent 写入 @ " + timestamp + ", source=" + source + " -->\n- " + req.getFact() + "\n";

        if (!Files.exists(filePath)) {
            String header = "# " + filePath.getFileName().toString().replace(".md", "") + "\n\n";
            Files.writeString(filePath, header, StandardCharsets.UTF_8);
        }
        Files.writeString(filePath, appendBlock, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        log.info("Appended fact to {}: {}", filePath, req.getFact());

        String relPath = Paths.get(vaultPath).relativize(filePath).toString().replace(java.io.File.separatorChar, '/');
        final Path filePathFinal = filePath;
        final Long ownerId = owner == null ? null : owner.getId();
        MemoryEntry entry = entryRepo.findByFilePath(relPath).orElseGet(() ->
            MemoryEntry.builder()
                    .ownerId(ownerId)
                    .title(filePathFinal.getFileName().toString().replace(".md", ""))
                    .filePath(relPath)
                    .category(inferCategory(req.getTargetFile()))
                    .summary("")
                    .tags("")
                    .cueTags("")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build()
        );
        entry.setUpdatedAt(LocalDateTime.now());
        String existing = entry.getSummary() == null ? "" : entry.getSummary();
        String newSummary = (existing + " | " + req.getFact()).trim();
        if (newSummary.length() > 500) newSummary = newSummary.substring(newSummary.length() - 500);
        entry.setSummary(newSummary);
        entry = entryRepo.save(entry);

        logRepo.save(ApiAccessLog.builder()
                .endpoint("/api/memory/append-fact")
                .query(req.getUserUtterance())
                .sourceAgent(req.getSourceAgent())
                .success(true)
                .createdAt(LocalDateTime.now())
                .build());
        return entry;
    }

    private String inferCategory(String relPath) {
        if (relPath.startsWith("core/")) return "CORE";
        if (relPath.startsWith("context/")) return "CONTEXT";
        if (relPath.startsWith("archive/")) return "ARCHIVE";
        return "INBOX";
    }

    private String buildInboxMarkdown(AddMemoryRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append("---\n");
        sb.append("title: ").append(req.getTitle()).append("\n");
        sb.append("created: ").append(LocalDateTime.now()).append("\n");
        sb.append("source: ").append(req.getSourceAgent() == null ? "unknown" : req.getSourceAgent()).append("\n");
        if (req.getTags() != null && !req.getTags().isEmpty()) {
            sb.append("tags: [").append(String.join(", ", req.getTags())).append("]\n");
        }
        sb.append("category: INBOX\n");
        sb.append("---\n\n");
        sb.append("# ").append(req.getTitle()).append("\n\n");
        sb.append(req.getContent()).append("\n");
        return sb.toString();
    }

    private String buildSummary(String content) {
        if (content == null) return "";
        String oneline = content.replaceAll("\\s+", " ").trim();
        return oneline.length() > 200 ? oneline.substring(0, 200) + "..." : oneline;
    }

    private String slugify(String s) {
        if (s == null) return "untitled";
        String slug = s.toLowerCase()
                .replaceAll("[\\s_]+", "-")
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        if (slug.isEmpty()) slug = "untitled";
        return slug.length() > 50 ? slug.substring(0, 50) : slug;
    }
}

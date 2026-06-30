package com.convergentmemory.service;

import com.convergentmemory.dto.SearchHitDto;
import com.convergentmemory.entity.MemoryEntry;
import com.convergentmemory.repository.MemoryEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemorySearchService {

    private final MemoryEntryRepository entryRepo;

    public List<SearchHitDto> search(String q, String category, int limit, Long ownerId) {
        if (q == null || q.isBlank()) return List.of();
        String qLow = q.toLowerCase(Locale.ROOT);
        String cat = (category == null || category.isBlank()) ? null : category.toUpperCase(Locale.ROOT);

        List<MemoryEntry> rows = entryRepo.search(q, cat, ownerId);
        log.info("search q='{}' category={} owner={} -> {} raw hits", q, cat, ownerId, rows.size());

        List<SearchHitDto> out = new ArrayList<>();
        for (MemoryEntry e : rows) {
            int score = 0;
            List<String> hitOn = new ArrayList<>();
            if (e.getTitle() != null && e.getTitle().toLowerCase(Locale.ROOT).contains(qLow)) {
                score += 3; hitOn.add("title");
            }
            if (e.getCueTags() != null && e.getCueTags().toLowerCase(Locale.ROOT).contains(qLow)) {
                score += 2; hitOn.add("cueTags");
            }
            if (e.getTags() != null && e.getTags().toLowerCase(Locale.ROOT).contains(qLow)) {
                score += 1; hitOn.add("tags");
            }
            if (e.getSummary() != null && e.getSummary().toLowerCase(Locale.ROOT).contains(qLow)) {
                score += 1; hitOn.add("summary");
            }
            if (score == 0) score = 1;

            out.add(new SearchHitDto(
                    e.getId(), e.getTitle(), e.getFilePath(), e.getCategory(),
                    e.getSummary(), e.getTags(), e.getCueTags(),
                    score, String.join(",", hitOn)
            ));
        }
        out.sort(Comparator.comparingInt(SearchHitDto::getScore).reversed());
        return out.size() > limit ? out.subList(0, limit) : out;
    }
}

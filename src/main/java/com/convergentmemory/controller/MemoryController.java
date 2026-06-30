package com.convergentmemory.controller;

import com.convergentmemory.dto.AddMemoryRequest;
import com.convergentmemory.dto.AddMemoryResponse;
import com.convergentmemory.dto.AppendFactRequest;
import com.convergentmemory.dto.SearchHitDto;
import com.convergentmemory.entity.MemoryEntry;
import com.convergentmemory.entity.User;
import com.convergentmemory.service.MemorySearchService;
import com.convergentmemory.service.MemoryWriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/memory")
@RequiredArgsConstructor
@Slf4j
public class MemoryController {

    private final MemoryWriteService writeService;
    private final MemorySearchService searchService;

    private User currentUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !(a.getPrincipal() instanceof User u)) return null;
        return u;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK - Convergent Memory v0.2 (multi-user)");
    }

    @PostMapping("/add")
    public ResponseEntity<AddMemoryResponse> add(@RequestBody AddMemoryRequest req) throws IOException {
        User u = currentUser();
        MemoryEntry entry = writeService.addInboxNote(req, u);
        return ResponseEntity.ok(new AddMemoryResponse(
                entry.getId(), entry.getFilePath(),
                "已写入 inbox + DB 索引(owner=" + (u == null ? "anon" : u.getUsername()) + ")"));
    }

    @PostMapping("/append-fact")
    public ResponseEntity<AddMemoryResponse> appendFact(@RequestBody AppendFactRequest req) throws IOException {
        User u = currentUser();
        MemoryEntry entry = writeService.appendFact(req, u);
        return ResponseEntity.ok(new AddMemoryResponse(
                entry.getId(), entry.getFilePath(),
                "已追加事实到 " + entry.getFilePath()));
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam("q") String q,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        User u = currentUser();
        Long ownerId = u == null ? null : u.getId();
        List<SearchHitDto> hits = searchService.search(q, category, limit, ownerId);
        return ResponseEntity.ok(Map.of(
                "query", q,
                "category", category == null ? "" : category,
                "owner", u == null ? "anonymous(全局公开)" : u.getUsername(),
                "total", hits.size(),
                "hits", hits
        ));
    }
}

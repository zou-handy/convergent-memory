package com.convergentmemory.controller;

import com.convergentmemory.dto.ConvergeApplyResponse;
import com.convergentmemory.dto.ConvergeRequest;
import com.convergentmemory.entity.ConvergenceBatch;
import com.convergentmemory.repository.ConvergenceBatchRepository;
import com.convergentmemory.service.ConvergenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/converge")
@RequiredArgsConstructor
@Slf4j
public class ConvergeController {

    private final ConvergenceService convergenceService;
    private final ConvergenceBatchRepository batchRepo;

    @PostMapping("/preview")
    public ResponseEntity<Map<String, Object>> preview(@RequestBody(required = false) ConvergeRequest req) throws IOException {
        if (req == null) req = new ConvergeRequest();
        ConvergenceService.PreviewResult result = convergenceService.preview(
                req.getMode(), req.getCategory(), req.getLimit() == null ? 50 : req.getLimit());
        return ResponseEntity.ok(Map.of(
                "batchId", result.batchId(),
                "draft", result.draft(),
                "hint", "草案已生成,POST /api/converge/apply/" + result.batchId() + " 确认落盘"
        ));
    }

    @PostMapping("/apply/{batchId}")
    public ResponseEntity<ConvergeApplyResponse> apply(@PathVariable Long batchId) throws IOException {
        ConvergenceService.ApplyResult result = convergenceService.apply(batchId);
        return ResponseEntity.ok(new ConvergeApplyResponse(
                result.batchId(), result.applied(),
                "已落盘 " + result.applied() + " 份 cluster md + DB 索引"));
    }

    @GetMapping("/list")
    public ResponseEntity<List<ConvergenceBatch>> list() {
        return ResponseEntity.ok(batchRepo.findAll());
    }
}

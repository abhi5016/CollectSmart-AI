package com.collectsmart.controller;

import com.collectsmart.entity.LoanAgingSnapshot;
import com.collectsmart.service.LoanAgingSnapshotService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/aging")
public class LoanAgingSnapshotController {

    private final LoanAgingSnapshotService snapshotService;

    public LoanAgingSnapshotController(
            LoanAgingSnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    // Capture snapshots manually
    @PostMapping("/capture")
    public ResponseEntity<Map<String, Object>> captureSnapshots() {

        int count = snapshotService.captureDailySnapshots();

        return ResponseEntity.ok(
                Map.of(
                        "message", "Daily snapshots captured successfully",
                        "savedCount", count
                )
        );
    }

    // Get historical snapshots for a loan
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<LoanAgingSnapshot>> getLoanHistory(
            @PathVariable Long loanId) {

        return ResponseEntity.ok(
                snapshotService.getLoanHistory(loanId)
        );
    }

    // Calculate roll rate between two dates
    @GetMapping("/roll-rate")
    public ResponseEntity<?> getRollRate(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        try {
            return ResponseEntity.ok(
                    snapshotService.calculateRollRate(
                            startDate,
                            endDate
                    )
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
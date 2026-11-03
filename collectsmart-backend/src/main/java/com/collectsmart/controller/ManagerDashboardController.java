package com.collectsmart.controller;

import com.collectsmart.service.ManagerDashboardService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/manager/dashboard")
public class ManagerDashboardController {

    private final ManagerDashboardService dashboardService;

    public ManagerDashboardController(
            ManagerDashboardService dashboardService) {

        this.dashboardService = dashboardService;
    }

    // Dashboard summary
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {

        return ResponseEntity.ok(
                dashboardService.getDashboardSummary()
        );
    }

    // Loan aging report
    @GetMapping("/aging")
    public ResponseEntity<Map<String, Long>> getLoanAging() {

        return ResponseEntity.ok(
                dashboardService.getLoanAgingReport()
        );
    }
}
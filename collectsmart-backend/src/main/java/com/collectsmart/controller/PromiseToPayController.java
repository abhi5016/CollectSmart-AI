package com.collectsmart.controller;

import com.collectsmart.entity.PromiseToPay;
import com.collectsmart.service.PromiseToPayService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/promises")
public class PromiseToPayController {

    private final PromiseToPayService promiseService;

    public PromiseToPayController(PromiseToPayService promiseService) {
        this.promiseService = promiseService;
    }

    // Request body for creating a promise
    public record PromiseRequest(
            Long officerId,
            BigDecimal promisedAmount,
            LocalDate promisedDate
    ) {}

    // Create a new promise
    @PostMapping("/loan/{loanId}")
    public ResponseEntity<?> createPromise(
            @PathVariable Long loanId,
            @RequestBody PromiseRequest request) {

        try {
            PromiseToPay promise = promiseService.createPromise(
                    loanId,
                    request.officerId(),
                    request.promisedAmount(),
                    request.promisedDate()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(promise);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get promises for a loan
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<PromiseToPay>> getByLoan(
            @PathVariable Long loanId) {

        return ResponseEntity.ok(
                promiseService.getPromisesByLoan(loanId)
        );
    }

    // Get promises by officer
    @GetMapping("/officer/{officerId}")
    public ResponseEntity<List<PromiseToPay>> getByOfficer(
            @PathVariable Long officerId) {

        return ResponseEntity.ok(
                promiseService.getPromisesByOfficer(officerId)
        );
    }

    // Get promises by status
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(
            @PathVariable String status) {

        try {
            return ResponseEntity.ok(
                    promiseService.getPromisesByStatus(status)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get all pending promises
    @GetMapping("/pending")
    public ResponseEntity<List<PromiseToPay>> getPendingPromises() {

        return ResponseEntity.ok(
                promiseService.getPendingPromises()
        );
    }

    // Update promise status
    @PutMapping("/{promiseId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long promiseId,
            @RequestParam String status) {

        try {
            return ResponseEntity.ok(
                    promiseService.updatePromiseStatus(promiseId, status)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Mark expired promises as broken
    @PostMapping("/mark-broken")
    public ResponseEntity<Integer> markBrokenPromises() {

        return ResponseEntity.ok(
                promiseService.markBrokenPromises()
        );
    }
}
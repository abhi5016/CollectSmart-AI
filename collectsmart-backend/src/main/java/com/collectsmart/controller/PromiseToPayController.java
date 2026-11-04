package com.collectsmart.controller;

import com.collectsmart.config.AuthenticatedUser;
import com.collectsmart.entity.PromiseToPay;
import com.collectsmart.service.PromiseToPayService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promises")
public class PromiseToPayController {

private final PromiseToPayService promiseService;

public PromiseToPayController(PromiseToPayService promiseService) {
    this.promiseService = promiseService;
}

public record PromiseRequest(
        BigDecimal promisedAmount,
        LocalDate promisedDate
) {}

// Create promise - field officer only
@PostMapping("/loan/{loanId}")
@PreAuthorize("hasRole('FIELD_OFFICER')")
public ResponseEntity<?> createPromise(
        @PathVariable Long loanId,
        @RequestBody PromiseRequest request,
        @AuthenticationPrincipal AuthenticatedUser user) {

    try {
        if (user == null || user.fieldOfficerId() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message",
                            "Your account is not linked to a field officer."));
        }

        PromiseToPay promise = promiseService.createPromise(
                loanId,
                user.fieldOfficerId(),
                request.promisedAmount(),
                request.promisedDate()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(promise);

    } catch (SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", e.getMessage()));

    } catch (RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
    }
}

// Get promises for a loan
@GetMapping("/loan/{loanId}")
@PreAuthorize("hasAnyRole('MANAGER', 'FIELD_OFFICER')")
public ResponseEntity<?> getByLoan(
        @PathVariable Long loanId,
        @AuthenticationPrincipal AuthenticatedUser user) {

    try {
        List<PromiseToPay> promises =
                promiseService.getPromisesByLoan(
                        loanId,
                        user.role(),
                        user.fieldOfficerId()
                );

        return ResponseEntity.ok(promises);

    } catch (SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", e.getMessage()));

    } catch (RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
    }
}

// Get promises by officer
@GetMapping("/officer/{officerId}")
@PreAuthorize("hasAnyRole('MANAGER', 'FIELD_OFFICER')")
public ResponseEntity<?> getByOfficer(
        @PathVariable Long officerId,
        @AuthenticationPrincipal AuthenticatedUser user) {

    try {
        return ResponseEntity.ok(
                promiseService.getPromisesByOfficer(
                        officerId,
                        user.role(),
                        user.fieldOfficerId()
                )
        );

    } catch (SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", e.getMessage()));
    }
}

// Get promises by status - manager only
@GetMapping("/status/{status}")
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<?> getByStatus(
        @PathVariable String status,
        @AuthenticationPrincipal AuthenticatedUser user) {

    try {
        return ResponseEntity.ok(
                promiseService.getPromisesByStatus(
                        status,
                        user.role(),
                        user.fieldOfficerId()
                )
        );

    } catch (RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
    }
}

// Get pending promises
@GetMapping("/pending")
@PreAuthorize("hasAnyRole('MANAGER', 'FIELD_OFFICER')")
public ResponseEntity<?> getPendingPromises(
        @AuthenticationPrincipal AuthenticatedUser user) {

    try {
        return ResponseEntity.ok(
                promiseService.getPendingPromises(
                        user.role(),
                        user.fieldOfficerId()
                )
        );

    } catch (SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", e.getMessage()));
    }
}

// Update promise status - manager only
@PutMapping("/{promiseId}/status")
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<?> updateStatus(
        @PathVariable Long promiseId,
        @RequestParam String status) {

    try {
        return ResponseEntity.ok(
                promiseService.updatePromiseStatus(
                        promiseId,
                        status
                )
        );

    } catch (RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
    }
}

// Mark expired promises as broken - manager only
@PostMapping("/mark-broken")
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<Integer> markBrokenPromises() {

    return ResponseEntity.ok(
            promiseService.markBrokenPromises()
    );
}

}
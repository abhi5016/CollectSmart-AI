package com.collectsmart.controller;

import com.collectsmart.config.AuthenticatedUser;
import com.collectsmart.entity.LoanOutcome;
import com.collectsmart.service.LoanOutcomeService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/outcomes")
public class LoanOutcomeController {

private final LoanOutcomeService outcomeService;

public LoanOutcomeController(LoanOutcomeService outcomeService) {
    this.outcomeService = outcomeService;
}

// Keep officerId for compatibility with existing frontend requests.
// The backend ignores it and uses the JWT identity.
public record OutcomeRequest(
        Long officerId,
        String outcomeType,
        Double latitude,
        Double longitude,
        String notes
) {}

// Record an outcome using the authenticated officer identity
@PostMapping("/loan/{loanId}")
@PreAuthorize("hasRole('FIELD_OFFICER')")
public ResponseEntity<?> saveOutcome(
        @PathVariable Long loanId,
        @RequestBody OutcomeRequest request,
        @AuthenticationPrincipal AuthenticatedUser user) {

    try {
        if (user == null || user.fieldOfficerId() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "Your account is not linked to a field officer."
                    ));
        }

        LoanOutcome outcome = outcomeService.saveOutcome(
                loanId,
                user.fieldOfficerId(),
                request.outcomeType(),
                request.latitude(),
                request.longitude(),
                request.notes()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(outcome);

    } catch (RuntimeException exception) {
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "message",
                        exception.getMessage() != null
                                ? exception.getMessage()
                                : "Unable to save outcome."
                ));
    }
}

// Get outcomes for a loan
@GetMapping("/loan/{loanId}")
@PreAuthorize("hasAnyRole('MANAGER', 'FIELD_OFFICER')")
public ResponseEntity<?> getOutcomesByLoan(
        @PathVariable Long loanId,
        @AuthenticationPrincipal AuthenticatedUser user) {

    try {
        List<LoanOutcome> outcomes =
                outcomeService.getOutcomesByLoan(
                        loanId,
                        user.role(),
                        user.fieldOfficerId()
                );

        return ResponseEntity.ok(outcomes);

    } catch (RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "message",
                        exception.getMessage() != null
                                ? exception.getMessage()
                                : "Unable to retrieve outcomes."
                ));
    }
}

// Manager can view any officer's outcomes.
// Field officer can view only their own outcomes.
@GetMapping("/officer/{officerId}")
@PreAuthorize("hasAnyRole('MANAGER', 'FIELD_OFFICER')")
public ResponseEntity<?> getOutcomesByOfficer(
        @PathVariable Long officerId,
        @AuthenticationPrincipal AuthenticatedUser user) {

    try {
        if ("FIELD_OFFICER".equals(user.role())
                && (user.fieldOfficerId() == null
                || !user.fieldOfficerId().equals(officerId))) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "You cannot view another officer's outcomes."
                    ));
        }

        return ResponseEntity.ok(
                outcomeService.getOutcomesByOfficer(officerId)
        );

    } catch (RuntimeException exception) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", exception.getMessage()));
    }
}

}
package com.collectsmart.controller;

import com.collectsmart.entity.LoanOutcome;
import com.collectsmart.service.LoanOutcomeService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/outcomes")
public class LoanOutcomeController {

    private final LoanOutcomeService outcomeService;

    public LoanOutcomeController(LoanOutcomeService outcomeService) {
        this.outcomeService = outcomeService;
    }

    // Request body structure
    public record OutcomeRequest(
            Long officerId,
            String outcomeType,
            Double latitude,
            Double longitude,
            String notes
    ) {}

    // Record an outcome for a loan
    @PostMapping("/loan/{loanId}")
    public ResponseEntity<?> saveOutcome(
            @PathVariable Long loanId,
            @RequestBody OutcomeRequest request) {

        try {
            LoanOutcome outcome = outcomeService.saveOutcome(
                    loanId,
                    request.officerId(),
                    request.outcomeType(),
                    request.latitude(),
                    request.longitude(),
                    request.notes()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(outcome);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Get outcomes for a particular loan
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<?> getOutcomesByLoan(
            @PathVariable Long loanId) {

        try {
            List<LoanOutcome> outcomes =
                    outcomeService.getOutcomesByLoan(loanId);

            return ResponseEntity.ok(outcomes);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Get outcomes recorded by a field officer
    @GetMapping("/officer/{officerId}")
    public ResponseEntity<List<LoanOutcome>> getOutcomesByOfficer(
            @PathVariable Long officerId) {

        return ResponseEntity.ok(
                outcomeService.getOutcomesByOfficer(officerId)
        );
    }
}
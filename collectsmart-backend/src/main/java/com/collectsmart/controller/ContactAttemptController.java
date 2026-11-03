package com.collectsmart.controller;

import com.collectsmart.entity.ContactAttempt;
import com.collectsmart.entity.Loan;
import com.collectsmart.repository.ContactAttemptRepository;
import com.collectsmart.repository.LoanRepository;
import com.collectsmart.service.ContactTimeService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contact-attempts")
@CrossOrigin(origins = "*")
public class ContactAttemptController {

    private final ContactAttemptRepository attemptRepository;
    private final LoanRepository loanRepository;
    private final ContactTimeService contactTimeService;

    public ContactAttemptController(
            ContactAttemptRepository attemptRepository,
            LoanRepository loanRepository,
            ContactTimeService contactTimeService) {

        this.attemptRepository = attemptRepository;
        this.loanRepository = loanRepository;
        this.contactTimeService = contactTimeService;
    }

    // Authorize contact and record the attempt
    @PostMapping("/authorize")
    public ResponseEntity<?> authorizeContact(
            @RequestBody ContactRequest request) {

        try {
            Loan loan = loanRepository.findById(request.loanId())
                    .orElseThrow(() ->
                            new RuntimeException("Loan not found"));

            if (loan.getAssignedOfficerId() == null ||
                    !loan.getAssignedOfficerId()
                            .equals(request.officerId())) {

                return ResponseEntity.badRequest().body(
                        Map.of("message",
                                "Loan is not assigned to this officer")
                );
            }

            contactTimeService.validateAndRecord(
                    loan,
                    request.officerId()
            );

            return ResponseEntity.ok(
                    Map.of(
                            "allowed", true,
                            "message", "Contact is allowed. You may proceed with the call."
                    )
            );

        } catch (RuntimeException e) {

            boolean blocked = e.getMessage() != null &&
                    e.getMessage().contains("8 AM");

            return ResponseEntity
                    .status(blocked ? 403 : 400)
                    .body(Map.of(
                            "allowed", false,
                            "message", e.getMessage()
                    ));
        }
    }

    public record ContactRequest(
            Long loanId,
            Long officerId
    ) {}

    // Get all attempts
    @GetMapping
    public ResponseEntity<List<ContactAttempt>> getAllAttempts() {
        return ResponseEntity.ok(attemptRepository.findAll());
    }

    // Get attempts by loan
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<ContactAttempt>> getByLoan(
            @PathVariable Long loanId) {

        return ResponseEntity.ok(
                attemptRepository.findByLoanIdOrderByAttemptedAtDesc(
                        loanId)
        );
    }

    // Get attempts by officer
    @GetMapping("/officer/{officerId}")
    public ResponseEntity<List<ContactAttempt>> getByOfficer(
            @PathVariable Long officerId) {

        return ResponseEntity.ok(
                attemptRepository.findByOfficerIdOrderByAttemptedAtDesc(
                        officerId)
        );
    }

    // Get blocked attempts
    @GetMapping("/blocked")
    public ResponseEntity<List<ContactAttempt>> getBlockedAttempts() {
        return ResponseEntity.ok(
                attemptRepository.findByAttemptStatus("BLOCKED")
        );
    }
}
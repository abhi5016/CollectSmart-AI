package com.collectsmart.controller;

import com.collectsmart.config.AuthenticatedUser;
import com.collectsmart.entity.ContactAttempt;
import com.collectsmart.entity.Loan;
import com.collectsmart.repository.ContactAttemptRepository;
import com.collectsmart.repository.LoanRepository;
import com.collectsmart.service.ContactTimeService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

// Authorize contact using authenticated officer identity
@PostMapping("/authorize")
@PreAuthorize("hasRole('FIELD_OFFICER')")
public ResponseEntity<?> authorizeContact(
        @RequestBody ContactRequest request,
        @AuthenticationPrincipal AuthenticatedUser user) {

    try {
        if (user == null || user.fieldOfficerId() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "allowed", false,
                            "message", "Your account is not linked to a field officer."
                    ));
        }

        Long authenticatedOfficerId = user.fieldOfficerId();

        Loan loan = loanRepository.findById(request.loanId())
                .orElseThrow(() ->
                        new RuntimeException("Loan not found"));

        if (loan.getAssignedOfficerId() == null
                || !loan.getAssignedOfficerId().equals(authenticatedOfficerId)) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "allowed", false,
                            "message", "You are not authorized to contact this borrower."
                    ));
        }

        contactTimeService.validateAndRecord(
                loan,
                authenticatedOfficerId
        );

        return ResponseEntity.ok(
                Map.of(
                        "allowed", true,
                        "message", "Contact is allowed. You may proceed with the call."
                )
        );

    } catch (RuntimeException exception) {

        String errorMessage = exception.getMessage() != null
                ? exception.getMessage()
                : "Unable to authorize contact.";

        boolean blocked = errorMessage.contains("8 AM");

        return ResponseEntity
                .status(blocked ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "allowed", false,
                        "message", errorMessage
                ));
    }
}

// Keep officerId in the request for frontend compatibility.
// The backend deliberately ignores it and uses the JWT identity.
public record ContactRequest(
        Long loanId,
        Long officerId
) {}

// Manager-only: get all contact attempts
@GetMapping
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<List<ContactAttempt>> getAllAttempts() {
    return ResponseEntity.ok(attemptRepository.findAll());
}

// Manager can view any loan attempts; officer can view their own.
@GetMapping("/loan/{loanId}")
@PreAuthorize("hasAnyRole('MANAGER', 'FIELD_OFFICER')")
public ResponseEntity<?> getByLoan(
        @PathVariable Long loanId,
        @AuthenticationPrincipal AuthenticatedUser user) {

    Loan loan = loanRepository.findById(loanId).orElse(null);

    if (loan == null) {
        return ResponseEntity.notFound().build();
    }

    if ("FIELD_OFFICER".equals(user.role())
            && (user.fieldOfficerId() == null
            || !user.fieldOfficerId().equals(loan.getAssignedOfficerId()))) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "You cannot view another officer's contact attempts."));
    }

    return ResponseEntity.ok(
            attemptRepository.findByLoanIdOrderByAttemptedAtDesc(loanId)
    );
}

// Manager can view any officer; field officer can view only their own.
@GetMapping("/officer/{officerId}")
@PreAuthorize("hasAnyRole('MANAGER', 'FIELD_OFFICER')")
public ResponseEntity<?> getByOfficer(
        @PathVariable Long officerId,
        @AuthenticationPrincipal AuthenticatedUser user) {

    if ("FIELD_OFFICER".equals(user.role())
            && (user.fieldOfficerId() == null
            || !user.fieldOfficerId().equals(officerId))) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "You cannot view another officer's contact attempts."));
    }

    return ResponseEntity.ok(
            attemptRepository.findByOfficerIdOrderByAttemptedAtDesc(officerId)
    );
}

// Manager-only: view blocked attempts
@GetMapping("/blocked")
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<List<ContactAttempt>> getBlockedAttempts() {
    return ResponseEntity.ok(
            attemptRepository.findByAttemptStatus("BLOCKED")
    );
}

}
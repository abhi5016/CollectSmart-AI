package com.collectsmart.controller;

import com.collectsmart.config.AuthenticatedUser;
import com.collectsmart.entity.Loan;
import com.collectsmart.repository.LoanRepository;
import com.collectsmart.service.LoanPriorityService;
import com.collectsmart.service.LoanAssignmentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*")
public class LoanController {

    private final LoanRepository loanRepository;
    private final LoanPriorityService loanPriorityService;
    private final LoanAssignmentService loanAssignmentService;

    public LoanController(
            LoanRepository loanRepository,
            LoanPriorityService loanPriorityService,
            LoanAssignmentService loanAssignmentService) {

        this.loanRepository = loanRepository;
        this.loanPriorityService = loanPriorityService;
        this.loanAssignmentService = loanAssignmentService;
    }

    // Manager: Get all loans
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<Loan>> getAllLoans() {
        return ResponseEntity.ok(loanRepository.findAll());
    }

    // Manager: Get loan by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
        return loanRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Manager: Create loan
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> createLoan(@RequestBody Loan loan) {

        if (loanRepository.existsByLoanAccountNumber(
                loan.getLoanAccountNumber())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Loan account already exists."));
        }

        Loan savedLoan = loanRepository.save(loan);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedLoan);
    }

    // Manager: Get overdue loans
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<Loan>> getOverdueLoans() {
        return ResponseEntity.ok(loanRepository.findByStatus("OVERDUE"));
    }

    // Manager or the officer viewing their own loans
    @GetMapping("/officer/{officerId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'FIELD_OFFICER')")
    public ResponseEntity<?> getLoansByOfficer(
            @PathVariable Long officerId,
            @AuthenticationPrincipal AuthenticatedUser user) {

        if ("FIELD_OFFICER".equals(user.role())) {
            if (user.fieldOfficerId() == null ||
                    !user.fieldOfficerId().equals(officerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message",
                                "You cannot view another officer's loans."));
            }
        }

        return ResponseEntity.ok(
                loanRepository.findByAssignedOfficerId(officerId));
    }

    // Manager: Filter by pincode
    @GetMapping("/pincode/{pincode}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<Loan>> getLoansByPincode(
            @PathVariable String pincode) {

        return ResponseEntity.ok(loanRepository.findByPincode(pincode));
    }

    // Manager: Calculate individual priority
    @PostMapping("/{id}/calculate-priority")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> calculatePriority(@PathVariable Long id) {

        try {
            return ResponseEntity.ok(
                    loanPriorityService.calculatePriority(id));

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", exception.getMessage()));
        }
    }

    // Manager: Calculate all priorities
    @PostMapping("/calculate-all-priorities")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<Loan>> calculateAllPriorities() {

        List<Loan> overdueLoans =
                loanRepository.findByStatus("OVERDUE");

        List<Loan> updatedLoans = overdueLoans.stream()
                .map(loan ->
                        loanPriorityService.calculatePriority(loan.getId()))
                .toList();

        return ResponseEntity.ok(updatedLoans);
    }

    // Manager: Automatically assign loan
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> assignLoan(@PathVariable Long id) {

        try {
            Loan loan = loanAssignmentService.assignLoan(id);
            return ResponseEntity.ok(loan);

        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            exception.getMessage() != null
                                    ? exception.getMessage()
                                    : "Unable to assign loan."
                    ));
        }
    }
}
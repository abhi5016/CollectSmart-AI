package com.collectsmart.controller;

import com.collectsmart.entity.Loan;
import com.collectsmart.repository.LoanRepository;
import com.collectsmart.service.LoanPriorityService;
import com.collectsmart.service.LoanAssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*")
public class LoanController {

    private final LoanRepository loanRepository;
    private final LoanPriorityService loanPriorityService;
    private final LoanAssignmentService loanAssignmentService;

    // Constructor injection
   public LoanController(
        LoanRepository loanRepository,
        LoanPriorityService loanPriorityService,
        LoanAssignmentService loanAssignmentService) {

    this.loanRepository = loanRepository;
    this.loanPriorityService = loanPriorityService;
    this.loanAssignmentService = loanAssignmentService;
}

    // 1. Get all loans
    @GetMapping
    public ResponseEntity<List<Loan>> getAllLoans() {

        List<Loan> loans = loanRepository.findAll();

        return ResponseEntity.ok(loans);
    }

    // 2. Get loan by ID
    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {

        return loanRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Create a new loan
    @PostMapping
    public ResponseEntity<Loan> createLoan(@RequestBody Loan loan) {

        if (loanRepository.existsByLoanAccountNumber(
                loan.getLoanAccountNumber())) {

            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Loan savedLoan = loanRepository.save(loan);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedLoan);
    }

    // 4. Get all overdue loans
    @GetMapping("/overdue")
    public ResponseEntity<List<Loan>> getOverdueLoans() {

        List<Loan> loans = loanRepository.findByStatus("OVERDUE");

        return ResponseEntity.ok(loans);
    }

    // 5. Get loans assigned to a field officer
    @GetMapping("/officer/{officerId}")
    public ResponseEntity<List<Loan>> getLoansByOfficer(
            @PathVariable Long officerId) {

        List<Loan> loans =
                loanRepository.findByAssignedOfficerId(officerId);

        return ResponseEntity.ok(loans);
    }

    // 6. Get loans by pincode
    @GetMapping("/pincode/{pincode}")
    public ResponseEntity<List<Loan>> getLoansByPincode(
            @PathVariable String pincode) {

        List<Loan> loans = loanRepository.findByPincode(pincode);

        return ResponseEntity.ok(loans);
    }

    // 7. Calculate AI priority score for a loan
    @PostMapping("/{id}/calculate-priority")
    public ResponseEntity<Loan> calculatePriority(
            @PathVariable Long id) {

        try {
            Loan loan = loanPriorityService.calculatePriority(id);

            return ResponseEntity.ok(loan);

        } catch (RuntimeException exception) {

            return ResponseEntity.notFound().build();
        }
    }

    // 8. Calculate priority for all overdue loans
    @PostMapping("/calculate-all-priorities")
    public ResponseEntity<List<Loan>> calculateAllPriorities() {

        List<Loan> overdueLoans =
                loanRepository.findByStatus("OVERDUE");

        List<Loan> updatedLoans = overdueLoans.stream()
                .map(loan ->
                        loanPriorityService.calculatePriority(loan.getId()))
                .toList();

        return ResponseEntity.ok(updatedLoans);
    }

    // Automatically assign loan to a field officer
@PostMapping("/{id}/assign")
public ResponseEntity<?> assignLoan(@PathVariable Long id) {

    try {
        Loan loan = loanAssignmentService.assignLoan(id);
        return ResponseEntity.ok(loan);

    } catch (RuntimeException exception) {
        return ResponseEntity.badRequest()
                .body(exception.getMessage());
    }
  }
}
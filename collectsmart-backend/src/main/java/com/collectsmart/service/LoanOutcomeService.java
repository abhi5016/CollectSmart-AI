package com.collectsmart.service;

import com.collectsmart.entity.Loan;
import com.collectsmart.entity.LoanOutcome;
import com.collectsmart.repository.LoanOutcomeRepository;
import com.collectsmart.repository.LoanRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanOutcomeService {

private final LoanOutcomeRepository outcomeRepository;
private final LoanRepository loanRepository;
private final ContactTimeService contactTimeService;

public LoanOutcomeService(
        LoanOutcomeRepository outcomeRepository,
        LoanRepository loanRepository,
        ContactTimeService contactTimeService) {

    this.outcomeRepository = outcomeRepository;
    this.loanRepository = loanRepository;
    this.contactTimeService = contactTimeService;
}

// Save outcome for an authenticated field officer
public LoanOutcome saveOutcome(
        Long loanId,
        Long officerId,
        String outcomeType,
        Double latitude,
        Double longitude,
        String notes) {

    List<String> validOutcomes = List.of(
            "PAID",
            "PROMISE_TO_PAY",
            "NOT_REACHABLE",
            "DISPUTE"
    );

    if (outcomeType == null
            || !validOutcomes.contains(outcomeType.toUpperCase())) {

        throw new RuntimeException("Invalid outcome type");
    }

    if (officerId == null) {
        throw new RuntimeException("Invalid field officer account");
    }

    Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() ->
                    new RuntimeException("Loan not found"));

    // Verify that the authenticated officer owns this loan
    if (loan.getAssignedOfficerId() == null
            || !loan.getAssignedOfficerId().equals(officerId)) {

        throw new RuntimeException(
                "This loan is not assigned to your account"
        );
    }

    // Enforce permitted contact hours and record attempt
    contactTimeService.validateAndRecord(loan, officerId);

    LoanOutcome outcome = new LoanOutcome();

    outcome.setLoan(loan);
    outcome.setFieldOfficerId(officerId);
    outcome.setOutcomeType(outcomeType.toUpperCase());
    outcome.setLatitude(latitude);
    outcome.setLongitude(longitude);
    outcome.setNotes(notes);

    return outcomeRepository.save(outcome);
}

// Get outcomes for a loan with ownership validation
public List<LoanOutcome> getOutcomesByLoan(
        Long loanId,
        String role,
        Long officerId) {

    Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() ->
                    new RuntimeException("Loan not found"));

    if ("FIELD_OFFICER".equals(role)) {

        if (officerId == null
                || loan.getAssignedOfficerId() == null
                || !loan.getAssignedOfficerId().equals(officerId)) {

            throw new RuntimeException(
                    "You cannot view outcomes for another officer's loan"
            );
        }

    } else if (!"MANAGER".equals(role)) {
        throw new RuntimeException("Access denied");
    }

    return outcomeRepository.findByLoanId(loanId);
}

// Get outcomes by officer
public List<LoanOutcome> getOutcomesByOfficer(Long officerId) {

    if (officerId == null) {
        throw new RuntimeException("Invalid officer ID");
    }

    return outcomeRepository.findByFieldOfficerId(officerId);
}

}
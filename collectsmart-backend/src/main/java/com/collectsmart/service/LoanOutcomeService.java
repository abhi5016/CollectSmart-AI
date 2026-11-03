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

    // Save loan outcome
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

        if (outcomeType == null ||
                !validOutcomes.contains(outcomeType.toUpperCase())) {
            throw new RuntimeException("Invalid outcome type");
        }

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found"));

        if (loan.getAssignedOfficerId() == null ||
                !loan.getAssignedOfficerId().equals(officerId)) {
            throw new RuntimeException(
                    "This loan is not assigned to this officer");
        }

        // Check permitted contact hours
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

    // Get outcomes for a loan
    public List<LoanOutcome> getOutcomesByLoan(Long loanId) {

        if (!loanRepository.existsById(loanId)) {
            throw new RuntimeException("Loan not found");
        }

        return outcomeRepository.findByLoanId(loanId);
    }

    // Get outcomes by officer
    public List<LoanOutcome> getOutcomesByOfficer(Long officerId) {

        return outcomeRepository.findByFieldOfficerId(officerId);
    }
}
package com.collectsmart.service;

import com.collectsmart.entity.Loan;
import com.collectsmart.entity.FieldOfficer;
import com.collectsmart.repository.LoanRepository;
import com.collectsmart.repository.FieldOfficerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class LoanAssignmentService {

    private final LoanRepository loanRepository;
    private final FieldOfficerRepository officerRepository;

    public LoanAssignmentService(
            LoanRepository loanRepository,
            FieldOfficerRepository officerRepository) {

        this.loanRepository = loanRepository;
        this.officerRepository = officerRepository;
    }

    @Transactional
    public Loan assignLoan(Long loanId) {

        // Find loan
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found"));

        if (loan.getPincode() == null ||
                loan.getPincode().isBlank()) {
            throw new RuntimeException("Loan pincode is missing");
        }

        // Find active officers in the same pincode
        List<FieldOfficer> officers =
                officerRepository.findByPincodeAndActiveTrue(
                        loan.getPincode());

        if (officers.isEmpty()) {
            throw new RuntimeException(
                    "No active officers available for this pincode");
        }

        // Select officer with the lowest workload ratio
        FieldOfficer selectedOfficer = officers.stream()
                .filter(officer -> officer.getMaxWorkload() != null
                        && officer.getMaxWorkload() > 0)
                .filter(officer ->
                        loanRepository.findByAssignedOfficerId(
                                officer.getId()).size()
                                < officer.getMaxWorkload())
                .min(Comparator.comparingDouble(officer -> {

                    int workload = loanRepository
                            .findByAssignedOfficerId(officer.getId())
                            .size();

                    return (double) workload /
                            officer.getMaxWorkload();
                }))
                .orElseThrow(() ->
                        new RuntimeException(
                                "All officers have reached maximum workload"));

        // Assign loan
        loan.setAssignedOfficerId(selectedOfficer.getId());

        return loanRepository.save(loan);
    }
}
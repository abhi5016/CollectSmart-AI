package com.collectsmart.service;

import com.collectsmart.entity.Loan;
import com.collectsmart.repository.LoanRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoanPriorityService {

    private final LoanRepository loanRepository;

    public LoanPriorityService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public Loan calculatePriority(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found with ID: " + loanId));

        int score = 0;
        List<String> reasons = new ArrayList<>();

        // 1. Calculate priority based on days past due
        int days = loan.getDaysPastDue() == null
                ? 0 : loan.getDaysPastDue();

        if (days > 30) {
            score += 40;
            reasons.add("Loan overdue for more than 30 days");
        } else if (days > 15) {
            score += 30;
            reasons.add("Loan overdue for more than 15 days");
        } else if (days > 7) {
            score += 20;
            reasons.add("Loan overdue for more than 7 days");
        } else {
            score += 10;
            reasons.add("Loan is recently overdue");
        }

        // 2. Calculate priority based on overdue amount
        BigDecimal overdue = loan.getOverdueAmount() == null
                ? BigDecimal.ZERO : loan.getOverdueAmount();

        if (overdue.compareTo(new BigDecimal("50000")) >= 0) {
            score += 30;
            reasons.add("High overdue amount");
        } else if (overdue.compareTo(new BigDecimal("15000")) >= 0) {
            score += 22;
            reasons.add("Significant overdue amount");
        } else if (overdue.compareTo(new BigDecimal("5000")) >= 0) {
            score += 15;
            reasons.add("Moderate overdue amount");
        } else {
            score += 5;
            reasons.add("Low overdue amount");
        }

        // 3. Calculate priority based on overdue-to-loan ratio
        BigDecimal loanAmount = loan.getLoanAmount() == null
                ? BigDecimal.ZERO : loan.getLoanAmount();

        if (loanAmount.compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal ratio = overdue
                    .multiply(new BigDecimal("100"))
                    .divide(loanAmount, 2, java.math.RoundingMode.HALF_UP);

            if (ratio.compareTo(new BigDecimal("50")) > 0) {
                score += 30;
                reasons.add("Overdue amount exceeds 50% of loan amount");
            } else if (ratio.compareTo(new BigDecimal("25")) > 0) {
                score += 22;
                reasons.add("Overdue amount exceeds 25% of loan amount");
            } else if (ratio.compareTo(new BigDecimal("10")) > 0) {
                score += 15;
                reasons.add("Overdue amount exceeds 10% of loan amount");
            } else {
                score += 5;
                reasons.add("Low overdue-to-loan ratio");
            }
        } else {
            reasons.add("Loan amount unavailable; ratio not calculated");
        }

        // Save score and reasons
        loan.setPriorityScore((double) Math.min(score, 100));

        // Store top 3 reasons
        loan.setPriorityReasons(String.join("; ", reasons.subList(0, 3)));

        return loanRepository.save(loan);
    }
}
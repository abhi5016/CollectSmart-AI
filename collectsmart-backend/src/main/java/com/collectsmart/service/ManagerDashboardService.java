package com.collectsmart.service;

import com.collectsmart.entity.Loan;
import com.collectsmart.repository.LoanRepository;
import com.collectsmart.repository.PromiseToPayRepository;
import com.collectsmart.repository.ContactAttemptRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ManagerDashboardService {

    private final LoanRepository loanRepository;
    private final PromiseToPayRepository promiseRepository;
    private final ContactAttemptRepository attemptRepository;

    public ManagerDashboardService(
            LoanRepository loanRepository,
            PromiseToPayRepository promiseRepository,
            ContactAttemptRepository attemptRepository) {

        this.loanRepository = loanRepository;
        this.promiseRepository = promiseRepository;
        this.attemptRepository = attemptRepository;
    }

    // Main dashboard summary
    public Map<String, Object> getDashboardSummary() {

        List<Loan> loans = loanRepository.findAll();

        long totalLoans = loans.size();

        long overdueLoans = loans.stream()
                .filter(loan -> "OVERDUE".equalsIgnoreCase(
                        loan.getStatus()))
                .count();

        long assignedLoans = loans.stream()
                .filter(loan -> loan.getAssignedOfficerId() != null)
                .count();

        long unassignedLoans = totalLoans - assignedLoans;

        BigDecimal totalOverdueAmount = loans.stream()
                .filter(loan -> "OVERDUE".equalsIgnoreCase(
                        loan.getStatus()))
                .map(Loan::getOverdueAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingPromises = promiseRepository
                .findByStatus("PENDING").size();

        long brokenPromises = promiseRepository
                .findByStatus("BROKEN").size();

        long keptPromises = promiseRepository
                .findByStatus("KEPT").size();

        // Count blocked contact attempts
        long blockedAttempts = attemptRepository
                .findByAttemptStatus("BLOCKED").size();

        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("totalLoans", totalLoans);
        dashboard.put("overdueLoans", overdueLoans);
        dashboard.put("assignedLoans", assignedLoans);
        dashboard.put("unassignedLoans", unassignedLoans);
        dashboard.put("totalOverdueAmount", totalOverdueAmount);
        dashboard.put("pendingPromises", pendingPromises);
        dashboard.put("brokenPromises", brokenPromises);
        dashboard.put("keptPromises", keptPromises);
        dashboard.put("blockedContactAttempts", blockedAttempts);

        return dashboard;
    }

    // Loan aging distribution
    public Map<String, Long> getLoanAgingReport() {

        List<Loan> loans = loanRepository.findAll();

        Map<String, Long> aging = new HashMap<>();

        aging.put("1-30 Days", 0L);
        aging.put("31-60 Days", 0L);
        aging.put("61-90 Days", 0L);
        aging.put("Above 90 Days", 0L);

        for (Loan loan : loans) {

            if (!"OVERDUE".equalsIgnoreCase(loan.getStatus())) {
                continue;
            }

            int days = loan.getDaysPastDue() == null
                    ? 0 : loan.getDaysPastDue();

            if (days <= 30) {
                aging.merge("1-30 Days", 1L, Long::sum);
            } else if (days <= 60) {
                aging.merge("31-60 Days", 1L, Long::sum);
            } else if (days <= 90) {
                aging.merge("61-90 Days", 1L, Long::sum);
            } else {
                aging.merge("Above 90 Days", 1L, Long::sum);
            }
        }

        return aging;
    }
}
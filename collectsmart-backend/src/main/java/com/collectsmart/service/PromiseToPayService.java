package com.collectsmart.service;

import com.collectsmart.entity.Loan;
import com.collectsmart.entity.PromiseToPay;
import com.collectsmart.repository.LoanRepository;
import com.collectsmart.repository.PromiseToPayRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PromiseToPayService {

    private final PromiseToPayRepository promiseRepository;
    private final LoanRepository loanRepository;

    public PromiseToPayService(
            PromiseToPayRepository promiseRepository,
            LoanRepository loanRepository) {

        this.promiseRepository = promiseRepository;
        this.loanRepository = loanRepository;
    }

    // Create a new payment promise
    public PromiseToPay createPromise(
            Long loanId,
            Long officerId,
            BigDecimal promisedAmount,
            LocalDate promisedDate) {

        // Validate amount
        if (promisedAmount == null ||
                promisedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(
                    "Promised amount must be greater than zero");
        }

        // Validate promised date
        if (promisedDate == null ||
                promisedDate.isBefore(LocalDate.now())) {
            throw new RuntimeException(
                    "Promised date cannot be in the past");
        }

        // Find loan
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found"));

        // Verify officer assignment
        if (loan.getAssignedOfficerId() == null ||
                !loan.getAssignedOfficerId().equals(officerId)) {
            throw new RuntimeException(
                    "This loan is not assigned to this officer");
        }

        // Validate amount against overdue amount
        if (promisedAmount.compareTo(loan.getOverdueAmount()) > 0) {
            throw new RuntimeException(
                    "Promised amount cannot exceed overdue amount");
        }

        // Create promise
        PromiseToPay promise = new PromiseToPay();

        promise.setLoan(loan);
        promise.setFieldOfficerId(officerId);
        promise.setPromisedAmount(promisedAmount);
        promise.setPromisedDate(promisedDate);
        promise.setStatus("PENDING");

        return promiseRepository.save(promise);
    }

    // Get promises for a loan
    public List<PromiseToPay> getPromisesByLoan(Long loanId) {
        return promiseRepository.findByLoanId(loanId);
    }

    // Get promises by officer
    public List<PromiseToPay> getPromisesByOfficer(Long officerId) {
        return promiseRepository.findByFieldOfficerId(officerId);
    }

    // Get promises by status
    public List<PromiseToPay> getPromisesByStatus(String status) {
        return promiseRepository.findByStatus(status.toUpperCase());
    }

    // Get all pending promises
    public List<PromiseToPay> getPendingPromises() {
        return promiseRepository
                .findByStatusOrderByPromisedDateAsc("PENDING");
    }

    // Update promise status
    public PromiseToPay updatePromiseStatus(
            Long promiseId,
            String status) {

        List<String> validStatuses = List.of(
                "PENDING", "KEPT", "BROKEN"
        );

        if (status == null ||
                !validStatuses.contains(status.toUpperCase())) {
            throw new RuntimeException("Invalid promise status");
        }

        PromiseToPay promise = promiseRepository.findById(promiseId)
                .orElseThrow(() ->
                        new RuntimeException("Promise not found"));

        promise.setStatus(status.toUpperCase());

        return promiseRepository.save(promise);
    }

    // Automatically mark overdue pending promises as broken
    public int markBrokenPromises() {

        List<PromiseToPay> pendingPromises =
                getPendingPromises();

        int updatedCount = 0;

        for (PromiseToPay promise : pendingPromises) {

            if (promise.getPromisedDate().isBefore(LocalDate.now())) {
                promise.setStatus("BROKEN");
                promiseRepository.save(promise);
                updatedCount++;
            }
        }

        return updatedCount;
    }
}
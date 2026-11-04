package com.collectsmart.service;

import com.collectsmart.entity.Loan;
import com.collectsmart.entity.PromiseToPay;
import com.collectsmart.repository.LoanRepository;
import com.collectsmart.repository.PromiseToPayRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
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

// Create promise using authenticated officer ID
public PromiseToPay createPromise(
        Long loanId,
        Long officerId,
        BigDecimal promisedAmount,
        LocalDate promisedDate) {

    if (officerId == null) {
        throw new RuntimeException("Invalid field officer account.");
    }

    if (promisedAmount == null ||
            promisedAmount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new RuntimeException(
                "Promised amount must be greater than zero.");
    }

    if (promisedDate == null ||
            promisedDate.isBefore(LocalDate.now())) {
        throw new RuntimeException(
                "Promised date cannot be in the past.");
    }

    Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() ->
                    new RuntimeException("Loan not found."));

    if (loan.getAssignedOfficerId() == null ||
            !loan.getAssignedOfficerId().equals(officerId)) {
        throw new SecurityException(
                "This loan is not assigned to your account.");
    }

    if (loan.getOverdueAmount() == null ||
            promisedAmount.compareTo(loan.getOverdueAmount()) > 0) {
        throw new RuntimeException(
                "Promised amount cannot exceed overdue amount.");
    }

    PromiseToPay promise = new PromiseToPay();

    promise.setLoan(loan);
    promise.setFieldOfficerId(officerId);
    promise.setPromisedAmount(promisedAmount);
    promise.setPromisedDate(promisedDate);
    promise.setStatus("PENDING");

    return promiseRepository.save(promise);
}

// Get promises for a loan with ownership verification
public List<PromiseToPay> getPromisesByLoan(
        Long loanId,
        String role,
        Long officerId) {

    Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() ->
                    new RuntimeException("Loan not found."));

    if ("FIELD_OFFICER".equals(role)) {

        if (officerId == null ||
                loan.getAssignedOfficerId() == null ||
                !loan.getAssignedOfficerId().equals(officerId)) {

            throw new SecurityException(
                    "You cannot view promises for another officer's loan.");
        }

        return promiseRepository.findByLoanId(loanId)
                .stream()
                .filter(p -> officerId.equals(p.getFieldOfficerId()))
                .toList();
    }

    if (!"MANAGER".equals(role)) {
        throw new SecurityException("Access denied.");
    }

    return promiseRepository.findByLoanId(loanId);
}

// Get promises by officer
public List<PromiseToPay> getPromisesByOfficer(
        Long requestedOfficerId,
        String role,
        Long authenticatedOfficerId) {

    if ("FIELD_OFFICER".equals(role)) {

        if (authenticatedOfficerId == null ||
                !authenticatedOfficerId.equals(requestedOfficerId)) {

            throw new SecurityException(
                    "You cannot view another officer's promises.");
        }
    } else if (!"MANAGER".equals(role)) {
        throw new SecurityException("Access denied.");
    }

    return promiseRepository.findByFieldOfficerId(requestedOfficerId);
}

// Get promises by status
public List<PromiseToPay> getPromisesByStatus(
        String status,
        String role,
        Long officerId) {

    if (!"MANAGER".equals(role)) {
        throw new SecurityException(
                "Only managers can view promises by status.");
    }

    if (status == null || status.isBlank()) {
        throw new RuntimeException("Status is required.");
    }

    return promiseRepository.findByStatus(status.toUpperCase());
}

// Get pending promises
public List<PromiseToPay> getPendingPromises(
        String role,
        Long officerId) {

    if ("MANAGER".equals(role)) {
        return promiseRepository
                .findByStatusOrderByPromisedDateAsc("PENDING");
    }

    if ("FIELD_OFFICER".equals(role)) {

        if (officerId == null) {
            throw new SecurityException(
                    "Your account is not linked to a field officer.");
        }

        return promiseRepository.findByFieldOfficerId(officerId)
                .stream()
                .filter(p -> "PENDING".equalsIgnoreCase(p.getStatus()))
                .sorted(Comparator.comparing(
                        PromiseToPay::getPromisedDate))
                .toList();
    }

    throw new SecurityException("Access denied.");
}

// Update promise status (manager only)
public PromiseToPay updatePromiseStatus(
        Long promiseId,
        String status) {

    List<String> validStatuses = List.of(
            "PENDING", "KEPT", "BROKEN"
    );

    if (status == null ||
            !validStatuses.contains(status.toUpperCase())) {
        throw new RuntimeException("Invalid promise status.");
    }

    PromiseToPay promise = promiseRepository.findById(promiseId)
            .orElseThrow(() ->
                    new RuntimeException("Promise not found."));

    promise.setStatus(status.toUpperCase());

    return promiseRepository.save(promise);
}

// Automatically mark expired promises as broken
public int markBrokenPromises() {

    List<PromiseToPay> pendingPromises =
            promiseRepository
                    .findByStatusOrderByPromisedDateAsc("PENDING");

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
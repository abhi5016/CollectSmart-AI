package com.collectsmart.repository;

import com.collectsmart.entity.LoanOutcome;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanOutcomeRepository
        extends JpaRepository<LoanOutcome, Long> {

    List<LoanOutcome> findByLoanId(Long loanId);

    List<LoanOutcome> findByFieldOfficerId(Long fieldOfficerId);
}
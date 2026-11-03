package com.collectsmart.repository;

import com.collectsmart.entity.LoanAgingSnapshot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanAgingSnapshotRepository
        extends JpaRepository<LoanAgingSnapshot, Long> {

    // Get all snapshots for a loan
    List<LoanAgingSnapshot> findByLoanId(Long loanId);

    // Get snapshots for a particular date
    List<LoanAgingSnapshot> findBySnapshotDate(LocalDate snapshotDate);

    // Get a loan's snapshot on a particular date
    Optional<LoanAgingSnapshot> findByLoanIdAndSnapshotDate(
            Long loanId,
            LocalDate snapshotDate
    );

    // Get historical snapshots between two dates
    List<LoanAgingSnapshot> findBySnapshotDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );
}
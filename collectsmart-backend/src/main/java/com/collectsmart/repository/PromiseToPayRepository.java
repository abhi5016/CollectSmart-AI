package com.collectsmart.repository;

import com.collectsmart.entity.PromiseToPay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromiseToPayRepository
        extends JpaRepository<PromiseToPay, Long> {

    // Get promises for a particular loan
    List<PromiseToPay> findByLoanId(Long loanId);

    // Get promises recorded by an officer
    List<PromiseToPay> findByFieldOfficerId(Long officerId);

    // Get promises by status
    List<PromiseToPay> findByStatus(String status);

    // Get pending promises
    List<PromiseToPay> findByStatusOrderByPromisedDateAsc(
            String status
    );
}
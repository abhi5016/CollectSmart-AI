package com.collectsmart.repository;

import com.collectsmart.entity.ContactAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactAttemptRepository
        extends JpaRepository<ContactAttempt, Long> {

    List<ContactAttempt> findByOfficerIdOrderByAttemptedAtDesc(
            Long officerId
    );

    List<ContactAttempt> findByLoanIdOrderByAttemptedAtDesc(
            Long loanId
    );

    List<ContactAttempt> findByAttemptStatus(String attemptStatus);
}
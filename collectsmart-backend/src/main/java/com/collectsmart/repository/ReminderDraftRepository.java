package com.collectsmart.repository;

import com.collectsmart.entity.ReminderDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReminderDraftRepository
        extends JpaRepository<ReminderDraft, Long> {

    List<ReminderDraft> findByLoanId(Long loanId);

    List<ReminderDraft> findByFieldOfficerId(Long officerId);

    List<ReminderDraft> findByStatus(String status);
}
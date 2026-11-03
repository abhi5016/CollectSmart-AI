package com.collectsmart.service;

import com.collectsmart.entity.ContactAttempt;
import com.collectsmart.repository.ContactAttemptRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactWindowService {

    private final ContactAttemptRepository repository;

    public ContactWindowService(
            ContactAttemptRepository repository) {
        this.repository = repository;
    }

    public List<ContactAttempt> getOfficerAttempts(Long officerId) {
        return repository.findByOfficerIdOrderByAttemptedAtDesc(
                officerId
        );
    }

    public List<ContactAttempt> getLoanAttempts(Long loanId) {
        return repository.findByLoanIdOrderByAttemptedAtDesc(
                loanId
        );
    }
}
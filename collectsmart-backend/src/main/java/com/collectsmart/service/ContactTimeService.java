package com.collectsmart.service;

import com.collectsmart.entity.ContactAttempt;
import com.collectsmart.entity.Loan;
import com.collectsmart.repository.ContactAttemptRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@Service
public class ContactTimeService {

    private static final ZoneId INDIA_ZONE =
            ZoneId.of("Asia/Kolkata");

    private static final LocalTime START_TIME =
            LocalTime.of(8, 0);

    private static final LocalTime END_TIME =
            LocalTime.of(19, 0);

    private final ContactAttemptRepository attemptRepository;

    public ContactTimeService(
            ContactAttemptRepository attemptRepository) {
        this.attemptRepository = attemptRepository;
    }

    public boolean isContactAllowed() {

        LocalTime currentTime =
                LocalTime.now(INDIA_ZONE);

        return !currentTime.isBefore(START_TIME)
                && currentTime.isBefore(END_TIME);
    }

    public void validateAndRecord(Loan loan, Long officerId) {

        boolean allowed = isContactAllowed();

        ContactAttempt attempt = new ContactAttempt();

        attempt.setLoanId(loan.getId());
        attempt.setOfficerId(officerId);
        attempt.setAttemptedAt(
                LocalDateTime.now(INDIA_ZONE)
        );

        if (allowed) {
            attempt.setAttemptStatus("ALLOWED");
            attempt.setReason(
                    "Contact attempt within permitted hours."
            );
        } else {
            attempt.setAttemptStatus("BLOCKED");
            attempt.setReason(
                    "Contact blocked: Allowed hours are 8 AM to 7 PM IST."
            );
        }

        // Save the attempt
        attemptRepository.save(attempt);

        // Prevent contact outside permitted hours
        if (!allowed) {
            throw new RuntimeException(
                    "Contact is allowed only between 8 AM and 7 PM IST."
            );
        }
    }
}
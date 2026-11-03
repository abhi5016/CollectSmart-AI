package com.collectsmart.service;

import com.collectsmart.entity.Loan;
import com.collectsmart.entity.ReminderDraft;
import com.collectsmart.repository.LoanRepository;
import com.collectsmart.repository.ReminderDraftRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class ReminderDraftService {

    private final ReminderDraftRepository reminderRepository;
    private final LoanRepository loanRepository;

    public ReminderDraftService(
            ReminderDraftRepository reminderRepository,
            LoanRepository loanRepository) {

        this.reminderRepository = reminderRepository;
        this.loanRepository = loanRepository;
    }

    // Approved template phrases
    private static final List<String> BLOCKED_WORDS = List.of(
            "threat",
            "legal action",
            "arrest",
            "police",
            "harassment",
            "धमकी",
            "गिरफ्तार",
            "पुलिस"
    );

    // Generate payment reminder draft
    public ReminderDraft generateReminder(
            Long loanId,
            Long officerId,
            String language) {

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

        if (language == null) {
            throw new RuntimeException(
                    "Language is required");
        }

        language = language.toUpperCase(Locale.ROOT);

        String message;

        // English template
        if (language.equals("ENGLISH")) {

            message = "Dear " + loan.getBorrowerName()
                    + ", this is a friendly reminder regarding your "
                    + "overdue loan payment of Rs. "
                    + loan.getOverdueAmount()
                    + ". Kindly make the payment at your earliest "
                    + "convenience. Thank you.";

        }

        // Hindi template
        else if (language.equals("HINDI")) {

            message = "प्रिय " + loan.getBorrowerName()
                    + ", यह आपके बकाया ऋण भुगतान "
                    + loan.getOverdueAmount()
                    + " रुपये के संबंध में एक विनम्र अनुस्मारक है। "
                    + "कृपया अपनी सुविधानुसार जल्द भुगतान करें। धन्यवाद।";

        } else {

            throw new RuntimeException(
                    "Supported languages are ENGLISH and HINDI");
        }

        // Check threatening language
        validateMessage(message);

        // Create draft
        ReminderDraft reminder = new ReminderDraft();

        reminder.setLoan(loan);
        reminder.setFieldOfficerId(officerId);
        reminder.setLanguage(language);
        reminder.setMessage(message);
        reminder.setStatus("DRAFT");

        return reminderRepository.save(reminder);
    }

    // Validate message
    private void validateMessage(String message) {

        String lowerMessage =
                message.toLowerCase(Locale.ROOT);

        for (String word : BLOCKED_WORDS) {

            if (lowerMessage.contains(
                    word.toLowerCase(Locale.ROOT))) {

                throw new RuntimeException(
                        "Message contains prohibited language");
            }
        }
    }

    // Get reminders for a loan
    public List<ReminderDraft> getRemindersByLoan(Long loanId) {

        return reminderRepository.findByLoanId(loanId);
    }

    // Get reminders created by officer
    public List<ReminderDraft> getRemindersByOfficer(
            Long officerId) {

        return reminderRepository.findByFieldOfficerId(officerId);
    }

    // Get reminders by status
    public List<ReminderDraft> getRemindersByStatus(
            String status) {

        return reminderRepository.findByStatus(
                status.toUpperCase(Locale.ROOT));
    }
}
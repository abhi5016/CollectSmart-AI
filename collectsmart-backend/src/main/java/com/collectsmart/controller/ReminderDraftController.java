package com.collectsmart.controller;

import com.collectsmart.entity.ReminderDraft;
import com.collectsmart.service.ReminderDraftService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
public class ReminderDraftController {

    private final ReminderDraftService reminderService;

    public ReminderDraftController(
            ReminderDraftService reminderService) {
        this.reminderService = reminderService;
    }

    // Request body
    public record ReminderRequest(
            Long officerId,
            String language
    ) {}

    // Generate reminder draft
    @PostMapping("/loan/{loanId}/generate")
    public ResponseEntity<?> generateReminder(
            @PathVariable Long loanId,
            @RequestBody ReminderRequest request) {

        try {
            ReminderDraft reminder =
                    reminderService.generateReminder(
                            loanId,
                            request.officerId(),
                            request.language()
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(reminder);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Get reminders for a loan
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<ReminderDraft>> getByLoan(
            @PathVariable Long loanId) {

        return ResponseEntity.ok(
                reminderService.getRemindersByLoan(loanId)
        );
    }

    // Get reminders by officer
    @GetMapping("/officer/{officerId}")
    public ResponseEntity<List<ReminderDraft>> getByOfficer(
            @PathVariable Long officerId) {

        return ResponseEntity.ok(
                reminderService.getRemindersByOfficer(officerId)
        );
    }

    // Get reminders by status
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(
            @PathVariable String status) {

        try {
            return ResponseEntity.ok(
                    reminderService.getRemindersByStatus(status)
            );
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}
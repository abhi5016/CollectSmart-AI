package com.collectsmart.service;

import com.collectsmart.entity.Loan;
import com.collectsmart.repository.LoanRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class CsvImportService {

    private final LoanRepository loanRepository;
    private final LoanAssignmentService assignmentService;
    private final LoanPriorityService priorityService;

    public CsvImportService(
            LoanRepository loanRepository,
            LoanAssignmentService assignmentService,
            LoanPriorityService priorityService) {

        this.loanRepository = loanRepository;
        this.assignmentService = assignmentService;
        this.priorityService = priorityService;
    }

    public Map<String, Object> importLoans(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please upload a CSV file");
        }

        int imported = 0;
        int duplicates = 0;
        int failed = 0;
        int assigned = 0;

        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream()))) {

            String header = reader.readLine();

            if (header == null) {
                throw new RuntimeException("CSV file is empty");
            }

            String[] columns = header.split(",", -1);

            Map<String, Integer> columnIndex = new HashMap<>();

            for (int i = 0; i < columns.length; i++) {
                columnIndex.put(
                        columns[i].trim().replace("\uFEFF", ""),
                        i
                );
            }

            String[] requiredColumns = {
                    "loanAccountNumber",
                    "borrowerName",
                    "phoneNumber",
                    "pincode",
                    "loanAmount",
                    "overdueAmount",
                    "dueDate",
                    "daysPastDue",
                    "status"
            };

            for (String column : requiredColumns) {
                if (!columnIndex.containsKey(column)) {
                    throw new RuntimeException(
                            "Missing CSV column: " + column
                    );
                }
            }

            String line;
            int rowNumber = 1;

            while ((line = reader.readLine()) != null) {

                rowNumber++;

                if (line.isBlank()) {
                    continue;
                }

                try {

                    String[] values = line.split(",", -1);

                    String accountNumber = getValue(
                            values, columnIndex, "loanAccountNumber");

                    if (accountNumber.isBlank()) {
                        throw new RuntimeException(
                                "Loan account number is required");
                    }

                    if (loanRepository.existsByLoanAccountNumber(
                            accountNumber)) {

                        duplicates++;
                        continue;
                    }

                    Loan loan = new Loan();

                    loan.setLoanAccountNumber(accountNumber);

                    loan.setBorrowerName(getValue(
                            values, columnIndex, "borrowerName"));

                    loan.setPhoneNumber(getValue(
                            values, columnIndex, "phoneNumber"));

                    loan.setPincode(getValue(
                            values, columnIndex, "pincode"));

                    loan.setLoanAmount(new BigDecimal(
                            getValue(values, columnIndex, "loanAmount")));

                    loan.setOverdueAmount(new BigDecimal(
                            getValue(values, columnIndex, "overdueAmount")));

                    loan.setDueDate(LocalDate.parse(
                            getValue(values, columnIndex, "dueDate")));

                    loan.setDaysPastDue(Integer.parseInt(
                            getValue(values, columnIndex, "daysPastDue")));

                    loan.setStatus(getValue(
                            values, columnIndex, "status").toUpperCase());

                    Loan savedLoan = loanRepository.save(loan);

                    // Calculate AI-assisted priority
                    priorityService.calculatePriority(savedLoan.getId());

                    imported++;

                    // Automatically assign loan
                    try {
                        assignmentService.assignLoan(savedLoan.getId());
                        assigned++;
                    } catch (RuntimeException assignmentError) {
                        errors.add(
                                "Row " + rowNumber
                                + ": Imported but not assigned - "
                                + assignmentError.getMessage()
                        );
                    }

                } catch (Exception e) {

                    failed++;

                    errors.add(
                            "Row " + rowNumber + ": " + e.getMessage()
                    );
                }
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "CSV import failed: " + e.getMessage()
            );
        }

        Map<String, Object> result = new HashMap<>();

        result.put("imported", imported);
        result.put("duplicates", duplicates);
        result.put("failed", failed);
        result.put("assigned", assigned);
        result.put("errors", errors);

        return result;
    }

    private String getValue(
            String[] values,
            Map<String, Integer> columnIndex,
            String column) {

        int index = columnIndex.get(column);

        if (index >= values.length) {
            throw new RuntimeException(
                    "Missing value for " + column);
        }

        return values[index].trim();
    }
}
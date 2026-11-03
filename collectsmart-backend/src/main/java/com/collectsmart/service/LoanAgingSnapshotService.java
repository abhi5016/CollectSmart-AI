package com.collectsmart.service;

import com.collectsmart.entity.Loan;
import com.collectsmart.entity.LoanAgingSnapshot;
import com.collectsmart.repository.LoanRepository;
import com.collectsmart.repository.LoanAgingSnapshotRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoanAgingSnapshotService {

    private final LoanRepository loanRepository;
    private final LoanAgingSnapshotRepository snapshotRepository;

    public LoanAgingSnapshotService(
            LoanRepository loanRepository,
            LoanAgingSnapshotRepository snapshotRepository) {

        this.loanRepository = loanRepository;
        this.snapshotRepository = snapshotRepository;
    }

    // Determine loan aging bucket
    private String getAgingBucket(int days) {

        if (days <= 30) {
            return "1-30";
        } else if (days <= 60) {
            return "31-60";
        } else if (days <= 90) {
            return "61-90";
        } else {
            return "90+";
        }
    }

    // Capture daily snapshots
    @Transactional
    public int captureDailySnapshots() {

        LocalDate today = LocalDate.now();

        List<Loan> loans = loanRepository.findByStatus("OVERDUE");

        int savedCount = 0;

        for (Loan loan : loans) {

            // Avoid duplicate snapshot for the same loan and date
            if (snapshotRepository
                    .findByLoanIdAndSnapshotDate(
                            loan.getId(), today).isPresent()) {
                continue;
            }

            int days = loan.getDaysPastDue() == null
                    ? 0 : loan.getDaysPastDue();

            LoanAgingSnapshot snapshot = new LoanAgingSnapshot();

            snapshot.setLoan(loan);
            snapshot.setSnapshotDate(today);
            snapshot.setDaysPastDue(days);
            snapshot.setAgingBucket(getAgingBucket(days));

            snapshotRepository.save(snapshot);

            savedCount++;
        }

        return savedCount;
    }

    // Get historical snapshots for a loan
    public List<LoanAgingSnapshot> getLoanHistory(Long loanId) {

        if (!loanRepository.existsById(loanId)) {
            throw new RuntimeException("Loan not found");
        }

        return snapshotRepository.findByLoanId(loanId);
    }

    // Calculate roll rate between two dates
    public Map<String, Object> calculateRollRate(
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate == null || endDate == null ||
                startDate.isAfter(endDate)) {
            throw new RuntimeException("Invalid date range");
        }

        List<LoanAgingSnapshot> startSnapshots =
                snapshotRepository.findBySnapshotDate(startDate);

        List<LoanAgingSnapshot> endSnapshots =
                snapshotRepository.findBySnapshotDate(endDate);

        Map<Long, String> endBuckets = new HashMap<>();

        for (LoanAgingSnapshot snapshot : endSnapshots) {
            endBuckets.put(
                    snapshot.getLoan().getId(),
                    snapshot.getAgingBucket()
            );
        }

        Map<String, Integer> totalLoans = new HashMap<>();
        Map<String, Integer> rolledForward = new HashMap<>();

        for (LoanAgingSnapshot snapshot : startSnapshots) {

            String previousBucket = snapshot.getAgingBucket();

            totalLoans.merge(previousBucket, 1, Integer::sum);

            String currentBucket =
                    endBuckets.get(snapshot.getLoan().getId());

            if (currentBucket != null &&
                    getBucketRank(currentBucket) >
                    getBucketRank(previousBucket)) {

                rolledForward.merge(
                        previousBucket, 1, Integer::sum
                );
            }
        }

        Map<String, Object> result = new HashMap<>();

        Map<String, Double> rollRates = new HashMap<>();

        for (String bucket : List.of(
                "1-30", "31-60", "61-90", "90+")) {

            int total = totalLoans.getOrDefault(bucket, 0);

            int rolled = rolledForward.getOrDefault(bucket, 0);

            double percentage = total == 0
                    ? 0.0
                    : (rolled * 100.0) / total;

            rollRates.put(bucket, percentage);
        }

        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("totalLoansByBucket", totalLoans);
        result.put("rolledForwardByBucket", rolledForward);
        result.put("rollRatesPercentage", rollRates);

        return result;
    }

    private int getBucketRank(String bucket) {

        return switch (bucket) {
            case "1-30" -> 1;
            case "31-60" -> 2;
            case "61-90" -> 3;
            case "90+" -> 4;
            default -> 0;
        };
    }
}
package com.collectsmart.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "loan_aging_snapshots",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"loan_id", "snapshot_date"}
        )
    }
)
public class LoanAgingSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    private Integer daysPastDue;

    private String agingBucket;

    public Long getId() {
        return id;
    }

    public Loan getLoan() {
        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public Integer getDaysPastDue() {
        return daysPastDue;
    }

    public void setDaysPastDue(Integer daysPastDue) {
        this.daysPastDue = daysPastDue;
    }

    public String getAgingBucket() {
        return agingBucket;
    }

    public void setAgingBucket(String agingBucket) {
        this.agingBucket = agingBucket;
    }
}
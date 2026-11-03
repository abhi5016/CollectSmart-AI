package com.collectsmart.repository;

import com.collectsmart.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByStatus(String status);

    List<Loan> findByAssignedOfficerId(Long officerId);

    List<Loan> findByPincode(String pincode);

    boolean existsByLoanAccountNumber(String loanAccountNumber);
}
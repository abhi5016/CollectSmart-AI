package com.collectsmart.repository;

import com.collectsmart.entity.FieldOfficer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldOfficerRepository
        extends JpaRepository<FieldOfficer, Long> {

    List<FieldOfficer> findByPincodeAndActiveTrue(String pincode);

    List<FieldOfficer> findByActiveTrue();

    boolean existsByPhoneNumber(String phoneNumber);
}
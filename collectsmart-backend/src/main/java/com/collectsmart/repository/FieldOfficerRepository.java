package com.collectsmart.repository;

import com.collectsmart.entity.FieldOfficer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FieldOfficerRepository
        extends JpaRepository<FieldOfficer, Long> {

    boolean existsByPhoneNumber(String phoneNumber);

    List<FieldOfficer> findByActiveTrue();

    List<FieldOfficer> findByPincodeAndActiveTrue(String pincode);
}
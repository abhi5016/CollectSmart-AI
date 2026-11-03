package com.collectsmart.controller;

import com.collectsmart.entity.FieldOfficer;
import com.collectsmart.repository.FieldOfficerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/officers")
@CrossOrigin(origins = "*")
public class FieldOfficerController {

    private final FieldOfficerRepository officerRepository;

    public FieldOfficerController(FieldOfficerRepository officerRepository) {
        this.officerRepository = officerRepository;
    }

    // Register a new field officer
    @PostMapping
    public ResponseEntity<?> createOfficer(
            @RequestBody FieldOfficer officer) {

        if (officerRepository.existsByPhoneNumber(
                officer.getPhoneNumber())) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Phone number already registered");
        }

        FieldOfficer savedOfficer = officerRepository.save(officer);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedOfficer);
    }

    // Get all field officers
    @GetMapping
    public ResponseEntity<List<FieldOfficer>> getAllOfficers() {

        return ResponseEntity.ok(officerRepository.findAll());
    }

    // Get officer by ID
    @GetMapping("/{id}")
    public ResponseEntity<FieldOfficer> getOfficerById(
            @PathVariable Long id) {

        return officerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get active officers
    @GetMapping("/active")
    public ResponseEntity<List<FieldOfficer>> getActiveOfficers() {

        return ResponseEntity.ok(
                officerRepository.findByActiveTrue());
    }

    // Get officers by pincode
    @GetMapping("/pincode/{pincode}")
    public ResponseEntity<List<FieldOfficer>> getOfficersByPincode(
            @PathVariable String pincode) {

        return ResponseEntity.ok(
                officerRepository.findByPincodeAndActiveTrue(pincode));
    }
}
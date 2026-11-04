package com.collectsmart.service;

import com.collectsmart.entity.AppUser;
import com.collectsmart.repository.AppUserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitialUserSeeder implements CommandLineRunner {

private final AppUserRepository appUserRepository;
private final PasswordEncoder passwordEncoder;

@Value("${app.bootstrap.manager.email:}")
private String managerEmail;

@Value("${app.bootstrap.manager.password:}")
private String managerPassword;

@Value("${app.bootstrap.officer.email:}")
private String officerEmail;

@Value("${app.bootstrap.officer.password:}")
private String officerPassword;

@Value("${app.bootstrap.officer.id:}")
private String officerId;

// Officer 2 configuration
@Value("${app.bootstrap.officer2.email:}")
private String officer2Email;

@Value("${app.bootstrap.officer2.password:}")
private String officer2Password;

@Value("${app.bootstrap.officer2.id:}")
private String officer2Id;

public InitialUserSeeder(
        AppUserRepository appUserRepository,
        PasswordEncoder passwordEncoder
) {
    this.appUserRepository = appUserRepository;
    this.passwordEncoder = passwordEncoder;
}

@Override
public void run(String... args) {

    createManager();
    createFieldOfficer();
    createSecondFieldOfficer();
}

private void createManager() {

    if (managerEmail == null || managerEmail.isBlank()
            || managerPassword == null || managerPassword.isBlank()) {
        System.out.println("Manager bootstrap skipped: credentials not configured.");
        return;
    }

    if (appUserRepository.existsByEmail(managerEmail.trim().toLowerCase())) {
        System.out.println("Manager account already exists.");
        return;
    }

    validatePassword(managerPassword);

    AppUser manager = new AppUser();

    manager.setEmail(managerEmail.trim().toLowerCase());
    manager.setPasswordHash(passwordEncoder.encode(managerPassword));
    manager.setRole("MANAGER");
    manager.setActive(true);

    appUserRepository.save(manager);

    System.out.println("Initial manager account created.");
}

private void createFieldOfficer() {

    if (officerEmail == null || officerEmail.isBlank()
            || officerPassword == null || officerPassword.isBlank()
            || officerId == null || officerId.isBlank()) {
        System.out.println("Officer 1 bootstrap skipped: credentials or officer ID not configured.");
        return;
    }

    if (appUserRepository.existsByEmail(officerEmail.trim().toLowerCase())) {
        System.out.println("Officer 1 account already exists.");
        return;
    }

    validatePassword(officerPassword);

    Long fieldOfficerIdValue = parseOfficerId(officerId);

    AppUser officer = new AppUser();

    officer.setEmail(officerEmail.trim().toLowerCase());
    officer.setPasswordHash(passwordEncoder.encode(officerPassword));
    officer.setRole("FIELD_OFFICER");
    officer.setFieldOfficerId(fieldOfficerIdValue);
    officer.setActive(true);

    appUserRepository.save(officer);

    System.out.println("Initial field officer account created.");
}

private void createSecondFieldOfficer() {

    if (officer2Email == null || officer2Email.isBlank()
            || officer2Password == null || officer2Password.isBlank()
            || officer2Id == null || officer2Id.isBlank()) {
        System.out.println("Officer 2 bootstrap skipped: credentials or officer ID not configured.");
        return;
    }

    if (appUserRepository.existsByEmail(officer2Email.trim().toLowerCase())) {
        System.out.println("Officer 2 account already exists.");
        return;
    }

    validatePassword(officer2Password);

    Long fieldOfficerIdValue = parseOfficerId(officer2Id);

    AppUser officer = new AppUser();

    officer.setEmail(officer2Email.trim().toLowerCase());
    officer.setPasswordHash(passwordEncoder.encode(officer2Password));
    officer.setRole("FIELD_OFFICER");
    officer.setFieldOfficerId(fieldOfficerIdValue);
    officer.setActive(true);

    appUserRepository.save(officer);

    System.out.println("Officer 2 account created successfully.");
}

private Long parseOfficerId(String id) {

    try {
        return Long.parseLong(id);
    } catch (NumberFormatException exception) {
        throw new IllegalStateException(
                "Field officer ID must be a valid number."
        );
    }
}

private void validatePassword(String password) {

    if (password.length() < 12) {
        throw new IllegalStateException(
                "Bootstrap passwords must contain at least 12 characters."
        );
    }
}

}
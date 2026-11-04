package com.collectsmart.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
name = "app_users",
uniqueConstraints = {
@UniqueConstraint(columnNames = "email")
}
)
public class AppUser {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false, unique = true)
private String email;

@Column(nullable = false)
private String passwordHash;

@Column(nullable = false)
private String role;

@Column(nullable = false)
private Boolean active = true;

private Long fieldOfficerId;

@Column(nullable = false, updatable = false)
private LocalDateTime createdAt;

@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
    if (active == null) {
        active = true;
    }
}

public AppUser() {
}

public Long getId() {
    return id;
}

public void setId(Long id) {
    this.id = id;
}

public String getEmail() {
    return email;
}

public void setEmail(String email) {
    this.email = email;
}

public String getPasswordHash() {
    return passwordHash;
}

public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
}

public String getRole() {
    return role;
}

public void setRole(String role) {
    this.role = role;
}

public Boolean getActive() {
    return active;
}

public void setActive(Boolean active) {
    this.active = active;
}

public Long getFieldOfficerId() {
    return fieldOfficerId;
}

public void setFieldOfficerId(Long fieldOfficerId) {
    this.fieldOfficerId = fieldOfficerId;
}

public LocalDateTime getCreatedAt() {
    return createdAt;
}

public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
}

}
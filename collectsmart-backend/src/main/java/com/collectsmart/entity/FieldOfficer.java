package com.collectsmart.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "field_officers")
public class FieldOfficer {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false)
private String name;

@Column(unique = true, nullable = false, length = 10)
private String phoneNumber;

@Column(length = 6)
private String pincode;

@Column(nullable = false)
private Integer maxWorkload = 20;

@Column(nullable = false)
private Boolean active = true;

@Column(nullable = false, updatable = false)
private LocalDateTime createdAt = LocalDateTime.now();

public FieldOfficer() {
}

public Long getId() {
    return id;
}

public String getName() {
    return name;
}

public void setName(String name) {
    this.name = name;
}

public String getPhoneNumber() {
    return phoneNumber;
}

public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
}

public String getPincode() {
    return pincode;
}

public void setPincode(String pincode) {
    this.pincode = pincode;
}

public Integer getMaxWorkload() {
    return maxWorkload;
}

public void setMaxWorkload(Integer maxWorkload) {
    this.maxWorkload = maxWorkload;
}

public Boolean getActive() {
    return active;
}

public void setActive(Boolean active) {
    this.active = active;
}

public LocalDateTime getCreatedAt() {
    return createdAt;
}

}
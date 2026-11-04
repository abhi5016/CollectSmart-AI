package com.collectsmart.config;

public record AuthenticatedUser(
String email,
String role,
Long fieldOfficerId
) {
}
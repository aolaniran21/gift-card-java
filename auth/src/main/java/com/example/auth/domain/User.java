package com.example.auth.domain;

public class User {

    private Long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String role = "ROLE_USER";

    protected User() {
    }

    public User(Long id, String email, String password, String firstName, String lastName, String role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = (role == null || role.isBlank())
                ? "ROLE_USER"
                : (role.startsWith("ROLE_") ? role : "ROLE_" + role.trim().toUpperCase());
    }

    public User(String email, String password, String firstName, String lastName, String role) {
        this(null, email, password, firstName, lastName, role);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) { this.id = id; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

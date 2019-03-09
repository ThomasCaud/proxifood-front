package com.cdps.proxifood;

import java.util.Date;
import java.util.UUID;

public class User {
    private long id;
    private String login;
    private String email;
    private String firstName;
    private String lastName;
    private String description;
    private Date dateOfBirth;
    private Date createdAt;
    private Address address;

    public User() {
        this.id = -1;
        this.login = "User login";
        this.email = "User email";
        this.firstName = "User firstname";
        this.lastName = "User lastname";
        this.description = "User description";
        this.dateOfBirth = new Date();
        this.createdAt = new Date();
        this.address = new Address();
    }

    public User(long id, String login, String email, String firstName, String lastName, String description, Date dateOfBirth, Date createdAt, Address address) {
        this.id = id;
        this.login = login;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.description = description;
        this.dateOfBirth = dateOfBirth;
        this.createdAt = createdAt;
        this.address = address;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}

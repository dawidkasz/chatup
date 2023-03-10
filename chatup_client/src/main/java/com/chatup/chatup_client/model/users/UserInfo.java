package com.chatup.chatup_client.model.users;

import java.util.Objects;

public class UserInfo {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String toString() {
        return String.format("%s %s", firstName, lastName);
    }

    public boolean equals(Object obj) {
        if(!(obj instanceof UserInfo other)) return false;
        return Objects.equals(getId(), other.getId())
                && getUsername().equals(other.getUsername())
                && getFirstName().equals(other.getFirstName())
                && getLastName().equals(other.getLastName());
    }
}


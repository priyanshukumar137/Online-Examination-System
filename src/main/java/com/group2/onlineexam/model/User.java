package com.group2.onlineexam.model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Role { ADMIN, STUDENT }
    private final String name;
    private final String username;
    private final String password;
    private final Role role;

    public User(String name, String username, String password, Role role) {
        this.name = name; this.username = username; this.password = password; this.role = role;
    }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public boolean passwordMatches(String candidate) { return password.equals(candidate); }
}
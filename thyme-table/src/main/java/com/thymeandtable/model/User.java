package com.thymeandtable.model;

public class User {
    private final int id;
    private final String username, role;

    public User(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isInfluencer() {
        return "INFLUENCER".equals(role);
    }
}

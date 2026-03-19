package com.example.utilityapp;

public class Worker {
    // 1. Added 'phone', 'category', 'email', 'role' to match the database
    String id, name, service, status, phone, category, email, role;

    public Worker() {} // Required for Firestore

    public Worker(String name, String service, String status, String phone, String category) {
        this.name = name;
        this.service = service;
        this.status = status;
        this.phone = phone;
        this.category = category;
    }

    // --- GETTERS AND SETTERS ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // ✅ FIXED: Added getPhone() so the Adapter doesn't crash
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // ✅ FIXED: Added getCategory() so the Adapter doesn't crash
    public String getCategory() {
        // Fallback: If category is empty, return the service name instead
        if (category == null || category.isEmpty()) {
            return service;
        }
        return category;
    }
    public void setCategory(String category) { this.category = category; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
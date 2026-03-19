package com.example.utilityapp;

public class Service {
    // 1. We use 'Object' for price so it accepts "50" (String) OR 50 (Number) without crashing
    Object price;
    String name, image, id;

    public Service() {} // Required for Firebase

    // Constructor accepts Object for price
    public Service(String name, Object price, String image) {
        this.name = name;
        this.price = price;
        this.image = image;
    }

    public String getName() { return name; }

    // 2. Smart Getter: Converts whatever the price is (Number or String) into a String safely
    public String getPrice() {
        return String.valueOf(price);
    }

    public String getImage() { return image; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
package com.example.utilityapp;

public class Booking {

    private String serviceName;
    private String date;
    private String time;
    private String address;
    private double price;
    private String status;
    private String workerId;
    private String userId;        // ✅ ADDED: exists in Firestore but was missing here
    private String documentId;
    private double latitude;
    private double longitude;
    private long timestamp;       // ✅ ADDED: exists in Firestore but was missing here
    private String paymentStatus; // Add this variable

    // Required empty constructor for Firestore
    public Booking() {}

    // Full Constructor
    public Booking(String serviceName, String date, String time, String address,
                   double price, String status, String workerId, String userId,
                   double latitude, double longitude) {
        this.serviceName = serviceName;
        this.date = date;
        this.time = time;
        this.address = address;
        this.price = price;
        this.status = status;
        this.workerId = workerId;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // ─── Getters ────────────────────────────────────────────
    public String getServiceName() { return serviceName; }
    public String getDate()        { return date; }
    public String getTime()        { return time; }
    public String getAddress()     { return address; }
    public double getPrice()       { return price; }
    public String getStatus()      { return status; }
    public String getWorkerId()    { return workerId; }
    public String getUserId()      { return userId; }
    public String getDocumentId()  { return documentId; }
    public double getLatitude()    { return latitude; }
    public double getLongitude()   { return longitude; }
    public long   getTimestamp()   { return timestamp; }



    // Add this getter method
    public String getPaymentStatus() {
        return paymentStatus;
    }



    // ─── Setters (ALL required for Firestore toObject()) ────
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setDate(String date)               { this.date = date; }
    public void setTime(String time)               { this.time = time; }
    public void setAddress(String address)         { this.address = address; }
    public void setPrice(double price)             { this.price = price; }
    public void setStatus(String status)           { this.status = status; }
    public void setWorkerId(String workerId)       { this.workerId = workerId; }
    public void setUserId(String userId)           { this.userId = userId; }
    public void setDocumentId(String documentId)   { this.documentId = documentId; }
    public void setLatitude(double latitude)       { this.latitude = latitude; }
    public void setLongitude(double longitude)     { this.longitude = longitude; }
    public void setTimestamp(long timestamp)       { this.timestamp = timestamp; }
    // Add this setter method
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}
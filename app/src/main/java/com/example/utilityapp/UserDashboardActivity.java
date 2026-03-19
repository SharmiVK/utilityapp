package com.example.utilityapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDashboardActivity extends AppCompatActivity {

    // UI Components
    CardView cardStitching, cardCleaning, cardNurse, cardElectrician, cardLaundry;
    RecyclerView recyclerView;
    UserBookingAdapter adapter;
    List<Booking> bookingList;

    // 🌟 NEW: Logout Button
    ImageButton btnLogoutUser;

    // Firebase & Data
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    Map<String, String> latestPrices = new HashMap<>(); // Stores real prices

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);
        // 🔥 ROLE PROTECTION FOR USER DASHBOARD

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists() ||
                            !"user".equals(documentSnapshot.getString("role"))) {

                        // Not a normal user → close screen
                        finish();
                    }
                });

        // 1. Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2. Link Views
        cardStitching = findViewById(R.id.cardStitching);
        cardCleaning = findViewById(R.id.cardCleaning);
        cardNurse = findViewById(R.id.cardNurse);
        cardElectrician = findViewById(R.id.cardElectrician);
        cardLaundry = findViewById(R.id.cardLaundry);

        // 🌟 Link Logout Button
        btnLogoutUser = findViewById(R.id.btnLogoutUser);

        // 3. Load Real Prices from Database
        loadLatestPrices();

        // 4. Set Service Click Listeners
        cardStitching.setOnClickListener(v -> openBooking("Stitching"));
        cardCleaning.setOnClickListener(v -> openBooking("Cleaning"));
        cardNurse.setOnClickListener(v -> openBooking("Home Nurse"));
        cardElectrician.setOnClickListener(v -> openBooking("Electrician"));
        cardLaundry.setOnClickListener(v -> openBooking("Laundry"));

        // 5. 🔴 LOGOUT LOGIC
        btnLogoutUser.setOnClickListener(v -> {
            mAuth.signOut(); // Sign out from Firebase
            Intent intent = new Intent(UserDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear history
            startActivity(intent);
            finish(); // Close dashboard
        });

        // 6. Setup Booking List (RecyclerView)
        recyclerView = findViewById(R.id.recyclerUserBookings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookingList = new ArrayList<>();
        adapter = new UserBookingAdapter(this, bookingList);
        recyclerView.setAdapter(adapter);

        loadMyBookings();
    }

    // --- HELPER FUNCTIONS ---

    private void loadLatestPrices() {
        db.collection("services").addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null) {
                latestPrices.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    String name = doc.getString("name");
                    Object priceObj = doc.get("price"); // Get as Object to be safe

                    if (name != null && priceObj != null) {
                        latestPrices.put(name, String.valueOf(priceObj));
                    }
                }
            }
        });
    }

    private void openBooking(String serviceName) {
        String priceToUse = "0"; // Default fallback

        // 1. Try Exact Match
        if (latestPrices.containsKey(serviceName)) {
            priceToUse = latestPrices.get(serviceName);
        }
        // 2. Smart Matches (DB name vs Button name)
        else if (serviceName.equals("Stitching") && latestPrices.containsKey("Stitching Work")) {
            priceToUse = latestPrices.get("Stitching Work");
        }
        else if (serviceName.equals("Cleaning") && latestPrices.containsKey("Home Cleaning")) {
            priceToUse = latestPrices.get("Home Cleaning");
        }
        else if (serviceName.equals("Home Nurse") && latestPrices.containsKey("Nurse")) {
            priceToUse = latestPrices.get("Nurse");
        }

        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra("name", serviceName);
        intent.putExtra("price", priceToUse);
        startActivity(intent);
    }

    private void loadMyBookings() {
        if (mAuth.getCurrentUser() == null) return;
        String myUserId = mAuth.getCurrentUser().getUid();

        db.collection("bookings")
                .whereEqualTo("userId", myUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null) {
                        bookingList.clear();
                        for (DocumentSnapshot d : value.getDocuments()) {
                            Booking b = d.toObject(Booking.class);
                            if (b != null) {
                                bookingList.add(b);
                                // Notification Check (Optional)
                                if ("accepted".equals(b.getStatus())) {
                                    NotificationHelper.showNotification(
                                            this, "Booking Accepted!",
                                            "Your " + b.getServiceName() + " request has been accepted.");
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
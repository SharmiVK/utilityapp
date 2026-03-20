package com.example.utilityapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Log;

public class WorkerDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private List<Booking> bookingList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private SwitchMaterial switchAvailability;
    private ImageButton btnLogout;

    private FusedLocationProviderClient fusedLocationClient;
    private double workerLat = 0.0, workerLng = 0.0;

    // 🌟 STEP 3: DEFINE SEARCH RADIUS (5 KM)
    private static final double SEARCH_RADIUS_KM = 5.0;
    private String workerProfession = "";
    private static final int LOCATION_PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_dashboard);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) {
                        finish();
                        return;
                    }

                    String role = documentSnapshot.getString("role");

                    if (!"worker".equals(role)) {
                        finish();
                        return;
                    }

                    // ✅ Only after verification
                    initializeDashboard();
                });
    }
    private void initializeDashboard() {

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        bookingList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerWorker);
        switchAvailability = findViewById(R.id.switchAvailability);
        btnLogout = findViewById(R.id.btnLogout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(this, bookingList);
        recyclerView.setAdapter(adapter);

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                switchAvailability.setText("ONLINE");
                checkPermissionAndGetLocation();
            } else {
                switchAvailability.setText("OFFLINE");
                updateAvailabilityInDb(false, 0, 0);
                bookingList.clear();
                adapter.notifyDataSetChanged();
            }
        });

        fetchWorkerProfession();
    }
    private void fetchWorkerProfession() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        // 🔥 1️⃣ Check role safety (extra protection)
                        String role = documentSnapshot.getString("role");
                        if (!"worker".equals(role)) {
                            finish();
                            return;
                        }

                        // 🔥 2️⃣ Check approval status
                        String status = documentSnapshot.getString("status");
                        if (!"active".equals(status)){
                            Toast.makeText(this,
                                    "Wait for admin approval",
                                    Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }

                        // 🔥 3️⃣ Fetch service
                        workerProfession = documentSnapshot.getString("service");

                        if (workerProfession == null) {
                            Toast.makeText(this,
                                    "Service type not set",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        loadAvailableJobs();
                    }
                });
    }
    private void checkPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                null
        ).addOnSuccessListener(location -> {

            if (location != null) {

                workerLat = location.getLatitude();
                workerLng = location.getLongitude();

                updateAvailabilityInDb(true, workerLat, workerLng);
                loadAvailableJobs();

            } else {

                Toast.makeText(this,
                        "Unable to fetch current location",
                        Toast.LENGTH_SHORT).show();

                switchAvailability.setChecked(false);
            }
        });
    }

    private void updateAvailabilityInDb(boolean isAvailable, double lat, double lng) {

        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> updates = new HashMap<>();

        updates.put("isAvailable", isAvailable);

        // 🔥 ADD THIS LINE RIGHT HERE
        updates.put("lastSeen", System.currentTimeMillis());

        if (isAvailable) {
            updates.put("latitude", lat);
            updates.put("longitude", lng);
        }

        db.collection("users").document(userId).update(updates);
    }

    private void loadAvailableJobs() {
        // 🌟 STEP 4: FETCH ONLY RELEVANT JOBS
        db.collection("bookings")
                .whereEqualTo("status", "Confirmed")
                .whereEqualTo("serviceName", workerProfession)
                .whereEqualTo("paymentStatus", "paid")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FirestoreError", error.getMessage());
                        return;
                    }

                    if (value != null) {
                        bookingList.clear();
                        for (DocumentSnapshot d : value.getDocuments()) {
                            Booking booking = d.toObject(Booking.class);
                            if (booking != null) {
                                // 🌟 STEP 6: MATCHING LOGIC (Using Haversine)
                                double dist = calculateDistance(workerLat, workerLng, booking.getLatitude(), booking.getLongitude());
                                if (dist <= SEARCH_RADIUS_KM) {
                                    booking.setDocumentId(d.getId());
                                    bookingList.add(booking);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // 🌟 STEP 5: THE CORE ALGORITHM (Haversine Formula)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == 0 || lon1 == 0 || lat2 == 0 || lon2 == 0)
            return Double.MAX_VALUE;

        double R = 6371; // Earth's radius in KM

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.cos(rLat1) * Math.cos(rLat2) *
                        Math.pow(Math.sin(dLon / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        return R * c; // Final distance in KM
    }

    // 🌟 GETTERS FOR ADAPTER (Fixes Step 6 Errors)
    public double getWorkerLat() {
        return workerLat;
    }

    public double getWorkerLng() {
        return workerLng;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getCurrentLocation();  // Try again
            } else {
                Toast.makeText(this,
                        "Location permission required to go ONLINE",
                        Toast.LENGTH_LONG).show();
                switchAvailability.setChecked(false);
            }
        }
    }
}
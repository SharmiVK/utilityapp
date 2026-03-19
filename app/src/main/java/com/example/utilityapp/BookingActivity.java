package com.example.utilityapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingActivity extends AppCompatActivity {

    TextView tvServiceName, tvServicePrice;
    EditText etDate, etTime, etAddress;
    Button btnConfirm;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String serviceName, servicePrice;

    FusedLocationProviderClient fusedLocationClient;
    double customerLatitude = 0.0;
    double customerLongitude = 0.0;
    private static final int LOCATION_PERMISSION_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        serviceName = getIntent().getStringExtra("name");
        if (serviceName == null) {
            Toast.makeText(this, "Invalid service", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvServiceName = findViewById(R.id.tvServiceName);
        tvServicePrice = findViewById(R.id.tvServicePrice);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etAddress = findViewById(R.id.etAddress);
        btnConfirm = findViewById(R.id.btnConfirmBooking);

        tvServiceName.setText("Book: " + serviceName);
        tvServicePrice.setText("Loading price...");

        // ✅ FIXED: uses whereEqualTo query instead of document(serviceName)
        loadServicePrice();

        etAddress.setHint("Detecting your location...");
        checkPermissionAndGetLocation();

        btnConfirm.setOnClickListener(v -> {
            String date = etDate.getText().toString().trim();
            String time = etTime.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (TextUtils.isEmpty(date) || TextUtils.isEmpty(time) || TextUtils.isEmpty(address)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            saveBooking(date, time, address);
        });
    }

    // ✅ FIXED: Query by "name" field instead of using serviceName as document ID
    private void loadServicePrice() {
        db.collection("services")
                .whereEqualTo("name", serviceName)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        Double price = query.getDocuments().get(0).getDouble("price");
                        if (price != null) {
                            servicePrice = String.valueOf(price);
                            tvServicePrice.setText("Price: ₹" + servicePrice);
                        } else {
                            tvServicePrice.setText("Price: Not set");
                        }
                    } else {
                        Toast.makeText(this, "Service not found", Toast.LENGTH_SHORT).show();
                        tvServicePrice.setText("Price: Not available");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading price", Toast.LENGTH_SHORT).show());
    }

    private void checkPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                null
        ).addOnSuccessListener(location -> {
            if (location != null) {
                customerLatitude = location.getLatitude();
                customerLongitude = location.getLongitude();
                autoFillAddress(customerLatitude, customerLongitude);
            } else {
                etAddress.setHint("Unable to detect location. Enter manually.");
                Toast.makeText(this, "Location not available. Please enter manually.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void autoFillAddress(double lat, double lng) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    String fullAddress = addresses.get(0).getAddressLine(0);
                    runOnUiThread(() -> {
                        etAddress.setText(fullAddress);
                        Toast.makeText(this, "Location Auto-Detected!", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Unable to fetch address", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }

    private void saveBooking(String date, String time, String address) {
        if (mAuth.getCurrentUser() == null) return;

        if (customerLatitude == 0.0 || customerLongitude == 0.0) {
            Toast.makeText(this, "Please enable GPS before confirming booking.", Toast.LENGTH_LONG).show();
            return;
        }
        if (servicePrice == null) {
            Toast.makeText(this, "Please wait, loading service price...", Toast.LENGTH_SHORT).show();
            return;
        }

        double priceValue;
        try {
            priceValue = Double.parseDouble(servicePrice);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> booking = new HashMap<>();
        booking.put("serviceName", serviceName);
        booking.put("price", priceValue);
        booking.put("date", date);
        booking.put("time", time);
        booking.put("address", address);
        booking.put("userId", mAuth.getCurrentUser().getUid());
        booking.put("latitude", customerLatitude);
        booking.put("longitude", customerLongitude);
        booking.put("workerId", "");
        booking.put("status", "pending");
        booking.put("timestamp", System.currentTimeMillis());

        db.collection("bookings").add(booking)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Booking Request Sent!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
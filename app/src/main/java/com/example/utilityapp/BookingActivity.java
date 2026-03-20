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
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import org.json.JSONObject;
import android.util.Log;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import java.util.Calendar;


public class BookingActivity extends AppCompatActivity implements PaymentResultListener{

    TextView tvServiceName, tvServicePrice;
    EditText etDate, etTime, etAddress;
    Button btnConfirm;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String serviceName, servicePrice;
    String currentBookingId;

    FusedLocationProviderClient fusedLocationClient;
    double customerLatitude = 0.0;
    double customerLongitude = 0.0;
    private static final int LOCATION_PERMISSION_REQUEST = 101;
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Checkout.preload(getApplicationContext());
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
       // Button btnPay = findViewById(R.id.btnConfirmBooking);
        // Click listener for Date field
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
                // Sets the selected date into the EditText
                etDate.setText(dayOfMonth + "-" + (month1 + 1) + "-" + year1);
            }, year, month, day);
            datePickerDialog.show();
        });
        // Click listener for Time field
        etTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
                // Sets the selected time into the EditText
                etTime.setText(String.format("%02d:%02d", hourOfDay, minute1));
            }, hour, minute, true);
            timePickerDialog.show();
        });

        tvServiceName.setText("Book: " + serviceName);
        tvServicePrice.setText("Loading price...");

        // ✅ FIXED: uses whereEqualTo query instead of document(serviceName)
        loadServicePrice();

        etAddress.setHint("Detecting your location...");
        checkPermissionAndGetLocation();

        btnConfirm.setOnClickListener(v -> {
            if (isProcessing) return;
            String date = etDate.getText().toString().trim();
            String time = etTime.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (TextUtils.isEmpty(date) || TextUtils.isEmpty(time) || TextUtils.isEmpty(address)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            isProcessing = true; // Block further clicks
            btnConfirm.setEnabled(false); // Visual feedback
            saveBooking(date, time, address);
        });

       // btnPay.setOnClickListener(v -> {
          //  startPayment(500); // Pass the price of the service here
       // });
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

                            ((TextView) findViewById(R.id.tvTotalAmount)).setText("₹" + servicePrice);
                        } else {
                            tvServicePrice.setText("Price: Not set");

                            ((TextView) findViewById(R.id.tvTotalAmount)).setText("₹0.00");
                        }
                    } else {
                        Toast.makeText(this, "Service not found", Toast.LENGTH_SHORT).show();
                        tvServicePrice.setText("Price: Not available");

                        ((TextView) findViewById(R.id.tvTotalAmount)).setText("₹0.00");
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
            isProcessing = false; // Reset so they can try again after turning on GPS
            btnConfirm.setEnabled(true);
            return;
        }

        if (servicePrice == null) {
            Toast.makeText(this, "Please wait, loading service price...", Toast.LENGTH_SHORT).show();
            isProcessing = false;
            btnConfirm.setEnabled(true);
            return;
        }

        double priceValue;
        try {
            priceValue = Double.parseDouble(servicePrice);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            isProcessing = false;
            btnConfirm.setEnabled(true);
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
                    currentBookingId = documentReference.getId();
                    // Pass the price to Razorpay
                    startPayment((int) priceValue);
                    Toast.makeText(this, "Booking Request Sent!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {

                    isProcessing = false;
                    btnConfirm.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void startPayment(int amount) {
        Checkout checkout = new Checkout();
        //   real Test Key ID from Step 1
        checkout.setKeyID("rzp_test_ST5daufBblX6dL");

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Utility App");
            options.put("description", "Service Booking Fee");
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");

            // Amount is in paise (Multiply by 100)
            options.put("amount", amount * 100);

            checkout.open(BookingActivity.this, options);
        } catch (Exception e) {
            Log.e("PaymentError", "Error in starting Razorpay Checkout", e);
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        Toast.makeText(this, "Payment Successful: " + razorpayPaymentID, Toast.LENGTH_SHORT).show();

        if (currentBookingId != null) {
            // Create a Map to update multiple fields at once
            Map<String, Object> paymentUpdate = new HashMap<>();
            paymentUpdate.put("status", "Confirmed");      // Ready for worker assignment
            paymentUpdate.put("paymentStatus", "paid");    // NEW: as per Step 8
            paymentUpdate.put("paymentId", razorpayPaymentID); // NEW: as per Step 8

            db.collection("bookings").document(currentBookingId)
                    .update(paymentUpdate) // Updates all 3 fields in one go
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Booking fully updated for Step 8");

                        // Close this screen and go back to the dashboard
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error updating booking", e);
                        Toast.makeText(this, "Critical: Database update failed!", Toast.LENGTH_LONG).show();
                    });
        }
    }

    @Override
    public void onPaymentError(int code, String response) {
        isProcessing = false;
        // 1. Alert the user about the failure
        Toast.makeText(this, "Payment Failed: " + response, Toast.LENGTH_LONG).show();

        // 2. Optionally update status to 'failed' in Firestore
        if (currentBookingId != null) {
            db.collection("bookings").document(currentBookingId)
                    .update("status", "Failed")
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Booking marked as Failed"));
        }
    }
}
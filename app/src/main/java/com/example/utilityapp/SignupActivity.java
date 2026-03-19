package com.example.utilityapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    EditText nameField, emailField, passwordField, phoneField;
    Spinner spinnerServiceType;   // ✅ CHANGED: Spinner instead of EditText
    Button signupBtn;
    TextView goToLogin;
    RadioButton rbUser, rbWorker;
    RadioGroup roleGroup;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    // ✅ All service options — must match Firestore serviceName exactly
    private final String[] SERVICE_OPTIONS = {
            "Select Service Type",
            "Stitching",
            "Cleaning",
            "Home Nurse",
            "Electrician",
            "Laundry"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Link Views
        nameField     = findViewById(R.id.signupName);
        emailField    = findViewById(R.id.signupEmail);
        passwordField = findViewById(R.id.signupPassword);
        phoneField    = findViewById(R.id.signupPhone);
        signupBtn     = findViewById(R.id.signupBtn);
        goToLogin     = findViewById(R.id.goToLogin);
        rbUser        = findViewById(R.id.rbUser);
        rbWorker      = findViewById(R.id.rbWorker);
        roleGroup     = findViewById(R.id.roleGroup);

        // ✅ Link Spinner
        spinnerServiceType = findViewById(R.id.spinnerServiceType);

        // ✅ Setup Spinner with service options
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                SERVICE_OPTIONS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerServiceType.setAdapter(adapter);

        // Hide spinner by default (only show for workers)
        spinnerServiceType.setVisibility(View.GONE);

        // Show/hide spinner based on role
        roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbWorker) {
                spinnerServiceType.setVisibility(View.VISIBLE);
            } else {
                spinnerServiceType.setVisibility(View.GONE);
            }
        });

        // Register button click
        signupBtn.setOnClickListener(v -> {
            String name     = nameField.getText().toString().trim();
            String email    = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String phone    = phoneField.getText().toString().trim();

            if (TextUtils.isEmpty(name))  { nameField.setError("Name is required");   return; }
            if (TextUtils.isEmpty(email)) { emailField.setError("Email is required");  return; }
            if (TextUtils.isEmpty(phone)) { phoneField.setError("Phone is required");  return; }
            if (password.length() < 6)    { passwordField.setError("Password must be 6+ chars"); return; }

            String role = "user";
            String serviceType = "";

            if (rbWorker.isChecked()) {
                role = "worker";

                // ✅ Get selected service from spinner
                serviceType = spinnerServiceType.getSelectedItem().toString();

                // ✅ Validate — must pick a real service, not the placeholder
                if (serviceType.equals("Select Service Type")) {
                    Toast.makeText(this, "Please select your service type", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            registerUser(name, email, password, role, serviceType, phone);
        });

        goToLogin.setOnClickListener(v -> finish());
    }

    private void registerUser(String name, String email, String password,
                              String role, String serviceType, String phone) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        String userId = firebaseUser.getUid();

                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("name", name);
                        userMap.put("email", email);
                        userMap.put("role", role);
                        userMap.put("phone", phone);

                        if ("worker".equals(role)) {
                            userMap.put("category", serviceType);
                            userMap.put("service", serviceType); // ✅ Now always properly capitalized
                            userMap.put("status", "pending");
                            userMap.put("latitude", 0.0);
                            userMap.put("longitude", 0.0);
                            userMap.put("isAvailable", false);
                        } else {
                            userMap.put("status", "active");
                        }

                        db.collection("users").document(userId).set(userMap)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(SignupActivity.this,
                                            "Registration Successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(SignupActivity.this,
                                        "Error saving data: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(SignupActivity.this,
                                "Registration Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
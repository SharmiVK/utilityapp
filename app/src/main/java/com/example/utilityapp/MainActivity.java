package com.example.utilityapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvSignup;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2. Link Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        /*btnLoginUser = findViewById(R.id.btnLoginUser);
        btnLoginWorker = findViewById(R.id.btnLoginWorker);
        btnLoginAdmin = findViewById(R.id.btnLoginAdmin);*/
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);

        // 3. Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            checkUserRole(mAuth.getCurrentUser().getUid());
        }

        // 4. Set Click Listeners
        /*(btnLoginUser.setOnClickListener(v -> performLogin());
        btnLoginWorker.setOnClickListener(v -> performLogin());
        btnLoginAdmin.setOnClickListener(v -> performLogin());*/
        btnLogin.setOnClickListener(v -> performLogin());

        tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SignupActivity.class));
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required.");
            return;
        }

        // Sign in with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(MainActivity.this, "Login Successful! Checking Access...", Toast.LENGTH_SHORT).show();
                    checkUserRole(authResult.getUser().getUid());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUserRole(String userId) {

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this,
                                "Error: User profile not found in Database",
                                Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        return;
                    }

                    String role = documentSnapshot.getString("role");
                    String status = documentSnapshot.getString("status");

                    if (role == null) {
                        Toast.makeText(this,
                                "Error: Role missing in database",
                                Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        return;
                    }

                    String cleanRole = role.trim().toLowerCase();

                    if ("admin".equals(cleanRole)) {

                        startActivity(new Intent(this, AdminDashboardActivity.class));
                        finish();

                    } else if ("worker".equals(cleanRole)) {

            if (status == null) {
                Toast.makeText(this,
                        "Account status missing. Contact admin.",
                        Toast.LENGTH_LONG).show();
                mAuth.signOut();
                return;
            }

            String cleanStatus = status.trim().toLowerCase();

            if ("active".equals(cleanStatus)) {
                // ✅ ALLOW LOGIN
                startActivity(new Intent(this, WorkerDashboardActivity.class));
                finish();
            } else {
                // ❌ BLOCK
                Toast.makeText(this,
                        "Your account is awaiting Admin approval.",
                        Toast.LENGTH_LONG).show();
                mAuth.signOut();
                return; // ⚠️ IMPORTANT
            }


            //startActivity(new Intent(this, WorkerDashboardActivity.class));
            //finish();

                    } else {

                        startActivity(new Intent(this, UserDashboardActivity.class));
                        finish();
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Database Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
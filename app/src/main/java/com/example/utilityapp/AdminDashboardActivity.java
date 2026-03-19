package com.example.utilityapp;

import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {
    private View adminRoot;
    CardView cardVerify, cardManage, cardViewWorkers;
    // 🌟 Added variable for Logout button
    ImageButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        adminRoot = findViewById(R.id.adminRoot);

        // Hide UI until verified
        adminRoot.setVisibility(View.GONE);

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

                    if (!documentSnapshot.exists() ||
                            !"admin".equals(documentSnapshot.getString("role"))) {

                        finish();
                        return;
                    }

                    // ✅ Admin verified → show UI
                    adminRoot.setVisibility(View.VISIBLE);

                    initializeDashboard();
                });
    }
    private void initializeDashboard() {

        btnLogout = findViewById(R.id.btnAdminLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        cardViewWorkers = findViewById(R.id.cardViewWorkers);
        cardViewWorkers.setOnClickListener(v ->
                startActivity(new Intent(this, ViewWorkersActivity.class)));

        cardVerify = findViewById(R.id.cardVerifyWorkers);
        cardVerify.setOnClickListener(v ->
                startActivity(new Intent(this, VerificationActivity.class)));

        cardManage = findViewById(R.id.cardManageServices);
        cardManage.setOnClickListener(v ->
                startActivity(new Intent(this, ManageServicesActivity.class)));
    }
}
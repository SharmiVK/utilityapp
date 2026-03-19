package com.example.utilityapp;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import com.google.firebase.firestore.ListenerRegistration;

public class VerificationActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    WorkerAdapter adapter;
    List<Worker> workerList;
    FirebaseFirestore db;
    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        // ✅ FIXED: Changed ID to 'recyclerVerification' to match your XML
        recyclerView = findViewById(R.id.recyclerVerification);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        workerList = new ArrayList<>();
        adapter = new WorkerAdapter(this, workerList);
        recyclerView.setAdapter(adapter);

        loadPendingWorkers();
    }

    private void loadPendingWorkers() {

        registration = db.collection("users")
                .whereEqualTo("role", "worker")
                .whereEqualTo("status", "pending")
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {

                        workerList.clear();

                        for (DocumentSnapshot d : value.getDocuments()) {
                            Worker worker = d.toObject(Worker.class);
                            if (worker != null) {
                                worker.setId(d.getId());
                                workerList.add(worker);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }
                });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registration != null) {
            registration.remove();
        }
    }
}
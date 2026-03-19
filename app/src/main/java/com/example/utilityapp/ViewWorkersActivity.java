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

public class ViewWorkersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminWorkerListAdapter adapter;
    private List<Worker> workerList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. MUST HAVE THIS LINE TO SHOW THE SCREEN
        setContentView(R.layout.activity_view_workers);

        db = FirebaseFirestore.getInstance();
        workerList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerAllWorkers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Use the same adapter we fixed for approvals
        adapter = new AdminWorkerListAdapter(this, workerList);
        recyclerView.setAdapter(adapter);

        fetchAllWorkers();
    }

    private void fetchAllWorkers() {
        // Fetch EVERYONE from the workers collection
        db.collection("users").addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Error loading workers", Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                workerList.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Worker worker = doc.toObject(Worker.class);
                    if (worker != null) {
                        worker.setId(doc.getId()); // Essential for the Approve button to work
                        workerList.add(worker);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}
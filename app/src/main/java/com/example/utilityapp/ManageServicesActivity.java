package com.example.utilityapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import com.google.firebase.firestore.ListenerRegistration;

public class ManageServicesActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AdminServiceAdapter adapter;
    List<Service> serviceList;
    FirebaseFirestore db;
    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_services);

        recyclerView = findViewById(R.id.recyclerManageServices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        serviceList = new ArrayList<>();
        adapter = new AdminServiceAdapter(this, serviceList);
        recyclerView.setAdapter(adapter);

        loadServices();
    }

    private void loadServices() {

        registration = db.collection("services")
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        return;
                    }

                    if (value != null) {

                        serviceList.clear();

                        for (DocumentSnapshot d : value.getDocuments()) {
                            Service service = d.toObject(Service.class);
                            if (service != null) {
                                service.setId(d.getId());
                                serviceList.add(service);
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
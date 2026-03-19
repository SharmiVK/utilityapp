package com.example.utilityapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class AdminWorkerListAdapter extends RecyclerView.Adapter<AdminWorkerListAdapter.ViewHolder> {

    Context context;
    List<Worker> workerList;
    FirebaseFirestore db;

    public AdminWorkerListAdapter(Context context, List<Worker> workerList) {
        this.context = context;
        this.workerList = workerList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_worker_simple, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Worker worker = workerList.get(position);

        holder.name.setText(worker.getName());
        holder.category.setText("Category: " + worker.getCategory());
        holder.phone.setText("Phone: " + worker.getPhone());

        // 1. Set Status Text and Color
        String status = worker.getStatus() != null ? worker.getStatus() : "pending";
        holder.status.setText(status.toUpperCase());

        if (status.equalsIgnoreCase("approved") || status.equalsIgnoreCase("verified")) {
            holder.status.setTextColor(Color.parseColor("#4CAF50")); // Green
            // 🌟 HIDE BUTTON: If already approved, don't show the button
            holder.btnApprove.setVisibility(View.GONE);
        } else {
            holder.status.setTextColor(Color.parseColor("#F44336")); // Red
            // 🌟 SHOW BUTTON: Only show it for pending workers
            holder.btnApprove.setVisibility(View.VISIBLE);
        }

        // 2. Button Click Logic
        holder.btnApprove.setOnClickListener(v -> {
            if (worker.getId() == null) return;

            db.collection("users").document(worker.getId())
                    .update("status", "approved")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, worker.getName() + " Approved!", Toast.LENGTH_SHORT).show();
                        // This removes the card immediately from the Verification list
                        int currentPos = holder.getAdapterPosition();
                        if (currentPos != RecyclerView.NO_POSITION) {
                            workerList.remove(currentPos);
                            notifyItemRemoved(currentPos);
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() { return workerList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, category, phone, status;
        Button btnApprove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvWorkerName);
            category = itemView.findViewById(R.id.tvWorkerCategory);
            phone = itemView.findViewById(R.id.tvWorkerPhone);
            status = itemView.findViewById(R.id.tvWorkerStatus);
            btnApprove = itemView.findViewById(R.id.btnApprove);
        }
    }
}
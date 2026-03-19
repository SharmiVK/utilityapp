package com.example.utilityapp;

import android.content.Context;
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
import androidx.appcompat.app.AlertDialog;

public class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder> {

    Context context;
    List<Worker> workerList;
    FirebaseFirestore db;

    public WorkerAdapter(Context context, List<Worker> workerList) {
        this.context = context;
        this.workerList = workerList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public WorkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_worker, parent, false);
        return new WorkerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkerViewHolder holder, int position) {

        Worker worker = workerList.get(position);
        holder.name.setText(worker.getName());
        holder.service.setText(worker.getService());

        holder.approveBtn.setOnClickListener(v -> {

            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            Worker currentWorker = workerList.get(currentPosition);

            new AlertDialog.Builder(context)
                    .setTitle("Approve Worker")
                    .setMessage("Are you sure you want to approve this worker?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        holder.approveBtn.setEnabled(false);
                        holder.rejectBtn.setEnabled(false);

                        db.collection("users")
                                .document(currentWorker.getId())
                                .update("status", "active")
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(context, "Worker Approved!", Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e -> {
                                    holder.approveBtn.setEnabled(true);
                                    holder.rejectBtn.setEnabled(true);
                                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        holder.rejectBtn.setOnClickListener(v -> {

            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            Worker currentWorker = workerList.get(currentPosition);

            new AlertDialog.Builder(context)
                    .setTitle("Reject Worker")
                    .setMessage("Are you sure you want to reject this worker?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        holder.approveBtn.setEnabled(false);
                        holder.rejectBtn.setEnabled(false);

                        db.collection("users")
                                .document(currentWorker.getId())
                                .update("status", "rejected")
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(context, "Worker Rejected!", Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e -> {
                                    holder.approveBtn.setEnabled(true);
                                    holder.rejectBtn.setEnabled(true);
                                    Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

    }

    @Override
    public int getItemCount() { return workerList.size(); }

    public static class WorkerViewHolder extends RecyclerView.ViewHolder {
        TextView name, service;
        Button approveBtn,rejectBtn;

        public WorkerViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.workerName);
            service = itemView.findViewById(R.id.workerService);
            approveBtn = itemView.findViewById(R.id.btnApprove);
            rejectBtn = itemView.findViewById(R.id.btnReject);
        }
    }
}
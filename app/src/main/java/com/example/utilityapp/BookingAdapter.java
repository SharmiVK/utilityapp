package com.example.utilityapp;

import com.google.firebase.firestore.DocumentSnapshot;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private Context context;
    private List<Booking> list;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public BookingAdapter(Context context, List<Booking> list) {
        this.context = context;
        this.list = list;
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_request, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = list.get(position);

        holder.tvService.setText(booking.getServiceName());
        holder.tvPrice.setText("₹" + booking.getPrice());
        holder.tvDate.setText(booking.getDate() + " | " + booking.getTime());
        holder.tvAddress.setText(booking.getAddress());

        // 🌟 STEP 6: DISPLAY DISTANCE (Logic Output)
        if (context instanceof WorkerDashboardActivity) {
            WorkerDashboardActivity activity = (WorkerDashboardActivity) context;

            // Fetch worker coordinates from Dashboard via Getter methods
            double workerLat = activity.getWorkerLat();
            double workerLng = activity.getWorkerLng();

            // Calculate using Step 5 Algorithm (Haversine Formula)
            double dist = calculateDistance(workerLat, workerLng,
                    booking.getLatitude(), booking.getLongitude());

            holder.tvDistance.setText(String.format("%.1f km away", dist));
        }


        // 🌟 STEP 8: WORKER ACCEPTANCE LOGIC (Uber behavior)
        holder.btnAccept.setOnClickListener(v -> {

            if (mAuth.getCurrentUser() == null) return;

            String currentWorkerId = mAuth.getCurrentUser().getUid();

            db.runTransaction(transaction -> {

                DocumentReference ref = db.collection("bookings")
                        .document(booking.getDocumentId());

                DocumentSnapshot snapshot = transaction.get(ref);

                String status = snapshot.getString("status");

                if ("pending".equals(status)) {
                    transaction.update(ref,
                            "workerId", currentWorkerId,
                            "status", "accepted");
                } else {
                    throw new FirebaseFirestoreException(
                            "Already accepted",
                            FirebaseFirestoreException.Code.ABORTED);
                }

                return null;

            }).addOnSuccessListener(aVoid -> {

                Toast.makeText(context, "Success! Job Confirmed.", Toast.LENGTH_SHORT).show();

                int currentPos = holder.getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    list.remove(currentPos);
                    notifyItemRemoved(currentPos);
                }

            }).addOnFailureListener(e -> {

                Toast.makeText(context,
                        "Sorry! Already accepted by someone else.",
                        Toast.LENGTH_SHORT).show();

            });

        });
    }


    // 📐 STEP 5: THE CORE ALGORITHM (Haversine Formula)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == 0 || lat2 == 0) return 999.0;
        double R = 6371; // Earth's radius in KM
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.pow(Math.sin(dLon / 2), 2);
        return R * 2 * Math.asin(Math.sqrt(a));
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvService, tvPrice, tvDate, tvAddress, tvDistance;
        Button btnAccept;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvService = itemView.findViewById(R.id.tvService);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            btnAccept = itemView.findViewById(R.id.btnAccept);
        }
    }
}
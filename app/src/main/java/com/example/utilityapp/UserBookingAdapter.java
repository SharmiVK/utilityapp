package com.example.utilityapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.widget.Toast;

public class UserBookingAdapter extends RecyclerView.Adapter<UserBookingAdapter.ViewHolder> {

    Context context;
    List<Booking> list;

    public UserBookingAdapter(Context context, List<Booking> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = list.get(position);

        holder.tvService.setText(booking.getServiceName());
        holder.tvDate.setText(booking.getDate() + " | " + booking.getTime());
        holder.tvPrice.setText("₹" + (int) booking.getPrice());

        // 1. Get Payment Status (The "Gatekeeper")
        String payStatus = booking.getPaymentStatus();
        if (payStatus == null) payStatus = "pending";

        // 2. Get Booking Status
        String bookingStatus = booking.getStatus();
        if (bookingStatus == null) bookingStatus = "pending";

        // 🔵 STEP 9: Logic to block/allow based on payment
        if ("paid".equals(payStatus)) {
            // ✅ USER PAID: Show actual booking progress
            holder.tvStatus.setText(bookingStatus.toUpperCase());

            if (bookingStatus.equals("accepted")) {
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
                holder.tvStatus.getBackground().setTint(Color.parseColor("#E8F5E9"));
            } else {
                holder.tvStatus.setTextColor(Color.parseColor("#2196F3")); // Blue (Confirmed)
                holder.tvStatus.getBackground().setTint(Color.parseColor("#E8F5E9"));
            }
        } else {
            // ❌ USER HASN'T PAID: Block and show "Payment Pending"
            holder.tvStatus.setText("PAYMENT PENDING");
            holder.tvStatus.setTextColor(Color.parseColor("#F44336")); // Red
            holder.tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));

            // Optional: Alert the user if they try to click a non-paid booking
            holder.itemView.setOnClickListener(v ->
                    Toast.makeText(context, "Please complete payment to proceed", Toast.LENGTH_SHORT).show()
            );
        }
    }
    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvService, tvDate, tvPrice, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvService = itemView.findViewById(R.id.tvService);
            tvDate    = itemView.findViewById(R.id.tvDate);
            tvPrice   = itemView.findViewById(R.id.tvPrice);
            tvStatus  = itemView.findViewById(R.id.tvStatus);
        }
    }
}
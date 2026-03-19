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

        // ✅ FIXED: "₹" + number = String concatenation → safe for setText()
        holder.tvPrice.setText("₹" + (int) booking.getPrice());

        // ✅ FIXED: null check to prevent NPE crash
        String status = booking.getStatus();
        if (status == null) status = "pending";

        holder.tvStatus.setText(status.toUpperCase());

        if (status.equals("accepted")) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
            holder.tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));
            holder.tvStatus.setBackgroundColor(Color.parseColor("#FFF3E0"));
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
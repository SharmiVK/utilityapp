package com.example.utilityapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    Context context;
    List<Service> serviceList;

    public ServiceAdapter(Context context, List<Service> serviceList) {
        this.context = context;
        this.serviceList = serviceList;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your custom card design
        View view = LayoutInflater.from(context).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {

        Service service = serviceList.get(position);

        holder.name.setText(service.getName());
        holder.price.setText("₹" + service.getPrice());

        // 1. Image Logic (Keep this exactly as you have it)
        int resId = context.getResources().getIdentifier(service.getImage(), "drawable", context.getPackageName());
        if (resId != 0) {
            holder.icon.setImageResource(resId);
        } else {
            holder.icon.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // 2. NEW CLICK LOGIC (Add this part!)
        holder.itemView.setOnClickListener(v -> {
            // Create the intent to go to BookingActivity
            android.content.Intent intent = new android.content.Intent(context, BookingActivity.class);

            // Pack the data (Service Name and Price) to take with us
            intent.putExtra("name", service.getName());
            intent.putExtra("price", service.getPrice());

            // Go!
            context.startActivity(intent);
        });
    }
    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView name, price;
        ImageView icon;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.serviceName);
            price = itemView.findViewById(R.id.servicePrice);
            icon = itemView.findViewById(R.id.serviceIcon);
        }
    }
}
package com.example.utilityapp;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import android.text.InputType;

public class AdminServiceAdapter extends RecyclerView.Adapter<AdminServiceAdapter.ViewHolder> {

    Context context;
    List<Service> serviceList;
    FirebaseFirestore db;

    public AdminServiceAdapter(Context context, List<Service> serviceList) {
        this.context = context;
        this.serviceList = serviceList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Service service = serviceList.get(position);

        holder.name.setText(service.getName());
        holder.price.setText("₹ " + String.format("%.2f", service.getPrice()));

        // FEATURE 1: Existing Edit Price Logic
        holder.editBtn.setOnClickListener(v -> {

            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            Service currentService = serviceList.get(currentPosition);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Update Price");

            final EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            input.setHint("Enter new price");
            input.setText(String.valueOf(currentService.getPrice()));

            builder.setView(input);

            builder.setPositiveButton("Update", (dialog, which) -> {

                String priceText = input.getText().toString().trim();

                if (priceText.isEmpty()) {
                    Toast.makeText(context, "Price cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                double newPrice = Double.parseDouble(priceText);
                //  ADD THIS CHECK HERE
                if (newPrice <= 0) {
                    Toast.makeText(context, "Price must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.collection("services")
                        .document(currentService.getId())
                        .update("price", newPrice)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(context, "Price Updated!", Toast.LENGTH_SHORT).show()
                        )
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        });
    }



    @Override
    public int getItemCount() { return serviceList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, price;
        ImageView editBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.adminServiceName);
            price = itemView.findViewById(R.id.adminServicePrice);
            editBtn = itemView.findViewById(R.id.btnEditPrice);
        }
    }
}
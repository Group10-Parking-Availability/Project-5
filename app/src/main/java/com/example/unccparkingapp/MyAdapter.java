package com.example.unccparkingapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<ParkingData> data;

    public MyAdapter(List<ParkingData> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParkingData itemData = data.get(position);

        int parkingAvailableInt;
        String parkingAvailableString = itemData.getParkingAvailable();

        try {
            parkingAvailableInt = Integer.parseInt(parkingAvailableString);
            // or
            // parkingAvailableInt = Integer.valueOf(parkingAvailableString);
        } catch (NumberFormatException e) {
            // Handle the case where the string is not a valid integer
            e.printStackTrace();
            parkingAvailableInt = 0; // Default value or any appropriate handling
        }

        holder.textView1.setText("Location: " + itemData.getLocation());
        holder.textView2.setText("Parking Available: " + parkingAvailableInt + "%");



        holder.progressBarHorizontal.setProgress(parkingAvailableInt);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView1;
        TextView textView2;
        ProgressBar progressBarHorizontal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView1 = itemView.findViewById(R.id.textView1);
            textView2 = itemView.findViewById(R.id.textView2);
            progressBarHorizontal = itemView.findViewById(R.id.progressBarHorizontal);
        }
    }
}


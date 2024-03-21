package com.example.unccparkingapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
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

        holder.textView1.setText(itemData.getLocation());
        holder.textView2.setText(parkingAvailableInt + "%" + "\navailable");
        holder.progressBarHorizontal.setProgress(parkingAvailableInt);

        int uncc_green = 0xFF046A38;
        int uncc_gold = 0xFFB9975B;

        // Change colors to fit theme later
        if (parkingAvailableInt <= 25) {
            holder.textView2.setTextColor(Color.RED);
            holder.progressBarHorizontal.setProgressTintList(ColorStateList.valueOf(Color.RED));
        } else if (parkingAvailableInt <= 50 && parkingAvailableInt > 25) {
            holder.textView2.setTextColor(ColorStateList.valueOf(uncc_gold));
            holder.progressBarHorizontal.setProgressTintList(ColorStateList.valueOf(uncc_gold));
        } else {
            holder.textView2.setTextColor(ColorStateList.valueOf(uncc_green));
            holder.progressBarHorizontal.setProgressTintList(ColorStateList.valueOf(uncc_green));
        }

        if(!itemData.isExpand() == false)
        {
            holder.arrowRightView.setImageResource(R.drawable.ic_arrow_down);
        } else {
            holder.arrowRightView.setImageResource(R.drawable.ic_arrow_right);
        }

        holder.expandedView.setVisibility(itemData.isExpand() ? View.VISIBLE : View.GONE);
        holder.textView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemData.setExpand(!itemData.isExpand());

                notifyDataSetChanged();
            }
        });

        holder.arrowRightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemData.setExpand(!itemData.isExpand());
                notifyDataSetChanged();
            }
        });

    }



    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView1;
        TextView textView2;
        ProgressBar progressBarHorizontal;

        ConstraintLayout expandedView;

        ImageView arrowRightView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView1 = itemView.findViewById(R.id.textView1);
            textView2 = itemView.findViewById(R.id.textView2);
            progressBarHorizontal = itemView.findViewById(R.id.progressBarHorizontal);
            expandedView = itemView.findViewById(R.id.expanded_view);
            arrowRightView = itemView.findViewById(R.id.dropDownArrow);
        }
    }
}


package com.example.unccparkingapp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.api.LogDescriptor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    // Define the interface for handling favorite item clicks
    public interface FavoritesClickListener {
        void onFavoriteClicked(ParkingData itemData);
    }

    private List<ParkingData> data;
    private FavoritesClickListener listener;

    public MyAdapter(List<ParkingData> data, FavoritesClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    public List<ParkingData> getData() {
        Collections.sort(data, new Comparator<ParkingData>() {

            @Override
            public int compare(ParkingData o1, ParkingData o2) {
                return o1.getLocation().compareToIgnoreCase(o2.getLocation());
            }
        });
        return data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Retrieve data for the current position
        ParkingData itemData = data.get(position);

        // Convert parking availability string to integer
        int parkingAvailableInt;
        String parkingAvailableString = itemData.getParkingAvailable();
        try {
            parkingAvailableInt = Integer.parseInt(parkingAvailableString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            parkingAvailableInt = 0;
        }

        // Set text views and progress bar
        holder.textView1.setText(itemData.getLocation());
        holder.textView2.setText(parkingAvailableInt + "%" + "\navailable");
        holder.progressBarHorizontal.setProgress(parkingAvailableInt);

        // Define colors
        int uncc_green = 0xFF046A38;
        int uncc_gold = 0xFFB9975B;

        // Set colors based on parking availability
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

        // Set arrow icon based on expansion state
        if (!itemData.isExpand()) {
            holder.arrowRightView.setImageResource(R.drawable.ic_arrow_right);
        } else {
            holder.arrowRightView.setImageResource(R.drawable.ic_arrow_down);
        }

        // Show or hide expanded view based on expansion state
        holder.expandedView.setVisibility(itemData.isExpand() ? View.VISIBLE : View.GONE);

        // Set click listener for location text view to toggle expansion state
        holder.textView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemData.setExpand(!itemData.isExpand());
                notifyDataSetChanged();
            }
        });

        // Set click listener for arrow icon to toggle expansion state
        holder.arrowRightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemData.setExpand(!itemData.isExpand());
                notifyDataSetChanged();
            }
        });

        // Set favorite icon color based on favorite state
        int iconColor = itemData.isFavorite() ? R.color.uncc_gold : R.color.uncc_green;
        holder.favorite.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), iconColor));

        // Set click listener for favorite icon to toggle favorite state and color
        holder.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle favorite state
                itemData.setFavorite(!itemData.isFavorite());

                // Notify the listener about the favorite item click
                listener.onFavoriteClicked(itemData);

                // Update the color of the favorite icon
                int newIconColor = itemData.isFavorite() ? R.color.uncc_gold : R.color.uncc_green;
                holder.favorite.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), newIconColor));
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
        ImageView favorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView1 = itemView.findViewById(R.id.textView1);
            textView2 = itemView.findViewById(R.id.textView2);
            progressBarHorizontal = itemView.findViewById(R.id.progressBarHorizontal);
            expandedView = itemView.findViewById(R.id.expanded_view);
            arrowRightView = itemView.findViewById(R.id.dropDownArrow);
            favorite = itemView.findViewById(R.id.favoritesIcon);
        }
    }
}

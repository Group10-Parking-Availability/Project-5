package com.example.unccparkingapp;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    // Define the interface for handling favorite item clicks
    public interface FavoritesClickListener {
        void onFavoriteClicked(ParkingData itemData);
    }

    private final List<ParkingData> data;
    private final FavoritesClickListener listener;
    private final Context context;

    public MyAdapter(Context context, List<ParkingData> data, FavoritesClickListener listener) {
        this.context = context;
        this.data = data;
        this.listener = listener;
    }

    public List<ParkingData> getData() {
        data.sort((o1, o2) -> o1.getLocation().compareToIgnoreCase(o2.getLocation()));
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // Retrieve data for the current position
        ParkingData itemData = data.get(position);


        // Initialize chart
        LineChart chart = holder.itemView.findViewById(R.id.chart);
        chart.setDrawBorders(true);
        chart.setBorderColor(Color.rgb(4,106,56));


        // Timestamp labels
        String[] timestamps = new String[]{"12:00AM", "03:00AM", "06:00AM", "09:00AM", "12:00PM", "3:00PM", "6:00PM", "9:00PM"};

        // Manual data setup * need to implement dynamic functionality *
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 20));
        entries.add(new Entry(1, 50));
        entries.add(new Entry(2, 30));
        entries.add(new Entry(3, 30));
        entries.add(new Entry(4, 40));
        entries.add(new Entry(5, 50));
        entries.add(new Entry(6, 60));
        entries.add(new Entry(7, 30));

        // Configure the data set
        LineDataSet dataSet = new LineDataSet(entries, "Current % of spots Available");
        dataSet.setColor(Color.BLACK);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setCircleColor(Color.rgb(185,151,91));

        // Set up the chart data and refresh
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Customize the X-axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new TimestampAxisValueFormatter(timestamps));
        xAxis.setLabelCount(timestamps.length);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setDrawGridLines(false);

        // Configure the Y-axis (left)
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false); // Disable right Y-axis
        chart.invalidate(); // Redraw the chart


        // Convert parking availability string to integer
        int parkingAvailableInt;
        String parkingAvailableString = itemData.getParkingAvailable();
        try {
            parkingAvailableInt = Integer.parseInt(parkingAvailableString);
        } catch (NumberFormatException e) {
            Log.d("demo", "onBindViewHolder: " + e.getMessage());
            parkingAvailableInt = 0;
        }

        // Set text views and progress bar
        holder.textView1.setText(itemData.getLocation());
        if (parkingAvailableInt == -1) {
            holder.textView2.setText("N/A\nAvailable");
        } else {
            holder.textView2.setText(parkingAvailableInt + "%" + "\nAvailable");
        }
        holder.progressBarHorizontal.setProgress(parkingAvailableInt);

        // Define colors
        int uncc_green = 0xFF046A38;
        int uncc_gold = 0xFFB9975B;

        // Set colors based on parking availability
        if (parkingAvailableInt <= 25) {
            holder.textView2.setTextColor(Color.RED);
            holder.progressBarHorizontal.setProgressTintList(ColorStateList.valueOf(Color.RED));
        } else if (parkingAvailableInt <= 50) {
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
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view) {
                for (int i = 0; i < data.size(); i++){
                    if (data.get(i).isExpand() && !data.get(position).isExpand()) {
                        data.get(i).setExpand(false);
                    }
                }
                itemData.setExpand(!itemData.isExpand());
                notifyDataSetChanged();
            }
        });

        // Set click listener for arrow icon to toggle expansion state
        holder.arrowRightView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                for (int i = 0; i < data.size(); i++){
                    if (data.get(i).isExpand() && !data.get(position).isExpand()) {
                        data.get(i).setExpand(false);
                    }
                }
                itemData.setExpand(!itemData.isExpand());
                notifyDataSetChanged();
            }
        });

        holder.location.setOnClickListener(v -> {
            ParkingData clickedItem = data.get(holder.getAdapterPosition());

            try {
                // Read JSON data from file
                JSONArray jsonArray = loadJSONFromAsset(this.context);
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if (Objects.equals(jsonObject.getString("location"), clickedItem.getLocation())) {
                            String url = jsonObject.getString("url");
                            Log.d("demo", "onClick: url = " + url);
                            gotoUrl(url);
                            break;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });


        // Set favorite icon color based on favorite state
        int iconColor = itemData.isFavorite() ? R.color.uncc_gold : R.color.uncc_green;
        holder.favorite.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), iconColor));

        // Set click listener for favorite icon to toggle favorite state and color
        holder.favorite.setOnClickListener(v -> {
            // Toggle favorite state
            itemData.setFavorite(!itemData.isFavorite());

            // Notify the listener about the favorite item click
            listener.onFavoriteClicked(itemData);

            // Update the color of the favorite icon
            int newIconColor = itemData.isFavorite() ? R.color.uncc_gold : R.color.uncc_green;
            holder.favorite.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), newIconColor));
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // Takes a string and parses it to a url
    private void gotoUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception e) {
            Log.d("demo", "gotoUrl: error going to url.\n" + e.getMessage());
        }
    }

    public class TimestampAxisValueFormatter extends ValueFormatter {
        private final String[] values;

        // Constructor that takes an array of strings
        public TimestampAxisValueFormatter(String[] values) {
            this.values = values;
        }

        @Override
        public String getFormattedValue(float value) {
            int index = (int) value;
            if (index < 0 || index >= values.length) {
                return "";
            }
            return values[index];
        }
    }

    // Function to load JSON data from file
    private JSONArray loadJSONFromAsset (Context context) {
        try {
            InputStream is = context.getAssets().open("parkingData.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            return new JSONArray(json);
        } catch (IOException | JSONException e) {
            Log.d("demo", "loadJSONFromAsset: error loading JSON file");
            return null;
        }
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView1;
        TextView textView2;
        ProgressBar progressBarHorizontal;
        ConstraintLayout expandedView;
        ImageView arrowRightView;
        ImageView favorite;
        ImageView location;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView1 = itemView.findViewById(R.id.textView1);
            textView2 = itemView.findViewById(R.id.textView2);
            progressBarHorizontal = itemView.findViewById(R.id.progressBarHorizontal);
            expandedView = itemView.findViewById(R.id.expanded_view);
            arrowRightView = itemView.findViewById(R.id.dropDownArrow);
            favorite = itemView.findViewById(R.id.favoritesIcon);
            location = itemView.findViewById(R.id.location_icon);
        }
    }
}

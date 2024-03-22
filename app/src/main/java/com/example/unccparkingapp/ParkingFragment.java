package com.example.unccparkingapp;

import android.content.res.Resources;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.unccparkingapp.databinding.FragmentParkingBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ParkingFragment extends Fragment implements MyAdapter.FavoritesClickListener {

    FragmentParkingBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentParkingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<ParkingData> parkingList = new ArrayList<>(); // Initialize the list
        List<ParkingData> favoritesList = new ArrayList<>(); // Initialize favorites list

        MyAdapter adapter = new MyAdapter(parkingList, this);
        MyAdapter favAdapter = new MyAdapter(favoritesList, this);

        // Set adapters for RecyclerViews
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerViewFavorites.setAdapter(favAdapter);

        // Set layout managers for RecyclerViews
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Collect data from the database and set the location and percent of parking available
        db.collection("parking_data")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String location = document.getString("location");
                            String parkingPercent = document.getString("parking_available");

                            ParkingData parkingData = new ParkingData(location, parkingPercent);

                            // Check if data is a favorite, and store accordingly
                            if (parkingData.isFavorite()) {
                                favoritesList.add(parkingData);
                            } else {
                                parkingList.add(parkingData);
                            }
                        }

                        updateFavoritesVisibility(favoritesList);
                        // Notify adapters of data change
                        adapter.notifyDataSetChanged();
                        favAdapter.notifyDataSetChanged();
                    } else {
                        // Handle errors
                        Toast.makeText(getActivity(), "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateFavoritesVisibility(List<ParkingData> favoritesList) {
        // Check if the favorites list is empty, if so, hide the favorites RecyclerView and text
        if (favoritesList.isEmpty()) {
            binding.recyclerViewFavorites.setVisibility(View.GONE);
            binding.favoritesText.setVisibility(View.GONE);

            // Set top margin for textView3
            int marginTopInDp = 0;
            int marginTopInPx = (int) (marginTopInDp * getResources().getDisplayMetrics().density);
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.textView3.getLayoutParams();
            layoutParams.topMargin = marginTopInPx;
            binding.textView3.setLayoutParams(layoutParams);

        } else {
            binding.recyclerViewFavorites.setVisibility(View.VISIBLE);
            binding.favoritesText.setVisibility(View.VISIBLE);

            if (favoritesList.size() >= 3) {
                // Calculate the height in pixels for 250dp
                float density = getResources().getDisplayMetrics().density;
                int pixels = (int) (235 * density + 0.5f);

                // Set the height of the RecyclerView to 250dp
                ViewGroup.LayoutParams layoutParams = binding.recyclerViewFavorites.getLayoutParams();
                layoutParams.height = pixels;
                binding.recyclerViewFavorites.setLayoutParams(layoutParams);
            } else {
                // Set the height of the RecyclerView to wrap_content
                ViewGroup.LayoutParams layoutParams = binding.recyclerViewFavorites.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                binding.recyclerViewFavorites.setLayoutParams(layoutParams);
            }
            // Set top margin for textView3
            int marginTopInDp = 8;
            int marginTopInPx = (int) (marginTopInDp * getResources().getDisplayMetrics().density);
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.textView3.getLayoutParams();
            layoutParams.topMargin = marginTopInPx;
            binding.textView3.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void onFavoriteClicked(ParkingData itemData) {
        // Handle favorite item click here
        // You need to move the item between lists and update UI accordingly
        List<ParkingData> parkingList = ((MyAdapter) binding.recyclerView.getAdapter()).getData();
        List<ParkingData> favoritesList = ((MyAdapter) binding.recyclerViewFavorites.getAdapter()).getData();

        if (!itemData.isFavorite()) {
            // Remove from favorites list
            favoritesList.remove(itemData);
            // Add to parkingList
            parkingList.add(itemData);
        } else {
            // Remove from parkingList
            parkingList.remove(itemData);
            // Add to favoritesList
            favoritesList.add(itemData);
        }

        // Update UI
        binding.recyclerView.getAdapter().notifyDataSetChanged();
        binding.recyclerViewFavorites.getAdapter().notifyDataSetChanged();

        // Update favorites visibility
        updateFavoritesVisibility(favoritesList);
    }

}

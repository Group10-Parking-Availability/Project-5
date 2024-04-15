package com.example.unccparkingapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.unccparkingapp.databinding.FragmentParkingBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ParkingFragment extends Fragment implements MyAdapter.FavoritesClickListener {

    FragmentParkingBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentParkingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<ParkingData> parkingList = new ArrayList<>(); // Initialize the list
        List<ParkingData> favoritesList = new ArrayList<>(); // Initialize favorites list

        MyAdapter adapter = new MyAdapter(this.getContext(), parkingList, this);
        MyAdapter favAdapter = new MyAdapter(this.getContext(), favoritesList, this);

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
                            boolean favorite = loadJson(location);

                            ParkingData parkingData = new ParkingData(location, parkingPercent, favorite);

                            // Check if data is a favorite, and store accordingly
                            if (parkingData.isFavorite()) {
                                favoritesList.add(parkingData);
                            } else {
                                parkingList.add(parkingData);
                            }
                        }

                        sortArrayLocation(parkingList);
                        sortArrayLocation(favoritesList);
                        updateFavoritesVisibility(favoritesList);
                        // Notify adapters of data change
                        adapter.notifyDataSetChanged();
                        favAdapter.notifyDataSetChanged();
                    } else {
                        // Handle errors
                        Toast.makeText(getActivity(), "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });

        binding.reportbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch the PopUpReport activity
                startActivity(new Intent(requireContext(), PopUpReport.class));
            }
        });
    }

    // Updates the visibility of the favorites section
    private void updateFavoritesVisibility(List<ParkingData> favoritesList) {
        // Check if the favorites list is empty, if so, hide the favorites RecyclerView and text
        if (favoritesList.isEmpty()) {
            binding.recyclerViewFavorites.setVisibility(View.GONE);
            binding.favoritesText.setVisibility(View.GONE);

            int marginTopInDp = 0;
            int marginTopInPx = (int) (marginTopInDp * getResources().getDisplayMetrics().density);
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.textView3.getLayoutParams();
            layoutParams.topMargin = marginTopInPx;
            binding.textView3.setLayoutParams(layoutParams);

        } else {
            binding.recyclerViewFavorites.setVisibility(View.VISIBLE);
            binding.favoritesText.setVisibility(View.VISIBLE);

            if (favoritesList.size() >= 3) {
                float density = getResources().getDisplayMetrics().density;
                int pixels = (int) (235 * density + 0.5f);

                ViewGroup.LayoutParams layoutParams = binding.recyclerViewFavorites.getLayoutParams();
                layoutParams.height = pixels;
                binding.recyclerViewFavorites.setLayoutParams(layoutParams);
            } else {
                ViewGroup.LayoutParams layoutParams = binding.recyclerViewFavorites.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                binding.recyclerViewFavorites.setLayoutParams(layoutParams);
            }
            int marginTopInDp = 8;
            int marginTopInPx = (int) (marginTopInDp * getResources().getDisplayMetrics().density);
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.textView3.getLayoutParams();
            layoutParams.topMargin = marginTopInPx;
            binding.textView3.setLayoutParams(layoutParams);
        }
    }

    // Moves array items accordingly when the favorite icon is clicked
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onFavoriteClicked(ParkingData itemData) {
        List<ParkingData> parkingList = ((MyAdapter) Objects.requireNonNull(binding.recyclerView.getAdapter())).getData();
        List<ParkingData> favoritesList = ((MyAdapter) Objects.requireNonNull(binding.recyclerViewFavorites.getAdapter())).getData();

        boolean newFavoriteStatus = itemData.isFavorite();

        updateJson(itemData.getLocation(), newFavoriteStatus);

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

        sortArrayLocation(parkingList);
        sortArrayLocation(favoritesList);

        binding.recyclerView.getAdapter().notifyDataSetChanged();
        binding.recyclerViewFavorites.getAdapter().notifyDataSetChanged();

        updateFavoritesVisibility(favoritesList);
    }

    // Sorts the parking data array alphabetically
    public void sortArrayLocation(List<ParkingData> parkingData) {
        parkingData.sort((p1, p2) -> p1.getLocation().compareToIgnoreCase(p2.getLocation()));
    }

    // Loads the favorites.json stored internally on the device
    public boolean loadJson(String location) {
        try {
            FileInputStream fileInputStream = requireContext().openFileInput("favorites.json");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            fileInputStream.close();

            String json = stringBuilder.toString();
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String jsonLocation = jsonObject.getString("location");
                boolean jsonFavorite = jsonObject.getBoolean("favorite");

                if (location.equals(jsonLocation)) {
                    Log.d("demo", "loadJson: " + jsonObject);
                    return jsonFavorite;
                }
            }
        } catch (IOException | JSONException e) {
            Log.d("demo", "Error loading JSON: " + e.getMessage(), e);
        }
        return false;
    }

    // Updates the favorites.json file saved on the device, if there's no file it creates one
    public void updateJson(String location, boolean favorite) {
        try {
            File file = new File(requireContext().getFilesDir(), "favorites.json");

            if (!file.exists()) {
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("location", location);
                jsonObject.put("favorite", favorite);
                jsonArray.put(jsonObject);

                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(jsonArray.toString());
                bufferedWriter.close();

                Log.d("demo", "New JSON file created and data saved");

            } else {
                FileInputStream fileInputStream = requireContext().openFileInput("favorites.json");
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                fileInputStream.close();

                JSONArray jsonArray = getJsonArray(location, favorite, stringBuilder);

                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(jsonArray.toString());
                bufferedWriter.close();

                Log.d("demo", "JSON data updated and saved");

            }
        } catch (IOException | JSONException e) {
            Log.e("demo", "Error updating JSON: " + e.getMessage(), e);
        }
    }

    @NonNull
    private static JSONArray getJsonArray(String location, boolean favorite, StringBuilder stringBuilder) throws JSONException {
        String json = stringBuilder.toString();
        JSONArray jsonArray = new JSONArray(json);

        boolean locationFound = false;

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String jsonLocation = jsonObject.getString("location");

            if (location.equals(jsonLocation)) {
                jsonObject.put("favorite", favorite);
                jsonArray.put(i, jsonObject);
                locationFound = true;
                break;
            }
        }

        if (!locationFound) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("location", location);
            jsonObject.put("favorite", favorite);
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }


}

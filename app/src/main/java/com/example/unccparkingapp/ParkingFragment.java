package com.example.unccparkingapp;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.unccparkingapp.databinding.FragmentParkingBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkingFragment extends Fragment {

    public ParkingFragment() {
        // Required empty public constructor
    }

    FragmentParkingBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



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

        MyAdapter adapter = new MyAdapter(parkingList);
        MyAdapter favAdapter = new MyAdapter(favoritesList);

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
                            String data1 = document.getString("location");
                            String data2 = document.getString("parking_available");

                            ParkingData parkingData = new ParkingData(data1, data2);

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
        } else {
            binding.recyclerViewFavorites.setVisibility(View.VISIBLE);
            binding.favoritesText.setVisibility(View.VISIBLE);

        }
    }
}

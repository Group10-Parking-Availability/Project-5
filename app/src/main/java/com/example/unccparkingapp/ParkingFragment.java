package com.example.unccparkingapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.unccparkingapp.databinding.FragmentParkingBinding;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.*;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentParkingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    //Used to designate the column that we want to access
    static int CARPARKDESIG = 2;
    static int OCCUPANCYLIMIT = 6;
    static int CURRENTLEVEL = 7;


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

        try{
            // Used to access assets directory
            AssetManager assetManager = getContext().getAssets();

            // Used to open the excel file
            OPCPackage pkg = OPCPackage.open(assetManager.open("CPCOUNTING.xlsx"));

            // Create a xssfWorkbook from the excel file
            Workbook xssfWorkbook = new XSSFWorkbook(pkg);

            // Create a sheet form the workbook
            Sheet sheet = xssfWorkbook.getSheetAt(0);



            // There is information for a each deck through 49 rows Ex: North deck has infomation for rows 1-49
            // The 3rd row contains the information that is most relevant to us but we need to get the row that is the 3rd row for every deck which is:
            /*
            North Deck = 3
            Cone Deck Visitor = 51
            CRI Deck = 99
            Union Deck Upper = 147
            South Village Deck = 195
            East Deck 1 = 243
            West Deck = 291
            Cone Deck F/S = 339
            East Deck 2/3 = 387
            Union Deck Lower = 435
            Admissions Lot = 483 ## I don't know if this one is necessary or not

            Here we are populating a list of integers which we will use to get the rows we want when reading our sheet
             */
            List<Integer> rowNumbersForTotals = new ArrayList<Integer>();
            rowNumbersForTotals.add(3);
            rowNumbersForTotals.add(51);
            rowNumbersForTotals.add(99);
            rowNumbersForTotals.add(147);
            rowNumbersForTotals.add(195);
            rowNumbersForTotals.add(243);
            rowNumbersForTotals.add(291);
            rowNumbersForTotals.add(339);
            rowNumbersForTotals.add(387);
            rowNumbersForTotals.add(435);
            //rowNumbersForTotals.add(483);



            // The columns are as follows
            /*
            CARPARKNO = 0
            CARPARKABBR = 1
            CARPARKDESIG = 2
            COUNTINGCATEGORYNO = 3
            COUNTINGCATEGORY = 4
            FREELIMIT = 5
            OCCUPANCYLIMIT = 6
            OCCUPANCYLIMIT = 7
            RESERVATION = 8
            CAPACITY = 9

            Most of these are likely not going to be used, but CARPARKDESIG, OCCUPANCYLIMIT, and CURRENTLEVEL all have static ints that hold their column number
             */

            Row currentRow;
            Cell currentCell;

            String location;
            float parkingAvailableDecimal;
            int parkingAvailable;
            float totalSpots;
            float spotsFull;

            // Used to format the data of numeric cells
            DataFormatter formatter = new DataFormatter();



            // Looping through numbers we use to access the rows
            for (Integer i : rowNumbersForTotals){
                    // Sets current row based on our i
                    currentRow = sheet.getRow(i);

                    if (currentRow != null){
                        // Setting our cell based on the currentRow and CARPARKDESIG
                        currentCell = currentRow.getCell(CARPARKDESIG);
                        // Setting location based on the string in the cell
                        location = currentCell.getStringCellValue();

                        // Kind of hacky but this is how I am formatting the location for Cone Deck Faculty/Staff so that it fits in the ui
                        if (location.equals("Cone Deck Faculty/Staff")){
                            location = "Cone Deck F/S";
                        }

                        // Setting our cell based on the currentRow and OCCUPANCYLIMIT
                        currentCell = currentRow.getCell(OCCUPANCYLIMIT);
                        // Setting totalSpots based on the number in the cell, have to use formatter to access this since it is a numeric cell
                        totalSpots = Float.parseFloat(formatter.formatCellValue(currentCell));

                        // Setting our cell based on the currentRow and CURRENTLEVEL
                        currentCell = currentRow.getCell(CURRENTLEVEL);
                        // Setting spotsFull based on the number in the cell, have to use formatter to access this since it is a numeric cell
                        spotsFull = Float.parseFloat(formatter.formatCellValue(currentCell));

                        if (totalSpots != 9999) {
                            // Math to calculate the percentage of spots available
                            parkingAvailableDecimal = 100 - (100 * (spotsFull / totalSpots));
                            parkingAvailable = (int) Math.round(parkingAvailableDecimal);
                        } else {
                            parkingAvailable = -1;
                        }

                        // Loads if the location has been favorited from our Json file
                        boolean favorite = loadJson(location);

                        // Creating our parkingData obj
                        ParkingData parkingData = new ParkingData(location, parkingAvailable, favorite, (int)totalSpots, (int)spotsFull);

                        // Check if data is a favorite, and store accordingly
                        if (parkingData.isFavorite()) {
                            favoritesList.add(parkingData);
                        } else {
                            parkingList.add(parkingData);
                        }

                        //Sending lists to relevant functions
                        sortArrayLocation(parkingList);
                        sortArrayLocation(favoritesList);
                        updateFavoritesVisibility(favoritesList);
                        // Notify adapters of data change
                        adapter.notifyDataSetChanged();
                        favAdapter.notifyDataSetChanged();

                    }

                }
            pkg.close();

        }
        catch (IOException | InvalidFormatException e){
            Log.d("error", "Error: IOException when trying to read spreadsheet");
        }

        // Click listener for report button, opens pop up report menu
        binding.reportbtn.setOnClickListener(v -> {

            binding.reportbtn.setEnabled(false);

            startActivity(new Intent(requireContext(), PopUpReport.class));

            // Handler to allow delay
            new Handler().postDelayed(() -> binding.reportbtn.setEnabled(true), 500);
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

            if (favoritesList.size() >= 4) {
                float density = getResources().getDisplayMetrics().density;
                int pixels = (int) (317 * density + 0.5f);
                ViewGroup.LayoutParams layoutParams = binding.recyclerViewFavorites.getLayoutParams();
                layoutParams.height = pixels;
                binding.recyclerViewFavorites.setLayoutParams(layoutParams);
            } else {
                ViewGroup.LayoutParams layoutParams = binding.recyclerViewFavorites.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                binding.recyclerViewFavorites.setLayoutParams(layoutParams);
            }
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

package com.example.unccparkingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("UNCC Parking Availability");

        getSupportFragmentManager().beginTransaction()
                .add(R.id.rootView, new ParkingFragment())
                .commit();
    }
}
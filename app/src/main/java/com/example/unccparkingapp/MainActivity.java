package com.example.unccparkingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.InputStream;

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
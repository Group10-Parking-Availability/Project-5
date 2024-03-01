package com.example.unccparkingapp;

public class ParkingData {
    private String location;
    private String parkingAvailable;

    public ParkingData() {
        // Empty constructor needed for Firestore
    }

    public ParkingData(String location, String parkingAvailable) {
        this.location = location;
        this.parkingAvailable = parkingAvailable;
    }

    public String getLocation() {
        return location;
    }

    public String getParkingAvailable() {
        return parkingAvailable;
    }
}

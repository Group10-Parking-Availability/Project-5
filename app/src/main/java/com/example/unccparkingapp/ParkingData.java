package com.example.unccparkingapp;

public class ParkingData {
    private String location;
    private String parkingAvailable;
    private boolean expand;
    private boolean favorite;

    public ParkingData() {
        // Empty constructor needed for Firestore
    }

    public ParkingData(String location, String parkingAvailable, Boolean favorite) {
        this.location = location;
        this.parkingAvailable = parkingAvailable;
        this.favorite = favorite;
    }

    public String getLocation() {
        return location;
    }

    public String getParkingAvailable() {
        return parkingAvailable;
    }

    public boolean isExpand() {
        return expand;
    }

    public void setExpand(boolean expand) {
        this.expand = expand;
    }

    public boolean isFavorite() { return favorite; }

    public void setFavorite(boolean favorite) { this.favorite = favorite; }
}

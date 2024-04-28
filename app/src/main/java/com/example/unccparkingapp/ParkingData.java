package com.example.unccparkingapp;

public class ParkingData {
    private final String location;
    private final int parkingAvailable;
    private final int totalSpots;
    private final int spotsFull;
    private boolean expand;
    private boolean favorite;

    public ParkingData(String location, int parkingAvailable, Boolean favorite, int totalSpots, int spotsFull) {
        this.location = location;
        this.parkingAvailable = parkingAvailable;
        this.favorite = favorite;
        this.totalSpots = totalSpots;
        this.spotsFull = spotsFull;
    }

    public String getLocation() {
        return location;
    }

    public String getParkingAvailable() {
        return Integer.toString(parkingAvailable);
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

package com.DinukaNavaratna.SHAREtheFARE;

public class requestInfo {
    private String name, mobile, passcode, timestamp, comments;
    private Double lat, lng;
    private int id, donation_count;

    public requestInfo(int id, String name, String mobile, String passcode, int donation_count, String timestamp, Double lat, Double lng, String comments){
        this.id = id;
        this.name = name;
        this.mobile = mobile;
        this.passcode = passcode;
        this.donation_count = donation_count;
        this.timestamp = timestamp;
        this.lat = lat;
        this.lng = lng;
        this.comments = comments;
    }

    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }

    public String getPasscode() {
        return passcode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getComments() {
        return comments;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public int getId() {
        return id;
    }

    public int getDonation_count() {
        return donation_count;
    }
}

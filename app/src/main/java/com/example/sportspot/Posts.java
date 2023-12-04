package com.example.sportspot;

import com.google.firebase.database.PropertyName;

public class Posts {
    public String uid, time, date, postimage, description, sport, daterange, profileimage, username;

    public Posts() {
        // Default constructor required for calls to DataSnapshot.getValue(Posts.class)
    }

    public Posts(String uid, String time, String date, String postimage, String description, String sport, String daterange, String profileimage, String username) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.postimage = postimage;
        this.description = description;
        this.sport = sport;
        this.daterange = daterange;
        this.profileimage = profileimage;
        this.username = username;
    }

    @PropertyName("daterange")
    public String getDaterange() {
        return daterange;
    }

    public void setDaterange(String daterange) {
        this.daterange = daterange;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}


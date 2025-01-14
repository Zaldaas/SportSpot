package com.example.sportspot;

import com.google.firebase.database.PropertyName;

public class Posts {
    public String uid, time, date, postimage, description, sport, daterange, profileimage, username;

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

    public String getUid() {
        return uid;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public String getPostimage() {
        return postimage;
    }

    public String getDescription() {
        return description;
    }

    public String getSport() {
        return sport;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public String getUsername() {
        return username;
    }

}


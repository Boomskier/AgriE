package com.example.imagedetector;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String username;
    public String phoneno;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String phoneno) {
        this.username = username;
        this.phoneno = phoneno;
    }

}

package com.ateamincorporated.rentals.db;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {
    public String phoneNumber;
    public String location;

// --Commented out by Inspection START (04/23/21 3:56 PM):
//    public User() {
//        // Default constructor required for calls to DataSnapshot.getValue(User.class)
//
//    }
// --Commented out by Inspection STOP (04/23/21 3:56 PM)

    public User(String phoneNumber, String location) {
        this.phoneNumber = phoneNumber;
        this.location = location;
    }


// --Commented out by Inspection START (04/23/21 3:56 PM):
//    @Exclude
//    public Map<String, Object> toMap() {
//        HashMap<String, Object> result = new HashMap<>();
//        result.put("phoneNumber", phoneNumber);
//        result.put("location", location);
//
//        return result;
//    }
// --Commented out by Inspection STOP (04/23/21 3:56 PM)
}

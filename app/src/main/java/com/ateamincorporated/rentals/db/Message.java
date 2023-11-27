package com.ateamincorporated.rentals.db;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Message {
    public String email;
    public String message;

// --Commented out by Inspection START (04/23/21 3:57 PM):
//    public Message() {
//        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
//    }
// --Commented out by Inspection STOP (04/23/21 3:57 PM)

    public Message (String email, String message) {
        this.email = email;
        this.message = message;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("message", message);

        return result;
    }
}

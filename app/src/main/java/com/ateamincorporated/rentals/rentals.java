package com.ateamincorporated.rentals;

import com.google.firebase.database.FirebaseDatabase;

public class rentals extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        /* Enable disk persistence  */
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}

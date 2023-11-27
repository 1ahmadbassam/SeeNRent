package com.ateamincorporated.rentals;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.fragment.NavHostFragment;

public class AddAssetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        // Hide the title bar
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_add_asset);
        //NavHost
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        //        NavigationUI.setupWithNavController((NavigationView) findViewById(R.id.nav_view), navController);
    }
}
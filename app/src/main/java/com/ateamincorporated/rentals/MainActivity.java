package com.ateamincorporated.rentals;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        ConnectivityManager connectivityManager =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (!(activeNetwork != null && activeNetwork.isConnected())) {
            findViewById(R.id.error).setVisibility(View.VISIBLE);
            findViewById(R.id.nav_host_fragment).setVisibility(View.GONE);
            findViewById(R.id.bottomAppBar).setVisibility(View.GONE);
            findViewById(R.id.nav_view).setVisibility(View.GONE);
            findViewById(R.id.fab).setVisibility(View.GONE);
            return;
        }

        BottomNavigationView navView = findViewById(R.id.nav_view);
        BottomAppBar navBar = findViewById(R.id.bottomAppBar);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_user)
//                .build();
        // NavHost
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        //FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
//            Navigation.findNavController(v).navigate(R.id.add_asset_from_home);
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(MainActivity.this, "You need to be signed in to do that!", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(MainActivity.this, AddAssetActivity.class));
            }
        });
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.loginFragment || destination.getId() == R.id.registerFragment || destination.getId() == R.id.detailFragment || destination.getId() == R.id.secondFragment
                    || destination.getId() == R.id.fourthFragment || destination.getId() == R.id.thirdFragment || destination.getId() == R.id.resetPasswordFragment
            || destination.getId() == R.id.updateProfileFragment || destination.getId() == R.id.reAuthFragment || destination.getId() == R.id.supportMessageFragment) {
                navView.setVisibility(View.GONE);
                navBar.setVisibility(View.GONE);
                fab.setVisibility(View.GONE);
            } else {
                navBar.setVisibility(View.VISIBLE);
                navView.setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);
            }
        });
    }
}
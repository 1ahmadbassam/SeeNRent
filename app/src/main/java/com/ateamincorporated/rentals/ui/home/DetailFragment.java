package com.ateamincorporated.rentals.ui.home;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ateamincorporated.rentals.R;
import com.ateamincorporated.rentals.db.Asset;
import com.ateamincorporated.rentals.db.DatabaseContract;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DetailFragment extends Fragment {

    private static final String DATE_OF_CREATION = "This asset was created on %s at %s.";
    private static final String TIME_BASE = "This asset is available %s";
    private static final String TIME_START = "from %s.";
    private static final String TIME_END = "until %s.";

    private static final String PRICE_FORMAT = "%s L.L/%s";

    private Bundle bundle;
    private DatabaseReference reference;
    private String mUid;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail, container, false);

        reference = FirebaseDatabase.getInstance().getReference();

        bundle = getArguments();
        if (bundle == null) {
            throw new IllegalStateException();
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final CheckBox favoriteButton = root.findViewById(R.id.favorite);

        if (user == null) {
            mUid = null;
            favoriteButton.setVisibility(View.INVISIBLE);
        } else {
            mUid = user.getUid();
            favoriteButton.setVisibility(View.VISIBLE);
        }

        Asset asset = (Asset) bundle.getSerializable("asset");
        TextView name = root.findViewById(R.id.name);
        name.setText(asset.name);
        TextView description = root.findViewById(R.id.description);
        description.setText(asset.description);
        TextView location = root.findViewById(R.id.location);
        location.setText(asset.genericLocation);
        TextView price = root.findViewById(R.id.price);
        price.setText(String.format(PRICE_FORMAT, asset.price, asset.priceUnit));
        TextView timeOfCreation = root.findViewById(R.id.time_of_creation);
        name.setText(asset.name);
        Date date = new Date(Long.parseLong(asset.timeOfCreation));
        timeOfCreation.setText(String.format(DATE_OF_CREATION, DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault()).format(date), java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(date.getTime())));
        TextView category = root.findViewById(R.id.category);
        category.setText(asset.category);

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(Long.parseLong(asset.startDate));
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTimeInMillis(Long.parseLong(asset.endDate));

        final CalendarView calendarView = root.findViewById(R.id.date_view);
        calendarView.setMinDate(System.currentTimeMillis() - 1000);
        calendarView.setDate(startCalendar.getTimeInMillis(), false, true);
        final TextView startTimeDisplay = root.findViewById(R.id.start_time_display);
        startTimeDisplay.setPaintFlags(startTimeDisplay.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        startTimeDisplay.setText(String.format(TIME_BASE, String.format(TIME_START, java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(startCalendar.getTimeInMillis()))));
        startTimeDisplay.setOnClickListener(v -> calendarView.setDate(startCalendar.getTimeInMillis(), false, true));
        final TextView endTimeDisplay = root.findViewById(R.id.end_time_display);
        endTimeDisplay.setPaintFlags(endTimeDisplay.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        endTimeDisplay.setText(String.format(TIME_BASE, String.format(TIME_END, java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(endCalendar.getTimeInMillis()))));
        endTimeDisplay.setOnClickListener(v -> calendarView.setDate(endCalendar.getTimeInMillis(), false, true));

        final ImageView image = root.findViewById(R.id.image);
        if (asset.image != null && asset.image.equals("placeholder")) {
            Glide.with(this).load(R.drawable.placeholder).into(image);
        } else {
            Glide.with(this).load(asset.image).into(image);
        }

        if (!bundle.containsKey("myAdsDetail")) {
            favoriteButton.setChecked(bundle.getBoolean("favorite"));
            favoriteButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    addFavorite();
                } else {
                    removeFavorite();
                }
            });
        } else {
            favoriteButton.setVisibility(View.GONE);
            final Button getAsset = root.findViewById(R.id.get_asset);
            getAsset.setVisibility(View.GONE);
        }

        return root;
    }

    private void removeFavorite() {
        reference.child(DatabaseContract.GLOBAL_USER_PATH).child(mUid).child(DatabaseContract.GLOBAL_FAVORITE_PATH).child(bundle.getString("key")).removeValue();
    }

    private void addFavorite() {
        reference.child(DatabaseContract.GLOBAL_USER_PATH).child(mUid).child(DatabaseContract.GLOBAL_FAVORITE_PATH).child(bundle.getString("key")).setValue(true);
    }
}
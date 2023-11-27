package com.ateamincorporated.rentals.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ateamincorporated.rentals.R;
import com.ateamincorporated.rentals.db.Asset;
import com.ateamincorporated.rentals.db.DatabaseContract;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import static com.ateamincorporated.rentals.ui.home.HomeRecyclerAdapter.EMPTY_IMAGE_WAIT;
import static com.ateamincorporated.rentals.ui.home.HomeRecyclerAdapter.EMPTY_VIEW_WAIT;

public class HomeFragment extends Fragment {

    public static final String TITLE_SECONDARY_SEARCH = "Found %s results for \"%s\"";
    private static final String WELCOME_BASE = "Welcome";
    private static final String WELCOME_SIGNED_OUT = " to a world of rent!";
    private static final String WELCOME_SIGNED_IN = ", %s!";
    private static final String TITLE_SECONDARY = "Where do you want to go today?";
    private static final String CATEGORY_VIEW = "Filter results by.. %s";

    private FirebaseUser mUser;
    private HomeViewModel homeViewModel;
    private CheckBox locationFilter;
    private SearchView searchView;
    private RecyclerView mRecyclerView;
    private HomeRecyclerAdapter mAdapter;

    private String userLocation;
    private String filterSearch;
    private String categoryFilter;
    private boolean filterByLocation;

    private Bundle bundle;
    private ImageView profile;
    private SharedPreferences sharedpreferences;

    private static void unHideView(View v) {
        v.setVisibility(View.VISIBLE);
    }

    private static void hideView(View v) {
        v.setVisibility(View.GONE);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        sharedpreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        TextView emptyView = root.findViewById(R.id.empty);
        ImageView emptyImage = root.findViewById(R.id.empty_image);
        homeViewModel.getEmptyView().observe(getViewLifecycleOwner(), emptyView::setText);
        homeViewModel.getEmptyImage().observe(getViewLifecycleOwner(), emptyImage::setImageResource);

        TextView primaryTitle = root.findViewById(R.id.title);
        TextView secondaryTitle = root.findViewById(R.id.title_2);
        homeViewModel.setSecondaryTitle(TITLE_SECONDARY);

        locationFilter = root.findViewById(R.id.location_filter);
        searchView = root.findViewById(R.id.search);
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mRecyclerView = root.findViewById(R.id.list);
        String uid = null;
        if (mUser != null) {
            uid = mUser.getUid();
        }

        profile = root.findViewById(R.id.image_profile);
        mAdapter = new HomeRecyclerAdapter(homeViewModel, this, mRecyclerView, root.findViewById(R.id.empty_container), uid);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        TextView categoryView = root.findViewById(R.id.category_filter);
        categoryView.setPaintFlags(categoryView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        bundle = getArguments();
        if (bundle != null && bundle.containsKey("categoryFilter")) {
            filterByLocation = bundle.getBoolean("filterByLocation");
            categoryFilter = bundle.getString("categoryFilter");
        }

        String defLocation = sharedpreferences.getString("location", "");

        if (TextUtils.isEmpty(categoryFilter)) {
            homeViewModel.setCategoryView(String.format(CATEGORY_VIEW, ""));
        } else {
            homeViewModel.setCategoryView(String.format(CATEGORY_VIEW, categoryFilter));
        }

        if (mUser != null && TextUtils.isEmpty(userLocation)) {
            FirebaseDatabase.getInstance().getReference().child(DatabaseContract.GLOBAL_USER_PATH).child(mUser.getUid()).child("location").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userLocation = String.valueOf(snapshot.getValue());
                    if (!defLocation.equals(userLocation)) {
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("location", String.valueOf(snapshot.getValue()));
                        editor.apply();
                    }
                    completeInit();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
            completeInit();
        }

        if (bundle != null) {
            locationFilter.setChecked(filterByLocation);
            filter();
        } else {
            filterByLocation = true;
        }

        categoryView.setOnClickListener(v -> {
            if (bundle == null) {
                bundle = new Bundle();
            }
            bundle.putBoolean("filterByLocation", filterByLocation);
            bundle.putString("categoryFilter", categoryFilter);
            Navigation.findNavController(v).navigate(R.id.home_to_category, bundle);
        });

        homeViewModel.getPrimaryTitle().observe(getViewLifecycleOwner(), primaryTitle::setText);
        homeViewModel.getSecondaryTitle().observe(getViewLifecycleOwner(), secondaryTitle::setText);
        homeViewModel.getCategoryView().observe(getViewLifecycleOwner(), categoryView::setText);
        homeViewModel.setEmptyView(EMPTY_VIEW_WAIT);
        homeViewModel.setEmptyImage(EMPTY_IMAGE_WAIT);

        profile.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.home_to_user));

        return root;
    }

    private void completeInit() {
        if (mUser == null) {
            homeViewModel.setPrimaryTitle(WELCOME_BASE + WELCOME_SIGNED_OUT);
            hideView(locationFilter);
            filterByLocation = false;
            filter();
            homeViewModel.setEmptyView(EMPTY_VIEW_WAIT);
            homeViewModel.setEmptyImage(EMPTY_IMAGE_WAIT);
        } else {
            if (mUser.getPhotoUrl() != null) {
                String photo = Objects.requireNonNull(mUser.getPhotoUrl()).toString();
                Glide.with(this).load(photo).into(profile);
            }
            homeViewModel.setPrimaryTitle(WELCOME_BASE + String.format(WELCOME_SIGNED_IN, mUser.getDisplayName()));
            unHideView(locationFilter);
            filter();
            locationFilter.setOnCheckedChangeListener((buttonView, isChecked) -> {
                filterByLocation = isChecked;
                filter();
            });
        }
        mRecyclerView.setAdapter(mAdapter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // filter recycler view when query submitted
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterSearch = query;
                filter();
                if (TextUtils.isEmpty(query)) {
                    homeViewModel.setSecondaryTitle(TITLE_SECONDARY);
                }
                return false;
            }

            // filter recycler view when text is changed
            @Override
            public boolean onQueryTextChange(String query) {
                return onQueryTextSubmit(query);
            }
        });
//        if (mAdapter.getItemCount() == 0) {
//            homeViewModel.setEmptyView(HomeRecyclerAdapter.EMPTY_VIEW_NO_ITEM);
//            homeViewModel.setEmptyImage(EMPTY_IMAGE_NO_ITEM);
//        }
    }

    private void filter() {
        if (!TextUtils.isEmpty(categoryFilter) && !TextUtils.isEmpty(filterSearch) && filterByLocation) {
            mAdapter.getFilter(HomeRecyclerAdapter.FILTER_TYPE_CATEGORY_SEARCH_LOCATION, userLocation, categoryFilter).filter(filterSearch);
        } else if (TextUtils.isEmpty(categoryFilter) && !TextUtils.isEmpty(filterSearch) && filterByLocation) {
            mAdapter.getFilter(HomeRecyclerAdapter.FILTER_TYPE_SEARCH_LOCATION, userLocation, categoryFilter).filter(filterSearch);
        } else if (!TextUtils.isEmpty(categoryFilter) && TextUtils.isEmpty(filterSearch) && filterByLocation) {
            mAdapter.getFilter(HomeRecyclerAdapter.FILTER_TYPE_CATEGORY_LOCATION, userLocation, categoryFilter).filter(filterSearch);
        } else if (!TextUtils.isEmpty(categoryFilter) && !TextUtils.isEmpty(filterSearch) && !filterByLocation) {
            mAdapter.getFilter(HomeRecyclerAdapter.FILTER_TYPE_CATEGORY_SEARCH, userLocation, categoryFilter).filter(filterSearch);
        } else if (!TextUtils.isEmpty(categoryFilter) && TextUtils.isEmpty(filterSearch) && !filterByLocation) {
            mAdapter.getFilter(HomeRecyclerAdapter.FILTER_TYPE_CATEGORY, userLocation, categoryFilter).filter(filterSearch);
        } else if (TextUtils.isEmpty(categoryFilter) && !TextUtils.isEmpty(filterSearch) && !filterByLocation) {
            mAdapter.getFilter(HomeRecyclerAdapter.FILTER_TYPE_SEARCH, userLocation, categoryFilter).filter(filterSearch);
        } else if (TextUtils.isEmpty(categoryFilter) && TextUtils.isEmpty(filterSearch) && filterByLocation) {
            mAdapter.getFilter(HomeRecyclerAdapter.FILTER_TYPE_LOCATION, userLocation, categoryFilter).filter(filterSearch);
        } else if (TextUtils.isEmpty(categoryFilter) && TextUtils.isEmpty(filterSearch) && !filterByLocation) {
            mAdapter.getFilter(HomeRecyclerAdapter.FILTER_TYPE_LOCATION, null, null).filter(null);
        }
    }

    void navigateToDetailView(Asset asset, String key, Boolean favorite) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putBoolean("filterByLocation", filterByLocation);
        bundle.putString("categoryFilter", categoryFilter);
        bundle.putSerializable("asset", asset);
        bundle.putBoolean("favorite", favorite);
        bundle.putString("key", key);
        Navigation.findNavController(mRecyclerView).navigate(R.id.home_to_detail, bundle);
    }
}
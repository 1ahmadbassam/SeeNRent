package com.ateamincorporated.rentals.ui.myAds;

import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ateamincorporated.rentals.R;
import com.ateamincorporated.rentals.db.Asset;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.ateamincorporated.rentals.ui.myAds.MyAdsRecyclerAdapter.EMPTY_IMAGE_WAIT;
import static com.ateamincorporated.rentals.ui.myAds.MyAdsRecyclerAdapter.EMPTY_VIEW_WAIT;
import static com.ateamincorporated.rentals.ui.myAds.MyAdsRecyclerAdapter.FILTER_TYPE_CATEGORY;

public class MyAdsFragment extends Fragment {


    private static final String ERROR_NO_USER = "You need to be signed in \nto use this feature.";
    private static final String CATEGORY_VIEW = "Filter results by.. %s";

    private RecyclerView mRecyclerView;
    private MyAdsRecyclerAdapter mAdapter;

    private String filterSearch;
    private String categoryFilter;

    private Bundle bundle;

    private static void hideView(View v) {
        v.setVisibility(View.INVISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_my_ads, container, false);

        MyAdsViewModel myAdsViewModel = new ViewModelProvider(this).get(MyAdsViewModel.class);

        TextView emptyView = root.findViewById(R.id.empty);
        ImageView emptyImage = root.findViewById(R.id.empty_image);
        myAdsViewModel.getEmptyView().observe(getViewLifecycleOwner(), emptyView::setText);
        myAdsViewModel.getEmptyImage().observe(getViewLifecycleOwner(), emptyImage::setImageResource);

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null) {
            myAdsViewModel.setEmptyView(ERROR_NO_USER);
            hideView(emptyImage);
            return root;
        }
        myAdsViewModel.setEmptyView(EMPTY_VIEW_WAIT);
        myAdsViewModel.setEmptyImage(EMPTY_IMAGE_WAIT);

        SearchView searchView = root.findViewById(R.id.search);

        mRecyclerView = root.findViewById(R.id.list);
        mAdapter = new MyAdsRecyclerAdapter(myAdsViewModel, this, mRecyclerView, root.findViewById(R.id.empty_container), mUser.getUid());

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        TextView categoryView = root.findViewById(R.id.category_filter);
        categoryView.setPaintFlags(categoryView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        bundle = getArguments();
        if (bundle != null && bundle.containsKey("categoryFilter")) {
            categoryFilter = bundle.getString("categoryFilter");
        } else if (bundle != null) {
            filter();
        }

        if (TextUtils.isEmpty(categoryFilter)) {
            myAdsViewModel.setCategoryView(String.format(CATEGORY_VIEW, ""));
        } else {
            myAdsViewModel.setCategoryView(String.format(CATEGORY_VIEW, categoryFilter));
        }

        mRecyclerView.setAdapter(mAdapter);
        mAdapter.getFilter(FILTER_TYPE_CATEGORY, null).filter(null);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // filter recycler view when query submitted
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterSearch = query;
                filter();
                return false;
            }

            // filter recycler view when text is changed
            @Override
            public boolean onQueryTextChange(String query) {
                return onQueryTextSubmit(query);
            }
        });

        categoryView.setOnClickListener(v -> {
            if (bundle == null) {
                bundle = new Bundle();
            }
            bundle.putString("categoryFilter", categoryFilter);
            bundle.putBoolean("myAds", true);
            Navigation.findNavController(v).navigate(R.id.myAds_to_category, bundle);
        });

        myAdsViewModel.getCategoryView().observe(getViewLifecycleOwner(), categoryView::setText);
        filter();

        return root;
    }

    void navigateToDetailView(Asset asset, String key) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putString("categoryFilter", categoryFilter);
        bundle.putSerializable("asset", asset);
        bundle.putBoolean("myAdsDetail", true);
        bundle.putString("key", key);
        Navigation.findNavController(mRecyclerView).navigate(R.id.myAds_to_detail, bundle);
    }

    void navigateToEditView(Asset asset, String key) {
        if (bundle == null) {
            bundle = new Bundle();
        } else {
            bundle.remove("myAds");
        }
        bundle.putString("name", asset.name);
        bundle.putString("description", asset.description);
        bundle.putString("image", asset.image);
        bundle.putString("startDate", asset.startDate);
        bundle.putString("endDate", asset.endDate);
        bundle.putString("priceUnit", asset.priceUnit);
        bundle.putString("price", asset.price);
        bundle.putString("category", asset.category);
        bundle.putString("categoryFilter", asset.category);
//        bundle.putString("location", asset.genericLocation);
        bundle.putBoolean("myAdsEdit", true);
        bundle.putString("key", key);
        Navigation.findNavController(mRecyclerView).navigate(R.id.myAds_to_edit, bundle);
    }

    private void filter() {
        if (!TextUtils.isEmpty(categoryFilter) && !TextUtils.isEmpty(filterSearch)) {
            mAdapter.getFilter(MyAdsRecyclerAdapter.FILTER_TYPE_CATEGORY_SEARCH, categoryFilter).filter(filterSearch);
        } else if (!TextUtils.isEmpty(categoryFilter) && TextUtils.isEmpty(filterSearch)) {
            mAdapter.getFilter(MyAdsRecyclerAdapter.FILTER_TYPE_CATEGORY, categoryFilter).filter(filterSearch);
        } else if (TextUtils.isEmpty(categoryFilter) && !TextUtils.isEmpty(filterSearch)) {
            mAdapter.getFilter(MyAdsRecyclerAdapter.FILTER_TYPE_SEARCH, categoryFilter).filter(filterSearch);
        } else if (TextUtils.isEmpty(categoryFilter) && TextUtils.isEmpty(filterSearch)) {
            mAdapter.getFilter(MyAdsRecyclerAdapter.FILTER_TYPE_CATEGORY, null).filter(null);
        }
    }
}
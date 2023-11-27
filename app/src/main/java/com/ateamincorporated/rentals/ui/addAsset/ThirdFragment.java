package com.ateamincorporated.rentals.ui.addAsset;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ateamincorporated.rentals.R;

public class ThirdFragment extends Fragment {

    static final String TITLE_WHERE = "Where";
    static final String TITLE_IN = " in ";
    static final String TITLE_DOES_BELONG = " does it belong?";
    static final String TITLE_YOU_NEED = " do you need?";

    static final int SOURCE_ADD_ASSET = 0;
    static final int SOURCE_ASSET_DISPLAY = 1;
    static final int SOURCE_ASSET_LIST_USER = 2;

    private Bundle bundle;
    private int mSource = 0;

    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_add_asset_third, container, false);

        bundle = getArguments();

        if (bundle != null && bundle.containsKey("categoryFilter") && bundle.containsKey("myAds")) {
            mSource = SOURCE_ASSET_LIST_USER;
        } else if (bundle != null && bundle.containsKey("categoryFilter") && !bundle.containsKey("myAdsEdit")) {
            mSource = SOURCE_ASSET_DISPLAY;
        } else {
            mSource = SOURCE_ADD_ASSET;
        }

        RecyclerView recyclerView = root.findViewById(R.id.category_list);
//        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        ThirdViewModel thirdViewModel = new ViewModelProvider(this).get(ThirdViewModel.class);
        final TextView title = root.findViewById(R.id.title);
        if (mSource == SOURCE_ADD_ASSET) {
            thirdViewModel.setTitle(TITLE_WHERE + TITLE_DOES_BELONG);
            recyclerView.setAdapter(new ThirdRecyclerAdapter(thirdViewModel, this, SOURCE_ADD_ASSET));
        } else if (mSource == SOURCE_ASSET_DISPLAY || mSource == SOURCE_ASSET_LIST_USER) {
            thirdViewModel.setTitle(TITLE_WHERE + TITLE_YOU_NEED);
            recyclerView.setAdapter(new ThirdRecyclerAdapter(thirdViewModel, this, SOURCE_ASSET_DISPLAY));
        }
        thirdViewModel.getTitle().observe(getViewLifecycleOwner(), title::setText);

        return root;
    }

    public void navigateOnGetCategory(String category, String majorCategory) {
        if (mSource == SOURCE_ADD_ASSET) {
            bundle.putString("category", category);
            bundle.putString("majorCategory", majorCategory);
            Navigation.findNavController(root).navigate(R.id.third_to_fourth, bundle);
        } else if (bundle.containsKey("favorites")) {
            bundle.putString("categoryFilter", category);
            Navigation.findNavController(root).navigate(R.id.category_to_favorites, bundle);
        } else if (mSource == SOURCE_ASSET_DISPLAY) {
            bundle.putString("categoryFilter", category);
            Navigation.findNavController(root).navigate(R.id.category_to_home, bundle);
        } else if (mSource == SOURCE_ASSET_LIST_USER) {
            bundle.putString("categoryFilter", category);
            Navigation.findNavController(root).navigate(R.id.category_to_myAds, bundle);
        }
    }
}
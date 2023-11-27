package com.ateamincorporated.rentals.ui.home;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.ateamincorporated.rentals.R;
import com.ateamincorporated.rentals.db.Asset;
import com.ateamincorporated.rentals.db.DatabaseContract;
import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HomeRecyclerAdapter extends RecyclerView.Adapter<HomeRecyclerAdapter.ViewHolder> implements Filterable {

    public static final int FILTER_TYPE_LOCATION = 0;
    public static final int FILTER_TYPE_SEARCH = 1;
    public static final int FILTER_TYPE_SEARCH_LOCATION = 2;
    public static final int FILTER_TYPE_CATEGORY = 3;
    public static final int FILTER_TYPE_CATEGORY_SEARCH = 4;
    public static final int FILTER_TYPE_CATEGORY_LOCATION = 5;
    public static final int FILTER_TYPE_CATEGORY_SEARCH_LOCATION = 6;
    public static final String EMPTY_VIEW_NO_ITEM = "There are no items for the query specified.";
    public static final int EMPTY_IMAGE_NO_ITEM = R.drawable.ic_baseline_cancel;
    public static final String EMPTY_VIEW_WAIT = "Please wait... loading list.";
    public static final int EMPTY_IMAGE_WAIT = R.drawable.ic_baseline_refresh;
    private static final int FILTER_TYPE_INVALID = -1;
    private static final String PRICE_FORMAT = "%s L.L/%s";
    private final List<Asset> mDataSet;
    private final List<String> mDataSetFavorite;
    private final List<String> mDataIndex;
    private final HomeViewModel homeViewModel;
    private final HomeFragment fragment;
    private final String mUid;
    private final DatabaseReference reference;
    private final RecyclerView mRecyclerView;
    private final View mEmptyView;
    private List<Asset> mDataSetFiltered;
    private CharSequence query;
    private String category;
    private String location;
    private int filterType;

    /**
     * Initialize the dataset of the Adapter.
     */
    public HomeRecyclerAdapter(HomeViewModel homeViewModel, HomeFragment fragment, RecyclerView recyclerView, View emptyView, String uid) {
        filterType = FILTER_TYPE_INVALID;
        reference = FirebaseDatabase.getInstance().getReference();
        mDataSet = new ArrayList<>();
        mDataIndex = new ArrayList<>();
        mDataSetFavorite = new ArrayList<>();
        mDataSetFiltered = new ArrayList<>();
        this.homeViewModel = homeViewModel;
        this.fragment = fragment;
        mRecyclerView = recyclerView;
        mEmptyView = emptyView;
        mUid = uid;
        completeInit();
    }

    public void notifyAdapterDataSetChanged() {
//        homeViewModel.setEmptyView(EMPTY_VIEW_WAIT);
//        homeViewModel.setEmptyImage(EMPTY_IMAGE_WAIT);
        if (!(TextUtils.isEmpty(query) && TextUtils.isEmpty(location) && TextUtils.isEmpty(category) && filterType == FILTER_TYPE_INVALID)) {
            getFilter(filterType, location, category).filter(query);
        } else {
            notifyDataSetChanged();
            mEmptyView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void completeInit() {
        reference.child(DatabaseContract.GLOBAL_ASSET_PATH).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Asset asset = snapshot.getValue(Asset.class);
                mDataIndex.add(snapshot.getKey());
                mDataSet.add(asset);
                notifyAdapterDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Asset asset = snapshot.getValue(Asset.class);
                int position = mDataIndex.indexOf(snapshot.getKey());
                mDataIndex.remove(position);
                mDataSet.remove(position);
                mDataIndex.add(snapshot.getKey());
                mDataSet.add(asset);
                notifyAdapterDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int position = mDataIndex.indexOf(snapshot.getKey());
                mDataIndex.remove(position);
                mDataSet.remove(position);
                notifyAdapterDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        if (mUid != null) {
            reference.child(DatabaseContract.GLOBAL_USER_PATH).child(mUid).child(DatabaseContract.GLOBAL_FAVORITE_PATH)
//                    .addChildEventListener(new ChildEventListener() {
//                @Override
//                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                    if (mDataIndex.contains(snapshot.getKey())) {
//                        mDataSetFavorite.add(mDataSet.get(mDataIndex.indexOf(snapshot.getKey())));
//                        notifyAdapterDataSetChanged();
//                    }
//                }
//
//                @Override
//                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                }
//
//                @Override
//                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//                    if (mDataIndex.contains(snapshot.getKey())) {
//                        mDataSetFavorite.remove(mDataSet.get(mDataIndex.indexOf(snapshot.getKey())));
//                        notifyAdapterDataSetChanged();
//                    }
//                }
//
//                @Override
//                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                }
//            });
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot d : snapshot.getChildren()) {
                                notifyAdapterDataSetChanged();
                                mDataSetFavorite.add(d.getKey());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public HomeRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_item_home, viewGroup, false);

        if (mUid == null) {
            v.getRootView().findViewById(R.id.favorite).setVisibility(View.GONE);
        }

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NotNull HomeRecyclerAdapter.ViewHolder holder, int position) {
        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        Asset asset = mDataSetFiltered.get(position);
        holder.getRoot().setOnClickListener(v -> fragment.navigateToDetailView(asset, mDataIndex.get(mDataSet.indexOf(asset)), mDataSetFavorite.contains(mDataIndex.get(mDataSet.indexOf(asset)))));
        if (asset != null) {
            holder.getName().setText(asset.name);
            holder.getName().setSelected(true);
            holder.getLocation().setText(asset.genericLocation);
            holder.getPrice().setText(String.format(PRICE_FORMAT, asset.price, asset.priceUnit));
            String date = asset.timeOfCreation;
            if (date != null && !TextUtils.isEmpty(date)) {
                holder.getDateOfCreation().setText(new SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(new Date(Long.parseLong(date))));
            }
            if (asset.image != null && asset.image.equals("placeholder")) {
                Glide.with(holder.getRoot()).load(R.drawable.placeholder).into(holder.getImage());
            } else {
                Glide.with(holder.getRoot()).load(asset.image).into(holder.getImage());
            }
            if (mUid != null) {
                boolean isChecked = mDataSetFavorite.contains(mDataIndex.get(mDataSet.indexOf(asset)));
                holder.getFavouriteButton().setChecked(isChecked);
                holder.getFavouriteButton().setOnClickListener(v -> {
                    if (isChecked) {
                        removeFavorite(asset, v);
                    } else {
                        addFavorite(asset, v);
                    }
                });
            }
        }
    }

    private void removeFavorite(@NotNull Asset asset, View view) {
        mDataSetFavorite.remove(mDataIndex.get(mDataSet.indexOf(asset)));
        reference.child(DatabaseContract.GLOBAL_USER_PATH).child(mUid).child(DatabaseContract.GLOBAL_FAVORITE_PATH).child(mDataIndex.get(mDataSet.indexOf(asset))).removeValue();
        view.setOnClickListener(v -> addFavorite(asset, view));
    }

    private void addFavorite(@NotNull Asset asset, View view) {
        mDataSetFavorite.add(mDataIndex.get(mDataSet.indexOf(asset)));
        reference.child(DatabaseContract.GLOBAL_USER_PATH).child(mUid).child(DatabaseContract.GLOBAL_FAVORITE_PATH).child(mDataIndex.get(mDataSet.indexOf(asset))).setValue(true);
        view.setOnClickListener(v -> removeFavorite(asset, view));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        int size = mDataSetFiltered.size();
        if (size == 0 && filterType == FILTER_TYPE_INVALID) {
            homeViewModel.setEmptyView(EMPTY_VIEW_WAIT);
            homeViewModel.setEmptyImage(EMPTY_IMAGE_WAIT);
        } else if (size != 0 && Objects.equals(homeViewModel.getEmptyView().getValue(), EMPTY_VIEW_NO_ITEM)) {
            homeViewModel.setEmptyView(EMPTY_VIEW_WAIT);
            homeViewModel.setEmptyImage(EMPTY_IMAGE_WAIT);
        }
        return size;
    }


    @Override
    public void onBindViewHolder(@NonNull HomeRecyclerAdapter.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    //    @Override
    public Filter getFilter(int filterType, String location, String category) {
        if (location == null) {
            location = "";
        }
        if (category == null) {
            category = "";
        }
        this.filterType = filterType;
        this.location = location;
        this.category = category;
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence query) {
                HomeRecyclerAdapter.this.query = query;
                String queryString;
                if (query == null) {
                    queryString = "";
                } else {
                    queryString = query.toString();
                }
                if (queryString.isEmpty() && HomeRecyclerAdapter.this.location.isEmpty() && HomeRecyclerAdapter.this.category.isEmpty()) {
                    mDataSetFiltered.clear();
                    mDataSetFiltered.addAll(mDataSet);
                } else {
                    List<Asset> filteredList = new ArrayList<>();
                    for (Asset row : mDataSet) {

                        if (filterType == FILTER_TYPE_CATEGORY && row.category.equals(HomeRecyclerAdapter.this.category)) {
                            filteredList.add(row);
                        }

                        if (filterType == FILTER_TYPE_LOCATION && row.genericLocation.equals(HomeRecyclerAdapter.this.location)) {
                            filteredList.add(row);
                        }

                        if (filterType == FILTER_TYPE_CATEGORY_LOCATION && row.genericLocation.equals(HomeRecyclerAdapter.this.location) && row.category.equals(HomeRecyclerAdapter.this.category)) {
                            filteredList.add(row);
                        }

                        if (filterType == FILTER_TYPE_SEARCH && row.name.toLowerCase().contains(queryString.toLowerCase())) {
                            filteredList.add(row);
                        }

                        if (filterType == FILTER_TYPE_CATEGORY_SEARCH && row.name.toLowerCase().contains(queryString.toLowerCase()) && row.category.equals(HomeRecyclerAdapter.this.category)) {
                            filteredList.add(row);
                        }

                        if (filterType == FILTER_TYPE_SEARCH_LOCATION && row.name.toLowerCase().contains(queryString.toLowerCase()) && row.genericLocation.equals(HomeRecyclerAdapter.this.location)) {
                            filteredList.add(row);
                        }

                        if (filterType == FILTER_TYPE_CATEGORY_SEARCH_LOCATION && row.name.toLowerCase().contains(queryString.toLowerCase()) && row.genericLocation.equals(HomeRecyclerAdapter.this.location) && row.category.equals(HomeRecyclerAdapter.this.category)) {
                            filteredList.add(row);
                        }
                    }

                    mDataSetFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mDataSetFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mDataSetFiltered = (ArrayList<Asset>) filterResults.values;
                if (mDataSetFiltered == null) {
                    mDataSetFiltered = new ArrayList<>();
                } else {
                    try {
                        Collections.sort(mDataSetFiltered, (o1, o2) -> o1.name.compareTo(o2.name));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                notifyDataSetChanged();
                if (mDataSet.size() != 0 && mDataSetFiltered.size() == 0) {
                    homeViewModel.setEmptyView(EMPTY_VIEW_NO_ITEM);
                    homeViewModel.setEmptyImage(EMPTY_IMAGE_NO_ITEM);
                    mRecyclerView.setVisibility(View.INVISIBLE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    if (mEmptyView.getVisibility() == View.VISIBLE && getItemCount() != 0) {
                        mEmptyView.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                    }
                }
                if (filterType == FILTER_TYPE_SEARCH || filterType == FILTER_TYPE_CATEGORY_SEARCH || filterType == FILTER_TYPE_SEARCH_LOCATION || filterType == FILTER_TYPE_CATEGORY_SEARCH_LOCATION) {
                    homeViewModel.setSecondaryTitle(String.format(HomeFragment.TITLE_SECONDARY_SEARCH, getItemCount(), charSequence));
                }
            }
        };
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView location;
        private final TextView price;
        private final TextView dateOfCreation;
        private final ImageView image;
        private final CheckBox favouriteButton;

        private final View root;

        public ViewHolder(View view) {
            super(view);
            root = view;

            name = view.findViewById(R.id.name);
            location = view.findViewById(R.id.location);
            price = view.findViewById(R.id.price);
            dateOfCreation = view.findViewById(R.id.date_of_creation);
            image = view.findViewById(R.id.image);
            favouriteButton = view.findViewById(R.id.favorite);
        }

        public View getRoot() {
            return root;
        }

        public TextView getName() {
            return name;
        }

        public TextView getLocation() {
            return location;
        }

        public TextView getPrice() {
            return price;
        }

        public TextView getDateOfCreation() {
            return dateOfCreation;
        }

        public ImageView getImage() {
            return image;
        }

        public CheckBox getFavouriteButton() {
            return favouriteButton;
        }
    }
}

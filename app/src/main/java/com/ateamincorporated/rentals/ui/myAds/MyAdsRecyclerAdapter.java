package com.ateamincorporated.rentals.ui.myAds;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ateamincorporated.rentals.R;
import com.ateamincorporated.rentals.db.Asset;
import com.ateamincorporated.rentals.db.DatabaseContract;
import com.bumptech.glide.Glide;
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

public class MyAdsRecyclerAdapter extends RecyclerView.Adapter<MyAdsRecyclerAdapter.ViewHolder> implements Filterable {

    public static final int FILTER_TYPE_SEARCH = 1;
    public static final int FILTER_TYPE_CATEGORY = 2;
    public static final int FILTER_TYPE_CATEGORY_SEARCH = 3;
    public static final String EMPTY_VIEW_NO_ITEM = "There are no items for the query specified.";
    public static final int EMPTY_IMAGE_NO_ITEM = R.drawable.ic_baseline_cancel;
    public static final String EMPTY_VIEW_WAIT = "Please wait... loading list.";
    public static final int EMPTY_IMAGE_WAIT = R.drawable.ic_baseline_refresh;
    private static final int FILTER_TYPE_INVALID = -1;
    private static final String PRICE_FORMAT = "%s L.L/%s";
    private final List<Asset> mDataSet;
    private final List<String> mDataIndex;
    private final MyAdsViewModel myAdsViewModel;
    private final MyAdsFragment fragment;
    private final String mUid;
    private final RecyclerView mRecyclerView;
    private final View mEmptyView;
    private List<Asset> mDataSetFiltered;
    private CharSequence query;
    private String category;
    private int filterType;

    /**
     * Initialize the dataset of the Adapter.
     */
    public MyAdsRecyclerAdapter(MyAdsViewModel myAdsViewModel, MyAdsFragment fragment, RecyclerView recyclerView, View emptyView, String uid) {
        filterType = FILTER_TYPE_INVALID;
        mDataSet = new ArrayList<>();
        mDataIndex = new ArrayList<>();
        mDataSetFiltered = new ArrayList<>();
        this.myAdsViewModel = myAdsViewModel;
        this.fragment = fragment;
        mRecyclerView = recyclerView;
        mEmptyView = emptyView;
        mUid = uid;
        completeInit();
    }

    public void notifyAdapterDataSetChanged() {
        if (!(TextUtils.isEmpty(query) && TextUtils.isEmpty(category) && filterType == FILTER_TYPE_INVALID)) {
            getFilter(filterType, category).filter(query);
        } else {
            notifyDataSetChanged();
            mEmptyView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void completeInit() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        myAdsViewModel.setEmptyView(EMPTY_VIEW_WAIT);
        reference.child(DatabaseContract.GLOBAL_USER_PATH).child(mUid).child(DatabaseContract.GLOBAL_ASSET_PATH).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<String> assetIds = new ArrayList<>();
                    myAdsViewModel.setEmptyView(EMPTY_VIEW_WAIT);
                    for (DataSnapshot d : snapshot.getChildren()) {
                        assetIds.add(d.getKey());
                    }
                    for (String assetId : assetIds) {
                        reference.child(DatabaseContract.GLOBAL_ASSET_PATH).child(assetId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                mDataSet.add(snapshot.getValue(Asset.class));
                                mDataIndex.add(assetId);
                                notifyAdapterDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                } else {
                    myAdsViewModel.setEmptyView(EMPTY_VIEW_NO_ITEM);
                    myAdsViewModel.setEmptyImage(EMPTY_IMAGE_NO_ITEM);
                    mRecyclerView.setVisibility(View.INVISIBLE);
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public MyAdsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_item_my_ads, viewGroup, false);

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NotNull MyAdsRecyclerAdapter.ViewHolder holder, int position) {
        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        Asset asset = mDataSetFiltered.get(position);
        holder.getRoot().setOnClickListener(v -> fragment.navigateToDetailView(asset, mDataIndex.get(mDataSet.indexOf(asset))));
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
        }
        holder.getEditButton().setOnClickListener(v -> fragment.navigateToEditView(asset, mDataIndex.get(mDataSet.indexOf(asset))));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        int size = mDataSetFiltered.size();
        if (size == 0 && filterType == FILTER_TYPE_INVALID) {
            myAdsViewModel.setEmptyView(EMPTY_VIEW_WAIT);
            myAdsViewModel.setEmptyImage(EMPTY_IMAGE_WAIT);
        } else if (size != 0 && Objects.equals(myAdsViewModel.getEmptyView().getValue(), EMPTY_VIEW_NO_ITEM)) {
            myAdsViewModel.setEmptyView(EMPTY_VIEW_WAIT);
            myAdsViewModel.setEmptyImage(EMPTY_IMAGE_WAIT);
        }
        return size;
    }


    @Override
    public void onBindViewHolder(@NonNull MyAdsRecyclerAdapter.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    //    @Override
    public Filter getFilter(int filterType, String category) {
        if (category == null) {
            category = "";
        }
        this.filterType = filterType;
        this.category = category;
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence query) {
                MyAdsRecyclerAdapter.this.query = query;
                String queryString;
                if (query == null) {
                    queryString = "";
                } else {
                    queryString = query.toString();
                }
                if (queryString.isEmpty() && MyAdsRecyclerAdapter.this.category.isEmpty()) {
                    mDataSetFiltered.clear();
                    mDataSetFiltered.addAll(mDataSet);
                } else {
                    List<Asset> filteredList = new ArrayList<>();
                    for (Asset row : mDataSet) {

                        if (filterType == FILTER_TYPE_CATEGORY && row.category.equals(MyAdsRecyclerAdapter.this.category)) {
                            filteredList.add(row);
                        }

                        if (filterType == FILTER_TYPE_SEARCH && row.name.toLowerCase().contains(queryString.toLowerCase())) {
                            filteredList.add(row);
                        }

                        if (filterType == FILTER_TYPE_CATEGORY_SEARCH && row.name.toLowerCase().contains(queryString.toLowerCase()) && row.category.equals(MyAdsRecyclerAdapter.this.category)) {
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
                    myAdsViewModel.setEmptyView(EMPTY_VIEW_NO_ITEM);
                    myAdsViewModel.setEmptyImage(EMPTY_IMAGE_NO_ITEM);
                    mRecyclerView.setVisibility(View.INVISIBLE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    if (mEmptyView.getVisibility() == View.VISIBLE && getItemCount() != 0) {
                        mEmptyView.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                    }
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
        private final ImageView editButton;

        private final View root;

        public ViewHolder(View view) {
            super(view);
            root = view;

            name = view.findViewById(R.id.name);
            location = view.findViewById(R.id.location);
            price = view.findViewById(R.id.price);
            dateOfCreation = view.findViewById(R.id.date_of_creation);
            image = view.findViewById(R.id.image);
            editButton = view.findViewById(R.id.edit);
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

        public ImageView getEditButton() {
            return editButton;
        }
    }
}

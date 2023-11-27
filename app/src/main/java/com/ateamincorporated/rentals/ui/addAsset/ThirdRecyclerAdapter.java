package com.ateamincorporated.rentals.ui.addAsset;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.ateamincorporated.rentals.R;
import com.ateamincorporated.rentals.db.DatabaseContract;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ThirdRecyclerAdapter extends RecyclerView.Adapter<ThirdRecyclerAdapter.ViewHolder> {


    private final HashMap<String, ArrayList<String>> mDataSource;
    private final ThirdViewModel mViewModel;
    private final ThirdFragment mFragment;
    private final ArrayList<String> mDataSet;
    private final int mSource;
    private String majorCategory;
    private int mDataState;

    /**
     * Initialize the dataset of the Adapter.
     */
    public ThirdRecyclerAdapter(ThirdViewModel viewModel, ThirdFragment fragment, int source) {
        mSource = source;
        mDataSource = new HashMap<>();
        mDataSet = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(DatabaseContract.GLOBAL_CATEGORY_PATH);
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ArrayList<String> minorCategories = new ArrayList<>();
                for (DataSnapshot d : snapshot.getChildren()) {
                    minorCategories.add(d.getKey());
                }
                mDataSource.put(snapshot.getKey(), minorCategories);
                checkDataState();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ArrayList<String> minorCategories = new ArrayList<>();
                for (DataSnapshot d : snapshot.getChildren()) {
                    minorCategories.remove(d.getKey());
                    minorCategories.add(d.getKey());
                }
                mDataSource.remove(snapshot.getKey());
                mDataSource.put(snapshot.getKey(), minorCategories);
                checkDataState();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                mDataSource.remove(snapshot.getKey());
                checkDataState();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

            private void checkDataState() {
                if (mDataState == 0) {
                    mDataSet.clear();
                    mDataSet.addAll(mDataSource.keySet());
                    if (mSource == ThirdFragment.SOURCE_ASSET_DISPLAY || mSource == ThirdFragment.SOURCE_ASSET_LIST_USER) {
                        mDataSet.add("No category");
                    }
                    Collections.sort(mDataSet);
                } else if (mDataState == 1) {
                    mDataSet.clear();
                    mDataSet.addAll(Objects.requireNonNull(mDataSource.get(majorCategory)));
                    Collections.sort(mDataSet);
                } else {
                    throw new IllegalStateException();
                }
                notifyDataSetChanged();
            }
        });
        if (mSource == ThirdFragment.SOURCE_ASSET_DISPLAY || mSource == ThirdFragment.SOURCE_ASSET_LIST_USER) {
            mDataSet.add("No category");
        }
        Collections.sort(mDataSet);
        mDataState = 0;
        mViewModel = viewModel;
        mFragment = fragment;
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_item_add_asset_third, viewGroup, false);

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.getView().setText(mDataSet.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        holder.getView().setSelected(true);
        holder.getRoot().setOnClickListener(v -> {
            String category = holder.getView().getText().toString();
            if (category.equals("No category")) {
                mFragment.navigateOnGetCategory("", "");
            } else {
                if (mDataState == 0) {
                    majorCategory = category;
                    mDataSet.clear();
                    mDataSet.addAll(Objects.requireNonNull(mDataSource.get(majorCategory)));
                    Collections.sort(mDataSet);
                    notifyDataSetChanged();
                    if (mSource == ThirdFragment.SOURCE_ADD_ASSET) {
                        mViewModel.setTitle(ThirdFragment.TITLE_WHERE + ThirdFragment.TITLE_IN + category + ThirdFragment.TITLE_DOES_BELONG);
                    } else if (mSource == ThirdFragment.SOURCE_ASSET_DISPLAY || mSource == ThirdFragment.SOURCE_ASSET_LIST_USER) {
                        mViewModel.setTitle(ThirdFragment.TITLE_WHERE + ThirdFragment.TITLE_IN + category + ThirdFragment.TITLE_YOU_NEED);
                    }
                    mDataState = 1;
                } else if (mDataState == 1) {
                    mFragment.navigateOnGetCategory(category, majorCategory);
                } else {
                    throw new IllegalStateException();
                }
            }
        });
        super.onBindViewHolder(holder, position, payloads);
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView category;
        private final View root;

        public ViewHolder(View view) {
            super(view);
            root = view;
            category = view.findViewById(R.id.category);
        }

        public TextView getView() {
            return category;
        }

        public View getRoot() {
            return root;
        }
    }
}

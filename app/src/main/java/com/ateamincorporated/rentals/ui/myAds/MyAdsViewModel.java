package com.ateamincorporated.rentals.ui.myAds;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MyAdsViewModel extends ViewModel {
    private final MutableLiveData<String> mCategoryView;
    private final MutableLiveData<String> mEmptyView;
    private final MutableLiveData<Integer> mEmptyImage;

    public MyAdsViewModel() {
        mCategoryView = new MutableLiveData<>();
        mEmptyView = new MutableLiveData<>();
        mEmptyImage = new MutableLiveData<>();
    }

    public LiveData<String> getCategoryView() {
        return mCategoryView;
    }

    public void setCategoryView(String categoryView) {
        mCategoryView.setValue(categoryView);
    }

    public LiveData<String> getEmptyView() {
        return mEmptyView;
    }

    public void setEmptyView(String emptyView) {
        mEmptyView.setValue(emptyView);
    }

    public LiveData<Integer> getEmptyImage() {
        return mEmptyImage;
    }

    public void setEmptyImage(Integer resID) {
        mEmptyImage.setValue(resID);
    }
}

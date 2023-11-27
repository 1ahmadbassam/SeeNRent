package com.ateamincorporated.rentals.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mPrimaryTitle;
    private final MutableLiveData<String> mSecondaryTitle;
    private final MutableLiveData<String> mCategoryView;
    private final MutableLiveData<String> mEmptyView;
    private final MutableLiveData<Integer> mEmptyImage;

    public HomeViewModel() {
        mPrimaryTitle = new MutableLiveData<>();
        mSecondaryTitle = new MutableLiveData<>();
        mCategoryView = new MutableLiveData<>();
        mEmptyView = new MutableLiveData<>();
        mEmptyImage = new MutableLiveData<>();
    }

    public LiveData<String> getPrimaryTitle() {
        return mPrimaryTitle;
    }

    public void setPrimaryTitle(String primaryTitle) {
        mPrimaryTitle.setValue(primaryTitle);
    }

    public LiveData<String> getSecondaryTitle() {
        return mSecondaryTitle;
    }

    public void setSecondaryTitle(String secondaryTitle) {
        mSecondaryTitle.setValue(secondaryTitle);
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
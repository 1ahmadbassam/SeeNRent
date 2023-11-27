package com.ateamincorporated.rentals.ui.addAsset;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ThirdViewModel extends ViewModel {

    private final MutableLiveData<String> mTitle;

    public ThirdViewModel() {
        mTitle = new MutableLiveData<>();
    }

    public LiveData<String> getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle.setValue(title);
    }
}
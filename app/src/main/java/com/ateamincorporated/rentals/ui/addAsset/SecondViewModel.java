package com.ateamincorporated.rentals.ui.addAsset;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SecondViewModel extends ViewModel {

    private final MutableLiveData<String> mStartDate;
    private final MutableLiveData<String> mEndDate;

    public SecondViewModel() {
        mStartDate = new MutableLiveData<>();
        mEndDate = new MutableLiveData<>();
    }

    public LiveData<String> getStartDate() {
        return mStartDate;
    }

    public void setStartDate(String startDate) {
        mStartDate.setValue(startDate);
    }

    public LiveData<String> getEndDate() {
        return mEndDate;
    }

    public void setEndDate(String endDate) {
        mEndDate.setValue(endDate);
    }
}
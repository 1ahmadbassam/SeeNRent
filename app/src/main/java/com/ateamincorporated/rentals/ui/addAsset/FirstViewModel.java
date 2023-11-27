package com.ateamincorporated.rentals.ui.addAsset;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FirstViewModel extends ViewModel {

    private final MutableLiveData<String> mNameErrorMessage;
    private final MutableLiveData<String> mDescriptionErrorMessage;


    public FirstViewModel() {
        mNameErrorMessage = new MutableLiveData<>();
        mDescriptionErrorMessage = new MutableLiveData<>();
    }

    public void updateNameErrorMessage(String errorMessage) {
        mNameErrorMessage.setValue(errorMessage);
    }

    public void updateDescErrorMessage(String errorMessage) {
        mDescriptionErrorMessage.setValue(errorMessage);
    }

    public LiveData<String> getNameErrorMessage() {
        return mNameErrorMessage;
    }

    public LiveData<String> getDescriptionErrorMessage() {
        return mDescriptionErrorMessage;
    }
}
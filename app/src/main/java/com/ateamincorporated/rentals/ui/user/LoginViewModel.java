package com.ateamincorporated.rentals.ui.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<String> mError;

    public LoginViewModel() {
        mError = new MutableLiveData<>();
    }

    public void setError (String error) {mError.setValue(error);}

    public LiveData<String> getError() {
        return mError;
    }
}
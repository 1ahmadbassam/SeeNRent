package com.ateamincorporated.rentals.ui.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserViewModel extends ViewModel {

    public static final int STATE_SIGN_IN = 0;
    public static final int STATE_SIGN_OUT = 1;

    private final MutableLiveData<String> mUsername;
    private final MutableLiveData<String> mEmailAddress;
    private final MutableLiveData<String> mSignInButtonState;

    public UserViewModel() {
        mUsername = new MutableLiveData<>();
        mEmailAddress = new MutableLiveData<>();
        mSignInButtonState = new MutableLiveData<>();
    }

    public void updateUI(String username, String emailAddress, int buttonState) {
        mUsername.setValue(username);
        mEmailAddress.setValue(emailAddress);
        if (buttonState == STATE_SIGN_IN) {
            mSignInButtonState.setValue("Sign in");
        } else if (buttonState == STATE_SIGN_OUT) {
            mSignInButtonState.setValue("Sign out");
        }
    }

    public LiveData<String> getUsername() {
        return mUsername;
    }

    public LiveData<String> getEmailAddress() {
        return mEmailAddress;
    }

    public LiveData<String> getSignInButtonState() {
        return mSignInButtonState;
    }

}
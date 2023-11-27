package com.ateamincorporated.rentals.ui.user;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RegisterViewModel extends ViewModel {

    private final MutableLiveData<String> mUsernameError;
    private final MutableLiveData<String> mEmailAddressError;
    private final MutableLiveData<String> mPasswordError;
    private final MutableLiveData<String> mPhoneNumberError;

    public RegisterViewModel() {
        mUsernameError = new MutableLiveData<>();
        mEmailAddressError = new MutableLiveData<>();
        mPasswordError = new MutableLiveData<>();
        mPhoneNumberError = new MutableLiveData<>();
    }

    public MutableLiveData<String> getUsernameError() {
        return mUsernameError;
    }

    public MutableLiveData<String> getEmailAddressError() {
        return mEmailAddressError;
    }

    public MutableLiveData<String> getPasswordError() {
        return mPasswordError;
    }

    public MutableLiveData<String> getPhoneNumberError() {
        return mPhoneNumberError;
    }

    public void setUsernameError (String error) {
        mUsernameError.setValue(error);
    }

    public void setEmailAddressError (String error) {
        mEmailAddressError.setValue(error);
    }

    public void setPasswordError (String error) {
        mPasswordError.setValue(error);
    }

    public void setPhoneNumberError (String error) {
        mPhoneNumberError.setValue(error);
    }

}
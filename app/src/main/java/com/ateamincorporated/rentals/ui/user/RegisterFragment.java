package com.ateamincorporated.rentals.ui.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.ateamincorporated.rentals.R;
import com.ateamincorporated.rentals.db.DatabaseContract;
import com.ateamincorporated.rentals.db.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterFragment extends Fragment {
    private final ArrayList<String> locations = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String mUid;
    private DatabaseReference mDatabase;
    private String location;

    private RegisterViewModel registerViewModel;

    private TextView usernameErrorView;
    private TextView emailAddressErrorView;
    private TextView passwordErrorView;
    private TextView phoneNumberErrorView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_register, container, false);
        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        SharedPreferences sharedpreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);

        final EditText usernameField = root.findViewById(R.id.username_field);
        final EditText emailAddressField = root.findViewById(R.id.email_address_field);
        final EditText passwordField = root.findViewById(R.id.password_field);
        final EditText phoneNumberField = root.findViewById(R.id.phone_number_field);

        usernameErrorView = root.findViewById(R.id.error_username);
        registerViewModel.getUsernameError().observe(this.getViewLifecycleOwner(), usernameErrorView::setText);
        usernameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideView(usernameErrorView);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        emailAddressErrorView = root.findViewById(R.id.error_email_address);
        registerViewModel.getEmailAddressError().observe(this.getViewLifecycleOwner(), emailAddressErrorView::setText);
        emailAddressField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideView(emailAddressErrorView);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        passwordErrorView = root.findViewById(R.id.error_password);
        registerViewModel.getPasswordError().observe(this.getViewLifecycleOwner(), passwordErrorView::setText);
        passwordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideView(passwordErrorView);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        phoneNumberErrorView = root.findViewById(R.id.error_phone_number);
        registerViewModel.getPhoneNumberError().observe(this.getViewLifecycleOwner(), phoneNumberErrorView::setText);
        phoneNumberField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideView(phoneNumberErrorView);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        mAuth = FirebaseAuth.getInstance();
        mUid = null;
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Find views in the root layout

        Button registerButton = root.findViewById(R.id.register_button);

        // Deal with location spinner
        getLocations(root);

        registerButton.setOnClickListener(v -> {
            String username = usernameField.getText().toString().trim();
            String email = emailAddressField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String phoneNumber = phoneNumberField.getText().toString().trim();

            if (checkInput(username, email, password, phoneNumber)) {
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        mUser = mAuth.getCurrentUser();
                        assert mUser != null;
                        mUid = mUser.getUid();

                        // update username
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                        mUser.updateProfile(profileUpdates);

                        // add location and phone number to database
                        User user = new User(phoneNumber, location);
                        mDatabase.child(DatabaseContract.GLOBAL_USER_PATH).child(mUid).setValue(user);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("location", location);
                        editor.apply();

                        Toast.makeText(getActivity(), "Registration successful!", Toast.LENGTH_SHORT).show();
                        editor.putString("lastRegistration", String.valueOf(Calendar.getInstance().getTimeInMillis()));
                        editor.apply();
                        Navigation.findNavController(v).navigate(R.id.return_to_user_after_register);
                    } else {
                        registerViewModel.setEmailAddressError(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                        unHideView(emailAddressErrorView);
                    }
                });
            }
        });

        return root;
    }

    private void getLocations(View root) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(DatabaseContract.GLOBAL_LOCATION_PATH);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot d : snapshot.getChildren()) {
                    locations.add(d.getKey());
                }
                ArrayAdapter<CharSequence> spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, locations.toArray(new String[]{}));
                spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                final Spinner locationSpinner = root.findViewById(R.id.location);
                locationSpinner.setAdapter(spinnerAdapter);
                locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        location = parent.getItemAtPosition(position).toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private static final String INVALID_CHARS = "^.*[@/\\\\].*$";

    private static final String USERNAME_EMPTY_ERROR = "Username can't be empty!";
    private static final String EMAIL_ADDRESS_EMPTY_ERROR = "Email address can't be empty!";
    private static final String PASSWORD_EMPTY_ERROR = "Password can't be empty!";
    private static final String PHONE_NUMBER_EMPTY_ERROR = "Phone number can't be empty!";
    private static final String USERNAME_INVALID = "Your username contains invalid characters!";
    private static final String EMAIL_ADDRESS_INVALID = "This does not look like an email address..";
    private static final String PASSWORD_INVALID = "Your password must be at least 6 characters long!";
    private static final String PHONE_NUMBER_INVALID = "The entered phone number is invalid.";

    private boolean checkInput(String username, String email, String password, String phoneNumber) {
        if (TextUtils.isEmpty(username)) {
            registerViewModel.setUsernameError(USERNAME_EMPTY_ERROR);
            unHideView(usernameErrorView);
            return false;
        } else if (TextUtils.isEmpty(email)) {
            registerViewModel.setEmailAddressError(EMAIL_ADDRESS_EMPTY_ERROR);
            unHideView(emailAddressErrorView);
            return false;
        } else if (TextUtils.isEmpty(password)) {
            registerViewModel.setPasswordError(PASSWORD_EMPTY_ERROR);
            unHideView(passwordErrorView);
            return false;
        } else if (TextUtils.isEmpty(phoneNumber)) {
            registerViewModel.setPhoneNumberError(PHONE_NUMBER_EMPTY_ERROR);
            unHideView(phoneNumberErrorView);
            return false;
        } else if (Pattern.compile(INVALID_CHARS).matcher(username).matches()) {
            registerViewModel.setUsernameError(USERNAME_INVALID);
            unHideView(usernameErrorView);
            return false;
        } else if (!isEmailValid(email)) {
            registerViewModel.setEmailAddressError(EMAIL_ADDRESS_INVALID);
            unHideView(emailAddressErrorView);
            return false;
        } else if (password.length() < 6) {
            registerViewModel.setPasswordError(PASSWORD_INVALID);
            unHideView(passwordErrorView);
            return false;
        } else if (phoneNumber.length() < 8 || !Patterns.PHONE.matcher(phoneNumber).matches()) {
            registerViewModel.setPhoneNumberError(PHONE_NUMBER_INVALID);
            unHideView(phoneNumberErrorView);
            return false;
        } else {
            return true;
        }
    }

    private static void unHideView(View v) {
        v.setVisibility(View.VISIBLE);
    }

    private static void hideView(View v) {
        v.setVisibility(View.INVISIBLE);
    }


    /**
     * method is used for checking valid email id format.
     *
     * @return boolean true for valid false for invalid
     */
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.ateamincorporated.rentals.R;
import com.ateamincorporated.rentals.db.DatabaseContract;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfileFragment extends Fragment {

    private final ArrayList<String> locations = new ArrayList<>();
    private String location;
    private String newLocation = "";
    private SharedPreferences sharedpreferences;

    private RegisterViewModel registerViewModel;

    private TextView usernameErrorView;
    private TextView emailAddressErrorView;
    private TextView passwordErrorView;
    private TextView phoneNumberErrorView;

    private FirebaseUser mUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_update_profile, container, false);
        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        sharedpreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null) {
            throw new IllegalStateException();
        }
        location = sharedpreferences.getString("location", "");

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


        Button updateButton = root.findViewById(R.id.update_button);
        updateButton.setOnClickListener(v-> {
            String username = usernameField.getText().toString().trim();
            String email = emailAddressField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String phoneNumber = phoneNumberField.getText().toString().trim();

            if(checkInput(username, email, password, phoneNumber)) {
                Bundle bundle = new Bundle();
                bundle.putString("username", username);
                bundle.putString("email", email);
                bundle.putString("password", password);
                bundle.putString("phoneNumber", phoneNumber);

                if (newLocation.isEmpty() || newLocation.equalsIgnoreCase(location)) {
                    bundle.putBoolean("newLocation", false);
                } else {
                    location = newLocation;
                    bundle.putBoolean("newLocation", true);
                }
                bundle.putString("location", location);
                Navigation.findNavController(usernameErrorView).navigate(R.id.updateProfile_to_reAuth, bundle);
            }
        });


        // Deal with location spinner
        getLocations(root);

        return root;
    }

    private static final String INVALID_CHARS = "^.*[@/\\\\].*$";

    private static final String USERNAME_INVALID = "Your username contains invalid characters!";
    private static final String EMAIL_ADDRESS_INVALID = "This does not look like an email address..";
    private static final String PASSWORD_INVALID = "Your password must be at least 6 characters long!";
    private static final String PHONE_NUMBER_INVALID = "The entered phone number is invalid.";

    private boolean checkInput(String username, String email, String password, String phoneNumber) {
        if (!TextUtils.isEmpty(username) && Pattern.compile(INVALID_CHARS).matcher(username).matches()) {
            registerViewModel.setUsernameError(USERNAME_INVALID);
            unHideView(usernameErrorView);
            return false;
        }
        if (!TextUtils.isEmpty(email) && !isEmailValid(email)) {
            registerViewModel.setEmailAddressError(EMAIL_ADDRESS_INVALID);
            unHideView(emailAddressErrorView);
            return false;
        }
        if (!TextUtils.isEmpty(password) && password.length() < 6) {
            registerViewModel.setPasswordError(PASSWORD_INVALID);
            unHideView(passwordErrorView);
            return false;
        }
        if (!TextUtils.isEmpty(phoneNumber) && (phoneNumber.length() < 8 || !Patterns.PHONE.matcher(phoneNumber).matches())) {
            registerViewModel.setPhoneNumberError(PHONE_NUMBER_INVALID);
            unHideView(phoneNumberErrorView);
            return false;
        }
        return true;
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
                if (TextUtils.isEmpty(location)) {
                    FirebaseDatabase.getInstance().getReference().child(DatabaseContract.GLOBAL_USER_PATH).child(mUser.getUid()).child("location").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            location = String.valueOf(snapshot.getValue());
                            locationSpinner.setSelection(locations.indexOf(location));
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("location", String.valueOf(snapshot.getValue()));
                            editor.apply();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                } else {
                    locationSpinner.setSelection(locations.indexOf(location));
                }
                locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        newLocation = parent.getItemAtPosition(position).toString();
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
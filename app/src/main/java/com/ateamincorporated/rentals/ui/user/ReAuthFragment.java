package com.ateamincorporated.rentals.ui.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.ateamincorporated.rentals.R;
import com.ateamincorporated.rentals.db.Asset;
import com.ateamincorporated.rentals.db.DatabaseContract;
import com.ateamincorporated.rentals.db.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReAuthFragment extends Fragment {

    private static final String EMPTY_CREDENTIALS = "Credentials can't be empty!";
    private static final String INVALID_EMAIL = "This does not look like an email address..";

    private EditText emailAddressField;
    private EditText passwordField;
    private FirebaseUser mUser;
    private String mUid;
    private LoginViewModel loginViewModel;
    private TextView errorView;
    private SharedPreferences sharedpreferences;
    private DatabaseReference mDatabase;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_re_auth, container, false);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        sharedpreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);

        errorView = root.findViewById(R.id.error);
        loginViewModel.getError().observe(getViewLifecycleOwner(), errorView::setText);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            throw new IllegalStateException();
        }
        mUid = mUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Find views in the root layout
        emailAddressField = root.findViewById(R.id.email_address_field);
        passwordField = root.findViewById(R.id.password_field);
        Button signInButton = root.findViewById(R.id.sign_in_button);

        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideView(errorView);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        emailAddressField.addTextChangedListener(textWatcher);
        passwordField.addTextChangedListener(textWatcher);

        signInButton.setOnClickListener(v -> {
            String email = emailAddressField.getText().toString();
            String password = passwordField.getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                loginViewModel.setError(EMPTY_CREDENTIALS);
                unHideView(errorView);
            } else if (!isEmailValid(email)) {
                loginViewModel.setError(INVALID_EMAIL);
                unHideView(errorView);
            } else {
                AuthCredential credential = EmailAuthProvider
                        .getCredential(email, password);
                // Prompt the user to re-provide their sign-in credentials
                mUser.reauthenticate(credential)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Bundle bundle = getArguments();
                                if (bundle != null && bundle.containsKey("newLocation")) {
                                    updateUserData(bundle);
                                } else {
                                    onDeleteUser();
                                }
                            } else {
                                loginViewModel.setError(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                                unHideView(errorView);
                            }
                        });
            }
        });


        return root;
    }

    private void updateUserData (Bundle bundle) {
        String username = bundle.getString("username");
        String email = bundle.getString("email");
        String password = bundle.getString("password");
        String phoneNumber = bundle.getString("phoneNumber");

        String location = bundle.getString("location");
        boolean newLocation = bundle.getBoolean("newLocation");

        if (!TextUtils.isEmpty(username)) {
            // update username
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
            mUser.updateProfile(profileUpdates);
        }

        if (!TextUtils.isEmpty(email)) {
            mUser.updateEmail(email)
                    .addOnCompleteListener(task -> {if (!task.isSuccessful()) { onNavigate(false); }});
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("lastProfileUpdate", String.valueOf(Calendar.getInstance().getTimeInMillis()));
            editor.apply();
        }

        if (!TextUtils.isEmpty(password)) {
            mUser.updatePassword(password)
                    .addOnCompleteListener(task -> {if (!task.isSuccessful()) { onNavigate(false); }});
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("lastProfileUpdate", String.valueOf(Calendar.getInstance().getTimeInMillis()));
            editor.apply();
        }

        if (!TextUtils.isEmpty(phoneNumber) && !newLocation) {
            mDatabase.child(DatabaseContract.GLOBAL_USER_PATH).child(mUid).child("phoneNumber").setValue(phoneNumber);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("lastProfileUpdate", String.valueOf(Calendar.getInstance().getTimeInMillis()));
            editor.apply();
        } else if (!TextUtils.isEmpty(phoneNumber)) {
            User user = new User(phoneNumber, location);
            mDatabase.child(DatabaseContract.GLOBAL_USER_PATH).child(mUid).setValue(user);
            updateLocation(location);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("location", location);
            editor.putString("lastProfileUpdate", String.valueOf(Calendar.getInstance().getTimeInMillis()));
            editor.apply();
        } else if (newLocation) {
            mDatabase.child(DatabaseContract.GLOBAL_USER_PATH).child(mUid).child("location").setValue(location);
            updateLocation(location);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("location", location);
            editor.putString("lastProfileUpdate", String.valueOf(Calendar.getInstance().getTimeInMillis()));
            editor.apply();
        }

        onNavigate(true);
    }

    private static final String ACTION_SUCCESS = "Action Successful";
    private static final String ACTION_FAIL = "Action Failed";

    private void onNavigate (boolean isSuccessful) {
        if (isSuccessful) {
            Toast.makeText(getActivity(), ACTION_SUCCESS, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), ACTION_FAIL, Toast.LENGTH_SHORT).show();
        }

        Navigation.findNavController(errorView).navigate(R.id.reAuth_to_user);
    }

    private void updateLocation(String location) {
        mDatabase.child(DatabaseContract.GLOBAL_USER_PATH).child(mUid).child(DatabaseContract.GLOBAL_ASSET_PATH).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<String> assetIds = new ArrayList<>();
                    for (DataSnapshot d : snapshot.getChildren()) {
                        assetIds.add(d.getKey());
                    }
                    for (String assetId : assetIds) {
                        mDatabase.child(DatabaseContract.GLOBAL_ASSET_PATH).child(assetId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Asset asset = snapshot.getValue(Asset.class);
                                if (asset != null) {
                                    asset.genericLocation = location;
                                    mDatabase.child(DatabaseContract.GLOBAL_ASSET_PATH).child(assetId).setValue(asset);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                                    onNavigate(false);

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onNavigate(false);
            }
        });
    }

    private void onDeleteUser () {
        if (mUser.getPhotoUrl() != null) {
            String photo = mUser.getPhotoUrl().toString();
            FirebaseStorage.getInstance().getReferenceFromUrl(photo).delete();
        }

        FirebaseDatabase.getInstance().getReference().child(DatabaseContract.GLOBAL_USER_PATH).child(mUid).removeValue();

        mUser.delete()
                .addOnCompleteListener(task -> onNavigate(task.isSuccessful()));
    }
}
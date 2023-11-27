package com.ateamincorporated.rentals.ui.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
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
import com.ateamincorporated.rentals.db.DatabaseContract;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginFragment extends Fragment {

    private static final String EMPTY_CREDENTIALS = "Credentials can't be empty!";
    private static final String INVALID_EMAIL = "This does not look like an email address..";
    private static final String MANY_PASSWORD_RESET_ATTEMPTS = "You need to wait at least an hour before requesting another password reset!";
    private static final String MANY_REGISTER_ATTEMPTS = "You need to wait at least 24 hours before creating a new user account";

    private EditText emailAddressField;
    private EditText passwordField;
    private FirebaseAuth mAuth;
    private LoginViewModel loginViewModel;
    private TextView errorView;

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
        View root = inflater.inflate(R.layout.fragment_login, container, false);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        SharedPreferences sharedpreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);

        errorView = root.findViewById(R.id.error);
        loginViewModel.getError().observe(getViewLifecycleOwner(), errorView::setText);

        mAuth = FirebaseAuth.getInstance();

        final TextView registerLink = root.findViewById(R.id.register_account_link);
        registerLink.setPaintFlags(registerLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        final TextView resetPasswordLink = root.findViewById(R.id.forgot_password_link);
        resetPasswordLink.setPaintFlags(resetPasswordLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        String lastRegistration = sharedpreferences.getString("lastRegistration", null);
        registerLink.setOnClickListener(v -> {
            if (lastRegistration != null && Calendar.getInstance().getTimeInMillis() < Long.parseLong(lastRegistration) + 86400000) {
                Toast.makeText(getActivity(), MANY_REGISTER_ATTEMPTS, Toast.LENGTH_SHORT).show();
            } else {
                Navigation.findNavController(v).navigate(R.id.register_from_login);
            }
        });

        String lastPasswordReset = sharedpreferences.getString("lastPasswordReset", null);
        resetPasswordLink.setOnClickListener(v -> {
            if (lastPasswordReset != null && Calendar.getInstance().getTimeInMillis() < Long.parseLong(lastPasswordReset) + 3600000) {
                Toast.makeText(getActivity(), MANY_PASSWORD_RESET_ATTEMPTS, Toast.LENGTH_SHORT).show();
            } else {
                Navigation.findNavController(v).navigate(R.id.login_to_reset_password);
            }
        });

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
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Login successful!", Toast.LENGTH_SHORT).show();
                        FirebaseDatabase.getInstance().getReference().child(DatabaseContract.GLOBAL_USER_PATH).child(mAuth.getCurrentUser().getUid()).child("location").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString("location", String.valueOf(snapshot.getValue()));
                                editor.apply();
                                Navigation.findNavController(v).navigate(R.id.return_to_user_after_sign_in);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    } else {
                        loginViewModel.setError(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                        unHideView(errorView);
                    }
                });
            }
        });

        return root;
    }
}
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

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.ateamincorporated.rentals.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResetPasswordFragment extends Fragment {


    private static final String EMPTY_EMAIL = "Email address can't be empty!";
    private static final String INVALID_EMAIL = "This does not look like an email address..";
    private static final String EMAIL_SENT = "Reset email link has been sent. Please check your inbox.";

    private LoginViewModel loginViewModel;

    private static void unHideView(View v) {
        v.setVisibility(View.VISIBLE);
    }

    private static void hideView(View v) {
        v.setVisibility(View.INVISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_reset_password, container, false);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        SharedPreferences sharedpreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);

        final TextView errorView = root.findViewById(R.id.error);
        loginViewModel.getError().observe(getViewLifecycleOwner(), errorView::setText);
        final EditText emailAddressField = root.findViewById(R.id.email_address_field);

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

        final Button resetPassword = root.findViewById(R.id.reset_password_button);
        resetPassword.setOnClickListener(v->  {
            String email = emailAddressField.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                unHideView(errorView);
                loginViewModel.setError(EMPTY_EMAIL);
            } else if (!isEmailValid(email)) {
                unHideView(errorView);
                loginViewModel.setError(INVALID_EMAIL);
            } else {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), EMAIL_SENT, Toast.LENGTH_SHORT).show();
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString("lastPasswordReset", String.valueOf(Calendar.getInstance().getTimeInMillis()));
                                editor.apply();
                                Navigation.findNavController(v).navigate(R.id.reset_password_to_login);
                            } else {
                                unHideView(errorView);
                                loginViewModel.setError(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                            }
                        });
            }
        });

        return root;
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
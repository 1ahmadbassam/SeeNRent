package com.ateamincorporated.rentals.ui.user;

import android.os.Bundle;
import android.text.Editable;
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
import com.ateamincorporated.rentals.db.DatabaseContract;
import com.ateamincorporated.rentals.db.Message;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SupportMessageFragment extends Fragment {
    private static final String INVALID_EMAIL = "This does not look like an email address..";
    private static final String MESSAGE_SENT = "Message sent successfully";
    private static final String MESSAGE_EMPTY = "The email or message is empty";

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
        View root = inflater.inflate(R.layout.fragment_support_message, container, false);
        LoginViewModel loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        final TextView errorView = root.findViewById(R.id.error);
        loginViewModel.getError().observe(getViewLifecycleOwner(), errorView::setText);

        // Find views in the root layout
        final EditText emailAddressField = root.findViewById(R.id.email_address_field);
        final EditText messageField = root.findViewById(R.id.message_field);

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


        final Button submit = root.findViewById(R.id.submit_button);
        submit.setOnClickListener(v -> {
            String email = emailAddressField.getText().toString().trim();
            String message = messageField.getText().toString().trim();

            if (email.isEmpty() || message.isEmpty()) {
                Toast.makeText(getActivity(), MESSAGE_EMPTY, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(v).navigate(R.id.support_to_user);
            } else if (!isEmailValid(email)) {
                loginViewModel.setError(INVALID_EMAIL);
                unHideView(errorView);
            } else {
                String key = FirebaseDatabase.getInstance().getReference().child(DatabaseContract.GLOBAL_MESSAGE_PATH).push().getKey();
                Message msg = new Message(email, message);
                Map<String, Object> msgValues = msg.toMap();

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/" + DatabaseContract.GLOBAL_MESSAGE_PATH + "/" + key, msgValues);
                FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates).addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), MESSAGE_SENT, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(v).navigate(R.id.support_to_user);
                });

            }
        });

        return root;
    }
}
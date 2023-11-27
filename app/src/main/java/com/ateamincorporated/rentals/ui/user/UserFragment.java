package com.ateamincorporated.rentals.ui.user;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.ateamincorporated.rentals.R;
import com.ateamincorporated.rentals.db.DatabaseContract;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class UserFragment extends Fragment {

    private static final String APP_TAG = "See n Rent";
    private static final String FILE_PROVIDER_AUTHORITY = "com.ateamincorporated.rentals.fileprovider";
    private static final int REQUEST_PHOTO = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String DIALOG_TITLE = "Pick Image";
    private static final String TAKE_IMAGE = "Take Image";
    private static final String CHOOSE_FROM_GALLERY = "Choose from Gallery";
    private static final String REMOVE_IMAGE = "Remove Image";
    private static final String CANCEL = "Cancel";
    private static final String PROGRESS_TITLE = "Uploading image..";
    private static final String PROGRESS_FAIL = "Failed with an error: %s";
    private static final CharSequence[] DIALOG = new CharSequence[]{TAKE_IMAGE, CHOOSE_FROM_GALLERY, REMOVE_IMAGE, CANCEL};
    private static final String MANY_PROFILE_UPDATE_ATTEMPTS = "You need to wait at least 24 hours before you can update your profile again!";
    private static final String ALERT_TITLE = "Delete Account";
    private static final String ALERT_TEXT = "Are you sure you want to delete your account? This action cannot be undone!";
    private String mUid;
    private String currentPhotoPath;
    private Uri filePath;
    private ImageView image;
    private FirebaseUser user;
    private SharedPreferences sharedpreferences;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        View root = inflater.inflate(R.layout.fragment_user, container, false);
        sharedpreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        image = root.findViewById(R.id.image_profile);

        // Handle button sign-in logic
        final Button signInButton = root.findViewById(R.id.sign_in_button);
        final TextView updateImageProfile = root.findViewById(R.id.update_image_profile);
        final TextView updateProfile = root.findViewById(R.id.update_profile);
        final TextView deleteUser = root.findViewById(R.id.delete_user);
        final TextView contactUs = root.findViewById(R.id.contact_us);
        contactUs.setPaintFlags(contactUs.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        contactUs.setOnClickListener(v-> Navigation.findNavController(v).navigate(R.id.user_to_support));
        if (user == null) {
            setSignedOutButtonState(userViewModel, signInButton);
            updateImageProfile.setVisibility(View.GONE);
            updateProfile.setVisibility(View.GONE);
            deleteUser.setVisibility(View.GONE);
        } else {
            userViewModel.updateUI(user.getDisplayName(), user.getEmail(), UserViewModel.STATE_SIGN_OUT);
            userViewModel.getSignInButtonState().observe(getViewLifecycleOwner(), signInButton::setText);
            updateImageProfile.setPaintFlags(updateImageProfile.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            updateImageProfile.setOnClickListener(v -> getImage());
            updateProfile.setPaintFlags(updateProfile.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            String lastProfileUpdate = sharedpreferences.getString("lastProfileUpdate", null);
            updateProfile.setOnClickListener(v -> {
                if (lastProfileUpdate != null && Calendar.getInstance().getTimeInMillis() < Long.parseLong(lastProfileUpdate) + 3600000) {
                    Toast.makeText(getActivity(), MANY_PROFILE_UPDATE_ATTEMPTS, Toast.LENGTH_SHORT).show();
                } else {
                    Navigation.findNavController(v).navigate(R.id.user_to_updateProfile);
                }
            });
            deleteUser.setPaintFlags(deleteUser.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            deleteUser.setOnClickListener(v -> {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle(ALERT_TITLE);
                alert.setMessage(ALERT_TEXT);
                alert.setPositiveButton(android.R.string.yes, (dialog, which) -> Navigation.findNavController(v).navigate(R.id.user_to_reAuth));
                alert.setNegativeButton(android.R.string.no, (dialog, which) -> dialog.cancel());
                alert.show();
            });


            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(Objects.requireNonNull(user.getPhotoUrl()).toString()).into(image);
            }
            mUid = Objects.requireNonNull(user.getUid());
            signInButton.setOnClickListener(v -> {
                auth.signOut();
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.remove("location");
                editor.apply();
                Toast.makeText(getActivity(), "Logged out!", Toast.LENGTH_SHORT).show();
                image.setImageResource(R.drawable.sample_avatar);
                updateImageProfile.setVisibility(View.GONE);
                updateProfile.setVisibility(View.GONE);
                deleteUser.setVisibility(View.GONE);
                setSignedOutButtonState(userViewModel, signInButton);
            });
        }

        final TextView username = root.findViewById(R.id.username);
        userViewModel.getUsername().observe(getViewLifecycleOwner(), username::setText);
        final TextView emailAddress = root.findViewById(R.id.email_address);
        userViewModel.getEmailAddress().observe(getViewLifecycleOwner(), emailAddress::setText);


        return root;
    }

    private void setSignedOutButtonState(UserViewModel userViewModel, Button signInButton) {
        userViewModel.updateUI("Anonymous", "", UserViewModel.STATE_SIGN_IN);
        userViewModel.getSignInButtonState().observe(getViewLifecycleOwner(), signInButton::setText);
        signInButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.sign_in_from_user_information));
    }

    // UploadImage method
    private void uploadImage() {
        if (filePath != null && mUid != null) {
            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(getActivity());
            progressDialog.setTitle(PROGRESS_TITLE);

            // Defining the child of storageReference
            StorageReference ref
                    = FirebaseStorage.getInstance().getReference()
                    .child(
                            DatabaseContract.GLOBAL_PROFILE_PICTURES_PATH + "/"
                                    + mUid + "/" + "myProfilePicture");

            // adding listeners on upload
            // or failure of image
            // Progress Listener for loading
            // percentage on the dialog box
            UploadTask uploadTask = ref.putFile(filePath);
            progressDialog.show();
            uploadTask.addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                int currentProgress = (int) progress;
                progressDialog.setProgress(currentProgress);
            }).addOnPausedListener(taskSnapshot -> {
            });

            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(task.getResult()).build();
                    user.updateProfile(profileUpdates);
                    Glide.with(this).load(Objects.requireNonNull(task.getResult()).toString()).into(image);
                } else {
                    // Error, Image not uploaded
                    progressDialog.dismiss();
                    Toast
                            .makeText(getActivity(),
                                    String.format(PROGRESS_FAIL, Objects.requireNonNull(task.getException()).getMessage()),
                                    Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }
    }


    public void getImage() {
        Activity activity = requireActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(DIALOG_TITLE);

        builder.setItems(DIALOG, (dialog, item) -> {
            Intent intent;
            if (DIALOG[item].equals(TAKE_IMAGE)) {
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's an activity to handle the intent
                if (intent.resolveActivity(activity.getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        ex.printStackTrace();
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(activity,
                                FILE_PROVIDER_AUTHORITY,
                                photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            } else if (DIALOG[item].equals(REMOVE_IMAGE)) {
                image.setImageResource(R.drawable.sample_avatar);
                if (user.getPhotoUrl() != null) {
                    String photo = user.getPhotoUrl().toString();
                    FirebaseStorage.getInstance().getReferenceFromUrl(photo).delete();
                }
                dialog.dismiss();
            } else if (DIALOG[item].equals(CHOOSE_FROM_GALLERY)) {
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_PHOTO);
            } else if (DIALOG[item].equals(CANCEL)) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private File createImageFile() throws IOException {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(APP_TAG, "failed to create directory");
        }

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = mUid + "_JPEG_" + timeStamp + "_";

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                mediaStorageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void activityResult(int requestCode,
                               int resultCode,
                               Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            filePath = Uri.fromFile(new File(currentPhotoPath));
            uploadImage();
        } else if (requestCode == REQUEST_PHOTO && resultCode == RESULT_OK) {
            Uri photoUri = data.getData();
            Bitmap bitmap = null;
            try {
                // check version of Android on device
                if (Build.VERSION.SDK_INT > 27) {
                    // on newer versions of Android, use the new decodeBitmap method
                    ImageDecoder.Source source = ImageDecoder.createSource(requireActivity().getContentResolver(), photoUri);
                    bitmap = ImageDecoder.decodeBitmap(source);
                } else {
                    // support older versions of Android by using getBitmap
                    bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), photoUri);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (bitmap != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    ex.printStackTrace();
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    try (FileOutputStream out = new FileOutputStream(photoFile)) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    filePath = Uri.fromFile(new File(currentPhotoPath));
                    uploadImage();
                }
            }
        }
    }

    // Override onActivityResult method
    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data) {

        super.onActivityResult(requestCode,
                resultCode,
                data);
        activityResult(requestCode, resultCode, data);
    }
}
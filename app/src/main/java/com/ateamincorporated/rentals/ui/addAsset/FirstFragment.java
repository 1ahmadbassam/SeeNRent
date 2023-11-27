package com.ateamincorporated.rentals.ui.addAsset;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.ateamincorporated.rentals.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;

public class FirstFragment extends Fragment {

    private static final String NAME_ERROR_BASE = "Name";
    private static final String DESCRIPTION_ERROR_BASE = "Description";
    private static final String NAME_EMPTY_ERROR = "You must type a " + NAME_ERROR_BASE.toLowerCase() + "!";
    private static final String NAME_SHORT_ERROR = NAME_ERROR_BASE + " is too short!";
    private static final String INVALID_CHARS_ERROR_BASE = " contains invalid characters!";

    private static final String INVALID_CHARS = "^.*[@/\\\\].*$";

    private static final String APP_TAG = "See n Rent";
    private static final String FILE_PROVIDER_AUTHORITY = "com.ateamincorporated.rentals.fileprovider";
    private static final int REQUEST_PHOTO = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String DIALOG_TITLE = "Pick Image";
    private static final String TAKE_IMAGE = "Take Image";
    private static final String CHOOSE_FROM_GALLERY = "Choose from Gallery";
    private static final String REMOVE_IMAGE = "Remove Image";
    private static final String CANCEL = "Cancel";
    private static final CharSequence[] DIALOG = new CharSequence[]{TAKE_IMAGE, CHOOSE_FROM_GALLERY, REMOVE_IMAGE, CANCEL};
    private String mUid;
    private String currentPhotoPath;
    private ImageView image;

    public static boolean checkInput(String name, String description, TextView nameErrorView, TextView descriptionErrorView, EditText nameField, EditText descriptionField, FirstViewModel viewModel, Fragment context) {
        final TextWatcher nameWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideView(nameErrorView);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        if (TextUtils.isEmpty(name)) {
            viewModel.updateNameErrorMessage(NAME_EMPTY_ERROR);
            viewModel.getNameErrorMessage().observe(context.getViewLifecycleOwner(), nameErrorView::setText);
            unHideView(nameErrorView);
            nameField.addTextChangedListener(nameWatcher);
        } else if (name.length() < 8) {
            viewModel.updateNameErrorMessage(NAME_SHORT_ERROR);
            viewModel.getNameErrorMessage().observe(context.getViewLifecycleOwner(), nameErrorView::setText);
            unHideView(nameErrorView);
            nameField.addTextChangedListener(nameWatcher);
        } else if (Pattern.compile(INVALID_CHARS).matcher(name).matches()) {
            viewModel.updateNameErrorMessage(NAME_ERROR_BASE + INVALID_CHARS_ERROR_BASE);
            viewModel.getNameErrorMessage().observe(context.getViewLifecycleOwner(), nameErrorView::setText);
            unHideView(nameErrorView);
            nameField.addTextChangedListener(nameWatcher);
        } else if (Pattern.compile(INVALID_CHARS).matcher(description).matches()) {
            viewModel.updateDescErrorMessage(DESCRIPTION_ERROR_BASE + INVALID_CHARS_ERROR_BASE);
            viewModel.getDescriptionErrorMessage().observe(context.getViewLifecycleOwner(), descriptionErrorView::setText);
            unHideView(descriptionErrorView);
            final TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    hideView(descriptionErrorView);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            };
            descriptionField.addTextChangedListener(watcher);
        } else {
            return true;
        }
        return false;
    }

    private static void unHideView(View v) {
        v.setVisibility(View.VISIBLE);
    }

    private static void hideView(View v) {
        v.setVisibility(View.INVISIBLE);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FirstViewModel firstViewModel = new ViewModelProvider(this).get(FirstViewModel.class);
        View root = inflater.inflate(R.layout.fragment_add_asset_first, container, false);

        image = root.findViewById(R.id.image);
        if (currentPhotoPath != null) {
            Glide.with(root).load(currentPhotoPath).into(image);
        }

        mUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        final Button next = root.findViewById(R.id.next);
        next.setOnClickListener(nextV -> {
            final EditText nameField = root.findViewById(R.id.name);
            final String name = nameField.getText().toString().trim();

            final EditText descriptionField = root.findViewById(R.id.description);
            final String description = descriptionField.getText().toString().trim();

            final TextView nameErrorView = root.findViewById(R.id.error_name);
            final TextView descriptionErrorView = root.findViewById(R.id.error_description);

            if (checkInput(name, description, nameErrorView, descriptionErrorView, nameField, descriptionField, firstViewModel, this)) {
                // Navigate
                Bundle bundle = new Bundle();
                bundle.putString("name", name);
                bundle.putString("description", description);
                if (currentPhotoPath != null && !TextUtils.isEmpty(currentPhotoPath)) {
                    bundle.putString("image", currentPhotoPath);
                } else {
                    bundle.putString("image", "placeholder");
                }
                Navigation.findNavController(nextV).navigate(R.id.first_to_second, bundle);
            }
        });
        final Button uploadImage = root.findViewById(R.id.upload_image);
        uploadImage.setOnClickListener(v -> getImage());
        return root;
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
                currentPhotoPath = null;
                image.setImageResource(R.drawable.placeholder);
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

    public void activityResult(int requestCode,
                               int resultCode,
                               Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Glide.with(this).load(currentPhotoPath).into(image);
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
                    Glide.with(this).load(currentPhotoPath).into(image);
                }
            }
        }
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
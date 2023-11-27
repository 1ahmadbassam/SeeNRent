package com.ateamincorporated.rentals.ui.addAsset;

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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.ateamincorporated.rentals.MainActivity;
import com.ateamincorporated.rentals.R;
import com.ateamincorporated.rentals.db.Asset;
import com.ateamincorporated.rentals.db.DatabaseContract;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class FourthFragment extends Fragment {

    private static final String CATEGORY_MESSAGE = "Your asset currently belongs to the \"%s\" category.";
    private static final String EDIT_MESSAGE = "Edit your Asset";
    private static final String EDIT_BUTTON = "Edit";

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
    private static final String PROGRESS_TITLE = "Uploading image..";
    private static final String PROGRESS_FAIL = "Failed with an error: %s";
    private static final String ALERT_TITLE = "Delete Asset";
    private static final String ALERT_TEXT = "Are you sure you want to delete your asset?";

    private static final int SOURCE_ADD_ASSET = 0;
    private static final int SOURCE_LIST_ASSET_USER = 1;
    private int mSource;
    private boolean invokeUpload = true;

    private String mUid;
    private String currentPhotoPath;
    private Uri filePath;
    private ImageView image;
    private DatabaseReference mDatabase;
    private Bundle bundle;

    private String location;
    private SharedPreferences sharedpreferences;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_add_asset_fourth, container, false);
        sharedpreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        location = sharedpreferences.getString("location", "");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            throw new IllegalStateException();
        }
        mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final Button publish = root.findViewById(R.id.publish);
        final Button delete = root.findViewById(R.id.delete);
        final TextView title = root.findViewById(R.id.title);

        bundle = getArguments();
        if (bundle == null) {
            throw new IllegalStateException();
        } else if (bundle.containsKey("myAdsEdit")) {
            mSource = SOURCE_LIST_ASSET_USER;
            publish.setText(EDIT_BUTTON);
            title.setText(EDIT_MESSAGE);
            delete.setVisibility(View.VISIBLE);
            delete.setOnClickListener(v -> onDelete());
        } else {
            mSource = SOURCE_ADD_ASSET;
        }

        String name = bundle.getString("name");
        final EditText nameField = root.findViewById(R.id.name_field);
        nameField.setText(name);
        String description = bundle.getString("description");
        final EditText descriptionField = root.findViewById(R.id.description_field);
        descriptionField.setText(description);

        final String category = bundle.getString("category");
        final TextView categoryView = root.findViewById(R.id.category);
        categoryView.setText(String.format(CATEGORY_MESSAGE, category));
        final TextView changeCategory = root.findViewById(R.id.change_category);
        changeCategory.setPaintFlags(changeCategory.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        changeCategory.setOnClickListener(v -> {
            if (prepareForNavigation(root, nameField, descriptionField)) {
                final String nameUpd = nameField.getText().toString().trim();
                final String descriptionUpd = descriptionField.getText().toString().trim();
                bundle.putString("name", nameUpd);
                bundle.putString("description", descriptionUpd);
                Navigation.findNavController(v).navigate(R.id.fourth_to_third, bundle);
            }
        });

        final Button checkDateTime = root.findViewById(R.id.check_date_time);
        checkDateTime.setOnClickListener(v -> {
            if (prepareForNavigation(root, nameField, descriptionField)) {
                final String nameUpd = nameField.getText().toString().trim();
                final String descriptionUpd = descriptionField.getText().toString().trim();
                bundle.putString("name", nameUpd);
                bundle.putString("description", descriptionUpd);
                Navigation.findNavController(v).navigate(R.id.fourth_to_second, bundle);
            }
        });

        image = root.findViewById(R.id.image);

        currentPhotoPath = bundle.getString("image");
        if (mSource == SOURCE_ADD_ASSET) {
            if (!TextUtils.isEmpty(currentPhotoPath) && !bundle.getString("image").equals("placeholder")) {
                filePath = Uri.fromFile(new File(currentPhotoPath));
            }
            Glide.with(this).load(currentPhotoPath).into(image);
        } else if (mSource == SOURCE_LIST_ASSET_USER) {
            invokeUpload = false;
        }
        if (bundle.getString("image").equals("placeholder")) {
            Glide.with(this).load(R.drawable.placeholder).into(image);
        } else {
            Glide.with(this).load(bundle.getString("image")).into(image);
        }

        final Button changeImage = root.findViewById(R.id.upload_image);
        changeImage.setOnClickListener(v -> getImage());

        publish.setOnClickListener(v -> {
            if (prepareForNavigation(root, nameField, descriptionField)) {
                final String nameUpd = nameField.getText().toString().trim();
                final String descriptionUpd = descriptionField.getText().toString().trim();
                bundle.putString("name", nameUpd);
                bundle.putString("description", descriptionUpd);
                uploadImage();
            }
        });

        return root;
    }

    private boolean prepareForNavigation(View root, EditText nameField, EditText descriptionField) {
        final String nameUpd = nameField.getText().toString().trim();

        final String descriptionUpd = descriptionField.getText().toString().trim();

        final TextView nameErrorView = root.findViewById(R.id.error_name);
        final TextView descriptionErrorView = root.findViewById(R.id.error_description);

        final FirstViewModel firstViewModel = new ViewModelProvider(this).get(FirstViewModel.class);
        return FirstFragment.checkInput(nameUpd, descriptionUpd, nameErrorView, descriptionErrorView, nameField, descriptionField, firstViewModel, this);
    }

    private void publishToDatabase(String image) {
        if (mUid == null) {
            return;
        }
        if (image == null) {
            image = "placeholder";
        }
        // Create new asset at /assets/$assetId and creates a key at the user uid /users/$uid/assets/$assetId as well as at /categories/$majorCategory/$category/$assetId
        String key;
        if (mSource == SOURCE_LIST_ASSET_USER) {
            key = bundle.getString("key");
        } else {
            key = mDatabase.child(DatabaseContract.GLOBAL_ASSET_PATH).push().getKey();
        }
        String finalImage = image;
        if (TextUtils.isEmpty(location)) {
            mDatabase.child(DatabaseContract.GLOBAL_USER_PATH).child(mUid).child("location").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String location = String.valueOf(snapshot.getValue());
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("location", String.valueOf(snapshot.getValue()));
                    editor.apply();
                    saveToDatabase(key, finalImage, location);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            saveToDatabase(key, finalImage, location);
        }
    }

    private void saveToDatabase(String key, String finalImage, String location) {
        Asset asset = new Asset(bundle.getString("name"), bundle.getString("description"), String.valueOf(System.currentTimeMillis()), location,
                bundle.getString("startDate"), bundle.getString("endDate"), bundle.getString("price"), bundle.getString("priceUnit"), bundle.getString("category"), finalImage);
        Map<String, Object> assetValues = asset.toMap();
        Map<String, Object> userID = new HashMap<>();
        userID.put(mUid, true);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + DatabaseContract.GLOBAL_ASSET_PATH + "/" + key, assetValues);
        childUpdates.put("/" + DatabaseContract.GLOBAL_USER_PATH + "/" + mUid + "/" + DatabaseContract.GLOBAL_ASSET_PATH + "/" + key, userID);

        mDatabase.updateChildren(childUpdates);
        if (mSource == SOURCE_LIST_ASSET_USER) {
            bundle.clear();
            Navigation.findNavController(image).navigate(R.id.edit_to_myAds, bundle);
        } else {
            startActivity(new Intent(getActivity(), MainActivity.class));
        }
    }

    private void onDeleteImage() {
        if (!bundle.getString("image").equals("placeholder")) {
            FirebaseStorage.getInstance().getReferenceFromUrl(bundle.getString("image")).delete();
        }
    }

    // UploadImage method
    private void uploadImage() {
        if (filePath != null && mUid != null && invokeUpload) {
            if (mSource != SOURCE_ADD_ASSET) {
                onDeleteImage();
            }
            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(getActivity());
            progressDialog.setTitle(PROGRESS_TITLE);

            // Defining the child of storageReference
            StorageReference ref
                    = FirebaseStorage.getInstance().getReference()
                    .child(
                            DatabaseContract.GLOBAL_IMAGES_PATH + "/"
                                    + UUID.randomUUID().toString());

            // adding listeners on upload
            // or failure of image
            // Progress Listener for loading
            // percentage on the dialog box
            UploadTask uploadTask = ref.putFile(filePath);
//                    .addOnProgressListener(
//                    taskSnapshot -> {
//                        double progress
//                                = (100.0
//                                * taskSnapshot.getBytesTransferred()
//                                / taskSnapshot.getTotalByteCount());
//                        progressDialog.setMessage(
//                                String.format(PROGRESS_PERCENT, (int) progress));
//                        if (progress == 100.0) {
//                            progressDialog.dismiss();
//                        }
//                    });
//            progressDialog.show();
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
                    publishToDatabase(Objects.requireNonNull(task.getResult()).toString());
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
        } else if (!invokeUpload) {
            publishToDatabase(currentPhotoPath);
        } else {
            publishToDatabase(null);
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
                        invokeUpload = true;
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            } else if (DIALOG[item].equals(CHOOSE_FROM_GALLERY)) {
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                invokeUpload = true;
                startActivityForResult(intent, REQUEST_PHOTO);
            } else if (DIALOG[item].equals(REMOVE_IMAGE)) {
                currentPhotoPath = null;
                filePath = null;
                image.setImageResource(R.drawable.placeholder);
                dialog.dismiss();
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
            filePath = Uri.fromFile(new File(currentPhotoPath));
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
                    filePath = Uri.fromFile(new File(currentPhotoPath));
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

    private void onDelete() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(ALERT_TITLE);
        alert.setMessage(ALERT_TEXT);
        alert.setPositiveButton(android.R.string.yes, (dialog, which) -> {
            // continue with delete
            String image = bundle.getString("image");
            if (!image.equals("placeholder")) {
                StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(image);
                photoRef.delete().addOnSuccessListener(aVoid -> {
                    // File deleted successfully
                    deleteAsset();
                });
            } else {
                deleteAsset();
            }
        });
        alert.setNegativeButton(android.R.string.no, (dialog, which) -> dialog.cancel());
        alert.show();
    }

    private void deleteAsset() {
        String key = bundle.getString("key");
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + DatabaseContract.GLOBAL_ASSET_PATH + "/" + key, null);
        childUpdates.put("/" + DatabaseContract.GLOBAL_USER_PATH + "/" + mUid + "/" + DatabaseContract.GLOBAL_ASSET_PATH + "/" + key, null);

        mDatabase.updateChildren(childUpdates).addOnCompleteListener(task -> {
            bundle.clear();
            Navigation.findNavController(image).navigate(R.id.edit_to_myAds, bundle);
        });
    }
}
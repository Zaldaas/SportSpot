package com.example.sportspot;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName;
    private EditText FullName;
    private EditText CountryName; // final removed to simplify
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;

    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;

    private String currentUserID;

    // 1) Create an ActivityResultLauncher for picking images from the gallery
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    // Once we pick an image, launch the crop flow
                    launchCrop(uri);
                }
            });

    // 2) Create an ActivityResultLauncher for cropping images
    private final ActivityResultLauncher<CropImageContractOptions> cropImageLauncher =
            registerForActivityResult(new CropImageContract(), result -> {
                if (result.isSuccessful()) {
                    // The cropped image URI
                    Uri resultUri = result.getUriContent();
                    if (resultUri != null) {
                        uploadCroppedImage(resultUri);
                    } else {
                        Toast.makeText(SetupActivity.this,
                                "Error: Null cropped URI",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Crop failed or user canceled
                    Exception e = result.getError();
                    String msg = (e != null) ? e.getMessage() : "Crop canceled or failed";
                    Toast.makeText(SetupActivity.this,
                            "Error Occurred: " + msg,
                            Toast.LENGTH_SHORT).show();
                }
            });

    public SetupActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        UserName = findViewById(R.id.usernameEditText);
        FullName = findViewById(R.id.fnameEditText);
        CountryName = findViewById(R.id.lnameEditText);
        ProfileImage = findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);

        Button saveInformationButton = findViewById(R.id.signUpButton);
        saveInformationButton.setOnClickListener(view -> SaveAccountSetupInformation());

        // When user taps on the profile image, launch the gallery
        ProfileImage.setOnClickListener(view -> {
            // Launch "pick an image" flow
            pickImageLauncher.launch("image/*");
        });

        // Optionally load existing profile image
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("profileimage")) {
                        String image = String.valueOf(dataSnapshot.child("profileimage").getValue());
                        Picasso.get().load(image)
                                .placeholder(R.drawable.profile_image)
                                .into(ProfileImage);
                    } else {
                        Toast.makeText(SetupActivity.this,
                                "Please select a profile image first.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void launchCrop(Uri sourceUri) {
        CropImageOptions options = new CropImageOptions();
        options.guidelines = CropImageView.Guidelines.ON;

        options.aspectRatioX = 1;
        options.aspectRatioY = 1;
        options.fixAspectRatio = true;  // must be true to enforce aspectRatioX/Y

        // Launch the crop
        cropImageLauncher.launch(
                new CropImageContractOptions(sourceUri, options)
        );
    }


    private void uploadCroppedImage(Uri resultUri) {
        loadingBar.setTitle("Profile Image");
        loadingBar.setMessage("Please wait, while we update your profile image...");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);

        // Upload cropped image
        StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");
        filePath.putFile(resultUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(SetupActivity.this,
                        "Profile Image stored successfully to Firebase storage...",
                        Toast.LENGTH_SHORT).show();

                // Now get the download URL from the same reference
                filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                    final String downloadUrl = uri.toString();

                    // Store that URL in Realtime Database
                    UsersRef.child("profileimage").setValue(downloadUrl)
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                    startActivity(selfIntent);

                                    Toast.makeText(SetupActivity.this,
                                            "Profile Image stored to Firebase Database Successfully...",
                                            Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                } else {
                                    Exception e = task1.getException();
                                    String message = (e != null) ? e.getMessage() : "Unknown error";
                                    Toast.makeText(SetupActivity.this,
                                            "Error Occurred: " + message,
                                            Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            });
                });
            } else {
                Toast.makeText(SetupActivity.this,
                        "Image upload failed. Please try again.",
                        Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        });
    }

    private void SaveAccountSetupInformation() {
        String username = UserName.getText().toString().trim();
        String fullname = FullName.getText().toString().trim();
        String country = CountryName.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please write your username...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(this, "Please write your full name...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(country)) {
            Toast.makeText(this, "Please write your country...", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingBar.setTitle("Saving Information");
        loadingBar.setMessage("Please wait, while we are creating your new Account...");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);

        // Use a parameterized map to avoid raw type warnings
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("fullname", fullname);
        userMap.put("country", country);
        userMap.put("status", "Using SportSpot");
        userMap.put("gender", "none");
        userMap.put("dob", "none");

        UsersRef.updateChildren(userMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                SendUserToMainActivity();
                Toast.makeText(SetupActivity.this,
                        "Your Account is created Successfully.",
                        Toast.LENGTH_LONG).show();
            } else {
                Exception e = task.getException();
                String message = (e != null) ? e.getMessage() : "Unknown error";
                Toast.makeText(SetupActivity.this,
                        "Error Occurred: " + message,
                        Toast.LENGTH_SHORT).show();
            }
            loadingBar.dismiss();
        });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}

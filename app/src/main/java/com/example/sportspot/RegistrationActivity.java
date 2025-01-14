package com.example.sportspot;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegistrationActivity extends AppCompatActivity {

    // Declare Firebase Auth and UI elements
    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText usernameEditText;
    private EditText fnameEditText;
    private EditText lnameEditText;
    private CircleImageView profileImageView;

    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Reference UI elements
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        fnameEditText = findViewById(R.id.fnameEditText);
        lnameEditText = findViewById(R.id.lnameEditText);
        Button signUpButton = findViewById(R.id.signUpButton);
        profileImageView = findViewById(R.id.setup_profile_image);
        Button backButton = findViewById(R.id.BackButtonRegistration);
        // Set OnClickListener for the Sign Up button
        signUpButton.setOnClickListener(v -> registerUser());

        backButton.setOnClickListener(v -> backtowelcome());

        // Set OnClickListener for the profile image view
        profileImageView.setOnClickListener(view -> {
            // Open gallery to select an image
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, Gallery_Pick);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            // Set the selected image to the profile image view
            profileImageView.setImageURI(imageUri);
        }
    }

    private void backtowelcome() {
        Intent returntowelcome = new Intent(RegistrationActivity.this, WelcomePageActivity.class);
        returntowelcome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(returntowelcome);
        finish();
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String fname = fnameEditText.getText().toString().trim();
        String lname = lnameEditText.getText().toString().trim();

        // Basic validation
        if (email.isEmpty() || password.isEmpty() || fname.isEmpty() || lname.isEmpty() || username.isEmpty() || profileImageView.getDrawable() == null) {
            Toast.makeText(RegistrationActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        // Firebase registration
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegistrationActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();

                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            // Save user information to Realtime Database
                            saveUserToDatabase(currentUser.getUid(), fname, lname, username, email);

                            // Save profile image to Firebase Storage
                            saveProfileImageToStorage(currentUser.getUid());

                            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // Handle the case where currentUser is null
                            Toast.makeText(RegistrationActivity.this, "User information is missing", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        System.out.println(task.getException());
                    }
                });
        }

    private void saveUserToDatabase(String uid, String fname, String lname, String username, String email) {
        Users user = new Users(uid, fname, lname, username, email);
        mDatabase.child(uid).setValue(user);
    }

    private void saveProfileImageToStorage(String uid) {
        // Get the profile image drawable
        BitmapDrawable drawable = (BitmapDrawable) profileImageView.getDrawable();
        if (drawable != null) {
            // Convert drawable to Bitmap
            Bitmap bitmap = drawable.getBitmap();
            // Convert Bitmap to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();

            // Save the byte array to Firebase Storage
            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("ProfileImages").child(uid + ".jpg");
            filePath.putBytes(imageData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Image uploaded successfully, get the download URL
                            filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                                // Save the download URL to the Realtime Database
                                saveImageUrlToDatabase(uid, uri.toString());

                                // After saving the image and URL, you can proceed to other activities or show a success message
                                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            // Handle image upload failure
                            Toast.makeText(RegistrationActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        }

    private void saveImageUrlToDatabase(String uid, String imageUrl) {
        mDatabase.child(uid).child("profileImage").setValue(imageUrl);
    }
}

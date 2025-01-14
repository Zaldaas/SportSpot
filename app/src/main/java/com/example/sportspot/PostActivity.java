package com.example.sportspot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {

    private ProgressDialog loadingBar;
    private ImageButton SelectPostImage;
    private EditText PostDescription;
    private EditText PostSport;
    private EditText PostDateRange;
    private static final int Gallery_Pick = 1;
    private Uri ImageUri;
    private String Description;
    private String Sport;
    private String DateRange;

    private StorageReference PostsImagesReference;
    private DatabaseReference UsersRef, PostsRef;

    private String saveCurrentDate;
    private String saveCurrentTime;
    private String current_user_id;
    private String downloadUrl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        current_user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        PostsImagesReference = FirebaseStorage.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        SelectPostImage = findViewById(R.id.select_post_image);
        Button updatePostButton = findViewById(R.id.update_post_button);
        PostDescription = findViewById(R.id.post_description);
        PostSport = findViewById(R.id.post_sport);
        PostDateRange = findViewById(R.id.post_date_range);
        loadingBar = new ProgressDialog(this);

        Toolbar mToolbar = findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        SelectPostImage.setOnClickListener(v -> OpenGallery());

        updatePostButton.setOnClickListener(v -> ValidatePostInfo());
    }

    private void ValidatePostInfo() {
        Description = PostDescription.getText().toString();
        Sport = PostSport.getText().toString();
        DateRange = PostDateRange.getText().toString();

        if(ImageUri == null)
        {
            Toast.makeText(this, "Please select post image...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Description))
        {
            Toast.makeText(this, "Please say something about your image...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Sport))
        {
            Toast.makeText(this, "Please say something about the sport", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(DateRange))
        {
            Toast.makeText(this, "Please say something about the time", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please wait, while we are updating your new post...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            StoringImageToFirebaseStorage();
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void StoringImageToFirebaseStorage() {
        Calendar calFordDate = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calFordTime.getTime());

        String postRandomName = saveCurrentDate + saveCurrentTime;

        StorageReference filePath = PostsImagesReference.child("Post Images")
                .child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");

        filePath.putFile(ImageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                    downloadUrl = uri.toString();
                    Toast.makeText(PostActivity.this, "Image uploaded successfully to Storage...", Toast.LENGTH_SHORT).show();
                    SavingPostInformationToDatabase();
                }).addOnFailureListener(e -> {
                    // Handle failure to get download URL
                    Toast.makeText(PostActivity.this, "Error occurred while getting download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } else {
                String message = Objects.requireNonNull(task.getException()).getMessage();
                Toast.makeText(PostActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void SavingPostInformationToDatabase() {
        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                if(datasnapshot.exists()) {
                    String userName = Objects.requireNonNull(datasnapshot.child("userName").getValue()).toString();
                    String userProfileImage = Objects.requireNonNull(datasnapshot.child("profileImage").getValue()).toString();

                    //saving hashmap information to firebase
                    HashMap<String, Object> postsMap = new HashMap<>();
                    postsMap.put("uid", current_user_id);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("description", Description);
                    postsMap.put("sport", Sport);
                    postsMap.put("daterange", DateRange);
                    postsMap.put("postimage", downloadUrl);
                    postsMap.put("profileimage", userProfileImage);
                    postsMap.put("username", userName);

                    PostsRef.push().updateChildren(postsMap).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            SendUserToMainActivity();
                            Toast.makeText(PostActivity.this, "New Post has been updated successfully", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        } else {
                            Toast.makeText(PostActivity.this, "Error Occurred while updating your post.", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    });
                } else {
                    // Handle the case where the user data doesn't exist
                    Toast.makeText(PostActivity.this, "User data does not exist.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                Toast.makeText(PostActivity.this, "Error occurred: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        });
    }


    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        //connect to the Regestration Activity
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            SendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}
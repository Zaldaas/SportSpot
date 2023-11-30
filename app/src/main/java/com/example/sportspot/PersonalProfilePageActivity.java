package com.example.sportspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

public class PersonalProfilePageActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private FirebaseDatabase database;
    private ImageView personalProfileImage;
    private TextView username;
    private Button editButton;
    private String profileImageLocation = "";
    private FirebaseStorage firebaseStorage;
    private StorageReference storageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_profile_page);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        firebaseStorage = FirebaseStorage.getInstance();
        storageRef = firebaseStorage.getReference();


        personalProfileImage = findViewById(R.id.personalProfileImage);
        username = findViewById(R.id.profileUsername);
        editButton = findViewById(R.id.profileEditButton);

        FirebaseUser currentUser = mAuth.getCurrentUser();

        profileImageLocation = currentUser.getUid() + ".jpg";

        StorageReference pathReference = storageRef.child("ProfileImages/" + profileImageLocation);

        final long ONE_MEGABYTE = 1024 * 1024;
        pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                personalProfileImage.setImageBitmap(bmp);
                Toast.makeText(getApplicationContext(), "Image Found!", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("location", profileImageLocation);
                Toast.makeText(getApplicationContext(), "No path found", Toast.LENGTH_LONG).show();
            }
        });

        databaseReference.child("Users/" + currentUser.getUid() + "/userName").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    Log.i("value", String.valueOf(task.getResult().getValue()));
                    username.setText(String.valueOf(task.getResult().getValue()));
                }
                else{
                    Log.i("Error","error retrieving username", task.getException());
                }
            }
        });










    }

    private void setProfileImage(String location){

    }
}
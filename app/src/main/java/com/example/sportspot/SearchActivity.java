package com.example.sportspot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SearchActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseDatabase mData;
    private EditText searchEditText;
    private Button searchButton;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        searchEditText = findViewById(R.id.usersearch);
        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve the username entered by the user
                String usernameToSearch = searchEditText.getText().toString();

                // Query the database to find the user by their username
                Query query = mDatabase.child("Users").orderByChild("userName").equalTo(usernameToSearch);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // This method will still be called, but we handle results in onComplete
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle any database error here
                        Log.e("SearchActivity", "Database Error: " + databaseError.getMessage());
                        Toast.makeText(getApplicationContext(), "No path found", Toast.LENGTH_LONG).show();
                    }
                });

                query.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            DataSnapshot dataSnapshot = task.getResult();
                            if (dataSnapshot.exists()) {
                                // User found, retrieve their UID
                                String userUid = dataSnapshot.getChildren().iterator().next().getKey();
                                Log.d("SearchActivity", "Searching for username: " + usernameToSearch);

                                // Navigate to the user's profile with their UID using PublicProfilePageActivity
                                Intent intent = new Intent(SearchActivity.this, PublicProfilePageActivity.class);
                                intent.putExtra("userUid", userUid); // Pass the userUid
                                startActivity(intent);
                            } else {
                                // User not found
                                // Handle the case when the user is not found
                                Log.d("SearchActivity", "No User Found");
                                Toast.makeText(getApplicationContext(), "No User Found", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            // Handle the task failure
                            Log.e("SearchActivity", "Task failed: " + task.getException());
                            Toast.makeText(getApplicationContext(), "Task Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        // ... Other code for displaying user's own profile
    }
}
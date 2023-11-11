package com.example.sportspot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {

    // Declare Firebase Auth and UI elements
    private FirebaseAuth mAuth;

    private FirebaseDatabase mData;

    private DatabaseReference mDatabase;

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText usernameEditText;
    private EditText fnameEditText;
    private EditText lnameEditText;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Reference UI elements

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        fnameEditText = findViewById(R.id.fnameEditText);
        lnameEditText = findViewById(R.id.lnameEditText);
        signUpButton = findViewById(R.id.signUpButton);

        // Set OnClickListener for the Sign Up button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String fname = fnameEditText.getText().toString().trim();
        String lname = lnameEditText.getText().toString().trim();

        mDatabase = FirebaseDatabase.getInstance().getReference("Users");
        //reference = mData.getReference();

        // Basic validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(RegistrationActivity.this, "Email and Password required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase registration
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegistrationActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();

                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        Users user = new Users(currentUser.getUid(), fname, lname, username, email);
                        mDatabase.child(currentUser.getUid()).setValue(user);

                        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        System.out.println(task.getException());
                    }
                });
    }
}

package com.example.sportspot;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailEditText;
    private EditText passwordEditText;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Reference UI elements
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button signInButton = findViewById(R.id.signInButton);
        Button backButton = findViewById(R.id.BackButton);

        // Initialize loadingBar
        loadingBar = new ProgressDialog(this);

        // Set OnClickListener for the Sign Up button
        signInButton.setOnClickListener(v -> signInUser());

        // Set OnClickListener for the Back Button (using lambda to shorten code)
        backButton.setOnClickListener(v -> backtowelcome());
    }

    private void backtowelcome() {
        Intent returntowelcome = new Intent(LoginActivity.this, WelcomePageActivity.class);
        returntowelcome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(returntowelcome);
        finish();
    }
    private void signInUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Basic validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Email and Password required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize loadingBar
        loadingBar.setTitle("Signing In");
        loadingBar.setMessage("Please wait, while we are checking your credentials.");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        // Firebase registration
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loadingBar.dismiss(); // Dismiss the loadingBar

                    if(task.isSuccessful())
                    {
                        SendUserToMainActivity();
                        Toast.makeText(LoginActivity.this, "you are Logged In successfully.", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        String message = Objects.requireNonNull(task.getException()).getMessage();
                        Toast.makeText(LoginActivity.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}

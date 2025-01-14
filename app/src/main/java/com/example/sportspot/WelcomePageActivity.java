package com.example.sportspot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);
        //slowly swipe from the left to bring up navigation menu
        Button navigateToRegisterButton = findViewById(R.id.navigateToRegisterButton);
        Button navigateToLoginButton = findViewById(R.id.navigateToLoginButton);

        navigateToRegisterButton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomePageActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        navigateToLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomePageActivity.this, LoginActivity.class);
            startActivity(intent);
        });


    }
}
package com.example.mysteryartist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class WinnerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winner);

        // Get winner name from the Intent
        String winnerName = getIntent().getStringExtra("winnerName");

        // Set winner text
        TextView winnerTextView = findViewById(R.id.winnerTextView);
        winnerTextView.setText(" " + winnerName);

        // Set up the Home Button
        Button homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            // Go to HomeActivity or MainActivity
            Intent homeIntent = new Intent(WinnerActivity.this, HomeActivity.class); // Change to your home screen activity
            startActivity(homeIntent);
            finish();
        });
    }
}

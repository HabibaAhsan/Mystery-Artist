package com.example.mysteryartist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button createGameBtn = findViewById(R.id.createGameBtn);
        Button joinGameBtn = findViewById(R.id.joinGameBtn);

        createGameBtn.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, CreateGameActivity.class)));
        joinGameBtn.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, JoinGameActivity.class)));
    }
}

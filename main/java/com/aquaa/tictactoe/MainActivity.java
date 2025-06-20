package com.aquaa.tictactoe; // Updated package name

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button playAiButton = findViewById(R.id.play_ai_button);
        Button settingsButton = findViewById(R.id.settings_button);

        // Set up listener for the "Play AI Game" button
        playAiButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            // You can pass extra data if needed, e.g., to indicate AI mode
            intent.putExtra("game_mode", "AI_MODE");
            startActivity(intent);
        });

        // Set up listener for the "Settings" button
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}

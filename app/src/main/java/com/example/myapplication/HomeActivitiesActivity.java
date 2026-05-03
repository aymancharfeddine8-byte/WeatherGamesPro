package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HomeActivitiesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_activities);

        CardView cardMaze = findViewById(R.id.cardMathGame);
        CardView cardDrawing = findViewById(R.id.cardDrawing);
        CardView cardShooting = findViewById(R.id.cardShooting);

        cardMaze.setOnClickListener(v -> startActivity(new Intent(this, MazeGameActivity.class)));
        cardDrawing.setOnClickListener(v -> startActivity(new Intent(this, DrawingActivity.class)));
        cardShooting.setOnClickListener(v -> startActivity(new Intent(this, ShootingGameActivity.class)));
    }
}

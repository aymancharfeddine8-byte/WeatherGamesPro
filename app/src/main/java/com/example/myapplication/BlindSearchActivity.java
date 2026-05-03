package com.example.myapplication;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class BlindSearchActivity extends AppCompatActivity implements BlindSearchView.OnPathListener {

    private TextView tvTarget, tvCurrent, tvScore, tvBest;
    private ProgressBar pbTimer;
    private BlindSearchView blindSearchView;
    
    private int score = 0;
    private int highScore = 0;
    private int level = 1;
    private CountDownTimer countDownTimer;
    private long maxTime = 15000;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blind_search);
        setTitle("Blind Search");

        prefs = getSharedPreferences("BlindSearchPrefs", MODE_PRIVATE);
        highScore = prefs.getInt("highScore", 0);

        tvTarget = findViewById(R.id.tvTarget);
        tvCurrent = findViewById(R.id.tvCurrent);
        tvScore = findViewById(R.id.tvScore);
        tvBest = findViewById(R.id.tvBest);
        pbTimer = findViewById(R.id.pbTimer);
        blindSearchView = findViewById(R.id.blindSearchView);

        blindSearchView.setOnPathListener(this);
        tvBest.setText("BEST: " + highScore);

        startNewLevel();
    }

    private void startNewLevel() {
        tvTarget.setText(String.valueOf(level));
        tvCurrent.setText("Search the area to find the hidden target!");
        tvCurrent.setTextColor(Color.WHITE);
        
        blindSearchView.generateGrid();
        
        // Timer gets shorter as level increases
        maxTime = Math.max(5000, 15000 - (level * 500));
        startTimer();
    }

    @Override
    public void onPathChanged(int signal) {
        // Required by OnPathListener but not currently used.
    }

    @Override
    public void onPathComplete(int signal) {
        if (signal == 1) { // Target found
            if (countDownTimer != null) countDownTimer.cancel();
            
            score += 50 * level;
            level++;
            tvScore.setText("SCORE: " + score);
            
            if (score > highScore) {
                highScore = score;
                tvBest.setText("BEST: " + highScore);
                prefs.edit().putInt("highScore", highScore).apply();
            }

            Toast.makeText(this, "TARGET FOUND! 🎯", Toast.LENGTH_SHORT).show();
            
            // Short delay before next level
            tvStatusDelayed();
        }
    }

    private void tvStatusDelayed() {
        tvCurrent.postDelayed(this::startNewLevel, 1000);
    }

    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(maxTime, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                pbTimer.setProgress((int) (millisUntilFinished * 100 / maxTime));
            }

            @Override
            public void onFinish() {
                gameOver();
            }
        }.start();
    }

    private void gameOver() {
        Toast.makeText(this, "TIME'S UP! The target vanished.", Toast.LENGTH_LONG).show();
        score = 0;
        level = 1;
        tvScore.setText("SCORE: 0");
        startNewLevel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}

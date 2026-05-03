package com.example.myapplication;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MazeGameActivity extends AppCompatActivity implements MazeView.OnMazeListener {

    private TextView tvTarget, tvCurrent, tvScore, tvBest;
    private ProgressBar pbTimer;
    private MazeView mazeView;
    
    private int score = 0;
    private int highScore = 0;
    private int level = 1;
    private CountDownTimer countDownTimer;
    private long maxTime = 45000; // Complex mazes need more time
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maze_game);
        setTitle("Haunted Dark Maze");

        prefs = getSharedPreferences("MazeGamePrefs", MODE_PRIVATE);
        highScore = prefs.getInt("highScore", 0);

        tvTarget = findViewById(R.id.tvTarget);
        tvCurrent = findViewById(R.id.tvCurrent);
        tvScore = findViewById(R.id.tvScore);
        tvBest = findViewById(R.id.tvBest);
        pbTimer = findViewById(R.id.pbTimer);
        mazeView = findViewById(R.id.mazeView);

        mazeView.setOnMazeListener(this);
        tvBest.setText("BEST: " + highScore);

        startNewLevel();
    }

    private void startNewLevel() {
        tvTarget.setText(String.valueOf(level));
        tvCurrent.setText("Avoid the ghosts and find the star!");
        tvCurrent.setTextColor(Color.WHITE);
        
        mazeView.generateMaze(level);
        
        // Timer adjusts with level complexity
        maxTime = Math.max(15000, 45000 - (level * 1000));
        startTimer();
    }

    @Override
    public void onMazeComplete() {
        if (countDownTimer != null) countDownTimer.cancel();
        
        score += 150 * level;
        level++;
        tvScore.setText("SCORE: " + score);
        
        if (score > highScore) {
            highScore = score;
            tvBest.setText("BEST: " + highScore);
            prefs.edit().putInt("highScore", highScore).apply();
        }

        Toast.makeText(this, "WELL DONE! ✨", Toast.LENGTH_SHORT).show();
        
        tvCurrent.postDelayed(this::startNewLevel, 1500);
    }

    @Override
    public void onGameOver() {
        if (countDownTimer != null) countDownTimer.cancel();
        Toast.makeText(this, "THE GHOST CAUGHT YOU!", Toast.LENGTH_SHORT).show();
        resetLevel();
    }

    private void resetLevel() {
        score = Math.max(0, score - 50); // Small penalty
        tvScore.setText("SCORE: " + score);
        startNewLevel();
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
                handleTimeout();
            }
        }.start();
    }

    private void handleTimeout() {
        Toast.makeText(this, "YOUR TORCH RAN OUT OF BATTERY!", Toast.LENGTH_LONG).show();
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

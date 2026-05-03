package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class ShootingGameActivity extends AppCompatActivity {

    private TextView tvScore, tvTimer, tvTurn;
    private FrameLayout targetContainer, rootLayout;
    private ImageView ivCrosshair;
    private View vGunBarrel;
    private Button btnStart, btnFire;
    private int currentScore = 0;
    private int player1Score = -1;
    private boolean isPlayer2Turn = false;
    private boolean isGameRunning = false;
    private Random random = new Random();
    private CountDownTimer gameTimer;
    private final long GAME_DURATION = 30000;
    private Vibrator vibrator;

    private final Handler aimHandler = new Handler(Looper.getMainLooper());
    private float aimAngle = 0;
    private float driftSpeed = 0.08f;
    private final float aimRadius = 300f; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shooting_game);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        tvScore = findViewById(R.id.tvShootingScore);
        tvTimer = findViewById(R.id.tvShootingTimer);
        tvTurn = findViewById(R.id.tvShootingTurn);
        targetContainer = findViewById(R.id.targetContainer);
        ivCrosshair = findViewById(R.id.ivCrosshair);
        vGunBarrel = findViewById(R.id.vGunBarrel);
        rootLayout = findViewById(R.id.shootingLayout);
        btnStart = findViewById(R.id.btnStartShooting);
        btnFire = findViewById(R.id.btnFire);

        btnStart.setOnClickListener(v -> prepareTurn());
        btnFire.setOnClickListener(v -> fireShot());

        tvTurn.setText("Pivoting Aim Ready");
    }

    private void prepareTurn() {
        btnStart.setVisibility(View.GONE);
        btnFire.setVisibility(View.GONE);
        vGunBarrel.setVisibility(View.VISIBLE);
        tvScore.setText("Score: 0");
        currentScore = 0;
        driftSpeed = 0.08f;
        
        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTurn.setText(String.valueOf(millisUntilFinished / 1000 + 1));
            }
            @Override
            public void onFinish() {
                startGame();
            }
        }.start();
    }

    private void startGame() {
        isGameRunning = true;
        targetContainer.setVisibility(View.VISIBLE);
        ivCrosshair.setVisibility(View.VISIBLE);
        btnFire.setVisibility(View.VISIBLE);
        tvTurn.setText(isPlayer2Turn ? "P2: AIM & FIRE!" : "P1: AIM & FIRE!");
        
        moveTarget();
        startAimMovement();
        
        gameTimer = new CountDownTimer(GAME_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Time: " + (millisUntilFinished / 1000 + 1));
            }

            @Override
            public void onFinish() {
                endTurn();
            }
        }.start();
    }

    private void startAimMovement() {
        aimHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!isGameRunning) return;

                // Move crosshair in a complex drifting pattern
                aimAngle += driftSpeed;
                float offX = (float) Math.sin(aimAngle) * aimRadius;
                float offY = (float) Math.cos(aimAngle * 1.3f) * (aimRadius / 1.2f);

                float targetCenterX = targetContainer.getX() + targetContainer.getWidth() / 2f;
                float targetCenterY = targetContainer.getY() + targetContainer.getHeight() / 2f;

                ivCrosshair.setX(targetCenterX + offX - ivCrosshair.getWidth() / 2f);
                ivCrosshair.setY(targetCenterY + offY - ivCrosshair.getHeight() / 2f);

                // Rotate gun barrel to point at crosshair
                float dx = ivCrosshair.getX() + ivCrosshair.getWidth()/2f - (rootLayout.getWidth() / 2f);
                float dy = ivCrosshair.getY() + ivCrosshair.getHeight()/2f - rootLayout.getHeight();
                float rotation = (float) Math.toDegrees(Math.atan2(dy, dx)) + 90;
                vGunBarrel.setRotation(rotation);

                aimHandler.postDelayed(this, 20);
            }
        });
    }

    private void fireShot() {
        if (!isGameRunning) return;

        float centerX = targetContainer.getX() + targetContainer.getWidth() / 2f;
        float centerY = targetContainer.getY() + targetContainer.getHeight() / 2f;
        float crossX = ivCrosshair.getX() + ivCrosshair.getWidth() / 2f;
        float crossY = ivCrosshair.getY() + ivCrosshair.getHeight() / 2f;

        double distance = Math.sqrt(Math.pow(centerX - crossX, 2) + Math.pow(centerY - crossY, 2));
        float density = getResources().getDisplayMetrics().density;
        
        int points = 0;
        String label = "MISS!";

        if (distance < 25 * density) { points = 10; label = "BULLSEYE!"; driftSpeed += 0.01f; }
        else if (distance < 55 * density) { points = 5; label = "GREAT!"; }
        else if (distance < 85 * density) { points = 1; label = "HIT!"; }

        currentScore += points;
        tvScore.setText("Score: " + currentScore);

        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(70);
            }
        }

        showFloatingText(label, points);
        showMuzzleFlash();
        
        if (points > 0) {
            moveTarget(); 
            aimAngle = 0; 
        }
    }

    private void showMuzzleFlash() {
        final View flash = new View(this);
        flash.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        flash.setBackgroundColor(Color.argb(100, 255, 255, 255));
        rootLayout.addView(flash);
        new Handler(Looper.getMainLooper()).postDelayed(() -> rootLayout.removeView(flash), 50);
        
        TranslateAnimation shake = new TranslateAnimation(-20, 20, -20, 20);
        shake.setDuration(50);
        shake.setRepeatCount(2);
        rootLayout.startAnimation(shake);
    }

    private void showFloatingText(String text, int points) {
        final TextView floatText = new TextView(this);
        floatText.setText(text + (points > 0 ? " +" + points : ""));
        floatText.setTextColor(points == 10 ? Color.YELLOW : Color.WHITE);
        floatText.setTextSize(26);
        floatText.setTypeface(null, android.graphics.Typeface.BOLD);
        floatText.setX(ivCrosshair.getX());
        floatText.setY(ivCrosshair.getY() - 120);
        
        rootLayout.addView(floatText);

        AnimationSet animSet = new AnimationSet(true);
        animSet.addAnimation(new TranslateAnimation(0, 0, 0, -300));
        animSet.addAnimation(new AlphaAnimation(1.0f, 0.0f));
        animSet.setDuration(1000);
        animSet.setInterpolator(new AccelerateInterpolator());
        
        floatText.startAnimation(animSet);
        new Handler(Looper.getMainLooper()).postDelayed(() -> rootLayout.removeView(floatText), 1000);
    }

    private void moveTarget() {
        rootLayout.post(() -> {
            int width = rootLayout.getWidth();
            int height = rootLayout.getHeight();
            if (width <= 0 || height <= 0) return;

            int targetSize = targetContainer.getWidth();
            int minX = 150, maxX = width - targetSize - 150;
            int minY = 450, maxY = height - targetSize - 400;

            if (maxX > minX && maxY > minY) {
                targetContainer.setX(random.nextInt(maxX - minX) + minX);
                targetContainer.setY(random.nextInt(maxY - minY) + minY);
                
                ScaleAnimation anim = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                anim.setDuration(250);
                targetContainer.startAnimation(anim);
            }
        });
    }

    private void endTurn() {
        isGameRunning = false;
        targetContainer.setVisibility(View.GONE);
        ivCrosshair.setVisibility(View.GONE);
        btnFire.setVisibility(View.GONE);
        vGunBarrel.setVisibility(View.GONE);
        aimHandler.removeCallbacksAndMessages(null);

        if (!isPlayer2Turn) {
            player1Score = currentScore;
            isPlayer2Turn = true;
            btnStart.setVisibility(View.VISIBLE);
            btnStart.setText("Player 2: START");
            tvTurn.setText("P1 Score: " + player1Score);
        } else {
            showFinalWinner(currentScore);
        }
    }

    private void showFinalWinner(int p2Score) {
        String result;
        if (player1Score > p2Score) result = "🏆 Player 1 Wins! (" + player1Score + "-" + p2Score + ")";
        else if (p2Score > player1Score) result = "🏆 Player 2 Wins! (" + p2Score + "-" + player1Score + ")";
        else result = "🤝 Tie! (" + player1Score + ")";
        
        tvTurn.setText("GAME OVER");
        tvScore.setText(result);
        btnStart.setVisibility(View.VISIBLE);
        btnStart.setText("REMATCH?");
        isPlayer2Turn = false;
        player1Score = -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isGameRunning = false;
        aimHandler.removeCallbacksAndMessages(null);
        if (gameTimer != null) gameTimer.cancel();
    }
}

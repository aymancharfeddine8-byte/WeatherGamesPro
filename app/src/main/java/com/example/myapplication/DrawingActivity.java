package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

public class DrawingActivity extends AppCompatActivity {

    private DrawingView drawingView;
    private TextView tvTask;
    private Button btnReveal;
    private boolean isTaskHidden = true;
    private String currentTask = "";

    private final String[] tasks = {
        "Draw a Cat 🐱", "Draw a House 🏠", "Draw a Tree 🌳", 
        "Draw an Apple 🍎", "Draw a Car 🚗", "Draw a Sun ☀️",
        "Draw a Fish 🐟", "Draw a Robot 🤖", "Draw a Flower 🌻",
        "Draw a Pizza 🍕", "Draw a Rocket 🚀", "Draw a Butterfly 🦋"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        // 1. Initialize all views
        drawingView = findViewById(R.id.drawingView);
        tvTask = findViewById(R.id.tvTask);
        btnReveal = findViewById(R.id.btnReveal);
        Button btnNewTask = findViewById(R.id.btnNewTask);
        Button btnClear = findViewById(R.id.btnClear);
        Button btnShare = findViewById(R.id.btnShare);
        ImageButton btnUndo = findViewById(R.id.btnUndo);
        ImageButton btnRedo = findViewById(R.id.btnRedo);
        CheckBox cbGlow = findViewById(R.id.cbGlow);
        CheckBox cbRainbow = findViewById(R.id.cbRainbow);
        SeekBar sbSize = findViewById(R.id.sbSize);
        SeekBar sbOpacity = findViewById(R.id.sbOpacity);
        LinearLayout colorPalette = findViewById(R.id.colorPaletteLayout);

        // 2. Set the first secret task
        setNewTask();

        // 3. Reveal logic: Toggles text between "Secret" and the actual task
        btnReveal.setOnClickListener(v -> {
            isTaskHidden = !isTaskHidden;
            if (isTaskHidden) {
                tvTask.setText(R.string.hidden_task);
                btnReveal.setText(R.string.reveal_text);
            } else {
                tvTask.setText(currentTask);
                btnReveal.setText(R.string.hide_text);
            }
        });

        // 4. New Task logic: Clears the drawing and picks a new word
        btnNewTask.setOnClickListener(v -> {
            setNewTask();
            drawingView.clearCanvas();
            Toast.makeText(this, "New drawing challenge ready!", Toast.LENGTH_SHORT).show();
        });

        // 5. Utility buttons
        btnClear.setOnClickListener(v -> {
            drawingView.clearCanvas();
            Toast.makeText(this, "Canvas Cleared", Toast.LENGTH_SHORT).show();
        });

        btnUndo.setOnClickListener(v -> drawingView.undo());
        btnRedo.setOnClickListener(v -> drawingView.redo());
        btnShare.setOnClickListener(v -> shareDrawing());

        // 6. Settings listeners
        cbGlow.setOnCheckedChangeListener((btn, isChecked) -> drawingView.setGlowMode(isChecked));
        cbRainbow.setOnCheckedChangeListener((btn, isChecked) -> drawingView.setRainbowMode(isChecked));

        sbSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawingView.setBrushSize(progress + 1);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sbOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawingView.setOpacity(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 7. Setup color palette listeners
        for (int i = 0; i < colorPalette.getChildCount(); i++) {
            View colorView = colorPalette.getChildAt(i);
            colorView.setOnClickListener(v -> {
                String colorCode = v.getTag().toString();
                drawingView.setColor(Color.parseColor(colorCode));
                // Turn off rainbow mode if a solid color is picked
                if (cbRainbow.isChecked()) {
                    cbRainbow.setChecked(false);
                    drawingView.setRainbowMode(false);
                }
            });
        }
    }

    private void setNewTask() {
        currentTask = tasks[new Random().nextInt(tasks.length)];
        isTaskHidden = true;
        if (tvTask != null) tvTask.setText(R.string.hidden_task);
        if (btnReveal != null) btnReveal.setText(R.string.reveal_text);
    }

    private void shareDrawing() {
        Bitmap bitmap = drawingView.getBitmap();
        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "drawing.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);

            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Look at what I drew in my app!");
                startActivity(Intent.createChooser(shareIntent, "Share with friends"));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error sharing drawing", Toast.LENGTH_SHORT).show();
        }
    }
}

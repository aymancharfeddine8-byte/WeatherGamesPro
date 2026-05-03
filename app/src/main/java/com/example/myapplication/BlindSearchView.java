package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.Random;

public class BlindSearchView extends View {

    public interface OnPathListener {
        void onPathChanged(int signal);
        void onPathComplete(int signal);
    }

    private OnPathListener listener;
    private float touchX = -1, touchY = -1;
    private float targetX, targetY;
    private float targetRadius = 50f;
    private float flashlightRadius = 250f;
    
    private Paint targetPaint, lightPaint, maskPaint;
    private boolean isFound = false;
    private Random random = new Random();

    public BlindSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        targetPaint.setColor(Color.YELLOW);

        lightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lightPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        maskPaint = new Paint();
        maskPaint.setColor(Color.BLACK);
        
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public void setOnPathListener(OnPathListener listener) {
        this.listener = listener;
    }

    public void generateGrid() {
        isFound = false;
        touchX = -1;
        touchY = -1;
        
        post(() -> {
            if (getWidth() > 0 && getHeight() > 0) {
                targetX = targetRadius + random.nextInt((int) (getWidth() - 2 * targetRadius));
                targetY = targetRadius + random.nextInt((int) (getHeight() - 2 * targetRadius));
                invalidate();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 1. Dark background
        canvas.drawColor(Color.parseColor("#0A0A1A"));

        // 2. Draw target if found or if flashlight is over it
        boolean showTarget = isFound;
        if (!showTarget && touchX != -1) {
            float dx = touchX - targetX;
            float dy = touchY - targetY;
            if (Math.sqrt(dx * dx + dy * dy) < flashlightRadius) {
                showTarget = true;
            }
        }
        
        if (showTarget) {
            canvas.drawCircle(targetX, targetY, targetRadius, targetPaint);
        }

        // 3. Darkness layer with flashlight cutout
        canvas.saveLayer(0, 0, getWidth(), getHeight(), null);
        canvas.drawRect(0, 0, getWidth(), getHeight(), maskPaint);
        
        if (touchX != -1 && !isFound) {
            canvas.drawCircle(touchX, touchY, flashlightRadius, lightPaint);
        }
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isFound) return true;

        touchX = event.getX();
        touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                checkIfFound();
                break;
            case MotionEvent.ACTION_UP:
                touchX = -1;
                touchY = -1;
                break;
        }
        invalidate();
        return true;
    }

    private void checkIfFound() {
        float dx = touchX - targetX;
        float dy = touchY - targetY;
        if (Math.sqrt(dx * dx + dy * dy) < targetRadius) {
            isFound = true;
            if (listener != null) {
                listener.onPathComplete(1);
            }
        }
    }
}

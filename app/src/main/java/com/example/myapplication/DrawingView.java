package com.example.myapplication;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

public class DrawingView extends View {

    private Path drawPath;
    private Paint drawPaint;
    private int paintColor = Color.BLACK;
    private float brushSize = 20f;
    private int opacity = 255;
    private boolean eraserMode = false;
    private boolean glowMode = false;
    private boolean rainbowMode = false;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private ArrayList<CustomPath> paths = new ArrayList<>();
    private ArrayList<CustomPath> undonePaths = new ArrayList<>();

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setAntiAlias(true);
        drawPaint.setDither(true);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        setLayerType(LAYER_TYPE_SOFTWARE, null); // Required for ShadowLayer (glow)
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (CustomPath p : paths) {
            drawPaint.setStrokeWidth(p.brushThickness);
            
            if (p.isEraser) {
                drawPaint.setColor(Color.WHITE);
                drawPaint.clearShadowLayer();
            } else {
                drawPaint.setColor(p.color);
                if (p.isGlow) {
                    drawPaint.setShadowLayer(p.brushThickness * 1.5f, 0, 0, p.color);
                } else {
                    drawPaint.clearShadowLayer();
                }
            }
            drawPaint.setAlpha(p.alpha); // MUST set alpha AFTER color
            canvas.drawPath(p.path, drawPaint);
        }

        // Draw current path
        drawPaint.setStrokeWidth(brushSize);
        if (eraserMode) {
            drawPaint.setColor(Color.WHITE);
            drawPaint.clearShadowLayer();
        } else {
            drawPaint.setColor(paintColor);
            if (glowMode) {
                drawPaint.setShadowLayer(brushSize * 1.5f, 0, 0, paintColor);
            } else {
                drawPaint.clearShadowLayer();
            }
        }
        drawPaint.setAlpha(opacity); // MUST set alpha AFTER color
        canvas.drawPath(drawPath, drawPaint);
    }

    private void touchStart(float x, float y) {
        drawPath.reset();
        drawPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            drawPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
            
            if (rainbowMode && !eraserMode) {
                paintColor = Color.HSVToColor(new float[]{(System.currentTimeMillis() / 10) % 360, 1f, 1f});
            }
        }
    }

    private void touchUp() {
        drawPath.lineTo(mX, mY);
        paths.add(new CustomPath(paintColor, brushSize, new Path(drawPath), opacity, eraserMode, glowMode));
        drawPath.reset();
        undonePaths.clear();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: touchStart(x, y); break;
            case MotionEvent.ACTION_MOVE: touchMove(x, y); break;
            case MotionEvent.ACTION_UP: touchUp(); break;
        }
        invalidate();
        return true;
    }

    public void setColor(int newColor) { paintColor = newColor; eraserMode = false; rainbowMode = false; }
    public void setBrushSize(float newSize) { brushSize = newSize; }
    public void setOpacity(int newAlpha) { opacity = newAlpha; }
    public void setEraserMode(boolean isEraser) { eraserMode = isEraser; if(isEraser) rainbowMode = false; }
    public void setGlowMode(boolean isGlow) { glowMode = isGlow; }
    public void setRainbowMode(boolean isRainbow) { rainbowMode = isRainbow; if(isRainbow) eraserMode = false; }

    public void undo() { if (paths.size() > 0) { undonePaths.add(paths.remove(paths.size() - 1)); invalidate(); } }
    public void redo() { if (undonePaths.size() > 0) { paths.add(undonePaths.remove(undonePaths.size() - 1)); invalidate(); } }
    public void clearCanvas() { paths.clear(); undonePaths.clear(); invalidate(); }

    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        draw(canvas);
        return bitmap;
    }

    private static class CustomPath {
        int color, alpha;
        float brushThickness;
        Path path;
        boolean isEraser, isGlow;

        CustomPath(int color, float brushThickness, Path path, int alpha, boolean isEraser, boolean isGlow) {
            this.color = color;
            this.brushThickness = brushThickness;
            this.path = path;
            this.alpha = alpha;
            this.isEraser = isEraser;
            this.isGlow = isGlow;
        }
    }
}

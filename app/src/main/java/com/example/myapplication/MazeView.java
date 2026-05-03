package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class MazeView extends View {

    public interface OnMazeListener {
        void onMazeComplete();
        void onGameOver();
    }

    private OnMazeListener listener;
    private int cols = 25, rows = 40; 
    private Cell[][] cells;
    private float cellSize;
    private float hMargin, vMargin;

    private float playerX, playerY; 
    private float targetX, targetY; 
    
    private float touchX = -1, touchY = -1;
    private float flashlightRadius = 160f;
    private float flickerAlpha = 0;

    private Paint wallPaint, playerPaint, starPaint, lightPaint, maskPaint, ghostPaint, trailPaint;
    private boolean isComplete = false;
    private Random random = new Random();
    
    private ArrayList<Ghost> ghosts = new ArrayList<>();
    private boolean[][] visitedTrail;
    private Vibrator vibrator;

    public MazeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        init();
    }

    private void init() {
        wallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wallPaint.setColor(Color.WHITE);
        wallPaint.setStrokeWidth(3f);

        playerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        playerPaint.setColor(Color.CYAN);

        starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starPaint.setColor(Color.YELLOW);
        
        ghostPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ghostPaint.setColor(Color.RED);
        ghostPaint.setAlpha(180);

        trailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trailPaint.setColor(Color.CYAN);
        trailPaint.setAlpha(40);

        lightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lightPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        maskPaint = new Paint();
        maskPaint.setColor(Color.BLACK);

        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public void setOnMazeListener(OnMazeListener listener) {
        this.listener = listener;
    }

    public void generateMaze(int level) {
        isComplete = false;
        cells = new Cell[cols][rows];
        visitedTrail = new boolean[cols][rows];
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                cells[x][y] = new Cell(x, y);
            }
        }

        Stack<Cell> stack = new Stack<>();
        Cell current = cells[0][0];
        current.visited = true;
        
        int visitedCount = 1;
        int totalCells = cols * rows;

        while (visitedCount < totalCells) {
            ArrayList<Cell> neighbors = getUnvisitedNeighbors(current);
            if (!neighbors.isEmpty()) {
                Cell next = neighbors.get(random.nextInt(neighbors.size()));
                removeWall(current, next);
                stack.push(current);
                current = next;
                current.visited = true;
                visitedCount++;
            } else if (!stack.isEmpty()) {
                current = stack.pop();
            }
        }

        playerX = 0.5f;
        playerY = 0.5f;
        targetX = cols - 0.5f;
        targetY = rows - 0.5f;
        
        // Add ghosts based on level
        ghosts.clear();
        int ghostCount = Math.min(10, 2 + level);
        for (int i = 0; i < ghostCount; i++) {
            ghosts.add(new Ghost(random.nextInt(cols), random.nextInt(rows)));
        }
        
        invalidate();
    }

    private ArrayList<Cell> getUnvisitedNeighbors(Cell cell) {
        ArrayList<Cell> neighbors = new ArrayList<>();
        if (cell.x > 0 && !cells[cell.x - 1][cell.y].visited) neighbors.add(cells[cell.x - 1][cell.y]);
        if (cell.x < cols - 1 && !cells[cell.x + 1][cell.y].visited) neighbors.add(cells[cell.x + 1][cell.y]);
        if (cell.y > 0 && !cells[cell.x][cell.y - 1].visited) neighbors.add(cells[cell.x][cell.y - 1]);
        if (cell.y < rows - 1 && !cells[cell.x][cell.y + 1].visited) neighbors.add(cells[cell.x][cell.y + 1]);
        return neighbors;
    }

    private void removeWall(Cell current, Cell next) {
        if (current.x == next.x) {
            if (current.y > next.y) {
                current.topWall = false;
                next.bottomWall = false;
            } else {
                current.bottomWall = false;
                next.topWall = false;
            }
        } else {
            if (current.x > next.x) {
                current.leftWall = false;
                next.rightWall = false;
            } else {
                current.rightWall = false;
                next.leftWall = false;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cellSize = Math.min((float) getWidth() / cols, (float) getHeight() / rows);
        hMargin = (getWidth() - cols * cellSize) / 2;
        vMargin = (getHeight() - rows * cellSize) / 2;
        generateMaze(1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.parseColor("#050510"));

        if (cells == null) return;

        // 1. Draw visited trail
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                if (visitedTrail[x][y]) {
                    canvas.drawRect(hMargin + x * cellSize, vMargin + y * cellSize, 
                        hMargin + (x + 1) * cellSize, vMargin + (y + 1) * cellSize, trailPaint);
                }
            }
        }

        // 2. Draw walls, player, and star
        float px = hMargin + playerX * cellSize;
        float py = vMargin + playerY * cellSize;
        float tx = hMargin + targetX * cellSize;
        float ty = vMargin + targetY * cellSize;

        canvas.drawCircle(px, py, cellSize * 0.35f, playerPaint);
        drawStar(canvas, tx, ty, cellSize * 0.45f);

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                float left = hMargin + x * cellSize;
                float top = vMargin + y * cellSize;
                float right = left + cellSize;
                float bottom = top + cellSize;
                if (cells[x][y].topWall) canvas.drawLine(left, top, right, top, wallPaint);
                if (cells[x][y].bottomWall) canvas.drawLine(left, bottom, right, bottom, wallPaint);
                if (cells[x][y].leftWall) canvas.drawLine(left, top, left, bottom, wallPaint);
                if (cells[x][y].rightWall) canvas.drawLine(right, top, right, bottom, wallPaint);
            }
        }

        // 3. Draw Ghosts
        for (Ghost g : ghosts) {
            g.update();
            canvas.drawCircle(hMargin + g.x * cellSize + cellSize/2, vMargin + g.y * cellSize + cellSize/2, cellSize * 0.3f, ghostPaint);
            if (Math.abs(g.x - playerX) < 0.5f && Math.abs(g.y - playerY) < 0.5f) {
                if (!isComplete) gameOver();
            }
        }

        // 4. Torch Flicker and Mask
        float flicker = (float) (Math.random() * 20 - 10);
        canvas.saveLayer(0, 0, getWidth(), getHeight(), null);
        canvas.drawRect(0, 0, getWidth(), getHeight(), maskPaint);
        if (touchX != -1) {
            canvas.drawCircle(touchX, touchY, flashlightRadius + flicker, lightPaint);
        }
        canvas.restore();
        
        if (!isComplete) invalidate(); // Continuous update for ghosts and flicker
    }

    private void gameOver() {
        if (vibrator != null) vibrator.vibrate(200);
        if (listener != null) listener.onGameOver();
        generateMaze(1);
    }

    private void drawStar(Canvas canvas, float x, float y, float radius) {
        Path path = new Path();
        float outerRadius = radius;
        float innerRadius = radius / 2.5f;
        double angle = Math.PI / 5;
        for (int i = 0; i < 10; i++) {
            float r = (i % 2 == 0) ? outerRadius : innerRadius;
            float currX = (float) (x + r * Math.sin(i * angle));
            float currY = (float) (y - r * Math.cos(i * angle));
            if (i == 0) path.moveTo(currX, currY);
            else path.lineTo(currX, currY);
        }
        path.close();
        canvas.drawPath(path, starPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isComplete) return true;
        touchX = event.getX();
        touchY = event.getY();
        if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
            movePlayerTowards(touchX, touchY);
            checkWin();
        }
        return true;
    }

    private void movePlayerTowards(float tx, float ty) {
        float gx = (tx - hMargin) / cellSize;
        float gy = (ty - vMargin) / cellSize;
        float dx = gx - playerX;
        float dy = gy - playerY;
        float step = 0.2f;

        if (Math.abs(dx) > Math.abs(dy)) {
            if (Math.abs(dx) > 0.1) tryMove(playerX + Math.signum(dx) * step, playerY);
        } else {
            if (Math.abs(dy) > 0.1) tryMove(playerX, playerY + Math.signum(dy) * step);
        }
        
        int cx = (int) playerX;
        int cy = (int) playerY;
        if (cx >= 0 && cx < cols && cy >= 0 && cy < rows) visitedTrail[cx][cy] = true;
    }

    private void tryMove(float nextX, float nextY) {
        int cx = (int) playerX; int cy = (int) playerY;
        int nx = (int) nextX; int ny = (int) nextY;
        if (nx < 0 || nx >= cols || ny < 0 || ny >= rows) return;
        if (nx != cx) {
            if (nx > cx && cells[cx][cy].rightWall) return;
            if (nx < cx && cells[cx][cy].leftWall) return;
        }
        if (ny != cy) {
            if (ny > cy && cells[cx][cy].bottomWall) return;
            if (ny < cy && cells[cx][cy].topWall) return;
        }
        playerX = nextX; playerY = nextY;
    }

    private void checkWin() {
        if (Math.abs(playerX - targetX) < 0.5f && Math.abs(playerY - targetY) < 0.5f) {
            isComplete = true;
            if (listener != null) listener.onMazeComplete();
        }
    }

    private class Ghost {
        float x, y;
        int dir; // 0:up, 1:right, 2:down, 3:left
        long lastMove;
        Ghost(int x, int y) { this.x = x; this.y = y; this.dir = random.nextInt(4); }
        void update() {
            if (System.currentTimeMillis() - lastMove > 400) {
                int nx = (int)x, ny = (int)y;
                if (dir == 0) ny--; else if (dir == 1) nx++; else if (dir == 2) ny++; else nx--;
                
                if (nx >= 0 && nx < cols && ny >= 0 && ny < rows && !hasWall(nx, ny)) {
                    x = nx; y = ny;
                } else {
                    dir = random.nextInt(4);
                }
                lastMove = System.currentTimeMillis();
            }
        }
        boolean hasWall(int nx, int ny) {
            int cx = (int)x, cy = (int)y;
            if (nx > cx) return cells[cx][cy].rightWall;
            if (nx < cx) return cells[cx][cy].leftWall;
            if (ny > cy) return cells[cx][cy].bottomWall;
            if (ny < cy) return cells[cx][cy].topWall;
            return false;
        }
    }

    private static class Cell {
        int x, y;
        boolean topWall = true, bottomWall = true, leftWall = true, rightWall = true, visited = false;
        Cell(int x, int y) { this.x = x; this.y = y; }
    }
}

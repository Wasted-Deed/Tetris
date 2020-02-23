package com.example.tetris;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameField extends SurfaceView implements SurfaceHolder.Callback {
    public static int FPS_SPLIT = 8;
    private final int TILES_X = 10;
    private final int TILES_Y = 22;
    private final int step = 100;
    public static int points = 0;

    Tile currentTile;

    private MainThread thread;
    private Field field;
    private int displayWidth;
    private int displayHeight;
    private boolean isTouching = false;
    private int currentFPS;
    private int frameCounter = 0;

    public GameField(Context context) {
        super(context);
        getHolder().addCallback(this);
        thread = new MainThread(getHolder(), this);
        setFocusable(true);
        currentFPS = 32;
    }

    private void setupField() {
        field = new Field(TILES_X, TILES_Y);
        field.setWidth(displayWidth);
        field.setHeight(displayHeight);
        field.clearField();
    }

    private void setupTile() {
        int type = (int) (Math.random() * 7);
        currentTile = new Tile(TileType.values()[type]);
        currentTile.setX(TILES_X / 2);
        currentTile.setY(0);
        currentTile.setRandomColor(Math.random());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setupField();
        thread = new MainThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
        setupTile();
    }

    public void update() {
        frameCounter++;
        if (frameCounter == MainThread.MAX_FPS) {
            frameCounter = 0;
        }
        if (currentTile != null) {
            if (frameCounter % FPS_SPLIT == 0) {
                field.update(currentTile);
                if (field.checkCollision(currentTile)) {
                    field.setTileIn(currentTile, Cell.FILLED);
                    setupTile();
                } else {
                    currentTile.setY(currentTile.getY() + 1);
                }
            }
            currentTile.setMovingShift(frameCounter % FPS_SPLIT);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        canvas.drawRect(new Rect(0, 0, displayWidth, displayHeight), paint);
        field.draw(canvas, currentTile.getMovingShift());
        paint.setColor(Color.BLACK);
        int textSize = 50;
        paint.setTextSize(textSize);
        canvas.drawText("Score: " + points, step / 2, step, paint);
        canvas.drawText("FPS: " + currentFPS, step / 2, step + textSize * 1.5f, paint);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        double oneThird = displayWidth / 3;
        double heightStep = displayHeight * 3 / 4;
        FPS_SPLIT = 8;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            isTouching = false;
        }
        if (isTouching && event.getY() > heightStep) {
            FPS_SPLIT = 1;
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN && currentTile != null) {
            isTouching = true;
            if(event.getY() > heightStep){
                return true;
            }
            if (event.getX() > oneThird * 2 && field.checkTileSide(currentTile, 1)) {
                currentTile.setX(currentTile.getX() + 1);
            }
            if (event.getX() < oneThird && field.checkTileSide(currentTile, -1)) {
                currentTile.setX(currentTile.getX() - 1);
            }
            if (event.getX() >= oneThird && event.getX() <= oneThird * 2) {
                int prevRotation = currentTile.getRotation();
                currentTile.changeRotation();
                if (!field.checkTileSide(currentTile, 0)) {
                    currentTile.setRotation(prevRotation);
                }
            }
        }


        return super.onTouchEvent(event);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean isClosed = false;
        while (!isClosed) {
            try {
                thread.setRunning(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            isClosed = true;
        }
    }

    public void setDisplayWidth(int displayWidth) {
        this.displayWidth = displayWidth;
    }

    public void setDisplayHeight(int displayHeight) {
        this.displayHeight = displayHeight;
    }

    public void setCurrentFPS(int currentFPS) {
        this.currentFPS = currentFPS;
    }
}

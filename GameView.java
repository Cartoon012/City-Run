package com.cityrunner.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    // Game states
    private enum State { MENU, PLAYING, DEAD }
    private State state = State.MENU;

    // Threading
    private Thread gameThread;
    private boolean running = false;
    private SurfaceHolder holder;

    // Screen
    private float screenWidth, screenHeight;

    // Game objects
    private Player player;
    private CityBackground background;
    private ArrayList<Obstacle> obstacles = new ArrayList<>();
    private ArrayList<Coin> coins = new ArrayList<>();

    // Game variables
    private float speed = 12f;
    private float maxSpeed = 28f;
    private float speedIncrease = 0.003f;
    private int score = 0;
    private int highScore = 0;
    private int coinCount = 0;
    private int frameCount = 0;

    // Spawning
    private int obstacleTimer = 0;
    private int obstacleInterval = 120;
    private int coinTimer = 0;
    private int coinInterval = 80;
    private Random random = new Random();

    // Touch
    private float touchStartY = 0;
    private static final float SWIPE_THRESHOLD = 80f;

    // UI Paints
    private Paint scorePaint, titlePaint, subtitlePaint, btnPaint, btnTextPaint,
                  coinPaint, overlayPaint, hsPaint;

    // Prefs
    private SharedPreferences prefs;

    public GameView(Context context) {
        super(context);
        holder = getHolder();
        prefs = context.getSharedPreferences("city_runner", Context.MODE_PRIVATE);
        highScore = prefs.getInt("high_score", 0);
        setupPaints();
    }

    private void setupPaints() {
        scorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scorePaint.setColor(Color.WHITE);
        scorePaint.setTextSize(72f);
        scorePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.WHITE);
        titlePaint.setTextSize(120f);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextAlign(Paint.Align.CENTER);

        subtitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subtitlePaint.setColor(Color.parseColor("#A8DADC"));
        subtitlePaint.setTextSize(52f);
        subtitlePaint.setTextAlign(Paint.Align.CENTER);

        btnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnPaint.setColor(Color.parseColor("#E63946"));

        btnTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnTextPaint.setColor(Color.WHITE);
        btnTextPaint.setTextSize(64f);
        btnTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        btnTextPaint.setTextAlign(Paint.Align.CENTER);

        coinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        coinPaint.setColor(Color.parseColor("#FFD700"));
        coinPaint.setTextSize(56f);
        coinPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        overlayPaint = new Paint();
        overlayPaint.setColor(Color.parseColor("#CC000000"));

        hsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hsPaint.setColor(Color.parseColor("#E9C46A"));
        hsPaint.setTextSize(52f);
        hsPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;
        initGame();
    }

    private void initGame() {
        player = new Player(screenWidth, screenHeight);
        background = new CityBackground(screenWidth, screenHeight);
        obstacles.clear();
        coins.clear();
        score = 0;
        coinCount = 0;
        speed = 12f;
        frameCount = 0;
        obstacleTimer = 60; // grace period at start
        coinTimer = 40;
    }

    @Override
    public void run() {
        long targetFPS = 60;
        long frameTime = 1000 / targetFPS;

        while (running) {
            long startTime = System.currentTimeMillis();

            update();
            draw();

            long elapsed = System.currentTimeMillis() - startTime;
            long sleep = frameTime - elapsed;
            if (sleep > 0) {
                try { Thread.sleep(sleep); } catch (InterruptedException e) { break; }
            }
        }
    }

    private void update() {
        if (state != State.PLAYING) return;

        frameCount++;
        score = frameCount / 6;

        // Speed ramp
        if (speed < maxSpeed) speed += speedIncrease;

        // Background
        background.update(speed);

        // Player
        player.update();

        // Obstacle spawning
        obstacleTimer--;
        if (obstacleTimer <= 0) {
            Obstacle.Type t = random.nextBoolean() ? Obstacle.Type.WALL : Obstacle.Type.LOW_BAR;
            obstacles.add(new Obstacle(screenWidth, screenHeight, t, speed));
            obstacleTimer = obstacleInterval - (int)(speed * 2); // faster game = shorter interval
            obstacleInterval = Math.max(60, obstacleInterval);
        }

        // Coin spawning
        coinTimer--;
        if (coinTimer <= 0) {
            float groundY = screenHeight * 0.72f;
            float coinY = groundY - screenHeight * (0.08f + random.nextFloat() * 0.12f);
            float coinR = screenWidth * 0.025f;
            coins.add(new Coin(screenWidth + 30, coinY, coinR));
            coinTimer = coinInterval;
        }

        // Update obstacles
        for (int i = obstacles.size() - 1; i >= 0; i--) {
            Obstacle obs = obstacles.get(i);
            obs.update(speed);

            if (obs.isOffScreen()) {
                obstacles.remove(i);
                continue;
            }

            // Collision
            if (RectF.intersects(player.getHitbox(), obs.getHitbox())) {
                gameOver();
                return;
            }

            // Score for passing
            if (!obs.passed && obs.x + obs.width < player.x) {
                obs.passed = true;
                score += 10;
            }
        }

        // Update coins
        for (int i = coins.size() - 1; i >= 0; i--) {
            Coin coin = coins.get(i);
            coin.update(speed);

            if (coin.isOffScreen() || coin.collected) {
                coins.remove(i);
                continue;
            }

            if (RectF.intersects(player.getHitbox(), coin.getHitbox())) {
                coin.collected = true;
                coinCount++;
                score += 5;
            }
        }
    }

    private void gameOver() {
        state = State.DEAD;
        if (score > highScore) {
            highScore = score;
            prefs.edit().putInt("high_score", highScore).apply();
        }
    }

    private void draw() {
        if (!holder.getSurface().isValid()) return;
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) return;

        try {
            if (state == State.MENU) {
                drawMenu(canvas);
            } else if (state == State.PLAYING) {
                drawGame(canvas);
            } else {
                drawGame(canvas);
                drawGameOver(canvas);
            }
        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawGame(Canvas canvas) {
        background.draw(canvas);

        for (Coin coin : coins) coin.draw(canvas);
        for (Obstacle obs : obstacles) obs.draw(canvas);
        player.draw(canvas);

        // HUD
        drawHUD(canvas);
    }

    private void drawHUD(Canvas canvas) {
        // Score
        canvas.drawText("" + score, 40, 100, scorePaint);

        // Coin counter
        float coinX = screenWidth - 220;
        Paint goldDot = new Paint(Paint.ANTI_ALIAS_FLAG);
        goldDot.setColor(Color.parseColor("#FFD700"));
        canvas.drawCircle(coinX, 72, 24, goldDot);
        coinPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("x" + coinCount, coinX + 34, 90, coinPaint);

        // Slide hint (when approaching low bar)
        for (Obstacle obs : obstacles) {
            if (obs.type == Obstacle.Type.LOW_BAR && obs.x - player.x < screenWidth * 0.35f && obs.x - player.x > 0) {
                Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                hintPaint.setColor(Color.parseColor("#A8DADC"));
                hintPaint.setTextSize(40f);
                hintPaint.setTextAlign(Paint.Align.CENTER);
                hintPaint.setAlpha(180);
                canvas.drawText("↓ SLIDE", screenWidth / 2f, screenHeight * 0.15f, hintPaint);
                break;
            }
        }
    }

    private void drawMenu(Canvas canvas) {
        // Background — just the city if initialized
        if (background != null) {
            background.draw(canvas);
        } else {
            canvas.drawColor(Color.parseColor("#0D1B2A"));
        }

        canvas.drawColor(Color.parseColor("#99000000"));

        float cx = screenWidth / 2f;
        float cy = screenHeight / 2f;

        // Title
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#E63946"));
        linePaint.setStrokeWidth(6);
        canvas.drawLine(cx - 220, cy - 210, cx + 220, cy - 210, linePaint);

        canvas.drawText("CITY", cx, cy - 120, titlePaint);
        canvas.drawText("RUNNER", cx, cy - 10, titlePaint);

        canvas.drawLine(cx - 220, cy + 10, cx + 220, cy + 10, linePaint);

        subtitlePaint.setTextSize(46f);
        canvas.drawText("Rooftop Parkour", cx, cy + 70, subtitlePaint);

        // Play button
        RectF btn = new RectF(cx - 220, cy + 140, cx + 220, cy + 240);
        canvas.drawRoundRect(btn, 30, 30, btnPaint);
        canvas.drawText("PLAY", cx, cy + 213, btnTextPaint);

        subtitlePaint.setTextSize(42f);
        canvas.drawText("Tap to jump · Swipe down to slide", cx, cy + 300, subtitlePaint);

        if (highScore > 0) {
            canvas.drawText("Best: " + highScore, cx, cy + 360, hsPaint);
        }
    }

    private void drawGameOver(Canvas canvas) {
        canvas.drawRect(0, 0, screenWidth, screenHeight, overlayPaint);

        float cx = screenWidth / 2f;
        float cy = screenHeight / 2f;

        // Card
        Paint cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardPaint.setColor(Color.parseColor("#1D3557"));
        canvas.drawRoundRect(new RectF(cx - 300, cy - 300, cx + 300, cy + 320), 30, 30, cardPaint);

        Paint cardEdge = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardEdge.setColor(Color.parseColor("#E63946"));
        cardEdge.setStyle(Paint.Style.STROKE);
        cardEdge.setStrokeWidth(4);
        canvas.drawRoundRect(new RectF(cx - 300, cy - 300, cx + 300, cy + 320), 30, 30, cardEdge);

        // Game Over text
        Paint goPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        goPaint.setColor(Color.parseColor("#E63946"));
        goPaint.setTextSize(90f);
        goPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        goPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("GAME OVER", cx, cy - 190, goPaint);

        // Score
        Paint bigScore = new Paint(Paint.ANTI_ALIAS_FLAG);
        bigScore.setColor(Color.WHITE);
        bigScore.setTextSize(120f);
        bigScore.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        bigScore.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("" + score, cx, cy - 50, bigScore);

        canvas.drawText("SCORE", cx, cy - 140, subtitlePaint);

        // Coins
        Paint coinLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        coinLine.setColor(Color.parseColor("#FFD700"));
        coinLine.setTextSize(52f);
        coinLine.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("🪙 " + coinCount + " coins", cx, cy + 40, coinLine);

        // High score
        if (score >= highScore) {
            Paint newHs = new Paint(Paint.ANTI_ALIAS_FLAG);
            newHs.setColor(Color.parseColor("#E9C46A"));
            newHs.setTextSize(48f);
            newHs.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("⭐ NEW BEST!", cx, cy + 110, newHs);
        } else {
            canvas.drawText("Best: " + highScore, cx, cy + 110, hsPaint);
        }

        // Retry button
        RectF btn = new RectF(cx - 200, cy + 170, cx + 200, cy + 270);
        canvas.drawRoundRect(btn, 25, 25, btnPaint);
        canvas.drawText("RETRY", cx, cy + 244, btnTextPaint);

        // Menu button
        Paint menuBtn = new Paint(Paint.ANTI_ALIAS_FLAG);
        menuBtn.setColor(Color.parseColor("#415A77"));
        RectF menuBtnR = new RectF(cx - 200, cy + 290, cx + 200, cy + 370);
        canvas.drawRoundRect(menuBtnR, 20, 20, menuBtn);
        Paint menuBtnText = new Paint(Paint.ANTI_ALIAS_FLAG);
        menuBtnText.setColor(Color.WHITE);
        menuBtnText.setTextSize(52f);
        menuBtnText.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("MENU", cx, cy + 350, menuBtnText);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float cx = screenWidth / 2f;
        float cy = screenHeight / 2f;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartY = event.getY();

                if (state == State.MENU) {
                    // Check play button
                    if (event.getX() > cx - 220 && event.getX() < cx + 220
                            && event.getY() > cy + 140 && event.getY() < cy + 240) {
                        state = State.PLAYING;
                        initGame();
                    }
                } else if (state == State.DEAD) {
                    // Retry button
                    if (event.getX() > cx - 200 && event.getX() < cx + 200
                            && event.getY() > cy + 170 && event.getY() < cy + 270) {
                        state = State.PLAYING;
                        initGame();
                    }
                    // Menu button
                    if (event.getX() > cx - 200 && event.getX() < cx + 200
                            && event.getY() > cy + 290 && event.getY() < cy + 370) {
                        state = State.MENU;
                        initGame();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (state == State.PLAYING) {
                    float deltaY = event.getY() - touchStartY;
                    if (deltaY > SWIPE_THRESHOLD) {
                        player.slide();
                    } else {
                        player.jump();
                    }
                }
                break;
        }
        return true;
    }

    public void pause() {
        running = false;
        try {
            if (gameThread != null) gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
}

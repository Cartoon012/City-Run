package com.cityrunner.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Obstacle {

    public enum Type {
        WALL,       // jump over this
        LOW_BAR     // slide under this
    }

    public float x, y, width, height;
    public Type type;
    public boolean passed = false;

    private Paint mainPaint, shadowPaint, accentPaint;

    public Obstacle(float screenWidth, float screenHeight, Type type, float speed) {
        this.type = type;
        float groundY = screenHeight * 0.72f;

        if (type == Type.WALL) {
            // A wall/AC unit to jump over
            this.width = screenWidth * 0.055f;
            this.height = screenHeight * 0.11f;
            this.x = screenWidth + 20;
            this.y = groundY - this.height;

            mainPaint = new Paint();
            mainPaint.setColor(Color.parseColor("#457B9D"));

            accentPaint = new Paint();
            accentPaint.setColor(Color.parseColor("#1D3557"));

        } else {
            // A low hanging pipe/banner to slide under
            this.width = screenWidth * 0.04f;
            this.height = screenHeight * 0.22f;
            this.x = screenWidth + 20;
            this.y = groundY - screenHeight * 0.18f; // floats high, player must slide

            mainPaint = new Paint();
            mainPaint.setColor(Color.parseColor("#E63946"));

            accentPaint = new Paint();
            accentPaint.setColor(Color.parseColor("#C1121F"));
        }

        shadowPaint = new Paint();
        shadowPaint.setColor(Color.parseColor("#33000000"));
    }

    public void update(float speed) {
        x -= speed;
    }

    public void draw(Canvas canvas) {
        if (type == Type.WALL) {
            // Shadow
            canvas.drawRoundRect(new RectF(x + 6, y + 6, x + width + 6, y + height + 6), 6, 6, shadowPaint);
            // Main body
            canvas.drawRoundRect(new RectF(x, y, x + width, y + height), 6, 6, mainPaint);
            // Window/detail stripe
            canvas.drawRoundRect(new RectF(x + width * 0.2f, y + height * 0.15f, x + width * 0.8f, y + height * 0.45f), 3, 3, accentPaint);
            canvas.drawRoundRect(new RectF(x + width * 0.2f, y + height * 0.55f, x + width * 0.8f, y + height * 0.85f), 3, 3, accentPaint);

        } else {
            // Vertical pole
            canvas.drawRoundRect(new RectF(x, y, x + width, y + height), 4, 4, mainPaint);
            // Top cap
            canvas.drawRoundRect(new RectF(x - width * 0.3f, y, x + width * 1.3f, y + height * 0.12f), 4, 4, accentPaint);
            // Bottom cap
            canvas.drawRoundRect(new RectF(x - width * 0.3f, y + height * 0.88f, x + width * 1.3f, y + height), 4, 4, accentPaint);
        }
    }

    public RectF getHitbox() {
        float m = width * 0.1f;
        return new RectF(x + m, y + m, x + width - m, y + height - m);
    }

    public boolean isOffScreen() {
        return x + width < 0;
    }
}

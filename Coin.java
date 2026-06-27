package com.cityrunner.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Coin {

    public float x, y;
    private float radius;
    public boolean collected = false;

    private Paint coinPaint, innerPaint;
    private float bobOffset = 0;
    private float bobTimer = 0;

    public Coin(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;

        coinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        coinPaint.setColor(Color.parseColor("#FFD700"));

        innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerPaint.setColor(Color.parseColor("#FFA500"));
    }

    public void update(float speed) {
        x -= speed;
        bobTimer += 0.12f;
        bobOffset = (float) Math.sin(bobTimer) * 5f;
    }

    public void draw(Canvas canvas) {
        if (!collected) {
            canvas.drawCircle(x, y + bobOffset, radius, coinPaint);
            canvas.drawCircle(x, y + bobOffset, radius * 0.6f, innerPaint);
            // "$" symbol hint - a center dot
            Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            dotPaint.setColor(Color.parseColor("#FFD700"));
            canvas.drawCircle(x, y + bobOffset, radius * 0.18f, dotPaint);
        }
    }

    public RectF getHitbox() {
        return new RectF(x - radius, y - radius + bobOffset, x + radius, y + radius + bobOffset);
    }

    public boolean isOffScreen() {
        return x + radius < 0;
    }
}

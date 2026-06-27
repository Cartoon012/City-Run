package com.cityrunner.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Player {

    // Position & size
    public float x, y;
    public float width, height;

    // Physics
    private float velocityY = 0;
    private float gravity = 2.2f;
    private float jumpForce = -38f;
    private boolean onGround = false;

    // Slide
    public boolean isSliding = false;
    private int slideTimer = 0;
    private static final int SLIDE_DURATION = 35;

    // Normal vs slide dimensions
    private float normalHeight;
    private float slideHeight;

    // Ground Y (set from screen)
    private float groundY;

    // Animation
    private int animFrame = 0;
    private int animTimer = 0;

    // Paint
    private Paint bodyPaint, headPaint, accentPaint, darkPaint;

    public Player(float screenWidth, float screenHeight) {
        normalHeight = screenHeight * 0.12f;
        slideHeight = normalHeight * 0.5f;
        width = normalHeight * 0.55f;

        groundY = screenHeight * 0.72f; // top of the rooftop platform
        x = screenWidth * 0.15f;
        y = groundY - normalHeight;
        height = normalHeight;

        bodyPaint = new Paint();
        bodyPaint.setColor(Color.parseColor("#E63946")); // red jacket

        headPaint = new Paint();
        headPaint.setColor(Color.parseColor("#F4A261")); // skin tone

        accentPaint = new Paint();
        accentPaint.setColor(Color.parseColor("#1D3557")); // dark navy pants

        darkPaint = new Paint();
        darkPaint.setColor(Color.parseColor("#222222"));
        darkPaint.setAntiAlias(true);
    }

    public void jump() {
        if (onGround && !isSliding) {
            velocityY = jumpForce;
            onGround = false;
        }
    }

    public void slide() {
        if (onGround && !isSliding) {
            isSliding = true;
            slideTimer = SLIDE_DURATION;
            height = slideHeight;
            y = groundY - slideHeight;
        }
    }

    public void update() {
        // Gravity
        velocityY += gravity;
        y += velocityY;

        // Ground collision
        float groundTop = groundY - height;
        if (y >= groundTop) {
            y = groundTop;
            velocityY = 0;
            onGround = true;
        } else {
            onGround = false;
        }

        // Slide timer
        if (isSliding) {
            slideTimer--;
            if (slideTimer <= 0) {
                isSliding = false;
                height = normalHeight;
                y = groundY - normalHeight;
            }
        }

        // Animation
        animTimer++;
        if (animTimer > 6) {
            animTimer = 0;
            animFrame = (animFrame + 1) % 4;
        }
    }

    public void draw(Canvas canvas) {
        float cx = x + width / 2f;

        if (isSliding) {
            // Sliding: body low
            float bodyTop = y;
            float bodyBottom = y + height;
            // torso
            canvas.drawRoundRect(new RectF(x, bodyTop, x + width, bodyBottom), 10, 10, bodyPaint);
            // head to the side
            float headR = height * 0.45f;
            canvas.drawCircle(cx + width * 0.3f, bodyTop + headR * 0.8f, headR, headPaint);
        } else {
            // Standing/running
            float headRadius = width * 0.38f;
            float headCX = cx;
            float headCY = y + headRadius;

            // Head
            canvas.drawCircle(headCX, headCY, headRadius, headPaint);

            // Body (torso)
            float torsoTop = headCY + headRadius - 4;
            float torsoBottom = y + height * 0.65f;
            canvas.drawRoundRect(new RectF(cx - width * 0.35f, torsoTop, cx + width * 0.35f, torsoBottom), 8, 8, bodyPaint);

            // Legs - animated
            float legTop = torsoBottom;
            float legBottom = y + height;
            float legW = width * 0.22f;

            float leftOffset = (float) Math.sin(animFrame * Math.PI / 2.0) * width * 0.15f;
            float rightOffset = -leftOffset;

            // Left leg
            canvas.drawRoundRect(new RectF(cx - legW - 4 + leftOffset, legTop, cx - 2 + leftOffset, legBottom), 6, 6, accentPaint);
            // Right leg
            canvas.drawRoundRect(new RectF(cx + 2 + rightOffset, legTop, cx + legW + 4 + rightOffset, legBottom), 6, 6, accentPaint);

            // Arms
            float armTop = torsoTop + 4;
            float armLen = height * 0.22f;
            float armW = width * 0.15f;
            float armSwing = (float) Math.sin(animFrame * Math.PI / 2.0) * width * 0.2f;

            canvas.drawRoundRect(new RectF(cx - width * 0.5f - armW + armSwing, armTop, cx - width * 0.35f + armSwing, armTop + armLen), 4, 4, bodyPaint);
            canvas.drawRoundRect(new RectF(cx + width * 0.35f - armSwing, armTop, cx + width * 0.5f + armW - armSwing, armTop + armLen), 4, 4, bodyPaint);
        }
    }

    public RectF getHitbox() {
        float margin = width * 0.1f;
        return new RectF(x + margin, y + margin, x + width - margin, y + height - margin);
    }
}

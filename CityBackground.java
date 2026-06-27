package com.cityrunner.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.ArrayList;
import java.util.Random;

public class CityBackground {

    private float screenWidth, screenHeight;
    private float groundY;

    // Parallax layers
    private ArrayList<Building> farBuildings = new ArrayList<>();
    private ArrayList<Building> midBuildings = new ArrayList<>();
    private float farScroll = 0, midScroll = 0;

    // Platform / rooftop the player runs on
    private float platformScroll = 0;

    private Paint skyPaint, groundPaint, platformPaint, platformEdgePaint;
    private Paint moonPaint, starPaint;

    private Random random = new Random(42);

    private static class Building {
        float x, width, height;
        int color;
        boolean hasWindows;
        int windowColor;
    }

    public CityBackground(float screenWidth, float screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.groundY = screenHeight * 0.72f;

        setupPaints(screenWidth, screenHeight);
        generateBuildings();
    }

    private void setupPaints(float w, float h) {
        skyPaint = new Paint();
        // Night sky gradient is applied per-draw

        groundPaint = new Paint();
        groundPaint.setColor(Color.parseColor("#0D1B2A"));

        platformPaint = new Paint();
        platformPaint.setColor(Color.parseColor("#1B263B"));

        platformEdgePaint = new Paint();
        platformEdgePaint.setColor(Color.parseColor("#415A77"));

        moonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        moonPaint.setColor(Color.parseColor("#E9C46A"));

        starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starPaint.setColor(Color.parseColor("#FFFFFF"));
    }

    private void generateBuildings() {
        // Far layer - tall distant skyscrapers
        float bx = 0;
        while (bx < screenWidth * 2.5f) {
            Building b = new Building();
            b.x = bx;
            b.width = screenWidth * (0.06f + random.nextFloat() * 0.08f);
            b.height = screenHeight * (0.18f + random.nextFloat() * 0.22f);
            b.color = randomFarColor();
            b.hasWindows = random.nextBoolean();
            b.windowColor = Color.parseColor("#E9C46A");
            farBuildings.add(b);
            bx += b.width + screenWidth * (0.01f + random.nextFloat() * 0.02f);
        }

        // Mid layer - closer buildings
        bx = 0;
        while (bx < screenWidth * 2.5f) {
            Building b = new Building();
            b.x = bx;
            b.width = screenWidth * (0.08f + random.nextFloat() * 0.1f);
            b.height = screenHeight * (0.12f + random.nextFloat() * 0.18f);
            b.color = randomMidColor();
            b.hasWindows = true;
            b.windowColor = Color.parseColor("#A8DADC");
            midBuildings.add(b);
            bx += b.width + screenWidth * (0.005f + random.nextFloat() * 0.015f);
        }
    }

    private int randomFarColor() {
        int[] colors = {
            Color.parseColor("#1D3557"),
            Color.parseColor("#1B263B"),
            Color.parseColor("#162032"),
            Color.parseColor("#253B52"),
        };
        return colors[random.nextInt(colors.length)];
    }

    private int randomMidColor() {
        int[] colors = {
            Color.parseColor("#264653"),
            Color.parseColor("#2A3F54"),
            Color.parseColor("#1F3A4A"),
            Color.parseColor("#354F5E"),
        };
        return colors[random.nextInt(colors.length)];
    }

    public void update(float speed) {
        farScroll += speed * 0.2f;
        midScroll += speed * 0.5f;
        platformScroll += speed;

        float farTotal = getTotalWidth(farBuildings);
        if (farScroll > farTotal / 2f) farScroll -= farTotal / 2f;

        float midTotal = getTotalWidth(midBuildings);
        if (midScroll > midTotal / 2f) midScroll -= midTotal / 2f;
    }

    private float getTotalWidth(ArrayList<Building> list) {
        float total = 0;
        for (Building b : list) total += b.width + 8;
        return total;
    }

    public void draw(Canvas canvas) {
        // Sky gradient
        Paint skyGrad = new Paint();
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, groundY,
            new int[]{Color.parseColor("#020B18"), Color.parseColor("#0D1B2A"), Color.parseColor("#1D3557")},
            new float[]{0f, 0.6f, 1f},
            Shader.TileMode.CLAMP
        );
        skyGrad.setShader(gradient);
        canvas.drawRect(0, 0, screenWidth, groundY, skyGrad);

        // Moon
        canvas.drawCircle(screenWidth * 0.82f, screenHeight * 0.1f, screenWidth * 0.06f, moonPaint);
        Paint moonShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        moonShadow.setColor(Color.parseColor("#1D3557"));
        canvas.drawCircle(screenWidth * 0.86f, screenHeight * 0.09f, screenWidth * 0.055f, moonShadow);

        // Stars
        starPaint.setAlpha(180);
        long[] starPositions = {
            encodePos(0.05f, 0.04f), encodePos(0.15f, 0.08f), encodePos(0.28f, 0.03f),
            encodePos(0.40f, 0.07f), encodePos(0.55f, 0.02f), encodePos(0.65f, 0.09f),
            encodePos(0.72f, 0.05f), encodePos(0.90f, 0.04f), encodePos(0.35f, 0.13f),
            encodePos(0.10f, 0.17f), encodePos(0.48f, 0.16f), encodePos(0.78f, 0.14f),
        };
        for (long sp : starPositions) {
            float sx = (sp >> 16) / 1000f * screenWidth;
            float sy = (sp & 0xFFFF) / 1000f * screenHeight;
            canvas.drawCircle(sx, sy, 2, starPaint);
        }

        // Far buildings
        drawBuildingLayer(canvas, farBuildings, farScroll, groundY, 0.75f);
        // Mid buildings
        drawBuildingLayer(canvas, midBuildings, midScroll, groundY, 1.0f);

        // Ground strip below rooftop
        canvas.drawRect(0, groundY, screenWidth, screenHeight, groundPaint);

        // Rooftop platform (scrolling tiles)
        drawPlatform(canvas);
    }

    private long encodePos(float fx, float fy) {
        return ((long)(fx * 1000) << 16) | (long)(fy * 1000);
    }

    private void drawBuildingLayer(Canvas canvas, ArrayList<Building> buildings, float scroll,
                                    float baseY, float alpha) {
        Paint p = new Paint();
        Paint win = new Paint(Paint.ANTI_ALIAS_FLAG);

        float drawX = -scroll;
        // Draw twice for seamless loop
        for (int repeat = 0; repeat < 3; repeat++) {
            for (Building b : buildings) {
                float bx = drawX + b.x;
                if (bx > screenWidth) break;
                if (bx + b.width < 0) continue;

                p.setColor(b.color);
                p.setAlpha((int)(255 * alpha));
                float top = baseY - b.height;
                canvas.drawRect(bx, top, bx + b.width, baseY, p);

                // Windows
                if (b.hasWindows) {
                    win.setColor(b.windowColor);
                    win.setAlpha((int)(180 * alpha));
                    float ww = b.width * 0.22f, wh = ww * 1.2f;
                    float gap = b.width * 0.28f;
                    for (float wy = top + b.height * 0.1f; wy < baseY - wh; wy += wh + gap * 0.8f) {
                        for (float wx = bx + gap * 0.3f; wx + ww < bx + b.width - gap * 0.2f; wx += ww + gap * 0.5f) {
                            if (random.nextFloat() > 0.35f) {
                                canvas.drawRoundRect(new RectF(wx, wy, wx + ww, wy + wh), 2, 2, win);
                            }
                        }
                    }
                }
            }
            drawX += getTotalWidth(buildings);
        }
    }

    private void drawPlatform(Canvas canvas) {
        float platformHeight = screenHeight * 0.06f;
        float top = groundY;
        float bottom = groundY + platformHeight;

        // Main platform surface
        canvas.drawRect(0, top, screenWidth, bottom, platformPaint);

        // Edge highlight
        Paint edgePaint = new Paint();
        edgePaint.setColor(Color.parseColor("#415A77"));
        canvas.drawRect(0, top, screenWidth, top + 4, edgePaint);

        // Scrolling tile lines
        Paint tilePaint = new Paint();
        tilePaint.setColor(Color.parseColor("#162032"));
        tilePaint.setStrokeWidth(2);
        float tileW = screenWidth * 0.15f;
        float offset = platformScroll % tileW;
        for (float tx = -offset; tx < screenWidth; tx += tileW) {
            canvas.drawLine(tx, top, tx, bottom, tilePaint);
        }
    }
}

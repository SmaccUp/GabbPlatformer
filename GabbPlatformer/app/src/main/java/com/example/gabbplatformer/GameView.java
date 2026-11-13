package com.example.gabbplatformer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Thread gameThread;
    private volatile boolean running = false;
    private volatile boolean paused = false;
    private final SurfaceHolder holder;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float screenW = 960, screenH = 540;
    private float playerX = 100, playerY = 300, playerVY = 0;
    private float playerW = 42, playerH = 54;
    private boolean left, right, jumpHeld, onGround;
    private boolean gameOver = false, won = false;
    private int score = 0;
    private float cameraX = 0;
    private final float gravity = 1800f;
    private final float moveSpeed = 330f;
    private final float jumpSpeed = -690f;
    private final List<RectF> platforms = new ArrayList<>();
    private final List<RectF> coins = new ArrayList<>();
    private final List<RectF> spikes = new ArrayList<>();
    private long lastFrame = 0;

    public GameView(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        setFocusable(true);
        buildLevel();
    }

    private void buildLevel() {
        platforms.clear(); coins.clear(); spikes.clear();
        platforms.add(new RectF(0, 440, 520, 540));
        platforms.add(new RectF(610, 390, 900, 540));
        platforms.add(new RectF(1000, 330, 1270, 540));
        platforms.add(new RectF(1380, 390, 1680, 540));
        platforms.add(new RectF(1790, 320, 2130, 540));
        platforms.add(new RectF(2240, 400, 2600, 540));
        platforms.add(new RectF(2700, 340, 3100, 540));
        platforms.add(new RectF(3220, 410, 3700, 540));
        platforms.add(new RectF(3800, 360, 4300, 540));
        coins.add(new RectF(250, 365, 278, 393));
        coins.add(new RectF(700, 315, 728, 343));
        coins.add(new RectF(1080, 255, 1108, 283));
        coins.add(new RectF(1490, 315, 1518, 343));
        coins.add(new RectF(1900, 245, 1928, 273));
        coins.add(new RectF(2380, 325, 2408, 353));
        coins.add(new RectF(2860, 265, 2888, 293));
        coins.add(new RectF(3400, 335, 3428, 363));
        spikes.add(new RectF(390, 414, 455, 440));
        spikes.add(new RectF(1110, 304, 1165, 330));
        spikes.add(new RectF(1850, 294, 1910, 320));
        spikes.add(new RectF(2770, 314, 2840, 340));
        spikes.add(new RectF(3500, 384, 3570, 410));
    }

    @Override public void surfaceCreated(SurfaceHolder h) {
        running = true;
        gameThread = new Thread(this, "PlatformerLoop");
        gameThread.start();
    }

    @Override public void surfaceChanged(SurfaceHolder h, int format, int width, int height) {
        // Keep a fixed 960x540 game world; drawing and touch input scale to the device.
    }

    @Override public void surfaceDestroyed(SurfaceHolder h) {
        running = false;
        if (gameThread != null) {
            try { gameThread.join(); } catch (InterruptedException ignored) { }
        }
    }

    public void pauseGame() { paused = true; }
    public void resumeGame() { paused = false; lastFrame = 0; }

    @Override public void run() {
        while (running) {
            if (!paused && holder.getSurface().isValid()) {
                long now = System.nanoTime();
                float dt = lastFrame == 0 ? 0.016f : Math.min(0.033f, (now - lastFrame) / 1_000_000_000f);
                lastFrame = now;
                update(dt);
                Canvas canvas = null;
                try {
                    canvas = holder.lockCanvas();
                    if (canvas != null) drawGame(canvas);
                } finally {
                    if (canvas != null) holder.unlockCanvasAndPost(canvas);
                }
            } else {
                try { Thread.sleep(30); } catch (InterruptedException ignored) { }
            }
        }
    }

    private void update(float dt) {
        if (gameOver || won) return;
        float oldBottom = playerY + playerH;

        if (left) playerX -= moveSpeed * dt;
        if (right) playerX += moveSpeed * dt;
        if (jumpHeld && onGround) {
            playerVY = jumpSpeed;
            onGround = false;
        }
        playerVY += gravity * dt;
        playerY += playerVY * dt;
        onGround = false;

        RectF player = new RectF(playerX, playerY, playerX + playerW, playerY + playerH);
        for (RectF p : platforms) {
            if (player.right > p.left && player.left < p.right &&
                    oldBottom <= p.top + 8 && player.bottom >= p.top && playerVY >= 0) {
                playerY = p.top - playerH;
                playerVY = 0;
                onGround = true;
                player = new RectF(playerX, playerY, playerX + playerW, playerY + playerH);
            }
        }

        if (playerX < 0) playerX = 0;
        if (playerY > screenH + 250) gameOver = true;

        for (int i = coins.size() - 1; i >= 0; i--) {
            if (RectF.intersects(player, coins.get(i))) {
                coins.remove(i);
                score += 10;
            }
        }
        for (RectF s : spikes) {
            if (RectF.intersects(player, s)) gameOver = true;
        }
        if (playerX > 4250) won = true;
        cameraX = Math.max(0, Math.min(playerX - screenW * 0.32f, 4300 - screenW));
    }

    private void drawGame(Canvas c) {
        float sx = c.getWidth() / screenW;
        float sy = c.getHeight() / screenH;
        c.save();
        c.scale(sx, sy);

        c.drawColor(0xFF87CEEB);
        paint.setColor(0xFFFFE28A);
        c.drawCircle(790, 90, 42, paint);
        paint.setColor(0xFF6DBB73);
        for (int i = 0; i < 9; i++) {
            float x = i * 520 - cameraX * 0.25f;
            c.drawCircle(x + 80, 425, 105, paint);
            c.drawCircle(x + 200, 440, 130, paint);
        }

        c.save();
        c.translate(-cameraX, 0);

        paint.setColor(0xFF795548);
        for (RectF p : platforms) c.drawRoundRect(p, 8, 8, paint);
        paint.setColor(0xFF42A64A);
        for (RectF p : platforms) c.drawRect(p.left, p.top, p.right, p.top + 12, paint);

        paint.setColor(0xFFFFD52A);
        for (RectF coin : coins) c.drawOval(coin, paint);
        paint.setColor(0xFFE53935);
        for (RectF s : spikes) {
            c.drawRect(s, paint);
            paint.setColor(0xFFFF8A65);
            c.drawRect(s.left, s.top, s.right, s.top + 5, paint);
            paint.setColor(0xFFE53935);
        }

        // Player: simple animated-looking adventurer
        paint.setColor(0xFF263238);
        c.drawRoundRect(playerX, playerY + 16, playerX + playerW, playerY + playerH, 8, 8, paint);
        paint.setColor(0xFFFFCC80);
        c.drawCircle(playerX + playerW / 2, playerY + 12, 13, paint);
        paint.setColor(0xFF1565C0);
        c.drawRect(playerX + 7, playerY + 27, playerX + playerW - 7, playerY + 45, paint);
        paint.setColor(0xFF5D4037);
        c.drawRect(playerX + 4, playerY + 46, playerX + 18, playerY + 54, paint);
        c.drawRect(playerX + 24, playerY + 46, playerX + 38, playerY + 54, paint);

        c.restore();

        paint.setColor(0xFF17324D);
        paint.setTextSize(27);
        paint.setFakeBoldText(true);
        c.drawText("POCKET PLATFORMER", 22, 38, paint);
        paint.setTextSize(23);
        c.drawText("Coins: " + score, 22, 70, paint);
        paint.setFakeBoldText(false);

        drawButton(c, 28, screenH - 104, 105, screenH - 24, "◀");
        drawButton(c, 120, screenH - 104, 197, screenH - 24, "▶");
        drawButton(c, screenW - 130, screenH - 112, screenW - 25, screenH - 20, "JUMP");

        if (gameOver || won) {
            paint.setColor(0xAA102030);
            c.drawRect(0, 0, screenW, screenH, paint);
            paint.setColor(0xFFFFFFFF);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(48);
            c.drawText(won ? "LEVEL COMPLETE!" : "GAME OVER", screenW / 2, screenH / 2 - 10, paint);
            paint.setTextSize(23);
            c.drawText("Tap anywhere to play again", screenW / 2, screenH / 2 + 38, paint);
            paint.setTextAlign(Paint.Align.LEFT);
        }
        c.restore();
    }

    private void drawButton(Canvas c, float l, float t, float r, float b, String label) {
        paint.setColor(0xAA17324D);
        c.drawRoundRect(l, t, r, b, 18, 18, paint);
        paint.setColor(0xFFFFFFFF);
        paint.setTextSize(label.equals("JUMP") ? 20 : 42);
        paint.setTextAlign(Paint.Align.CENTER);
        c.drawText(label, (l + r) / 2, (t + b) / 2 + (label.equals("JUMP") ? 7 : 15), paint);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void resetGame() {
        playerX = 100; playerY = 300; playerVY = 0;
        score = 0; gameOver = false; won = false; cameraX = 0;
        buildLevel();
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() * screenW / Math.max(1, getWidth());
        float y = event.getY() * screenH / Math.max(1, getHeight());
        int action = event.getActionMasked();
        if (gameOver || won) {
            if (action == MotionEvent.ACTION_DOWN) resetGame();
            return true;
        }

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN ||
                action == MotionEvent.ACTION_MOVE) {
            left = false; right = false; jumpHeld = false;
            for (int i = 0; i < event.getPointerCount(); i++) {
                float px = event.getX(i) * screenW / Math.max(1, getWidth());
                float py = event.getY(i) * screenH / Math.max(1, getHeight());
                if (py > screenH - 130) {
                    if (px < 115) left = true;
                    else if (px < 220) right = true;
                    else if (px > screenW - 150) jumpHeld = true;
                }
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP ||
                action == MotionEvent.ACTION_CANCEL) {
            left = false; right = false; jumpHeld = false;
        }
        return true;
    }
}

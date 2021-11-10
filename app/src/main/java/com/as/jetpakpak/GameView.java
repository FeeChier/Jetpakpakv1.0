package com.as.jetpakpak;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private Thread thread;
    private boolean isPlaying, isGameOver = false;
    private int screenX, screenY, score ,coinscore= 0;
    public static float screenRatioX, screenRatioY;
    private Paint paint;
    private com.as.jetpakpak.Bird[] birds;
    private com.as.jetpakpak.Coin[] coins;
    private SharedPreferences prefs;
    private Random random;
    private SoundPool soundPool;
    private List<com.as.jetpakpak.Bullet> bullets;
    private int sound;
    private com.as.jetpakpak.Flight flight;
    private com.as.jetpakpak.Bullet bullet;
    private com.as.jetpakpak.GameActivity activity;
    private com.as.jetpakpak.Background background1, background2;


    private Bitmap coin ;
    private int coinX;
    private int coinY;
    private int coinSpeed;

    public GameView(com.as.jetpakpak.GameActivity activity, int screenX, int screenY) {
        super(activity);

        this.activity = activity;

        prefs = activity.getSharedPreferences("game", Context.MODE_PRIVATE);


        coin = BitmapFactory.decodeResource(getResources(), R.drawable.dogecoin);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .build();

        } else
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        sound = soundPool.load(activity, R.raw.shoot, 1);

        this.screenX = screenX;
        this.screenY = screenY;
        screenRatioX = 2400f / screenX;
        screenRatioY = 1080f / screenY;

        background1 = new com.as.jetpakpak.Background(screenX, screenY, getResources());
        background2 = new com.as.jetpakpak.Background(screenX, screenY, getResources());

        flight = new com.as.jetpakpak.Flight(this, screenY, getResources());
        bullet = new com.as.jetpakpak.Bullet(getResources());
        bullets = new ArrayList<>();

        background2.x = screenX;

        paint = new Paint();
        paint.setTextSize(128);
        paint.setColor(Color.WHITE);

        birds = new com.as.jetpakpak.Bird[4];
        coins = new com.as.jetpakpak.Coin[4];

        for (int i = 0;i < 4;i++) {

            com.as.jetpakpak.Bird bird = new com.as.jetpakpak.Bird(getResources());
            birds[i] = bird;

        }

        for (int j = 0; j<4; j++){
            com.as.jetpakpak.Coin coin = new com.as.jetpakpak.Coin(getResources());
            coins[j] = coin;
        }

        random = new Random();

    }

    @Override
    public void run() {

        while (isPlaying) {

            update ();
            try{

                draw ();
            }catch (ConcurrentModificationException e){

            }
            sleep ();

        }

    }

    private void update () {

        background1.x -= 10 * screenRatioX;
        background2.x -= 10 * screenRatioX;

        if (background1.x + background1.background.getWidth() < 0) {
            background1.x = screenX;
        }

        if (background2.x + background2.background.getWidth() < 0) {
            background2.x = screenX;
        }

        if (flight.isGoingUp)
            flight.y -= 13 * screenRatioY;
        else
            flight.y += 13 * screenRatioY;

        if (flight.y < 0)
            flight.y = 0;

        if (flight.y >= screenY - flight.height)
            flight.y = screenY - flight.height;

        List<com.as.jetpakpak.Bullet> trash = new ArrayList<>();

        for (com.as.jetpakpak.Bullet bullet : bullets) {

            if (bullet.y > screenY)
                trash.add(bullet);

            bullet.y += 150 * screenRatioY;

            for (com.as.jetpakpak.Bird bird : birds) {

                if (Rect.intersects(bird.getCollisionShape(),
                        bullet.getCollisionShape())) {

                    score++;
                    bird.x = -500;
                    bullet.y = screenY + 500;
                    bird.wasShot = true;

                }

            }
            for (com.as.jetpakpak.Coin coin : coins){

                if (Rect.intersects(coin.getCollisionShape(),bullet.getCollisionShape())){

                    coinscore++;
                    coin.x = -500;
                    bullet.y = screenY +500;
                    coin.wasCollected = true;
                }

            }

        }

        for (com.as.jetpakpak.Bullet bullet : trash)
            try {
                bullets.remove(bullet);
            }catch (ConcurrentModificationException e){

            }
        for (com.as.jetpakpak.Bird bird : birds) {

            bird.x -= bird.speed;

            if (bird.x + bird.width < 0) {
/*
                if (!bird.wasShot) {
                    isGameOver = true;
                    return;
                }*/

                int bound = (int) (10 * screenRatioX);
                bird.speed = random.nextInt(bound);

                if (bird.speed < 10 * screenRatioX)
                    bird.speed = (int) (10 * screenRatioX);

                bird.x = screenX;
                bird.y = 600;

                bird.wasShot = false;
            }

            if (Rect.intersects(bird.getCollisionShape(), flight.getCollisionShape())) {
                score++;
                //isGameOver = true;
                return;
            }

        }
        for (com.as.jetpakpak.Coin coin : coins){
            coin.x -= coin.speed;

            if (coin.x + coin.width < 0) {

                int bound = (int) (10 * screenRatioX);
                coin.speed = random.nextInt(bound);

                if (coin.speed < 10 * screenRatioX)
                    coin.speed = (int) (10 * screenRatioX);

                coin.x = screenX;
                coin.y = random.nextInt(screenY - coin.height);
                coin.wasCollected = false;
            }
                if (Rect.intersects(coin.getCollisionShape(), flight.getCollisionShape())) {
                    coinscore++;

                    coin.x = -500;
                    bullet.y = screenY +500;
                    coin.wasCollected = true;
                    return;
                }

        }

    }

    private void draw () {

        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();
            canvas.drawBitmap(background1.background, background1.x, background1.y, paint);
            canvas.drawBitmap(background2.background, background2.x, background2.y, paint);

            for (com.as.jetpakpak.Bird bird : birds)
                canvas.drawBitmap(bird.getBird(), bird.x, bird.y, paint);

            for (com.as.jetpakpak.Coin coin : coins)
                canvas.drawBitmap(coin.getCoin(), coin.x, coin.y, paint);
            canvas.drawText(score + "", screenX / 2, 164, paint);

            canvas.drawText(coinscore + "", screenX / 3, 164, paint);

            if (isGameOver) {
                isPlaying = false;
                canvas.drawBitmap(flight.getDead(), flight.x, flight.y, paint);
                getHolder().unlockCanvasAndPost(canvas);
                saveIfHighScore();
                waitBeforeExiting ();
                savecoinScore();
                return;
            }

            canvas.drawBitmap(flight.getFlight(), flight.x, flight.y, paint);

            for (com.as.jetpakpak.Bullet bullet : bullets)
                try{
                    canvas.drawBitmap(bullet.bullet, bullet.x, bullet.y, paint);
                }catch (ConcurrentModificationException e){

                }

            getHolder().unlockCanvasAndPost(canvas);

        }

    }

    private void waitBeforeExiting() {

        try {
            Thread.sleep(3000);
            activity.startActivity(new Intent(activity, com.as.jetpakpak.MainActivity.class));
            activity.finish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void saveIfHighScore() {

        if (prefs.getInt("highscore", 0) < score) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("highscore", score);
            editor.apply();
        }

    }
    private void savecoinScore() {
        SharedPreferences.Editor editorcoin = prefs.edit();
        editorcoin.putInt("Coins", coinscore);
        editorcoin.apply();
    }

    private void sleep () {
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume () {

        isPlaying = true;
        thread = new Thread(this);
        thread.start();

    }

    public void pause () {

        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                flight.isGoingUp = true;
                if (!prefs.getBoolean("isMute", false))
                    soundPool.play(sound, 1, 1, 0, 0, 1);
                flight.toShoot++;

                break;
            case MotionEvent.ACTION_UP:
                flight.isGoingUp = false;
                flight.toShoot=0;
                break;
        }

        return true;
    }
    public void newBullet() {

        com.as.jetpakpak.Bullet bullet = new com.as.jetpakpak.Bullet(getResources());
        bullet.x = flight.x + flight.width;
        bullet.y = flight.y + (flight.height / 2);
        bullets.add(bullet);


    }
}

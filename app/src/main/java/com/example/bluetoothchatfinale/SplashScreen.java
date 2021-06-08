package com.example.bluetoothchatfinale;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {

    private static int SPLASH_SCREEN = 3000;

    Animation topAnime,bottomAnime;
    ImageView image;
    TextView tvAppName,tvName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        //Animations
        topAnime = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomAnime = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);

        image = findViewById(R.id.ivLogo);
        tvAppName = findViewById(R.id.tvAppName);
        tvName = findViewById(R.id.tvName);

        image.setAnimation(topAnime);
        tvName.setAnimation(bottomAnime);
        tvAppName.setAnimation(bottomAnime);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        },3000);


    }
}
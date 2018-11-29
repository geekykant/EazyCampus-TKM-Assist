package com.diyandroid.eazycampus.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.diyandroid.eazycampus.R;
import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

public class SplashLoading extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_loading);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(getApplicationContext(), LoginPage.class);
                startActivity(intent);
                finish();
            }
        }, 600);

        DilatingDotsProgressBar progress = (DilatingDotsProgressBar) findViewById(R.id.progress);
        progress.showNow();

    }
}

package com.mad18.nullpointerexception.takeabook;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mad18.nullpointerexception.takeabook.mainActivity.MainActivity;

public class SplashScreenActivity extends AppCompatActivity {
    int timeout = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
//                Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
//                startActivity(i);
                finish();
            }
        }, timeout);
    }
}

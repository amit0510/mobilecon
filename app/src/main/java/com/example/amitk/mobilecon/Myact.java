package com.example.amitk.mobilecon;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class Myact extends AppCompatActivity {
    private TextView tv;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_myact);
        tv = (TextView) findViewById(R.id.txtname);
        iv = (ImageView) findViewById(R.id.imageView);

        Animation myanim = AnimationUtils.loadAnimation(this, R.anim.mytrasition);
        tv.startAnimation(myanim);
        iv.startAnimation(myanim);

        final Intent intent=new Intent(Myact.this,MainActivity.class);

        Thread timer = new Thread() {
            public void run() {
                {
                    try{
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    finally {
                        startActivity(intent);
                        finish();
                    }
                }
            }
        };
        timer.start();
    }
}
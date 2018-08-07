package com.example.amitk.mobilecon;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity implements ServiceConnection {

    private SinchService.SinchServiceInterface mSinchServiceInterface;
    private SinchServiceVoice.SinchServiceVoiceInterface mSinchServiceVoiceInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationContext().bindService(new Intent(this, SinchService.class), this,
                BIND_AUTO_CREATE);
        getApplicationContext().bindService(new Intent(this, SinchServiceVoice.class), this,
                BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (SinchService.class.getName().equals(componentName.getClassName())) {
            mSinchServiceInterface = (SinchService.SinchServiceInterface) iBinder;
//            onServiceConnected();
        }
        if (SinchServiceVoice.class.getName().equals(componentName.getClassName())) {
            mSinchServiceVoiceInterface = (SinchServiceVoice.SinchServiceVoiceInterface) iBinder;
            onServiceConnected();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        if (SinchService.class.getName().equals(componentName.getClassName())) {
            mSinchServiceInterface = null;
            onServiceDisconnected();
        }
        if (SinchServiceVoice.class.getName().equals(componentName.getClassName())) {
            mSinchServiceVoiceInterface = null;
            onServiceDisconnected();
        }
    }

    protected void onServiceConnected() {
        // for subclasses
    }

    protected void onServiceDisconnected() {
        // for subclasses
    }

    protected SinchService.SinchServiceInterface getSinchServiceInterface() {
        return mSinchServiceInterface;
    }
    protected SinchServiceVoice.SinchServiceVoiceInterface getSinchServiceVoiceInterface() {
        return mSinchServiceVoiceInterface;
    }
}

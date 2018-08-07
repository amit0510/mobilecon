package com.example.amitk.mobilecon;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Timestamp;

public class OnClearFromRecentService extends Service {
    public OnClearFromRecentService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        System.out.println("---------------------------------------------------**END");
        DatabaseReference lstseen= FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().toString()).child("profile");
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        final String currenttime=Long.toString(timeStamp.getTime());
        lstseen.child("lastseen").setValue(currenttime);
        stopSelf();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("--------------------------------------------------**Service Started");
        return START_NOT_STICKY;
    }
}

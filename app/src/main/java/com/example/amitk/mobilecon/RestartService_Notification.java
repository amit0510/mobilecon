package com.example.amitk.mobilecon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RestartService_Notification extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println(RestartService_Notification.class.getSimpleName()+ "Service Stops! Oooooooooooooppppssssss!!!!");
        context.startService(new Intent(context, Notification_Service.class));;
    }
}

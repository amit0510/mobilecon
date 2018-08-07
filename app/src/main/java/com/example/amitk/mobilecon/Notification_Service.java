package com.example.amitk.mobilecon;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.Nullable;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class Notification_Service extends Service {
    public int counter=0;
    public Notification_Service() {
    }
    public Notification_Service(Context context) {
        super();
        System.out.println("---------------------------------HERE"+ ",here I am!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        String token=FirebaseInstanceId.getInstance().getToken();

        final String u= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        FirebaseDatabase.getInstance().getReference().child(u).child("profile").child("token").setValue(token);

        System.out.println("/****************************************************************************** Token" + token);
        startTimer();
        notification();

        return START_STICKY;
    }
    public void notification(){
        final DatabaseReference ds= FirebaseDatabase.getInstance().getReference();
        final String u= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().toString();


        ds.child(u).child("receive").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getChildrenCount()==1) {
                    System.out.println("---------------------------------Msg occure");
                   // Toast.makeText(Notification_Service.this, "Msg occure", Toast.LENGTH_SHORT).show();

                    ds.child(u).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int tot_msg=0;
                            if(dataSnapshot.hasChild("NoOfMsg")){
                                for (DataSnapshot d:dataSnapshot.child("NoOfMsg").getChildren()){
                                    System.out.println("-------------------------------" + d.toString());
                                    tot_msg+=Integer.parseInt(d.getValue().toString());
                                }
                            }


                            int totalMsgCount=(int)dataSnapshot.child("receive").getChildrenCount();
                            int totMsg=Integer.parseInt(dataSnapshot.child("TotalReceiveMsg").getValue().toString());
                            Toast.makeText(Notification_Service.this, "Msg occure : "+(totalMsgCount-totMsg), Toast.LENGTH_SHORT).show();
                            System.out.println("------------------------------- receiver Msg" + (totalMsgCount-totMsg));
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(Notification_Service.this);

                            //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.androidauthority.com/"));
                            Intent intent=new Intent(Notification_Service.this,MainActivity.class);
                            PendingIntent pendingIntent = PendingIntent.getActivity(Notification_Service.this, 0, intent, 0);

                            Bitmap img = BitmapFactory.decodeResource(null, R.drawable.app_ic);
                            mBuilder.setSmallIcon(R.drawable.app_ic)
                                    .setContentTitle("My Messanger")
                                    .setContentText("You have " + (totalMsgCount-tot_msg) +" Unreaded messages")
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setSmallIcon(R.drawable.icon)
                                    .setLargeIcon(img)
                                    .setBadgeIconType(R.drawable.app_ic);



                            Uri sound= Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.slow_spring_board);
                            mBuilder.setSound(sound);

                            mBuilder.setSmallIcon(R.drawable.app_ic);
                            mBuilder.setContentIntent(pendingIntent);

                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(Notification_Service.this);

// notificationId is a unique int for each notification that you must define
                            notificationManager.notify(101, mBuilder.build());
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                FirebaseUser fuser=FirebaseAuth.getInstance().getCurrentUser();
                fuser.getIdToken(true).toString();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("----------------------------------EXIT"+ "ondestroy!");
        Intent broadcastIntent = new Intent(".RestartService_Notification");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;
    public void startTimer() {

        timer = new Timer();

        initializeTimerTask();

        timer.schedule(timerTask, 1000, 1000); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                System.out.println("-----------------------------in timer"+ "in timer ++++  "+ (counter++));
            }
        };
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
}

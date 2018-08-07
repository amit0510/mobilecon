package com.example.amitk.mobilecon;

import com.sinch.android.rtc.MissingPermissionException;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ng.max.slideview.SlideView;

public class IncomingCallScreenVoice extends BaseActivity {

    static final String TAG = IncomingCallScreenVoice.class.getSimpleName();
    private String mCallId;
    private AudioPlayer mAudioPlayer;
    ImageView profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.incoming_voice);
            profile=(ImageView)findViewById(R.id.userProfilePic) ;

        SlideView slideView = (SlideView) findViewById(R.id.accept);
        slideView.setOnSlideCompleteListener(new SlideView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(SlideView slideView) {
                Toast.makeText(IncomingCallScreenVoice.this,"Accept",Toast.LENGTH_SHORT).show();
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                answerClicked();
            }
        });
        SlideView slideView2 = (SlideView) findViewById(R.id.reject);
        slideView2.setOnSlideCompleteListener(new SlideView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(SlideView slideView) {
                Toast.makeText(IncomingCallScreenVoice.this,"Reject",Toast.LENGTH_SHORT).show();
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                declineClicked();
            }
        });

        mAudioPlayer = new AudioPlayer(this);
        mAudioPlayer.playRingtone();
        mCallId = getIntent().getStringExtra(SinchServiceVoice.CALL_ID);
    }
    public void retriveImage(String x){
        String fileName = x + ".jpeg";
        String completePath = Environment.getExternalStorageDirectory() + "/My Messenger/" + fileName;
        File file = new File(completePath);
        Uri imageUri = Uri.fromFile(file);

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            profile.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onServiceConnected() {
        Call call = getSinchServiceVoiceInterface().getCall(mCallId);
        if (call != null) {
            call.addCallListener(new SinchCallListener());
            TextView remoteUser = (TextView) findViewById(R.id.remoteUser);

            GlobalVar globalVar=(GlobalVar)getApplicationContext();
            ArrayList<String> names=globalVar.getName();
            ArrayList<String> numbers=globalVar.getNumbers();

            String[] id=call.getRemoteUserId().toString().split("v");

            int i=numbers.indexOf(id[0]);
            System.out.println("---------------------------------------------------- " + i + call.getRemoteUserId().toString() + numbers.size() + names.size());
            String n="";
            if(i!=-1)
                n=names.get(i);
            else
                n=id[0];

            remoteUser.setText(n);
            retriveImage(n);
        } else {
            Log.e(TAG, "Started with invalid callId, aborting");
            finish();
        }
    }

    private void answerClicked() {
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceVoiceInterface().getCall(mCallId);
        if (call != null) {
            try {
                call.answer();
                Intent intent = new Intent(this, CallScreenVoice.class);
                intent.putExtra(SinchServiceVoice.CALL_ID, mCallId);
                startActivity(intent);
                finish();
            } catch (MissingPermissionException e) {
                ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
            }
        } else {
            finish();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You may now answer the call", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "This application needs permission to use your microphone to function properly.", Toast
                    .LENGTH_LONG).show();
        }
    }

    private void declineClicked() {
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceVoiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended, cause: " + cause.toString());
            mAudioPlayer.stopRingtone();
            finish();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // Send a push through your push provider here, e.g. GCM
        }

    }

//    private View.OnClickListener mClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            switch (v.getId()) {
//                case R.id.answerButton:
//                    answerClicked();
//                    break;
//                case R.id.declineButton:
//                    declineClicked();
//                    break;
//            }
//        }
//    };
}

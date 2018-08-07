package com.example.amitk.mobilecon;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ChangedPackages;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import java.util.concurrent.TimeUnit;
import static android.R.attr.phoneNumber;


public class MainActivity extends AppCompatActivity {
    ImageView logo;
    SharedPreferences sharedPreferences;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    FirebaseAuth mAuth;
    EditText txtmob;
    TextView detail,change;
    PinEntryEditText txtverify;
    RelativeLayout sendlayout,verifylayout;
    Button send,verify;
    TextView resend;

    private FirebaseAuth.AuthStateListener mAuthListener;
    String mverificationId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        detail=(TextView)findViewById(R.id.stext1);
        change=(TextView)findViewById(R.id.change);
        sendlayout=(RelativeLayout) findViewById(R.id.layoutSend);
        verifylayout=(RelativeLayout) findViewById(R.id.layoutVerify);
        logo=(ImageView)findViewById(R.id.logoimg);

        sendlayout.setVisibility(View.VISIBLE);
        verifylayout.setVisibility(View.INVISIBLE);

        send=(Button)findViewById(R.id.btnsend);
        verify=(Button)findViewById(R.id.btnverify);
        resend=(TextView) findViewById(R.id.btnresend);

        txtmob=(EditText)findViewById(R.id.phoneno);
        txtverify=(PinEntryEditText)findViewById(R.id.verify);

        sharedPreferences=getSharedPreferences("login", Context.MODE_PRIVATE);

        mAuth=FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                   txtmob.setError("Invalid Phone Number");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                }
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                mverificationId=verificationId;
                mResendToken=token;
            }
        };

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendlayout.setVisibility(View.VISIBLE);
                verifylayout.setVisibility(View.INVISIBLE);
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pn=txtmob.getText().toString().trim();
                if (pn.length()!=10) {
                    txtmob.setError("Invalid Phone Number");
                    return;
                }else {
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(pn, 60, TimeUnit.SECONDS, MainActivity.this, mCallbacks);
                    verifylayout.setVisibility(View.VISIBLE);
                    sendlayout.setVisibility(View.INVISIBLE);
                    detail.setText("OTP has been send to you on\n" +
                            "mobile number +91" + txtmob.getText().toString().trim() + ". Plase enter it below.");
                    change.setText("+91" + txtmob.getText().toString().trim() + " its not your Number?");
                }
            }
        });

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = txtverify.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    txtverify.setError("Cannot be empty.");
                    return;
                }else if(code.length()!=6){
                    txtverify.setError("Number Must be 6 digit long");
                }else{
                    PhoneAuthCredential credential=PhoneAuthProvider.getCredential(mverificationId,code);
                    signInWithPhoneAuthCredential(credential);
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putString("phone",txtmob.getText().toString().trim());
                    editor.commit();
                }
            }
        });

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendVerificationCode(txtmob.getText().toString(), mResendToken);
                Toast.makeText(getApplicationContext(),"OTP send on your Number",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private boolean validatePhoneNumber() {
        String phoneNumber = txtmob.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            txtmob.setError("Invalid phone number.");
            return false;
        }
        return true;
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),"Send",Toast.LENGTH_SHORT).show();
                            FirebaseUser user = task.getResult().getUser();
                            startActivity(new Intent(MainActivity.this, ProfileUpload.class));
                            finish();

                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(),"Fail",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
    @Override
    protected void onStart() {
        super.onStart();
        contactPermission();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null) {
            startActivity(new Intent(MainActivity.this, ListUser.class));
            finish();
        }

    }
    public void contactPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                            android.Manifest.permission.INTERNET,
                            android.Manifest.permission.WRITE_CONTACTS,
                            android.Manifest.permission.READ_CONTACTS,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.RECORD_AUDIO,
                            android.Manifest.permission.READ_PHONE_STATE,
                            android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
                            android.Manifest.permission.ACCESS_NETWORK_STATE,
                            android.Manifest.permission.CAMERA,
                            Manifest.permission.VIBRATE
                    },
                    5000);

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {

        switch (requestCode) {
            case 5000:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this,"Permission Rejected",Toast.LENGTH_SHORT).show();
                }
                return;
        }

    }
}

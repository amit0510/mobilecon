package com.example.amitk.mobilecon;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.Contacts;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
public class FriendsProfile extends AppCompatActivity {
    String receiver;
    ImageView dp;
    StorageReference storageReference;
    FirebaseAuth mAuth;
    TextView username,bio,number;
    String o;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_profile);

        Intent i=getIntent();
        receiver=i.getStringExtra("receiver");
        o=i.getStringExtra("orirec");
        getSupportActionBar().setTitle(o);
        Toast.makeText(getApplicationContext(),receiver,Toast.LENGTH_SHORT).show();

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setHomeButtonEnabled(true);

        dp=(ImageView)findViewById(R.id.dp);
        username=(TextView)findViewById(R.id.Username);
        bio=(TextView)findViewById(R.id.bio);
        number=(TextView)findViewById(R.id.number);

        storageReference= FirebaseStorage.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        number.setText(receiver);

        retriveImage();
        loadTextData();

    }
    public void retriveImage(){
        String fileName = receiver + ".jpeg";
        String completePath = Environment.getExternalStorageDirectory() + "/My Messenger/" + fileName;
        File file = new File(completePath);
        Uri imageUri = Uri.fromFile(file);

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            dp.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void loadTextData(){

        DatabaseReference dbRef= FirebaseDatabase.getInstance().getReference().child(receiver).child("profile");
        ValueEventListener valueEventListener=new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("username")) {
                    String i = dataSnapshot.child("username").getValue().toString();
                    if (!i.trim().equals(null)) {
                        username.setText(i);

                    }
                }
                if(dataSnapshot.hasChild("bio")) {

                    String i = dataSnapshot.child("bio").getValue().toString();
                    if (!i.trim().equals(null)) {
                        bio.setText(i);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        dbRef.addValueEventListener(valueEventListener);
    }
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

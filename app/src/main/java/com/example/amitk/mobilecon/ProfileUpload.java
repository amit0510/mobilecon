  package com.example.amitk.mobilecon;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import static com.example.amitk.mobilecon.R.id.btnEditUsername;
import static com.example.amitk.mobilecon.R.id.imageView;

  public class ProfileUpload extends AppCompatActivity implements View.OnClickListener {
    private static final int pic_image_req=234;
      private Button upload;
      private ImageView img,choose;
      private Uri filePath;
      FirebaseAuth mAuth;
      FirebaseUser mCurrentUser;
      DatabaseReference mDatabaseUser,mDatabase;
      StorageReference  storageReference;
      RelativeLayout myLayout;
      ImageView edituserName,editBio;
      TextView tvUsername,tvBio;
      EditText etUsername;
      String e="";
      @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_upload);

          storageReference= FirebaseStorage.getInstance().getReference();
          mAuth=FirebaseAuth.getInstance();
          e=mAuth.getCurrentUser().getPhoneNumber().toString();
          mDatabase= FirebaseDatabase.getInstance().getReference().child(e).child("profile");
          getSupportActionBar().setTitle("Profile");

          upload=(Button)findViewById(R.id.buttonUpload);
          img=(ImageView)findViewById(imageView);
          choose=(ImageView)findViewById(R.id.choose);

          upload.setOnClickListener(this);
          choose.setOnClickListener(this);

          retriveImage();
          tvUsername=(TextView)findViewById(R.id.tvUser);
          etUsername=(EditText)findViewById(R.id.etUser);

          edituserName=(ImageView)findViewById(R.id.btnEditUsername);
          editBio=(ImageView)findViewById(R.id.btnEditBio);
          tvBio=(TextView)findViewById(R.id.Bio);
          editBio.setOnClickListener(this);
          edituserName.setOnClickListener(this);

          myLayout=(RelativeLayout)findViewById(R.id.layoutBack);
          //storageReference.child("profilePic/"+ FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().toString()+".jpg");
    }
      protected void onStart(){
          super.onStart();
          mAuth=FirebaseAuth.getInstance();
          final String uname="";
          final String bio="";
          e=mAuth.getCurrentUser().getPhoneNumber().toString();
          DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference().child(e).child("profile");
          ValueEventListener valueEventListener=new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("username")) {
                    String i = dataSnapshot.child("username").getValue().toString();
                    if (!i.trim().equals(null)) {
                        tvUsername.setText(i);
                        etUsername.setText(i);
                    }
                }
                if(dataSnapshot.hasChild("bio")) {

                    String i = dataSnapshot.child("bio").getValue().toString();
                    if (!i.trim().equals(null)) {
                        tvBio.setText(i);
                    }
                }
              }
              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
          };
          dbRef.addValueEventListener(valueEventListener);
          //System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++" + uname);
      }

      @Override
      public void onClick(View view) {
        if(view==choose){
            showFileChooser();
            //addProfile();
            upload.setText("Change");
        }
        else if(view==upload){
            if(upload.getText().equals("Done")){
                addProfile();
                startActivity(new Intent(ProfileUpload.this,ListUser.class));
                finish();
            }
            else {
                addProfile();
                uploadProfile();
            }
        }
        else if(view==edituserName){
            tvUsername.setVisibility(View.INVISIBLE);
            etUsername.setVisibility(View.VISIBLE);
            etUsername.setFocusable(true);
        }
        else if(view==editBio){
            final Dialog dialog = new Dialog(ProfileUpload.this);
            // Include dialog.xml file
            dialog.setContentView(R.layout.customdialog_bio);
            // Set dialog title
            dialog.setTitle("Edit Bio");

            // set values for custom dialog components - text, image and button
            final EditText text = (EditText) dialog.findViewById(R.id.et);
            text.setText(tvBio.getText().toString());

            dialog.show();
            Button declineButton = (Button) dialog.findViewById(R.id.btnOk);
            // if decline button is clicked, close the custom dialog
            declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close dialog
                    tvBio.setText(text.getText().toString());
                    dialog.dismiss();
                }
            });
        }
      }

      private void showFileChooser() {
          Intent intent = new Intent();
          intent.setType("image/*");
          intent.setAction(Intent.ACTION_GET_CONTENT);
          startActivityForResult(Intent.createChooser(intent, "Select Picture"), pic_image_req);
      }

      @Override
      protected void onActivityResult(int requestCode, int resultCode, Intent data) {
          super.onActivityResult(requestCode, resultCode, data);
          if (requestCode == pic_image_req && resultCode == RESULT_OK && data != null && data.getData() != null) {
              filePath = data.getData();
              try {
                  Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                  img.setImageBitmap(bitmap);
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
      }

      //*********************************************************************************************************
      private void uploadProfile(){
          if(filePath!=null){
              final ProgressDialog progressDialog=new ProgressDialog(this);
              progressDialog.setTitle("Profile Upldationg...");
              progressDialog.show();

              StorageReference stRef=storageReference.child("profilePic/"+ FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().toString()+".jpg");
              stRef.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                  @Override
                  public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                      progressDialog.dismiss();
                      Toast.makeText(getApplicationContext(),"Profile picture updated...",Toast.LENGTH_LONG).show();
                      startActivity(new Intent(ProfileUpload.this,ListUser.class));
                      mDatabase.child("url").setValue(taskSnapshot.getDownloadUrl().toString());
                      //System.out.println("-------------------------------------------------------"+taskSnapshot.getDownloadUrl().toString());
                      finish();
                  }
              }).addOnFailureListener(new OnFailureListener() {
                  @Override
                  public void onFailure(@NonNull Exception e) {
                      progressDialog.dismiss();
                      Toast.makeText(getApplicationContext(),"File Uploaded Failed",Toast.LENGTH_LONG).show();
                  }
              }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                  @Override
                  public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                      double progress=(100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                      progressDialog.setMessage("Upload : " + ((int)progress)+"%...");
                  }
              });
          }
      }
      public void retriveImage(){
//
          String fileName = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().toString() + ".jpeg";
          String completePath = Environment.getExternalStorageDirectory() + "/My Messenger/" + fileName;
          File file = new File(completePath);
          Uri imageUri = Uri.fromFile(file);

          try {
              Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
              img.setImageBitmap(bitmap);
          } catch (IOException e) {
              e.printStackTrace();
          }
          //***************************************************************************************************************

      }
      public void addProfile(){
            String uname=etUsername.getText().toString().trim();
            mDatabase.child("username").setValue(uname);
            String bio=tvBio.getText().toString().trim();
            mDatabase.child("bio").setValue(bio);
      }

  }

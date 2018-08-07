package com.example.amitk.mobilecon;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.CamcorderProfile;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;

public class GroupProfile extends AppCompatActivity implements View.OnClickListener {
    ArrayList<String> number=new ArrayList<>();
    EditText gname;
    String groupname,user;
    ImageView choose,img;
    private Uri filePath;
    StorageReference  storageReference;
    DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);
        Intent i= getIntent();
        number=i.getStringArrayListExtra("nos");
        Toast.makeText(getApplicationContext(),number.toString(),Toast.LENGTH_SHORT).show();

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Group Create");

        user=FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().toString();
        storageReference= FirebaseStorage.getInstance().getReference();
        mDatabase=FirebaseDatabase.getInstance().getReference();

        choose=(ImageView)findViewById(R.id.chooser);
        img=(ImageView)findViewById(R.id.groupdp);

        gname=(EditText)findViewById(R.id.groupname);
        groupname=gname.getText().toString();

        choose.setOnClickListener(this);

        //addGroup();
    }

    private void addGroup() {

        if(!groupname.equals(null)){
            String jj="";
            for(int i=0;i<number.size();i++){
                jj+=number.get(i)+" ";
            }
            for(int i=0;i<number.size();i++){
                DatabaseReference g= FirebaseDatabase.getInstance().getReference().child(number.get(i)).child("group").child(gname.getText().toString());
                final DatabaseReference newpost=g.child("receive").push();
                g.child("member").setValue(jj);
                g.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //newpost.setValue("You are added in Group");
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }else{
            Toast.makeText(getApplicationContext(),"First Enter Group Name",Toast.LENGTH_SHORT).show();
        }

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.donegroup, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.done){
            addGroup();
            //Toast.makeText(getApplicationContext(),"Done",Toast.LENGTH_SHORT).show();
            uploadProfile();
            startActivity(new Intent(GroupProfile.this,ListUser.class));
            finish();
        }else if(item.getItemId()==android.R.id.home)
        {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if(view==choose){
            showFileChooser();
            Toast.makeText(getApplicationContext(),"choose",Toast.LENGTH_SHORT).show();
        }
    }
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 20001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 20001 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                img.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadProfile(){
        if(filePath!=null){
//            final ProgressDialog progressDialog=new ProgressDialog(GroupProfile.this);
//            progressDialog.setTitle("Profile Upldationg...");
//            progressDialog.show();

            StorageReference stRef=storageReference.child("profilePic/"+gname.getText().toString()+" amit "+ FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().toString()+".jpg");
            stRef.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(),"Profile picture updated...",Toast.LENGTH_LONG).show();
                    for(int i=0;i<number.size();i++){
                        mDatabase.child(number.get(i)).child("group").child(gname.getText().toString()).child("profile").setValue(taskSnapshot.getDownloadUrl().toString());
                    }

                    System.out.println("-------------------------------------------------------"+taskSnapshot.getDownloadUrl().toString());
                    startActivity(new Intent(GroupProfile.this,ListUser.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
//                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(),"File Uploaded Failed",Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress=(100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
//                    progressDialog.setMessage("Upload : " + ((int)progress)+"%...");
                }
            });
        }
    }

}

package com.example.amitk.mobilecon;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ShowableListMenu;
import android.support.v7.widget.ForwardingListener;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.PropertyName;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class watch_story extends AppCompatActivity implements StoriesProgressView.StoriesListener{
    StoriesProgressView storiesProgressView;
    ImageView profileImage;
    ArrayList<Bitmap> imageBitmap=new ArrayList<>();
    String user;
    DatabaseReference mDatabaseReference;
    public ArrayList<StoryModel> fileData=new ArrayList<>();
    int index=0;
    ArrayList<String> listFiles=new ArrayList<>();
    RelativeLayout left,right;
    VideoView profileVideo;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_story);

        Intent i=getIntent();
        user=i.getStringExtra("user");

        File file = new File(Environment.getExternalStorageDirectory() + "/My Messenger/Story/");
        String[] listOfFiles =file.list();
        //String[] listOfFiles = getApplication().getExternalCacheDir().list();
        if(listOfFiles!=null)
            listFiles=new ArrayList<>(Arrays.asList(listOfFiles));

        mDatabaseReference=FirebaseDatabase.getInstance().getReference().child("story").child(user);
        retriveDataFromDatabase();

        storiesProgressView=(StoriesProgressView)findViewById(R.id.stories);
        profileImage=(ImageView)findViewById(R.id.storyimage);
        profileVideo=(VideoView)findViewById(R.id.storyVideo);
        progressBar=(ProgressBar)findViewById(R.id.progress);

        profileImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        right=(RelativeLayout)findViewById(R.id.rightButton);
        left=(RelativeLayout)findViewById(R.id.leftButton);
        progressBar.setVisibility(View.INVISIBLE);

        profileImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    storiesProgressView.pause();
                } else if (motionEvent.getAction() == android.view.MotionEvent.ACTION_UP) {
                    storiesProgressView.resume();
                }
                return true;
            }
        });


        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.reverse();
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.skip();
            }
        });
    }

    private void setStoryData(int cnt) {
        try {
            storiesProgressView.setStoriesCount(cnt);
            storiesProgressView.setStoryDuration(3000);
            storiesProgressView.setStoriesListener(this);
            storiesProgressView.startStories();
        }catch (Exception e){
            finish();
        }
    }


    @Override
    public void onNext() {
        index++;
        xyz();
        //retriveImageLocal(fileData.get(index).filenamewithNumber.toString());
    }

    @Override
    public void onPrev() {
        index--;
        xyz();
        //retriveImageLocal(fileData.get(index).filenamewithNumber.toString());
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        // Very important !
        storiesProgressView.destroy();
        super.onDestroy();
    }

    private void retriveImageLocal(String filename,String ex) {
        File file = new File(Environment.getExternalStorageDirectory() + "/My Messenger/Story/",filename + "." + ex);
        Uri imageUri = Uri.fromFile(file);
        if(ex.equals("jpeg") ||  ex.equals("jpg") || ex.equals("png") || ex.equals("gif")){
            storiesProgressView.setStoryDuration(3000);
            profileImage.setVisibility(View.VISIBLE);
            profileVideo.setVisibility(View.INVISIBLE);
            //Toast.makeText(this,"image",Toast.LENGTH_SHORT).show();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(ex.equals("mp4")){
            profileImage.setVisibility(View.INVISIBLE);
            profileVideo.setVisibility(View.VISIBLE);
            profileVideo.setVideoPath(imageUri.getPath());
            profileVideo.requestFocus();
            profileVideo.start();

            MediaPlayer mp = MediaPlayer.create(this,imageUri);
            int duration=mp.getDuration();
            storiesProgressView.setStoryDuration(duration);
            //long duration = profileVideo.getDuration();
            Toast.makeText(watch_story.this,"video : " + duration,Toast.LENGTH_SHORT).show();


        }

    }
    public void retriveDataFromDatabase(){
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot dt:dataSnapshot.getChildren()){
                    fileData.add(new StoryModel(dt.child("ctime").getValue().toString(),
                            dt.child("url").getValue().toString(),
                            dt.child("filename").getValue().toString(),
                                    dt.child("extension").getValue().toString()));
                }
                //Toast.makeText(watch_story.this,""+fileData.size(),Toast.LENGTH_LONG).show();
                xyz(fileData);
                setStoryData(fileData.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void xyz(ArrayList<StoryModel> fileData){

        //System.out.println("-----------------------------------" + listFiles.get(0));
        try{
            if(!listFiles.contains(fileData.get(index).filenamewithNumber.toString()+"."+fileData.get(index).extension.toString())) {
                //Toast.makeText(watch_story.this,"Download",Toast.LENGTH_SHORT).show();
                //
                //progressBar.setVisibility(View.VISIBLE);
                downloadImage(fileData.get(index).getFilename().toString(), fileData.get(index).filenamewithNumber.toString(),fileData.get(index).extension.toString());
            }

            retriveImageLocal(fileData.get(index).filenamewithNumber.toString(),fileData.get(index).extension.toString());
        }catch (IndexOutOfBoundsException e){
            Toast.makeText(this,"You have no Recent Story",Toast.LENGTH_SHORT).show();
            finish();
        }


    }
    public void xyz(){
        try {
            if (!listFiles.contains(fileData.get(index).filenamewithNumber.toString() + "." + fileData.get(index).extension.toString())) {
                Toast.makeText(watch_story.this, "Download", Toast.LENGTH_SHORT).show();
                // progressBar.setVisibility(View.VISIBLE);
                downloadImage(fileData.get(index).getFilename().toString(), fileData.get(index).filenamewithNumber.toString(), fileData.get(index).extension.toString());
            }

            retriveImageLocal(fileData.get(index).filenamewithNumber.toString(), fileData.get(index).extension.toString());
        }catch (Exception e){
            Toast.makeText(this,"Previous story not available",Toast.LENGTH_SHORT).show();
            index++;
            //finish();
        }

    }

    private void downloadImage(String u,String filename,String extension) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        progressBar.setVisibility( View.VISIBLE);
        URL url=null;
        try {
            url=new URL(u);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        int count;
        try {
            URLConnection conexion=url.openConnection();
            conexion.connect();
            int file_size = conexion.getContentLength();
            System.out.println("/*/*//**////*/**//*/**/--------------" + file_size);
            AlertDialog.Builder builder= new AlertDialog.Builder(getApplication());
            builder.setMessage("This Video "+((file_size/1024)/1024) + "MB long Do you want to continue Download").setTitle("Warning");
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                         System.out.println("---------------------------------------------------------" + "yes");
                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    System.out.println("---------------------------------------------------------" + "no");
                }
            });
            AlertDialog dialog=builder.create();

            //Toast.makeText(this,file_size+"",Toast.LENGTH_SHORT).show();
            String targetfilename= "/"+filename + "." + extension;
            File file=new File(Environment.getExternalStorageDirectory() + "/My Messenger/Story/");
            if (!file.exists()) {
                file.mkdir();
            }
            InputStream input=new BufferedInputStream(url.openStream());
            OutputStream output=new FileOutputStream(file+targetfilename);
            byte data[]=new byte[1024];
            long total=0;
            while((count=input.read(data))!=-1){
                total+=count;
                output.write(data,0,count);
            }
            output.flush();
            output.close();
            input.close();
            progressBar.setVisibility(View.INVISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

package com.example.amitk.mobilecon;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Story_List extends AppCompatActivity implements View.OnClickListener {
    public String user;
    ImageView dp,img;
    String imageBitmap="";
    File file;
    Uri fileUri;
    StorageReference storageReference;
    String timeStand="";
    String timeStandWithNumber="";
    ListView storyList;
    ArrayList<String> listUser=new ArrayList<>();
    ArrayList<String> name=new ArrayList<>();
    ArrayList<String> numbers=new ArrayList<>();
    String FileExtension="";
    String timeStandImage="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story__list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Story");
        setSupportActionBar(toolbar);

        storyList=(ListView)findViewById(R.id.storyList);
        dp=(ImageView)findViewById(R.id.story_dp);
        user= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().toString().replace(" ","");

        //img=(ImageView)findViewById(R.id.storyimage);


        final GlobalVar globalVar=(GlobalVar)getApplicationContext();
        name=globalVar.getName();
        numbers=globalVar.getNumbers();

        dp.setOnClickListener(this);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        listUser();
        storageReference= FirebaseStorage.getInstance().getReference();

        setprofilepic();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });


        FloatingActionButton gallery=(FloatingActionButton)findViewById(R.id.gallary);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/* video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                timeStand=String.valueOf(System.currentTimeMillis());
                timeStandWithNumber=user+"_"+timeStand;
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 6565);
            }
        });
    }

    private void setprofilepic() {
        String fileName = user + ".jpeg";
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
    private void openCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        timeStand=String.valueOf(System.currentTimeMillis());
        timeStandWithNumber=user+"_"+timeStand;
        file = new File(Environment.getExternalStorageDirectory() + "/My Messenger/Story",timeStandWithNumber + ".jpg");

        //Toast.makeText(this,currenttime+"\n" + String.valueOf(System.currentTimeMillis()),Toast.LENGTH_LONG).show();

        fileUri = Uri.fromFile(file);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        startActivityForResult(cameraIntent, 5555);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Uri filePath=null;
        if (requestCode == 5555 && resultCode==-1) {
            uploadProfile(fileUri);
           //Toast.makeText(this,fileUri.toString() + "--" + file.getAbsolutePath() ,Toast.LENGTH_LONG).show();
        }
        else if (requestCode == 6565 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            FileExtension=getMimeType(getApplicationContext(),filePath);
            if (filePath.toString().contains("image")) {

                FileExtension = getMimeType(getApplicationContext(), filePath);
                InputStream inputStream = null;
                OutputStream outputStream = null;

                try
                {
                    Context context=getApplicationContext();
                    ContentResolver content = context.getContentResolver();
                    inputStream = content.openInputStream(filePath);

                    File saveDirectory = new File(Environment.getExternalStorageDirectory() + "/My Messenger/Story");

                    saveDirectory.mkdirs();
                    timeStandImage=String.valueOf(System.currentTimeMillis());

                    if(FileExtension.equals("jpeg") || FileExtension.equals("png"))
                        FileExtension="jpg";

                    outputStream = new FileOutputStream( saveDirectory +"/"+ timeStandWithNumber +"."+FileExtension); // filename.png, .mp3, .mp4 ...

                    byte[] buffer = new byte[1000];
                    int bytesRead = 0;
                    while ( ( bytesRead = inputStream.read( buffer, 0, buffer.length ) ) >= 0 )
                    {
                        outputStream.write( buffer, 0, buffer.length );
                    }
                } catch ( Exception e ){
                    //Log.e( TAG, "Exception occurred " + e.getMessage());
                } finally{

                }

                File file = new File(Environment.getExternalStorageDirectory() + "/My Messenger/Story/",timeStandWithNumber + "."+FileExtension);
                Uri fileUri=Uri.fromFile(file);

                uploadProfile(fileUri);
//                try {
//                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
//                    uploadProfile(filePath);
//                   // Toast.makeText(getApplicationContext(), FileExtension, Toast.LENGTH_SHORT).show();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

            } else  if (filePath.toString().contains("video")) {
                MediaPlayer mp = MediaPlayer.create(this,filePath);
                int duration=mp.getDuration();
                if(duration>30000)
                    Toast.makeText(getApplicationContext(), "File length must be less than 30 second.", Toast.LENGTH_LONG).show();
                else
                    uploadProfile(filePath);
            }
        }
    }
    public static String getMimeType(Context context, Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
    }
   private void addDatabase(final String bitmap) {
        DatabaseReference ds= FirebaseDatabase.getInstance().getReference().child("story").child(user);
        final DatabaseReference newPost=ds.push();
        ds.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                newPost.child("ctime").setValue(timeStand);
                newPost.child("url").setValue(bitmap);
                newPost.child("filename").setValue(timeStandWithNumber);
                newPost.child("extension").setValue(FileExtension);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void uploadProfile(Uri filePath){
        if(filePath!=null){
            String filePathdec="";
            String[] filename;
            int file_size=0;
            String filetype="";

            if (FileExtension.equals("jpg") || FileExtension.equals("jpeg") || FileExtension.equals("png")) {
                file = new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile", "Compressed");
                filePathdec = SiliCompressor.with(getApplicationContext()).compress(filePath.toString(), file);
                filename = filePathdec.split("/");
                file = new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile/Compressed", filename[filename.length - 1]);
                file_size = Integer.parseInt(String.valueOf(file.length() / 1024));
                fileUri = Uri.fromFile(file);
                filetype="image";
            }


            final ProgressDialog progressDialog=new ProgressDialog(this);
            progressDialog.setTitle("Profile Upldationg...");
            progressDialog.show();

            StorageReference stRef=storageReference.child("Story/"+ timeStandWithNumber+"." + FileExtension);
            stRef.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(),"Story updated...",Toast.LENGTH_LONG).show();
                    addDatabase(taskSnapshot.getDownloadUrl().toString());

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

    @Override
    public void onClick(View view) {
        if(view==dp){
            Intent i=new Intent(Story_List.this,watch_story.class);
            i.putExtra("user",user);
            startActivity(i);
        }
    }
    public void listUser(){
        DatabaseReference db=FirebaseDatabase.getInstance().getReference().child("story");
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String[] timeStand=new String[(int)dataSnapshot.getChildrenCount()-1];
                //System.out.println("-------------------------------------------------" + dataSnapshot.toString());
                int i=-1;
                for(DataSnapshot dt:dataSnapshot.getChildren()){

                    if(!dt.getKey().toString().replace(" ", "").equals(user)){
                        i++;
                        for(DataSnapshot d:dt.getChildren()){
                            timeStand[i]=d.child("ctime").getValue().toString();
                        }
                    }
                    //System.out.println("-------------------------------------------------" + timeStand[i]);
                    listUser.add(dt.getKey().toString().replace(" ", ""));
                }
                listUser.remove(user);
                customAdapterCall(listUser,timeStand);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void customAdapterCall(ArrayList<String> listUser1,String[] timeStand) {
        String u[]=new String[listUser1.size()];


        GlobalVar globalVar=(GlobalVar)getApplicationContext();
        ArrayList<String> names=globalVar.getName();
        ArrayList<String> numbers=globalVar.getNumbers();

        try{
            for(int i=0;i<listUser1.size();i++){
                System.out.println("//////////////////*****************" + listUser1.get(i));
                if(numbers.contains(listUser1.get(i)))
                    u[i]=names.get(numbers.indexOf(listUser1.get(i)));
                else
                    u[i]=listUser1.get(i);
            }

        }catch (Exception e){
            System.out.println("//////////////////****************-----------*" +u.length);
        }

        String[] img=new String[listUser1.size()];
        for(int j=0;j<listUser1.size();j++){
            img[j]="https://firebasestorage.googleapis.com/v0/b/mobilecon-f06a9.appspot.com/o/profilePic%2Fic_user.png?alt=media&token=c41ec1ec-9166-4d43-a12d-38f78e18860d";
        }

        String nos[]=new String[listUser1.size()];
        for(int j=0;j<listUser1.size();j++){
            if(name.contains(listUser1.get(j)))
                nos[j]=numbers.get(name.indexOf(listUser.get(j)));
            else
                nos[j]=listUser1.get(j);
        }
        storyList.setAdapter(new CustomAdapterStory(u,img,getApplicationContext(),nos,getContentResolver(),timeStand));
    }

}

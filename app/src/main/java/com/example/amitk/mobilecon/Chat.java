package com.example.amitk.mobilecon;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;
import com.mp4parser.iso14496.part15.HevcDecoderConfigurationRecord;
import com.sinch.android.rtc.MissingPermissionException;
import com.sinch.android.rtc.calling.Call;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.example.amitk.mobilecon.Story_List.getMimeType;

public class Chat extends BaseActivity {
    final List<CharModel> lstChat = new ArrayList<CharModel>();
    ListView listView;
    DatabaseReference q1, q2;
    String receiver = "";
    ArrayList<String> numbers;
    ArrayList<String> name;
    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;
    ArrayList<String> contentRec = new ArrayList<>();
    Toolbar toolbar;
    TextView title;
    TextView lastSeen;
    private EditText editMessage;
    String user;
    private DatabaseReference mDatabaseUsers, f;
    private DatabaseReference mDatabase, mDatabaseReveiver;
    int flag = 0;
    int temp = 0;
    boolean isGroup;
    String o;
    ArrayList<String> groupMember = new ArrayList<>();
    ImageView userProfile;
    RelativeLayout linearLayout;
    private Uri fileUri;
    private String FileExtension;
    private String timeStandImage;
    LinearLayout sendmsglayout, blockLayout;
    FloatingActionMenu fab;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        title = (TextView) findViewById(R.id.titleText);
        listView = (ListView) findViewById(R.id.listView);
        userProfile = (ImageView) findViewById(R.id.userprofile);
        sendmsglayout = (LinearLayout) findViewById(R.id.linearLayout);
        fab = (FloatingActionMenu) findViewById(R.id.menu_green);
        blockLayout = (LinearLayout) findViewById(R.id.block_layout);

        lastSeen = (TextView) findViewById(R.id.lastSeen);
        linearLayout = (RelativeLayout) findViewById(R.id.layout);
        ImageView backbrn = (ImageView) findViewById(R.id.back);

        backbrn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Chat.this, ListUser.class));
                finish();
            }
        });


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        mAuth = FirebaseAuth.getInstance();
        FragmentCallMethod();
        Intent i = getIntent();
        receiver = i.getStringExtra("receiver");
        o = receiver;


        //Toast.makeText(Chat.this,receiver,Toast.LENGTH_SHORT).show();

        numbers = new ArrayList<String>(Arrays.asList(i.getStringArrayExtra("number")));
        name = new ArrayList<String>(Arrays.asList(i.getStringArrayExtra("name")));

        if (receiver.charAt(0) == '+')
            flag = 1;

        final String e = mAuth.getCurrentUser().getPhoneNumber().toString();


        title.setText(receiver);

        for (int in = 0; in < name.size(); in++) {
            if (name.contains(receiver)) {
                receiver = numbers.get(name.indexOf(receiver));
            }
        }

        blockReceiverCheck(receiver);

        DatabaseReference lstseen = FirebaseDatabase.getInstance().getReference().child(receiver).child("profile");

        lstseen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("lastseen")) {
                    String data = dataSnapshot.child("lastseen").getValue().toString();
                    Toast.makeText(Chat.this, data, Toast.LENGTH_SHORT).show();
                    if (data.equals("online")) {
                        lastSeen.setText("Online");
                    } else {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(dataSnapshot.child("lastseen").getValue().toString()));
                        lastSeen.setText("Last Seen : " + simpleDateFormat.format(calendar.getTime()));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        updateProfilePicture();
        if (receiver.charAt(0) == '+') {
            isGroup = false;
            mDatabase = FirebaseDatabase.getInstance().getReference().child(e).child("send");
        } else {
            isGroup = true;
            mDatabase = FirebaseDatabase.getInstance().getReference().child(e).child("group").child(receiver).child("send");
            getListGroupUser();
            lastSeen.setVisibility(View.INVISIBLE);
        }

        editMessage = (EditText) findViewById(R.id.editMesssageE);

        user = mAuth.getCurrentUser().getPhoneNumber().toString();

        mDatabaseReveiver = FirebaseDatabase.getInstance().getReference().child(receiver).child("receive");


        listView.setAdapter(null);
        lstChat.clear();
        getUser();
        fetch();
        blockCheckBackGround(receiver);
    }

    private void updateProfilePicture() {
        String fileName = receiver + ".jpeg";
        String completePath = Environment.getExternalStorageDirectory() + "/My Messenger/" + fileName;
        File file = new File(completePath);
        Uri imageUri = Uri.fromFile(file);

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            userProfile.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, o + "Profile pic not Found", Toast.LENGTH_SHORT).show();
        }

    }

    private void fetch() {
        if (isGroup) {
            f = FirebaseDatabase.getInstance().getReference().child(user).child("group").child(receiver).child("receive");
        } else {
            f = FirebaseDatabase.getInstance().getReference().child(user).child("receive");
        }

        f.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getChildrenCount() == 1)
                    getUser();
                // Toast.makeText(getApplicationContext(),"count***" + dataSnapshot.getChildrenCount(),Toast.LENGTH_SHORT).show();
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
    public void onStart() {
        super.onStart();

    }

    public void getUser() {
        lstChat.clear();
//        listView.smoothScrollByOffset(listView.getCount());

        if (isGroup)
            q1 = FirebaseDatabase.getInstance().getReference().child(user).child("group").child(receiver).child("send");
        else
            q1 = FirebaseDatabase.getInstance().getReference().child(user).child("send");

        q1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    if (dataSnapshot.getValue() != null) {
                        if (dataSnapshot != null && dataSnapshot.getValue().toString() != null) {
                            collectiouser2((Map<String, Object>) dataSnapshot.getValue(), false);
                        }
                    }

                }

                if (isGroup)
                    q2 = FirebaseDatabase.getInstance().getReference().child(user).child("group").child(receiver).child("receive");
                else
                    q2 = FirebaseDatabase.getInstance().getReference().child(user).child("receive");
                q2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            //System.out.println("////////////////////////////////////////////////////////// + " + dataSnapshot.toString());
                            if (dataSnapshot.getValue() != null) {
                                if (dataSnapshot != null && dataSnapshot.getValue().toString() != null) {
                                    collectiouser2((Map<String, Object>) dataSnapshot.getValue(), true);
                                    final GlobalVar globalVar = (GlobalVar) getApplicationContext();
                                    ArrayList<String> names = globalVar.getName();
                                    ArrayList<String> number = globalVar.getNumbers();

                                    CustomAdapterChat adapterChat = new CustomAdapterChat(lstChat, Chat.this, isGroup, names, number, getContentResolver());
                                    listView.setAdapter(adapterChat);
                                }
                            } else {
                                final GlobalVar globalVar = (GlobalVar) getApplicationContext();
                                ArrayList<String> names = globalVar.getName();
                                ArrayList<String> number = globalVar.getNumbers();

                                CustomAdapterChat adapterChat = new CustomAdapterChat(lstChat, Chat.this, isGroup, names, number, getContentResolver());
                                listView.setAdapter(adapterChat);
                            }


                        } catch (ClassCastException e) {
                        }
//                        catch (Exception e){}

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//        toolbar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isGroup) {
                    Intent i = new Intent(Chat.this, GroupProfileUpdate.class);
                    i.putExtra("receiver", receiver);
                    i.putExtra("gmember", groupMember);
                    i.putExtra("name", name);
                    i.putExtra("number", numbers);
                    startActivity(i);
                } else {
                    Intent i = new Intent(Chat.this, FriendsProfile.class);
                    i.putExtra("receiver", receiver);
                    i.putExtra("orirec", o);
                    startActivity(i);
                }

            }
        });

    }

    private void collectiouser2(Map<String, Object> value, boolean res) {
        if (value == null)
            System.out.println("----------------------------------------------null");
        else {
            for (Map.Entry<String, Object> entry : value.entrySet()) {
                Map content = (Map) entry.getValue();
                //System.out.println("-----------------------------------******************************" + content.get("receiver"));
                try {
                    if (receiver.equals(content.get("receiver")) || isGroup) {
                        if (content.get("ctime").toString() != null) {
                            Long t = Long.parseLong(content.get("ctime").toString());
                            String uname = content.get("receiver").toString();
                            if (uname.equals(receiver))
                                uname = "You";
                            setMessage(content.get("content").toString(), res, t, uname);
                        } else {
                            Toast.makeText(getApplicationContext(), "Null Time Stemp", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("-----------------------------------******************************" + e.toString());
                }

            }
        }
    }

    public void sendButtonClicked(View view) {
        final String messageValue = editMessage.getText().toString().trim();
        SendMessageMethod(messageValue);
    }

    private void SendMessageMethod(final String messageValue) {
        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        final String currenttime = Long.toString(timeStamp.getTime());

        if (!TextUtils.isEmpty(messageValue)) {
            final DatabaseReference newPost = mDatabase.push();
            final DatabaseReference newPosrReceive = mDatabaseReveiver.push();
            final DatabaseReference[] newpostGroup = new DatabaseReference[groupMember.size()];
            for (int i = 0; i < groupMember.size(); i++) {
                newpostGroup[i] = FirebaseDatabase.getInstance().getReference().child(groupMember.get(i)).child("group").child(receiver).child("receive").push();
            }
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    newPost.child("receiver").setValue(receiver);
                    if (messageValue.startsWith("Itis1209Image-->")) {
                        String[] d = messageValue.split("-->");
                        newPost.child("content").setValue(messageValue);
                        newPost.child("ctime").setValue(d[2]);
                    } else {
                        newPost.child("content").setValue(messageValue);
                        newPost.child("ctime").setValue(currenttime);
                    }
                    newPost.child("username").setValue(mCurrentUser.getPhoneNumber().toString());


                    if (isGroup) {
                        for (int i = 0; i < groupMember.size(); i++) {
                            if (!mCurrentUser.getPhoneNumber().toString().equals(groupMember.get(i))) {
                                newpostGroup[i].child("receiver").setValue(mCurrentUser.getPhoneNumber().toString());

                                if (messageValue.startsWith("Itis1209Image-->")) {
                                    String[] d = messageValue.split("-->");
                                    newpostGroup[i].child("content").setValue(messageValue);
                                    newpostGroup[i].child("ctime").setValue(d[2]);
                                } else {
                                    newpostGroup[i].child("content").setValue(messageValue);
                                    newpostGroup[i].child("ctime").setValue(currenttime);
                                }

//                                newpostGroup[i].child("content").setValue(messageValue);
//                                newpostGroup[i].child("username").setValue(receiver);
                                newpostGroup[i].child("ctime").setValue(currenttime);
                            }
                        }

                    } else {
                        newPosrReceive.child("receiver").setValue(mCurrentUser.getPhoneNumber().toString());

                        if (messageValue.startsWith("Itis1209Image-->")) {
                            String[] d = messageValue.split("-->");
                            newPosrReceive.child("content").setValue(messageValue);
                            newPosrReceive.child("ctime").setValue(d[2]);
                        } else {
                            newPosrReceive.child("content").setValue(messageValue);
                            newPosrReceive.child("ctime").setValue(currenttime);
                        }

//                        newPosrReceive.child("content").setValue(messageValue);
                        newPosrReceive.child("username").setValue(receiver);
//                        newPosrReceive.child("ctime").setValue(currenttime);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        lstChat.clear();
        getUser();
        editMessage.setText("");
    }

    private void setMessage(String cont, boolean s, long t, String uname) {

        lstChat.add(new CharModel(cont, s, t, uname));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menulogout, menu);
        MenuItem i = menu.findItem(R.id.addcon);
        MenuItem videoCall = menu.findItem(R.id.videoCall);
        MenuItem voiceCall = menu.findItem(R.id.voiceCall);
        blockUsercheck(menu);
        if (flag == 0)
            i.setVisible(false);
        else
            i.setVisible(true);

        if (isGroup) {
            videoCall.setVisible(false);
            voiceCall.setVisible(false);
        } else {
            videoCall.setVisible(true);
            voiceCall.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

//        if(item.getItemId()==R.id.logout){
//            FirebaseAuth.getInstance().signOut();
//            Intent i=new Intent(Chat.this,MainActivity.class);
//            startActivity(i);
//        }
//        else if(item.getItemId()==R.id.select){
//            BottomSheetDialogFragment bottomSheetDialogFragment = new Bottom_Sheet();
//            bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
//        }
//        else
        if (item.getItemId() == R.id.addcon) {
            Intent addContactIntent = new Intent(Contacts.Intents.Insert.ACTION, Contacts.People.CONTENT_URI);
            addContactIntent.putExtra(Contacts.Intents.Insert.PHONE, receiver); // an example, there is other data available
            startActivity(addContactIntent);
        } else if (item.getItemId() == R.id.profile) {
            startActivity(new Intent(Chat.this, ProfileUpload.class));
            finish();
        } else if (item.getItemId() == R.id.videoCall) {
            VideoCallMethod();
        } else if (item.getItemId() == R.id.block) {
            lockUser(item);
        } else if (item.getItemId() == R.id.voiceCall) {
            VoiceCallMethod();
        }
        return super.onOptionsItemSelected(item);
    }

    public void VoiceCallMethod() {
        try {
            Call call = getSinchServiceVoiceInterface().callUser(receiver+"voice");
            if (call == null) {
                // Service failed for some reason, show a Toast and abort
                Toast.makeText(this, "Service is not started. Try stopping the service and starting it again before "
                        + "placing a call.", Toast.LENGTH_LONG).show();
                return;
            }
            String callId = call.getCallId();
            Intent callScreen = new Intent(this, CallScreenVoice.class);
            callScreen.putExtra(SinchServiceVoice.CALL_ID, callId);
            startActivity(callScreen);
        } catch (MissingPermissionException e) {
            ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
        }
    }

    private void VideoCallMethod() {

        String userName = receiver;
        if (userName.isEmpty()) {
            Toast.makeText(this, "Please enter a user to call", Toast.LENGTH_LONG).show();
            return;
        }

        Call call = getSinchServiceInterface().callUserVideo(userName);
        String callId = call.getCallId();

        Intent callScreen = new Intent(this, CallScreenActivity.class);
        callScreen.putExtra(SinchService.CALL_ID, callId);
        startActivity(callScreen);
    }

    public void getListGroupUser() {
        FirebaseDatabase.getInstance().getReference().child(mAuth.getCurrentUser().getPhoneNumber().toString()).child("group").child(receiver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String member = dataSnapshot.child("member").getValue().toString();
                groupMember = new ArrayList<String>(Arrays.asList(member.split(" ")));
                //System.out.println("///////////////////////////////////////////// " + groupMember.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You may now place a call", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "This application needs permission to use your microphone to function properly.", Toast
                    .LENGTH_LONG).show();
        }
    }

    public void FragmentCallMethod() {
        FloatingActionButton document = findViewById(R.id.fabdocument);
        FloatingActionButton camera = findViewById(R.id.fabcamera);
        FloatingActionButton gallery = findViewById(R.id.fabgallery);
        FloatingActionButton location = findViewById(R.id.fablocation);
        FloatingActionButton contact = findViewById(R.id.fabcontact);
        FloatingActionButton audio = findViewById(R.id.fabaudio);


        View.OnClickListener fragmentClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.fabdocument:
                        Intent i = new Intent();
                        i.setType("application/pdf");
                        i.setAction(Intent.ACTION_GET_CONTENT);
                        //String timeStand=String.valueOf(System.currentTimeMillis());
                        startActivityForResult(Intent.createChooser(i, "Select Pdf"), 1212);
                        //Toast.makeText(Chat.this,"Document",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.fabcamera:
                        openCamera();
                        //Toast.makeText(Chat.this,"Camera",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.fabgallery:
                        Intent intent = new Intent();
                        intent.setType("*/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        String timeStand = String.valueOf(System.currentTimeMillis());
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 6565);
                        //Toast.makeText(Chat.this,"Gallery",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.fablocation:
                        //Toast.makeText(Chat.this,"Location",Toast.LENGTH_SHORT).show();
                        LocationTrack();
                        break;
                    case R.id.fabcontact:
                        //Toast.makeText(Chat.this,"Contact",Toast.LENGTH_SHORT).show();
                        ContactShare();
                        break;
                    case R.id.fabaudio:
                        Intent i2 = new Intent();
                        i2.setType("audio/*");
                        i2.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(i2, "Select Pdf"), 1313);
                        Toast.makeText(Chat.this, "Audio", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        document.setOnClickListener(fragmentClickListener);
        camera.setOnClickListener(fragmentClickListener);
        gallery.setOnClickListener(fragmentClickListener);
        location.setOnClickListener(fragmentClickListener);
        contact.setOnClickListener(fragmentClickListener);
        audio.setOnClickListener(fragmentClickListener);
    }

    private void ContactShare() {
        Uri uri = Uri.parse("content://contacts");
        Intent intent = new Intent(Intent.ACTION_PICK, uri);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, 7575);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        timeStandImage = String.valueOf(System.currentTimeMillis());
        File file2 = new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile");
        if (!file2.exists()) {
            file2.mkdirs();
        }

        File file = new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile", timeStandImage + ".jpg");

        fileUri = Uri.fromFile(file);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(cameraIntent, 5555);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Uri filePath = null;
        if (requestCode == 5555 && resultCode == -1) {
            uploadProfile(fileUri);
        } else if (requestCode == 6565 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            FileExtension = getMimeType(getApplicationContext(), filePath);
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                Context context = getApplicationContext();
                ContentResolver content = context.getContentResolver();
                inputStream = content.openInputStream(filePath);

                File saveDirectory = new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile");

                saveDirectory.mkdirs();
                timeStandImage = String.valueOf(System.currentTimeMillis());

                if (FileExtension.equals("jpeg") || FileExtension.equals("png"))
                    FileExtension = "jpg";

                outputStream = new FileOutputStream(saveDirectory + "/" + timeStandImage + "." + FileExtension); // filename.png, .mp3, .mp4 ...

                byte[] buffer = new byte[1000];
                int bytesRead = 0;
                while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                    outputStream.write(buffer, 0, buffer.length);
                }
            } catch (Exception e) {
                //Log.e( TAG, "Exception occurred " + e.getMessage());
            } finally {

            }

            File file = new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile/", timeStandImage + "." + FileExtension);
            Uri fileUri = Uri.fromFile(file);

            uploadProfile(fileUri);

        } else if (requestCode == 7575 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};

            Cursor cursor = getContentResolver().query(uri, projection,
                    null, null, null);
            cursor.moveToFirst();

            int numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            String number = cursor.getString(numberColumnIndex);

            int nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            String name = cursor.getString(nameColumnIndex);
            SendMessageMethod("Itis1209Contact-->" + number + "-->" + name);

        } else if (requestCode == 1212 && resultCode == RESULT_OK) {

            filePath = data.getData();

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                Context context = getApplicationContext();
                ContentResolver content = context.getContentResolver();
                inputStream = content.openInputStream(filePath);

                File saveDirectory = new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile");

                saveDirectory.mkdirs();
                timeStandImage = String.valueOf(System.currentTimeMillis());

                outputStream = new FileOutputStream(saveDirectory + "/" + timeStandImage + ".pdf"); // filename.png, .mp3, .mp4 ...

                byte[] buffer = new byte[1000];
                int bytesRead = 0;
                while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                    outputStream.write(buffer, 0, buffer.length);
                }
            } catch (Exception e) {
                //Log.e( TAG, "Exception occurred " + e.getMessage());
            } finally {

            }

            File file = new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile/", timeStandImage + ".pdf");
            Uri fileUri = Uri.fromFile(file);

            uploadProfile(fileUri);
        } else if (requestCode == 1313 && resultCode == RESULT_OK) {
            filePath = data.getData();
            //Toast.makeText(this,"path : " + filePath.toString(),Toast.LENGTH_SHORT).show();

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                Context context = getApplicationContext();
                ContentResolver content = context.getContentResolver();
                inputStream = content.openInputStream(filePath);

                File saveDirectory = new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile");

                saveDirectory.mkdirs();
                timeStandImage = String.valueOf(System.currentTimeMillis());

                outputStream = new FileOutputStream(saveDirectory + "/" + timeStandImage + ".mp3"); // filename.png, .mp3, .mp4 ...

                byte[] buffer = new byte[1000];
                int bytesRead = 0;
                while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                    outputStream.write(buffer, 0, buffer.length);
                }
            } catch (Exception e) {
                //Log.e( TAG, "Exception occurred " + e.getMessage());
            } finally {

            }

            File file = new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile/", timeStandImage + ".mp3");
            Uri fileUri = Uri.fromFile(file);

            uploadProfile(fileUri);

        }
    }

    private void uploadProfile(Uri filePath) {
        if (filePath != null) {

            System.out.println("////////////////////////////////////////////////////////////" + filePath.toString());
            String filetype = null;
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Image Uploading...");
            progressDialog.show();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            File file = null;
            String filePathdec = null;
            String[] filename;
            int file_size = 0;
            int temp = 0;
            FileExtension = getMimeType(getApplicationContext(), filePath);
            // Toast.makeText(this, "File Type : " + FileExtension, Toast.LENGTH_SHORT).show();
            if (FileExtension.equals("jpg") || FileExtension.equals("jpeg") || FileExtension.equals("png")) {
                file = new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile", "Compressed");
                filePathdec = SiliCompressor.with(getApplicationContext()).compress(filePath.toString(), file);
                filename = filePathdec.split("/");
                file = new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile/Compressed", filename[filename.length - 1]);
                file_size = Integer.parseInt(String.valueOf(file.length() / 1024));
                fileUri = Uri.fromFile(file);
                filetype = "image";
                temp = 1;
            } else if (FileExtension.equals("pdf")) {
                file = new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile/", timeStandImage + ".pdf");
                fileUri = Uri.fromFile(file);
                file_size = Integer.parseInt(String.valueOf(file.length() / 1024));
                filetype = "pdf";
                temp = 1;
            } else if (FileExtension.equals("mp3")) {
                file = new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile/", timeStandImage + ".mp3");
                fileUri = Uri.fromFile(file);
                file_size = Integer.parseInt(String.valueOf(file.length() / 1024));
                filetype = "audio";
                temp = 1;
            } else if (FileExtension.equals("mp4")) {

                file = new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile/", timeStandImage + ".mp4");
                fileUri = Uri.fromFile(file);
                file_size = Integer.parseInt(String.valueOf(file.length() / 1024));
                filetype = "video";
                temp = 1;
            } else {
                Toast.makeText(this, "File Type Not Supported", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            if (temp == 1 && file_size < 5120) {
                final String x = filetype;
                StorageReference stRef = storageReference.child("SharedFiles/" + timeStandImage + "." + FileExtension);
                stRef.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        //Toast.makeText(getApplicationContext(), "File Shared...", Toast.LENGTH_LONG).show();
                        if (x.equals("pdf"))
                            SendMessageMethod("Itis1209Document-->" + taskSnapshot.getDownloadUrl().toString() + "-->" + timeStandImage);
                        else if (x.equals("image"))
                            SendMessageMethod("Itis1209Image-->" + taskSnapshot.getDownloadUrl().toString() + "-->" + timeStandImage);
                        else if (x.equals("audio"))
                            SendMessageMethod("Itis1209Audio-->" + taskSnapshot.getDownloadUrl().toString() + "-->" + timeStandImage);
                        else if (x.equals("video"))
                            SendMessageMethod("Itis1209Video-->" + taskSnapshot.getDownloadUrl().toString() + "-->" + timeStandImage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "File Uploaded Failed", Toast.LENGTH_LONG).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage("Upload : " + ((int) progress) + "%...");
                    }
                });
            } else {
                progressDialog.dismiss();
                Toast.makeText(this, "File Size must be less than 5MB", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        countMessage();
    }

    @Override
    public void onBackPressed() {
        countMessage();
        super.onBackPressed();
    }

    public void countMessage() {
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child(user).child("receive").orderByChild("receiver").equalTo(receiver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = ((int) dataSnapshot.getChildrenCount());
                database.child(user).child("NoOfMsg").child(receiver).setValue("" + i);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void blockUsercheck(final Menu menu) {
        final DatabaseReference ds = FirebaseDatabase.getInstance().getReference();
        final MenuItem menuItem = menu.findItem(R.id.block);
//        ArrayList<String> Block=new ArrayList<>();
        ds.child(user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String blockList = "";
                if (dataSnapshot.hasChild("BlockList")) {
                    blockList = dataSnapshot.child("BlockList").getValue().toString();
                    ArrayList<String> Block = new ArrayList<>(Arrays.asList(blockList.split("<-->")));
                    if (Block.contains(receiver)) {
                        menuItem.setTitle("Unblock User");
                        System.out.println("--------------------------------------------Unblock User");
                    } else {
                        System.out.println("--------------------------------------------Block User");
                        menuItem.setTitle("Block User");
                        //blockList=blockList + "<-->" + receiver;
                    }
                }
                if (isGroup)
                    menuItem.setVisible(false);
                //ds.child(user).child("BlockList").setValue(blockList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void lockUser(final MenuItem item) {
        final String userStatus = item.getTitle().toString();
        final DatabaseReference ds = FirebaseDatabase.getInstance().getReference();
        ds.child(user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (userStatus.equals("Block User") || userStatus.equals("Block Contact")) {
                    String blockList = "";
                    if (dataSnapshot.hasChild("BlockList")) {
                        blockList = dataSnapshot.child("BlockList").getValue().toString();
                        blockList = blockList + "<-->" + receiver;
                    } else {
                        blockList = receiver;
                    }
                    ds.child(user).child("BlockList").setValue(blockList);
                    item.setTitle("Unblock User");

                } else if (userStatus.equals("Unblock User")) {
                    if (dataSnapshot.hasChild("BlockList")) {
                        String blockList = dataSnapshot.child("BlockList").getValue().toString();
                        //ArrayList<String> block=new ArrayList<>(Arrays.asList(blockList.split("<-->")));
                        blockList = blockList.replace(receiver, "");
                        blockList = blockList.replace("<--><-->", "<-->");
                        ds.child(user).child("BlockList").setValue(blockList);
                        if (blockList.equals("<-->"))
                            ds.child(user).child("BlockList").removeValue();
                        item.setTitle("Block User");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void blockReceiverCheck(String receiver) {
        final DatabaseReference ds = FirebaseDatabase.getInstance().getReference();
        ds.child(receiver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("BlockList")) {
                    //System.out.println("---------------------********************" + user + receiver + dataSnapshot.toString());
                    String blockList = dataSnapshot.child("BlockList").getValue().toString();
                    if (blockList.contains(user)) {
                        sendmsglayout.setVisibility(View.INVISIBLE);
                        fab.setVisibility(View.INVISIBLE);
                        //Toast.makeText(Chat.this,"You are block from User",Toast.LENGTH_SHORT).show();
                        blockLayout.setVisibility(View.VISIBLE);

                    } else {
                        sendmsglayout.setVisibility(View.VISIBLE);
                        fab.setVisibility(View.VISIBLE);
                        // Toast.makeText(Chat.this,"You are Unblock from User",Toast.LENGTH_SHORT).show();
                        blockLayout.setVisibility(View.INVISIBLE);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void blockCheckBackGround(final String receiver) {
        //Toast.makeText(this,"Call" + receiver,Toast.LENGTH_SHORT).show();
        final DatabaseReference ds = FirebaseDatabase.getInstance().getReference();
        ds.child(receiver).child("BlockList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                blockReceiverCheck(receiver);
                //Toast.makeText(Chat.this,"Call Added",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void LocationTrack() {
        GPSTracker gpsTracker = new GPSTracker(this);
        if(gpsTracker.getIsGPSTrackingEnabled()){
            String Data="";

            Data = "Latitude : " + String.valueOf(gpsTracker.latitude)
                    +   "\nLongitude : " + String.valueOf(gpsTracker.longitude)
                    +   "\nCountry : "   + gpsTracker.getCountryName(this)
                    +   "\nCity : "      + gpsTracker.getLocality(this)
                    +   "\nPost Code : " + gpsTracker.getPostalCode(this)
                    +   "\nAdd Line : "  + gpsTracker.getAddressLine(this);
            double latitude = gpsTracker.latitude;
            double longitude = gpsTracker.longitude;
            //if(!gpsTracker.getCountryName(this).equals(null)){
            try{
                if(!gpsTracker.getCountryName(this).equals(null)) {

                    Toast.makeText(this, Data, Toast.LENGTH_SHORT).show();
                    System.out.println("//////////////////////////Data " + Data);
                    timeStandImage = String.valueOf(System.currentTimeMillis());
                    String data = "Itis1209Location-->" + latitude + "<->" + longitude + "<->" + gpsTracker.getAddressLine(this) + "-->" + timeStandImage;

                    SendMessageMethod(data);
                }
            }catch (Exception e){
                Toast.makeText(this,"Can not get your location",Toast.LENGTH_SHORT).show();
                Toast.makeText(this,Data,Toast.LENGTH_SHORT).show();
            }


//            Uri navigationIntentUri = Uri.parse("google.navigation:q=" + latitude  +"," + longitude);
//            Intent mapIntent = new Intent(Intent.ACTION_VIEW, navigationIntentUri);
//            mapIntent.setPackage("com.google.android.apps.maps");
//            startActivity(mapIntent);


        }else{
            Toast.makeText(this,"Please Turn on your GPS",Toast.LENGTH_SHORT).show();
        }
    }


}

package com.example.amitk.mobilecon;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncAdapterType;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.SyncStateContract;
import android.support.constraint.Constraints;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sinch.android.rtc.SinchError;

import org.json.JSONArray;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ng.max.slideview.SlideView;

public class ListUser extends BaseActivity implements SinchService.StartFailedListener,SinchServiceVoice.StartFailedListener {

    private ArrayList<String> listuser = new ArrayList<>();
    String user;
    SharedPreferences sharedPreferences;
    ListView listViewUser;
    String[] numbers, names;
    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;
    StorageReference storageReference;
    boolean doubleBackToExitPressedOnce = false;
    public AsyncTask mTask;
    boolean isGroup[];
    ProgressDialog progressDialog;
    ArrayList<String> lastmsg = new ArrayList<>();
    ArrayList<String> lastmsgtime = new ArrayList<>();
    ProgressDialog mSpinner;
    ArrayList<String> uno=new ArrayList<>();
    String[] url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user);
        listViewUser = (ListView) findViewById(R.id.userList);

        File saveDirectory = new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile");
        if (!saveDirectory.exists())
            saveDirectory.mkdirs();

        startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
        // fetchbydefault();
        sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        user = sharedPreferences.getString("phone", null);
        //Toast.makeText(getApplicationContext(),user,Toast.LENGTH_LONG).show();
        DatabaseReference q1 = FirebaseDatabase.getInstance().getReference();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.show();

        storageReference = FirebaseStorage.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        user = mCurrentUser.getPhoneNumber().toString();

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        numbers = new String[phones.getCount()];
        names = new String[phones.getCount()];
        phones.moveToFirst();
        for (int cnt = 0; cnt < phones.getCount() - 1; cnt++) {
            phones.moveToNext();
            names[cnt] = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            numbers[cnt] = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if (!numbers[cnt].startsWith("+91")) {
                numbers[cnt] = "+91" + numbers[cnt];
            }
            if (numbers[cnt].contains("-") || numbers[cnt].contains(" ")) {
                numbers[cnt] = numbers[cnt].replaceAll("-", "");
                numbers[cnt] = numbers[cnt].replaceAll(" ", "");
            }
        }
        phones.close();

        final GlobalVar globalVar = (GlobalVar) getApplicationContext();
        globalVar.setName(new ArrayList<String>(Arrays.asList(names)));
        globalVar.setNumbers(new ArrayList<String>(Arrays.asList(numbers)));

        q1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //System.out.println("****************************************Data"  + dataSnapshot.toString());

                collectiouser((Map<String, Object>) dataSnapshot.getValue());
                if (dataSnapshot.child(user).hasChild("group")) {
                    //String a=dataSnapshot.child(user).child("group").get;
                    for (DataSnapshot snapshot : dataSnapshot.child(user).child("group").getChildren()) {
                        // System.out.println("-------------------------------------------------------" + snapshot.getKey());
                        listuser.add(snapshot.getKey());
                    }
                }

                String uk = dataSnapshot.child(user).child("profile").child("url").getValue().toString();
                try {
                    mTask = new DownloadTask().execute(new URL(uk + "-----" + user));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                //final String[]
                 url = new String[listuser.size()];
                isGroup = new boolean[listuser.size()];
//                lastmsg=new String[listuser.size()];
//                lastmsgtime=new String[listuser.size()];

                //ArrayList<String> uno = new ArrayList<String>(listuser);
                uno = new ArrayList<String>(listuser);
                for (int i = 0; i < listuser.size(); i++) {
                    String add = "https://firebasestorage.googleapis.com/v0/b/mobilecon-f06a9.appspot.com/o/profilePic%2Fic_user.png?alt=media&token=c41ec1ec-9166-4d43-a12d-38f78e18860d";
                    //add=dataSnapshot.child(listuser.get(i)).child("profile").child("url").getValue().toString();


                    if (listuser.get(i).startsWith("+91")) {
                        if (dataSnapshot.child(listuser.get(i)).hasChild("profile")) {
                            isGroup[i] = false;
                            if (dataSnapshot.child(listuser.get(i)).child("profile").hasChild("url")) {
                                add = dataSnapshot.child(listuser.get(i)).child("profile").child("url").getValue().toString();
                            }
                        }
                    } else {
                        isGroup[i] = true;
                        if (dataSnapshot.child(user).hasChild("group")) {
                            if (dataSnapshot.child(user).child("group").hasChild(listuser.get(i))) {
                                if (dataSnapshot.child(user).child("group").child(listuser.get(i)).hasChild("profile")) {
                                    add = dataSnapshot.child(user).child("group").child(listuser.get(i)).child("profile").getValue().toString();
                                }
                            }
                        }
                    }
                    url[i] = add;
                    // mTask = new DownloadTask().execute(stringToURL(add));
                    try {
                        mTask = new DownloadTask().execute(new URL(url[i] + "-----" + listuser.get(i)));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                }

                int[] profilePic = new int[listuser.size()];
                for (int k = 0; k < listuser.size(); k++)
                    profilePic[k] = R.drawable.app_ic;

                ArrayList<String> conTime = setLastmsgtime(listuser, uno, url);
                DatabaseReference ds=FirebaseDatabase.getInstance().getReference();
                ds.child(user).child("receive").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(dataSnapshot.getChildrenCount()==1){
                            setLastmsgtime(listuser,uno,url);
                            Toast.makeText(ListUser.this,dataSnapshot.getValue().toString(),Toast.LENGTH_SHORT).show();
                        }
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
               // System.out.println("**********************************************" + conTime.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        listViewUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ListUser.this, Chat.class);
                intent.putExtra("receiver", listuser.get(i));
                intent.putExtra("number", numbers);
                intent.putExtra("name", names);
                startActivity(intent);
            }
        });
        addTotalMsg();
        notification();
        startNotificationService();
    }

    private void collectiouser(Map<String, Object> value) {
        for (Map.Entry<String, Object> entry : value.entrySet()) {
            String u = entry.getKey();
            if (!u.equals(user)) {
                if (!u.equals("story")) {
                    listuser.add(u);
                    //System.out.println("----------------------------------------------------" + u);
                }

                //System.out.println("----------------------------------------------------" + entry);
            }

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        DatabaseReference lstseen = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().toString()).child("profile");
        lstseen.child("lastseen").setValue("online");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DatabaseReference lstseen = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().toString()).child("profile");
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        final String currenttime = Long.toString(timeStamp.getTime());
        lstseen.child("lastseen").setValue(currenttime);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.listusermenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

//        if (item.getItemId() == R.id.logout) {
//            FirebaseAuth.getInstance().signOut();
//            Intent i = new Intent(ListUser.this, MainActivity.class);
//            startActivity(i);
//            finish();
//        } else
            if (item.getItemId() == R.id.group) {
            createGroup();
            //System.out.print("----------------------------------------"+names.length);
            //Toast.makeText(getApplicationContext(),"Check",Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.profile) {
            startActivity(new Intent(ListUser.this, ProfileUpload.class));
            finish();
            // mTask=new DownloadTask().execute(stringToURL("https://firebasestorage.googleapis.com/v0/b/mobilecon-f06a9.appspot.com/o/profilePic%2Fic_user.png?alt=media&token=c41ec1ec-9166-4d43-a12d-38f78e18860d"));
        } else if (item.getItemId() == R.id.story) {
            startActivity(new Intent(ListUser.this, Story_List.class));
        } else if (item.getItemId() == R.id.bot) {
            startActivity(new Intent(ListUser.this, ChatBot.class));
        } else if (item.getItemId() == R.id.videoCall) {
            startActivity(new Intent(ListUser.this, LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    protected URL stringToURL(String urlString) {
        try {
            URL url = new URL(urlString);
            return url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createGroup() {
        DatabaseReference group = FirebaseDatabase.getInstance().getReference().child(user).child("group").child("GroupName");
        final DatabaseReference newPost = group.push();
        Intent i = new Intent(ListUser.this, CreateGroup.class);
        i.putExtra("name", names);
        i.putExtra("number", numbers);
        i.putExtra("listuser", listuser);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public void onStartFailed(SinchError error) {

    }

    @Override
    protected void onServiceConnected() {
        getSinchServiceInterface().setStartListener(this);
        getSinchServiceVoiceInterface().setStartListener(this);
        loginClicked();
    }

    @Override
    public void onStarted() {
        openPlaceCallActivity();
    }

    private void loginClicked() {
        String userName = user;

        if (userName.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show();
            return;
        }

        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceVoiceInterface().startClient(userName+"voice");
            getSinchServiceInterface().startClient(userName);
        } else {
            openPlaceCallActivity();
        }
//        if (!getSinchServiceVoiceInterface().isStarted()) {
//            getSinchServiceVoiceInterface().startClient(userName+"voice");
//        } else {
//            openPlaceCallActivity();
//        }
    }

    private void openPlaceCallActivity() {
    }

    public ArrayList<String> setLastmsgtime(final ArrayList<String> listuser, final ArrayList<String> uno, final String[] url) {
        int t = 0;
        for (int k = 0; k < listuser.size(); k++) {
            if (!listuser.get(k).startsWith("+91")) {
                t++;
            }
        }
        final int noOfGroup = t;
        final ArrayList<String> conTime = new ArrayList<>();
        for (int i = 0; i < listuser.size(); i++) {

            if (listuser.get(i).startsWith("+91")) {

                final int x = i;

                final String no = listuser.get(x);

                final DatabaseReference ds = FirebaseDatabase.getInstance().getReference().child(no).child("send");
                ds.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final ArrayList<String> content = new ArrayList<>();
                        final ArrayList<String> ctime = new ArrayList<>();

                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            try {
                                if (d.hasChild("content") && d.hasChild("ctime")) {
                                    content.add((d.child("content").getValue().toString()));
                                    ctime.add((d.child("ctime").getValue().toString()));
                                } else {
                                    content.add("no send message found");
                                    ctime.add("1516609519032");
                                }
                            } catch (Exception e) {

                            }


                            //LastMsgClass ls=new LastMsgClass(d.child("content").getValue().toString(),d.child("ctime").getValue().toString(),d.child("receiver").getValue().toString(),d.child("username").getValue().toString());
                        }
                        final DatabaseReference ds = FirebaseDatabase.getInstance().getReference().child(no).child("receive");
                        ds.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot d : dataSnapshot.getChildren()) {
                                    content.add(d.child("content").getValue().toString());
                                    ctime.add(d.child("ctime").getValue().toString());
                                    //LastMsgClass ls=new LastMsgClass(d.child("content").getValue().toString(),d.child("ctime").getValue().toString(),d.child("receiver").getValue().toString(),d.child("username").getValue().toString());

                                }
                                if (ctime.size() >= 2) {
                                    if (Long.parseLong(ctime.get(0)) > Long.parseLong(ctime.get(1))) {
                                        conTime.add(ctime.get(0) + "<-->" + content.get(0));
                                        System.out.println("++--------------------++s" + ctime.get(0) + " > " + ctime.get(1));
                                        // System.out.println("++--------------------++s" + conTime.size());
                                    } else {
                                        conTime.add(ctime.get(1) + "<-->" + content.get(1));
                                        System.out.println("++--------------------++s" + ctime.get(0) + " < " + ctime.get(1));
                                        // System.out.println("++--------------------++s" + conTime.size());
                                    }
                                }
                                int temp = noOfGroup;
                                if (conTime.size() == listuser.size()) {
                                    for (int l = 0; l < numbers.length; l++) {
                                        if (listuser.contains(numbers[l]))
                                            listuser.set(listuser.indexOf(numbers[l]), names[l]);
                                    }

                                    String[] ex = listuser.toArray(new String[listuser.size()]);

                                    SharedPreferences listuserData = getSharedPreferences("listUser", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = listuserData.edit();

                                    Set<String> setname = new HashSet<String>();
                                    setname.addAll(listuser);
                                    editor.putStringSet("name", setname);

                                    Set<String> setimg = new HashSet<String>();
                                    setimg.addAll(listuser);
                                    editor.putStringSet("img", setimg);

                                    editor.commit();

                                    //System.out.println("///////////////*************///////////////////// " + uno.toString());
                                    listViewUser.setAdapter(new CustomAdapter(ListUser.this, ex, url, getApplicationContext(), uno, getContentResolver(), conTime));
                                    progressDialog.dismiss();
                                } else if (conTime.size() == listuser.size() - temp) {
//                            for(int j=0;j<temp;j++){
//                                conTime.add(ctime.get(1) + "<-->" + content.get(1));
//                            }
                                    //System.out.println("***********************************************--" +conTime.size() + listuser.size());
                                    addgroupLastMsg(temp, conTime, url, uno);

                                } else {
                                    //System.out.println("////////////////////////********************* Last Message");
                                }
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
            }

        }

        return conTime;
    }

    private void addgroupLastMsg(final int temp, final ArrayList<String> conTime, final String[] url, final ArrayList<String> uno) {
//        System.out.println("++--------------------+++++++++++++s " + listuser.get(listuser.size()-1));
//        System.out.println("++--------------------+++++++++++++s " + listuser.get(listuser.size()-2));
        //System.out.println("++--------------------+++++++++++++      " + temp);
        //final ArrayList<String> conTime=new ArrayList<>();
        for (int i = 0; i < temp; i++) {
            final int x = i;
            int b = listuser.size() - (temp - i);
            final String no = listuser.get(b);
            System.out.println("***********************************************--" + b);

            final DatabaseReference ds = FirebaseDatabase.getInstance().getReference().child(user).child("group").child(no).child("send");
            ds.orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //System.out.println("***********************************************" + dataSnapshot.toString());
                    final ArrayList<String> content = new ArrayList<>();
                    final ArrayList<String> ctime = new ArrayList<>();
                    if (dataSnapshot.getValue() != null) {
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            try {
                                if (d.hasChild("content") && d.hasChild("ctime")) {
                                    //System.out.println("***********************************************--- if block");
                                    content.add((d.child("content").getValue().toString()));
                                    ctime.add((d.child("ctime").getValue().toString()));
                                } else {
                                    // System.out.println("***********************************************--- else block");
                                    content.add("no send message found");
                                    ctime.add("1516609519032");
                                }
                            } catch (Exception e) {

                            }

                        }
                    } else {
                        content.add("no send message found");
                        ctime.add("1516609519032");
                    }

                    final DatabaseReference ds = FirebaseDatabase.getInstance().getReference().child(user).child("group").child(no).child("receive");
                    ds.orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                for (DataSnapshot d : dataSnapshot.getChildren()) {
                                    try {
                                        if (d.hasChild("content") && d.hasChild("ctime")) {
                                            //System.out.println("***********************************************--- if block");
                                            content.add((d.child("content").getValue().toString()));
                                            ctime.add((d.child("ctime").getValue().toString()));
                                        } else {
                                            //System.out.println("***********************************************--- else block");
                                            content.add("no send message found");
                                            ctime.add("1516609519032");
                                        }
                                    } catch (Exception e) {


                                    }

                                }
                            } else {
                                content.add("no send message found");
                                ctime.add("1516609519032");
                            }

                            if (ctime.size() >= 2) {
                                if (Long.parseLong(ctime.get(0)) > Long.parseLong(ctime.get(1))) {
                                    conTime.add(ctime.get(0) + "<-->" + content.get(0));
                                    //System.out.println("++--------------------++s" + ctime.get(0) + " > " + ctime.get(1));
                                    // System.out.println("++--------------------++s" + conTime.size());
                                } else {
                                    conTime.add(ctime.get(1) + "<-->" + content.get(1));
                                    //System.out.println("++--------------------++s" + ctime.get(0) + " < " + ctime.get(1));
                                    // System.out.println("++--------------------++s" + conTime.size());
                                }
                            }
//                            System.out.println("***********************************************---" + content.get(1) + ctime.get(1));
                            if (conTime.size() == listuser.size()) {
                                for (int l = 0; l < numbers.length; l++) {
                                    if (listuser.contains(numbers[l]))
                                        listuser.set(listuser.indexOf(numbers[l]), names[l]);
                                }

                                String[] ex = listuser.toArray(new String[listuser.size()]);

                                SharedPreferences listuserData = getSharedPreferences("listUser", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = listuserData.edit();

                                Set<String> setname = new HashSet<String>();
                                setname.addAll(listuser);
                                editor.putStringSet("name", setname);

                                Set<String> setimg = new HashSet<String>();
                                setimg.addAll(listuser);
                                editor.putStringSet("img", setimg);

                                editor.commit();
                                // progressDialog.dismiss();
                                //System.out.println("///////////////*************///////////////////// " + uno.toString());
                                listViewUser.setAdapter(new CustomAdapter(ListUser.this, ex, url, getApplicationContext(), uno, getContentResolver(), conTime));
                                progressDialog.dismiss();
                            } else {
                                System.out.println("*******************Error****************************--- conTime and listUser size not same line561");
                            }
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
        }
    }


    public void notification(){
        DatabaseReference ds=FirebaseDatabase.getInstance().getReference();
//        String u=FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().toString();
//        ds.child(u).child("receive").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Toast.makeText(ListUser.this,"Msg occure",Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }
    public void startNotificationService(){
        Intent mServiceIntent;
        Notification_Service mNotificationService;
        mNotificationService = new Notification_Service(this);
        mServiceIntent = new Intent(this, mNotificationService.getClass());
        if (!isMyServiceRunning(mNotificationService.getClass())) {
            startService(mServiceIntent);
        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
               System.out.println("isMyServiceRunning?"+ true+"");
                return true;
            }
        }
        System.out.println("isMyServiceRunning?"+ false+"");
        return false;
    }

    public void addTotalMsg(){
        final DatabaseReference ds=FirebaseDatabase.getInstance().getReference();
        ds.child(user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long totalmsg=dataSnapshot.child("receive").getChildrenCount();
                ds.child(user).child("TotalReceiveMsg").setValue(totalmsg);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
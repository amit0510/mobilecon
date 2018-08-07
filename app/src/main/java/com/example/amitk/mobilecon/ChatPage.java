package com.example.amitk.mobilecon;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Contacts;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.SyncFailedException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class ChatPage extends AppCompatActivity {
    String uname="";
    private EditText editMessage;
    private DatabaseReference mDatabase,mDatabaseReveiver,mDatabaserec;
    private RecyclerView mMessageList;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabaseUsers;
    SharedPreferences sharedPreferences;
    String user=null;
    String receiver;
    ArrayList<String> numbers;
    ArrayList<String> name;
    Toolbar toolbar;
    TextView title;
    MenuItem i;
    int flag=0;
    String o;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        title=(TextView) findViewById(R.id.titleText);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        Intent intent=getIntent();
        receiver=intent.getStringExtra("receiver");
        o=receiver;
        numbers=new ArrayList<String>(Arrays.asList(intent.getStringArrayExtra("number")));
        name=new ArrayList<String>(Arrays.asList(intent.getStringArrayExtra("name")));

        title.setText(receiver);

        if(receiver.charAt(0)=='+')
            flag=1;

        //System.out.println("-----------------------------------------------"+name.contains(receiver));
        for (int i=0;i<name.size();i++){
            if(name.contains(receiver)){
                receiver=numbers.get(name.indexOf(receiver));
                //System.out.println("**********************************************Found---------");
            }
        }

        editMessage=(EditText)findViewById(R.id.editMesssageE);

        sharedPreferences=getSharedPreferences("login", Context.MODE_PRIVATE);
        user=sharedPreferences.getString("phone",null);

        mAuth=FirebaseAuth.getInstance();

        mMessageList=(RecyclerView)findViewById(R.id.messageRec);
        mMessageList.setHasFixedSize(true);
        getUsers();
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mMessageList.setLayoutManager(linearLayoutManager);

        final String e=mAuth.getCurrentUser().getPhoneNumber().toString();
        mDatabase=FirebaseDatabase.getInstance().getReference().child(e).child("send");
        mDatabaserec=FirebaseDatabase.getInstance().getReference().child(e).child("receive");
        mDatabaseReveiver=FirebaseDatabase.getInstance().getReference().child(receiver).child("receive");

        getUsers();

        Toast.makeText(getApplicationContext()," " + e,Toast.LENGTH_SHORT).show();
        mAuthListener=new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                if(firebaseAuth.getCurrentUser()==null){
                    startActivity(new Intent(ChatPage.this,MainActivity.class));
                }
            }
        };
    }
    public void getUsers(){
        final ArrayList arrayList=new ArrayList();
        DatabaseReference q1=FirebaseDatabase.getInstance().getReference();
        q1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                collectiouser((Map<String,Object>)dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(ChatPage.this,FriendsProfile.class);
                i.putExtra("receiver",receiver);
                startActivity(i);
                finish();
            }
        });

    }

    private void collectiouser(Map<String, Object> value) {
        ArrayList<String> user=new ArrayList<>();
        for(Map.Entry<String,Object> entry : value.entrySet()){
            Map singleuser=(Map)entry.getValue();
            String u=entry.getKey();
            String s=singleuser.toString();
            user.add(u);
        }
        //Toast.makeText(getApplicationContext(),user.get(0) +"\n" + user.get(1) +"\n" + user.get(2)+"\n" + user.get(3),Toast.LENGTH_SHORT).show();
    }

    public void sendButtonClicked(View view){
        mCurrentUser=mAuth.getCurrentUser();
        mDatabaseUsers=FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        final String messageValue=editMessage.getText().toString().trim();

//        SimpleDateFormat df=new SimpleDateFormat("h:mm a");
//        final String currenttime=df.format(Calendar.getInstance().getTime());

        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        final String currenttime=Long.toString(timeStamp.getTime());


        if(!TextUtils.isEmpty(messageValue)){
            final DatabaseReference newPost= mDatabase.push();
            final DatabaseReference newPosrReceive=mDatabaseReveiver.push();
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    newPost.child("receiver").setValue(receiver);
                    newPosrReceive.child("receiver").setValue(mCurrentUser.getPhoneNumber().toString());

                    newPost.child("content").setValue(messageValue);
                    newPosrReceive.child("content").setValue(messageValue);

                    newPost.child("username").setValue(mCurrentUser.getPhoneNumber().toString());
                    newPosrReceive.child("username").setValue(receiver);

                    newPost.child("ctime").setValue(currenttime);
                    newPosrReceive.child("ctime").setValue(currenttime);

                    mMessageList.scrollToPosition(mMessageList.getAdapter().getItemCount()+1);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menulogout, menu);
        MenuItem i=menu.findItem(R.id.addcon);
        if(flag==0)
            i.setVisible(false);
        else
            i.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

//        if(item.getItemId()==R.id.logout){
//            //Toast.makeText(getApplicationContext(),"Logout",Toast.LENGTH_SHORT).show();
//            FirebaseAuth.getInstance().signOut();
//            Intent i=new Intent(ChatPage.this,MainActivity.class);
//            startActivity(i);
//        }
//        else if(item.getItemId()==R.id.select){
//            BottomSheetDialogFragment bottomSheetDialogFragment = new Bottom_Sheet();
//            bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
//        }
//        else
        if(item.getItemId()==R.id.addcon){
            Intent addContactIntent = new Intent(Contacts.Intents.Insert.ACTION, Contacts.People.CONTENT_URI);
            addContactIntent.putExtra(Contacts.Intents.Insert.PHONE, receiver); // an example, there is other data available
            startActivity(addContactIntent);
        }
        else if(item.getItemId()==R.id.profile){
            startActivity(new Intent(ChatPage.this,ProfileUpload.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        FirebaseRecyclerAdapter <Message,MessageViewHolder> FBRA= new FirebaseRecyclerAdapter<Message, MessageViewHolder>(
                Message.class,
                R.layout.singlemessagelayout,
                MessageViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, Message model, int position) {
                viewHolder.setContent(model.getContent());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setCtime(model.getCtime());
            }
        };
        mMessageList.setAdapter(FBRA);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public MessageViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }
        public void setContent(String content){
            TextView message_content=(TextView)mView.findViewById(R.id.messageText);
            message_content.setText(content);
        }
        public void setUsername(String username){
            TextView username_content=(TextView)mView.findViewById(R.id.usernameText);
            username_content.setText(username);
        }
        public void  setCtime(String ctime){
            TextView time_content=(TextView)mView.findViewById(R.id.dateText);
            time_content.setText(ctime);
        }
    }
}

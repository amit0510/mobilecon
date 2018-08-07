package com.example.amitk.mobilecon;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.Contacts;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GroupProfileUpdate extends AppCompatActivity implements View.OnClickListener {
    String receiver="";
    ArrayList<String> groupMember=new ArrayList<>();
    ArrayList<String> name=new ArrayList<>();
    ArrayList<String> number=new ArrayList<>();
    ListView mem;
    TextView tgname;
    Button ExitGroup;
    ImageView groupDp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile_update);

        Intent i=getIntent();
        receiver=i.getStringExtra("receiver");
        name=i.getStringArrayListExtra("name");
        number=i.getStringArrayListExtra("number");
        ExitGroup=(Button)findViewById(R.id.btnExit);
        groupDp=(ImageView)findViewById(R.id.groupdp);

        ExitGroup.setOnClickListener(this);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(receiver);

        groupMember=i.getStringArrayListExtra("gmember");
        System.out.println(receiver + " + " + groupMember.toString());

        String[] img=new String[groupMember.size()];
        for(int j=0;j<groupMember.size();j++){
            img[j]="https://firebasestorage.googleapis.com/v0/b/mobilecon-f06a9.appspot.com/o/profilePic%2Fic_user.png?alt=media&token=c41ec1ec-9166-4d43-a12d-38f78e18860d";
        }
        String[] u=groupMember.toArray(new String[groupMember.size()]);
        String[] arrname=new String[groupMember.size()];
        for(int j=0;j<arrname.length;j++){
            if(number.contains(groupMember.get(j)))
                arrname[j]=name.get(number.indexOf(groupMember.get(j)));
            else
                arrname[j]=groupMember.get(j);
        }

        tgname=(TextView)findViewById(R.id.txtgname);
        mem=(ListView)findViewById(R.id.listmember);

        loadImage();

        tgname.setText(receiver);
        mem.setAdapter(new CustomAdapterGroup(arrname,img,getApplicationContext(),u,getContentResolver()));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_add_member, menu);
        return true;
    }
    private void loadImage() {
        String fileName = receiver + ".jpeg";
        String completePath = Environment.getExternalStorageDirectory() + "/My Messenger/" + fileName;
        File file = new File(completePath);
        Uri imageUri = Uri.fromFile(file);

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            groupDp.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            this.finish();
        }else if(item.getItemId()==R.id.addMember){

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if(view==ExitGroup){
            //Toast.makeText(this,"Exit",Toast.LENGTH_SHORT).show();
            DatabaseReference ds=FirebaseDatabase.getInstance().getReference();
            final String user=FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().toString();

            ds.child(user).child("group").child(receiver).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String allmember=dataSnapshot.child("member").getValue().toString().replace(user,"");
                    String[] member=dataSnapshot.child("member").getValue().toString().split(" ");
                    for(int i=0;i<member.length;i++){
                        if(!member.equals(null)) {
                            if(!member.equals(user))
                            FirebaseDatabase.getInstance().getReference().child(member[i]).child("group").child(receiver).child("member").setValue(allmember);
                        }
                    }
                    FirebaseDatabase.getInstance().getReference().child(user).child("group").child(receiver).removeValue();
                    Intent i=new Intent(GroupProfileUpdate.this,ListUser.class);
                    startActivity(i);
                    //Toast.makeText(GroupProfileUpdate.this,member, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}

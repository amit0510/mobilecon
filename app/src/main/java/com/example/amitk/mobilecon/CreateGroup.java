package com.example.amitk.mobilecon;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

public class CreateGroup extends AppCompatActivity {
    ArrayList<String> name=new ArrayList<>();
    ArrayList<String> number=new ArrayList<>();
    ArrayList<String> listUser=new ArrayList<>();
    ArrayList<String> gUser=new ArrayList<>();
    ListView list;
    String user;
    String[] nos;
    Button creteGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        Intent i=getIntent();
//        name=i.getStringArrayExtra("name");
//        number=i.getStringArrayExtra("number");
        user=FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().toString();
        name=new ArrayList<>(Arrays.asList(i.getStringArrayExtra("name")));
        number=new ArrayList<>(Arrays.asList(i.getStringArrayExtra("number")));
        creteGroup=findViewById(R.id.btnCreate);
        listUser=i.getStringArrayListExtra("listuser");

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Create Group");
        String[] u=listUser.toArray(new String[listUser.size()]);
        list=(ListView)findViewById(R.id.userListGroup);
        String[] img=new String[listUser.size()];
        for(int j=0;j<listUser.size();j++){
            img[j]="https://firebasestorage.googleapis.com/v0/b/mobilecon-f06a9.appspot.com/o/profilePic%2Fic_user.png?alt=media&token=c41ec1ec-9166-4d43-a12d-38f78e18860d";
        }
        if(!gUser.contains(user)){
            gUser.add(user);
        }
        nos=new String[listUser.size()];
        for(int j=0;j<listUser.size();j++){
            if(name.contains(listUser.get(j)))
                nos[j]=number.get(name.indexOf(listUser.get(j)));
            else
                nos[j]=listUser.get(j);
        }
        System.out.println("---------------////////////-----------------" + nos[0]);
        list.setAdapter(new CustomAdapterGroup(u,img,getApplicationContext(),nos,getContentResolver()));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View rowView, int i, long l) {
                String no=number.get(name.indexOf(listUser.get(i)));
                TextView tv=(TextView)rowView.findViewById(R.id.textView1);
                ImageView ck=(ImageView)rowView.findViewById(R.id.check);
                if(ck.getVisibility()==View.VISIBLE){
                    ck.setVisibility(View.INVISIBLE);
                    gUser.remove(no);
                }
                else if(ck.getVisibility()==View.INVISIBLE){
                    ck.setVisibility(View.VISIBLE);
                    gUser.add(no);
                }
                //Toast.makeText(getApplicationContext(),gUser.toString(),Toast.LENGTH_SHORT).show();
            }
        });
        creteGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CreateGroup.this, GroupProfile.class);
                i.putExtra("nos",gUser);
                startActivity(i);
            }
        });
        getall();
    }

    public void getall() {
        Toast.makeText(getApplicationContext(),""+listUser.size(),Toast.LENGTH_SHORT).show();
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.create) {
            //Toast.makeText(getApplicationContext(),"create",Toast.LENGTH_SHORT).show();
            Intent i = new Intent(CreateGroup.this, GroupProfile.class);
            i.putExtra("nos",gUser);
            startActivity(i);
        }else if(item.getItemId()==android.R.id.home)
        {
           this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

}

package com.example.amitk.mobilecon;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class CustomAdapter extends BaseAdapter{
   // public class CustomAdapter extends BaseAdapter {
        String [] result;
        Context context;
        String [] imageId;
        Context mcon;
        ContentResolver cr;
        ArrayList<String> nos=new ArrayList<>();
        ArrayList<String> conTime;
        private static LayoutInflater inflater=null;
        public CustomAdapter(ListUser mainActivity, String[] prgmNameList, String[] prgmImages, Context con, ArrayList<String> nos, ContentResolver cr,ArrayList<String> conTime) {
            // TODO Auto-generated constructor stub
            result=prgmNameList;
            context=mainActivity;
            imageId=prgmImages;
            inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mcon=con;
            this.nos=nos;
            this.cr=cr;
            this.conTime=conTime;
        }


    @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return result.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public class Holder
        {
            TextView tv;
            TextView lm;
            TextView mtime;
            ImageView img;
            TextView new_Msg;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            String[] arrconTime=conTime.get(position).split("<-->");
            Holder holder=new Holder();
            View rowView;
            rowView = inflater.inflate(R.layout.usertemp, null);
            holder.tv=(TextView) rowView.findViewById(R.id.textView1);
            holder.lm=(TextView) rowView.findViewById(R.id.textView2);
            holder.img=(ImageView) rowView.findViewById(R.id.imageView1);
            holder.mtime=(TextView)rowView.findViewById(R.id.mtime);
            holder.new_Msg=(TextView)rowView.findViewById(R.id.newMsg);

            Long t=Long.parseLong(arrconTime[0]);
            Timestamp timestamp=new Timestamp(t);
            String t2="";

            if(timestamp.getHours()>12)
                t2=(timestamp.getHours()-12)+":" + (timestamp.getMinutes() < 10 ? "0" : "") +timestamp.getMinutes() + " PM";
            else
                t2=timestamp.getHours() + ":" + (timestamp.getMinutes() < 10 ? "0" : "") + timestamp.getMinutes() + " AM";

            holder.tv.setText(result[position]);

            if(arrconTime[1].startsWith("Itis1209Contact"))
                holder.lm.setText("Contact");
            else if(arrconTime[1].startsWith("Itis1209Image"))
                holder.lm.setText("Image");
            else if(arrconTime[1].startsWith("Itis1209Document"))
                holder.lm.setText("Document");
            else if(arrconTime[1].startsWith("Itis1209Audio"))
                holder.lm.setText("Audio");
            else if(arrconTime[1].startsWith("Itis1209Video"))
                holder.lm.setText("Video");
            else if(arrconTime[1].startsWith("Itis1209Location"))
                holder.lm.setText("Location");
            else
                holder.lm.setText(arrconTime[1]);
            holder.mtime.setText(t2);

            countMessege(nos.get(position),holder.new_Msg);

            String c=nos.get(position);
            String fileName = c + ".jpeg";
            System.out.println("-------------------------------------------------- File Name : " + fileName);
            String completePath = Environment.getExternalStorageDirectory() + "/My Messenger/" + fileName;
            File file = new File(completePath);
            Uri imageUri = Uri.fromFile(file);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(cr, imageUri);
                holder.img.setImageBitmap(bitmap);
            } catch (IOException e) {
                System.out.println("----------------------- File not Found " + fileName + "  " + imageUri);
            }catch (Exception e){
                System.out.println("----------------------- file error");
            }

            holder.img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.custom_dialog_profilepic);

                    //Fdialog.setTitle("Profile");
                    final ImageView iv=(ImageView)dialog.findViewById(R.id.frdpic);

                    String fileName = nos.get(position) + ".jpeg";
                    String completePath = Environment.getExternalStorageDirectory() + "/My Messenger/" + fileName;
                    File file = new File(completePath);
                    Uri imageUri = Uri.fromFile(file);

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(cr, imageUri);
                        iv.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    dialog.show();

                }
            });
            return rowView;
        }
    private void countMessege(final String ListUser,final TextView new_Msg) {


            final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            final String user= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().toString();
            database.child(user).child("receive").orderByChild("receiver").equalTo(ListUser).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final int total_message = ((int) dataSnapshot.getChildrenCount());
                    database.child(user).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.hasChild("NoOfMsg") && dataSnapshot.child("NoOfMsg").hasChild(ListUser)){
                                if(!dataSnapshot.getValue().toString().equals(null)) {
                                    final int old_msg= Integer.parseInt(dataSnapshot.child("NoOfMsg").child(ListUser).getValue().toString());
                                    System.out.println("///////////////////////////////// Total Msg : " + total_message);
                                    System.out.println("///////////////////////////////// old Msg : " + old_msg);
                                    System.out.println("///////////////////////////////// Arrived : " + (total_message-old_msg));
                                    if((total_message-old_msg)>0) {
                                        new_Msg.setVisibility(View.VISIBLE);
                                        new_Msg.setText("" + (total_message - old_msg));
                                    }
                                    else
                                        new_Msg.setVisibility(View.INVISIBLE);
                                }
                            }else{
                                System.out.println("Old Not Found///////////////////////////////// Total Msg : " + total_message);
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


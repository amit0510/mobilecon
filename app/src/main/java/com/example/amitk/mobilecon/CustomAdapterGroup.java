package com.example.amitk.mobilecon;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

/**
 * Created by amitk on 29-Dec-17.
 */

public class CustomAdapterGroup extends BaseAdapter{
   // public class CustomAdapter extends BaseAdapter {
        String [] result;
       // Context context;
        String [] imageId;
        Context mcon;
        String[] nos;
        String[] url;
        ContentResolver cr;
        private static LayoutInflater inflater=null;
        public CustomAdapterGroup(String[] prgmNameList, String[] prgmImages, Context con, String[] nos, ContentResolver cr) {
            // TODO Auto-generated constructor stub
            result=prgmNameList;
           // context=mainActivity;
            imageId=prgmImages;
            inflater = ( LayoutInflater )con.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mcon=con;
            this.nos=nos;
            this.cr=cr;
            //url=url1;
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
            ImageView img;
            ImageView check;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            final Holder holder=new Holder();
            View rowView;
            rowView = inflater.inflate(R.layout.usertempgroup, null);
            holder.tv=(TextView) rowView.findViewById(R.id.textView1);
            holder.img=(ImageView) rowView.findViewById(R.id.imageView1);
            holder.check=(ImageView)rowView.findViewById(R.id.check);
            holder.tv.setText(result[position]);

            nos[position]=nos[position].replace(" ","");
            String fileName = nos[position] + ".jpeg";
            String completePath = Environment.getExternalStorageDirectory() + "/My Messenger/" + fileName;
            File file = new File(completePath);
            Uri imageUri = Uri.fromFile(file);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(cr, imageUri);
                holder.img.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                //Toast.makeText(this,"Profile pic not Found",Toast.LENGTH_SHORT).show();
            }

           // Picasso.with(mcon).load(imageId[position]).into(holder.img);
            return rowView;
        }

    }

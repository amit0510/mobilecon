package com.example.amitk.mobilecon;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by amitk on 29-Dec-17.
 */

public class CustomAdapterStory extends BaseAdapter{
    String [] result;
    String [] imageId;
    Context mcon;
    String[] nos;
    String[] timestand;
    ContentResolver cr;
    private static LayoutInflater inflater=null;
    public CustomAdapterStory(String[] prgmNameList, String[] prgmImages, Context con, String[] nos, ContentResolver cr,String[] timestand) {
        // TODO Auto-generated constructor stub
        result=prgmNameList;
        imageId=prgmImages;
        inflater = (LayoutInflater)con.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mcon=con;
        this.nos=nos;
        this.timestand=timestand;
        this.cr=cr;
        sortdata();
    }
    public void sortdata(){
        List<String> namelist= Arrays.asList(result);
        List<String> img=Arrays.asList(imageId);
        List<String> time=Arrays.asList(timestand);

        concurrentSort(time, namelist, img, time);

        final GlobalVar globalVar = (GlobalVar)mcon;
        ArrayList<String> name=globalVar.getName();
        ArrayList<String> number=globalVar.getNumbers();
        for(int i=0;i<namelist.size();i++){
            if(name.contains(namelist.get(i)))
                nos[i]=number.get(name.indexOf(namelist.get(i)));
        }
        Collections.reverse(Arrays.asList(result));
        Collections.reverse(Arrays.asList(imageId));
        Collections.reverse(Arrays.asList(timestand));
        Collections.reverse(Arrays.asList(nos));
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
        TextView time;
        ImageView img;
        ImageView check;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final CustomAdapterStory.Holder holder=new CustomAdapterStory.Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.usertempstory, null);
        holder.tv=(TextView) rowView.findViewById(R.id.textView1);
        holder.img=(ImageView) rowView.findViewById(R.id.imageView1);
        holder.check=(ImageView)rowView.findViewById(R.id.check);
        holder.tv.setText(result[position]);
        holder.time=(TextView)rowView.findViewById(R.id.time);

        Long t=Long.parseLong(timestand[position]);
        Timestamp timestamp=new Timestamp(t);
        String t2="";

        if(timestamp.getHours()>12)
            t2=(timestamp.getHours()-12)+":" + (timestamp.getMinutes() < 10 ? "0" : "") +timestamp.getMinutes() + " PM";
        else
            t2=timestamp.getHours() + ":" + (timestamp.getMinutes() < 10 ? "0" : "") + timestamp.getMinutes() + " AM";

        holder.time.setText(t2);
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
        }
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //.makeText(mcon,""+nos[position],Toast.LENGTH_SHORT).show();
                Intent p=new Intent(mcon,watch_story.class);
                p.putExtra("user",nos[position]);
                p.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mcon.startActivity(p);
            }
        });
        // Picasso.with(mcon).load(imageId[position]).into(holder.img);
        return rowView;
    }
    public static <T extends Comparable<T>> void concurrentSort(
            final List<T> key, List<?>... lists){
        // Create a List of indices
        List<Integer> indices = new ArrayList<Integer>();
        for(int i = 0; i < key.size(); i++)
            indices.add(i);

        // Sort the indices list based on the key
        Collections.sort(indices, new Comparator<Integer>(){
            @Override public int compare(Integer i, Integer j) {
                return key.get(i).compareTo(key.get(j));
            }
        });
        Map<Integer,Integer> swapMap = new HashMap<Integer, Integer>(indices.size());
        List<Integer> swapFrom = new ArrayList<Integer>(indices.size()),
                swapTo   = new ArrayList<Integer>(indices.size());
        for(int i = 0; i < key.size(); i++){
            int k = indices.get(i);
            while(i != k && swapMap.containsKey(k))
                k = swapMap.get(k);

            swapFrom.add(i);
            swapTo.add(k);
            swapMap.put(i, k);
        }

        for(List<?> list : lists)
            for(int i = 0; i < list.size(); i++)
                Collections.swap(list, swapFrom.get(i), swapTo.get(i));
    }

}

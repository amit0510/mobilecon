package com.example.amitk.mobilecon;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iceteck.silicompressorr.SiliCompressor;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CustomAdapterChat extends BaseAdapter{
    private List<CharModel> lstChat;
    private Context context;
    private LayoutInflater inflater;
    private boolean isGroup;
    ArrayList<String> name=new ArrayList<>();
    ArrayList<String> number=new ArrayList<>();
    ContentResolver contentResolver;
    public CustomAdapterChat(){

    }
    public CustomAdapterChat(List<CharModel> lstChat, Context context,boolean isGroup,ArrayList<String> name,ArrayList<String> number,ContentResolver contentResolver){
        this.lstChat=lstChat;
        this.context=context;
        stredit();
        inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.isGroup=isGroup;
        this.name=name;
        this.number=number;
        this.contentResolver=contentResolver;
    }
    public void stredit(){
        Collections.sort(lstChat, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                CharModel c1=(CharModel)o1;
                CharModel c2=(CharModel)o2;
                return c1.getTimes().compareTo(c2.getTimes());
            }
        });
    }

    @Override
    public int getCount() {
        return lstChat.size();
    }

    @Override
    public Object getItem(int i) {
        return lstChat.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vi=null;

       if(vi==null){

           try{
               if(lstChat.get(i).getChatMessage().startsWith("Itis1209Contact-->")){
                    System.out.println("----------************-----------*************** Its is Contact");
                    if(lstChat.get(i).isSend)
                        vi=inflater.inflate(R.layout.list_send_contact,null);
                    else
                        vi=inflater.inflate(R.layout.list_rec_contact,null);

                    final String[] data=lstChat.get(i).chatMessage.split("-->");
                    final TextView conName=(TextView) vi.findViewById(R.id.conName);
                    TextView conNumber=(TextView)vi.findViewById(R.id.conNumber);
                    TextView conAdd=(TextView)vi.findViewById(R.id.addSave);

                    conName.setText(data[2]);
                    conNumber.setText(data[1].replace(" ",""));

                       conAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent contactIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
                            contactIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);

                            contactIntent
                                    .putExtra(ContactsContract.Intents.Insert.NAME, data[2])
                                    .putExtra(ContactsContract.Intents.Insert.PHONE, data[1].replace(" ",""));
                            ((Chat)context).startActivityForResult(contactIntent,1212);

                        }
                    });
               }else if(lstChat.get(i).getChatMessage().startsWith("Itis1209Image-->")){
                   System.out.println("----------************-----------*************** Its is IMAGE");
                   if(lstChat.get(i).isSend)
                       vi=inflater.inflate(R.layout.list_sendimage,null);
                   else
                       vi=inflater.inflate(R.layout.list_recimage,null);

                   final ImageView sharedImg=(ImageView)vi.findViewById(R.id.SharedImage);
                   TextView msgtime=(TextView)vi.findViewById(R.id.time);
                   final ProgressBar progressBar=(ProgressBar)vi.findViewById(R.id.progressBar2);
                   final String[] data=lstChat.get(i).chatMessage.split("-->");

                   Long t=Long.parseLong(data[2]);
                   Timestamp timestamp=new Timestamp(t);
                   String t2="";

                   if(timestamp.getHours()>12)
                       t2=(timestamp.getHours()-12)+":" + (timestamp.getMinutes() < 10 ? "0" : "") +timestamp.getMinutes() + " PM";
                   else
                       t2=timestamp.getHours() + ":" + (timestamp.getMinutes() < 10 ? "0" : "") + timestamp.getMinutes() + " AM";
                   msgtime.setText(t2);


                   final File imgFile = new  File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile/" + data[2] +".jpg");

                   if(imgFile.exists()){
                       try{
                           //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                           Uri fileUri= Uri.fromFile(imgFile);
                           Bitmap imageBitmap = SiliCompressor.with(context).getCompressBitmap(fileUri.toString());

                           sharedImg.setImageBitmap(imageBitmap);
                       }catch (Exception e){
                           System.out.println("--------------------------Exception in file exist");

                       }
                   }else {
                       final View x=vi;
                       System.out.println("---------------------Shared Image Not Found--------------" + data[2] +".jpg");
                   }
                   sharedImg.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                            if(imgFile.exists()){
                                Intent i=new Intent(context,SharedImageFull.class);
                                i.putExtra("fname",data[2]);
                                context.startActivity(i);
                            }else{
                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);

                                progressBar.setVisibility(View.VISIBLE);
                                URL url=null;
                                try {
                                    url=new URL(data[1] + "<-->" + data[2]);
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }

                                AsyncTask mtask=new AsyncTask() {
                                    @Override
                                    protected Object doInBackground(Object[] objects) {
                                        // String data[]=urls[0].toString().split("<-->");
                                        URL url= null;
                                        try {
                                            url = new URL(data[1]);
                                        } catch (MalformedURLException e) {
                                            e.printStackTrace();
                                        }
//        try {
//            url = new URL(urls[0]);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
                                        System.out.println("*********************Asyntask************************* " + data[0] + data[1]);
                                        int count;
                                        try {
                                            URLConnection conexion=url.openConnection();
                                            conexion.connect();

                                            String targetfilename="/" + data[2] + ".jpg";
                                            File file=new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile");
                                            if (!file.exists()) {
                                                file.mkdir();
                                            }
                                            InputStream input=new BufferedInputStream(url.openStream());
                                            OutputStream output=new FileOutputStream(file+targetfilename);
                                            byte data1[]=new byte[1024];
                                            long total=0;
                                            while((count=input.read(data1))!=-1){
                                                total+=count;
                                                output.write(data1,0,count);
                                            }
                                            output.flush();
                                            output.close();
                                            input.close();
                                            //setView(x);

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Object o) {

                                        super.onPostExecute(o);
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                        sharedImg.setImageBitmap(myBitmap);

                                    }
                                }.execute();
                                System.out.println("---------------------*Shared Image Not Found*--------------" + data[2] +".jpg");

                            }
                       }
                   });
               }
               else if(lstChat.get(i).getChatMessage().startsWith("Itis1209Document-->")){
                   System.out.println("----------************-----------*************** Its is Document");
                   if(lstChat.get(i).isSend)
                       vi=inflater.inflate(R.layout.list_send_document,null);
                   else
                       vi=inflater.inflate(R.layout.list_rec_document,null);

                   final ImageView sharedImg=(ImageView)vi.findViewById(R.id.SharedImage);
                   TextView msgtime=(TextView)vi.findViewById(R.id.time);
                   final ProgressBar progressBar=(ProgressBar)vi.findViewById(R.id.progressBar2);
                   final String[] data=lstChat.get(i).chatMessage.split("-->");

                   Long t=Long.parseLong(data[2]);
                   Timestamp timestamp=new Timestamp(t);
                   String t2="";

                   if(timestamp.getHours()>12)
                       t2=(timestamp.getHours()-12)+":" + (timestamp.getMinutes() < 10 ? "0" : "") +timestamp.getMinutes() + " PM";
                   else
                       t2=timestamp.getHours() + ":" + (timestamp.getMinutes() < 10 ? "0" : "") + timestamp.getMinutes() + " AM";
                   msgtime.setText(t2);

                   final File imgFile = new  File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile/" + data[2] +".pdf");

                   if(imgFile.exists()){
                       try{
                           sharedImg.setImageResource(R.mipmap.ic_pdf);
                       }catch (Exception e){
                           System.out.println("--------------------------Exception in file exist");

                       }
                   }else {
                       final View x=vi;
                       System.out.println("---------------------Shared Image Not Found--------------" + data[2] +".pdf");
                   }

                   sharedImg.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                           if(imgFile.exists()){
                               //File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+ filename);
                               Intent intent = new Intent(Intent.ACTION_VIEW);
                               intent.setDataAndType(Uri.fromFile(imgFile),"application/pdf");
                               intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                               context.startActivity(intent);
                           }else{
                               StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                               StrictMode.setThreadPolicy(policy);

                               progressBar.setVisibility(View.VISIBLE);
                               URL url=null;
                               try {
                                   url=new URL(data[1] + "<-->" + data[2]);
                               } catch (MalformedURLException e) {
                                   e.printStackTrace();
                               }

                               AsyncTask mtask=new AsyncTask() {
                                   @Override
                                   protected Object doInBackground(Object[] objects) {
                                       // String data[]=urls[0].toString().split("<-->");
                                       URL url= null;
                                       try {
                                           url = new URL(data[1]);
                                       } catch (MalformedURLException e) {
                                           e.printStackTrace();
                                       }

                                       System.out.println("*********************Asyntask************************* " + data[0] + data[1]);
                                       int count;
                                       try {
                                           URLConnection conexion=url.openConnection();
                                           conexion.connect();

                                           String targetfilename="/" + data[2] + ".pdf";
                                           File file=new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile");
                                           if (!file.exists()) {
                                               file.mkdir();
                                           }
                                           InputStream input=new BufferedInputStream(url.openStream());
                                           OutputStream output=new FileOutputStream(file+targetfilename);
                                           byte data1[]=new byte[1024];
                                           long total=0;
                                           while((count=input.read(data1))!=-1){
                                               total+=count;
                                               output.write(data1,0,count);
                                           }
                                           output.flush();
                                           output.close();
                                           input.close();
                                           //setView(x);

                                       } catch (IOException e) {
                                           e.printStackTrace();
                                       }

                                       return null;
                                   }

                                   @Override
                                   protected void onPostExecute(Object o) {

                                       super.onPostExecute(o);
                                       progressBar.setVisibility(View.INVISIBLE);

                                       sharedImg.setImageResource(R.mipmap.ic_pdf);

                                   }
                               }.execute();
                               System.out.println("---------------------*Shared Image Not Found*--------------" + data[2] +".jpg");

                           }
                       }
                   });

               }else if(lstChat.get(i).getChatMessage().startsWith("Itis1209Audio-->")){
                   System.out.println("----------************-----------*************** Its is Audio");
                   if(lstChat.get(i).isSend)
                       vi=inflater.inflate(R.layout.list_send_audio,null);
                   else
                       vi=inflater.inflate(R.layout.list_rec_audio,null);

                   final ImageView sharedImg=(ImageView)vi.findViewById(R.id.SharedImage);
                   TextView msgtime=(TextView)vi.findViewById(R.id.time);
                   final ProgressBar progressBar=(ProgressBar)vi.findViewById(R.id.progressBar2);
                   final String[] data=lstChat.get(i).chatMessage.split("-->");

                   Long t=Long.parseLong(data[2]);
                   Timestamp timestamp=new Timestamp(t);
                   String t2="";

                   if(timestamp.getHours()>12)
                       t2=(timestamp.getHours()-12)+":" + (timestamp.getMinutes() < 10 ? "0" : "") +timestamp.getMinutes() + " PM";
                   else
                       t2=timestamp.getHours() + ":" + (timestamp.getMinutes() < 10 ? "0" : "") + timestamp.getMinutes() + " AM";
                   msgtime.setText(t2);

                   final File imgFile = new  File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile/" + data[2] +".mp3");

                   if(imgFile.exists()){
                       try{
                           sharedImg.setImageResource(R.mipmap.ic_play);
                       }catch (Exception e){
                           System.out.println("--------------------------Exception in file exist");

                       }
                   }else {
                       final View x=vi;
                       System.out.println("---------------------Shared Image Not Found--------------" + data[2] +".mp3");
                   }

                   sharedImg.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                           if(imgFile.exists()){
                               //File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+ filename);
                               Intent intent = new Intent(Intent.ACTION_VIEW);
                               intent.setDataAndType(Uri.fromFile(imgFile),"audio/*");
                               intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                               context.startActivity(intent);
                           }else{
                               StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                               StrictMode.setThreadPolicy(policy);

                               progressBar.setVisibility(View.VISIBLE);
                               URL url=null;
                               try {
                                   url=new URL(data[1] + "<-->" + data[2]);
                               } catch (MalformedURLException e) {
                                   e.printStackTrace();
                               }

                               AsyncTask mtask=new AsyncTask() {
                                   @Override
                                   protected Object doInBackground(Object[] objects) {
                                       // String data[]=urls[0].toString().split("<-->");
                                       URL url= null;
                                       try {
                                           url = new URL(data[1]);
                                       } catch (MalformedURLException e) {
                                           e.printStackTrace();
                                       }

                                       System.out.println("*********************Asyntask************************* " + data[0] + data[1]);
                                       int count;
                                       try {
                                           URLConnection conexion=url.openConnection();
                                           conexion.connect();

                                           String targetfilename="/" + data[2] + ".mp3";
                                           File file=new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile");
                                           if (!file.exists()) {
                                               file.mkdir();
                                           }
                                           InputStream input=new BufferedInputStream(url.openStream());
                                           OutputStream output=new FileOutputStream(file+targetfilename);
                                           byte data1[]=new byte[1024];
                                           long total=0;
                                           while((count=input.read(data1))!=-1){
                                               total+=count;
                                               output.write(data1,0,count);
                                           }
                                           output.flush();
                                           output.close();
                                           input.close();
                                           //setView(x);

                                       } catch (IOException e) {
                                           e.printStackTrace();
                                       }

                                       return null;
                                   }

                                   @Override
                                   protected void onPostExecute(Object o) {

                                       super.onPostExecute(o);
                                       progressBar.setVisibility(View.INVISIBLE);

                                       sharedImg.setImageResource(R.mipmap.ic_play);

                                   }
                               }.execute();
                               System.out.println("---------------------*Shared Image Not Found*--------------" + data[2] +".mp3");

                           }
                       }
                   });
               }
               else if(lstChat.get(i).getChatMessage().startsWith("Itis1209Video-->")){
                   System.out.println("----------************-----------*************** Its is Video");
                   if(lstChat.get(i).isSend)
                       vi=inflater.inflate(R.layout.list_send_video,null);
                   else
                       vi=inflater.inflate(R.layout.list_rec_video,null);

                   final ImageView sharedImg=(ImageView)vi.findViewById(R.id.SharedImage);
                   TextView msgtime=(TextView)vi.findViewById(R.id.time);
                   final ProgressBar progressBar=(ProgressBar)vi.findViewById(R.id.progressBar2);
                   final String[] data=lstChat.get(i).chatMessage.split("-->");

                   Long t=Long.parseLong(data[2]);
                   Timestamp timestamp=new Timestamp(t);
                   String t2="";

                   if(timestamp.getHours()>12)
                       t2=(timestamp.getHours()-12)+":" + (timestamp.getMinutes() < 10 ? "0" : "") +timestamp.getMinutes() + " PM";
                   else
                       t2=timestamp.getHours() + ":" + (timestamp.getMinutes() < 10 ? "0" : "") + timestamp.getMinutes() + " AM";
                   msgtime.setText(t2);

                   final File imgFile = new  File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile/" + data[2] +".mp4");

                   if(imgFile.exists()){
                       try{
                           //sharedImg.setImageResource(R.mipmap.audio);
                           Bitmap bitmap= ThumbnailUtils.createVideoThumbnail(imgFile.getPath(), MediaStore.Video.Thumbnails.MICRO_KIND);
                           sharedImg.setImageBitmap(bitmap);
                       }catch (Exception e){
                           System.out.println("--------------------------Exception in file exist");

                       }
                   }else {
                       final View x=vi;
                       System.out.println("---------------------Shared Image Not Found--------------" + data[2] +".mp4");
                   }

                   sharedImg.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                           if(imgFile.exists()){
                               //File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+ filename);
                               Intent intent = new Intent(Intent.ACTION_VIEW);
                               intent.setDataAndType(Uri.fromFile(imgFile),"video/*");
                               intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                               context.startActivity(intent);
                           }else{
                               StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                               StrictMode.setThreadPolicy(policy);

                               progressBar.setVisibility(View.VISIBLE);
                               URL url=null;
                               try {
                                   url=new URL(data[1] + "<-->" + data[2]);
                               } catch (MalformedURLException e) {
                                   e.printStackTrace();
                               }

                               AsyncTask mtask=new AsyncTask() {
                                   @Override
                                   protected Object doInBackground(Object[] objects) {
                                       // String data[]=urls[0].toString().split("<-->");
                                       URL url= null;
                                       try {
                                           url = new URL(data[1]);
                                       } catch (MalformedURLException e) {
                                           e.printStackTrace();
                                       }

                                       System.out.println("*********************Asyntask************************* " + data[0] + data[1]);
                                       int count;
                                       try {
                                           URLConnection conexion=url.openConnection();
                                           conexion.connect();

                                           String targetfilename="/" + data[2] + ".mp4";
                                           File file=new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile");
                                           if (!file.exists()) {
                                               file.mkdir();
                                           }
                                           InputStream input=new BufferedInputStream(url.openStream());
                                           OutputStream output=new FileOutputStream(file+targetfilename);
                                           byte data1[]=new byte[1024];
                                           long total=0;
                                           while((count=input.read(data1))!=-1){
                                               total+=count;
                                               output.write(data1,0,count);
                                           }
                                           output.flush();
                                           output.close();
                                           input.close();
                                           //setView(x);

                                       } catch (IOException e) {
                                           e.printStackTrace();
                                       }

                                       return null;
                                   }

                                   @Override
                                   protected void onPostExecute(Object o) {

                                       super.onPostExecute(o);
                                       progressBar.setVisibility(View.INVISIBLE);
                                       File file=new File(Environment.getExternalStorageDirectory() + "/My Messenger/SharedFile/" +data[2] +".mp4");
                                       Bitmap bitmap= ThumbnailUtils.createVideoThumbnail(file.getPath(), MediaStore.Video.Thumbnails.MICRO_KIND);
                                       sharedImg.setImageBitmap(bitmap);
                                       //sharedImg.setImageResource(R.mipmap.audio);

                                   }
                               }.execute();
                               System.out.println("---------------------*Shared Image Not Found*--------------" + data[2] +".mp4");

                           }
                       }
                   });
               }
               else if(lstChat.get(i).getChatMessage().startsWith("Itis1209Location-->")){
                   if(lstChat.get(i).isSend)
                       vi=inflater.inflate(R.layout.list_rec_location,null);
                   else
                       vi=inflater.inflate(R.layout.list_rec_location,null);

                   final String[] data=lstChat.get(i).chatMessage.split("-->");
                   final TextView conName=(TextView) vi.findViewById(R.id.Address);
                   final TextView conAdd=(TextView)vi.findViewById(R.id.navigate);
                   final String[] add=data[1].split("<->");


                   conName.setText(add[2]);


                   conAdd.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {

                           double lat=Double.parseDouble(add[0]);
                           double lon=Double.parseDouble(add[1]);

                           Toast.makeText(context,add[0] + " " + add[1],Toast.LENGTH_SHORT).show();
                           Uri navigationIntentUri = Uri.parse("google.navigation:q=" + lat  +"," + lon);
                           Intent mapIntent = new Intent(Intent.ACTION_VIEW, navigationIntentUri);
                           mapIntent.setPackage("com.google.android.apps.maps");
                           context.startActivity(mapIntent);
                       }
                   });
               }
               else{
                    if(lstChat.get(i).isSend)
                        if(isGroup)
                            vi=inflater.inflate(R.layout.list_sendg,null);
                        else
                            vi=inflater.inflate(R.layout.list_send,null);
                    else
                        if (isGroup)
                            vi=inflater.inflate(R.layout.list_recg,null);
                        else
                            vi=inflater.inflate(R.layout.list_rec,null);

                       TextView bubbleTextView=(TextView)vi.findViewById(R.id.bubbleChat);
                       TextView txtTime=(TextView)vi.findViewById(R.id.time);


                       Long t=lstChat.get(i).getTimes();
                       Timestamp timestamp=new Timestamp(t);
                       String t2="";

                       if(timestamp.getHours()>12)
                           t2=(timestamp.getHours()-12)+":" + (timestamp.getMinutes() < 10 ? "0" : "") +timestamp.getMinutes() + " PM";
                       else
                           t2=timestamp.getHours() + ":" + (timestamp.getMinutes() < 10 ? "0" : "") + timestamp.getMinutes() + " AM";

                       bubbleTextView.setText(lstChat.get(i).chatMessage);
                       if(isGroup){

                           TextView txtUname=(TextView)vi.findViewById(R.id.uname);
                           String u=lstChat.get(i).getUname();
                           if(number.contains(u))
                               u=name.get(number.indexOf(u));
                           System.out.println("//////////////*************/////////////" + name.size() +""+number.size());
                           txtUname.setText(u);
                       }

                       txtTime.setText(t2);
             }
           }catch (Exception e){
               //Toast.makeText(context,""+lstChat.get(i).isSend,Toast.LENGTH_SHORT).show();
           }
       }

        return vi;
    }
}

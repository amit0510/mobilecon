package com.example.amitk.mobilecon;

import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by amitk on 02-Feb-18.
 */
public class DownloadTask extends AsyncTask<URL,Void,Bitmap>{
    protected void onPreExecute(){
    }
    protected Bitmap doInBackground(URL...urls){
        String[] u=urls[0].toString().split("-----");
        System.out.println("************************************************" + urls[0].toString());

        URL url = null;
        try {
            url = new URL(u[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
       // System.out.println("//////////////////////////*****************************" + u.length);
        int count;
        try {
            URLConnection conexion=url.openConnection();
            conexion.connect();

                String targetfilename="/" + u[1] + ".jpeg";
                File file=new File(Environment.getExternalStorageDirectory() + "/My Messenger");
                if (!file.exists()) {
                    file.mkdir();
                }
                InputStream input=new BufferedInputStream(url.openStream());
                OutputStream output=new FileOutputStream(file+targetfilename);
                byte data[]=new byte[1024];
                long total=0;
                while((count=input.read(data))!=-1){
                    total+=count;
                    output.write(data,0,count);
                }
                output.flush();
                output.close();
                input.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(Bitmap result){}
}

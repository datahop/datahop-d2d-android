/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.backend;

import android.content.Context;
//import android.content.Intent;
import android.os.AsyncTask;

//import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

//import network.datahop.localsharing.MainActivity;
import network.datahop.localfirst.data.Chunking;
import network.datahop.localfirst.data.ContentDatabaseHandler;
import network.datahop.localfirst.net.DiscoveryListener;
import network.datahop.localfirst.utils.Config;
import network.datahop.localfirst.utils.G;

/**
 * Async Task to download file from URL
 */
public class DownloadFile extends AsyncTask<String, String, String> {

    //private String fileName,folder;
    private static final String TAG="DownloadFile";
    Context context;
    ContentDatabaseHandler db;
    String name;
    int id;
    String url;
    int total;
    DiscoveryListener listener;
    public DownloadFile(Context context, String name, int id, String url, int total, DiscoveryListener listener)
    {
        //this.context =  context;
        db = new ContentDatabaseHandler(context);
        this.context = context;
        this.name = name;
        this.id = id;
        this.url = url;
        this.total = total;
        this.listener = listener;
        //this.parent = parent;
        //this.group = group;
    }

    /**
     * Before starting background thread
     * Show Progress Bar Dialog
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * Downloading file in background thread
     */
    @Override
    protected String doInBackground(String... f_url) {
        int count;
        try {
            URL durl = new URL(f_url[0]);
            URLConnection connection = durl.openConnection();
            connection.connect();
            // getting file length
            int lengthOfFile = connection.getContentLength();


            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(durl.openStream(), 8192);

            //String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

            //Extract file name from URL
            //fileName = f_url[0].substring(f_url[0].lastIndexOf('/') + 1, f_url[0].length());


            File f = new File(context.getExternalFilesDir(Config.FOLDER) + "/" + name+"."+id);

            // Output stream to write file
            OutputStream output = new FileOutputStream(f);

            byte data[] = new byte[1024];

            //long total = 0;

            //cd.flagDownloaded(fileName,id,"DataHop");


            while ((count = input.read(data)) != -1) {
                //total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                //publishProgress("" + (int) ((total * 100) / lengthOfFile));
                //G.Log(TAG, "Progress: " + (int) ((total * 100) / lengthOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();
            G.Log(TAG,"SAVED FILE "+f.getAbsolutePath());
            return name;

        } catch (Exception e) {
            G.Log(TAG,"Error: ", e.getMessage());
            db.rmContent(name,"TestGroup");
        }

        return "";
    }



    @Override
    protected void onPostExecute(String name) {

        if(!name.equals("")) {
            //String baseFilename = context.getExternalFilesDir("share_it") + "/"+name;
            //G.Log(TAG,"Savefile2 "+name +" "+id+" "+total+" "+Chunking.getNumberParts(baseFilename));

            db.setContentDownloaded(name, "TestGroup", id);
            /*Intent broadcast = new Intent(MainActivity.NEW_CHUNK_RECEIVED);
            broadcast.putExtra("group", "TestGroup");
            LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);*/
            try {
                G.Log(TAG, "Result savefile " + name + " " + total + " " + db.getReceived(name, "TestGroup"));

                if (db.getReceived(name, "TestGroup") == total) {
                    if (!Chunking.exists(context.getExternalFilesDir(Config.FOLDER) + "/" + name)) {
                        G.Log(TAG, "Chunking started");
                        Chunking.join(name, context, "TestGroup");
                        db.setContentJoined(name,"TestGroup");
                        listener.onNewContent(name,"TestGroup",0,300);
                        //LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);

                        //Intent broadcast2 = new Intent(DataSharingClient.DOWNLOAD_COMPLETED);
                        //context.sendBroadcast(broadcast2);
                    }
                }
            } catch (IOException io) {
                G.Log(TAG, "IOException " + io.getMessage());
            }
        }
    }

}

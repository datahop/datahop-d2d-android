/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.data;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

//import network.datahop.localfirst.MainActivity;
import network.datahop.localfirst.net.DiscoveryListener;
import network.datahop.localfirst.utils.Config;
import network.datahop.localfirst.utils.G;


public class SaveFile extends AsyncTask<String, Void, String> {

    public static final String TAG = SaveFile.class.getName();

    Context context;
    ContentDatabaseHandler db;
    byte[] msg;
    String group;
    DiscoveryListener listener;

    public SaveFile(Context context, byte[] msg, String group, DiscoveryListener listener)
    {
        this.context =  context;
        db = new ContentDatabaseHandler(context);
        this.msg = msg;
        this.group = group;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {

        G.Log(TAG,"Save file "+params[0]);
        String filename = params[0];
        String baseName = params[1];
        try {

            File f;
            f = new File(context.getExternalFilesDir(Config.FOLDER) + "/" + filename);
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(msg);
            fos.close();

        } catch (FileNotFoundException e) {
            G.Log(TAG, "FileNotFoundException" + e.getMessage());
        } catch (IOException e) {
            G.Log(TAG, "IOException" + e.getMessage());
        }
        return baseName;
    }

    @Override
    protected void onPostExecute(String name) {
        int total = db.getContent(name).get(0).getTotal();
        //String baseFilename = context.getExternalFilesDir("share_it") + "/"+name;
        //G.Log(TAG,"Savefile2 "+name +" "+id+" "+total+" "+Chunking.getNumberParts(baseFilename));

        try {
            G.Log(TAG,"Result savefile "+name+" "+total+" "+db.getReceived(name,group));

            if (db.getReceived(name,group) == total) {
                if(!Chunking.exists(context.getExternalFilesDir(Config.FOLDER) + "/"+name)) {
                    G.Log(TAG,"Chunking started");
                    Chunking.join(name, context,group);
                    db.setContentJoined(name,group);
                    listener.onNewContent(name,group,msg.length,300);
                    /*Intent broadcast = new Intent(MainActivity.NEW_CHUNK_RECEIVED);
                    broadcast.putExtra("group", group);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);*/

                }
            }
        }catch (IOException io){G.Log(TAG,"IOException "+io.getMessage());}
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }


}
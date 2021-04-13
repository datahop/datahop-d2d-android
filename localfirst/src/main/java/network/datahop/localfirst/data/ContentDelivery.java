/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.data;


import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

//import network.datahop.localfirst.MainActivity;
import network.datahop.localfirst.backend.DataHopBackend;
import network.datahop.localfirst.net.DiscoveryListener;
import network.datahop.localfirst.utils.Config;
import network.datahop.localfirst.utils.G;

//import static network.datahop.localsharing.data.DataSharingClient.DOWNLOAD_COMPLETED;

public final class ContentDelivery {

    private final static String TAG = "ContentDelivery";
    ContentDatabaseHandler db;
    Context context;
    DiscoveryListener listener;
    DataHopBackend backend;

    public ContentDelivery(Context context, DiscoveryListener listener, DataHopBackend backend){
        db = new ContentDatabaseHandler(context);
        this.context=context;
        this.listener = listener;
        this.backend = backend;
    }


    public JSONObject getVideoListJSON(JSONObject object,List<String> groups)
    {
        JSONObject json = new JSONObject();
        JSONArray cArray = new JSONArray();
        JSONArray mArray = new JSONArray();

        try {
            JSONArray fileArray = object.getJSONArray("files");
            boolean found;
            for(String group : groups) {
                for (Content content : db.getContentDownloaded(group)) {
                    found = false;
                    String name = content.getName() + "." + content.getId();
                    for (int i = 0; i < fileArray.length(); i++) {
                        //G.Log(TAG,"Content "+name+" "+fileArray.getString(i));
                        if (name.equals(fileArray.getString(i))) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) cArray.put(name);
                }
            }
            for (int i = 0; i < fileArray.length(); i++) {
                found = false;
                for(String group : groups) {
                    for (Content content : db.getContentDownloaded(group)) {
                        String name = content.getName() + "." + content.getId();
                        if (name.equals(fileArray.getString(i))) {
                            found = true;
                            break;
                        }
                    }
                }
                if(!found)mArray.put(fileArray.getString(i));
            }
            if(cArray.length()>0)
                json.put("service_response",cArray);
            else
                json.put("service_response","OK");

            if(mArray.length()>0)
                json.put("missing_response",mArray);
            else
                json.put("missing_response","OK");
        }catch (JSONException j){
            G.Log(TAG,"JSONException "+j);
        }
        return json;
    }

    public JSONObject getVideoListOnlyLocalJSON(JSONObject object)
    {
        JSONObject json = new JSONObject();
        JSONArray cArray = new JSONArray();
        JSONArray mArray = new JSONArray();

        for(Group group : db.getGroups())
        {
            try
            {
                JSONArray fileArray = object.getJSONArray(group.getName());
                if (fileArray != null)
                {
                    boolean found;
                    for (Content content : db.getContentDownloaded(group.getName()))
                    {
                        found = false;
                        String name = content.getName() + "." + content.getId();
                        for (int i = 0; i < fileArray.length(); i++) {
                            //G.Log(TAG, "Content " + group.getName() + " " + name + " " + fileArray.getString(i));
                            if (name.equals(fileArray.getString(i))) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) cArray.put(name);
                    }

                    for (int i = 0; i < fileArray.length(); i++) {
                        found = false;
                        for (Content content : db.getContentDownloaded()) {
                            String name = content.getName() + "." + content.getId();
                            if (name.equals(fileArray.getString(i))) {
                                found = true;
                                break;
                            }
                        }

                        if (!found) mArray.put(fileArray.getString(i));
                    }
                    if (cArray.length() > 0)
                        json.put("service_response", cArray);
                    else
                        json.put("service_response", "OK");

                    if (mArray.length() > 0)
                        json.put("missing_response", mArray);
                    else
                        json.put("missing_response", "OK");
                }
            } catch(JSONException j){
                G.Log(TAG, "JSONException " + j);
            }

        }
        return json;
    }

    public JSONObject getVideoListJSON(JSONObject object)
    {
        JSONObject json = new JSONObject();
        JSONArray cArray = new JSONArray();
        JSONArray mArray = new JSONArray();

        try {
            JSONArray fileArray = object.getJSONArray("files");
            boolean found;
            for (Content content : db.getContentDownloaded()) {
                found = false;
                String name = content.getName() + "." + content.getId();
                for (int i = 0; i < fileArray.length(); i++) {
                    //G.Log(TAG,"Content "+name+" "+fileArray.getString(i));
                    if (name.equals(fileArray.getString(i))) {
                        found = true;
                        break;
                    }
                }
                if (!found) cArray.put(name);
            }

            for (int i = 0; i < fileArray.length(); i++) {
                found = false;
                for (Content content : db.getContentDownloaded()) {
                    String name = content.getName() + "." + content.getId();
                    if (name.equals(fileArray.getString(i))) {
                        found = true;
                        break;
                    }
                }

                if(!found)mArray.put(fileArray.getString(i));
            }
            if(cArray.length()>0)
                json.put("service_response",cArray);
            else
                json.put("service_response","OK");

            if(mArray.length()>0)
                json.put("missing_response",mArray);
            else
                json.put("missing_response","OK");
        }catch (JSONException j){
            G.Log(TAG,"JSONException "+j);
        }
        return json;
    }

    public JSONObject getVideoListJSON(List<Group> groups){

        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();

        try {
            for(Group group : groups) {
                for (Content content : db.getContentDownloaded(group.getName())) {
                    array.put(content.getName() + "." + content.getId());
                }
                json.put(group.getName(),array);

            }
        }catch (Exception e){G.Log(TAG,"getVideosList error "+e);}

        return json;
    }

    public JSONObject getVideoListJSON(){

        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();

        try {
            for(Content content: db.getContentDownloaded())
            {
                array.put(content.getName()+"."+content.getId());
            }
            json.put("files",array);
        }catch (Exception e){G.Log(TAG,"getVideosList error "+e);}

        return json;

    }



    public void flagDownloaded(String uri, int id,String group)
    {
        db.setContentDownloaded(uri,group,id);
    }

    public byte[] readFile(String fileName){
        File file = new File(context.getExternalFilesDir(Config.FOLDER), fileName);

        byte[] buffer = new byte[(int) file.length()];
        InputStream ios = null;
        try {
            ios = new FileInputStream(file);
            if (ios.read(buffer) == -1) {
                G.Log(TAG, "EOF reached while trying to read the whole file");
            }
        }catch (FileNotFoundException e){
            G.Log(TAG, "FileNotFoundException " +e);

        } catch (IOException e){
            G.Log(TAG, "IOException "+e);

        } finally{
            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
            }
        }
        return buffer;
    }

    /*public byte[] readFile(String fileName)
    {
        File file = new File(context.getExternalFilesDir("share_it"), fileName);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] bytes=new byte[1024];
        try {
            bytes = Files.readAllBytes(file.toPath());
        }catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }*/

    public void saveFile(String filename, String name, byte[] msg,int id, int total,String group,long millis) {
        /*Intent broadcast = new Intent(MainActivity.NEW_CHUNK_RECEIVED);
        broadcast.putExtra("group", group);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);*/
        backend.fileReceived(new Date(),filename,group,id,msg.length,millis);
        Content c = new Content(name, name,"","",id,total,"",group);
        db.addContent(c);
        flagDownloaded(name,id,group);
        G.Log(TAG,"Savefile "+filename +" "+id+" "+total+" "+group+" "+db.getReceived(name,group));
        //if(db.getReceived(name,group) == total)
        new SaveFile(context,msg,group,listener).execute(filename,name);
    }

    public Content getContent(String filename) {
        List<Content> c = db.getContent(filename);

        return !c.isEmpty()? c.get(0) : null;
    }
}

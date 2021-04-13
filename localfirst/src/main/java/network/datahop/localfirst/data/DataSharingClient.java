/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.data;

import android.content.Context;
import android.util.Log;

import androidx.core.util.Pair;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import network.datahop.localfirst.backend.DataHopBackend;
import network.datahop.localfirst.net.DiscoveryListener;
import network.datahop.localfirst.net.wifi.WifiLink;
import network.datahop.localfirst.utils.Config;
import network.datahop.localfirst.utils.G;

import static java.lang.Thread.sleep;

//import java.util.Date;

public class DataSharingClient  {


    public static final String NEW_VIDEO_RECEIVED = "new_video_received";
    public static final String NEW_CHUNK_RECEIVED = "new_chunk_received";

    public static final String TAG = "DataSharingClient";
    //public static final String DOWNLOAD_COMPLETED = "dl_completed";
    //DataHopBackend backend;
    //Application context
    Context context;
    //instance of the main background service
    //DataHopConnectivityService service;

    String address;
    private RequestQueue queue;

    ContentDelivery cd;

    //long date;

    private String userId;

    private int count;
    private final int total=5;

    List<String> pendingDownloadContent;
    List<HashMap<String,String>> pendingUploadHeaders;
    WifiLink link;

    //SettingsPreferences pref;
    ContentDatabaseHandler db;
    DiscoveryListener listener;
    DataHopBackend backend;
    boolean anyGroupSharing;

    public DataSharingClient(Context context, WifiLink link, DiscoveryListener listener,DataHopBackend backend,boolean anyGroupSharing){
        this.context = context;
        //service = (DataHopConnectivityService)context;
        cd = new ContentDelivery(context,listener,backend);
        //this.backend = backend;
        queue = Volley.newRequestQueue(context);
        this.link = link;
        //pref = new SettingsPreferences(context);
        db = new ContentDatabaseHandler(context);
        this.listener = listener;
        count=0;
        this.anyGroupSharing = anyGroupSharing;
    }

    public void start(String userId, String address){
        this.userId = userId;
        this.address=address;
        if(!anyGroupSharing)
            sendList(cd.getVideoListJSON(db.getGroups()));
        else
            sendList(cd.getVideoListJSON());
        //sendList(cd.getVideoListJSON());
        //todo sendList(cd.getVideoListJSON());
        pendingDownloadContent = new ArrayList<>();
        pendingUploadHeaders = new ArrayList<>();
    }

    private void downloadFile(){
        G.Log(TAG,"Download file "+pendingDownloadContent.get(0));
        StreamVolleyRequest streamVolleyRequest = new StreamVolleyRequest(Request.Method.GET, pendingDownloadContent.remove(0),
                new Response.Listener<Pair<byte[], Map<String, String>>>() {
                    @Override
                    public void onResponse(Pair<byte[], Map<String, String>> response) {
                        G.Log(TAG, "Volley response ");
                        if (response != null) {
                            G.Log(TAG, "Volley response2 " + response.toString()+" "+pendingDownloadContent.size());
                            receiveVideo(response.first, response.second);
                            if(pendingDownloadContent.size()==0&&pendingUploadHeaders.size()==0){
                                //sleep(2000);
                                G.Log(TAG, "No pending content");
                                //service.disconnect();
                                link.disconnect();

                            } if(pendingDownloadContent.size()>0) {
                                downloadFile();
                            }
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                G.Log(TAG, "UNABLE TO DOWNLOAD THE FILE: " + error.getMessage()+" "+error.networkResponse);
                //service.disconnect();
                link.disconnect();
            }
        }, null);
        queue.add(streamVolleyRequest);
    }

    private void uploadFile(){
        HashMap<String,String> headers = pendingUploadHeaders.get(0);
        byte[] f = cd.readFile(headers.get("filename"));
        G.Log(TAG,"upload file "+f.length+" "+f);
        UploadFileRequest fileRequest = new UploadFileRequest(Request.Method.POST,  getUploadUrl(), f, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                G.Log(TAG, "File uploaded "+response);
                pendingUploadHeaders.remove(0);
                if(pendingUploadHeaders.size()==0&&pendingDownloadContent.size()==0)//service.disconnect();
                {
                    G.Log(TAG, "No pending content");
                    link.disconnect();
                }
                if(pendingUploadHeaders.size()>0) {
                    uploadFile();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                G.Log(TAG, "ERROR uploadfile: " + error.toString());
                //service.disconnect();
                link.disconnect();
            }
        },headers);
        fileRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 30000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        queue.add(fileRequest);
    }

    private void sendList(final JSONObject jsonBody) {
        G.Log(TAG, "Sending JSON LIST: " + jsonBody.length()+" "+jsonBody.toString()+" "+getListUrl());
        //try{sleep(3000);}catch (Exception e){};
        JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.POST, getListUrl(), jsonBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    boolean noFiles=false;
                    G.Log(TAG, "File received "+response);
                    if(response.get("service_response").equals("OK")){
                        // NO REQUEST REQUIRED
                        G.Log(TAG, "No files to download");
                        noFiles=true;
                    } else {
                        JSONArray jarr = (JSONArray) response.get("service_response");
                        String url = getDownloadUrl();
                        if(jarr.length() > 0){
                            for (int i = 0; i < jarr.length(); i++) {
                                String downloadUrl = url + "file=" + jarr.get(i).toString();
                                pendingDownloadContent.add(downloadUrl);
                            }
                            downloadFile();

                        }
                    }

                    if(response.get("missing_response").equals("OK")){
                        // NO REQUEST REQUIRED
                        G.Log(TAG, "No files to upload");
                        if(noFiles)link.disconnect();
                    } else {
                        JSONArray jarr = (JSONArray) response.get("missing_response");
                        if(jarr.length() > 0){
                            for (int i = 0; i < jarr.length(); i++) {
                                HashMap<String,String> headers = new HashMap<>();
                                String filename = jarr.get(i).toString();
                                String cName = filename.substring(0,filename.lastIndexOf("."));
                                String id = filename.substring(filename.lastIndexOf(".") + 1);
                                G.Log(TAG,"Send "+cName);
                                Content c = cd.getContent(cName);
                                headers.put("name",c.getName());
                                headers.put("filename",filename);
                                headers.put("id",String.valueOf(id));
                                headers.put("total",String.valueOf(c.getTotal()));
                                //headers.put("address",pref.getAddress());
                                headers.put("group",c.getGroup());
                                headers.put("time",String.valueOf(System.currentTimeMillis()));

                                G.Log(TAG,"Headers "+headers);
                                pendingUploadHeaders.add(headers);
                            }
                            uploadFile();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    //service.disconnect();
                    link.disconnect();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "ERROR: " + error.toString());
                if(count<total){
                    count++;
                    try {
                        sleep(Config.HTTP_TIMEOUT);
                        //todo sendList(cd.getVideoListJSON());
                        if(!anyGroupSharing)
                            sendList(cd.getVideoListJSON(db.getGroups()));
                        else
                            sendList(cd.getVideoListJSON());
                    }catch (InterruptedException e){
                        G.Log(TAG,"InterruptedException "+e);
                        //service.disconnect();
                        link.disconnect();
                    }
                } else {
                    count = 0;
                    G.Log(TAG,"Count 0 disconnect");
                    //service.disconnect();
                    link.disconnect();
                }
                //onBackPressed();

            }
        });
        queue.add(jsonObject);

    }

    private void receiveVideo(byte[] msg,Map<String,String> headers) {

        String content = headers.get("Content-Disposition").toString();
        G.Log(TAG,"File received "+headers.get("Id")+" "+headers.get("Total"));
        if (msg!=null) {
            try {
                //Read file name from headers
                String[] arrTag = content.split("=");
                String filename = arrTag[1];
                filename = filename.replace(":", ".");
                filename = filename.replace("\"", "");
                cd.saveFile(filename,headers.get("Name"),msg,Integer.parseInt(headers.get("Id")),Integer.parseInt(headers.get("Total")),headers.get("group"),System.currentTimeMillis()-Long.parseLong(headers.get("time")));
                //G.Log(TAG, "Received video now:" + (new Date()).getTime() + " before:" + date + " diff " + diffInMillies);
                //date = (new Date()).getTime();
                //backend.fileReceived(new Date(), filename, msg.length, System.currentTimeMillis()-Long.parseLong(headers.get("time")));

                /*Intent broadcast = new Intent(NEW_CHUNK_RECEIVED);
                broadcast.putExtra("message", "New file received "+filename);
                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);*/

            } catch (IndexOutOfBoundsException e) {
                G.Log(TAG, "IndexOutOfBoundsException " + e);
                //service.disconnect();
                link.disconnect();
            }
        }

    }

    private String getListUrl()
    {
        if(address!=null)
            return "http://"+address+":"+Config.port+"/upload-list";
        else
            return null;
    }

    private String getDownloadUrl()
    {
        if(address!=null)
            return "http://"+address+":"+Config.port+"/getFile?";
        else
            return null;
    }

    private String getUploadUrl()
    {
        if(address!=null)
            return "http://"+address+":"+Config.port+"/uploadFile";
        else
            return null;
    }




}

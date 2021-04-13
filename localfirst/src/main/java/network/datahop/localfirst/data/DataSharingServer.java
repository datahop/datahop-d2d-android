/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.data;

import android.content.Context;

import network.datahop.localfirst.backend.DataHopBackend;
import network.datahop.localfirst.net.DiscoveryListener;
import network.datahop.localfirst.utils.Config;
import network.datahop.localfirst.utils.G;

public class DataSharingServer {

    public static final String NEW_VIDEO_RECEIVED = "new_video_received";
    public static final String DOWNLOAD_COMPLETED = "dl_completed";

    public static final String TAG = "DataSharingServer";

    Context context;
//    DataHopConnectivityService service;

    private HttpServer httpServer;
    private boolean isStarted = false;

    ContentDelivery cd;

    boolean anyGroupSharing;

    public DataSharingServer(Context context, DataHopBackend backend, DiscoveryListener listener,boolean  anyGroupSharing) {
        this.context = context;
        this.anyGroupSharing = anyGroupSharing;
//        service = (DataHopConnectivityService) context;
        cd = new ContentDelivery(context,listener,backend);
        //this.service = service;

    }

    //public void start(String userId){
    public void start(){
        //this.userId = userId;
        if (!isStarted && startAndroidWebServer()) {
            G.Log(TAG, "Starting server");
            isStarted = true;
        } else {
            G.Log(TAG, "Server already started");
        }
    }

    public void close(){
        stopAndroidWebServer();
    }


    //region Start And Stop HttpServer
    private boolean startAndroidWebServer() {
        if (!isStarted) {
            G.Log(TAG,"Start http server");
            int port = Integer.valueOf(Config.port);
            try {
                if (port == 0) {
                    throw new Exception();
                }
                httpServer = new HttpServer(port,cd,context,anyGroupSharing);
                httpServer.start();

                G.Log(TAG, "Port "+httpServer.getHostname()+" "+httpServer.getListeningPort()+" "+httpServer.isAlive());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean stopAndroidWebServer() {
        G.Log(TAG,"Stopandroidserver "+isStarted+" "+httpServer);
        if (isStarted && httpServer != null) {
            G.Log(TAG,"Stop httpserver");
            httpServer.stop();
            isStarted = false;
            return true;
        }
        return false;
    }

}
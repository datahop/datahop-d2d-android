/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.net.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.os.Handler;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Collection;

import network.datahop.localfirst.net.DataHopService;
import network.datahop.localfirst.net.StatsHandler;
import network.datahop.localfirst.utils.Config;
import network.datahop.localfirst.utils.G;

import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION;


/**
 * Created by srenevic on 03/08/17.
 */
public class WifiDirectHotSpot implements ConnectionInfoListener,ChannelListener,GroupInfoListener {

    WifiDirectHotSpot that = this;
    Context context;


    private WifiP2pManager p2p;
    private WifiP2pManager.Channel channel;

    String mNetworkName = "";
    String mPassphrase = "";
    String mInetAddress = "";

    private BroadcastReceiver receiver;
    private IntentFilter filter;

    private StatsHandler stats;

    public static final String TAG = "WifiDirectHotSpot";

    boolean started;

    private boolean connected=false;

    Handler handler;

    Handler broadcastHandler;

   // SettingsPreferences timers;

    HotspotListener nListener;

    //JobParameters params;

    /** Interface for callback invocation on an application action */
    public interface StartStopListener {

        public void onSuccess();

        public void onFailure(int reason);
    }

    public WifiDirectHotSpot(Context Context, HotspotListener nListener, StatsHandler stats/*, SettingsPreferences timers, JobParameters params*/)
    {
        this.context = Context;
        this.stats = stats;
        handler = new Handler();
        broadcastHandler = new Handler();
        //this.timers = timers;
        started = false;
        this.nListener = nListener;
        //this.params = params;
    }

    public void start(StartStopListener listener){
        G.Log(TAG,"Trying to start");
        if(!started) {
            G.Log(TAG,"Start");
            started=true;

            p2p = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);

            if (p2p == null) {
                G.Log(TAG, "This device does not support Wi-Fi Direct");
            } else {

                channel = p2p.initialize(context, context.getMainLooper(), this);
                //setWifiChannel();
                receiver = new AccessPointReceiver();

                filter = new IntentFilter();
                filter.addAction(WIFI_P2P_STATE_CHANGED_ACTION);
                filter.addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION);

                try{
                    this.context.registerReceiver(receiver, filter);
                }catch (Exception e){G.Log(TAG,"leaked register");}

                p2p.createGroup(channel, new WifiP2pManager.ActionListener() {
                    public void onSuccess() {
                        G.Log(TAG, "Creating Local Group ");
                        listener.onSuccess();
                    }

                    public void onFailure(int reason) {
                        G.Log(TAG, "Local Group failed, error code " + reason);
                        listener.onFailure(reason);
                    }
                });
            }

        } else {
            G.Log(TAG,"Trying to set network");
            /*if(mNetworkName!=null&&mPassphrase!=null)
            {
                String action = NETWORK_READY;
                G.Log(TAG,"Set network");
                Intent broadcast = new Intent(action)
                        .putExtra("name", mNetworkName)
                        .putExtra("password", mPassphrase);
                context.sendBroadcast(broadcast);
            }*/
            nListener.setNetwork(mNetworkName,mPassphrase);
        }
    }

    public void stop(StartStopListener listener) {
        if(started)
        {
            G.Log(TAG,"Stop");
            stats.setHsSSID("");
            broadcastHandler.removeCallbacksAndMessages(null);
            handler.removeCallbacksAndMessages(null);
            this.context.unregisterReceiver(receiver);
            removeGroup(listener);
            started=false;
        } else {
            listener.onSuccess();
        }

    }

    public boolean isRunning()
    {
        return started;
    }

    public boolean isConnected() {return connected;}

    public void removeGroup(StartStopListener listener) {
        G.Log(TAG,"removegroup");
        p2p.removeGroup(channel,new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                G.Log(TAG,"Cleared Local Group ");
                if(listener!=null)listener.onSuccess();
            }

            public void onFailure(int reason) {
                G.Log(TAG,"Clearing Local Group failed, error code " + reason);
                if(listener!=null)listener.onFailure(reason);
            }
        });
    }

    public String getNetworkName(){
        return mNetworkName;
    }

    public String getPassphrase(){
        return mPassphrase;
    }

    public void startConnection()
    {
        Intent broadcast = new Intent(DataHopService.STOP_DIS_BLUETOOTH);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
        broadcastHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isConnected()) {
                    Intent broadcast = new Intent(DataHopService.START_DIS_BLUETOOTH);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
                }
            }
        }, Config.wifiConnectionWaitingTime);
    }
    @Override
    public void onChannelDisconnected() {
        // see how we could avoid looping
        //     p2p = (WifiP2pManager) this.context.getSystemService(this.context.WIFI_P2P_SERVICE);
        //     channel = p2p.initialize(this.context, this.context.getMainLooper(), this);
    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {

        try {
            Collection<WifiP2pDevice> devlist = group.getClientList();
            stats.setHsSSID(group.getNetworkName());

            int numm = 0;
            for (WifiP2pDevice peer : group.getClientList()) {
                numm++;
                G.Log(TAG,"Client " + numm + " : "  + peer.deviceName + " " + peer.deviceAddress);
            }
            stats.setHsClients(numm);
            if(numm>0&!connected){
                G.Log(TAG,"Client " + numm +" connect");
                connected=true;
                //service.setServer();
                broadcastHandler.removeCallbacksAndMessages(null);
                nListener.connected();
                //Intent broadcast = new Intent(DiscoveryService.STOP_DIS_BLUETOOTH);
                //LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
            }
            else if(numm==0&connected){
                G.Log(TAG,"Client " + numm +" disconnect");
                connected=false;
                nListener.disconnected();
                Intent broadcast = new Intent(DataHopService.START_DIS_BLUETOOTH);
                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
                stop(null);
                //mod service.stopHttpServer();
                //service.jobFinished(params,false);
                //Start();
                //service.serviceStopUbiCDN();
                //service.serviceStartUbiCDN();

            }

            if(mNetworkName.equals(group.getNetworkName()) && mPassphrase.equals(group.getPassphrase())){
                G.Log(TAG,"Already have local service for " + mNetworkName + " ," + mPassphrase);
            }else {

                mNetworkName = group.getNetworkName();
                mPassphrase = group.getPassphrase();

                /*String action = NETWORK_READY;

                Intent broadcast = new Intent(action)
                        .putExtra("name", mNetworkName)
                        .putExtra("password", mPassphrase);
                context.sendBroadcast(broadcast);*/
                nListener.setNetwork(mNetworkName,mPassphrase);
            }

        } catch(Exception e) {
            G.Log(TAG,"onGroupInfoAvailable, error: " + e.toString());
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        G.Log(TAG,"WifiP2pinfo received "+info);
        try {
            if (info.isGroupOwner) {
                mInetAddress = info.groupOwnerAddress.getHostAddress();
                G.Log(TAG, "inet address " + mInetAddress);
                p2p.requestGroupInfo(channel,this);
            } else {
                G.Log(TAG,"we are client !! group owner address is: " + info.groupOwnerAddress.getHostAddress());
            }
        } catch(Exception e) {
            G.Log(TAG,"onConnectionInfoAvailable, error: " + e.toString());
        }
    }

    private class AccessPointReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    //debug_print("We are connected, will check info now");
                    G.Log(TAG,"We are connected, will check info now");
                    p2p.requestConnectionInfo(channel, that);
                } else{
                    //debug_print("We are DIS-connected");
                    G.Log(TAG,"We are DIS-connected");
                }
            }
        }
    }



}
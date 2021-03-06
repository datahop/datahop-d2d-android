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
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import network.datahop.localfirst.backend.DataHopBackend;
import network.datahop.localfirst.net.StatsHandler;
import network.datahop.localfirst.utils.G;


/**
 * Created by srenevic on 03/08/17.
 */
public class WifiLink  {


    static final public int ConectionStateNONE = 0;
    static final public int ConectionStatePreConnecting = 1;
    static final public int ConectionStateConnecting = 2;
    static final public int ConectionStateConnected = 3;
    static final public int ConectionStateDisconnected = 4;

    private int  mConectionState = ConectionStateNONE, mPreviousState= ConectionStateNONE;

    public static final String TAG = "WifiLink";

    private boolean hadConnection = false;

    StatsHandler stats;
    WifiLinkListener listener;
    WifiManager wifiManager = null;
    WifiConfiguration wifiConfig = null;
    Context context = null;
    int netId = 0;
    WiFiConnectionReceiver receiver;
    private IntentFilter filter;
    String inetAddress = "";
    boolean connected=false;
    String ssid;

    Handler handler;
    // create a class member variable.
    WifiManager.WifiLock mWifiLock = null;
    //PowerManager.WakeLock wakeLock;
    //SettingsPreferences timers;

    Date started;

    String userId;

    DataHopBackend backend;

    long waitingTime;

public WifiLink(Context context, WifiLinkListener listener, StatsHandler stats, DataHopBackend backend,long waitingTime)
   {

        this.context = context;
        this.listener = listener;
        filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        receiver = new WiFiConnectionReceiver();
        //WIFI connection
        this.wifiManager = (WifiManager)this.context.getSystemService(Context.WIFI_SERVICE);
        handler = new Handler();
        this.stats = stats;
        //this.timers = timers;
        this.backend = backend;
        this.waitingTime = waitingTime;

    }

    public void connect(String SSID, String password,String ip,String userId){

        if(!connected&&(mConectionState==ConectionStateNONE||mConectionState==ConectionStateDisconnected)) {
            started = new Date();
            this.userId = userId;
            this.wifiConfig = new WifiConfiguration();
            this.wifiConfig.SSID = String.format("\"%s\"", SSID);
            this.wifiConfig.preSharedKey = String.format("\"%s\"", password);
            //G.Log(TAG, "WE HAVE REACHED HERE");
            ssid = this.wifiManager.getConnectionInfo().getSSID();
            this.wifiConfig.priority = 10000;
            G.Log(TAG,"Connected to "+ssid+" "+ip);
            List<WifiConfiguration> wifis = this.wifiManager.getConfiguredNetworks();
            boolean result;
            connected = true;
            hadConnection=false;
            if(wifis!=null) {
                for (WifiConfiguration wifi : wifis) {
                    if(wifi.SSID.startsWith("DIRECT-"))
                        result = this.wifiManager.removeNetwork(wifi.networkId);
                    else
                        result = this.wifiManager.disableNetwork(wifi.networkId);

                    G.Log(TAG,"Disable "+wifi.SSID+" "+result);

                }
            }
            if (this.wifiConfig  != null)
            {
                try
                {
                    setStaticIpConfiguration(wifiManager, wifiConfig,
                            InetAddress.getByName(ip), 24,
                            InetAddress.getByName("192.168.49.1"),
                            new InetAddress[] { InetAddress.getByName("192.168.49.1")});
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }


            //this.wifiManager.startScan();
            //this.wifiManager.disconnect();
            this.context.registerReceiver(receiver, filter);
            this.netId = this.wifiManager.addNetwork(this.wifiConfig);
            this.wifiManager.disconnect();
            this.wifiManager.enableNetwork(this.netId, true);
            boolean success = this.wifiManager.reconnect();
            stats.setWStatus("Connecting...");


            holdWifiLock();
            handler.removeCallbacksAndMessages(null);
            final String user = userId;
            handler.postDelayed(timeout,waitingTime);
            backend.connectionStarted(started);

        }
    }

    public void disconnect(){
        releaseWifiLock();
        handler.removeCallbacksAndMessages(null);
        G.Log(TAG,"Disconnect");
        try {
            this.context.unregisterReceiver(receiver);
        }catch (IllegalArgumentException e){G.Log(TAG,"Unregister failed "+e);}
        if(connected){
            connected = false;
            this.wifiManager.removeNetwork(this.netId);
            List<WifiConfiguration> wifis = this.wifiManager.getConfiguredNetworks();
            if(wifis!=null) {
                for (WifiConfiguration wifi : wifis) {
                    boolean attempt = false;
                    if (wifi.SSID.equals(ssid)) attempt = true;
                    boolean result = this.wifiManager.enableNetwork(wifi.networkId, attempt);
                    G.Log(TAG,"Wifi enable "+wifi.SSID + " "+result);

                }
            }
            mConectionState=ConectionStateNONE;
            mPreviousState=ConectionStateNONE;
            G.Log(TAG,"Report disconnection");
            listener.wifiLinkDisconnected();

        }


    }

    public void SetInetAddress(String address){
        this.inetAddress = address;
    }

    public String GetInetAddress(){
        return this.inetAddress;
    }


    /***
     * Calling this method will aquire the lock on wifi. This is avoid wifi
     * from going to sleep as long as <code>releaseWifiLock</code> method is called.
     **/
    private void holdWifiLock() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        /*PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();*/
        if( mWifiLock == null )
            mWifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, TAG);

        mWifiLock.setReferenceCounted(false);

        if( !mWifiLock.isHeld() )
            mWifiLock.acquire();
    }

    /***
     * Calling this method will release if the lock is already help. After this method is called,
     * the Wifi on the device can goto sleep.
     **/
    private void releaseWifiLock() {

        if( mWifiLock == null )
            Log.w(TAG, "#releaseWifiLock mWifiLock was not created previously");

        if( mWifiLock != null && mWifiLock.isHeld() ){
            mWifiLock.release();
        }
        //wakeLock.release();


    }


    @SuppressWarnings("unchecked")
    private static void setStaticIpConfiguration(WifiManager manager, WifiConfiguration config, InetAddress ipAddress, int prefixLength, InetAddress gateway, InetAddress[] dns) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException, InstantiationException
    {
        // First set up IpAssignment to STATIC.
        Object ipAssignment = getEnumValue("android.net.IpConfiguration$IpAssignment", "STATIC");
        callMethod(config, "setIpAssignment", new String[] { "android.net.IpConfiguration$IpAssignment" }, new Object[] { ipAssignment });

        // Then set properties in StaticIpConfiguration.
        Object staticIpConfig = newInstance("android.net.StaticIpConfiguration");
        Object linkAddress = newInstance("android.net.LinkAddress", new Class<?>[] { InetAddress.class, int.class }, new Object[] { ipAddress, prefixLength });

        setField(staticIpConfig, "ipAddress", linkAddress);
        setField(staticIpConfig, "gateway", gateway);
        getField(staticIpConfig, "dnsServers", ArrayList.class).clear();
        for (int i = 0; i < dns.length; i++)
            getField(staticIpConfig, "dnsServers", ArrayList.class).add(dns[i]);

        callMethod(config, "setStaticIpConfiguration", new String[] { "android.net.StaticIpConfiguration" }, new Object[] { staticIpConfig });
        manager.updateNetwork(config);
        manager.saveConfiguration();
    }


    private static Object newInstance(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException
    {
        return newInstance(className, new Class<?>[0], new Object[0]);
    }

    private static Object newInstance(String className, Class<?>[] parameterClasses, Object[] parameterValues) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException
    {
        Class<?> clz = Class.forName(className);
        Constructor<?> constructor = clz.getConstructor(parameterClasses);
        return constructor.newInstance(parameterValues);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object getEnumValue(String enumClassName, String enumValue) throws ClassNotFoundException
    {
        Class<Enum> enumClz = (Class<Enum>)Class.forName(enumClassName);
        return Enum.valueOf(enumClz, enumValue);
    }

    private static void setField(Object object, String fieldName, Object value) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException
    {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.set(object, value);
    }

    private static <T> T getField(Object object, String fieldName, Class<T> type) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException
    {
        Field field = object.getClass().getDeclaredField(fieldName);
        return type.cast(field.get(object));
    }

    private static void callMethod(Object object, String methodName, String[] parameterTypes, Object[] parameterValues) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
    {
        Class<?>[] parameterClasses = new Class<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++)
            parameterClasses[i] = Class.forName(parameterTypes[i]);

        Method method = object.getClass().getDeclaredMethod(methodName, parameterClasses);
        method.invoke(object, parameterValues);
    }

    Runnable timeout = new Runnable() {
        @Override
        public void run() {
            //Do something after 100ms
            if (!hadConnection) {
                G.Log(TAG, "timeout");
                backend.connectionFailed(started, new Date());
                disconnect();
                stats.setWStatus("Disconnected");
                int conn = stats.getConnections();
                stats.setConnectionsFailed(++conn);
                listener.timeout();
            }
        }
    };

    private class WiFiConnectionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if(info != null) {
                    mPreviousState = mConectionState;
                    if (info.isConnected()) {
                        mConectionState = ConectionStateConnected;
                    }else if(info.isConnectedOrConnecting()) {
                        mConectionState = ConectionStateConnecting;
                    }else {
                        if(hadConnection){
                            mConectionState = ConectionStateDisconnected;
                        }else{
                            mConectionState = ConectionStatePreConnecting;
                        }
                    }

                    G.Log(TAG,"DetailedState: " + info.getDetailedState());

                    String conStatus = "";
                    if(mConectionState == WifiLink.ConectionStateNONE) {
                        conStatus = "NONE";
                    }else if(mConectionState == WifiLink.ConectionStatePreConnecting) {
                        conStatus = "PreConnecting";
                    }else if(mConectionState == WifiLink.ConectionStateConnecting) {
                        conStatus = "Connecting";
                    }else if(mConectionState == WifiLink.ConectionStateConnected) {
                        conStatus = "Connected";
                    }else if(mConectionState == WifiLink.ConectionStateDisconnected) {
                        conStatus = "Disconnected";
                        G.Log(TAG,"Had connection "+hadConnection);

                        if(hadConnection)disconnect();

                    }
                    G.Log(TAG, "Status " + conStatus);

                }

                WifiInfo wiffo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);

                if(wiffo==null)G.Log(TAG,"Wiifiinfo null "+wifiManager.getConnectionInfo());
                if(wiffo==null&&mConectionState==ConectionStateConnected)wiffo = wifiManager.getConnectionInfo();
                if(wiffo!=null)G.Log(TAG,"Wifiinfo "+wiffo.getSSID()+" "+ConectionStateConnected+" "+mConectionState+" "+hadConnection+" "+wifiConfig.SSID);
                if(wiffo!=null&&mConectionState==ConectionStateConnected&&mPreviousState==ConectionStateConnecting){
                    handler.removeCallbacks(timeout);
                    if(wiffo.getSSID().equals(wifiConfig.SSID)&&!hadConnection) {
                        //G.Log(TAG, "Ip address: " + wiffo.getIpAddress());
                        //G.Log(TAG, "Create face to " + inetAddress);
                        hadConnection=true;
                        G.Log(TAG, "Connected to " + wiffo);
                        int ip = wiffo.getIpAddress();
                        int gateway = wifiManager.getDhcpInfo().gateway;
                        try {
                            String ipAddress = InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ip).array()).getHostAddress();
                            String gwAddress = InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(gateway).array()).getHostAddress();
                            G.Log(TAG, "IP  " + ipAddress + " " + gwAddress);
                            stats.setWStatus("Connected");
                            listener.wifiLinkConnected(gwAddress);
                        }catch (UnknownHostException e){}
                        backend.connectionCompleted(started,new Date(),wiffo.getRssi(),wiffo.getLinkSpeed(),wiffo.getFrequency());
                    } else if(!wiffo.getSSID().equals(wifiConfig.SSID)) {
                        G.Log(TAG, "Not connected");
                        listener.wifiLinkDisconnected();
                    }

                }
            }
        }
    }


}

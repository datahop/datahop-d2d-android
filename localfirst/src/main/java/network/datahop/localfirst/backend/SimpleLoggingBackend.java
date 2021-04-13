/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.backend;

        import android.Manifest;
        import android.bluetooth.BluetoothAdapter;
        import android.content.Context;
        import android.content.pm.PackageManager;
        import android.location.Location;
        import android.net.Uri;
        import android.os.Build;
        import android.os.Bundle;
        import android.provider.Settings;
        import android.util.Log;

        import androidx.annotation.NonNull;
        import androidx.annotation.Nullable;


        import java.text.SimpleDateFormat;
        import java.util.Date;
        import java.util.HashMap;
        import java.util.Map;

        import network.datahop.localfirst.utils.G;
        import network.datahop.localfirst.utils.Util;


public class SimpleLoggingBackend extends DataHopBackend {

    //App app;
    //FirebaseAnalytics mFirebaseAnalytics;
    SimpleDateFormat dateFormat;
    String userId;
    private static String TAG="SimpleLoggingBackend";
    Context context;

    String name;
    //SettingsPreferences pref;
    public SimpleLoggingBackend(Context context){
        // app = (App)(context.getApplicationContext());
        // mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        this.context = context;

        //pref = new SettingsPreferences(context);
        try{
            name = new String(Settings.Secure.getString(context.getContentResolver(), "bluetooth_name").getBytes("UTF-8"));
        }catch (Exception e){
            G.Log("Getname exception "+e);}
    }

    public void setUserId(String user)
    {
        this.userId = user;
    }


    @Override
    public void serviceStarted(Date startTime)
    {
        G.Log(TAG,"serviceStarted "+userId+" "+startTime+" "+name);
    }

    @Override
    public void serviceStopped(Date stopTime)
    {

        G.Log(TAG,"serviceStopped "+userId+" "+stopTime+" "+name);

    }

    @Override
    public void fileSent(Date date, String videoName)
    {
        G.Log(TAG,"fileSent "+userId+" "+date+" "+videoName);

    }

    @Override
    public void fileReceived(Date date, String fileName,String group, int id, int size, long transferTime)
    {
        G.Log(TAG,"fileReceived "+userId+" "+date+" "+fileName+" "+group+" "+id+" "+size+" "+transferTime);

    }


    @Override
    public void serviceDiscovered(Date date, String serviceName, int success)
    {

        G.Log(TAG,"Service discovered "+userId);

    }

    @Override
    public void connectionStarted(Date date)
    {

        G.Log(TAG,"connectionStarted "+userId+" "+date);

    }

    @Override
    public void connectionCompleted(Date started, Date completed, int rssi, int speed, int freq)
    {

        G.Log(TAG,"connectionCompleted "+userId+" "+completed+" "+rssi+" "+speed+" "+freq);



    }

    @Override
    public void connectionFailed(Date started, Date failed)
    {
        G.Log(TAG,"connectionFailed "+userId+" "+failed);

    }





}

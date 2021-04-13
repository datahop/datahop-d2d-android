package network.datahop.localfirst;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import network.datahop.localfirst.data.Chunking;
import network.datahop.localfirst.data.Content;
import network.datahop.localfirst.data.ContentDatabaseHandler;
import network.datahop.localfirst.data.Group;
//import network.datahop.localfirst.net.DataHopServer;
//import network.datahop.localfirst.net.DataHopClientService;
import network.datahop.localfirst.data.NewContentEvent;
import network.datahop.localfirst.data.NewDataEvent;
import network.datahop.localfirst.data.NewUserEvent;
import network.datahop.localfirst.net.DataHopService;
import network.datahop.localfirst.utils.*;

public class LocalFirstSDK {

    public static final String CHANNEL_ID = "DataHopServiceChannel";

    //public static final String CLEAR_VIDEOS = "clear";
    public static final String NEW_CHUNK_RECEIVED = "new_chunk_received";
    public static final String DIRECT_CONNECTION = "direct_connection";
    public static final String DIRECT_CONNECTION_ACCEPTED = "direct_connection_accepted";
    public static final String DIRECT_CONNECTION_REJECTED = "direct_connection_rejected";
    public static final String NOT_SUPPORTED = "not_supported";
    public static final String STOPFOREGROUND_ACTION = "stop_service";
    public static final String STARTFOREGROUND_ACTION = "start_service";

    public static final String ACTION_ACCEPT = "action_accept";
    public static final String ACTION_REJECT = "action_reject";

    public static final String USER = "user";

    public static final String ACCEPTANCE = "acceptance";
    public static final int STOP_SERVICE = 0;
    public static final int RESUMED = 1;
    public static final int PAUSED = 2;
    public static final int CHECK_STATUS = 3;
    public static final int NEW_CONTENT = 4;
    public static final int NEW_CHUNK = 5;
    //public static final int NOT_SUPPORTED = 6;

    private static LocalFirstSDK a;

    public static final String TAG="LocalFirst";

    LocalFirstListener listener;

    //DataHopServer server;

    public static LocalFirstSDK getInstance() {
        if (a == null)
            throw new IllegalStateException("LocalFirst must be initialized before trying to reference it.");
        return a;
    }

    /*@VisibleForTesting(otherwise = 2)
    private static void b(Context paramContext, BridgefyClient paramBridgefyClient) {
        synchronized (LocalFirstSDK.class) {
            setInstance((new a(paramContext, paramBridgefyClient)).a());
        }
    }*/

    private static void setInstance(LocalFirstSDK paramLocalFirst) {
        a = paramLocalFirst;
    }

    public static void init(Context context)
    {
        //AUTHENTICATE
        setInstance(new LocalFirstSDK());
    }

    public static void start(Context context,String userName, boolean isScanning,long scanTime, long btIdleFgTime, long btIdleBgTime, long hotspotRestartTime, LocalFirstListener listener)
    {
        EventBus.getDefault().register(getInstance());
        //Messenger messengerIncoming,messengerAdvIncoming;
        //this.listener = listener;
        getInstance().setLocalFirstListener(listener);
        //getInstance().getDataHopServer().start();
        //ClientHandler mHandler = new ClientHandler(this);
        createNotificationChannel(context);
        G.Log(TAG,"Starting service "+isScanning+" "+ Build.VERSION.SDK_INT+" "+Build.VERSION_CODES.P);
        Intent serviceIntent = new Intent(context, DataHopService.class);
        serviceIntent.putExtra("isScanning", isScanning);
        serviceIntent.putExtra("scanTime", scanTime);
        serviceIntent.putExtra("btIdleFgTime", btIdleFgTime);
        serviceIntent.putExtra("btIdleBgTime", btIdleBgTime);
        serviceIntent.putExtra("hotspotRestartTime", hotspotRestartTime);
        serviceIntent.putExtra("userName", userName);
        serviceIntent.setAction(STARTFOREGROUND_ACTION);
        ContextCompat.startForegroundService(context, serviceIntent);


    }
    public static void stop(Context context){
        G.Log(TAG,"Stop services");
        //getInstance().getDataHopServer().start();
        Intent stopIntent = new Intent(context, DataHopService.class);
        stopIntent.setAction(STOPFOREGROUND_ACTION);
        ContextCompat.startForegroundService(context, stopIntent);
    }

    public static void addFile(Context context,String fileName,String path, String group)
    {

        try {
            G.Log(TAG, "Filename " + fileName+" "+ Chunking.getTotalParts(path)+" "+group);
            Chunking.split(path, group, context);
        } catch (IOException f) {
            G.Log(TAG, "IO error " + f);
        }

        Intent broadcast = new Intent(DataHopService.RESTART);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
    }

    public static List<String> getUsers()
    {
        List<String> users = new ArrayList<>();
        return users;
    }

    @Subscribe
    public void onEvent(NewContentEvent event){
        listener.newFileReceived(event.getFileName());
    }

    @Subscribe
    public void onEvent(NewDataEvent event){
        //Toast.makeText(this, event.getMessage(), Toast.LENGTH_SHORT).show();
        listener.newDataDiscovered(event.getNetworkName());
    }

    @Subscribe
    public void onEvent(NewUserEvent event){
        //Toast.makeText(this, event.getMessage(), Toast.LENGTH_SHORT).show();
        listener.newUSerDiscovered(event.getUserName());
    }

    public static void setUser(String userName)
    {

    }

    public static boolean addGroup(Context context,String groupName)
    {
        ContentDatabaseHandler db = new ContentDatabaseHandler(context);
        Group group = new Group();
        //group.setGroupId(0);
        group.setName(groupName);
        G.Log(TAG,"Add group "+System.currentTimeMillis());
        group.setTimestamp(System.currentTimeMillis());
        if (!db.getGroups().contains(group)) {
            db.addGroup(group);
            return true;
        } else
            return false;

    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "DataHop Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public void setLocalFirstListener(LocalFirstListener listener) {
        this.listener = listener;
    }


    private static void deleteVideos(Context ctx,String group)
    {
        File dir = ctx.getExternalFilesDir(Config.FOLDER);
        File[] subFiles = dir.listFiles();

        ContentDatabaseHandler db = new ContentDatabaseHandler(ctx);
        List<Content> content = db.getGroupContent(group);
        G.Log(TAG,"Files " +subFiles.length+" "+content.size());
        for (Content cn : content) {
            // Writing Contacts to log
            String name = cn.getName();
            String chunkName=name+"."+cn.getId();
            G.Log(TAG,"Name:"+ name+ " desc:" + cn.getText() + " url:"+cn.getUrl());
            if (subFiles != null) {
                //G.Log("Files " +subFiles);
                for (File file : subFiles) {
                    G.Log("Filename " + cn.getName() + " " +file.getAbsolutePath()+" "+file.getName()+" "+file.length());
                    if (file.getName().equals(name)||file.getName().equals(chunkName)) file.delete();
                }
            }
            db.rmContent(cn.getUri(),group);

        }

    }


}

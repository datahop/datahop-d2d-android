/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.net;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

//import network.datahop.localfirst.MainActivity;
//import network.datahop.localsharing.R;
import network.datahop.localfirst.LocalFirstSDK;
import network.datahop.localfirst.R;
import network.datahop.localfirst.backend.DataHopBackend;
//import network.datahop.localfirst.backend.DataHopBackend.OnFileListener;
//import network.datahop.localfirst.backend.DownloadFile;
import network.datahop.localfirst.backend.SimpleLoggingBackend;
import network.datahop.localfirst.data.Content;
import network.datahop.localfirst.data.ContentAdvertisement;
import network.datahop.localfirst.data.ContentDatabaseHandler;
import network.datahop.localfirst.data.DataSharingClient;
import network.datahop.localfirst.data.DataSharingServer;
import network.datahop.localfirst.data.Group;
import network.datahop.localfirst.data.NewContentEvent;
import network.datahop.localfirst.net.ble.BLEServiceDiscovery;
import network.datahop.localfirst.net.ble.GattServerCallback;
import network.datahop.localfirst.net.wifi.HotspotListener;
import network.datahop.localfirst.net.wifi.WifiDirectHotSpot;
import network.datahop.localfirst.net.wifi.WifiLink;
import network.datahop.localfirst.net.wifi.WifiLinkListener;
import network.datahop.localfirst.utils.Config;
import network.datahop.localfirst.utils.G;

import static android.bluetooth.le.AdvertiseSettings.ADVERTISE_MODE_BALANCED;
import static android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM;
import static java.util.UUID.nameUUIDFromBytes;
import static network.datahop.localfirst.net.ble.Constants.CHARACTERISTIC_DIRECT_UUID;

public class DataHopService extends Service implements /*OnFileListener,*/ LinkListener, WifiLinkListener, DiscoveryListener, HotspotListener {

    private static final String TAG = DataHopService.class.getSimpleName();
    public static final String CHANNEL_ID = "DataHopServiceChannel";

    public static final String START_ADV_BLUETOOTH = "Datahop_start_adv_bluetooth";
    public static final String STOP_ADV_BLUETOOTH = "Datahop_stop_adv_bluetooth";
    public static final String STOP_DIS_BLUETOOTH  = "Datahop_stop_discovery_bluetooth";
    public static final String START_DIS_BLUETOOTH = "Datahop_start_discovery_bluetooth";
    public static final String NOT_SUPPORTED = "Datahop_not_supported";
    public static final String STOP = "Datahop_stop";
    public static final String RESTART = "Datahop_restart";

    private BluetoothLeAdvertiser adv;
    private AdvertiseCallback advertiseCallback;
    GattServerCallback serverCallback;
    BluetoothGattServer mBluetoothGattServer;
    BluetoothManager manager;
    BluetoothAdapter btAdapter;
    WifiDirectHotSpot hotspot;
    ParcelUuid SERVICE_UUID;
    ContentDatabaseHandler db;
    BLEServiceDiscovery bleScan;
    StatsHandler stats;
    DataHopBackend backend;
    WifiLink mWifiLink;
    long scanTime,btIdleFgTime,btIdleBgTime,hotspotRestartTime;
    boolean isScanning;
    //SettingsPreferences timers;
    DataSharingClient mDataSharingClient;
    DataSharingServer mDataSharingServer;
    BroadcastReceiver mBroadcastReceiver;
    String userName;
    private boolean m_isConnected = false;

    //boolean stop = false,
    boolean exit = false;

    //int repeat;
    PowerManager.WakeLock wakeLock;
    Handler mHandler;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, DataHopService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("DataHop Service")
                // .setContentText(input)
                .setSmallIcon(R.drawable.ic_icon)
                .setContentIntent(pendingIntent)
                .build();
        isScanning = true;
        startForeground(1, notification);
        if (intent.getAction().equals(LocalFirstSDK.STOPFOREGROUND_ACTION)) {
            G.Log(TAG, "Received Stop Foreground Intent");
            //your end servce code
            exit = true;
            if (isScanning) stopDiscovery();
            stopHotspot();
            if(stats!=null)stats.close();
            if(wakeLock!=null) {
                if (wakeLock.isHeld())
                    wakeLock.release();
            }
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        } else if(intent.getAction().equals(LocalFirstSDK.STARTFOREGROUND_ACTION)) {
            //String input = intent.getStringExtra("inputExtra");
            mHandler = new Handler(Looper.getMainLooper());
            //repeat=0;
            //timers = new SettingsPreferences(getApplicationContext());
            long wifiWaitingTime = Config.wifiConnectionWaitingTime;
            db = new ContentDatabaseHandler(getApplicationContext());
            stats = new StatsHandler(getApplicationContext());
            backend = new SimpleLoggingBackend(getApplicationContext());
            //backend.signIn(stats.getUserName());
            backend.setUserId(stats.getUserId());
            //backend.setFilesCallback(this);
            G.Log(TAG, "Stats:  Wifi connections:" + stats.getConnections() + " Wifi failed:" + stats.getConnectionsFailed() + " Bt connections:" + stats.getBtConnections() + " Files transferred:" + stats.getTransferred());
            mWifiLink = new WifiLink(getApplicationContext(), this, stats, backend, wifiWaitingTime);
            mDataSharingClient = new DataSharingClient(getApplicationContext(), mWifiLink, this, backend, true);
            manager = (BluetoothManager) this.getSystemService(BLUETOOTH_SERVICE);
            btAdapter = manager.getAdapter();
            mDataSharingServer = new DataSharingServer(getApplicationContext(), backend, this, true);
            hotspot = new WifiDirectHotSpot(getApplicationContext(), this, stats/*, timers,params*/);

            SERVICE_UUID = new ParcelUuid(nameUUIDFromBytes(Config.meetingId.getBytes()));
            exit = false;

            Bundle b = intent.getExtras();

            if (b != null) {
                isScanning = b.containsKey("isScanning") ? b.getBoolean("isScanning") : true;
                scanTime = b.containsKey("scanTime") ? b.getLong("scanTime") : Config.bleScanDuration;
                hotspotRestartTime = b.containsKey("hotspotRestartTime") ? b.getLong("hotspotRestartTime") : Config.hotspotRestartTime;
                btIdleFgTime = b.containsKey("btIdleFgTime") ? b.getLong("btIdleFgTime") : Config.bleAdvertiseForegroundDuration;
                btIdleBgTime = b.containsKey("btIdleBgTime") ? b.getLong("btIdleBgTime") : Config.bleAdvertiseBackgroundDuration;
            }

            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    G.Log(TAG, "Broadcast received " + intent);
                    switch (intent.getAction()) {
                        case RESTART:
                            G.Log(TAG, "Restart");
                            /*obFinished(gparams, true);*/
                            scheduleRefresh();
                            break;
                    }
                }
            };

            try {
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBroadcastReceiver, getIntentFilter());
            } catch (Exception e) {
                G.Log(TAG, "Leaked exp " + e);
            }


            //do heavy work on a background thread
            //stopSelf();
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "DataHopService::WakelockTag");
            wakeLock.acquire();
            if (isScanning && !exit) startDiscovery();
            startHotspot();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startDiscovery() {

        G.Log(TAG,"Starting discovery");

        ParcelUuid SERVICE_UUID = new ParcelUuid(UUID.nameUUIDFromBytes(Config.meetingId.getBytes()));
        bleScan = new BLEServiceDiscovery(this, this, this, stats);
        /*repeat++;
        if(repeat==Config.loops){
            deleteItem(this);
            repeat=0;
        }*/
        try {
            if (!bleScan.start(prepareContentFilter(), SERVICE_UUID))
                G.Log(TAG, "Unable to use bluetooth in this device");
        } catch (Exception e) {
            G.Log(TAG, "Exception DiscoveryService " + e);
            /*jobFinished(params, false);
            scheduleRefresh();
            return true;*/
        }
        // Uses a handler to delay the execution of jobFinished().
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                    G.Log(TAG, "Stop scan");
                    if (bleScan != null) bleScan.stop();
                    bleScan.tryConnection();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            G.Log(TAG,"Start service");
                            //bleScan.tryConnection();
                            if(!exit&&isScanning)startDiscovery();
                        }
                    }, btIdleFgTime);
            }
        }, scanTime);
    }

    public void stopDiscovery(){
        if(bleScan!=null)bleScan.stop();
        if(mHandler!=null)mHandler.removeCallbacksAndMessages(null);
    }

    public void startHotspot() {
        G.Log(TAG, "Hotspot start");


        startBluetooth(prepareContentFilter());

        if (!hotspot.isConnected()) {
            G.Log(TAG, "Job adv finished not connected");
            hotspot.stop(new WifiDirectHotSpot.StartStopListener() {
                public void onSuccess() {
                    G.Log(TAG, "Hotspot stop success");
                    hotspot.start(new WifiDirectHotSpot.StartStopListener() {
                        public void onSuccess() {
                            G.Log(TAG, "Hotspot started");
                        }

                        public void onFailure(int reason) {
                            G.Log(TAG, "Hotspot started failed, error code " + reason);
                        }
                    });
                }

                public void onFailure(int reason) {
                    G.Log(TAG, "Hotspot stop failed, error code " + reason);
                }
            });

        }

        //mDataSharingServer.start();
    }

    public void stopHotspot()
    {
        if(hotspot!=null) {
            hotspot.stop(new WifiDirectHotSpot.StartStopListener() {
                public void onSuccess() {
                    G.Log(TAG, "Hotspot stop success");
                }

                public void onFailure(int reason) {
                    G.Log(TAG, "Hotspot stop failed, error code " + reason);
                }
            });
            stopBluetooth();
            mDataSharingServer.close();
            try {
                LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mBroadcastReceiver);
            } catch (Exception e) {
                G.Log(TAG, "Failed unregistering receiver");
            }
        }
    }

    private void scheduleRefresh()
    {
        stopHotspot();
        if (isScanning)stopDiscovery();
        if(!exit) {
            startHotspot();
            if (isScanning) startDiscovery();
        }
    }

    private void startBluetooth(HashMap<UUID, ContentAdvertisement> ca) {

        if (btAdapter != null) {
            if (btAdapter.isMultipleAdvertisementSupported()) {
                adv = btAdapter.getBluetoothLeAdvertiser();
                advertiseCallback = createAdvertiseCallback();
                startAdvertising(SERVICE_UUID);
                startServer(ca);
            } else {
                Intent broadcast = new Intent(NOT_SUPPORTED);
                broadcast.putExtra("message", "BLE Advertising not supported");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);

            }
        } else {
            Intent broadcast = new Intent(NOT_SUPPORTED);
            broadcast.putExtra("message", "BLE not supported");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);

        }
    }

    private void stopBluetooth() {
        stopAdvertising();
        stopServer();
    }


    private void startAdvertising(ParcelUuid parcelUuid) {
        G.Log(TAG, "Starting ADV, Tx power " + parcelUuid.toString());
        AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(ADVERTISE_MODE_BALANCED)
                .setTxPowerLevel(ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable(true)
                .build();

        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .addManufacturerData(0, stats.getUserName().getBytes())
                .addServiceUuid(parcelUuid)
                .setIncludeTxPowerLevel(false)
                .setIncludeDeviceName(false)
                .build();
        adv.startAdvertising(advertiseSettings, advertiseData, advertiseCallback);
        G.Log(TAG, "Name length " + stats.getUserName().getBytes().length + " " + advertiseData);

    }


    private void stopAdvertising() {
        G.Log(TAG, "Stopping ADV");
        adv.stopAdvertising(advertiseCallback);
    }

    /**
     * Initialize the GATT server instance with the services/characteristics
     * from the Time Profile.
     */
    private void startServer(HashMap<UUID, ContentAdvertisement> ca) {
        G.Log(TAG, "Start server " + hotspot.getNetworkName());

        stopServer();
        serverCallback = new GattServerCallback(getApplicationContext(), hotspot, ca, SERVICE_UUID, stats);
        mBluetoothGattServer = manager.openGattServer(getApplicationContext(), serverCallback);
        serverCallback.setServer(mBluetoothGattServer);
        if (hotspot.getNetworkName() != null)
            serverCallback.setNetwork(hotspot.getNetworkName(), hotspot.getPassphrase());
        if (mBluetoothGattServer == null) {
            G.Log(TAG, "Unable to create GATT server");
            return;
        }

        setupServer();

    }

    /**
     * Shut down the GATT server.
     */
    private void stopServer() {
        if (mBluetoothGattServer == null) return;
        try {
            mBluetoothGattServer.clearServices();
            mBluetoothGattServer.close();
            serverCallback.stop();
        } catch (Exception e) {
            G.Log(TAG, "Stop ble adv server exception " + e);
        }
    }

    // GattServer

    private void setupServer() {
        BluetoothGattService service = new BluetoothGattService(SERVICE_UUID.getUuid(),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        for (Group group : db.getGroups()) {
            // Write characteristic
            UUID CHARACTERISTIC_UUID = nameUUIDFromBytes(group.getName().getBytes());
            G.Log(TAG, "Advertising characteristic " + CHARACTERISTIC_UUID.toString());
            BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(
                    CHARACTERISTIC_UUID,
                    BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE);
            service.addCharacteristic(writeCharacteristic);
        }
        // Write characteristic
        G.Log(TAG, "Advertising characteristic " + CHARACTERISTIC_DIRECT_UUID.toString());
        BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(
                CHARACTERISTIC_DIRECT_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);
        service.addCharacteristic(writeCharacteristic);
        mBluetoothGattServer.addService(service);
    }

    private AdvertiseCallback createAdvertiseCallback() {
        return new AdvertiseCallback() {
            @Override
            public void onStartFailure(int errorCode) {
                Intent broadcast = new Intent(NOT_SUPPORTED);
                broadcast.putExtra("message", "BLE Advertising failed");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);
                switch (errorCode) {
                    case ADVERTISE_FAILED_DATA_TOO_LARGE:
                        G.Log(TAG, "ADVERTISE_FAILED_DATA_TOO_LARGE");
                        break;
                    case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                        G.Log(TAG, "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS");
                        break;
                    case ADVERTISE_FAILED_ALREADY_STARTED:
                        G.Log(TAG, "ADVERTISE_FAILED_ALREADY_STARTED");
                        break;
                    case ADVERTISE_FAILED_INTERNAL_ERROR:
                        G.Log(TAG, "ADVERTISE_FAILED_INTERNAL_ERROR");
                        break;
                    case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                        G.Log(TAG, "ADVERTISE_FAILED_FEATURE_UNSUPPORTED");
                        break;
                    default:
                        G.Log(TAG, "startAdvertising failed with unknown error " + errorCode);
                        break;
                }
            }
        };
    }


    @Override
    public void timeout() {
        G.Log(TAG, "timeout");
        //scheduleRefresh();
    }

    @Override
    public void onNewContent(String name, String group, int size, int latency) {
        G.Log(TAG, "New content " + name);
        int times = stats.getTransferred();

        try {
            times++;
            stats.setTransferred(times);
        }catch (Exception e){G.Log(TAG,"New content Exception "+e.getMessage());}
        G.Log(TAG, "New content " + name + " " + times);
        EventBus.getDefault().post(new NewContentEvent("New content!"));
        /*Intent broadcast = new Intent(MainActivity.NEW_CHUNK_RECEIVED);
        broadcast.putExtra("group", group);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);*/
            /*jobFinished(gparams, false);
            JobScheduler mJobScheduler = (JobScheduler) getApplicationContext()
                    .getSystemService(JOB_SCHEDULER_SERVICE);
            mJobScheduler.cancel(jobId);
            onStopJob(gparams);
            onStartJob(gparams);*/
        //sendMessage(MainActivity.NEW_CONTENT, new Pair<String,String>(name,group));
    }

    @Override
    public void onUserDiscovered(String user, String address){
        G.Log(TAG,"onUserDiscovered "+user+" "+address);

    }

    @Override
    public void linkNetworkSameDiscovered(String device) {
        G.Log(TAG, "Network discovered same content on " + device);
            /*Intent broadcast = new Intent(AdvertisingService.STOP_ADV_BLUETOOTH);
            broadcast.putExtra("message", "Same content discovered on " + device);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);*/
        backend.serviceDiscovered(new Date(),device,0);
//        serviceStop();
    }

    @Override
    public void linkNetworkDiscovered(String network) {
        G.Log(TAG, "Network discovered " + network);
        String[] separated = network.split(":");
        /*Intent broadcast = new Intent(AdvertisingService.STOP_ADV_BLUETOOTH);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);*/
        G.Log(TAG, "Connecting to network: " + separated[0]);
        stopHotspot();
        stopDiscovery();
        backend.serviceDiscovered(new Date(),network,1);
        //if (!separated[0].equals("")&&!stop) {
        if (!separated[0].equals("")){
            mWifiLink.disconnect();

            mWifiLink.connect(separated[0], separated[1], separated[2], "");

            /*JobScheduler mJobScheduler = (JobScheduler) getApplicationContext()
                    .getSystemService(JOB_SCHEDULER_SERVICE);
            mJobScheduler.cancel(jobId);*/
        } else {
            //backend.serviceDiscovered(new Date(),network);
            startHotspot();
            startDiscovery();
        }
    }

    @Override
    public void wifiLinkConnected(String address) {
        G.Log(TAG, "wifiLinkConnected " + m_isConnected);
        if (!m_isConnected) {
            m_isConnected = true;
            int conn = stats.getConnections();
            stats.setConnections(++conn);
            mDataSharingClient.start(stats.getUserName(), address);
        }
    }

    @Override
    public void wifiLinkDisconnected() {
        G.Log(TAG, "wifiLinkDisconnected " + m_isConnected);
        m_isConnected = false;
        /*JobScheduler mJobScheduler = (JobScheduler) getApplicationContext()
                .getSystemService(JOB_SCHEDULER_SERVICE);
        mJobScheduler.cancel(jobId);
        scheduleRefresh();*/

        /*Intent broadcast = new Intent(AdvertisingService.START_ADV_BLUETOOTH);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);*/
        //serviceStop();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                G.Log(TAG,"Start service");
                //bleScan.tryConnection();
                if(!exit)startDiscovery();
            }
        }, btIdleFgTime);
        startHotspot();
    }

    private HashMap<UUID, ContentAdvertisement> prepareContentFilter(){

        if(db!=null) {
            HashMap<UUID,ContentAdvertisement> hm = new HashMap<>();
            for (Group group : db.getGroups()) {
                ContentAdvertisement ca = new ContentAdvertisement();
                //if(timers.isOnlyLocalGroupsSharing())
                G.Log(TAG, "Group " + group.getName());
                for (Content content : db.getContentDownloaded(group.getName())) {
                    G.Log(TAG, "Content advertisement add element " + content.getName() + "." + content.getId());
                    ca.addElement(content.getName() + "." + content.getId());
                }
                hm.put(nameUUIDFromBytes(group.getName().getBytes()),ca);
            }
            return hm;
        }
        return null;
    }

    private void deleteItem(Context context){
        G.Log(TAG,"Delete "+"TestGroup");
        File dir = context.getExternalFilesDir(Config.FOLDER);
        File[] subFiles = dir.listFiles();
        ContentDatabaseHandler db = new ContentDatabaseHandler(context);
        List<Content> content = db.getContentDownloaded("TestGroup");
        //G.Log(TAG,"Files " +subFiles.length+" "+content.size());
        for (Content cn : content) {
            // Writing Contacts to log
            String name = cn.getName();
            if(cn.getId()>0)name+="."+cn.getId();
            //G.Log(TAG,"Name:"+ name+ " desc:" + cn.getText() + " url:"+cn.getUrl());
            if (subFiles != null) {
                //G.Log("Files " +subFiles);
                for (File file : subFiles) {
                    // G.Log("Filename " + cn.getName() + " " +file.getAbsolutePath()+" "+file.getName()+" "+file.length());
                    if (file.getName().equals(name)|| file.getName().equals(cn.getName())) file.delete();
                }
            }
            db.rmContent(cn.getUri(),"TestGroup");

        }
        //Intent broadcast = new Intent(CLEAR_VIDEOS);
        //LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
    }

    private IntentFilter getIntentFilter()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(START_DIS_BLUETOOTH);
        filter.addAction(STOP_DIS_BLUETOOTH);
        filter.addAction(STOP);
        filter.addAction(STOP_ADV_BLUETOOTH);
        filter.addAction(START_ADV_BLUETOOTH);
        filter.addAction(RESTART);
        //filter.addAction(AdvertisingService.SIGNIN);
        return filter;
    }

    @Override
    public void setNetwork(String network, String password) {
        G.Log(TAG, "Set network " + network);
        serverCallback.setNetwork(network, password);
    }

    @Override
    public void connected() {
        mDataSharingServer.start();
    }

    @Override
    public void disconnected() {
        mDataSharingServer.close();
    }
}

package network.datahop.localsharing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobScheduler;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import androidx.fragment.app.Fragment;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import network.datahop.localfirst.LocalFirstSDK;
import network.datahop.localfirst.LocalFirstListener;
import network.datahop.localfirst.data.Group;
import network.datahop.localfirst.data.Content;
import network.datahop.localfirst.data.ContentDatabaseHandler;
import network.datahop.localsharing.ui.GroupActivity;
import network.datahop.localsharing.ui.fragments.GroupsListFragment;
import network.datahop.localsharing.ui.fragments.SettingsFragment;
import network.datahop.localsharing.utils.Config;
import network.datahop.localsharing.utils.G;
import network.datahop.localsharing.utils.SettingsPreferences;
import network.datahop.localsharing.ui.fragments.detail.OnGroupClickListener;

import static network.datahop.localfirst.LocalFirstSDK.ACCEPTANCE;
import static network.datahop.localfirst.LocalFirstSDK.ACTION_ACCEPT;
import static network.datahop.localfirst.LocalFirstSDK.ACTION_REJECT;
import static network.datahop.localfirst.LocalFirstSDK.CHECK_STATUS;
import static network.datahop.localfirst.LocalFirstSDK.DIRECT_CONNECTION;
import static network.datahop.localfirst.LocalFirstSDK.DIRECT_CONNECTION_ACCEPTED;
import static network.datahop.localfirst.LocalFirstSDK.DIRECT_CONNECTION_REJECTED;
import static network.datahop.localfirst.LocalFirstSDK.NEW_CONTENT;
//import static network.datahop.localfirst.LocalFirstSDK.RESTART;
//import static network.datahop.localfirst.LocalFirstSDK.STOP;
import static network.datahop.localfirst.LocalFirstSDK.USER;
import static network.datahop.localfirst.LocalFirstSDK.NOT_SUPPORTED;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnGroupClickListener  {



    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    private static final int PERMISSION_WIFI_STATE = 3;
    private static final String TAG = "MainActivity";

    BroadcastReceiver mBroadcastReceiver;
    SettingsPreferences timers;
    private ClientHandler mHandler;

    ContentDatabaseHandler db;
    JobScheduler tm;
    MainActivity that;
    String name;
    boolean resumed=true;
    boolean groupActivityStarted=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        {
            G.Log(TAG,"Android pie");
            if(!isLocnEnabled(getApplicationContext()))
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }

        try {


            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) wifiManager.setWifiEnabled(true);
        }catch (NullPointerException e){
            network.datahop.localfirst.utils.G.Log(TAG,"Null pointer exception");
            Toast.makeText(this, "Unable to start service", Toast.LENGTH_SHORT).show();
            return;
        }

        timers = new SettingsPreferences(getApplicationContext());
        mHandler = new ClientHandler(this);

        db = new ContentDatabaseHandler(this);

        tm = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        that = this;

        initNotificationChannel();



        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                G.Log(TAG, "Broadcast received " + intent);
                Bundle extras;
                switch (intent.getAction()) {
                    case DIRECT_CONNECTION:
                        extras = intent.getExtras();
                        if (extras != null) {
                            if (extras.containsKey("user")) {
                                G.Log(TAG,"Direct connection received "+extras.getString("user"));

                                if(((App)getApplication()).isForeground()) {
                                    acceptConnection(extras.getString("user"));
                                } else {
                                    acceptanceNotification(extras.getString("user"));
                                }
                            }
                        }
                        break;
                    case NOT_SUPPORTED:
                        extras = intent.getExtras();
                        if (extras != null) {
                            if (extras.containsKey("message")) {
                                Toast.makeText(getApplicationContext(), intent.getStringExtra("message"), Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;

                }
            }
        };



        G.Log(TAG,Build.VERSION.SDK_INT+" "+Build.VERSION_CODES.M);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
            setContentView(R.layout.activity_main);
        else
            setContentView(R.layout.activity_main_legacy);


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Groups");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View hView =  navigationView.getHeaderView(0);
        TextView nav_user = (TextView)hView.findViewById(R.id.userview);
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("pref",Context.MODE_PRIVATE);
        String defaultName = getResources().getString(R.string.user_name);
        name = sharedPref.getString(getString(R.string.user_name), defaultName);

        try{
            nav_user.setText(name);
        }catch (Exception e){G.Log(TAG, "no username ");}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestForPermissions();
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBroadcastReceiver, getIntentFilter());
        LocalFirstSDK.init(getApplicationContext());
        if(timers.getStoragePermission()&&timers.getStoragePermission())startService();
        super.onCreate(savedInstanceState);



    }

    private void requestForPermissions()
    {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
                }
            });
            builder.show();
        }
        if (this.checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.CHANGE_WIFI_STATE}, PERMISSION_WIFI_STATE);
                }
            });
            builder.show();
        }
        if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            });
            builder.show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        G.Log(TAG,"Permissions "+requestCode+" "+permissions+" "+grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    G.Log(TAG,"Location accepted");
                    timers.setLocationPermission(true);
                    if(timers.getStoragePermission())startService();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    G.Log(TAG,"Location not accepted");

                }
                break;
            }
                case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE:

                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // permission was granted, yay! Do the
                        // contacts-related task you need to do.
                        G.Log(TAG,"Storage accepted");
                        timers.setStoragePermission(true);
                        //new CreateWallet(getApplicationContext()).execute();
                        if(timers.getLocationPermission())startService();
                    } else {
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                        G.Log(TAG,"Storage not accepted");

                    }
            }

            // other 'case' lines to check for other
            // permissions this app might request.

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {


        int id = item.getItemId();


        if (id == R.id.nav_content) {

            Fragment fragment = GroupsListFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_fragment_container, fragment, "MainFragment")
                    .commit();
            GroupsListFragment group = (GroupsListFragment)fragment;
            group.setOnChatGroupClickListener(this);
        } else if (id == R.id.nav_settings) {
            Fragment fragment = SettingsFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_fragment_container, fragment, "SettingsFragment")
                    .commit();

        } else if (id == R.id.nav_quit) {
            //unbindService();
            stopServices();
            finishAffinity();
            finishAndRemoveTask();
            this.finish();
            moveTaskToBack(true);
        } else if (id == R.id.nav_share) {
            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
                String shareMessage= "\nLet me recommend you this application\n\n";
                shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "choose one"));
                //return true;
            } catch(Exception e) {
                e.toString();
            }

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume(){
        G.Log(TAG,"onResume");

        //GetLatestVersion checkVersion = new GetLatestVersion(this);
        resumed=true;

        super.onResume();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        G.Log(TAG, "onNewIntent");
        Bundle extras = intent.getExtras();
        int tabNumber;

        if (extras != null) {
            tabNumber = extras.getInt(ACCEPTANCE);
            G.Log(TAG, "Tab Number: " + tabNumber + " " + extras.get(USER));
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            if (tabNumber == 0)
                acceptConnection(extras.getString(USER));

// notificationId is a unique int for each notification that you must define
            notificationManager.cancel(0);

        } else {
            Log.d(TAG, "Extras are NULL");

        }
    }

    @Override
    protected void onPause(){
        G.Log(TAG,"onPause");
        resumed=false;
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        // A service can be "started" and/or "bound". In this case, it's "started" by this Activity
        // and "bound" to the JobScheduler (also called "Scheduled" by the JobScheduler). This call
        // to stopService() won't prevent scheduled jobs to be processed. However, failing
        // to call stopService() would keep it alive indefinitely.
        G.Log(TAG,"ondestroy");
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mBroadcastReceiver);

        stopServices();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start service and provide it a way to communicate with this class.
        setMainFragment();
    }


    @Override
    public void onGroupClicked(Group chatGroup, int position) {
        G.Log(TAG,"Group clicked "+db.getGroupName(position)+" "+position);
        groupActivityStarted=true;
        db.groupClearPending(chatGroup.getName());

        Intent intent = new Intent(that, GroupActivity.class);
        intent.putExtra("group", db.getGroupName(position+1));
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }


    @Override
    public void onGroupLongClicked(Group chatGroup, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove all groups?");
        // Set up the buttons
        builder.setMessage(R.string.action_delete_group);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteGroup(chatGroup);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void stopServices(){
        G.Log(TAG,"Stop services");

        LocalFirstSDK.stop(getApplicationContext());


    }

    private void refresh()
    {
        //Intent broadcast = new Intent(RESTART);
        //LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);
        GroupsListFragment fragment = (GroupsListFragment)getSupportFragmentManager().findFragmentByTag("MainFragment");
        if (fragment != null && fragment.isVisible()) {
            G.Log(TAG,"Group list fragment");
            fragment.updateChatGroupsListAdapter(db.getGroups());
        }
    }

    private class ClientHandler extends Handler {

        // Prevent possible leaks with a weak reference.
        private WeakReference<MainActivity> mActivity;

        ClientHandler(MainActivity activity) {
            super(/* default looper */);
            this.mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = mActivity.get();
            if (mainActivity == null) {
                // Activity is no longer available, exit.
                return;
            }
            Pair<String,String> messagePair = (Pair<String,String>) msg.obj;
            App app = (App)getApplication();
            G.Log(TAG,"ClientHandler: "+msg.what+" "+messagePair.first+" "+messagePair.second+" "+!app.isForeground());

            switch (msg.what) {
                case CHECK_STATUS:

                    break;

                //case NEW_CHUNK:
                case NEW_CONTENT:
                    if(!app.isForeground())
                    {
                        sendNotification(messagePair.first,messagePair.second);
                    }

                    db.groupIncreasePending(messagePair.second);

                    GroupsListFragment fragment = (GroupsListFragment)getSupportFragmentManager().findFragmentByTag("MainFragment");
                    
                    if (fragment != null && fragment.isVisible()) {
                        G.Log(TAG,"Group list fragment "+db.getGroupPending(messagePair.second));
                        fragment.updateChatGroupsListAdapter(db.getGroups());
                    }

                    break;

                /*case DataHopConnectivityService.LOGIN_OK:
                    //hideProgressDialog();
                    //startApp();
                    G.Log(TAG,"ClientHandler: DataHop service is Running.");
                    break;

                case DataHopConnectivityService.LOGIN_KO:
                    //hideProgressDialog();
                    //Toast.makeText(getApplicationContext(), "Authentication failed",
                    //        Toast.LENGTH_SHORT).show();
                    G.Log(TAG,"ClientHandler: DataHop service  is Stopped.");
                    break;

                case DataHopConnectivityService.LOGOUT_OK:

                    G.Log(TAG,"ClientHandler: logout.");
                    //Fragment
                    //backToLogIn();
                    break;*/
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
        public void
        replyToClient(Message message, int replyMessage) {
            try {
                message.replyTo.send(Message.obtain(null, replyMessage));
            } catch (RemoteException e) {
                // Nothing to do here; It means that client end has been terminated.
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Remove all groups?");
            // Set up the buttons
            builder.setMessage(R.string.action_delete);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteGroups();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
            //  return true;
        } else if (id == R.id.action_meeting) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Group name");

            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            //input.setText(timers.getMeetingId());
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //timers.setMeetingId(input.getText().toString());
                    //getSupportActionBar().setTitle("Meeting Id: "+timers.getMeetingId());
                    //startService();
                    G.Log(TAG,"ADD group "+db.getGroupMaxId());
                    Group g = new Group();
                    g.setGroupId(db.getGroupMaxId()+1);
                    g.setName(input.getText().toString());
                    g.setTimestamp(System.currentTimeMillis());
                    if(!db.getGroups().contains(g))
                        db.addGroup(g);
                    else
                        Toast.makeText(getApplicationContext(), "Group already exists", Toast.LENGTH_SHORT).show();
                    refresh();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
            // return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void setMainFragment(){
        Fragment fragment = GroupsListFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment_container, fragment, "MainFragment")
                .commit();
        GroupsListFragment group = (GroupsListFragment)fragment;
        group.setOnChatGroupClickListener(this);

    }

    public void startService()
    {

        LocalFirstSDK.start(getApplicationContext(),name,timers.isScanning(),timers.getBtScanTime(),timers.getBtIdleFgTime(),timers.getBtIdleBgTime(),timers.getHotspotRestartTime(), new LocalFirstListener(){
            @Override
            public void newFileReceived(String name) {
                G.Log(TAG,"New file received "+name);
            }

            @Override
            public void newUSerDiscovered(String name) {
                G.Log(TAG,"New user discovered "+name);

            }

            @Override
            public void newDataDiscovered(String data) {

            }
        });
    }


    private void deleteGroups(){

        File dir = this.getExternalFilesDir(Config.FOLDER);
        File[] subFiles = dir.listFiles();

        for(Group group : db.getGroups() )
        {
            //if(group.getGroupId()>0) {
                for(Content content : db.getGroupContent(group.getName())) {
                        // Writing Contacts to log
                        String name = content.getName();
                        if (content.getId() > 0) name += "." + content.getId();
                        if (subFiles != null) {
                            for (File file : subFiles)
                            {
                                if (file.getName().equals(name) || file.getName().equals(content.getName()))
                                    file.delete();
                            }
                        }
                        db.rmContent(content.getUri(), group.getName());

                }
                db.rmGroup(group.getName());
  
        }

        refresh();

    }

    private void deleteGroup(Group group)
    {
        File dir = this.getExternalFilesDir(Config.FOLDER);
        File[] subFiles = dir.listFiles();

        if(group.getGroupId()>0) {
            for(Content content : db.getGroupContent(group.getName())) {
                // Writing Contacts to log
                String name = content.getName();
                if (content.getId() > 0) name += "." + content.getId();
                //G.Log(TAG,"Name:"+ name+ " desc:" + cn.getText() + " url:"+cn.getUrl());
                if (subFiles != null) {
                    for (File file : subFiles)
                    {
                        if (file.getName().equals(name) || file.getName().equals(content.getName()))
                            file.delete();
                    }
                }
                db.rmContent(content.getUri(), group.getName());

            }
            db.rmGroup(group.getName());
        }

        refresh();
    }

    public void initNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }


        // Create channel to show notifications.
        String channelId  = getString(R.string.default_notification_channel_id);
        String channelName = getString(R.string.default_notification_channel_name);
        NotificationManager notificationManager =
                getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_LOW));


    }

    public void acceptanceNotification(final String msg){

        Intent intentAccept = new Intent(this, MainActivity.class);
        intentAccept.setAction(ACTION_ACCEPT);
        intentAccept.putExtra(ACCEPTANCE, 0);
        intentAccept.putExtra(USER,msg);
        G.Log(TAG,"accept notification "+msg+" "+intentAccept.getExtras().getString(USER));


        PendingIntent pIntentAccept = PendingIntent.getActivity(this, 0, intentAccept, 0);
        intentAccept.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentAccept.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent intentReject = new Intent(this, MainActivity.class);
        intentReject.setAction(ACTION_REJECT);
        intentReject.putExtra(ACCEPTANCE, 1);
        PendingIntent pIntentReject = PendingIntent.getActivity(this, 0, intentReject, 0);
        intentReject.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentReject.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_stat_looks)
                .setContentTitle("Accept connection?")
                .setChannelId(getString(R.string.default_notification_channel_id))
                .addAction(R.drawable.ic_icon,"Accept",pIntentAccept)
                .addAction(R.drawable.ic_icon,"Decline",pIntentReject)
                .setAutoCancel(true)
                .setContentText("User "+msg+" is trying to create a group with you.");

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, mBuilder.build());

        // Turn on the screen for notification
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean result= Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT_WATCH&&powerManager.isInteractive()|| Build.VERSION.SDK_INT< Build.VERSION_CODES.KITKAT_WATCH&&powerManager.isScreenOn();

        if (!result){
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MH24_SCREENLOCK");
            wl.acquire(10000);
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl_cpu = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MH24_SCREENLOCK");
            wl_cpu.acquire(10000);
        }
    }

    public void sendNotification(final String msg, final String group){


        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_stat_looks)
                .setContentTitle("New file received at group "+group)
                .setChannelId(getString(R.string.default_notification_channel_id))
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .setContentText(msg);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, builder.build());

        // Turn on the screen for notification
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean result= Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT_WATCH&&powerManager.isInteractive()|| Build.VERSION.SDK_INT< Build.VERSION_CODES.KITKAT_WATCH&&powerManager.isScreenOn();

        if (!result){
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MH24_SCREENLOCK");
            wl.acquire(10000);
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl_cpu = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MH24_SCREENLOCK");
            wl_cpu.acquire(10000);
        }
    }

    private void acceptConnection(String user)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(that, R.style.myDialog));
        //AlertDialog.Builder builder = new AlertDialog.Builder(that);
        builder.setTitle("Request received");
        // Set up the buttons
        builder.setMessage("Do you want to share files with " + user + "?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String action = DIRECT_CONNECTION_ACCEPTED;
                Intent broadcast = new Intent(action);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);

                //StatsHandler st = new StatsHandler(getApplicationContext());
                Group group = new Group();
                group.setGroupId(db.getGroupMaxId() + 1);
                group.setName(name + "-" + user);
                G.Log(TAG, "Add group " + System.currentTimeMillis());
                group.setTimestamp(System.currentTimeMillis());
                if (!db.getGroups().contains(group))
                    db.addGroup(group);
                else
                    Toast.makeText(getApplicationContext(), "Group already exists", Toast.LENGTH_SHORT).show();
                refresh();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                String action = DIRECT_CONNECTION_REJECTED;
                Intent broadcast = new Intent(action);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);
            }
        });

        builder.show();
    }

    private static boolean isLocnEnabled(Context context) {
        G.Log(TAG,"Checking location");
        List locnProviders = null;
        try {
            LocationManager lm =(LocationManager) context.getApplicationContext().getSystemService(Activity.LOCATION_SERVICE);
            locnProviders = lm.getProviders(true);

            return (locnProviders.size() != 0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (BuildConfig.DEBUG) {
                if ((locnProviders == null) || (locnProviders.isEmpty()))
                    G.Log(TAG, "Location services disabled");
                else
                    G.Log(TAG, "locnProviders: " + locnProviders.toString());
            }
        }
        return(false);
    }

    private IntentFilter getIntentFilter()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(NOT_SUPPORTED);
        filter.addAction(DIRECT_CONNECTION);
        return filter;
    }



}

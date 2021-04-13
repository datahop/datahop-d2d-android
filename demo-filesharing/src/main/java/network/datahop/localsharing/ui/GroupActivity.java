package network.datahop.localsharing.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import network.datahop.localfirst.LocalFirstSDK;
import network.datahop.localfirst.data.Content;
import network.datahop.localfirst.data.ContentDatabaseHandler;
import network.datahop.localfirst.data.DataSharingClient;
import network.datahop.localfirst.data.Group;
import network.datahop.localfirst.net.DataHopService;
import network.datahop.localfirst.net.ble.GattServerCallback;
import network.datahop.localsharing.App;
import network.datahop.localsharing.MainActivity;
import network.datahop.localsharing.R;
import network.datahop.localsharing.data.AddFile;
import network.datahop.localsharing.ui.fragments.detail.ContentAdapter;
import network.datahop.localsharing.ui.fragments.detail.DataViewHolder;
import network.datahop.localsharing.utils.Config;
import network.datahop.localsharing.utils.G;

/**
 * Demonstrates the use of {@link RecyclerView} with a {@link LinearLayoutManager} and a
 * {@link GridLayoutManager}.
 */
public class GroupActivity extends AppCompatActivity implements DataViewHolder.Callbacks {

    private static final String TAG = GroupActivity.class.getSimpleName();
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    private static final int CHOOSE_FILE_REQUESTCODE = 42;
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 2;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }


    protected LayoutManagerType mCurrentLayoutManagerType;
    protected RecyclerView mRecyclerView;
    protected ContentAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected String[] mDataset;

    ContentDatabaseHandler db;

    String group;

    BroadcastReceiver mBroadcastReceiver;
    @Override
    public void onResume() {
        //setHasOptionsMenu(true);
        refreshAdapter();
        super.onResume();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBroadcastReceiver,getIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mBroadcastReceiver);
    }

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            group = extras.getString("group");
            // and get whatever type user account id is
        }

        db = new ContentDatabaseHandler(this);
        //mProgressDialog = new ProgressDialog(this);
        initDataset(group);
        //View rootView = inflater.inflate(R.layout.fragment_content, container, false);
        //rootView.setTag(TAG);
        setContentView(R.layout.fragment_content);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(group);
        //getSupportActionBar().setHomeButtonEnabled(true);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                        //  builder.setTitle("This app needs location access");
                        //  builder.setMessage("Please grant location access");
                        //  builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
                            }
                        });
                        builder.show();
                    }
                }
                G.Log(TAG, "Open file");
                openFile("*/*");
            }
        });


        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) findViewById(R.id.items_list);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(this);

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                G.Log(TAG,"Broadcast received "+intent);
                Bundle extras;

                switch (intent.getAction()) {
                    case LocalFirstSDK.DIRECT_CONNECTION:
                        extras = intent.getExtras();
                        if (extras != null) {
                            if (extras.containsKey("user")) {
                                if(((App)getApplication()).isForeground()) {
                                    acceptConnection(extras.getString("user"));
                                } else {
                                    acceptanceNotification(extras.getString("user"));
                                }
                            }
                        }
                        break;
                    case LocalFirstSDK.NEW_CHUNK_RECEIVED:
                        String extra = intent.getStringExtra("group");
                        G.Log(TAG,"New chunk "+extra +" "+group);
                        if(extra.equals(group))
                            refreshAdapter();
                        break;

                }
            }
        };

    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(this, SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(this);
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(this);
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.group, menu);
        return true;
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                exit();
                //return true;
            //noinspection SimplifiableIfStatement
                break;
            case R.id.action_clear:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Remove content?");
                // Set up the buttons
                builder.setMessage(R.string.action_delete);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteVideos(group);
                        refreshAdapter();
                        //LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(LocalFirstSDK.RESTART));
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                break;

        }
        return super.onOptionsItemSelected(item);

    }
    /**
     * Generates Strings for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    private void initDataset(String group) {
        mDataset = new String[0];
        /*for (int i = 0; i < DATASET_COUNT; i++) {
            mDataset[i] = "This is element #" + i;
        }
        */
        File dir = getExternalFilesDir(Config.FOLDER);
        File[] subFiles = dir.listFiles();

        List<Content> content = db.getContentDownloaded(group);

        G.Log(TAG, "Files " + subFiles.length + " " + db.getContentCount(group) + " " + content.size()+" "+group);
        //G.Log(TAG, "Getcontent " + db.getDatabaseName() + " " + db.getContentCount(group) + " " + db.getPendingCount(group));

        for (File files : subFiles) {
            G.Log(TAG, "File " + files.getAbsolutePath());
        }

        mAdapter = new ContentAdapter(content, this,this,group);



    }

    private void openFile(String minmeType) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(minmeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        // if you want any file type, you can skip next line
        sIntent.putExtra("CONTENT_TYPE", minmeType);
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;
        if (getPackageManager().resolveActivity(sIntent, 0) != null) {
            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Open file");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent});
        } else {
            chooserIntent = Intent.createChooser(intent, "Open file");
        }

        try {
            startActivityForResult(chooserIntent, CHOOSE_FILE_REQUESTCODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == CHOOSE_FILE_REQUESTCODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (resultData != null) {

                Uri uri = resultData.getData();
                G.Log(TAG, "Uri: " + uri.toString() + " " + uri.getPath() + " " + uri.getEncodedPath()+" "+group);

                //saveFile(uri);
                //showProgressDialog("Adding file");
                //MainActivity activity = (MainActivity)getActivity();
                //activity.stopServices();
                (new AddFile(this, this, group)).execute(uri);

            }
        }
    }


    @Override
    public void onBackPressed() {
        exit();
    }

    public void refreshAdapter()
    {
        G.Log(TAG,"refresh "+db.getContentDownloaded(group).size());
        mAdapter.refreshAdapter(db.getContentDownloaded(group));
    }

    public void onNewContent()
    {
        refreshAdapter();
        //LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(LocalFirstSDK.RESTART));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        G.Log(TAG,"onNewIntent");
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one
        processExtraData();
    }

    private void processExtraData(){
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String content;
        if (extras != null) {
            content = extras.getString("new_content");
            G.Log(TAG,"received "+content);
            refreshAdapter();
            // and get whatever type user account id is
        }
        //use the data received here
    }

    private void exit()
    {
        Intent intent = new Intent(GroupActivity.this, MainActivity.class);
        db.groupClearPending(group);
        // intent.putExtra(ChatUI.BUNDLE_RECIPIENT, groupRecipient);
        //intent.putExtra(BUNDLE_CHANNEL_TYPE, Message.GROUP_CHANNEL_TYPE);
        //setResult(1);
        startActivity(intent);
        finish();
    }

    public void acceptanceNotification(final String msg){
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    // build notification
    // the addAction re-use the same intent to keep the example short
        Notification n  = new Notification.Builder(this)
                .setContentTitle("Accept connection?")
                .setContentText("User "+msg+" is trying to create a group with you.")
                .setSmallIcon(R.mipmap.ic_stat_looks)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_icon, "Accept", pIntent)
                .addAction(R.drawable.ic_icon, "Decline", pIntent).build();
        //.addAction(R.drawable.ic_icon, "See", pIntent).build();


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, n);

        // Turn on the screen for notification
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean result= powerManager.isInteractive() || Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH && powerManager.isScreenOn();

        if (!result){
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MH24_SCREENLOCK");
            wl.acquire(10000);
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl_cpu = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MH24_SCREENLOCK");
            wl_cpu.acquire(10000);
        }
    }

    private void acceptConnection(String user)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.myDialog));
        //AlertDialog.Builder builder = new AlertDialog.Builder(that);
        builder.setTitle("Request received");
        // Set up the buttons
        builder.setMessage("Do you want to share files with " + user + "?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String action = GattServerCallback.DIRECT_CONNECTION_ACCEPTED;
                Intent broadcast = new Intent(action);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);

                //StatsHandler st = new StatsHandler(getApplicationContext());
                /*Group group = new Group();
                //group.setGroupId(db.getGroupMaxId() + 1);
                group.setName(st.getUserName() + "-" + user);
                G.Log(TAG, "Add group " + System.currentTimeMillis());
                group.setTimestamp(System.currentTimeMillis());
                if (!db.getGroups().contains(group))
                    db.addGroup(group);
                else
                    Toast.makeText(getApplicationContext(), "Group already exists", Toast.LENGTH_SHORT).show();
                //refresh();*/
                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                String defaultName = getResources().getString(R.string.user_name);
                String name = sharedPref.getString(getString(R.string.user_name), defaultName);
                if(!LocalFirstSDK.addGroup(getApplicationContext(),name + "-" + user))
                    Toast.makeText(getApplicationContext(), "Group already exists", Toast.LENGTH_SHORT).show();
                exit();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                String action = GattServerCallback.DIRECT_CONNECTION_REJECTED;
                Intent broadcast = new Intent(action);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);
            }
        });

        builder.show();
    }

    private void deleteVideos(String group)
    {
        File dir = getExternalFilesDir(network.datahop.localfirst.utils.Config.FOLDER);
        File[] subFiles = dir.listFiles();

        List<Content> content = db.getGroupContent(group);
        network.datahop.localfirst.utils.G.Log(TAG,"Files " +subFiles.length+" "+content.size());
        for (Content cn : content) {
            // Writing Contacts to log
            String name = cn.getName();
            String chunkName=name+"."+cn.getId();
            network.datahop.localfirst.utils.G.Log(TAG,"Name:"+ name+ " desc:" + cn.getText() + " url:"+cn.getUrl());
            if (subFiles != null) {
                //G.Log("Files " +subFiles);
                for (File file : subFiles) {
                    network.datahop.localfirst.utils.G.Log("Filename " + cn.getName() + " " +file.getAbsolutePath()+" "+file.getName()+" "+file.length());
                    if (file.getName().equals(name)||file.getName().equals(chunkName)) file.delete();
                }
            }
            db.rmContent(cn.getUri(),group);

        }
        /*String action = DataSharingClient.DOWNLOAD_COMPLETED;;
        Intent broadcast = new Intent(action);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);*/
        //setMainFragment();
        //startService();

    }

    private IntentFilter getIntentFilter()
    {
        IntentFilter filter = new IntentFilter();
        //filter.addAction(DiscoveryService.DISCOVERED);
        //filter.addAction(DiscoveryService.SAME_DISCOVERED);
        //filter.addAction(DataSharingClient.DOWNLOAD_COMPLETED);
        //filter.addAction(DataSharingClient.NEW_VIDEO_RECEIVED);
        filter.addAction(DataSharingClient.NEW_CHUNK_RECEIVED);
        //filter.addAction(CLEAR_VIDEOS);
        filter.addAction(DataHopService.NOT_SUPPORTED);
        filter.addAction(GattServerCallback.DIRECT_CONNECTION);
        //filter.addAction(TransferFunds.FUNDS_RESULTS);
        return filter;
    }



}


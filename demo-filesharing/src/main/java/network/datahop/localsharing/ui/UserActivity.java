package network.datahop.localsharing.ui;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import network.datahop.localfirst.LocalFirstSDK;
import network.datahop.localsharing.MainActivity;
import network.datahop.localsharing.R;
import network.datahop.localsharing.utils.G;
import network.datahop.localsharing.utils.SettingsPreferences;

import static java.lang.Thread.sleep;

public class UserActivity extends AppCompatActivity  implements View.OnClickListener{

    public static final String TAG="UserActivity";

    private TextView mUserTextView;

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    private static final int PERMISSION_WIFI_STATE = 3;

    // UserActivity that=this;
    SettingsPreferences timers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_login);

        mUserTextView = findViewById(R.id.user);
        timers = new SettingsPreferences(getApplicationContext());
        findViewById(R.id.setuser).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.setuser) {
            if(mUserTextView.getText().length()<4||mUserTextView.getText().length()>6){
                Toast.makeText(getApplicationContext(), "Invalid username. Should be between 4 and 6 characters .", Toast.LENGTH_LONG).show();
            } else {

                //LocalFirstSDK.addGroup(this,"TestGroup");
                //ContentDatabaseHandler db = new ContentDatabaseHandler(getApplicationContext());
                //db.addGroup(group);
                requestForPermissions();
            }
            //createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
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
            /*if (this.checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.CHANGE_WIFI_STATE}, PERMISSION_REQUEST_WIFI_CHANGE);
                    }
                });
                builder.show();
            }
            if (this.checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_WIFI_STATE}, PERMISSION_REQUEST_WIFI_STATE);
                    }
                });
                builder.show();
            }*/
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
                    if(timers.getStoragePermission())
                    {
                        //StatsHandler st = new StatsHandler(getApplicationContext());
                        //st.setUserName(mUserTextView.getText().toString());
                        G.Log(TAG,"Set username "+mUserTextView.getText().toString());
                        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("pref",Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.user_name), mUserTextView.getText().toString());
                        editor.commit();
                        LocalFirstSDK.setUser(mUserTextView.getText().toString());
                        //ProgressDialog pd = ProgressDialog.show(this, "Loading", "Setting user name");
                        Intent intent;
                        //try{sleep(2000);}catch (Exception e){}
                        //    G.Log(TAG,"sleep exception "+e);
                        intent = new Intent(UserActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        //pd.dismiss();
                        startActivity(intent);
                    }
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
                    if(timers.getLocationPermission())
                    {
                        //StatsHandler st = new StatsHandler(getApplicationContext());
                        //st.setUserName(mUserTextView.getText().toString());
                        G.Log(TAG,"Set username "+mUserTextView.getText().toString());
                        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("pref",Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.user_name), mUserTextView.getText().toString());
                        editor.commit();
                        LocalFirstSDK.setUser(mUserTextView.getText().toString());
                        //ProgressDialog pd = ProgressDialog.show(this, "Loading", "Setting user name");
                        Intent intent;
                        //try{sleep(2000);}catch (Exception e){}
                        //    G.Log(TAG,"sleep exception "+e);
                        intent = new Intent(UserActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        //pd.dismiss();
                        startActivity(intent);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    G.Log(TAG,"Storage not accepted");

                }
        }

        // other 'case' lines to check for other
        // permissions this app might request.

    }


}

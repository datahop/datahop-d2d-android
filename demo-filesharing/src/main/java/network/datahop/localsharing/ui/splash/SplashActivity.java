package network.datahop.localsharing.ui.splash;

/**
 * Created by srenevic on 08/09/16.
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import network.datahop.localsharing.MainActivity;
import network.datahop.localsharing.R;
import network.datahop.localsharing.ui.UserActivity;
import network.datahop.localsharing.utils.Config;
import network.datahop.localsharing.utils.G;
import network.datahop.localsharing.utils.SettingsPreferences;

public class SplashActivity extends AppCompatActivity {

        public static final String TAG="SplashActivity";


        SettingsPreferences timers;
        @Override
        protected void onCreate(Bundle savedInstanceState) {

            timers = new SettingsPreferences(getApplicationContext());

            super.onCreate(savedInstanceState);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                setContentView(R.layout.splash_activity);
            } else {
                setContentView(R.layout.splash_activity_legacy);
            }
            //StatsHandler st = new StatsHandler(getApplicationContext());
            //final String name = st.getUserName();
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("pref",Context.MODE_PRIVATE);
            String defaultName = getResources().getString(R.string.user_name);
            String name = sharedPref.getString(getString(R.string.user_name), defaultName);

            G.Log(TAG,"User name "+name+" "+timers.getLocationPermission()+" "+timers.getStoragePermission());
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        int waited = 0;
                        while (waited < Config.SPLASHTIMEOUT) {
                            sleep(1000);
                            waited += 1000;
                        }
                        Intent intent;
                        if(name.equals("user_name")||!timers.getLocationPermission()||!timers.getStoragePermission()) {
                            intent = new Intent(SplashActivity.this, UserActivity.class);
                        } else {
                            intent = new Intent(SplashActivity.this, MainActivity.class);
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        SplashActivity.this.finish();
                    } catch (InterruptedException e) {

                    } finally {
                        SplashActivity.this.finish();
                    }

                }
            };
            thread.start();

        }


}
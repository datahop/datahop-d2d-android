package network.datahop.localsharing;

import android.app.Application;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import network.datahop.localsharing.utils.G;

public class App extends Application implements LifecycleObserver {

    public static boolean IS_APP_IN_FOREGROUND = false;
    private static final String TAG=Application.class.getSimpleName();
    @Override
    public void onCreate() {
        super.onCreate();
        G.Log(TAG,"On create");

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void appInForeground(){
        G.Log(TAG,"App foreground");
        IS_APP_IN_FOREGROUND = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void appInBackground(){
        G.Log(TAG,"App background");
        IS_APP_IN_FOREGROUND = false;
    }

    public boolean isForeground()
    {
        G.Log(TAG,"isforeground "+IS_APP_IN_FOREGROUND);
        return IS_APP_IN_FOREGROUND;
    }
}
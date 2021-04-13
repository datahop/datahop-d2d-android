
package network.datahop.localsharing.ui.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.grandcentrix.tray.AppPreferences;

import network.datahop.localfirst.net.StatsHandler;
import network.datahop.localsharing.R;
import network.datahop.localsharing.utils.Config;
import network.datahop.localsharing.utils.G;

public class ServiceFragment extends Fragment {

  private static final String TAG="ServiceFragment";
  public static ServiceFragment newInstance() {
    // Create fragment arguments here (if necessary)
    return new ServiceFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    m_handler = new Handler();
    //m_handler.postDelayed(m_statusUpdateRunnable, 1000);


  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState)
  {
    @SuppressLint("InflateParams")
    View v =  inflater.inflate(R.layout.fragment_service, null);



    m_wifi_status_view = v.findViewById(R.id.wifi_status_view);
   // m_wifi_status_view.setVisibility(View.GONE);

    m_Status = v.findViewById(R.id.status);
    m_hsSSID = v.findViewById(R.id.sd);
    m_connections = v.findViewById(R.id.started);
    m_users = v.findViewById(R.id.users);
    m_failed = v.findViewById(R.id.failed);

    m_btStatusView = v.findViewById(R.id.bt_status_view);
    m_btCtView = v.findViewById(R.id.btconnect);
    m_btStView = v.findViewById(R.id.btstatus);
    return v;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState)
  {
      G.Log("ServiceFragment::onActivityCreated()");
      super.onActivityCreated(savedInstanceState);
      //m_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
      m_appPreferences = new AppPreferences(getContext()); // this Preference comes for free from the library

  }

  @Override
  public void
  onResume() {
    G.Log("ServiceFragment::onResume()");
    super.onResume();
    m_handler.post(m_statsUpdateRunnable);

  }

  @Override
  public void
  onPause() {
    super.onPause();
    G.Log("ServiceFragment::onPause()");

   // m_handler.removeCallbacks(m_statusUpdateRunnable);
    m_handler.removeCallbacks(m_statsUpdateRunnable);
    //m_handler.removeCallbacks(m_retryConnectionToService);
  }

  private class UpdateTask extends AsyncTask<Void, Void, StatsHandler> {

    @Override
    protected StatsHandler
    doInBackground(Void... voids)
    {
      try {
        //App app = (App)getActivity().getApplication();
        StatsHandler st = new StatsHandler(getActivity());
          return st;

      }
      catch (Exception e) {
        G.Log(TAG,"Error communicating with status service (" + e.getMessage() + ")");
        return null;
      }

    }

    @Override
    protected void
    onPostExecute(StatsHandler fs)
    {
      if (fs == null) {
        // when failed, try after 0.5 seconds
        m_handler.postDelayed(m_statsUpdateRunnable, Config.statusRefrestIfFailed);
      }
      else {
        m_btStatusView.setVisibility(View.VISIBLE);
        m_btStView.setText(fs.getBtStatus());
        m_btCtView.setText(String.valueOf(fs.getBtConnections()));
        m_Status.setText(String.valueOf(fs.getWStatus()));
        m_hsSSID.setText(fs.getHsSSID());
        m_users.setText(String.valueOf(fs.getHsClients()));
        m_connections.setText(String.valueOf(fs.getConnections()));
        m_failed.setText(String.valueOf(fs.getConnectionsFailed()));
        m_wifi_status_view.setVisibility(View.VISIBLE);

        // refresh after 5 seconds
        m_handler.postDelayed(m_statsUpdateRunnable, Config.statusRefresh);
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////////


  private ViewGroup m_wifi_status_view;

  private TextView m_Status;
  private TextView m_hsSSID;
  private TextView m_users;
  private TextView m_connections;
  private TextView m_failed;

  private ViewGroup m_btStatusView;
  private TextView m_btStView;
  private TextView m_btCtView;

  private Handler m_handler;
  /*private Runnable m_statusUpdateRunnable = new Runnable() {
    @Override
    public void run()
    {
      new StatusUpdateTask().execute();
    }
  };*/

  private Runnable m_statsUpdateRunnable = new Runnable() {
    @Override
    public void run()
    {
      new UpdateTask().execute();
    }
  };

//  private SharedPreferences m_sharedPreferences;
  private AppPreferences m_appPreferences;
//  private Messenger m_serviceMessenger2 = null;

  //  public static final String PREF_UBICDN_SERVICE_STATUS = "UBICDN_SERVICE_STATUS";
  public static final String PREF_SERVICE_SOURCE = "SERVICE_TYPE";

  /*private AlertDialog dialogVpn = null;

  private static final int REQUEST_VPN = 1;
  private static final int REQUEST_INVITE = 2;
  private static final int REQUEST_LOGCAT = 3;
  public static final int REQUEST_ROAMING = 4;*/

    private int restarts=0;

}

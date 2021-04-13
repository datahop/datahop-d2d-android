/*******************************************************
 * Copyright (C) 2017-2018 DataHop Network Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localsharing.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import network.datahop.localsharing.MainActivity;
import network.datahop.localsharing.R;
import network.datahop.localsharing.utils.G;
import network.datahop.localsharing.utils.SettingsPreferences;

public class SettingsFragment extends Fragment {

  public static SettingsFragment newInstance() {
    // Create fragment arguments here (if necessary)
    return new SettingsFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    preferences = new SettingsPreferences(getContext());

  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState)
  {
    @SuppressLint("InflateParams")
    View v =  inflater.inflate(R.layout.fragment_settings, null);
    MainActivity activity = (MainActivity)getActivity();

    isSource = v.findViewById(R.id.checkbox_source);
    isSource.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
          //App app = (App)getActivity().getApplication();
          if (((CheckBox) v).isChecked()) {
              preferences.setScanning(false);
              activity.stopServices();
              activity.startService();
            //  app.setScanning(true);
              G.Log("Set source "+ preferences.isScanning());
          } else {
            //  app.setScanning(false);
            preferences.setScanning(true);
            activity.stopServices();
            activity.startService();
            G.Log("Set source "+ preferences.isScanning());

          }


      }
    });

    localSharing = v.findViewById(R.id.checkbox_sharing);
    localSharing.setEnabled(false);
    localSharing.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {

        if (((CheckBox) v).isChecked()) {
          preferences.setLocalGroupsSharing(true);
          activity.stopServices();
          activity.startService();
        } else {
          //  app.setScanning(false);
          preferences.setLocalGroupsSharing(false);
          activity.stopServices();
          activity.startService();
        }

      }
    });

    btScanTime = v.findViewById(R.id.bt_scan_input);
    btIdleFgTime = v.findViewById(R.id.bt_advfg_input);
    btIdleBgTime = v.findViewById(R.id.bt_advbg_input);

    btScanTime.addTextChangedListener(new TextChangedListener<EditText>(btScanTime) {
      @Override
      public void onTextChanged(EditText target, Editable s) {
        if(!s.toString().equals("")) preferences.setBtScanTime(Long.parseLong(s.toString()));
      }
    });
    btIdleFgTime.addTextChangedListener(new TextChangedListener<EditText>(btIdleFgTime) {
      @Override
      public void onTextChanged(EditText target, Editable s) {
        if(!s.toString().equals("")) preferences.setBtIdleFgTime(Long.parseLong(s.toString()));
      }
    });
    btIdleBgTime.addTextChangedListener(new TextChangedListener<EditText>(btIdleBgTime) {
      @Override
      public void onTextChanged(EditText target, Editable s) {
        if(!s.toString().equals("")) preferences.setBtIdleBgTime(Long.parseLong(s.toString()));
      }
    });

    return v;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState)
  {
      G.Log("ServiceFragment::onActivityCreated()");
      super.onActivityCreated(savedInstanceState);

  }

  @Override
  public void
  onResume() {
    G.Log("ServiceFragment::onResume()");
    super.onResume();
    isSource.setChecked(!preferences.isScanning());
    localSharing.setChecked(preferences.isOnlyLocalGroupsSharing());
    btScanTime.setText(Long.toString(preferences.getBtScanTime()), TextView.BufferType.EDITABLE);
    btIdleFgTime.setText(Long.toString(preferences.getBtIdleFgTime()), TextView.BufferType.EDITABLE);
    btIdleBgTime.setText(Long.toString(preferences.getBtIdleBgTime()), TextView.BufferType.EDITABLE);

  }

  @Override
  public void
  onPause() {
    super.onPause();
    G.Log("ServiceFragment::onPause()");

  }


  //////////////////////////////////////////////////////////////////////////////

  private CheckBox isSource;
  private CheckBox localSharing;

  private EditText btScanTime;
  private EditText btIdleFgTime;
  private EditText btIdleBgTime;


  public static final String PREF_SERVICE_SOURCE = "SERVICE_TYPE";

  SettingsPreferences preferences;

  public abstract class TextChangedListener<T> implements TextWatcher {
    private T target;

    public TextChangedListener(T target) {
      this.target = target;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
      this.onTextChanged(target, s);
    }

    public abstract void onTextChanged(T target, Editable s);
  }
}

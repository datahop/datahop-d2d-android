/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/


package network.datahop.localfirst.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Global class used for logging
 *
 */
public class G {

  /** Flag that turns on/off debugging log output. */
  private static final boolean DEBUG = true;

  private static final boolean DEBUG_WIFI = false;

  private static final boolean DEBUG_BT = false;

  private static final List<String> btTags = new ArrayList<>(Arrays.asList("BtPort","BluetoothAdapter", "BluetoothLeScanner","BluetoothLeAdvertiser","BLEServiceDiscovery","BLEAdvertisement","BleScanner","BTTransport","BtServiceDiscovery","BtServer","BtSwitcherDumb"));

  private static final List<String> wifiTags = new ArrayList<>(Arrays.asList("ContentAdvertisement","WifiLink", "WifiDirectHotSpot"));

  /** Tag used in log output to identify NFD Service. */
  private static final String TAG = "DataHop";

  /**
   * Designated log message method that provides flexibility in message logging.
   *
   * @param tag Tag to identify log message.
   * @param format Format qualifiers as used in String.format()
   * @param args Output log message.
   */
  public static void Log(String tag, String format, Object... args) {
    if (DEBUG_WIFI) {
      if(isWifi(tag))
        Log.d(tag, String.format(format, args));
    } else if (DEBUG_BT) {
      if(isBt(tag))
        Log.d(tag, String.format(format, args));
    } else if(DEBUG){
        Log.d(tag, String.format(format, args));
    }
  }

  /**
   * Convenience method to log a message with a specified tag.
   *
   * @param tag Tag to identify log message.
   * @param message Output log message.
   */
  public static void Log(String tag, String message) {
    Log(tag, "%s", message);
  }

  /**
   * Convenience method to log messages with the default tag.
   *
   * @param message Output log message.
   */
  public static void Log(String message) {
    Log(TAG, message);
  }

  /**
   * Gets the tag in which logs are posted with.
   *
   * @return TAG that is used by this log class.
   */
  public static String getLogTag() {
    return TAG;
  }

  private static boolean isBt(String TAG){
    return btTags.contains(TAG);
  }

  private static boolean isWifi(String TAG){
    return wifiTags.contains(TAG);
  }
}

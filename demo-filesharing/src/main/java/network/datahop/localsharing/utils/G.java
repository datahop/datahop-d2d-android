/* -*- Mode:jde; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
/**
 * Copyright (c) 2015 Regents of the University of California
 *
 * This file is part of NFD (Named Data Networking Forwarding Daemon) Android.
 * See AUTHORS.md for complete list of NFD Android authors and contributors.
 *
 * NFD Android is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * NFD Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * NFD Android, e.g., in COPYING.md file.  If not, see <http://www.gnu.org/licenses/>.
 */

package network.datahop.localsharing.utils;

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

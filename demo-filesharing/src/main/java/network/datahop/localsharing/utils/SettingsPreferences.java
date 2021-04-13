package network.datahop.localsharing.utils;

import android.content.Context;

import net.grandcentrix.tray.AppPreferences;

//import net.grandcentrix.tray.AppPreferences;


/**
 * Created by srenevic on 01/01/18.
 */

public class SettingsPreferences {

    private final static String WIFI="WF_Waiting_time";
    private final static String WIFI_HS_RESTART="WF_Hotspot_restart_time";
    private final static String PEER_SUCCESS="Peer_success_time";
    private final static String PEER_FAILED="Peer_retry_time";
    private final static String SD_SUCCESS="Sd_success_time";
    private final static String SD_FAILED="Sd_retry_time";
    private final static String MEETING_ID="meeting_Id";
    private final static String BT_SCAN="bt_scan_time";
    private final static String BT_FG="bt_idle_fg";
    private final static String BT_BG="bt_idle_bg";
    public final static String BT_ACT="bt_activated";
    public final static String WD_ACT="wd_activated";
    public final static String WALLET="wallet";
    public final static String SCAN ="source";
    public final static String ADDRESS="addr";
    public final static String BALANCE="balance";
    public final static String PRESTIGE="prestige";
    public final static String LOCALGROUPS="groups";


    public final static String LOC_PERM="location_permission";
    public final static String STOR_PERM="storage_permission";

    final AppPreferences appPreferences;


    public SettingsPreferences(Context context){
        //Getting if is source device checkbox enabled from sharedpreferences
       appPreferences = new AppPreferences(context); // this Preference comes for free from the library
    }


    public long getWifiWaitingTime()
    {
        return appPreferences.getLong(WIFI,Config.wifiConnectionWaitingTime);

    }

    public void setWifiWaitingTime(long time)
    {
        appPreferences.put(WIFI,time);
    }


    public long getBtScanTime()
    {
        return appPreferences.getLong(BT_SCAN,Config.bleScanDuration);
    }

    public void setBtScanTime(long time)
    {
        appPreferences.put(BT_SCAN,time);

    }
    public long getBtIdleFgTime()
    {
        return appPreferences.getLong(BT_FG,Config.bleAdvertiseForegroundDuration);
    }

    public void setBtIdleFgTime(long time)
    {
        appPreferences.put(BT_FG,time);

    }
    public long getBtIdleBgTime()
    {
        return appPreferences.getLong(BT_BG,Config.bleAdvertiseBackgroundDuration);
    }

    public void setBtIdleBgTime(long time)
    {
        appPreferences.put(BT_BG,time);

    }

    public long getHotspotRestartTime()
    {
        return appPreferences.getLong(WIFI_HS_RESTART,Config.hotspotRestartTime);
    }

    public void setHotspotRestartTime(long time)
    {
        appPreferences.put(WIFI_HS_RESTART,time);

    }

    public String getMeetingId()
    {
        return appPreferences.getString(MEETING_ID,Config.meetingId);
    }

    public void setMeetingId(String meetingId)
    {
        appPreferences.put(MEETING_ID,meetingId);

    }


    public void setWd(boolean active)
    {

        appPreferences.put(WD_ACT,active);
    }

    public boolean getWd()
    {
        return appPreferences.getBoolean(WD_ACT,false);
    }

    public void setBt(boolean active)
    {
        appPreferences.put(BT_ACT,active);

    }

    public boolean getBt()
    {
        return appPreferences.getBoolean(BT_ACT,false);
    }

    public void setAddress(String address){
        appPreferences.put(ADDRESS,address);
    }

    public String getAddress()
    {
        G.Log("Preferences","Wallet file "+appPreferences.getString(ADDRESS,""));
        return appPreferences.getString(ADDRESS,"");
    }

    public void setWallet(String walletName){
        appPreferences.put(WALLET,walletName);
    }

    public String getWallet()
    {
        G.Log("Preferences","Wallet file "+appPreferences.getString(WALLET,""));
        return appPreferences.getString(WALLET,"");
    }

    public void setBalance(int balance){
        appPreferences.put(BALANCE,balance);
    }

    public int getBalance()
    {
        //G.Log("Preferences","Wallet file "+appPreferences.getString(WALLET,""));
        return appPreferences.getInt(BALANCE,0);
    }

    public void setScanning(boolean source)
    {
        appPreferences.put(SCAN,source);
    }

    public boolean isScanning()
    {
        return appPreferences.getBoolean(SCAN,true);

    }

    public void setPrestige(boolean source)
    {
        appPreferences.put(PRESTIGE,source);
    }

    public boolean isPrestigeEnabled()
    {
        return appPreferences.getBoolean(PRESTIGE,true);

    }

    public void setLocalGroupsSharing(boolean source)
    {
        appPreferences.put(LOCALGROUPS,source);
    }

    public boolean isOnlyLocalGroupsSharing()
    {
        return appPreferences.getBoolean(LOCALGROUPS,true);

    }

    public void setStoragePermission(boolean source)
    {
        appPreferences.put(STOR_PERM,source);
    }

    public boolean getStoragePermission()
    {
        return appPreferences.getBoolean(STOR_PERM,false);

    }

    public void setLocationPermission(boolean source)
    {
        appPreferences.put(LOC_PERM,source);
    }

    public boolean getLocationPermission()
    {
        return appPreferences.getBoolean(LOC_PERM,false);

    }

    /*public Credentials getCredentials(){
        return credentials;
    }

    public void setCredentials(Credentials credentials)
    {
        this.credentials = credentials;
    }*/


}

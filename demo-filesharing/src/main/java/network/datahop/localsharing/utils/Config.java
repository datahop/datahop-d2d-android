/*******************************************************
 * Copyright (C) 2017-2018 DataHop Network Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/


package network.datahop.localsharing.utils;

public class Config
{

	public static final String meetingId ="DataHop";
	// How long to scan for Bluetooth LE devices.
	public static final long bleScanDuration =2000;

	// How long to advertise for Bluetooth LE devices in foreground.
	public static final long bleAdvertiseForegroundDuration = 15000;

	// How long to advertise for Bluetooth LE devices in background.
	public static final long bleAdvertiseBackgroundDuration = 25000;

	// How long to advertise for Bluetooth LE devices in background.
	public static final long statusRefresh = 5000;

	// How long to advertise for Bluetooth LE devices in background.
	public static final long statusRefrestIfFailed = 500;

	//How long to wait for a connection to be successful
	public static final long wifiConnectionWaitingTime = 30000;

	//How long to wait for another source device
	public static final long networkWaitingTime = 2000;


	public static final long hotspotRestartTime = 3600000;

	//Tries to create a face if failed because network not ready
	public static final int maxRetry = 5;


	public static final String SSID="ubicdnap";

	public static final String passwd="Raspberry";

	public static final String owner_ip="10.0.2.2";

	public static final String ip="192.168.49";

	public static final String port="8080";

	public static final int MAX_CHUNK_SIZE=6000000;

	public static final int SPLASHTIMEOUT=5000;

	public static final String FOLDER="datahop";

	public static final int HTTP_TIMEOUT=3000;








} // Config

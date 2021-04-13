/*******************************************************
 * Copyright (C) 2020 DataHop Labs Ltd <sergi@datahop.network>
 *
 * This file is part of DataHop Network project.
 *
 * All rights reserved
 *******************************************************/

package network.datahop.localfirst.net.ble;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import network.datahop.localfirst.data.ContentAdvertisement;
import network.datahop.localfirst.data.ContentDatabaseHandler;
import network.datahop.localfirst.net.DiscoveryListener;
import network.datahop.localfirst.net.LinkListener;
import network.datahop.localfirst.net.StatsHandler;
import network.datahop.localfirst.utils.Config;
import network.datahop.localfirst.utils.G;

import static android.content.Context.BLUETOOTH_SERVICE;
import static java.lang.Thread.sleep;


public class BLEServiceDiscovery {

	private static final String TAG = "BLEServiceDiscovery";

	private Context context;

	/* Bluetooth API */
	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothGatt mBluetoothGatt;
	private int mConnectionState = STATE_DISCONNECTED;

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	//public static final String USER_DISCOVERED = "user_discovered";

	private HashMap<UUID,ContentAdvertisement> ca;

    private boolean mScanning;
	Set<BluetoothDevice> results;

	private boolean started=false;
	//SettingsPreferences mTimers;
	private BluetoothLeScanner mLEScanner;

	boolean mInitialized = false;
	LinkListener lListener;
	DiscoveryListener dListener;

	BluetoothDevice device=null;
	ParcelUuid mServiceUUID;
	StatsHandler stats;
	ContentDatabaseHandler db;
	int pendingWrite;
	boolean sending;

	public BLEServiceDiscovery(LinkListener lListener, DiscoveryListener dListener, Context context/*, SettingsPreferences timers*/, StatsHandler stats)
	{
		mBluetoothManager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		this.context = context;
		//mHandler = new Handler();
        //sHandler = new Handler();

		this.lListener = lListener;
		this.dListener = dListener;
		if(mBluetoothAdapter!=null)mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
		results = new HashSet<BluetoothDevice>();
		//mTimers = timers;
		this.stats = stats;
		db = new ContentDatabaseHandler(context);
		//mServiceUUID = service_uuid;
        /*mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                G.Log(TAG,"Broadcast received "+intent);
                switch (intent.getAction()) {
                    case BLESERVER_STATE:

                        int serverstate = intent.getIntExtra("state",0);
                        if(serverstate==BluetoothProfile.STATE_CONNECTED){
                        	mLEScanner.stopScan(mScanCallback);
                        	//stop();
                        	//disconnect();
						}
                        break;

				}
            }
        };*/

    }

    public boolean start(HashMap<UUID,ContentAdvertisement> ca, ParcelUuid service_uuid)
	{

		G.Log(TAG,"Service uuid:"+service_uuid.getUuid()+" "+started);
		if(mBluetoothAdapter!=null&&!started) {
			pendingWrite = 0;
			sending = false;
			started = true;
			mServiceUUID = service_uuid;
			this.ca = ca;
			results.clear();
			//serverstate = BluetoothProfile.STATE_DISCONNECTED;
			mConnectionState = STATE_DISCONNECTED;

			if (!mBluetoothAdapter.isEnabled()) {
				G.Log(TAG, "Bluetooth is currently disabled...enabling ");
				mBluetoothAdapter.enable();
			} else {
				G.Log(TAG, "Bluetooth enabled...starting services");

			}

			/*try{
				LocalBroadcastManager.getInstance(context).registerReceiver(mBroadcastReceiver, getIntentFilter());
			}catch (Exception e){G.Log(TAG,"leaked register");}*/
			scanLeDevice();
			return true;
		} else {
			return false;

		}

    }

	/*public static IntentFilter getIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BLESERVER_STATE);
		return filter;
	}*/

	public void stop()
	{
		//if(started) {
			G.Log(TAG,"Stop");
			//disconnect();
			//try{context.unregisterReceiver(mBroadcastReceiver);}catch (IllegalArgumentException e){G.Log(TAG,"Unregister failed "+e);}
			try {
				mLEScanner.stopScan(mScanCallback);
				mLEScanner.flushPendingScanResults(mScanCallback);
			}catch (Exception e){G.Log(TAG,"Failed when stopping ble scanner "+e);}
			//started=false;

		//}

	}


	public void scanLeDevice() {

		results.clear();
        mConnectionState=STATE_DISCONNECTED;
		ScanSettings settings = new ScanSettings.Builder()
				.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
				.build();
		List<ScanFilter> filters = new ArrayList<ScanFilter>();
			// Stops scanning after a pre-defined scan period.
		ScanFilter scanFilter = new ScanFilter.Builder()
				//.setServiceUuid(mServiceUUID)
				.setDeviceName("BLEDataHop")
				.build();
		ScanFilter scanFilter2 = new ScanFilter.Builder()
				.setServiceUuid(mServiceUUID)
				//.setDeviceName("BLEDataHop")
				.build();
		filters.add(scanFilter);
		filters.add(scanFilter2);

		G.Log(TAG, "Start scan");

		if(mConnectionState==STATE_DISCONNECTED&&!mScanning)
		{
		    try {
                mLEScanner.startScan(filters, settings, mScanCallback);
                mScanning = true;
            }catch (IllegalStateException e){G.Log(TAG,"Exception "+e);}
		}

	}

	private ScanCallback mScanCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			//byte[] mScanRecord = result.getScanRecord().getBytes();
			//final StringBuilder stringBuilder = new StringBuilder(advertisementData.length);
			//for (byte byteChar : advertisementData) { stringBuilder.append((char) byteChar);}
			G.Log(TAG,"Scan result "+result.getScanRecord().getDeviceName()+" "+result.getScanRecord().getServiceUuids()+" "+new String(result.getScanRecord().getManufacturerSpecificData(0)));
			/*String action = USER_DISCOVERED;
			Intent broadcast = new Intent(action);
			broadcast.putExtra("username", new String(result.getScanRecord().getManufacturerSpecificData(0)));
			broadcast.putExtra("address", result.getDevice().getAddress());
			LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);*/
			//String device = new String(result.getScanRecord().getManufacturerSpecificData(0));
			dListener.onUserDiscovered(new String(result.getScanRecord().getManufacturerSpecificData(0)),result.getDevice().getAddress());
			//if(device.equals("pixel2"))
			results.add(result.getDevice());
		}

		@Override
		public void onBatchScanResults(List<ScanResult> results) {
			for (ScanResult sr : results) {
				G.Log(TAG,"ScanResult - Results", sr.toString());
			}
		}

		@Override
		public void onScanFailed(int errorCode) {
			G.Log(TAG,"Scan Failed Error Code: " + errorCode);
			if(errorCode==2){
				mBluetoothAdapter.disable();
				mBluetoothAdapter.enable();
			}
			try{ sleep(Config.bleScanDuration);}catch (Exception e){}
			if(!started){
				G.Log(TAG,"Not started cancelling");
				return;
			}
			scanLeDevice();
		}
	};

	public void tryConnection(){
		//String address = android.provider.Settings.Secure.getString(context.getContentResolver(), "bluetooth_address");
		G.Log(TAG,"TryConnection "+results.size()+" "+started+" "+mConnectionState);
		if (mConnectionState == STATE_DISCONNECTED) {

			for (BluetoothDevice res : results) {
				//G.Log(TAG,"Connections "+res.getAddress()+" "+res.getAddress().hashCode()+" "+res.getUuids());
				if (connect(res.getAddress())) {
					G.Log(TAG,"Connect to "+res.getAddress());
					results.remove(res);
					break;
				}
			}
		}
	}

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 *
	 * @param address The device address of the destination device.
	 *
	 * @return Return true if the connection is initiated successfully. The connection result
	 *         is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */
	private boolean connect(final String address) {
		if (mBluetoothAdapter == null || address == null) {
			G.Log(TAG, "BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		device = mBluetoothAdapter.getRemoteDevice(address);
		if (device == null) {
			G.Log(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the autoConnect
		// parameter to false.
		mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
		G.Log(TAG, "Trying to create a new connection to "+address);
		mConnectionState = STATE_CONNECTING;
		return true;
	}

	/**
	 * Disconnects an existing connection or cancel a pending connection. The disconnection result
	 * is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public void disconnect() {
		G.Log(TAG,"Disconnect");
		mInitialized = false;
		started=false;
		mConnectionState = STATE_DISCONNECTED;
		if (mBluetoothAdapter == null ) {
			G.Log(TAG, "BluetoothAdapter not initialized");
			return;
		}
		if (mBluetoothGatt == null ) {
			G.Log(TAG, "mBluetoothGatt not initialized");
			return;
		}
		mBluetoothGatt.disconnect();
		mBluetoothGatt.close();
		mBluetoothGatt = null;
		//try{LocalBroadcastManager.getInstance(context).unregisterReceiver(mBroadcastReceiver);}catch (IllegalArgumentException e){G.Log(TAG,"Unregister failed "+e);}
	}

	/**
	 * After using a given BLE device, the app must call this method to ensure resources are
	 * released properly.
	 */
	public void close() {
		G.Log(TAG,"Close");
		if (mBluetoothGatt == null) {
			return;
		}
		started=false;
		mBluetoothGatt.close();
		//mBluetoothGatt = null;
		//context.unregisterReceiver(mBroadcastReceiver);

	}



	private void enableCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
		boolean characteristicWriteSuccess = gatt.setCharacteristicNotification(characteristic, true);
		if (characteristicWriteSuccess) {
			//if (BluetoothUtils.isDataHopCharacteristic(characteristic)) {
				if(!mInitialized){
					G.Log(TAG, "Requesting MTU CHANGE");
					mInitialized = true;
					gatt.requestMtu(512);
				}
			//}
		} else {
			G.Log(TAG,"Characteristic notification set failure for " + characteristic.getUuid().toString());
		}
	}


	private void readCharacteristic(BluetoothGattCharacteristic characteristic) {

        byte[] messageBytes = characteristic.getValue();
        String message = StringUtils.stringFromBytes(messageBytes);
		G.Log(TAG, "Message from remote: " + message +" pending:"+pendingWrite);
		sending=false;
        if (message == null) {
            G.Log(TAG, "Unable to convert bytes to string");
            return;
        }
		pendingWrite--;
		if (Arrays.equals(new byte[]{0x00}, messageBytes)){
            lListener.linkNetworkSameDiscovered(device.getAddress());
            if (pendingWrite<=0)
				disconnect();
            tryConnection();
        }else {
        	G.Log(TAG, "Attempting to connect");
			lListener.linkNetworkDiscovered(message);
			disconnect();
			//started=false;
			//close();
			//results.clear();
		}

	}

	private void sendMessage() {
		if (mConnectionState != STATE_CONNECTED || !mInitialized) {
			G.Log(TAG,"Not initialized.");
			return;
		}

		//BluetoothGattCharacteristic characteristic = BluetoothUtils.findDataHopCharacteristic(mBluetoothGatt,mServiceUUID.getUuid());
		List<BluetoothGattCharacteristic> characteristics = BluetoothUtils.findCharacteristics(mBluetoothGatt,mServiceUUID.getUuid(),db.getGroups());
		pendingWrite+=characteristics.size();
		G.Log(TAG,"Found "+pendingWrite+" characteristics. TryWriting");
		Runnable r = new TryWriting(characteristics);
		new Thread(r).start();

	}

	// Implements callback methods for GATT events that the app cares about.  For example,
	// connection change and services discovered.
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED&&mConnectionState!=BluetoothProfile.STATE_CONNECTED) {
				mConnectionState = STATE_CONNECTED;
				G.Log(TAG, "Connected to GATT server: "+gatt.getDevice().getAddress());
				int con = stats.getBtConnections();
				stats.setBtConnections(++con);
				if(mBluetoothGatt!=null)mBluetoothGatt.discoverServices();

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				mConnectionState = STATE_DISCONNECTED;
				G.Log(TAG, "Disconnected from GATT server.");
				if(mBluetoothGatt!=null)mBluetoothGatt.close();
				if(started)tryConnection();
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {

			super.onServicesDiscovered(gatt, status);

			if (status != BluetoothGatt.GATT_SUCCESS) {
				G.Log(TAG,"Device service discovery unsuccessful, status " + status);
				return;
			}

			G.Log(TAG,"Gatt "+gatt.getServices().size());

			List<BluetoothGattCharacteristic> matchingCharacteristics = BluetoothUtils.findCharacteristics(gatt,mServiceUUID.getUuid(),db.getGroups());
			if (matchingCharacteristics.isEmpty()) {
				G.Log(TAG,"Unable to find characteristics.");
				return;
			}

			for (BluetoothGattCharacteristic characteristic : matchingCharacteristics) {
				G.Log(TAG, "characteristic: " + characteristic.getUuid().toString());
				//characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
				characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
				//characteristic.setWriteType(BluetoothGattCharacteristic.PARCELABLE_WRITE_RETURN_VALUE);
				subscribe(characteristic);
				enableCharacteristicNotification(gatt, characteristic);

			}
		}

		@Override
		public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
			super.onMtuChanged(gatt, mtu, status);
			G.Log(TAG, "ON MTU CHANGED");
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if(mInitialized)sendMessage();
			}
		}


		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
											BluetoothGattCharacteristic characteristic) {
			G.Log(TAG, "onCharacteristicChanged received: "+characteristic.getUuid().toString());
			readCharacteristic(characteristic);

		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			super.onDescriptorWrite(gatt, descriptor, status);

			G.Log(TAG, "On Descriptor Write");
			if(status == BluetoothGatt.GATT_SUCCESS){
				G.Log(TAG, "On Descriptor Write - GATT SUCCESS");
				//sendMessage(gatt);
			}
		}


		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
										 BluetoothGattCharacteristic characteristic,
										 int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				G.Log(TAG, "Characteristic Read");
				printIncoming(characteristic);
			}
		}
	};


	private void printIncoming(final BluetoothGattCharacteristic characteristic) {
		G.Log(TAG, "\n\nBroadcast Update Printing Services!!!\n\n");
		for(BluetoothGattService b : characteristic.getService().getIncludedServices()) {
			G.Log(TAG, b.toString());
		}

		if(Constants.CHARACTERISTIC_DATAHOP_UUID.equals(characteristic.getUuid())) {
			// For all other profiles, writes the data formatted in HEX.
			G.Log(TAG, "Received data from Bleno...\n");
			final byte[] data = characteristic.getValue();
			if (data != null && data.length > 0) {
				final StringBuilder stringBuilder = new StringBuilder(data.length);
				for(byte byteChar : data)
					stringBuilder.append(String.format("%02X ", byteChar));
				G.Log(TAG,"Received Data OutPut\n");
				G.Log(TAG,stringBuilder.toString());
				G.Log(TAG, "Data received: " + new String(data));
			}
		}
	}

	private void subscribe(BluetoothGattCharacteristic characteristic) {
		Log.d(TAG, "Subscribing on characteristic");

		if (characteristic == null) {
			Log.d(TAG,"Characteristic does not exist");
			return;
		}

		BluetoothGattDescriptor descriptor = characteristic.getDescriptor(Constants.CLIENT_CONFIGURATION_DESCRIPTOR_UUID);
		if(descriptor!=null){
			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor);
		}
	}

	private class TryWriting implements Runnable {

		List<BluetoothGattCharacteristic> characteristics;

		public TryWriting (List<BluetoothGattCharacteristic> characteristics)
		{
			this.characteristics = characteristics;
		}

		@Override
		public void run() {

			G.Log(TAG,"Trywriting "+characteristics.size()+" "+started);
			for (BluetoothGattCharacteristic characteristic : characteristics) {
				//if (characteristic == null||!started) {
				if (characteristic == null){
					G.Log(TAG, "Unable to find echo characteristic.");
					disconnect();
					return;
				}

				if(!started)return;

				ContentAdvertisement cAdv = ca.get(characteristic.getUuid());
				if(cAdv!=null) {
					byte[] messageBytes = cAdv.getFilterBytes();

					G.Log(TAG, "Sending message: " + new String(messageBytes) + " " + messageBytes.length + " " + characteristic.getUuid().toString());

					if (messageBytes.length == 0) {
						G.Log(TAG, "Unable to convert message to bytes");
						return;
					}

					characteristic.setValue(messageBytes);
					mInitialized = false;
					sending = true;
					//mBluetoothGatt.getDevice().
					//mBluetoothGatt.abortReliableWrite();
					boolean success = mBluetoothGatt.writeCharacteristic(characteristic);
					int tries = 0;
					while (started && !success && tries < 5) {
						try {
							sleep(1000);
							G.Log(TAG, "Failed retry " + tries);
							success = mBluetoothGatt.writeCharacteristic(characteristic);
							tries++;
						} catch (InterruptedException e) {
						}
					}
					if (started && !success) G.Log(TAG, "Failed to write data");
					while (sending) try {
						sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}


	//endregion
} // BtServiceDiscovery


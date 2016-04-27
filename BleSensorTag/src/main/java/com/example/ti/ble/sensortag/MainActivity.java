package com.example.ti.ble.sensortag;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.example.ti.ble.common.BleDeviceInfo;
import com.example.ti.ble.common.BluetoothLeService;
import com.example.ti.ble.common.HCIDefines;
import com.example.ti.ble.common.HelpView;
import com.example.ti.util.CustomToast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends ViewPagerActivity {
	// Log
	// private static final String TAG = "HomeScreen";

	// URLs
	private static final Uri URL_FORUM = Uri
			.parse("http://e2e.ti.com/support/low_power_rf/default.aspx?DCMP=hpa_hpa_community&HQS=NotApplicable+OT+lprf-forum");
	private static final Uri URL_STHOME = Uri
			.parse("http://www.ti.com/ww/en/wireless_connectivity/sensortag/index.shtml?INTC=SensorTagGatt&HQS=sensortag");

	// Requests to other activities
	private static final int REQ_ENABLE_BT = 0;
	private static final int REQ_DEVICE_ACT = 1;

	// GUI
	private ScanView mScanView;
	private Intent mDeviceIntent;
	private static final int STATUS_DURATION = 5;

	// BLE management
	private boolean mBtAdapterEnabled = false;
	private boolean mBleSupported = true;
	private boolean mScanning = false;
	private int mNumDevs = 0;
	private int mConnIndex = NO_DEVICE;
	private List<BleDeviceInfo> xDeviceInfoList;
	private static BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBtAdapter = null;
	private BluetoothDevice mBluetoothDevice = null;
	private BluetoothLeService mBluetoothLeService = null;
	private IntentFilter mFilter;
	private String[] mDeviceFilter = null;
	public String Minor;
	public String Major;
	public String UUID1;
	public int major = 0;
	public int minor =0;
	public int txPower =-55;
	public String uuid="";
	public double dist;
	public int SumRssi=0;
	public int counterRssi=0;
	public RangedIBeacon rangedIBeacon;
	private Map<BleDeviceInfo,RangedIBeacon> rangedIBeacons = new HashMap<BleDeviceInfo,RangedIBeacon>();
	public BleDeviceInfo[] DeviceArray =new BleDeviceInfo[3];
	public double [][]Locations;
	public String [][]StrLocations;
	// Housekeeping
	private static final int NO_DEVICE = -1;
	private boolean mInitialised = false;
	SharedPreferences prefs = null;
	private static MainActivity mThis = null;
	public MainActivity() {
		mThis = this;
		mResourceFragmentPager = R.layout.fragment_pager;
		mResourceIdPager = R.id.pager;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);


		// Initialize device list container and device filter
		xDeviceInfoList = new ArrayList<BleDeviceInfo>();
		Resources res = getResources();
		mDeviceFilter = res.getStringArray(R.array.device_filter);

		// Create the fragments and add them to the view pager and tabs
		mScanView = new ScanView();
		mSectionsPagerAdapter.addSection(mScanView, "BLE Device List");

		HelpView hw = new HelpView();
		hw.setParameters("help_scan.html", R.layout.fragment_help, R.id.webpage);
		mSectionsPagerAdapter.addSection(hw, "Help");

		Locations=new double[3][2];

		Intent i = getIntent();
		Bundle b = getIntent().getExtras();
		//****************************************************************************************
		String previousActivity= i.getStringExtra("FROM_ACTIVITY");
		if(previousActivity.equals("A"))
		{
			String[][] arrayReceived=null;
			Object[] objectArray = (Object[]) getIntent().getExtras().getSerializable("Array");
			if(objectArray!=null){
				arrayReceived = new String[objectArray.length][];
				for(int j=0;j<objectArray.length;j++){
					arrayReceived[j]=(String[]) objectArray[j];
					Log.d("CalibrationActivity", "6666666666666666666666666666666677777777777777777778888888888888888888888888");
				}
			}


			//******************************************************************************************

			//   StrLocations = (String[][]) b.getSerializable("Array");
			for(int n = 0; n < 3; n++)
				for(int m = 0; m < 2; m++)
				{
					Locations[n][m] = Double.parseDouble(arrayReceived[n][m]);
					Log.d("CalibrationActivity", "2222222222222222222222777777777777777777788888888888"+Locations[n][m]);
				}
			//////////////////////////////////////////***************************************************************

		}



		// Register the BroadcastReceiver
		mFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		mFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		mFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);

		Locations=new double[3][2];
		StrLocations=new String[3][2];
	}



	@Override
	public void onDestroy() {
		// Log.e(TAG,"onDestroy");
		super.onDestroy();


		mBtAdapter = null;

		// Clear cache
		File cache = getCacheDir();
		String path = cache.getPath();
		try {
			Runtime.getRuntime().exec(String.format("rm -rf %s", path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
			case R.id.opt_bt:
				onBluetooth();
				break;
			case R.id.opt_e2e:
				onUrl(URL_FORUM);
				break;
			case R.id.opt_sthome:
				onUrl(URL_STHOME);
				break;
			case R.id.opt_license:
				onLicense();
				break;
			case R.id.opt_about:
				onAbout();
				break;
			case R.id.opt_exit:
				Toast.makeText(this, "Exit...", Toast.LENGTH_SHORT).show();
				finish();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.optionsMenu = menu;
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private void onUrl(final Uri uri) {
		Intent web = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(web);
	}

	private void onBluetooth() {
		Intent settingsIntent = new Intent(
				android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
		startActivity(settingsIntent);
	}

	private void onLicense() {
		final Dialog dialog = new LicenseDialog(this);
		dialog.show();
	}

	private void onAbout() {
		final Dialog dialog = new AboutDialog(this);
		dialog.show();
	}

	void onScanViewReady(View view) {


		// License popup on first run
		if (prefs.getBoolean("firstrun", true)) {
			onLicense();
			prefs.edit().putBoolean("firstrun", false).commit();
		}

		if (!mInitialised) {
			// Broadcast receiver
			mBluetoothLeService = BluetoothLeService.getInstance();
			mBluetoothManager = mBluetoothLeService.getBtManager();
			mBtAdapter = mBluetoothManager.getAdapter();
			registerReceiver(mReceiver, mFilter);
			mBtAdapterEnabled = mBtAdapter.isEnabled();
			if (mBtAdapterEnabled) {
				// Start straight away
				//startBluetoothLeService();
			} else {
				// Request BT adapter to be turned on
				Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQ_ENABLE_BT);
			}
			mInitialised = true;
		} else {
			mScanView.notifyDataSetChanged();
		}
		// Initial state of widgets
		updateGuiState();
	}

	public void onBtnScan(View view) {
		if (mScanning) {
			stopScan();
		} else {
			startScan();
		}
	}

	void onConnect() {
		if (mNumDevs > 0) {

			int connState = mBluetoothManager.getConnectionState(mBluetoothDevice,
					BluetoothGatt.GATT);

			switch (connState) {
				case BluetoothGatt.STATE_CONNECTED:
					mBluetoothLeService.disconnect(null);
					break;
				case BluetoothGatt.STATE_DISCONNECTED:
					boolean ok = mBluetoothLeService.connect(mBluetoothDevice.getAddress());
					if (!ok) {
						setError("Connect failed");
					}
					break;
				default:
					setError("Device busy (connecting/disconnecting)");
					break;
			}
		}
	}

	private void startScan() {
		// Start device discovery
		if (mBleSupported) {
			mNumDevs = 0;
			xDeviceInfoList.clear();
			mScanView.notifyDataSetChanged();
			scanLeDevice(true);
			mScanView.updateGui(mScanning);
			if (!mScanning) {
				setError("Device discovery start failed");
				setBusy(false);
			}
		} else {
			setError("BLE not supported on this device");
		}

	}

	private void stopScan() {
		double dist1,dist2,dist3;
		double distance[] = new double[3];
		double rssi[] = new double[3];
		mScanning = false;
		mScanView.updateGui(false);
		scanLeDevice(false);
		location loc= new location();
		double lat =  (loc.getLatg());
		double lng =  (loc.getLang());
		Trilateration  tri;

		//      dist=calculateAccuracy(-70,SumRssi);
		Log.d("CalibrationActivity", "66666666666666666666666666666666  latitude:" +lat);
		Log.d("CalibrationActivity", "66666666666666666666666666666666  longitude:" + lng);

		xDeviceInfoList.get(0).setAvaragedRssi(rangedIBeacons.get(DeviceArray[0]).getAvaragedRssi());
		//      for (int i = 0; i < xDeviceInfoList.size(); i++) {
		distance[0]= xDeviceInfoList.get(0).getAccuracy();
		//         distance[1]= xDeviceInfoList.get(1).getAccuracy();
		//         distance[2]= xDeviceInfoList.get(2).getAccuracy();
		rssi[0]= rangedIBeacons.get(DeviceArray[0]).getAvaragedRssi();//addRangeMeasurement((int) deviceInfo.getRssi());
		//        rssi[0]= xDeviceInfoList.get(0).getAvaragedRssi();
		//        rssi[1]= xDeviceInfoList.get(1).getRssi();
		//       rssi[2]= xDeviceInfoList.get(2).getRssi();
		//      dist2=xDeviceInfoList.get(1).getdistance();
		//      dist3=xDeviceInfoList.get(2).getdistance();

		Log.d("MainActivity", " distance: " + distance[0] + " RSSI" + rssi[0] + "  deviceRSSI:" + xDeviceInfoList.get(0).getAvaragedRssi());
//		Trilateration.MyTrilateration(lng, lat, rssi[0], distance[0], lng, lat, rssi[1], distance[1], lng, lat, rssi[2], distance[2]);
		for (int n = 0; n < 3; n++)
			for (int m = 0; m < 2; m++)
				StrLocations[n][m] = String.valueOf(Locations[n][m]);
		startBeaconStatusActivity();
		//*******************************************************************************************************************************
		Log.d("CalibrationActivity","999999999999999999999999999999999");
		//    }
	}
	private void startBeaconStatusActivity() {
//        CalibrationActivity appContext = (CalibrationActivity) getApplicationContext();
		///      appContext.xDeviceInfoList= xDeviceInfoList;
		Intent i =  new Intent(this, CheckBeaconStatus.class);
		i.putParcelableArrayListExtra("list", (ArrayList<? extends Parcelable>) xDeviceInfoList);
		i.putExtra(DeviceActivity.EXTRA_DEVICE, mBluetoothDevice);
  		Bundle mBundle = new Bundle();
		mBundle.putSerializable("Array",  StrLocations);
		i.putExtras(mBundle);
  /*      Bundle b= new Bundle();
        b.putParcelableArrayList("list", (ArrayList<? extends Parcelable>) mDeviceInfoList);
        i.putExtras(b);
        i.setClass(CalibrationActivity.this, BeaconStatus.class);
  */      startActivity(i);
	}

	//*******************************************************************************************************************************
	private void startDeviceActivity() {
		mDeviceIntent = new Intent(this, DeviceActivity.class);
		mDeviceIntent.putExtra(DeviceActivity.EXTRA_DEVICE, mBluetoothDevice);
		startActivityForResult(mDeviceIntent, REQ_DEVICE_ACT);
	}

	private void stopDeviceActivity() {
		finishActivity(REQ_DEVICE_ACT);
	}
	//****************************************************************************************************************************
	public void onDeviceClick(final int pos) {

		if (mScanning)
			stopScan();

		setBusy(true);
		mBluetoothDevice = xDeviceInfoList.get(pos).getBluetoothDevice();
		if (mConnIndex == NO_DEVICE) {
			mScanView.setStatus("Connecting");
			mConnIndex = pos;
			onConnect();
		} else {
			mScanView.setStatus("Disconnecting");
			if (mConnIndex != NO_DEVICE) {
				mBluetoothLeService.disconnect(mBluetoothDevice.getAddress());
			}
		}
	}
	//*******************************************************************************************************************************
	public void onScanTimeout() {
		runOnUiThread(new Runnable() {
			public void run() {
				stopScan();
			}
		});
	}

	public void onConnectTimeout() {
		runOnUiThread(new Runnable() {
			public void run() {
				setError("Connection timed out");
			}
		});
		if (mConnIndex != NO_DEVICE) {
			mBluetoothLeService.disconnect(mBluetoothDevice.getAddress());
			mConnIndex = NO_DEVICE;
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// GUI methods
	//
	public void updateGuiState() {
		boolean mBtEnabled = mBtAdapter.isEnabled();

		if (mBtEnabled) {
			if (mScanning) {
				// BLE Host connected
				if (mConnIndex != NO_DEVICE) {
					String txt = mBluetoothDevice.getName() + " connected";
					mScanView.setStatus(txt);
				} else {
					mScanView.setStatus(mNumDevs + " devices");
				}
			}
		} else {
			xDeviceInfoList.clear();
			mScanView.notifyDataSetChanged();
			Log.d("CalibrationActivity","2222222222222222222222222222222222222");
		}
	}

	private void setBusy(boolean f) {
		mScanView.setBusy(f);
	}

	void setError(String txt) {
		mScanView.setError(txt);
		//CustomToast.middleBottom(this, "Turning BT adapter off and on again may fix Android BLE stack problems");
	}

	private BleDeviceInfo createDeviceInfo(BluetoothDevice device, int rssi, int major,int minor, String UUID1, int txPower, double dist) {
		BleDeviceInfo deviceInfo = new BleDeviceInfo(device, rssi, major,minor,UUID1,txPower);

		return deviceInfo;
	}

	boolean checkDeviceFilter(String deviceName) {
		if (deviceName == null)
			return false;

		int n = mDeviceFilter.length;
		if (n > 0) {
			boolean found = false;
			for (int i = 0; i < n && !found; i++) {
				found = deviceName.equals(mDeviceFilter[i]);
			}
			return found;
		} else
			// Allow all devices if the device filter is empty
			return true;
	}

	private void addDevice(BleDeviceInfo device) {
		mNumDevs++;
		xDeviceInfoList.add(device);
		mScanView.notifyDataSetChanged();
		if (mNumDevs > 1)
			mScanView.setStatus(mNumDevs + " devices");
		else
			mScanView.setStatus("1 device");
	}

	private boolean deviceInfoExists(String address) {
		for (int i = 0; i < xDeviceInfoList.size(); i++) {
			if (xDeviceInfoList.get(i).getBluetoothDevice().getAddress()
					.equals(address)) {
				return true;
			}
		}
		return false;
	}

	private BleDeviceInfo findDeviceInfo(BluetoothDevice device) {
		for (int i = 0; i < xDeviceInfoList.size(); i++) {
			if (xDeviceInfoList.get(i).getBluetoothDevice().getAddress()
					.equals(device.getAddress())) {
				return xDeviceInfoList.get(i);
			}
		}
		return null;
	}

	private boolean scanLeDevice(boolean enable) {
		if (enable) {
			mScanning = mBtAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBtAdapter.stopLeScan(mLeScanCallback);
		}
		return mScanning;
	}

	List<BleDeviceInfo> getDeviceInfoList() {
		return xDeviceInfoList;
	}



	// Activity result handling
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case REQ_DEVICE_ACT:
				// When the device activity has finished: disconnect the device
				if (mConnIndex != NO_DEVICE) {
					mBluetoothLeService.disconnect(mBluetoothDevice.getAddress());
				}
				break;

			case REQ_ENABLE_BT:
				// When the request to enable Bluetooth returns
				if (resultCode == Activity.RESULT_OK) {

					Toast.makeText(this, R.string.bt_on, Toast.LENGTH_SHORT).show();
				} else {
					// User did not enable Bluetooth or an error occurred
					Toast.makeText(this, R.string.bt_not_on, Toast.LENGTH_SHORT).show();
					finish();
				}
				break;
			default:
				CustomToast.middleBottom(this, "Unknown request code: " + requestCode);

				// Log.e(TAG, "Unknown request code");
				break;
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Broadcasted actions from Bluetooth adapter and BluetoothLeService
	//
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				// Bluetooth adapter state change
				switch (mBtAdapter.getState()) {
					case BluetoothAdapter.STATE_ON:
						mConnIndex = NO_DEVICE;
						//startBluetoothLeService();
						break;
					case BluetoothAdapter.STATE_OFF:
						Toast.makeText(context, R.string.app_closing, Toast.LENGTH_LONG)
								.show();
						finish();
						break;
					default:
						// Log.w(TAG, "Action STATE CHANGED not processed ");
						break;
				}

				updateGuiState();
			} else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				// GATT connect
				int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS,
						BluetoothGatt.GATT_FAILURE);
				if (status == BluetoothGatt.GATT_SUCCESS) {
					setBusy(false);
					startDeviceActivity();
					//                startBeaconStatusActivity();
					//***************************************************************************************************************
				} else
					setError("Connect failed. Status: " + status);
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				// GATT disconnect
				int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS,
						BluetoothGatt.GATT_FAILURE);
				stopDeviceActivity();
				if (status == BluetoothGatt.GATT_SUCCESS) {
					setBusy(false);
					mScanView.setStatus(mBluetoothDevice.getName() + " disconnected",
							STATUS_DURATION);
				} else {
					setError("Disconnect Status: " + HCIDefines.hciErrorCodeStrings.get(status));
				}
				mConnIndex = NO_DEVICE;
				mBluetoothLeService.close();
			} else {
				// Log.w(TAG,"Unknown action: " + action);
			}

		}
	};


	// Device scan callback.
	// NB! Nexus 4 and Nexus 7 (2012) only provide one scan result per scan
	public BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		public void onLeScan(final BluetoothDevice device, final int rssi,
							 final byte[] scanRecord) {
			Major="";
			Minor="";
			UUID1="";

			int startByte = 0;
			final String finalUuid = uuid;
			runOnUiThread(new Runnable() {
				public void run() {
					// Filter devices
					int i=0;
					if (checkDeviceFilter(device.getName())) {
						Log.d("MainActivity", "Entra en onLeScan55555555555555555555555555555555555555");
						byte[] uuidBytes = new byte[16];
						System.arraycopy(scanRecord, 9, uuidBytes, 0, 16);
						String hexString = bytesToHex(uuidBytes);

						//Here is your UUID
						uuid = hexString.substring(0, 8) + "-" +
								hexString.substring(8, 12) + "-" +
								hexString.substring(12, 16) + "-" +
								hexString.substring(16, 20) + "-" +
								hexString.substring(20, 32);

						//Here is your Major value
						major = (scanRecord[25] & 0xff) * 0x100 + (scanRecord[26] & 0xff);

						//Here is your Minor value
						minor = (scanRecord[27] & 0xff) * 0x100 + (scanRecord[28] & 0xff);

						txPower= (int)scanRecord[29];
						//              SumRssi+=rssi;
						//              counterRssi+=1;
						//              Log.d("MainActivity", "Got a didExitRegion call with MAJOR:" + major + " MINOR: " + minor + " TXPOWER: " + txPower +" and UUID: " + uuid);
						//              dist=calculateAccuracy(-49,rssi);
						//              dist=calDistToDeg(calFeetToMeter(calcDistance(rssi)));

						Log.d("MainActivity", " MAJOR:" + major + " MINOR: " + minor + " TXPOWER: " + txPower +" and UUID: " + uuid +" distance: "+dist);
    /*
							String major = String.format("%02x", scanRecord[25]) + String.format("%02x", scanRecord[26]);
        String minor = String.format("%02x", scanRecord[27]) + String.format("%02x", scanRecord[28]);
							for (int i = 0; i<scanRecord.length; i++){
								if(i>8 && i<25)
									UUID1 += String.format("%02x", scanRecord[i]);
								else if(i>24 && i<27)
									Major += String.format("%02x", scanRecord[i]);
								else if(i>26 && i<29)
									Minor += String.format("%02x", scanRecord[i]);



							}*/


						if (!deviceInfoExists(device.getAddress())) {
							// New device
							BleDeviceInfo deviceInfo = createDeviceInfo(device, rssi, major, minor, uuid,txPower,dist);
							addDevice(deviceInfo);
							rangedIBeacons.put(deviceInfo, new RangedIBeacon(deviceInfo));
						} else {
							// Already in list, update RSSI info
							BleDeviceInfo deviceInfo = findDeviceInfo(device);
							DeviceArray[i++]=deviceInfo;
							rangedIBeacons.get(deviceInfo).addRangeMeasurement((int) deviceInfo.getRssi());

							Log.d("MainActivity", "555 running avarage rssi: "+rangedIBeacons.get(deviceInfo).getAvaragedRssi() );
							deviceInfo.updateRssi(rssi);
							mScanView.notifyDataSetChanged();
						}


					}
				}

			});
		}
		final protected char[] hexArray = "0123456789ABCDEF".toCharArray();
		public String bytesToHex(byte[] bytes) {
			char[] hexChars = new char[bytes.length * 2];
			for ( int j = 0; j < bytes.length; j++ ) {
				int v = bytes[j] & 0xFF;
				hexChars[j * 2] = hexArray[v >>> 4];
				hexChars[j * 2 + 1] = hexArray[v & 0x0F];
			}
			return new String(hexChars);
		}

	};
//*%%%%%%%%%%%%%%%%%%%%%$
}

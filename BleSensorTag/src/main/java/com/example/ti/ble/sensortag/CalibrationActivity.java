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
import java.util.List;

public class CalibrationActivity extends ViewPagerActivity {
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
    private CalibrationScanView mScanView;
    private Intent mDeviceIntent;
    private static final int STATUS_DURATION = 5;

    // BLE management
    private boolean mBtAdapterEnabled = false;
    private boolean mBleSupported = true;
    private boolean mScanning = false;
    private int mNumDevs = 0;
    private int mConnIndex = NO_DEVICE;
    private List<BleDeviceInfo> mDeviceInfoList;
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
    // Housekeeping
    private static final int NO_DEVICE = -1;
    private boolean mInitialised = false;
    SharedPreferences prefs = null;
    private static CalibrationActivity mThis = null;
    public CalibrationActivity() {
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
        mDeviceInfoList = new ArrayList<BleDeviceInfo>();
        Resources res = getResources();
        mDeviceFilter = res.getStringArray(R.array.device_filter);

        // Create the fragments and add them to the view pager and tabs
        mScanView = new CalibrationScanView();
        mSectionsPagerAdapter.addSection(mScanView, "BLE Device List");

        HelpView hw = new HelpView();
        hw.setParameters("help_scan.html", R.layout.fragment_help, R.id.webpage);
        mSectionsPagerAdapter.addSection(hw, "Help");

        // Register the BroadcastReceiver
        mFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        mFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
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
            mDeviceInfoList.clear();
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
        SumRssi=SumRssi/counterRssi;
        dist=calculateAccuracy(-70,SumRssi);
        Log.d("MainActivity", " distance: "+dist +" RSSI"+SumRssi);

        //      for (int i = 0; i < mDeviceInfoList.size(); i++) {
            distance[0]= mDeviceInfoList.get(0).getdistance();
            distance[1]= mDeviceInfoList.get(1).getdistance();
            distance[2]= mDeviceInfoList.get(2).getdistance();
            rssi[0]= mDeviceInfoList.get(0).getRssi();
            rssi[1]= mDeviceInfoList.get(1).getRssi();
            rssi[2]= mDeviceInfoList.get(2).getRssi();
      //      dist2=mDeviceInfoList.get(1).getdistance();
      //      dist3=mDeviceInfoList.get(2).getdistance();
            Trilateration.MyTrilateration(lng,lat,rssi[0],distance[0],lng,lat,rssi[1],distance[1],lng,lat,rssi[2],distance[2]);
  //*******************************************************************************************************************************
            Log.d("CalibrationActivity","999999999999999999999999999999999");
    //    }
    }

    private void startDeviceActivity() {
        mDeviceIntent = new Intent(this, DeviceActivity.class);
        mDeviceIntent.putExtra(DeviceActivity.EXTRA_DEVICE, mBluetoothDevice);
        startActivityForResult(mDeviceIntent, REQ_DEVICE_ACT);
    }

    private void stopDeviceActivity() {
        finishActivity(REQ_DEVICE_ACT);
    }

    public void onDeviceClick(final int pos) {

        if (mScanning)
            stopScan();

        setBusy(true);
        mBluetoothDevice = mDeviceInfoList.get(pos).getBluetoothDevice();
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
            mDeviceInfoList.clear();
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
        BleDeviceInfo deviceInfo = new BleDeviceInfo(device, rssi, major,minor,UUID1,txPower,dist);

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
        mDeviceInfoList.add(device);
        mScanView.notifyDataSetChanged();
        if (mNumDevs > 1)
            mScanView.setStatus(mNumDevs + " devices");
        else
            mScanView.setStatus("1 device");
    }

    private boolean deviceInfoExists(String address) {
        for (int i = 0; i < mDeviceInfoList.size(); i++) {
            if (mDeviceInfoList.get(i).getBluetoothDevice().getAddress()
                    .equals(address)) {
                return true;
            }
        }
        return false;
    }

    private BleDeviceInfo findDeviceInfo(BluetoothDevice device) {
        for (int i = 0; i < mDeviceInfoList.size(); i++) {
            if (mDeviceInfoList.get(i).getBluetoothDevice().getAddress()
                    .equals(device.getAddress())) {
                return mDeviceInfoList.get(i);
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
        return mDeviceInfoList;
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

    protected static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        Log.d("TAG", "calculating accuracy based on rssi of "+rssi);


        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            Log.d("TAG", " avg rssi: "+rssi+" accuracy: "+accuracy);
            return accuracy;
        }
    }

    static double calcDistance(double rssi) {
        double base = 10;
        double exponent = -(rssi + 51.504)/16.532;
        //double distance = Math.pow(base, exponent);
        //104.09004338 + 13.26842562x + 0.57250833x^2 + 0.00986120x^3 + 0.00006099x^4

        // SI NORTH THIRD FLOOR (room 3250)
//		  double distance = 104.09004338 + 13.26842562 * rssi + 0.57250833* Math.pow(rssi,2)
//		        + 0.00986120*Math.pow(rssi, 3) + 0.00006099 * Math.pow(rssi,4);

        // SI NORTH FIRST FLOOR
        // 0 degree
        //double distance = 3324.4981666 + 234.0366524 * rssi + 6.0593624* Math.pow(rssi,2)
        //  + 0.0683264*Math.pow(rssi, 3) + 0.0002843 * Math.pow(rssi,4);

        double distance = 730.24198315 + 52.33325511*rssi + 1.35152407*Math.pow(rssi, 2)
                + 0.01481265*Math.pow(rssi, 3) + 0.00005900*Math.pow(rssi, 4) + 0.00541703*180;


        //return (distance>0)?distance:rssi;
        return distance;
    }

    static double calFeetToMeter(double rssi) {
        return rssi*0.3048;
    }

    static double calDistToDeg(double dist) {
        double result;
        double DistToDeg;

        final int lat = 42;
        final double EarthRadius = 6367449;
        final double a = 6378137;
        final double b = 6356752.3;
        final double ang = lat*(Math.PI/180);

        // This function will calculate the longitude distance based on the latitude
        // More information is
        // http://en.wikipedia.org/wiki/Geographic_coordinate_system#Expressing_latitude_and_longitude_as_linear_units

//		 result = Math.cos(ang)*Math.sqrt((Math.pow(a,4)*(Math.pow(Math.cos(ang),2))
//				 + (Math.pow(b,4)*(Math.pow(Math.sin(ang),2))))
//				 / (Math.pow((a*Math.cos(ang)),2)+Math.pow((b*Math.sin(ang)),2)))
//				 * Math.PI/180;

        DistToDeg = 82602.89223259855;  // unit (meter), based on 42degree.
        result = dist/DistToDeg;		 // convert distance to lat,long degree.
        return result;

    }
/*
    protected static int calculateProximity(double accuracy) {
        if (accuracy < 0) {
            return PROXIMITY_UNKNOWN;
            // is this correct?  does proximity only show unknown when accuracy is negative?  I have seen cases where it returns unknown when
            // accuracy is -1;
        }
        if (accuracy < 0.5 ) {
            return IBeacon.PROXIMITY_IMMEDIATE;
        }
        // forums say 3.0 is the near/far threshold, but it looks to be based on experience that this is 4.0
        if (accuracy <= 4.0) {
            return IBeacon.PROXIMITY_NEAR;
        }
        // if it is > 4.0 meters, call it far
        return IBeacon.PROXIMITY_FAR;

    }*/
    // Device scan callback.
    // NB! Nexus 4 and Nexus 7 (2012) only provide one scan result per scan
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        public void onLeScan(final BluetoothDevice device, final int rssi,
                             final byte[] scanRecord) {
            Major="";
            Minor="";
            UUID1="";

            int startByte = 0;
            boolean patternFound = true;
	/*		while (startByte <= 15) {
				if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
						((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
					patternFound = true;
					Log.d("MainActivity", "flag111111111" + startByte);
					break;
				}
				startByte++;
				Log.d("MainActivity", "count" + startByte+ ":::"+ (scanRecord[startByte + 2] & 0xff));
			}
		*/	if (patternFound) {

            }
            final String finalUuid = uuid;
            runOnUiThread(new Runnable() {
                public void run() {
                    // Filter devices
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
                        SumRssi+=rssi;
                        counterRssi+=1;
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
                        } else {
                            // Already in list, update RSSI info
                            BleDeviceInfo deviceInfo = findDeviceInfo(device);
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

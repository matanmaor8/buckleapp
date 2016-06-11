package com.example.ti.ble.sensortag;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.ti.ble.common.BleDeviceInfo;
import com.example.ti.ble.common.BluetoothLeService;

import java.util.ArrayList;
import java.util.List;

public class CheckBeaconStatus extends Activity {
    private List<BleDeviceInfo> mDevices;
    private static final int REQ_DEVICE_ACT = 1;
    public static final String EXTRA_DEVICE_LIST = "EXTRA_DEVICE_LIST";
    BleDeviceInfo deviceInfo;
    BluetoothDevice device;
    private CheckBox ch1, ch2, ch3;
    CalibrationActivity Cactivity;

    private double[][] wifiLocation;
    //	private double myLatitude, myLongitude;
    private double[] myLocation = new double[2];
//    public double [][] xLocations=new double[3][2];;
//    public String [][]StrLocations;
    public String []StrLocation;
    double distance[] = new double[3];
    double rssi[] = new double[3];
    location loc = new location();
    Trilateration tri;
    //   public MyLocationListener locationListener;
    private String lac;
    private String lng;
    private IntentFilter mFilter;
    private String[] mDeviceFilter = null;
    private BluetoothDevice mBluetoothDevice = null;
    private static BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothLeService mBluetoothLeService = null;
    private int mConnIndex = NO_DEVICE;
    private static final int NO_DEVICE = -1;
    double longitude, latitude;
    private String TPID;
    private Button Startbtn;
    private Button btn;

    private Intent mDeviceIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_beacon_status);
        ch1 = (CheckBox) findViewById(R.id.checkBox4);
        ch2 = (CheckBox) findViewById(R.id.checkBox5);
        ch3 = (CheckBox) findViewById(R.id.checkBox6);
        btn = (Button) findViewById(R.id.button6);
        Startbtn= (Button) findViewById(R.id.button);
        Intent i = getIntent();
        mDevices = (List) i.getParcelableArrayListExtra("list");
        mBluetoothDevice = i.getParcelableExtra(DeviceActivity.EXTRA_DEVICE);
        Bundle b = getIntent().getExtras();
        //****************************************************************************************
 /*       String[][] arrayReceived=null;
        Object[] objectArray = (Object[]) getIntent().getExtras().getSerializable("Array");
        if(objectArray!=null){
            arrayReceived = new String[objectArray.length][];
            for(int j=0;j<objectArray.length;j++){
                arrayReceived[j]=(String[]) objectArray[j];
            }
        }
*/
        Object[] objectArray2 = (Object[]) getIntent().getExtras().getSerializable("LocArray");
        if(objectArray2!=null){
            StrLocation = new String[objectArray2.length];
            for(int j=0;j<objectArray2.length;j++){
                StrLocation[j]= (String) objectArray2[j];
            }
        }

        //******************************************************************************************

        //   StrLocations = (String[][]) b.getSerializable("Array");
        for(int n = 0; n < 3; n++)
            for(int m = 0; m < 2; m++)
            {
  //              xLocations[n][m] = Double.parseDouble(arrayReceived[n][m]);
            }
        //////////////////////////////////////////***************************************************************

        //     ArrayList<BleDeviceInfo> Devices =  getIntent().getParcelableArrayListExtra("list");
        //     mDevices=new List<list>();
        //       Bundle b = this.getIntent().getExtras();
        //       if(b!=null)
        //           mDevices = b.getParcelable("list");
        //       mDevices=getIntent().getParcelableExtra("list");
        //      device= Cactivity.mDeviceInfoList.get(0).getBluetoothDevice();
        //    device = deviceInfo.getBluetoothDevice();
        //       rssi = (int) deviceInfo.getAvaragedRssi();
        //      Cactivity = (CalibrationActivity) getActivity();
        //      mDevices= (List<BleDeviceInfo>) intent.getData(EXTRA_DEVICE_LIST);
        //      CalibrationActivity appContext = (CalibrationActivity) getApplicationContext();
        //      mDevices=appContext.getDeviceInfoList();
        //       List<BleDeviceInfo> deviceList = Cactivity.getDeviceInfoList();
        //       mDevices=Cactivity.mDeviceInfoList;
        if (mDevices.get(0).getAvaragedRssi() >= -90.0) {
            Log.d("CalibrationActivity", "999999999999999999999999999999999  avarage RSSI:" + mDevices.get(0).getAvaragedRssi());
            Log.d("CalibrationActivity", "999999999999999999999999999999999  major:" + mDevices.get(0).getmajor());
            Log.d("CalibrationActivity", "999999999999999999999999999999999  minor:" + mDevices.get(0).getminor());
            Log.d("CalibrationActivity", "999999999999999999999999999999999  uuid:" + mDevices.get(0).getUUID1());
            Log.d("CalibrationActivity", "999999999999999999999999999999999  accuracy:" + mDevices.get(0).getAccuracy());
            Log.d("CalibrationActivity", "999999999999999999999999999999999  RSSI:" + mDevices.get(0).getRssi());
            Log.d("CalibrationActivity", "999999999999999999999999999999999  Bluetooth device:" + mDevices.get(0).getBluetoothDevice());
            Log.d("CalibrationActivity", "999999999999999999999999999999999  TXpower:" + mDevices.get(0).gettxPower());
            //         ch1.setChecked(true);
            //         ch2.setChecked(true);
            //         ch3.setChecked(false);


        }
        //      locationListener = new MyLocationListener();

        mFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        mFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        wifiLocation = new double[3][4];
 //       xLocations =new double[3][2];
 //       StrLocations=new String[3][2];
    }

    public void onStart(View view) {
        int i=0;
/*
        for(int i = 0; i < wifiList.size(); i++) {
            ScanResult scan = wifiList.get(i);
            // POI Hash filtering
            // showing only registered WIFI APs
            //loc = "00:40:5a:21:61:59, 123, 456";
//             if ((loc=poiHash.get(scan.BSSID)) != null)
//             {
            int dist = (int) Utilities.calcDistance(scan.level);
            Log.d("Distance", "" + dist);
//
//          	   lat_lng = loc.split(",");
//          	   wifiLocation[i][0] = Double.parseDouble(lat_lng[0]);
//          	   wifiLocation[i][1] = Double.parseDouble(lat_lng[1]);
//          	   wifiLocation[i][2] = scan.level;
//          	   wifiLocation[i][3] = dist;
//
            sb.append(scan.SSID+"\t"+scan.BSSID+"\t"+scan.level+"\t"+dist+"\n");
//          	   //sb.append(poiHash.get(scan.BSSID) +"\n\n");
//              }
        }*/

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        wifiLocation[0][0] = latitude;
        wifiLocation[0][1] = longitude;
        wifiLocation[0][2] = mDevices.get(0).getAvaragedRssi();
        wifiLocation[0][3] = mDevices.get(0).getAccuracy();
        wifiLocation[1][0] = latitude;
        wifiLocation[1][1] = longitude;
        wifiLocation[1][2] = mDevices.get(1).getAvaragedRssi();
        wifiLocation[1][3] = mDevices.get(1).getAccuracy();
        wifiLocation[2][0] = latitude;
        wifiLocation[2][1] = longitude;
        wifiLocation[2][2] = mDevices.get(2).getAvaragedRssi();
        wifiLocation[2][3] = mDevices.get(2).getAccuracy();
        Log.d("CalibrationActivity", "333333333333333333333333333333333  longitude:" + wifiLocation[0][0]);
        Log.d("CalibrationActivity", "333333333333333333333333333333333  latitute:" + wifiLocation[0][1]);
        Log.d("CalibrationActivity", "333333333333333333333333333333333  avarage RSSI:" + wifiLocation[0][2]);
        Log.d("CalibrationActivity", "333333333333333333333333333333333  accuracy:" + wifiLocation[0][3]);

        myLocation = Trilateration.MyTrilateration(wifiLocation[0][0], wifiLocation[0][1], wifiLocation[0][2], wifiLocation[0][3], wifiLocation[1][0], wifiLocation[1][1], wifiLocation[1][2], wifiLocation[1][3], wifiLocation[2][0], wifiLocation[2][1], wifiLocation[2][2], wifiLocation[2][3]);

        Log.d("CalibrationActivity", "11111111113333333333333My Location :" + myLocation[0] + "  , " + myLocation[1]);
 //       Log.d("CalibrationActivity", "5555My Location :" + myLocation[0] + "  , " + myLocation[1]+"  Locations:"+ xLocations[0][0] + "  , " + xLocations[0][1]);
 //       Log.d("CalibrationActivity", "5555My Location :" + myLocation[0] + "  , " + myLocation[1]+"  Locations:"+ xLocations[1][0] + "  , " + xLocations[1][1]);
 //       Log.d("CalibrationActivity", "5555My Location :" + myLocation[0] + "  , " + myLocation[1] + "  Locations:" + xLocations[2][0] + "  , " + xLocations[2][1]);



//        if((xLocations[0][0]==myLocation[0]) && (xLocations[0][1]==myLocation[1]))
        if(StrLocation[0].compareTo(mDevices.get(getMinValue()).getBluetoothDevice().toString())==0)
        {
   //         Log.d("CalibrationActivity", "11My Location :" + myLocation[0] + "  , " + myLocation[1]+"  Locations:"+ xLocations[0][0] + "  , " + xLocations[0][1]);
            ch1.setChecked(false);
            ch2.setChecked(false);
            ch3.setChecked(true);
        }
        //else if((xLocations[1][0]==myLocation[0]) && (xLocations[1][1]==myLocation[1]))
        else if(StrLocation[1].compareTo(mDevices.get(getMinValue()).getBluetoothDevice().toString())==0)
        {
     //       Log.d("CalibrationActivity", "11My Location :" + myLocation[0] + "  , " + myLocation[1]+"  Locations:"+ xLocations[1][0] + "  , " + xLocations[1][1]);

            ch1.setChecked(false);
            ch2.setChecked(true);
            ch3.setChecked(false);
        }
        //else if((xLocations[2][0]==myLocation[0]) && (xLocations[2][1]==myLocation[1]))
        else if(StrLocation[2].compareTo(mDevices.get(getMinValue()).getBluetoothDevice().toString())==0)
        {
       //     Log.d("CalibrationActivity", "11My Location :" + myLocation[0] + "  , " + myLocation[1]+"  Locations:"+ xLocations[2][0] + "  , " + xLocations[2][1]);
            ch1.setChecked(true);
            ch2.setChecked(false);
            ch3.setChecked(false);
        }


        while(i<10000000)
        {
            i++;
        }
        Startbtn.setVisibility(view.GONE);
        btn.setVisibility(1);
 //      for (int n = 0; n < 3; n++)
 //           for (int m = 0; m < 2; m++)
  //              StrLocations[n][m] = String.valueOf(xLocations[n][m]);


  //      onDeviceClick();

 //       startDeviceActivity();

    }


    public void onFinish(View view) {
        startBeaconStatusActivity();

    }
/*
    private void startDeviceActivity() {
        mDeviceIntent = new Intent(this, DeviceActivity.class);
        mDeviceIntent.putExtra(DeviceActivity.EXTRA_DEVICE, mBluetoothDevice);
        startActivityForResult(mDeviceIntent, REQ_DEVICE_ACT);
    }

*/
    private void startBeaconStatusActivity() {
//        CalibrationActivity appContext = (CalibrationActivity) getApplicationContext();
        ///      appContext.mDeviceInfoList= mDeviceInfoList;
        Intent i =  new Intent(this, ConnectionActivity.class);
        i.putParcelableArrayListExtra("list", (ArrayList<? extends Parcelable>) mDevices);
        Bundle mBundle = new Bundle();
  //      mBundle.putSerializable("Array",  StrLocations);
        i.putExtras(mBundle);
  /*      Bundle b= new Bundle();
        b.putParcelableArrayList("list", (ArrayList<? extends Parcelable>) mDeviceInfoList);
        i.putExtras(b);
        i.setClass(CalibrationActivity.this, BeaconStatus.class);
  */      startActivity(i);
    }
    private void startDeviceActivity() {
        mDeviceIntent = new Intent(this, DeviceActivity.class);
        mDeviceIntent.putExtra(DeviceActivity.EXTRA_DEVICE, mBluetoothDevice);
        startActivityForResult(mDeviceIntent, REQ_DEVICE_ACT);
    }

    void onConnect() {

            int connState = mBluetoothManager.getConnectionState(mBluetoothDevice, BluetoothGatt.GATT);

            switch (connState) {
                case BluetoothGatt.STATE_CONNECTED:
                    mBluetoothLeService.disconnect(null);
                    break;
                case BluetoothGatt.STATE_DISCONNECTED:
                    boolean ok = mBluetoothLeService.connect(mBluetoothDevice.getAddress());
                    if (!ok) {
   //                     setError("Connect failed");
                    }
                    break;
                default:
     //               setError("Device busy (connecting/disconnecting)");
                    break;
            }

    }

    public void onDeviceClick() {

 //       setBusy(true);

        mBluetoothDevice = mDevices.get(getMinValue()).getBluetoothDevice();
        Log.d("CalibrationActivity", "TEST 99999999999  Bluetooth device:" + mBluetoothDevice);
        if (mConnIndex == NO_DEVICE) {
  //          mScanView.setStatus("Connecting");
            mConnIndex = getMinValue();
            onConnect();
        } else {
  //          mScanView.setStatus("Disconnecting");
            if (mConnIndex != NO_DEVICE) {
                mBluetoothLeService.disconnect(mBluetoothDevice.getAddress());
            }
        }
    }
    private void stopDeviceActivity() {
        finishActivity(REQ_DEVICE_ACT);
    }

    // getting the miniumum value
    public int getMinValue(){
        int index=0;
        double minValue = mDevices.get(0).getAccuracy();
        for(int i=0;i<mDevices.size();i++){
            if(mDevices.get(i).getAccuracy() < minValue){
                minValue = mDevices.get(i).getAccuracy();
                index=i;
            }
        }
        return index;
    }
    //*******************************************************************************************************************************
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

           //     updateGuiState();
            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                // GATT connect
                int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS,
                        BluetoothGatt.GATT_FAILURE);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                //    setBusy(false);
                    startDeviceActivity();
                    //                startBeaconStatusActivity();
                    //***************************************************************************************************************
                } else
                    Log.d("CalibrationActivity", "333333333333333333333333333333333  accuracy:" + wifiLocation[0][3]);
                   // setError("Connect failed. Status: " + status);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                // GATT disconnect
                int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS,
                        BluetoothGatt.GATT_FAILURE);
                stopDeviceActivity();
                if (status == BluetoothGatt.GATT_SUCCESS) {
                 //   setBusy(false);
               //     mScanView.setStatus(mBluetoothDevice.getName() + " disconnected", STATUS_DURATION);
                } else {
               //     setError("Disconnect Status: " + HCIDefines.hciErrorCodeStrings.get(status));
                }
                mConnIndex = NO_DEVICE;
                mBluetoothLeService.close();
            } else {
                // Log.w(TAG,"Unknown action: " + action);
            }

        }
    };
/*
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

*/
}


package com.example.ti.ble.sensortag;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import com.example.ti.ble.common.BleDeviceInfo;

import java.util.ArrayList;
import java.util.List;

public class BeaconStatus extends Activity {
    private List<BleDeviceInfo> mDevices;
    public static final String EXTRA_DEVICE_LIST = "EXTRA_DEVICE_LIST";
    BleDeviceInfo deviceInfo;
    BluetoothDevice device;
    private CheckBox ch1, ch2, ch3;
    CalibrationActivity Cactivity;

    private double[][] wifiLocation;
    //	private double myLatitude, myLongitude;
    private double[] myLocation = new double[2];
    public double [][]Locations;
    public String [][]StrLocations;
    public String []StrLocation;
    double distance[] = new double[3];
    double rssi[] = new double[3];
    location loc = new location();
    Trilateration tri;
    //   public MyLocationListener locationListener;
    private String lac;
    private String lng;

    double longitude, latitude;
    private String TPID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_status);
        ch1 = (CheckBox) findViewById(R.id.checkBox4);
        ch2 = (CheckBox) findViewById(R.id.checkBox5);
        ch3 = (CheckBox) findViewById(R.id.checkBox6);
        Intent i = getIntent();
        mDevices = (List) i.getParcelableArrayListExtra("list");
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
            Log.d("CalibrationActivity", "999999999999999999999999999999999  Bluetooth device1:" + mDevices.get(0).getBluetoothDevice());
            Log.d("CalibrationActivity", "999999999999999999999999999999999  Bluetooth device2:" + mDevices.get(1).getBluetoothDevice());
            Log.d("CalibrationActivity", "999999999999999999999999999999999  Bluetooth device3:" + mDevices.get(2).getBluetoothDevice());
            Log.d("CalibrationActivity", "999999999999999999999999999999999  TXpower:" + mDevices.get(0).gettxPower());
            //         ch1.setChecked(true);
            //         ch2.setChecked(true);
            //         ch3.setChecked(false);


        }
        //      locationListener = new MyLocationListener();
        wifiLocation = new double[3][4];
        Locations=new double[3][2];
        StrLocations=new String[3][2];
  //      StrLocation=new String[3];
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
        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
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
        Log.d("CalibrationActivity", "333333333333333333333333333333333  longitude:" + longitude);
        Log.d("CalibrationActivity", "333333333333333333333333333333333  latitute:" + latitude);
        Log.d("CalibrationActivity", "333333333333333333333333333333333  avarage RSSI:" + wifiLocation[0][2]);
        Log.d("CalibrationActivity", "333333333333333333333333333333333  accuracy:" + wifiLocation[0][3]);

        myLocation = Trilateration.MyTrilateration(wifiLocation[0][0], wifiLocation[0][1], wifiLocation[0][2], wifiLocation[0][3], wifiLocation[1][0], wifiLocation[1][1], wifiLocation[1][2], wifiLocation[1][3], wifiLocation[2][0], wifiLocation[2][1], wifiLocation[2][2], wifiLocation[2][3]);
        Locations[0][0]=myLocation[0];
        Locations[0][1]=myLocation[1];
        Log.d("CalibrationActivity", "9999999999 My Location :" + myLocation[0] + "   " + myLocation[1]);
        ch1.setChecked(false);
        ch2.setChecked(false);
        ch3.setChecked(true);
        while(i<10000000)
        {
            i++;
        }
        StrLocation=new String[3];
        StrLocation[0]=mDevices.get(getMinValue()).getBluetoothDevice().toString();
        StrLocation[1]="";
        StrLocation[2]="";
        Log.d("CalibrationActivity", "1111111111111999999999999999999  device111:" + StrLocation[0]);

 /*       for (int n = 0; n < 3; n++)
            for (int m = 0; m < 2; m++)
                StrLocations[n][m] = String.valueOf(Locations[n][m]);
*/
        startBeaconStatusActivity();

    }
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
    private void startBeaconStatusActivity() {
//        CalibrationActivity appContext = (CalibrationActivity) getApplicationContext();
        ///      appContext.mDeviceInfoList= mDeviceInfoList;
        Intent i =  new Intent(this, CalibrationActivity2.class);
        i.putParcelableArrayListExtra("list", (ArrayList<? extends Parcelable>) mDevices);
        Bundle mBundle = new Bundle();
 //       mBundle.putSerializable("Array",  StrLocations);
        mBundle.putSerializable("LocArray",  StrLocation);
        i.putExtras(mBundle);
  /*      Bundle b= new Bundle();
        b.putParcelableArrayList("list", (ArrayList<? extends Parcelable>) mDeviceInfoList);
        i.putExtras(b);
        i.setClass(CalibrationActivity.this, BeaconStatus.class);
  */      startActivity(i);
    }
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


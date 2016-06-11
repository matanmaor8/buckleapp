package com.example.ti.ble.sensortag;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.example.ti.ble.common.BleDeviceInfo;

import java.util.ArrayList;
import java.util.List;

public class GetLocationActivity extends Activity {
    private List<BleDeviceInfo> mDevices;
    public static final String EXTRA_DEVICE_LIST = "EXTRA_DEVICE_LIST";
    BleDeviceInfo deviceInfo;
    BluetoothDevice device;
    private CheckBox ch1, ch2, ch3;
    private Button btn;
    CalibrationActivity Cactivity;

    private double[][] wifiLocation;
    //	private double myLatitude, myLongitude;
    private double[] myLocation = new double[2];
    public double [][]LocationsLatLong;
    public String [][]StrLocations;
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
        setContentView(R.layout.activity_get_location);

        ch1 = (CheckBox) findViewById(R.id.checkBox4);
        ch2 = (CheckBox) findViewById(R.id.checkBox5);
        ch3 = (CheckBox) findViewById(R.id.checkBox6);
        btn = (Button) findViewById(R.id.button5);
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
            Log.d("CalibrationActivity", "999999999999999999999999999999999  Bluetooth device:" + mDevices.get(0).getBluetoothDevice());
            Log.d("CalibrationActivity", "999999999999999999999999999999999  TXpower:" + mDevices.get(0).gettxPower());
            //         ch1.setChecked(true);
            //         ch2.setChecked(true);
            //         ch3.setChecked(false);


        }
        //      locationListener = new MyLocationListener();
        wifiLocation = new double[3][4];
        LocationsLatLong=new double[3][2];
        StrLocations=new String[3][2];
    }

    public void onCheck1(View view) {
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

        LocationsLatLong[0][0]=latitude;
        LocationsLatLong[0][1]=longitude;
        Log.d("CalibrationActivity", "9999999999 My Location :" + myLocation[0] + "   " + myLocation[1]);
 //       ch1.setChecked(false);
 //       ch2.setChecked(false);
        ch3.setChecked(true);
        if(ch1.isChecked()&&ch2.isChecked()&&ch3.isChecked())
            btn.setVisibility(1);

    }

    public void onCheck2(View view) {
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

        LocationsLatLong[1][0]=latitude;
        LocationsLatLong[1][1]=longitude;
        Log.d("CalibrationActivity", "9999999999 My Location :" + myLocation[0] + "   " + myLocation[1]);
        //       ch1.setChecked(false);
        ch2.setChecked(true);
        //      ch3.setChecked(true);
        if(ch1.isChecked()&&ch2.isChecked()&&ch3.isChecked())
            btn.setVisibility(1);

    }

    public void onCheck3(View view) {
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

        LocationsLatLong[2][0]=latitude;
        LocationsLatLong[2][1]=longitude;
        Log.d("CalibrationActivity", "9999999999 My Location :" + myLocation[0] + "   " + myLocation[1]);
        ch1.setChecked(true);
  //      ch2.setChecked(false);
  //      ch3.setChecked(true);

        if(ch1.isChecked()&&ch2.isChecked()&&ch3.isChecked())
            btn.setVisibility(1);


    }

    public void onConfirm(View view) {
        int i=0;


        for (int n = 0; n < 3; n++)
            for (int m = 0; m < 2; m++)
                StrLocations[n][m] = String.valueOf(LocationsLatLong[n][m]);
        startBeaconStatusActivity();

    }

    private void startBeaconStatusActivity() {
//        CalibrationActivity appContext = (CalibrationActivity) getApplicationContext();
        ///      appContext.mDeviceInfoList= mDeviceInfoList;
        Intent i =  new Intent(this, BeaconStatus.class);
        i.putParcelableArrayListExtra("list", (ArrayList<? extends Parcelable>) mDevices);
        Bundle mBundle = new Bundle();
        mBundle.putSerializable("Array",  StrLocations);
        i.putExtras(mBundle);
  /*      Bundle b= new Bundle();
        b.putParcelableArrayList("list", (ArrayList<? extends Parcelable>) mDeviceInfoList);
        i.putExtras(b);
        i.setClass(CalibrationActivity.this, BeaconStatus.class);
  */      startActivity(i);
    }

}

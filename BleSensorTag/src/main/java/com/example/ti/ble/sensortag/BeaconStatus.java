package com.example.ti.ble.sensortag;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;

import com.example.ti.ble.common.BleDeviceInfo;

import java.util.List;

public class BeaconStatus extends Activity {
    private List<BleDeviceInfo> mDevices;
    public static final String EXTRA_DEVICE_LIST = "EXTRA_DEVICE_LIST";
    BleDeviceInfo deviceInfo;
    BluetoothDevice device;
    int rssi;
    private CheckBox ch1,ch2,ch3;
    CalibrationActivity Cactivity;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_status);
        ch1= (CheckBox)findViewById(R.id.checkBox);
        ch2= (CheckBox)findViewById(R.id.checkBox2);
        ch3= (CheckBox)findViewById(R.id.checkBox3);
       Intent i = getIntent();
        mDevices=(List)i.getParcelableArrayListExtra("list");
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
        if(mDevices.get(0).getAvaragedRssi()>= -90.0) {
            Log.d("CalibrationActivity", "999999999999999999999999999999999  avarage RSSI:" +mDevices.get(0).getAvaragedRssi());
            Log.d("CalibrationActivity", "999999999999999999999999999999999  major:" +mDevices.get(0).getmajor());
            Log.d("CalibrationActivity", "999999999999999999999999999999999  minor:" +mDevices.get(0).getminor());
            Log.d("CalibrationActivity", "999999999999999999999999999999999  uuid:" +mDevices.get(0).getUUID1());
            Log.d("CalibrationActivity", "999999999999999999999999999999999  accuracy:" +mDevices.get(0).getAccuracy());
            Log.d("CalibrationActivity", "999999999999999999999999999999999  RSSI:" +mDevices.get(0).getRssi());
            Log.d("CalibrationActivity", "999999999999999999999999999999999  Bluetooth device:" +mDevices.get(0).getBluetoothDevice());
            Log.d("CalibrationActivity", "999999999999999999999999999999999  TXpower:" +mDevices.get(0).gettxPower());
            ch1.setChecked(true);
            ch2.setChecked(true);
            ch3.setChecked(false);


        }


    }


}

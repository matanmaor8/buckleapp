package com.example.ti.ble.sensortag;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.widget.CheckBox;

import com.example.ti.ble.common.BleDeviceInfo;

import java.util.List;

public class BeaconStatus extends Activity {
    private List<BleDeviceInfo> mDevices;
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
  //      device= Cactivity.mDeviceInfoList.get(0).getBluetoothDevice();
    //    device = deviceInfo.getBluetoothDevice();
 //       rssi = (int) deviceInfo.getAvaragedRssi();
        CalibrationActivity Cactivity=new CalibrationActivity();
        mDevices=Cactivity.mDeviceInfoList;
        if(mDevices.get(0).getAvaragedRssi()> -60) {

            ch1.setChecked(true);
            ch2.setChecked(true);
            ch3.setChecked(false);


        }


    }

}

package com.example.ti.ble.sensortag;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.widget.CheckBox;

import com.example.ti.ble.common.BleDeviceInfo;

import java.util.List;

public class BeaconStatus extends Activity {
    private List<BleDeviceInfo> mDevices;
    BleDeviceInfo deviceInfo = mDevices.get(2);
    BluetoothDevice device = deviceInfo.getBluetoothDevice();
    private CheckBox ch1,ch2,ch3;
    int rssi = (int) deviceInfo.getAvaragedRssi();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_status);
        ch1= (CheckBox)findViewById(R.id.checkBox);
        ch2= (CheckBox)findViewById(R.id.checkBox2);
        ch3= (CheckBox)findViewById(R.id.checkBox3);
        if(deviceInfo.getRssi()> -60) {

            ch1.setChecked(true);
            ch2.setChecked(true);
            ch3.setChecked(false);


        }


    }

}

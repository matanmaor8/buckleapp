package com.example.ti.ble.sensortag;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ti.ble.common.BleDeviceInfo;
import com.example.ti.util.CustomTimer;
import com.example.ti.util.CustomTimerCallback;

import java.util.List;

/**
 * Created by צח on 28/04/2016.
 */
public class ConnectionScanView extends Fragment {
    // private static final String TAG = "ScanView";
    private final int SCAN_TIMEOUT = 20; // Seconds
    private final int CONNECT_TIMEOUT = 20; // Seconds
    private ConnectionActivity mActivity = null;

    private DeviceListAdapter mDeviceAdapter = null;
    private TextView mEmptyMsg;
    private TextView mStatus;
    private Button mBtnScan = null;
    private ListView mDeviceListView = null;
    private boolean mBusy;

    private CustomTimer mScanTimer = null;
    private CustomTimer mConnectTimer = null;
    @SuppressWarnings("unused")
    private CustomTimer mStatusTimer;
    private Context mContext;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Log.i(TAG, "onCreateView");

        // The last two arguments ensure LayoutParams are inflated properly.
        View view = inflater.inflate(R.layout.activity_calibration, container, false);

        mActivity = (ConnectionActivity) getActivity();
        mContext = mActivity.getApplicationContext();

        // Initialize widgets
        mStatus = (TextView) view.findViewById(R.id.status);
        mBtnScan = (Button) view.findViewById(R.id.btn_scan);
        mDeviceListView = (ListView) view.findViewById(R.id.device_list);
        mDeviceListView.setClickable(true);
        mDeviceListView.setOnItemClickListener(mDeviceClickListener);
        mEmptyMsg = (TextView)view.findViewById(R.id.no_device);
        mBusy = false;

        // Alert parent activity
        mActivity.onScanViewReady(view);

        return view;
    }

    @Override
    public void onDestroy() {
        // Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    void setStatus(String txt) {
        mStatus.setText(txt);
        mStatus.setTextAppearance(mContext, R.style.statusStyle_Success);
    }

    void setStatus(String txt, int duration) {
        setStatus(txt);
        mStatusTimer = new CustomTimer(null, duration, mClearStatusCallback);
    }

    void setError(String txt) {
        setBusy(false);
        stopTimers();
        mStatus.setText(txt);
        mStatus.setTextAppearance(mContext, R.style.statusStyle_Failure);
    }

    void notifyDataSetChanged() {
        List<BleDeviceInfo> deviceList = mActivity.getDeviceInfoList();
        if (mDeviceAdapter == null) {
            mDeviceAdapter = new DeviceListAdapter(mActivity,deviceList);
        }
        mDeviceListView.setAdapter(mDeviceAdapter);
        Log.d("CalibrationScanView", "111111111111111111111111111111111111111111111");
        mDeviceAdapter.notifyDataSetChanged();
        if (deviceList.size() > 0) {
            mEmptyMsg.setVisibility(View.GONE);
        } else {
            mEmptyMsg.setVisibility(View.VISIBLE);
        }

    }

    void setBusy(boolean f) {
        if (f != mBusy) {
            mBusy = f;
            if (!mBusy) {
                stopTimers();
                mBtnScan.setEnabled(true);	// Enable in case of connection timeout
                mDeviceAdapter.notifyDataSetChanged(); // Force enabling of all Connect buttons
            }
            mActivity.showBusyIndicator(f);
        }
    }

    void updateGui(boolean scanning) {
        if (mBtnScan == null)
            return; // UI not ready

        setBusy(scanning);

        if (scanning) {
            // Indicate that scanning has started
            mScanTimer = new CustomTimer(null, SCAN_TIMEOUT, mPgScanCallback);
            mBtnScan.setText("Stop");
            mBtnScan.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_cancel, 0);
            mStatus.setTextAppearance(mContext, R.style.statusStyle_Busy);
            Log.d("CalibrationScanView", "44444444444444444444444444444444444444444444");
            mStatus.setText("Scanning...");
            mEmptyMsg.setText(R.string.nodevice);
            mActivity.updateGuiState();
            Log.d("CalibrationScanView", "333333333333333333333333333333333333333");
        } else {
            // Indicate that scanning has stopped
            mStatus.setTextAppearance(mContext, R.style.statusStyle_Success);
            mBtnScan.setText("Scan");
            mBtnScan.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_refresh, 0);
            mEmptyMsg.setText(R.string.scan_advice);
            mActivity.setProgressBarIndeterminateVisibility(false);
            mDeviceAdapter.notifyDataSetChanged();

        }
    }
    //*******************************************************************************************************************************
    // Listener for device list
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
            // Log.d(TAG,"item click");
            mConnectTimer = new CustomTimer(null, CONNECT_TIMEOUT, mPgConnectCallback);
            mBtnScan.setEnabled(false);
            mDeviceAdapter.notifyDataSetChanged(); // Force disabling of all Connect buttons
            mActivity.onDeviceClick(pos);
        }
    };
    //*******************************************************************************************************************************
    // Listener for progress timer expiration
    private CustomTimerCallback mPgScanCallback = new CustomTimerCallback() {
        public void onTimeout() {
            mActivity.onScanTimeout();
        }

        public void onTick(int i) {
            mActivity.refreshBusyIndicator();
        }
    };

    // Listener for connect/disconnect expiration
    private CustomTimerCallback mPgConnectCallback = new CustomTimerCallback() {
        public void onTimeout() {
            mActivity.onConnectTimeout();
            mBtnScan.setEnabled(true);
        }

        public void onTick(int i) {
            mActivity.refreshBusyIndicator();
        }
    };

    // Listener for connect/disconnect expiration
    private CustomTimerCallback mClearStatusCallback = new CustomTimerCallback() {
        public void onTimeout() {
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    setStatus("");
                }
            });
            mStatusTimer = null;
        }

        public void onTick(int i) {
        }
    };

    private void stopTimers() {
        if (mScanTimer != null) {
            mScanTimer.stop();
            mScanTimer = null;
        }
        if (mConnectTimer != null) {
            mConnectTimer.stop();
            mConnectTimer = null;
        }
    }

    //
    // CLASS DeviceAdapter: handle device list
    //
    @SuppressLint("InflateParams") class DeviceListAdapter extends BaseAdapter {
        private List<BleDeviceInfo> mDevices;
        private LayoutInflater mInflater;
        private int counter=0;
        List <BluetoothGattService> serviceList;
        public DeviceListAdapter(Context context, List<BleDeviceInfo> devices) {
            mInflater = LayoutInflater.from(context);
            mDevices = devices;
        }

        public int getCount() {
            return mDevices.size();
        }

        public Object getItem(int position) {
            return mDevices.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
                Log.d("CalibrationScanView","77777777777777777777777777777777777");
            } else {
                vg = (ViewGroup) mInflater.inflate(R.layout.element_device, null);
                Log.d("CalibrationScanView","6666666666666666666666666666666666");
            }
            //     serviceList = BluetoothLeService.getInstance().getSupportedGattServices();
            //     BluetoothGattService s = serviceList.get(counter);

//      counter++;
            BleDeviceInfo deviceInfo = mDevices.get(position);
            BluetoothDevice device = deviceInfo.getBluetoothDevice();
            int rssi = (int) deviceInfo.getRssi();
            int major= deviceInfo.getmajor();
            int minor=deviceInfo.getminor();
            String UUID1= deviceInfo.getUUID1();
            String name;
            name = device.getName();
            if (name == null) {
                name = new String("Unknown device");
            }
            //    BluetoothGattService s;
            String s=String.valueOf(device.getUuids());

            String descr = name + "\n" +"\nRssi: " + rssi + " dBm" + "\nmajor:"+major+ "\nminor:"+minor+ "\n\nUUID:"+UUID1;
            ((TextView) vg.findViewById(R.id.descr)).setText(descr);
            Log.d("ScanView", "uuid:" + s.toString());
            ImageView iv = (ImageView)vg.findViewById(R.id.devImage);
            if (name.equals("SensorTag2") || name.equals("CC2650 SensorTag"))
                iv.setImageResource(R.drawable.sensortag2_300);
            else {
                iv.setImageResource(R.drawable.sensortag_300);
            }
            // Disable connect button when connecting or connected
            Button bv = (Button)vg.findViewById(R.id.btnConnect);
            bv.setEnabled(mConnectTimer == null);

            return vg;
        }
    }

}


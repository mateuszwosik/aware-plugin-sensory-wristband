package com.aware.plugin.sensory_wristband.activities;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.aware.Bluetooth;
import com.aware.plugin.sensory_wristband.R;
import com.aware.plugin.sensory_wristband.utils.Device;
import com.aware.plugin.sensory_wristband.utils.DeviceArrayAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class ScanActivity extends AppCompatActivity {

    private static String TAG;

    public static final String ACTION_AWARE_SENSORY_WRISTBAND_SCAN_COMPLETE = "ACTION_AWARE_SENSORY_WRISTBAND_SCAN_COMPLETE";
    public static final String EXTRA_DEVICE = "bluetooth_device";

    private static final int IDLE = 0;
    private static final int SCANNING = 1;

    //State of scanning
    private static int scanState = IDLE;

    //Map with devices on list view
    private HashMap<String, Device> devices = new HashMap<>();

    //ArrayAdapter used in displaying devices in list view
    private DeviceArrayAdapter deviceArrayAdapter;

    public BluetoothBroadcastReceiver bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver();
    private class BluetoothBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case Bluetooth.ACTION_AWARE_BLUETOOTH_NEW_DEVICE:
                    Bundle bundle = intent.getExtras();
                    ContentValues contentValues = bundle.getParcelable(Bluetooth.EXTRA_DEVICE);
                    if (contentValues != null) {
                        Device device = new Device(contentValues.getAsString("bt_name"), contentValues.getAsString("bt_address"));
                        Log.d(TAG, "New device: " + device.toString());
                        if (!devices.containsKey(device.getAddress())) {
                            devices.put(device.getAddress(), device);
                            deviceArrayAdapter.add(device);
                        }
                    }
                    break;
                case Bluetooth.ACTION_AWARE_BLUETOOTH_SCAN_STARTED:
                    Log.d(TAG, "Bluetooth scan started");
                    scanState = SCANNING;
                    deviceArrayAdapter.clear();
                    devices.clear();
                    break;
                case Bluetooth.ACTION_AWARE_BLUETOOTH_SCAN_ENDED:
                    Log.d(TAG, "Bluetooth scan ended");
                    scanState = IDLE;
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);

        TAG = getResources().getString(R.string.app_name) + "::ScanActivity: ";

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Bluetooth.ACTION_AWARE_BLUETOOTH_NEW_DEVICE);
        intentFilter.addAction(Bluetooth.ACTION_AWARE_BLUETOOTH_SCAN_ENDED);
        intentFilter.addAction(Bluetooth.ACTION_AWARE_BLUETOOTH_SCAN_STARTED);
        registerReceiver(bluetoothBroadcastReceiver, intentFilter);

        //Create new array adapter fot list view
        deviceArrayAdapter = new DeviceArrayAdapter(this,R.layout.device_list_item,new ArrayList<Device>());

        //Get List View
        ListView devicesListView = (ListView) findViewById(R.id.devicesListView);

        //Set adapter to list view
        devicesListView.setAdapter(deviceArrayAdapter);

        //Click on list view item
        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Device device = deviceArrayAdapter.getItem(position);
                Intent intent = new Intent(ACTION_AWARE_SENSORY_WRISTBAND_SCAN_COMPLETE);
                intent.putExtra(EXTRA_DEVICE,device);
                sendBroadcast(intent);
                finish();
            }
        });

        //Automatically start scanning for devices
        sendBroadcast(new Intent(Bluetooth.ACTION_AWARE_BLUETOOTH_REQUEST_SCAN));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothBroadcastReceiver);
    }

    /**
     * Start scanning for devices nearby.
     */
    public void startScan(View view){
        switch(scanState){
            case IDLE:
                sendBroadcast(new Intent(Bluetooth.ACTION_AWARE_BLUETOOTH_REQUEST_SCAN));
                break;
            case SCANNING:
                break;
        }
    }

}

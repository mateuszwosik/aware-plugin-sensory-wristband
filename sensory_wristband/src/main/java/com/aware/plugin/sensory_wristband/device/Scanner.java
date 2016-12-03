package com.aware.plugin.sensory_wristband.device;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.util.Log;

public class Scanner {

    /**
     * Start scanning for Bluetooth devices
     * @param callback - Callback with implemented result events
     */
    public static void startScan(ScanCallback callback) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (null == adapter) {
            Log.e("Scanner", "BluetoothAdapter is null");
            return;
        }
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (null == scanner) {
            Log.e("Scanner", "BluetoothLeScanner is null");
            return;
        }
        scanner.startScan(callback);
    }

    /**
     * Stop scanning for Bluetooth devices
     * @param callback - Callback with implemented result events
     */
    public static void stopScan(ScanCallback callback) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (null == adapter) {
            Log.e("Scanner", "BluetoothAdapter is null");
            return;
        }
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (null == scanner) {
            Log.e("Scanner", "BluetoothLeScanner is null");
            return;
        }
        scanner.stopScan(callback);
    }
}

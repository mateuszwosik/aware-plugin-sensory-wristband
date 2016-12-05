package com.aware.plugin.sensory_wristband;

import android.Manifest;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.sensory_wristband.activities.ScanActivity;
import com.aware.plugin.sensory_wristband.device.ActionCallback;
import com.aware.plugin.sensory_wristband.device.Band;
import com.aware.plugin.sensory_wristband.device.DeviceSelector;
import com.aware.plugin.sensory_wristband.device.HeartRateNotifyListener;
import com.aware.plugin.sensory_wristband.device.MiBand.model.BatteryInfo;
import com.aware.plugin.sensory_wristband.device.MiBand2.model.Protocol;
import com.aware.plugin.sensory_wristband.device.NotifyListener;
import com.aware.plugin.sensory_wristband.device.RealtimeStepsNotifyListener;
import com.aware.plugin.sensory_wristband.utils.Device;
import com.aware.ui.PermissionsHandler;
import com.aware.utils.Aware_Plugin;

public class Plugin extends Aware_Plugin {

    public static final String ACTION_AWARE_BAND_DISCONNECTED = "ACTION_AWARE_BAND_DISCONNECTED";
    public static final String ACTION_AWARE_BAND_ENABLE_NOTIFICATION = "ACTION_AWARE_BAND_ENABLE_NOTIFICATION";
    public static final String ACTION_AWARE_BAND_DISABLE_NOTIFICATION = "ACTION_AWARE_BAND_DISABLE_NOTIFICATION";
    public static final String ACTION_AWARE_BAND_RSSI = "ACTION_AWARE_BAND_RSSI";
    public static final String ACTION_AWARE_BAND_BATTERY = "ACTION_AWARE_BAND_BATTERY";
    public static final String ACTION_AWARE_BAND_HEART_RATE = "ACTION_AWARE_BAND_HEART_RATE";
    public static final String ACTION_AWARE_BAND_STEPS = "ACTION_AWARE_BAND_STEPS";
    public static final String ACTION_AWARE_BAND_NAME = "ACTION_AWARE_BAND_NAME";
    public static final String EXTRA_DATA = "extra_data";

    /**
     * Set 1 - need to confirm each match/pairing. Just like in the official application.
     * Set 0 - no need for confirmation when User ID is the same as previous.
     *         In case device does not respond will be sent to normal notification listener with value 3
     */
    private static final int TYPE = 0;

    private static int UPDATE_BASIC_INFO_PERIOD = 10000;//10s
    private static int SENSORS_ACTIVATION_DELAY = 5000;//3s

    private static final int HEART_RATE_MAX_VALUE = 221;
    private static final int HEART_RATE_MIN_VALUE = 39;
    private static final int HEART_RATE_NO_GET_VALUE = 0;

    private BluetoothAdapter bluetoothAdapter;
    private static Band band;
    private static Handler basicInfoHandler;
    private static Runnable basicInfoPeriodicUpdater;
    private static Handler sensorsActivationHandler;
    private static Runnable sensorsActivationRunnable;

    private static ContextProducer contextProducer;

    /**
     * BroadcastReceiver responsible for connecting to selected device.
     */
    public DeviceBroadcastReceiver deviceBroadcastReceiver = new DeviceBroadcastReceiver();
    private class DeviceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ScanActivity.ACTION_AWARE_SENSORY_WRISTBAND_SCAN_COMPLETE.equals(intent.getAction())){
                Device device = (Device) intent.getSerializableExtra(ScanActivity.EXTRA_DEVICE);
                BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());
                band = DeviceSelector.getInstance().getSupportedDevice(bluetoothDevice);
                if (band == null){
                    Log.d(TAG, "Not supported device");
                    Toast.makeText(getApplicationContext(),"Not supported device",Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Supported device " + band.getClass().getSimpleName());
                    connectDevice(bluetoothDevice);
                }
            }
        }
    }

    /**
     * BroadcastReceiver responsible for enable and disable notifications of supported device services.
     */
    public EnableNotificationBroadcastReceiver enableNotificationReceiver = new EnableNotificationBroadcastReceiver();
    private class EnableNotificationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(ACTION_AWARE_BAND_ENABLE_NOTIFICATION.equalsIgnoreCase(action)){
                /*Enable notifications*/
                enableHeartRateNotification();
                enableStepNotification();
            } else if(ACTION_AWARE_BAND_DISABLE_NOTIFICATION.equalsIgnoreCase(action)) {
                /*Disable notifications*/
                disableHeartRateNotification();
                disableStepNotification();
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = getResources().getString(R.string.app_name);

        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        band = null;
        DeviceSelector.getInstance().setSupportedDevices(this);

        IntentFilter deviceIntentFilter = new IntentFilter();
        deviceIntentFilter.addAction(ScanActivity.ACTION_AWARE_SENSORY_WRISTBAND_SCAN_COMPLETE);
        registerReceiver(deviceBroadcastReceiver, deviceIntentFilter);

        IntentFilter notificationIntentFilter = new IntentFilter();
        notificationIntentFilter.addAction(ACTION_AWARE_BAND_ENABLE_NOTIFICATION);
        notificationIntentFilter.addAction(ACTION_AWARE_BAND_DISABLE_NOTIFICATION);
        registerReceiver(enableNotificationReceiver, notificationIntentFilter);

        //Get signal strength and battery info cyclically
        basicInfoHandler = new Handler();
        basicInfoPeriodicUpdater = new Runnable() {
            @Override
            public void run() {
                refreshRSSI();
                refreshBatteryInfo();
                basicInfoHandler.postDelayed(basicInfoPeriodicUpdater,UPDATE_BASIC_INFO_PERIOD);
            }
        };

        sensorsActivationHandler = new Handler();
        sensorsActivationRunnable = new Runnable() {
            @Override
            public void run() {
                //Start heart rate continuous measurement
                startHeartRateMeasurement(Protocol.HEART_RATE_CONTINUOUS_MODE);
                //Start step notifications
                startStepNotification();
            }
        };

        //Any active plugin/sensor shares its overall context using broadcasts
        contextProducer = new ContextProducer() {
            @Override
            public void onContext() {

            }
        };
        CONTEXT_PRODUCER = contextProducer;

        //Add permissions you need (Support for Android M). By default, AWARE asks access to the #Manifest.permission.WRITE_EXTERNAL_STORAGE
        REQUIRED_PERMISSIONS.add(Manifest.permission.BLUETOOTH);
        REQUIRED_PERMISSIONS.add(Manifest.permission.BLUETOOTH_ADMIN);
        REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_FINE_LOCATION);
        REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        //To sync data to the server, you'll need to set this variables from your ContentProvider
        DATABASE_TABLES = Provider.DATABASE_TABLES;
        TABLES_FIELDS = Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ Provider.TableOne_Data.CONTENT_URI }; //this syncs dummy TableOne_Data to server

        //Activate plugin -- do this ALWAYS as the last thing (this will restart your own plugin and apply the settings)
        Aware.startPlugin(this, "com.aware.plugin.sensory_wristband");
    }

    //This function gets called every 5 minutes by AWARE to make sure this plugin is still running.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean permissions_ok = true;
        for (String p : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                permissions_ok = false;
                break;
            }
        }

        if (permissions_ok) {
            //Check if the user has toggled the debug messages
            DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

            //Initialize our plugin's settings
            Aware.setSetting(this, Settings.STATUS_PLUGIN_SENSORY_WRISTBAND, true);

            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
                //Ask AWARE for Bluetooth
                Aware.setSetting(this, Aware_Preferences.STATUS_BLUETOOTH, true);
                Aware.setSetting(this, Aware_Preferences.FREQUENCY_BLUETOOTH, 60);
            } else {
                if (band == null) {
                    startScanActivity();
                }
            }

        } else {
            Intent permissions = new Intent(this, PermissionsHandler.class);
            permissions.putExtra(PermissionsHandler.EXTRA_REQUIRED_PERMISSIONS, REQUIRED_PERMISSIONS);
            permissions.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(permissions);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Unregister receivers
        unregisterReceiver(deviceBroadcastReceiver);
        unregisterReceiver(enableNotificationReceiver);

        //Deactivate plugin
        Aware.setSetting(this, Settings.STATUS_PLUGIN_SENSORY_WRISTBAND, false);

        //Stop AWARE
        Aware.stopAWARE();
    }

    private void startScanActivity(){
        Intent scanIntent = new Intent(this, ScanActivity.class);
        scanIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(scanIntent);

    }

    /**
     * Establish connection with device
     * When connection establish create disconnect listener
     * @param device - Supported sensory wristband device
     */
    private void connectDevice(BluetoothDevice device){
        band.connect(device, new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                Log.d(TAG,"Connected to band");
                //Set on disconnect listener
                band.setDisconnectedListener(new NotifyListener() {
                    @Override
                    public void onNotify(byte[] data) {
                        Log.d(TAG,"Device disconnected");
                        basicInfoHandler.removeCallbacks(basicInfoPeriodicUpdater);
                        stopStepNotification();
                        stopHeartRateMeasurement(Protocol.HEART_RATE_CONTINUOUS_MODE);
                        band.disconnect();
                        band = null;
                        contextProducer = new ContextProducer() {
                            @Override
                            public void onContext() {
                                Intent intent = new Intent(ACTION_AWARE_BAND_DISCONNECTED);
                                sendBroadcast(intent);
                            }
                        };
                        CONTEXT_PRODUCER = contextProducer;
                        contextProducer.onContext();
                    }
                });
                //Show band services and characteristics
                band.showServicesAndCharacteristics();
                //Pair/Authenticate band
                pairDevice();
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.d(TAG,"Failed on connect. Error :" + errorCode + " - " + msg);
            }
        });
    }

    /**
     * Pair/Authenticate device.
     */
    private void pairDevice(){
        band.pair(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                Log.d(TAG,"Successful pairing");
                //After successful pairing enable notification os supported band services
                contextProducer = new ContextProducer() {
                    @Override
                    public void onContext() {
                        Intent intent = new Intent(ACTION_AWARE_BAND_ENABLE_NOTIFICATION);
                        sendBroadcast(intent);
                        intent = new Intent(ACTION_AWARE_BAND_NAME);
                        intent.putExtra(EXTRA_DATA,band.getDevice().getName());
                        sendBroadcast(intent);
                    }
                };
                CONTEXT_PRODUCER = contextProducer;
                contextProducer.onContext();
                //Set user info to band
                int uuid = 20202;
                int sex = 1;
                int age = 30;
                int height = 180;
                int weight = 80;
                String alias = "test";
                band.setUserInfo(uuid,sex,age,height,weight,alias,TYPE);
                //Start periodic updaters
                basicInfoHandler.post(basicInfoPeriodicUpdater);
                //Start after delay sensors notifications
                sensorsActivationHandler.postDelayed(sensorsActivationRunnable,SENSORS_ACTIVATION_DELAY);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.d(TAG,"Failed when pairing. Error: " + errorCode + " :: "+ msg);
            }
        });
    }

    /**
     * Refresh Received Signal Strength Indicator (RSSI).
     */
    private void refreshRSSI(){
        band.getSignalStrength(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                final int rssi = (int) data;
                Log.d(TAG,"Rssi: " + rssi);
                contextProducer = new ContextProducer() {
                    @Override
                    public void onContext() {
                        Intent intent = new Intent(ACTION_AWARE_BAND_RSSI);
                        intent.putExtra(EXTRA_DATA,rssi);
                        sendBroadcast(intent);
                    }
                };
                CONTEXT_PRODUCER = contextProducer;
                contextProducer.onContext();
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.d(TAG,"Failed refreshing RSSI. Error :" + errorCode + " :: " + msg);
            }
        });
    }

    /**
     * Refresh Battery Info of the device
     */
    private void refreshBatteryInfo(){
        band.getBatteryInfo(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                final BatteryInfo batteryInfo = (BatteryInfo) data;
                Log.d(TAG,"Battery info: " + batteryInfo.toString());
                contextProducer = new ContextProducer() {
                    @Override
                    public void onContext() {
                        Intent intent = new Intent(ACTION_AWARE_BAND_BATTERY);
                        intent.putExtra(EXTRA_DATA, batteryInfo);
                        sendBroadcast(intent);
                    }
                };
                CONTEXT_PRODUCER = contextProducer;
                contextProducer.onContext();
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.d(TAG,"Failed refreshing Battery Info. Error :" + errorCode + " :: " + msg);
            }
        });
    }

    /**
     * Enable real-time notification of the number of steps.
     */
    private void enableStepNotification(){
        band.setRealtimeStepsNotifyListener(new RealtimeStepsNotifyListener() {
            @Override
            public void onNotify(final int steps) {
                Log.d(TAG,"Number of steps: " + steps);
                contextProducer = new ContextProducer() {
                    @Override
                    public void onContext() {
                        Intent intent = new Intent(ACTION_AWARE_BAND_STEPS);
                        intent.putExtra(EXTRA_DATA, steps);
                        sendBroadcast(intent);
                    }
                };
                CONTEXT_PRODUCER = contextProducer;
                contextProducer.onContext();
            }
        });
    }

    /**
     * Disable step notification.
     */
    private void disableStepNotification(){
        band.removeRealtimeStepsNotifyListener();
    }

    /**
     * Start step notification.
     */
    private void startStepNotification(){
        band.enableRealtimeStepsNotify();
    }

    /**
     * Stops/Pause real-time notification of the number of steps.
     */
    private void stopStepNotification(){
        band.disableRealtimeStepsNotify();
    }

    /**
     * Enable heart rate notification.
     */
    private void enableHeartRateNotification(){
        //Set the heart rate scan notification
        band.setHeartRateScanListener(new HeartRateNotifyListener() {
            @Override
            public void onNotify(int heartRate) {
                if (isHeartRateMeasured(heartRate)) {
                    if (isHeartRateValid(heartRate)) {
                        Log.d(TAG, "Heart rate: " + heartRate + " bmp");
                    } else {
                        Log.d(TAG, "Heart rate is out of range");
                        heartRate = 0;
                    }
                } else {
                    Log.d(TAG, "Heart rate not measured");
                    heartRate = 0;
                }
                final int value = heartRate;
                contextProducer = new ContextProducer() {
                    @Override
                    public void onContext() {
                        Intent intent = new Intent(ACTION_AWARE_BAND_HEART_RATE);
                        intent.putExtra(EXTRA_DATA, value);
                        sendBroadcast(intent);
                    }
                };
                CONTEXT_PRODUCER = contextProducer;
                contextProducer.onContext();
            }
        });
    }

    /**
     * Disable heart rate notifications.
     */
    private void disableHeartRateNotification(){
        band.removeHeartRateScanListener();
    }

    /**
     * Start heart rate measurement/scan.
     * If heart rate mode is not supported it is ignored.
     * @param heartRateMode - mode of heart rate measurement
     */
    private void startHeartRateMeasurement(byte heartRateMode){
        band.startHeartRateScan(heartRateMode);
    }

    /**
     * Stop heart rate measurement/scan.
     * If heart rate mode is not supported it is ignored.
     * @param heartRateMode - mode of heart rate measurement
     */
    private void stopHeartRateMeasurement(byte heartRateMode){
        band.stopHeartRateScan(heartRateMode);
    }

    /**
     * Check if heart rate value is correct.
     * @param heartRate - measured heart rate
     * @return true - heart rate value valid | false - heart rate value NOT valid
     */
    private boolean isHeartRateValid(int heartRate){
        return heartRate < HEART_RATE_MAX_VALUE && heartRate > HEART_RATE_MIN_VALUE;
    }

    /**
     * Check if heart rate is measured.
     * If heart rate sensor is correctly placed.
     * @param heartRate - measured heart rate
     * @return true - heart rate measured | false - heart rate not measured
     */
    private boolean isHeartRateMeasured(int heartRate){
        return heartRate != HEART_RATE_NO_GET_VALUE;
    }

}

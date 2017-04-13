package com.aware.plugin.sensory_wristband;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.sensory_wristband.activities.ScanActivity;
import com.aware.plugin.sensory_wristband.device.ActionCallback;
import com.aware.plugin.sensory_wristband.device.Band;
import com.aware.plugin.sensory_wristband.device.DeviceSelector;
import com.aware.plugin.sensory_wristband.device.MiBand2.model.Protocol;
import com.aware.plugin.sensory_wristband.device.MiBand2.model.StepsInfo;
import com.aware.plugin.sensory_wristband.utils.Device;
import com.aware.utils.Aware_Plugin;

public class Plugin extends Aware_Plugin {

    public static final String ACTION_AWARE_BAND_CONNECTED = "ACTION_AWARE_BAND_CONNECTED";
    public static final String ACTION_AWARE_BAND_DISCONNECTED = "ACTION_AWARE_BAND_DISCONNECTED";
    public static final String ACTION_AWARE_BAND_ENABLE_NOTIFICATION = "ACTION_AWARE_BAND_ENABLE_NOTIFICATION";
    public static final String ACTION_AWARE_BAND_DISABLE_NOTIFICATION = "ACTION_AWARE_BAND_DISABLE_NOTIFICATION";
    public static final String ACTION_AWARE_BAND_RSSI = "ACTION_AWARE_BAND_RSSI";
    public static final String ACTION_AWARE_BAND_BATTERY = "ACTION_AWARE_BAND_BATTERY";
    public static final String ACTION_AWARE_BAND_HEART_RATE = "ACTION_AWARE_BAND_HEART_RATE";
    public static final String ACTION_AWARE_BAND_STEPS = "ACTION_AWARE_BAND_STEPS";
    public static final String EXTRA_DATA = "extra_data";

    /**
     * Set 1 - need to confirm each match/pairing. Just like in the official application.
     * Set 0 - no need for confirmation when User ID is the same as previous.
     *         In case device does not respond will be sent to normal notification listener with value 3
     * private static final int TYPE = 0;
     */

    private static int UPDATE_BASIC_INFO_PERIOD = 10000;//10s
    public static int UPDATE_HEART_RATE_PERIOD = 30000;//30s
    private static int SENSORS_ACTIVATION_DELAY = 5000;//3s

    private static final int HEART_RATE_MAX_VALUE = 221;
    private static final int HEART_RATE_MIN_VALUE = 39;
    private static final int HEART_RATE_NO_GET_VALUE = 0;

    private BluetoothAdapter bluetoothAdapter;
    private static Band band;
    private static Handler basicInfoHandler;
    private static Runnable basicInfoPeriodicUpdater;
    private static Handler heartRateHandler;
    private static Runnable heartRatePeriodicUpdater;
    private static Handler sensorsActivationHandler;
    private static Runnable sensorsActivationRunnable;

    private static ContextProducer contextProducer;

    private static StepsInfo stepsInfo = new StepsInfo(0,0,0);


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
                enableBatteryNotification();
            } else if(ACTION_AWARE_BAND_DISABLE_NOTIFICATION.equalsIgnoreCase(action)) {
                /*Disable notifications*/
                disableHeartRateNotification();
                disableStepNotification();
                disableBatteryNotification();
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
        basicInfoPeriodicUpdater = () -> {
            refreshRSSI();
            basicInfoHandler.postDelayed(basicInfoPeriodicUpdater,UPDATE_BASIC_INFO_PERIOD);
        };

        heartRateHandler = new Handler();
        heartRatePeriodicUpdater = () -> {
            startHeartRateMeasurement(Protocol.HEART_RATE_MANUAL_MODE);
            heartRateHandler.postDelayed(heartRatePeriodicUpdater,UPDATE_HEART_RATE_PERIOD);
        };

        sensorsActivationHandler = new Handler();
        sensorsActivationRunnable = () -> {
            //Start heart rate measurement
            heartRateHandler.post(heartRatePeriodicUpdater);
        };

        //Any active plugin/sensor shares its overall context using broadcasts
        contextProducer = () -> {};
        CONTEXT_PRODUCER = contextProducer;

        //Add permissions you need (Support for Android M+)
        REQUIRED_PERMISSIONS.add(Manifest.permission.BLUETOOTH);
        REQUIRED_PERMISSIONS.add(Manifest.permission.BLUETOOTH_ADMIN);
        REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_FINE_LOCATION);
        REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        REQUIRED_PERMISSIONS.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        REQUIRED_PERMISSIONS.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        //To sync data to the server, you'll need to set this variables from your ContentProvider
        DATABASE_TABLES = Provider.DATABASE_TABLES;
        TABLES_FIELDS = Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ Provider.TableHeartRate_Data.CONTENT_URI, Provider.TableSteps_Data.CONTENT_URI};
    }

    //This function gets called every 5 minutes by AWARE to make sure this plugin is still running.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (PERMISSIONS_OK) {
            //Check if the user has toggled the debug messages
            DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

            //Initialize our plugin's settings
            Aware.setSetting(this, Settings.STATUS_PLUGIN_SENSORY_WRISTBAND, true);

            if (Aware.getSetting(getApplicationContext(), Settings.FREQUENCY_HEART_RATE).length() == 0)
                Aware.setSetting(getApplicationContext(), Settings.FREQUENCY_HEART_RATE, "30000");

            //Initialise AWARE instance in plugin
            Aware.startAWARE(this);

            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
                //Ask AWARE for Bluetooth
                Aware.setSetting(this, Aware_Preferences.STATUS_BLUETOOTH, true);
                Aware.setSetting(this, Aware_Preferences.FREQUENCY_BLUETOOTH, 60);
            } else {
                if (band == null) {
                    startScanActivity();
                }
            }

        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (band != null) {
            disconnect();
        }

        //Unregister receivers
        unregisterReceiver(deviceBroadcastReceiver);
        unregisterReceiver(enableNotificationReceiver);

        //Deactivate plugin
        Aware.setSetting(this, Settings.STATUS_PLUGIN_SENSORY_WRISTBAND, false);

        //Stop AWARE instance in plugin
        Aware.stopAWARE(this);
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
                band.setDisconnectedListener((d) -> disconnect());
                //Show band services and characteristics
                band.showServicesAndCharacteristics();
                //Pair/Authenticate band
                pairDevice();
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.e(TAG,"Failed on connect. Error :" + errorCode + " - " + msg);
            }
        });
    }

    /**
     * Disconnect device.
     */
    private void disconnect(){
        Log.d(TAG,"Device disconnected");
        if (!stepsInfo.isStepInfoEmpty()) {
            saveStepsInfoToDB(Plugin.stepsInfo);
            Plugin.stepsInfo = new StepsInfo(0, 0, 0);
        }
        basicInfoHandler.removeCallbacks(basicInfoPeriodicUpdater);
        heartRateHandler.removeCallbacks(heartRatePeriodicUpdater);
        stopStepNotification();
        stopHeartRateMeasurement(Protocol.HEART_RATE_MANUAL_MODE);
        band.disconnect();
        band = null;
        contextProducer = () -> {
            Intent intent = new Intent(ACTION_AWARE_BAND_DISCONNECTED);
            sendBroadcast(intent);
        };
        CONTEXT_PRODUCER = contextProducer;
        contextProducer.onContext();
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
                contextProducer = () -> {
                    Intent intent = new Intent(ACTION_AWARE_BAND_ENABLE_NOTIFICATION);
                    sendBroadcast(intent);
                    Intent nameIntent = new Intent(ACTION_AWARE_BAND_CONNECTED);
                    nameIntent.putExtra(EXTRA_DATA,band.getDevice().getName());
                    sendBroadcast(nameIntent);
                };
                CONTEXT_PRODUCER = contextProducer;
                contextProducer.onContext();
                //Start periodic updaters
                basicInfoHandler.post(basicInfoPeriodicUpdater);
                //Start after delay sensors notifications
                sensorsActivationHandler.postDelayed(sensorsActivationRunnable,SENSORS_ACTIVATION_DELAY);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.e(TAG,"Failed when pairing. Error: " + errorCode + " :: "+ msg);
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
                contextProducer = () -> {
                    Intent intent = new Intent(ACTION_AWARE_BAND_RSSI);
                    intent.putExtra(EXTRA_DATA,rssi);
                    sendBroadcast(intent);
                };
                CONTEXT_PRODUCER = contextProducer;
                contextProducer.onContext();
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.e(TAG,"Failed refreshing RSSI. Error :" + errorCode + " :: " + msg);
            }
        });
    }

    /**
     * Refresh Battery Info of the device
     */
    private void enableBatteryNotification(){
        band.setBatteryInfoListener((batteryInfo) -> {
            Log.d(TAG,batteryInfo.toString());
            contextProducer = () -> {
                Intent intent = new Intent(ACTION_AWARE_BAND_BATTERY);
                intent.putExtra(EXTRA_DATA, batteryInfo);
                sendBroadcast(intent);
            };
            CONTEXT_PRODUCER = contextProducer;
            contextProducer.onContext();
        });
    }

    private void disableBatteryNotification(){

    }

    /**
     * Enable real-time notification of the number of steps.
     */
    private void enableStepNotification(){
        band.setRealtimeStepsNotifyListener((stepsInfo) -> {
            Log.d(TAG,stepsInfo.toString());
            Plugin.stepsInfo = stepsInfo;
            contextProducer = () -> {
                Intent intent = new Intent(ACTION_AWARE_BAND_STEPS);
                intent.putExtra(EXTRA_DATA, stepsInfo);
                sendBroadcast(intent);
            };
            CONTEXT_PRODUCER = contextProducer;
            contextProducer.onContext();
        });
        startStepNotification();
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
        band.setHeartRateScanListener((heartRate) -> {
            if (isHeartRateMeasured(heartRate)) {
                if (isHeartRateValid(heartRate)) {
                    Log.d(TAG, "Heart rate: " + heartRate + " bmp");
                    saveHeartRateToDB(heartRate);
                } else {
                    Log.d(TAG, "Heart rate is out of range");
                    heartRate = 0;
                }
            } else {
                Log.d(TAG, "Heart rate not measured");
                heartRate = 0;
            }
            final int value = heartRate;
            contextProducer = () -> {
                Intent intent = new Intent(ACTION_AWARE_BAND_HEART_RATE);
                intent.putExtra(EXTRA_DATA, value);
                sendBroadcast(intent);
            };
            CONTEXT_PRODUCER = contextProducer;
            contextProducer.onContext();
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

    /**
     * Saves heart rate to database.
     *  - Heart Rate
     * @param heartRate - heart rate to save
     */
    private void saveHeartRateToDB(final int heartRate){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Provider.TableHeartRate_Data.TIMESTAMP, System.currentTimeMillis());
        contentValues.put(Provider.TableHeartRate_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
        contentValues.put(Provider.TableHeartRate_Data.HEART_RATE, heartRate);
        getContentResolver().insert(Provider.TableHeartRate_Data.CONTENT_URI, contentValues);
    }

    /**
     * Saves StepsInfo to database.
     *  - Steps
     *  - Distance
     *  - Calories
     * @param stepsInfo - StepsInfo to save
     */
    private void saveStepsInfoToDB(final StepsInfo stepsInfo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Provider.TableSteps_Data.TIMESTAMP, System.currentTimeMillis());
        contentValues.put(Provider.TableSteps_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
        contentValues.put(Provider.TableSteps_Data.STEPS, stepsInfo.getSteps());
        contentValues.put(Provider.TableSteps_Data.DISTANCE, stepsInfo.getDistance());
        contentValues.put(Provider.TableSteps_Data.CALORIES, stepsInfo.getCalories());
        getContentResolver().insert(Provider.TableSteps_Data.CONTENT_URI, contentValues);
    }

}

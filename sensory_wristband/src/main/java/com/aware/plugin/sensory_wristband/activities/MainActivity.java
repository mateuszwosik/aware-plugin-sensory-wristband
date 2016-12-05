package com.aware.plugin.sensory_wristband.activities;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import com.aware.plugin.sensory_wristband.R;
import com.aware.plugin.sensory_wristband.device.ActionCallback;
import com.aware.plugin.sensory_wristband.device.Band;
import com.aware.plugin.sensory_wristband.device.DeviceSelector;
import com.aware.plugin.sensory_wristband.device.HeartRateNotifyListener;
import com.aware.plugin.sensory_wristband.device.MiBand.model.BatteryInfo;
import com.aware.plugin.sensory_wristband.device.MiBand2.model.Protocol;
import com.aware.plugin.sensory_wristband.device.NotifyListener;
import com.aware.plugin.sensory_wristband.device.RealtimeStepsNotifyListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Set 1 - need to confirm each match/pairing. Just like in the official application.
     * Set 0 - no need for confirmation when User ID is the same as previous.
     *         In case device does not respond will be sent to normal notification listener with value 3
     */
    private static final int TYPE = 0;

    private static final String ENABLE = "mw.agh.thesis.engineering.swaware.ACTION_ENABLE";
    private static final String DISABLE = "mw.agh.thesis.engineering.swaware.ACTION_DISABLE";
    private static final int PERIOD = 10000;

    private Band band;
    private TextView rssiTextView;
    private TextView batteryTextView;
    private TextView sensorTextView;
    private TextView stepsTextView;
    private TextView heartRateTextView;

    private Handler handler;
    private Runnable periodicUpdater;

    private int uuid = 20202;//To change
    private int sex;
    private int age;
    private int height;
    private int weight;
    private String alias;

    /**
     * BroadcastReceiver responsible for enable and disable notifications of supported device services.
     */
    private final BroadcastReceiver enableNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(ENABLE.equalsIgnoreCase(action)){
                /*Enable notifications*/
                enableHeartRateNotification();
                enableStepNotification();
                enableNormalNotification();
                enableSensorNotification();
            } else if(DISABLE.equalsIgnoreCase(action)) {
                /*Disable notifications*/
                disableHeartRateNotification();
                disableStepNotification();
                disableNormalNotification();
                disableSensorNotification();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get user information and band
        Intent intent = getIntent();
        sex = intent.getIntExtra("sex",-1);
        age = intent.getIntExtra("age",0);
        height = intent.getIntExtra("height",0);
        weight = intent.getIntExtra("weight",0);
        alias = intent.getStringExtra("alias");
        BluetoothDevice device = intent.getParcelableExtra("device");

        //Get layout views
        rssiTextView = (TextView) findViewById(R.id.rssiTextView);
        batteryTextView = (TextView) findViewById(R.id.batteryTextView);
        sensorTextView = (TextView) findViewById(R.id.sensorTextView);
        stepsTextView = (TextView) findViewById(R.id.stepsTextView);
        heartRateTextView = (TextView) findViewById(R.id.heartRateTextView);

        //Register receiver that enables notification services
        registerReceiver(enableNotificationReceiver, createEnableNotificationIntentFilter());

        //Get band class
        band = DeviceSelector.getInstance().getSupportedDevice(device, this);
        if (band == null){
            Toast.makeText(this,"Not supported device",Toast.LENGTH_SHORT).show();
            Intent scanIntent = new Intent();
            scanIntent.setClass(MainActivity.this, ScanActivity.class);
            MainActivity.this.startActivity(scanIntent);
            MainActivity.this.finish();
        }

        //Connect to band
        connectDevice(device);

        //Get signal strength and battery info cyclically
        handler = new Handler();
        periodicUpdater = new Runnable() {
            @Override
            public void run() {
                refreshRSSI();
                refreshBatteryInfo();
                handler.postDelayed(periodicUpdater,PERIOD);
            }
        };

        //Disconnect band button
        (findViewById(R.id.disconnectButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                band.disconnect();
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ScanActivity.class);
                MainActivity.this.startActivity(intent);
                MainActivity.this.finish();
            }
        });

        //============ Immediate Alert ===========
        findViewById(R.id.alertMessageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                band.immediateAlert(Protocol.ALERT_LEVEL_MESSAGE);
            }
        });

        findViewById(R.id.alertPhoneCallButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                band.immediateAlert(Protocol.ALERT_LEVEL_PHONE_CALL);
            }
        });

        findViewById(R.id.alertVibrateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                band.immediateAlert(Protocol.ALERT_LEVEL_VIBRATE_ONLY);
            }
        });
        //========================================

        findViewById(R.id.startHearRateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHeartRateMeasurement();
            }
        });

        findViewById(R.id.startStepButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSensorNotification();
            }
        });

        findViewById(R.id.startSensorButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSensorNotification();
            }
        });

        findViewById(R.id.batteryButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        findViewById(R.id.rssiButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshRSSI();
            }
        });

        //
        findViewById(R.id.readButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        findViewById(R.id.writeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(enableNotificationReceiver);
        handler.removeCallbacks(periodicUpdater);
    }

    //============================================================
    /**
     * Creates IntentFilter for enable notification broadcast receiver.
     * @return IntentFilter with supported actions.
     */
    private static IntentFilter createEnableNotificationIntentFilter(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ENABLE);
        intentFilter.addAction(DISABLE);
        return intentFilter;
    }
    //============================================================

    //============================================================
    /**
     * Establish connection with device
     * When connection establish create disconnect listener
     * @param device - MiBand device
     */
    private void connectDevice(BluetoothDevice device){
        band.connect(device, new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                Log.d("connectDevice","Connected");
                //Set on disconnect listener
                band.setDisconnectedListener(new NotifyListener() {
                    @Override
                    public void onNotify(byte[] data) {
                        //Toast.makeText(getParent(),"Device disconnected",Toast.LENGTH_SHORT).show();
                        Log.d("connectDevice","Device disconnected");
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, ScanActivity.class);
                        MainActivity.this.startActivity(intent);
                        MainActivity.this.finish();
                    }
                });
                //Show band services and characteristics
                band.showServicesAndCharacteristics();
                //Pair/Authenticate band
                pairDevice();
            }

            @Override
            public void onFail(int errorCode, String msg) {
                //Toast.makeText(getParent(),"Connection failed",Toast.LENGTH_SHORT).show();
                Log.d("connectDevice","Error :" + errorCode + " - " + msg);
            }
        });
    }
    //============================================================

    //============================================================
    /**
     * Pair device.
     */
    private void pairDevice(){
        band.pair(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                Log.d("pairDevice","Successful pairing");
                //After successful pairing enable notification os supported band services
                Intent intent = new Intent(ENABLE);
                sendBroadcast(intent);
                //Start periodic updater
                handler.post(periodicUpdater);
                //Set user info to band
                band.setUserInfo(uuid,sex,age,height,weight,alias,TYPE);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.d("pairDevice","Failed pairing. " + errorCode + " :: "+ msg);
            }
        });
    }
    //============================================================


    //============================================================
    /**
     * Refresh Received Signal Strength Indicator (RSSI).
     */
    private void refreshRSSI(){
        band.getSignalStrength(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                int rssi = (int) data;
                displayRSSI(rssi);
                if(rssi > -70){
                    Log.d(TAG,"Signal excellent");
                } else if(rssi > -80){
                    Log.d(TAG,"Signal very good");
                } else if(rssi > -85){
                    Log.d(TAG,"Signal good");
                } else if(rssi > -90){
                    Log.d(TAG,"Signal bad");
                } else {
                    Log.d(TAG,"No signal");
                }
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.d(TAG,"Error :" + errorCode + " :: " + msg);
            }
        });
    }

    /**
     * Display Received Signal Strength Indicator (RSSI).
     * @param rssi - signal strength
     */
    private void displayRSSI(final int rssi){
        final String text = "Signal strength: " + rssi + " db";
        Log.d(TAG,text);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rssiTextView.setText(text);
            }
        });
    }
    //============================================================

    //============================================================
    /**
     * Refresh label with Battery Info of device
     */
    private void refreshBatteryInfo(){
        band.getBatteryInfo(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                BatteryInfo batteryInfo = (BatteryInfo) data;
                displayBatteryInfo(batteryInfo);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.d(TAG,"Error :" + errorCode + " :: " + msg);
            }
        });
    }

    /**
     * Display battery information.
     * @param batteryInfo - battery information
     */
    private void displayBatteryInfo(final BatteryInfo batteryInfo){
        final String text = "Battery: " + batteryInfo.toString();
        //cycles:4,level:44,status:unknown,last:2015-04-15 03:37:55
        Log.d(TAG,text);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                batteryTextView.setText(text);
            }
        });
    }
    //============================================================

    //============================================================
    /**
     * Enable normal notifications
     * Data length = 1
     * Not yet collected?
     */
    private void enableNormalNotification(){
        band.setNormalNotifyListener(new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                displayNormalNotifications(data);
            }
        });
    }

    /**
     * Disable normal notification.
     */
    private void disableNormalNotification(){
        band.removeNormalNotifyListener();
    }

    /**
     * Display normal notifications.
     * @param data - normal notification data
     */
    private void displayNormalNotifications(final byte[] data){
        Log.d(TAG, Arrays.toString(data));
    }
    //============================================================

    //============================================================
    /**
     * Enable real-time notification of the number of steps.
     * Set. Shake bracelet (need to shake under 10-20 to trigger).
     * It is based on two steps:
     *  1. Set the listener
     *  2. Turn on notification
     */
    private void enableStepNotification(){
        band.setRealtimeStepsNotifyListener(new RealtimeStepsNotifyListener() {
            @Override
            public void onNotify(int steps) {
                displayStepNumber(steps);
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
     * To start again steps notification only need to call startStepNuotification()
     */
    private void stopStepNotification(){
        band.disableRealtimeStepsNotify();
    }

    /**
     * Display number of steps.
     * @param steps - number of steps
     */
    private void displayStepNumber(final int steps){
        final String text = "Number of steps: " + steps;
        Log.d(TAG,text);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stepsTextView.setText(text);
            }
        });
    }
    //============================================================

    //============================================================
    /**
     * Enable sensor notify listener (raw data).
     * Two steps:
     *  1. Set the listener
     *  2. Turn on notification
     */
    private void enableSensorNotification(){
        band.setSensorDataNotifyListener(new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                int i = 0;
                int index = (data[i++] & 0xFF) | (data[i++] & 0xFF) << 8;  // serial number
                int d1 = (data[i++] & 0xFF) | (data[i++] & 0xFF) << 8;
                int d2 = (data[i++] & 0xFF) | (data[i++] & 0xFF) << 8;
                int d3 = (data[i++] & 0xFF) | (data[i] & 0xFF) << 8;
                displaySensorNotification(index, d1, d2, d3);
            }
        });
    }

    /**
     * Disable sensor notification.
     */
    private void disableSensorNotification(){
        band.removeSensorDataNotifyListener();
    }

    /**
     * Start sensor notification.
     */
    private void startSensorNotification(){
        band.enableSensorDataNotify();
    }

    /**
     * Stop sensor notification.
     */
    private void stopSensorNotification(){
        band.disableSensorDataNotify();
    }

    /**
     * Display sensor notification data.
     * @param index - index
     * @param d1 - d1
     * @param d2 - d2
     * @param d3 - d3
     */
    private void displaySensorNotification(final int index, final int d1, final int d2, final int d3){
        final String text = "[index: " + index + "; d1: " + d1 + "; d2: " + d2 + "; d3: " + d3 + "]";
        Log.d(TAG, text);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sensorTextView.setText(text);
            }
        });
    }
    //============================================================

    //============================================================
    /**
     * Enable heart rate notification.
     * Two steps:
     *  1. Set the listener
     *  2. Turn on notification
     */
    private void enableHeartRateNotification(){
        //Set the heart rate scan notification
        band.setHeartRateScanListener(new HeartRateNotifyListener() {
            @Override
            public void onNotify(int heartRate) {
                displayHeartRateMeasurement(heartRate);
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
     */
    private void startHeartRateMeasurement(){
        band.startHeartRateScan(Protocol.HEART_RATE_MANUAL_MODE);
    }

    /**
     * Stop heart rate measurement/scan.
     */
    private void stopHeartRateMeasurement(){
        band.stopHeartRateScan(Protocol.HEART_RATE_MANUAL_MODE);
    }

    /**
     * Display Hear Rate Measurement result.
     * @param heartRate - heart rate
     */
    private void displayHeartRateMeasurement(final int heartRate){
        final String text = "Heart rate: " + heartRate + "bpm";
        Log.d(TAG, text);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                heartRateTextView.setText(text);
            }
        });
    }
    //============================================================

}

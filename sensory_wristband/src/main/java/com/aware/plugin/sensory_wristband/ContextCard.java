package com.aware.plugin.sensory_wristband;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aware.plugin.sensory_wristband.device.BatteryInfo;
import com.aware.plugin.sensory_wristband.device.StepsInfo;
import com.aware.utils.IContextCard;


public class ContextCard implements IContextCard {

    private TextView heartRateTextView;
    private TextView stepsTextView;
    private TextView distanceTextView;
    private TextView caloriesTextView;
    private ImageView batteryImageView;
    private TextView batteryLevelTextView;
    private ImageView rssiImageView;
    private TextView nameTextView;
    private TextView infoTextView;

    private static String name = "";
    private static String heartRate ="0 bmp";
    private static String steps = "0";
    private static String distance = "0 m";
    private static String calories = "0 kcal";
    private static int rssiResId  = R.drawable.rssi_0;
    private static int batteryResId = R.drawable.battery_level_0;
    private static int batteryLevel = 0;
    private static String info = "";

    //Constructor used to instantiate this card
    public ContextCard() {}

    @Override
    public View getContextCard(Context context) {
        //Load card layout
        View card = LayoutInflater.from(context).inflate(R.layout.card, null);

        //Initialize UI elements from the card
        heartRateTextView = (TextView) card.findViewById(R.id.heartRateTextView);
        stepsTextView = (TextView) card.findViewById(R.id.stepsTextView);
        distanceTextView = (TextView) card.findViewById(R.id.distanceTextView);
        caloriesTextView = (TextView) card.findViewById(R.id.caloriesTextView);
        batteryImageView = (ImageView) card.findViewById(R.id.batteryImageView);
        batteryLevelTextView = (TextView) card.findViewById(R.id.batteryLevelTextView);
        rssiImageView = (ImageView) card.findViewById(R.id.rssiImageView);
        nameTextView = (TextView) card.findViewById(R.id.nameTextView);
        infoTextView = (TextView) card.findViewById(R.id.infoTextView);

        infoTextView.setText(info);
        nameTextView.setText(name);
        rssiImageView.setImageResource(rssiResId);
        batteryImageView.setImageResource(batteryResId);
        final String level = batteryLevel + "%";
        batteryLevelTextView.setText(level);
        stepsTextView.setText(steps);
        distanceTextView.setText(distance);
        caloriesTextView.setText(calories);
        heartRateTextView.setText(heartRate);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Plugin.ACTION_AWARE_BAND_CONNECTED);
        intentFilter.addAction(Plugin.ACTION_AWARE_BAND_DISCONNECTED);
        intentFilter.addAction(Plugin.ACTION_AWARE_BAND_NEW_DATA);
        context.registerReceiver(dataBroadcastReceiver,intentFilter);

        //Return the card to AWARE/apps
        return card;
    }

    /**
     * Update Received Signal Strength Indicator (RSSI).
     * @param rssi - signal strength
     */
    private void updateRSSI(final int rssi){
        if(rssi > -70){
            rssiResId = R.drawable.rssi_4;
        } else if(rssi > -80){
            rssiResId = R.drawable.rssi_3;
        } else if(rssi > -85){
            rssiResId = R.drawable.rssi_2;
        } else if(rssi > -90){
            rssiResId = R.drawable.rssi_1;
        } else {
            rssiResId = R.drawable.rssi_0;
        }
        rssiImageView.setImageResource(rssiResId);
    }

    /**
     * Update battery information.
     * @param batteryInfo - battery information
     */
    private void updateBatteryInfo(final BatteryInfo batteryInfo){
        if (batteryInfo != null) {
            batteryLevel = batteryInfo.getLevel();
            if (batteryLevel > 80) {
                batteryResId = R.drawable.battery_level_4;
            } else if (batteryLevel > 60) {
                batteryResId = R.drawable.battery_level_3;
            } else if (batteryLevel > 40) {
                batteryResId = R.drawable.battery_level_2;
            } else if (batteryLevel > 20) {
                batteryResId = R.drawable.battery_level_1;
            } else {
                batteryResId = R.drawable.battery_level_0;
            }
            batteryImageView.setImageResource(batteryResId);
            final String level = batteryLevel + "%";
            batteryLevelTextView.setText(level);
        }
    }

    /**
     * Update steps information.
     * @param stepsInfo - steps information (steps,distance,calories)
     */
    private void updateStepNumber(final StepsInfo stepsInfo){
        if (stepsInfo != null) {
            ContextCard.steps = String.valueOf(stepsInfo.getSteps());
            ContextCard.distance = String.valueOf(stepsInfo.getDistance()) + " m";
            ContextCard.calories = String.valueOf(stepsInfo.getCalories()) + " kcal";
            stepsTextView.setText(ContextCard.steps);
            distanceTextView.setText(ContextCard.distance);
            caloriesTextView.setText(ContextCard.calories);
        }
    }

    /**
     * Update Hear Rate.
     * @param heartRate - heart rate
     */
    private void updateHeartRate(final int heartRate){
        if (heartRate != 0) {
            ContextCard.info = "";
        } else {
            ContextCard.info = "Band is in wrong position. Wear band tightly above the wrist and try again.";
        }
        ContextCard.heartRate = String.valueOf(heartRate) + " bpm";
        heartRateTextView.setText(ContextCard.heartRate);
        infoTextView.setText(ContextCard.info);
    }

    /**
     * Display connected band name.
     * @param name - name of the connected band
     */
    private void setBandName(final String name) {
        ContextCard.name = name;
        nameTextView.setText(ContextCard.name);
    }

    /**
     * Initialize all views and variables.
     */
    private void init() {
        setBandName("");

        updateRSSI(0);

        batteryResId = R.drawable.battery_level_0;
        batteryImageView.setImageResource(batteryResId);
        batteryLevel = 0;
        final String level = batteryLevel + "%";
        batteryLevelTextView.setText(level);

        steps = "0";
        distance = "0 m";
        calories = "0 kcal";
        stepsTextView.setText(steps);
        distanceTextView.setText(distance);
        caloriesTextView.setText(calories);

        heartRate = "0 bpm";
        heartRateTextView.setText(heartRate);
        info = "";
        infoTextView.setText(info);
    }

    private DataBroadcastReceiver dataBroadcastReceiver = new DataBroadcastReceiver();
    public class DataBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case Plugin.ACTION_AWARE_BAND_CONNECTED:
                    setBandName(intent.getStringExtra(Plugin.EXTRA_BAND_NAME_DATA));
                    break;
                case Plugin.ACTION_AWARE_BAND_DISCONNECTED:
                    init();
                    break;
                case Plugin.ACTION_AWARE_BAND_NEW_DATA:
                    updateRSSI(intent.getIntExtra(Plugin.EXTRA_BAND_RSSI_DATA,0));
                    updateBatteryInfo((BatteryInfo)intent.getSerializableExtra(Plugin.EXTRA_BAND_BATTERY_DATA));
                    updateHeartRate(intent.getIntExtra(Plugin.EXTRA_BAND_HEART_RATE_DATA,0));
                    updateStepNumber((StepsInfo) intent.getSerializableExtra(Plugin.EXTRA_BAND_STEPS_DATA));
                    break;
            }
        }
    }

}

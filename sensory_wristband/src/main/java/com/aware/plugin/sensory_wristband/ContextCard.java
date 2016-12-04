package com.aware.plugin.sensory_wristband;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aware.plugin.sensory_wristband.device.MiBand.model.BatteryInfo;
import com.aware.utils.IContextCard;

import java.util.Timer;
import java.util.TimerTask;

public class ContextCard implements IContextCard {

    private TextView heartRateTextView;
    private TextView stepsTextView;
    private ImageView batteryImageView;
    private ImageView rssiImageView;

    //Constructor used to instantiate this card
    public ContextCard() {

    }

    @Override
    public View getContextCard(Context context) {
        //Load card information to memory
        LayoutInflater sInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View card = sInflater.inflate(R.layout.card, null);

        //Initialize UI elements from the card
        heartRateTextView = (TextView) card.findViewById(R.id.heartRateTextView);
        stepsTextView = (TextView) card.findViewById(R.id.stepsTextView);
        batteryImageView = (ImageView) card.findViewById(R.id.batteryImageView);
        rssiImageView = (ImageView) card.findViewById(R.id.rssiImageView);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Plugin.ACTION_AWARE_BAND_RSSI);
        intentFilter.addAction(Plugin.ACTION_AWARE_BAND_BATTERY);
        intentFilter.addAction(Plugin.ACTION_AWARE_BAND_HEART_RATE);
        intentFilter.addAction(Plugin.ACTION_AWARE_BAND_STEPS);
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
            rssiImageView.setImageResource(R.drawable.rssi_4);
        } else if(rssi > -80){
            rssiImageView.setImageResource(R.drawable.rssi_3);
        } else if(rssi > -85){
            rssiImageView.setImageResource(R.drawable.rssi_2);
        } else if(rssi > -90){
            rssiImageView.setImageResource(R.drawable.rssi_1);
        } else {
            rssiImageView.setImageResource(R.drawable.rssi_0);
        }
    }

    /**
     * Update battery information.
     * @param batteryInfo - battery information
     */
    private void updateBatteryInfo(final BatteryInfo batteryInfo){
        //cycles:4,level:44,status:unknown,last:2015-04-15 03:37:55
        int level = batteryInfo.getLevel();
        if(level > 80){
            batteryImageView.setImageResource(R.drawable.battery_level_4);
        } else if(level > 60){
            batteryImageView.setImageResource(R.drawable.battery_level_3);
        } else if(level > 40){
            batteryImageView.setImageResource(R.drawable.battery_level_2);
        } else if(level > 20){
            batteryImageView.setImageResource(R.drawable.battery_level_1);
        } else {
            batteryImageView.setImageResource(R.drawable.battery_level_0);
        }
    }

    /**
     * Update number of steps.
     * @param steps - number of steps
     */
    private void updateStepNumber(final int steps){
        stepsTextView.setText(String.valueOf(steps));
    }

    /**
     * Update Hear Rate.
     * @param heartRate - heart rate
     */
    private void updateHeartRate(final int heartRate){
        final String text = String.valueOf(heartRate) + " bpm";
        heartRateTextView.setText(text);
    }

    private DataBroadcastReceiver dataBroadcastReceiver = new DataBroadcastReceiver();
    public class DataBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case Plugin.ACTION_AWARE_BAND_RSSI:
                    updateRSSI(intent.getIntExtra(Plugin.EXTRA_DATA,0));
                    break;
                case Plugin.ACTION_AWARE_BAND_BATTERY:
                    updateBatteryInfo((BatteryInfo)intent.getSerializableExtra(Plugin.EXTRA_DATA));
                    break;
                case Plugin.ACTION_AWARE_BAND_STEPS:
                    updateStepNumber(intent.getIntExtra(Plugin.EXTRA_DATA,0));
                    break;
                case Plugin.ACTION_AWARE_BAND_HEART_RATE:
                    updateHeartRate(intent.getIntExtra(Plugin.EXTRA_DATA,0));
                    break;
            }
        }
    }

}

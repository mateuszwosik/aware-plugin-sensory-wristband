package com.aware.plugin.sensory_wristband.device.MiBand2.model;

import android.util.Log;

import com.aware.plugin.sensory_wristband.device.BatteryInfo;

public class BatteryInfoMiBand2 implements BatteryInfo {

    private static final int BATTERY_NORMAL = 0;
    private static final int BATTERY_CHARGING = 1;

    private int level;
    private int state;

    private BatteryInfoMiBand2() {

    }

    public BatteryInfoMiBand2(int level) {
        this.level = level;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "BatteryInfoMiBand2[level="+getLevel()+"%]";
    }

    /**
     * Length 20 bytes
     * @param data - bates representing battery information
     * @return BatteryInfoMiBand2
     * last charge level -> data[19]
     * last charge time -> data[10,11,12,13,14,15,16,17]
     *      GregorianCalendar(
     *          10,11 -> year,
     *          12 & 0xFF,
     *          13 & 0xFF,
     *          14 & 0xFF,
     *          15 & 0xFF,
     *          16 & 0xFF,
     *          17 & 0xFF
     *      )
     */
    public static BatteryInfoMiBand2 fromByteData(byte[] data) {
        if (data.length < 20) {
            return null;
        }
        BatteryInfoMiBand2 batteryInfoMiBand2 = new BatteryInfoMiBand2();
        batteryInfoMiBand2.level = data[1];
        batteryInfoMiBand2.state = data[2];
        return batteryInfoMiBand2;
    }

}

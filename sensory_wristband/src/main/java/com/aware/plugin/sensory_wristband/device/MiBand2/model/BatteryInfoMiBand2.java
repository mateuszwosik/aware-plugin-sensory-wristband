package com.aware.plugin.sensory_wristband.device.MiBand2.model;

import com.aware.plugin.sensory_wristband.device.BatteryInfo;

public class BatteryInfoMiBand2 implements BatteryInfo {

    private int level;

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
     */
    public static BatteryInfoMiBand2 fromByteData(byte[] data) {
        if (data.length < 20) {
            return null;
        }
        BatteryInfoMiBand2 batteryInfoMiBand2 = new BatteryInfoMiBand2();
        batteryInfoMiBand2.level = data[1];
        return batteryInfoMiBand2;
    }

}

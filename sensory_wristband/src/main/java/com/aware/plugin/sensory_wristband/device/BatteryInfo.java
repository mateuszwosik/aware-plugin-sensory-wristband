package com.aware.plugin.sensory_wristband.device;

import java.io.Serializable;

public interface BatteryInfo extends Serializable {

    enum BatteryState{
        NORMAL, CHARGING
    }

    int getLevel();
    BatteryState getBatteryState();
    String toString();

}

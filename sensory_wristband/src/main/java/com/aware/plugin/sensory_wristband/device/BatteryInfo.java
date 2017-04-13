package com.aware.plugin.sensory_wristband.device;

import java.io.Serializable;

public interface BatteryInfo extends Serializable {
    int getLevel();
    String toString();

}

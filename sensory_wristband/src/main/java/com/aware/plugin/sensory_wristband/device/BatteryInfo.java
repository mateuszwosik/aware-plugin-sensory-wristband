package com.aware.plugin.sensory_wristband.device;

import java.io.Serializable;

public interface BatteryInfo extends Serializable {
    public int getLevel();
    public String toString();

}

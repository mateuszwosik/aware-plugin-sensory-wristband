package com.aware.plugin.sensory_wristband.device;

import com.aware.plugin.sensory_wristband.device.MiBand2.model.StepsInfo;

public interface RealtimeStepsNotifyListener {
    void onNotify(StepsInfo stepsInfo);
}

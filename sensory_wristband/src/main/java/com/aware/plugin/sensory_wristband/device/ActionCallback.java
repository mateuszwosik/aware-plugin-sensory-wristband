package com.aware.plugin.sensory_wristband.device;

public interface ActionCallback {
    void onSuccess(Object data);

    void onFail(int errorCode, String msg);
}

package com.aware.plugin.sensory_wristband.device;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import com.aware.plugin.sensory_wristband.device.MiBand.MiBand;
import com.aware.plugin.sensory_wristband.device.MiBand2.MiBand2;

public class DeviceSelector {

    private static final DeviceSelector deviceSelector = new DeviceSelector();

    private static List<Band> supportedDevices;

    public static DeviceSelector getInstance(){
        return deviceSelector;
    }

    private List<Band> getSupportedDevices(Context context){
        List<Band> bands = new ArrayList<>(2);
        bands.add(new MiBand2(context));
        bands.add(new MiBand(context));
        return bands;
    }

    public void setSupportedDevices(Context context){
        supportedDevices = new ArrayList<>(2);
        supportedDevices.add(new MiBand2(context));
        supportedDevices.add(new MiBand(context));
    }

    @Nullable
    public Band getSupportedDevice(BluetoothDevice device){
        for (Band band : supportedDevices) {
            if (band.supports(device)){
                return band;
            }
        }
        return null;
    }

    @Nullable
    public Band getSupportedDevice(BluetoothDevice device, Context context){
        for (Band band : getSupportedDevices(context)) {
            if (band.supports(device)){
                return band;
            }
        }
        return null;
    }

    @Nullable
    public static Band selectDevice(BluetoothDevice device, Context context){
        String deviceName = device.getName();
        if("Mi Band 2".equals(deviceName)){
            return new MiBand2(context);
        } else if("Mi Band".equals(deviceName)){
            return new MiBand(context);
        } else {
            return null;
        }
    }

}

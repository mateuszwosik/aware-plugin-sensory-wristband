package com.aware.plugin.sensory_wristband.device;

import android.bluetooth.BluetoothDevice;

public interface Band {

    /**
     * Check if band is supported.
     * @param device - Device to check
     * @return true - device supported | false - device not supported
     */
    boolean supports(BluetoothDevice device);

    /**
     * Get Bluetooth Device which is connected to the application.
     * @return - BluetoothDevice which is connected.
     */
    BluetoothDevice getDevice();

    /**
     * Connect to the device.
     * @param device - Device to connect
     * @param callback - Callback which handles successful and failed connect events
     */
    void connect(BluetoothDevice device, final ActionCallback callback);

    /**
     * Disconnect device and close connection with BluetoothGatt.
     */
    void disconnect();

    /**
     * Set listener that handles disconnect events. For example when band is out of reach.
     * @param disconnectedListener - Listener that handles disconnected event
     */
    void setDisconnectedListener(NotifyListener disconnectedListener);

    /**
     * Pair/Authenticate device.
     * @param callback - Callback that handles pair events (success and fail)
     */
    void pair(final ActionCallback callback);

    /**
     * Get signal strength (RSSI). Return signal strength in callback on success method as int (data).
     * @param callback - Callback that handles signal strength events
     */
    void getSignalStrength(ActionCallback callback);

    /**
     * Get battery information.
     * @param listener - Handle returned battery information from the band
     */
    void setBatteryInfoListener(final BatteryNotifyListener listener);

    /**
     * Set user information.
     * @param uid - Unique identifier
     * @param gender - User gender (1 - men | 0 - women)
     * @param age - User age
     * @param height - User height
     * @param weight - User weight
     * @param alias - User alias/name
     * @param type - Type of connection (if it is always needed pairing)
     */
    void setUserInfo(int uid, int gender, int age, int height, int weight, String alias, int type);

    /**
     * Show Services, Characteristics and Descriptors of the band.
     */
    void showServicesAndCharacteristics();

    /**
     * Immediate alert with one of alert levels: message, phone call, vibrate only.
     * @param alertLevel - Alert level
     */
    void immediateAlert(byte alertLevel);

    /**
     * Stop immediate alert.
     */
    void stopImmediateAlert();

    /**
     * Set/Enable realtime steps notification.
     * @param listener - Handle returned steps information from the band
     */
    void setStepsNotifyListener(final StepsNotifyListener listener);

    /**
     * Set/Enable Heart Rate Measurement Notifications and set listener that handle heart rate from the band.
     * @param listener - listener that will be notify when heart rate will be measured
     */
    void setHeartRateScanListener(final HeartRateNotifyListener listener);

    /**
     * Start Heart Rate Measurement.
     * @param hearRateMode - heart rate mode
     */
    void startHeartRateScan(byte hearRateMode);

    /**
     * Stop Heart Rate Measurement.
     * @param heartRateMode - which heart rate mode stop
     */
    void stopHeartRateScan(byte heartRateMode);

    /**
     * Set/Enable Button notifications when it will be touched.
     * @param listener - listener that will be notify when button will be touched
     */
    void setButtonListener(final NotifyListener listener);

}

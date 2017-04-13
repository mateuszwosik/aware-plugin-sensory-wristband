package com.aware.plugin.sensory_wristband.device;


import android.bluetooth.BluetoothDevice;

public interface Band {

    /*==================== Device ====================*/
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
    /*==================== Device END ====================*/

    /*==================== Connect ====================*/
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
    /*==================== Connect END ====================*/

    /*==================== Pair/Authenticate ====================*/
    /**
     * Pair/Authenticate device.
     * @param callback - Callback that handles pair events (success and fail)
     */
    void pair(final ActionCallback callback);
    /*==================== Pair/Authenticate END ====================*/

    /*==================== RSSI ====================*/
    /**
     * Get signal strength (RSSI). Return signal strength in callback on success method as int (data).
     * @param callback - Callback that handles signal strength events
     */
    void getSignalStrength(ActionCallback callback);
    /*==================== RSSI END ====================*/

    /*==================== Generic Access ====================*/
    /**
     * Get device name.
     * @param callback - Callback that handles success and fail events
     */
    void getDeviceName(ActionCallback callback);

    /**
     * Get device appearance.
     * @param callback - Callback that handles success and fail events
     */
    void getAppearance(ActionCallback callback);

    /**
     * Get device peripheral preferred parameters.
     * @param callback - Callback that handles success and fail events
     */
    void getPeripheralPreferredParameters(ActionCallback callback);
    /*==================== Generic Access END ====================*/

    /*==================== Generic Attribute ====================*/
    /**
     * Indicate when device service changed.
     */
    void setServiceChangedIndicate();
    /*==================== Generic Attribute END ====================*/

    /*==================== Device Information ====================*/
    /**
     * Get serial number of the device.
     * @param callback - Callback that handles success and fail events
     */
    void getSerialNumber(ActionCallback callback);

    /**
     * Get hardware revision string of the device.
     * @param callback - Callback that handles success and fail events
     */
    void getHardwareRevision(ActionCallback callback);

    /**
     * Get software revision string of the device.
     * @param callback - Callback that handles success and fail events
     */
    void getSoftwareRevision(ActionCallback callback);

    /**
     * Get system identification of the device.
     * @param callback - Callback that handles success and fail events
     */
    void getSystemID(ActionCallback callback);

    /**
     * Get Plug and Play identification of the device.
     * @param callback - Callback that handles success and fail events
     */
    void getPnPID(ActionCallback callback);

    /**
     * Get all device information in one array of length 5.
     * Uses only onSuccess method of given ActionCallback.
     * @param callback - Callback that handles success and fail events
     */
    void getDeviceInformation(ActionCallback callback);
    /*==================== Device Information END ====================*/

    /*==================== Battery Information ====================*/
    /**
     * Get battery information.
     * @param listener - Handle returned battery information from the band
     */
    void setBatteryInfoListener(final BatteryNotifyListener listener);
    /*==================== Battery Information END ====================*/

    /*==================== User Information ====================*/
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
    /*==================== User Information END ====================*/

    /*==================== Services and Characteristics ====================*/
    /**
     * Show Services, Characteristics and Descriptors of the band.
     */
    void showServicesAndCharacteristics();
    /*==================== Services and Characteristics END ====================*/

    /*==================== Immediate Alert ====================*/
    /**
     * Immediate alert with one of alert levels: message, phone call, vibrate only.
     * @param alertLevel - Alert level
     */
    void immediateAlert(byte alertLevel);

    /**
     * Stop immediate alert.
     */
    void stopImmediateAlert();
    /*==================== Immediate Alert END ====================*/

    /*==================== Normal Notification ====================*/
    /**
     * Set/Enable Normal Notification.
     * @param listener - Handle returned data
     */
    void setNormalNotifyListener(NotifyListener listener);

    /**
     * Remove/Disable Normal Notification.
     */
    void removeNormalNotifyListener();
    /*==================== Normal Notification END ====================*/

    /*==================== Sensor Data ====================*/
    /**
     * Set/Enable sensor data notification.
     * @param listener - Handle returned data
     */
    void setSensorDataNotifyListener(final NotifyListener listener);

    /**
     * Remove/Disable sensor data notification.
     */
    void removeSensorDataNotifyListener();

    /**
     * Start sensor data notification.
     */
    void enableSensorDataNotify();

    /**
     * Stop sensor data notification.
     */
    void disableSensorDataNotify();
    /*==================== Sensor Data END ====================*/

    /*==================== Steps ====================*/
    /**
     * Set/Enable realtime steps notification.
     * @param listener - Handle returned steps information from the band
     */
    void setRealtimeStepsNotifyListener(final RealtimeStepsNotifyListener listener);

    /**
     * Remove/Disable realtime steps notification.
     */
    void removeRealtimeStepsNotifyListener();

    /**
     * Start realtime steps notification.
     * Start counting number of steps.
     */
    void enableRealtimeStepsNotify();

    /**
     * Stop realtime steps notification.
     * Stop counting number of steps.
     */
    void disableRealtimeStepsNotify();
    /*==================== Steps END ====================*/

    /*==================== Heart Rate ====================*/
    /**
     * Set/Enable Heart Rate Measurement Notifications and set listener that handle heart rate from the band.
     * @param listener - listener that will be notify when heart rate will be measured
     */
    void setHeartRateScanListener(final HeartRateNotifyListener listener);

    /**
     * Remove/Disable Heart Rate Measurement Notifications.
     */
    void removeHeartRateScanListener();

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
    /*==================== Heart Rate END ====================*/

    /*==================== Button ====================*/

    /**
     * Set/Enable Button notifications when it will be touched.
     * @param listener - listener that will be notify when button will be touched
     */
    void setButtonListener(final NotifyListener listener);
    /*==================== Button END ====================*/
}

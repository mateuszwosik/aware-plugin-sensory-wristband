package com.aware.plugin.sensory_wristband.device.MiBand2.model;

import java.util.UUID;

public class Profile {
    // ========================== Mi Band 2 ============================

    /*=== New service - Generic Access ===*/
    public static final UUID UUID_SERVICE_GENERIC_ACCESS = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHAR_DEVICE_NAME = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");//READ
    public static final UUID UUID_CHAR_APPEARANCE = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb");//READ
    public static final UUID UUID_CHAR_PERIPHERAL_PREFERRED_PARAMETERS = UUID.fromString("00002a04-0000-1000-8000-00805f9b34fb");//READ

    /*=== New service - Generic Attribute ===*/
    public static final UUID UUID_SERVICE_GENERIC_ATTRIBUTE = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHAR_SERVICE_CHANGED = UUID.fromString("00002a05-0000-1000-8000-00805f9b34fb");//INDICATE //descriptor: 2902

    /*=== New service - Device Information ===*/
    public static final UUID UUID_SERVICE_DEVICE_INFORMATION = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");//ff01
    public static final UUID UUID_CHAR_SERIAL_NUMBER_STRING = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb");//READ
    public static final UUID UUID_CHAR_HARDWARE_REVISION_STRING = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb");//READ
    public static final UUID UUID_CHAR_SOFTWARE_REVISION_STRING = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb");//READ
    public static final UUID UUID_CHAR_SYSTEM_ID = UUID.fromString("00002a23-0000-1000-8000-00805f9b34fb");//READ
    public static final UUID UUID_CHAR_PNP_ID = UUID.fromString("00002a50-0000-1000-8000-00805f9b34fb");//READ

    /*=== New service - Firmware ===*/
    public static final UUID UUID_SERVICE_FIRMWARE = UUID.fromString("00001530-0000-3512-2118-0009af100700");
    public static final UUID UUID_CHAR_FIRMWARE = UUID.fromString("00001531-0000-3512-2118-0009af100700");//INDICATE?? //descriptor: 2902
    public static final UUID UUID_CHAR_FIRMWARE_DATA = UUID.fromString("00001532-0000-3512-2118-0009af100700");//WRITE??

    /*=== New service - Alert Notification ===*/
    public static final UUID UUID_SERVICE_ALERT_NOTIFICATION = UUID.fromString("00001811-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHAR_NEW_ALERT = UUID.fromString("00002a46-0000-1000-8000-00805f9b34fb");//NOTIFY
    public static final UUID UUID_CHAR_ALERT_NOTIFICATION_CONTROL_POINT = UUID.fromString("00002a44-0000-1000-8000-00805f9b34fb");//WRITE //descriptor: 2902

    /*=== New service - Immediate Alert ===*/
    public static final UUID UUID_SERVICE_IMMEDIATE_ALERT = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHAR_ALERT_LEVEL = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");//WRITEWithOutResponse

    /*=== New service - Heart Rate ===*/
    public static final UUID UUID_SERVICE_HEART_RATE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHAR_HEART_RATE_MEASUREMENT = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");//NOTIFY //descriptor: 2902
    public static final UUID UUID_CHAR_HEART_RATE_CONTROL_POINT = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb");//WRITE

    /*=== New service - Mili Service (???) ===*/
    public static final UUID UUID_SERVICE_MILI = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHAR_CURRENT_TIME = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb");//READ NOTIFY write //descriptor: 2902
    public static final UUID UUID_CHAR_UNKNOWN_1 = UUID.fromString("00000001-0000-3512-2118-0009af100700");//descriptor: 2902
    public static final UUID UUID_CHAR_UNKNOWN_2 = UUID.fromString("00000002-0000-3512-2118-0009af100700");//descriptor: 2902
    public static final UUID UUID_CHAR_CONFIGURATION = UUID.fromString("00000003-0000-3512-2118-0009af100700");//descriptor: 2902 !?
    public static final UUID UUID_CHAR_PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS = UUID.fromString("00002a04-0000-1000-8000-00805f9b34fb");//descriptor: 2902
    public static final UUID UUID_CHAR_UNKNOWN_4 = UUID.fromString("00000004-0000-3512-2118-0009af100700");//descriptor: 2902 !?
    public static final UUID UUID_CHAR_ACTIVITY_DATA = UUID.fromString("00000005-0000-3512-2118-0009af100700");//descriptor: 2902
    public static final UUID UUID_CHAR_BATTERY = UUID.fromString("00000006-0000-3512-2118-0009af100700");//descriptor: 2902
    public static final UUID UUID_CHAR_STEPS = UUID.fromString("00000007-0000-3512-2118-0009af100700");//descriptor: 2902
    public static final UUID UUID_CHAR_UNKNOWN_8 = UUID.fromString("00000008-0000-3512-2118-0009af100700");//descriptor: 2902
    public static final UUID UUID_CHAR_BUTTON = UUID.fromString("00000010-0000-3512-2118-0009af100700");//descriptor: 2902

    /*=== New service - MiBand 2 Service ===*/
    public static final UUID UUID_SERVICE_MIBAND2 = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHARACTERISTIC_AUTH = UUID.fromString("00000009-0000-3512-2118-0009af100700");//descriptor: 2902
    public static final UUID UUID_CHAR_UNKNOWN_11 = UUID.fromString("0000fedd-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHAR_UNKNOWN_12 = UUID.fromString("0000fede-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHAR_UNKNOWN_13 = UUID.fromString("0000fedf-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHAR_UNKNOWN_14 = UUID.fromString("0000fed0-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHAR_UNKNOWN_15 = UUID.fromString("0000fed1-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHAR_UNKNOWN_16 = UUID.fromString("0000fed2-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_CHAR_UNKNOWN_17 = UUID.fromString("0000fed3-0000-1000-8000-00805f9b34fb");

    /*=== Descriptor for characteristics ===*/
    public static final UUID UUID_DESCRIPTOR_UPDATE_NOTIFICATION = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // ========================== end ============================
}

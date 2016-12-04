package com.aware.plugin.sensory_wristband.device.MiBand2.model;


import com.aware.plugin.sensory_wristband.utils.ByteArray;

public class Protocol {

    public static final byte DISABLE = 0x0;
    public static final byte ENABLE = 0x1;

    // ==================== HEART RATE =====================
    /**
     * 0x15 - means Heart Rate sensor
     * MODE bytes is the second parameter in heart rate scan.
     * Third parameter and last one is for enable/disable functionality.
     */
    public static final byte HEART_RATE_SLEEP_MODE = 0x0;
    public static final byte HEART_RATE_CONTINUOUS_MODE = 0x1;
    public static final byte HEART_RATE_MANUAL_MODE = 0x2;
    public static final byte[] START_HEART_RATE_SCAN = {0x15, HEART_RATE_MANUAL_MODE, ENABLE};
    // ========================== END ===========================

    //Alert levels
    public static final byte ALERT_LEVEL_NONE = 0x00;//NO
    public static final byte ALERT_LEVEL_MESSAGE = 0x01;//MILD
    public static final byte ALERT_LEVEL_PHONE_CALL = 0x02;//HIGH
    public static final byte ALERT_LEVEL_VIBRATE_ONLY = 0x03;

    // ==================== ALERT CATEGORIES =====================
    /**
     * General text alert or non-text alert
     */
    public static final byte ALERT_CATEGORY_SIMPLE_ALERT = 0x00;

    /**
     * Alert when Email messages arrives
     */
    public static final byte ALERT_CATEGORY_EMAIL = 0x01;

    /**
     * News feeds such as RSS, Atom
     */
    public static final byte ALERT_CATEGORY_NEWS = 0x02;

    /**
     * Incoming call
     */
    public static final byte ALERT_CATEGORY_CALL = 0x03;

    /**
     * Missed Call
     */
    public static final byte ALERT_CATEGORY_MISSED_CALL = 0x04;

    /**
     * SMS/MMS message arrives
     */
    public static final byte ALERT_CATEGORY_SMS_MMS = 0x05;

    /**
     * Voice mail
     */
    public static final byte ALERT_CATEGORY_VOICE_MAIL = 0x06;

    /**
     * Alert occurred on calendar, planner
     */
    public static final byte ALERT_CATEGORY_SCHEDULE = 0x07;

    /**
     * Alert that should be handled as high priority
     */
    public static final byte ALERT_CATEGORY_HIGHT_PRIORITIZED_ALERT = 0x08;

    /**
     * Alert for incoming instant messages
     */
    public static final byte ALERT_CATEGORY_INSTANT_MESSAGE = 0x09;
    // ========================== END ===========================

    // ==================== AUTHENTICATION =====================
    /**
     * Mi Band 2 authentication has tree steps.
     *  1. Send 'secret' key to the band. ({AUTH_SECRET_NUMBER, AUTH_BYTE, AUTH_SECRET_KEY})
     *  2. Request a random authentication key from the band. ({REQUEST_AUTH_RANDOM_NUMBER, AUTH_BYTE})
     *  3. Send the encrypted random authentication key to the band. ({AUTH_ENCRYPTED_NUMBER, AUTH_BYTE, encrypted authentication key})
     */

    public static final byte[] AUTH_SECRET_KEY = {0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45};
    public static final byte[] AUTH_SECRET_NUMBER = {0x01};
    public static final byte[] AUTH_BYTE = {0x8};
    public static final byte[] REQUEST_AUTH_RANDOM_NUMBER = {0x02};
    public static final byte[] REQUEST_AUTH_RANDOM_KEY = ByteArray.merge(REQUEST_AUTH_RANDOM_NUMBER,AUTH_BYTE);
    public static final byte[] AUTH_ENCRYPTED_NUMBER = {0x03};

    /**
     * Received in response to any authentication request (byte 0 in the byte[] value)
     */
    public static final byte AUTH_RESPONSE = 0x10;

    /**
     * Successful authentication (byte 2 in the byte[] value)
     */
    public static final byte AUTH_SUCCESS = 0x01;

    /**
     * Failed authentication (byte 2 in the byte[] value)
     */
    public static final byte AUTH_FAIL = 0x04;
    // ========================= END ===========================
}

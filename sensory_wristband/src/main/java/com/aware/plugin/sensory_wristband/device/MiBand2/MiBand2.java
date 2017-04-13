package com.aware.plugin.sensory_wristband.device.MiBand2;

/**
 * Check: https://github.com/Freeyourgadget/Gadgetbridge
 */

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.aware.plugin.sensory_wristband.device.ActionCallback;
import com.aware.plugin.sensory_wristband.device.Band;
import com.aware.plugin.sensory_wristband.device.BatteryInfo;
import com.aware.plugin.sensory_wristband.device.BatteryNotifyListener;
import com.aware.plugin.sensory_wristband.device.HeartRateNotifyListener;
import com.aware.plugin.sensory_wristband.device.MiBand2.model.BatteryInfoMiBand2;
import com.aware.plugin.sensory_wristband.device.MiBand2.model.Profile;
import com.aware.plugin.sensory_wristband.device.MiBand2.model.Protocol;
import com.aware.plugin.sensory_wristband.device.MiBand2.model.StepsInfo;
import com.aware.plugin.sensory_wristband.device.NotifyListener;
import com.aware.plugin.sensory_wristband.device.RealtimeStepsNotifyListener;
import com.aware.plugin.sensory_wristband.utils.ByteArray;

public class MiBand2 implements Band {

    private static final String TAG = MiBand2.class.getSimpleName();
    private static final String supportedDeviceName = "MI Band 2";

    private Context context;
    private BluetoothIO io;

    public MiBand2(Context context) {
        this.context = context;
        this.io = new BluetoothIO();
    }

    /*==================== Device ====================*/
    @Override
    public boolean supports(BluetoothDevice device) {
        return device != null && !device.getName().isEmpty()&& supportedDeviceName.equalsIgnoreCase(device.getName());
    }

    @Override
    public BluetoothDevice getDevice() {
        return io.getDevice();
    }
    /*==================== Device END ====================*/

    /*==================== Connect ====================*/
    @Override
    public void connect(BluetoothDevice device, final ActionCallback callback) {
        this.io.connect(context, device, callback);
    }

    @Override
    public void disconnect(){
        io.disconnect();
        io.close();
    }

    @Override
    public void setDisconnectedListener(NotifyListener disconnectedListener) {
        io.setDisconnectedListener(disconnectedListener);
    }
    /*==================== Connect END ====================*/

    /*==================== Pair/Authenticate ====================*/
    @Override
    public void pair(final ActionCallback callback) {
        //After successful descriptor write start authentication
        io.setAuthenticationListener(data -> {
            if (data == null){
                //Step ONE
                byte[] sendKey = ByteArray.merge(Protocol.AUTH_SECRET_NUMBER, Protocol.AUTH_BYTE, Protocol.AUTH_SECRET_KEY);
                io.writeCharacteristic(Profile.UUID_CHARACTERISTIC_AUTH, sendKey);
            }
        });

        //Set notification for authentication
        io.setNotifyListener(Profile.UUID_SERVICE_MIBAND2,Profile.UUID_CHARACTERISTIC_AUTH, data -> {
            if (data[0] == Protocol.AUTH_RESPONSE && data[1] == Protocol.AUTH_SECRET_NUMBER[0] && data[2] == Protocol.AUTH_SUCCESS) {
                //Step TWO
                io.writeCharacteristic(Profile.UUID_CHARACTERISTIC_AUTH, Protocol.REQUEST_AUTH_RANDOM_KEY);
            } else if (data[0] == Protocol.AUTH_RESPONSE && data[1] == Protocol.REQUEST_AUTH_RANDOM_NUMBER[0] && data[2] == Protocol.AUTH_SUCCESS) {
                //Step THREE
                byte[] encryptedKey = ByteArray.merge(Protocol.AUTH_ENCRYPTED_NUMBER, Protocol.AUTH_BYTE, encryptKey(data));
                io.writeCharacteristic(Profile.UUID_CHARACTERISTIC_AUTH, encryptedKey);
            } else if (data[0] == Protocol.AUTH_RESPONSE && data[1] == Protocol.AUTH_ENCRYPTED_NUMBER[0] && data[2] == Protocol.AUTH_SUCCESS) {
                callback.onSuccess(null);
            } else if (data[0] == Protocol.AUTH_RESPONSE && data[2] == Protocol.AUTH_FAIL) {
                Log.d("Authentication","Authentication failed. Try again.");
                callback.onFail(-2, "Authentication failed");
            } else {
                Log.d("Authentication","Something is wrong. Incorrect response.");
                callback.onFail(-1, "Unhandled response from the device");
            }
        });
    }

    /**
     * Encrypt key for third step of authentication.
     * @param response - response array after second authentication step
     * @return encrypted byte array
     */
    private byte[] encryptKey(byte[] response){
        byte[] encryptedKey = {};
        byte[] input = Arrays.copyOfRange(response,3,19);
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKeySpec newKey = new SecretKeySpec(Protocol.AUTH_SECRET_KEY,"AES");
            cipher.init(Cipher.ENCRYPT_MODE, newKey);
            encryptedKey = cipher.doFinal(input);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            Log.e("encryptKey",e.getMessage());
        }
        return encryptedKey;
    }
    /*==================== Pair/Authenticate END ====================*/

    /*==================== RSSI ====================*/
    @Override
    public void getSignalStrength(ActionCallback callback) {
        io.readRssi(callback);
    }
    /*==================== RSSI END ====================*/

    /*==================== Generic Access ====================*/
    @Override
    public void getDeviceName(ActionCallback callback){
        io.readCharacteristic(Profile.UUID_SERVICE_GENERIC_ACCESS, Profile.UUID_CHAR_DEVICE_NAME, callback);
    }

    @Override
    public void getAppearance(ActionCallback callback){
        io.readCharacteristic(Profile.UUID_SERVICE_GENERIC_ACCESS, Profile.UUID_CHAR_APPEARANCE, callback);
    }

    @Override
    public void getPeripheralPreferredParameters(ActionCallback callback){
        io.readCharacteristic(Profile.UUID_SERVICE_GENERIC_ACCESS, Profile.UUID_CHAR_PERIPHERAL_PREFERRED_PARAMETERS, callback);
    }
    /*==================== Generic Access END ====================*/

    /*==================== Generic Attribute ====================*/
    @Override
    public void setServiceChangedIndicate(){
        //Profile.UUID_SERVICE_GENERIC_ATTRIBUTE
        //Profile.UUID_CHAR_SERVICE_CHANGED
    }
    /*==================== Generic Attribute END ====================*/

    /*==================== Device Information ====================*/
    @Override
    public void getSerialNumber(ActionCallback callback){
        io.readCharacteristic(Profile.UUID_SERVICE_DEVICE_INFORMATION, Profile.UUID_CHAR_SERIAL_NUMBER_STRING, callback);
    }

    @Override
    public void getHardwareRevision(ActionCallback callback){
        io.readCharacteristic(Profile.UUID_SERVICE_DEVICE_INFORMATION, Profile.UUID_CHAR_HARDWARE_REVISION_STRING, callback);
    }

    @Override
    public void getSoftwareRevision(ActionCallback callback){
        io.readCharacteristic(Profile.UUID_SERVICE_DEVICE_INFORMATION, Profile.UUID_CHAR_SOFTWARE_REVISION_STRING, callback);
    }

    @Override
    public void getSystemID(ActionCallback callback){
        io.readCharacteristic(Profile.UUID_SERVICE_DEVICE_INFORMATION, Profile.UUID_CHAR_SYSTEM_ID, callback);
    }

    @Override
    public void getPnPID(ActionCallback callback){
        io.readCharacteristic(Profile.UUID_SERVICE_DEVICE_INFORMATION, Profile.UUID_CHAR_PNP_ID, callback);
    }

    @Override
    public void getDeviceInformation(ActionCallback callback){
        final List<String> information = new ArrayList<>(5);
        getSerialNumber(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                String info = "Serial Number" + ": " + data;
                information.add(info);
                Log.d(TAG,info);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.d(TAG, "Could not get device information. " + errorCode + " :: " + msg);
            }
        });
        getHardwareRevision(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                String info = "Hardware Revision" + ": " + data;
                information.add(info);
                Log.d(TAG,info);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.d(TAG, "Could not get device information. " + errorCode + " :: " + msg);
            }
        });
        getSoftwareRevision(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                String info = "Software Revision" + ": " + data;
                information.add(info);
                Log.d(TAG,info);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.d(TAG, "Could not get device information. " + errorCode + " :: " + msg);
            }
        });
        getSystemID(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                String info = "System ID" + ": " + data;
                information.add(info);
                Log.d(TAG,info);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.d(TAG, "Could not get device information. " + errorCode + " :: " + msg);
            }
        });
        getPnPID(new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                String info = "PnP ID" + ": " + data;
                information.add(info);
                Log.d(TAG,info);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.d(TAG, "Could not get device information. " + errorCode + " :: " + msg);
            }
        });
        callback.onSuccess(information);
    }
    /*==================== Device Information END ====================*/

    /*==================== Battery Information ====================*/
    @Override
    public void setBatteryInfoListener(final BatteryNotifyListener listener) {
        io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_BATTERY, data -> {
            BatteryInfo batteryInfo = BatteryInfoMiBand2.fromByteData(data);
            if (batteryInfo != null){
                Log.d(TAG, batteryInfo.toString());
                listener.onNotify(batteryInfo);
            } else {
                Log.d(TAG, "BatteryInfo data is incorrect");
            }
        });
    }
    /*==================== Battery Information END ====================*/

    /*==================== User Information ====================*/
    @Override
    public void setUserInfo(int uid, int gender, int age, int height, int weight, String alias, int type) {

    }
    /*==================== User Information END ====================*/

    /*==================== Services and Characteristics ====================*/
    @Override
    public void showServicesAndCharacteristics() {
        for (BluetoothGattService service : io.gatt.getServices()) {
            Log.d(TAG, "onServicesDiscovered:" + service.getUuid());
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                Log.d(TAG, "  char:" + characteristic.getUuid());
                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    Log.d(TAG, "    descriptor:" + descriptor.getUuid());
                }
            }
        }
    }
    /*==================== Services and Characteristics END ====================*/

    /*==================== Immediate Alert ====================*/
    @Override
    public void immediateAlert(byte alertLevel){
        byte[] level = {alertLevel};
        io.writeCharacteristic(Profile.UUID_SERVICE_IMMEDIATE_ALERT, Profile.UUID_CHAR_ALERT_LEVEL, level, null);
    }

    @Override
    public void stopImmediateAlert(){
        byte[] level = {Protocol.ALERT_LEVEL_NONE};
        io.writeCharacteristic(Profile.UUID_SERVICE_IMMEDIATE_ALERT, Profile.UUID_CHAR_ALERT_LEVEL, level, null);
    }
    /*==================== Immediate Alert END ====================*/

    /*==================== Normal Notification ====================*/
    @Override
    public void setNormalNotifyListener(NotifyListener listener) {

    }

    @Override
    public void removeNormalNotifyListener(){

    }
    /*==================== Normal Notification END ====================*/

    /*==================== Sensor Data ====================*/
    @Override
    public void setSensorDataNotifyListener(final NotifyListener listener) {

    }

    @Override
    public void removeSensorDataNotifyListener(){

    }

    @Override
    public void enableSensorDataNotify() {

    }

    @Override
    public void disableSensorDataNotify() {

    }
    /*==================== Sensor Data END ====================*/

    /*==================== Steps ====================*/
    @Override
    public void setRealtimeStepsNotifyListener(final RealtimeStepsNotifyListener listener) {
        io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_STEPS, data -> {
            StepsInfo stepsInfo = StepsInfo.fromByteData(data);
            if (stepsInfo != null){
                Log.d(TAG, stepsInfo.toString());
                listener.onNotify(stepsInfo);
            } else {
                Log.d(TAG, "StepsInfo data is incorrect");
            }
        });
    }

    @Override
    public void removeRealtimeStepsNotifyListener(){

    }

    @Override
    public void enableRealtimeStepsNotify() {

    }

    @Override
    public void disableRealtimeStepsNotify() {

    }
    /*==================== Steps END ====================*/

    /*==================== Heart Rate ====================*/
    @Override
    public void setHeartRateScanListener(final HeartRateNotifyListener listener) {
        //TODO - change heart rate calculation base on format of response
        io.setNotifyListener(Profile.UUID_SERVICE_HEART_RATE, Profile.UUID_CHAR_HEART_RATE_MEASUREMENT, data -> {
            Log.d(TAG, Arrays.toString(data));
            if (data.length == 2) { // && data[0] == 6
                int heartRate = data[1] & 0xFF;
                listener.onNotify(heartRate);
            }
        });
    }

    @Override
    public void removeHeartRateScanListener(){

    }

    @Override
    public void startHeartRateScan(byte heartRateMode) {
        io.writeCharacteristic(Profile.UUID_SERVICE_HEART_RATE, Profile.UUID_CHAR_HEART_RATE_CONTROL_POINT, new byte[]{Protocol.HEART_RATE_SCAN,heartRateMode,Protocol.ENABLE}, null);
    }

    @Override
    public void stopHeartRateScan(byte heartRateMode){
        io.writeCharacteristic(Profile.UUID_SERVICE_HEART_RATE, Profile.UUID_CHAR_HEART_RATE_CONTROL_POINT, new byte[]{Protocol.HEART_RATE_SCAN,heartRateMode,Protocol.DISABLE},null);
    }
    /*==================== Heart Rate END ====================*/

    /*==================== Button ====================*/
    @Override
    public void setButtonListener(final NotifyListener listener){
        io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_BUTTON, (data) -> {
            Log.d(TAG, "Button touched: " + Arrays.toString(data));
            listener.onNotify(data);
        });
    }
    /*==================== Button END ====================*/
}

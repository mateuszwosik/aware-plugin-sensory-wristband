package com.aware.plugin.sensory_wristband.device.MiBand2;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.aware.plugin.sensory_wristband.device.ActionCallback;
import com.aware.plugin.sensory_wristband.device.Band;
import com.aware.plugin.sensory_wristband.device.BatteryInfo;
import com.aware.plugin.sensory_wristband.device.BatteryNotifyListener;
import com.aware.plugin.sensory_wristband.device.HeartRateMode;
import com.aware.plugin.sensory_wristband.device.HeartRateNotifyListener;
import com.aware.plugin.sensory_wristband.device.MiBand2.model.BatteryInfoMiBand2;
import com.aware.plugin.sensory_wristband.device.MiBand2.model.Profile;
import com.aware.plugin.sensory_wristband.device.MiBand2.model.Protocol;
import com.aware.plugin.sensory_wristband.device.MiBand2.model.StepsInfoMiBand2;
import com.aware.plugin.sensory_wristband.device.NotifyListener;
import com.aware.plugin.sensory_wristband.device.StepsNotifyListener;
import com.aware.plugin.sensory_wristband.device.StepsInfo;
import com.aware.plugin.sensory_wristband.utils.ByteArray;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class MiBand2 implements Band {

    private static final String TAG = MiBand2.class.getSimpleName();
    private static final String supportedDeviceName = "MI Band 2";

    private Context context;
    private BluetoothIO io;

    public MiBand2(Context context) {
        this.context = context;
        this.io = new BluetoothIO();
    }

    @Override
    public boolean supports(BluetoothDevice device) {
        return device != null && !device.getName().isEmpty()&& supportedDeviceName.equalsIgnoreCase(device.getName());
    }

    @Override
    public BluetoothDevice getDevice() {
        return io.getDevice();
    }

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

    @Override
    public void getSignalStrength(ActionCallback callback) {
        io.readRssi(callback);
    }

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

    @Override
    public void setUserInfo(int uid, int gender, int age, int height, int weight, String alias, int type) {

    }

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

    @Override
    public void setStepsNotifyListener(final StepsNotifyListener listener) {
        io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_STEPS, data -> {
            StepsInfo stepsInfo = StepsInfoMiBand2.fromByteData(data);
            if (stepsInfo != null){
                Log.d(TAG, stepsInfo.toString());
                listener.onNotify(stepsInfo);
            } else {
                Log.d(TAG, "StepsInfoMiBand2 data is incorrect");
            }
        });
    }

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
    public void startHeartRateScan(byte heartRateMode) {
        io.writeCharacteristic(Profile.UUID_SERVICE_HEART_RATE, Profile.UUID_CHAR_HEART_RATE_CONTROL_POINT, new byte[]{Protocol.HEART_RATE_SCAN,heartRateMode,Protocol.ENABLE}, null);
    }

    @Override
    public void stopHeartRateScan(byte heartRateMode){
        io.writeCharacteristic(Profile.UUID_SERVICE_HEART_RATE, Profile.UUID_CHAR_HEART_RATE_CONTROL_POINT, new byte[]{Protocol.HEART_RATE_SCAN,heartRateMode,Protocol.DISABLE},null);
    }

    @Override
    public byte getHeartRateScanMode(HeartRateMode mode){
        switch (mode){
            case HEART_RATE_MANUAL_MODE:
                return Protocol.HEART_RATE_MANUAL_MODE;
            case HEART_RATE_CONTINUOUS_MODE:
                return Protocol.HEART_RATE_CONTINUOUS_MODE;
            case HEART_RATE_SLEEP_MODE:
                return Protocol.HEART_RATE_SLEEP_MODE;
            default:
                return Protocol.HEART_RATE_MANUAL_MODE;
        }
    }

    @Override
    public void setButtonListener(final NotifyListener listener){
        io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_BUTTON, data -> {
            Log.d(TAG, "Button touched: " + Arrays.toString(data));
            listener.onNotify(data);
        });
    }

}

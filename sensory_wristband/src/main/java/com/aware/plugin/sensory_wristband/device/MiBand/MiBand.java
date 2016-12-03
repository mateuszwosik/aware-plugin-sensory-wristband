package com.aware.plugin.sensory_wristband.device.MiBand;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.util.Log;

import java.util.Arrays;

import com.aware.plugin.sensory_wristband.device.ActionCallback;
import com.aware.plugin.sensory_wristband.device.Band;
import com.aware.plugin.sensory_wristband.device.HeartRateNotifyListener;
import com.aware.plugin.sensory_wristband.device.MiBand.model.BatteryInfo;
import com.aware.plugin.sensory_wristband.device.MiBand.model.LedColor;
import com.aware.plugin.sensory_wristband.device.MiBand.model.Profile;
import com.aware.plugin.sensory_wristband.device.MiBand.model.Protocol;
import com.aware.plugin.sensory_wristband.device.MiBand.model.UserInfo;
import com.aware.plugin.sensory_wristband.device.NotifyListener;
import com.aware.plugin.sensory_wristband.device.RealtimeStepsNotifyListener;

public class MiBand implements Band {

    private static final String TAG = MiBand.class.getSimpleName();
    private static final String supportedDeviceName = "MI Band";

    private Context context;
    private BluetoothIO io;

    public MiBand(Context context) {
        this.context = context;
        this.io = new BluetoothIO();
    }

    public static void startScan(ScanCallback callback) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (null == adapter) {
            Log.e(TAG, "BluetoothAdapter is null");
            return;
        }
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (null == scanner) {
            Log.e(TAG, "BluetoothLeScanner is null");
            return;
        }
        scanner.startScan(callback);
    }

    public static void stopScan(ScanCallback callback) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (null == adapter) {
            Log.e(TAG, "BluetoothAdapter is null");
            return;
        }
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (null == scanner) {
            Log.e(TAG, "BluetoothLeScanner is null");
            return;
        }
        scanner.stopScan(callback);
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
    public void setDisconnectedListener(NotifyListener disconnectedListener) {
        this.io.setDisconnectedListener(disconnectedListener);
    }

    @Override
    public void disconnect() {
        io.disconnect();
        io.close();
    }
    /*==================== Connect END ====================*/

    /*==================== Pair/Authenticate ====================*/
    @Override
    public void pair(final ActionCallback callback) {
        ActionCallback ioCallback = new ActionCallback() {

            @Override
            public void onSuccess(Object data) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                Log.d(TAG, "pair result " + Arrays.toString(characteristic.getValue()));
                if (characteristic.getValue().length == 1 && characteristic.getValue()[0] == 2) {
                    callback.onSuccess(null);
                } else {
                    callback.onFail(-1, "response values no succ!");
                }
            }

            @Override
            public void onFail(int errorCode, String msg) {
                callback.onFail(errorCode, msg);
            }
        };
        io.writeAndRead(Profile.UUID_CHAR_PAIR, Protocol.PAIR, ioCallback);
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
    public void getDeviceName(ActionCallback callback) {

    }

    @Override
    public void getAppearance(ActionCallback callback) {

    }

    @Override
    public void getPeripheralPreferredParameters(ActionCallback callback) {

    }
    /*==================== Generic Access END ====================*/

    /*==================== Generic Attribute ====================*/
    @Override
    public void setServiceChangedIndicate() {

    }
    /*==================== Generic Attribute END ====================*/

    /*==================== Device Information ====================*/
    @Override
    public void getSerialNumber(ActionCallback callback) {

    }

    @Override
    public void getHardwareRevision(ActionCallback callback) {

    }

    @Override
    public void getSoftwareRevision(ActionCallback callback) {

    }

    @Override
    public void getSystemID(ActionCallback callback) {

    }

    @Override
    public void getPnPID(ActionCallback callback) {

    }

    @Override
    public void getDeviceInformation(ActionCallback callback) {

    }
    /*==================== Device Information END ====================*/

    /*==================== Battery Information ====================*/
    @Override
    public void getBatteryInfo(final ActionCallback callback) {
        ActionCallback ioCallback = new ActionCallback() {

            @Override
            public void onSuccess(Object data) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                Log.d(TAG, "getBatteryInfo result " + Arrays.toString(characteristic.getValue()));
                if (characteristic.getValue().length == 10) {
                    BatteryInfo info = BatteryInfo.fromByteData(characteristic.getValue());
                    callback.onSuccess(info);
                } else {
                    callback.onFail(-1, "result format wrong!");
                }
            }

            @Override
            public void onFail(int errorCode, String msg) {
                callback.onFail(errorCode, msg);
            }
        };
        io.readCharacteristic(Profile.UUID_CHAR_BATTERY, ioCallback);
    }
    /*==================== Battery Information END ====================*/

    /*==================== User Information ====================*/
    @Override
    public void setUserInfo(int uid, int gender, int age, int height, int weight, String alias, int type) {
        BluetoothDevice device = io.getDevice();
        UserInfo userInfo = new UserInfo(uid, gender, age, height, weight, alias, type);
        byte[] data = userInfo.getBytes(device.getAddress());
        io.writeCharacteristic(Profile.UUID_CHAR_USER_INFO, data, null);
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
    public void immediateAlert(byte alertLevel) {
        byte[] protocol;
        switch (alertLevel) {
            case 0x01:
                protocol = Protocol.VIBRATION_WITH_LED;
                break;
            case 0x02:
                protocol = Protocol.VIBRATION_10_TIMES_WITH_LED;
                break;
            case 0x03:
                protocol = Protocol.VIBRATION_WITHOUT_LED;
                break;
            default:
                return;
        }
        io.writeCharacteristic(Profile.UUID_SERVICE_VIBRATION, Profile.UUID_CHAR_VIBRATION, protocol, null);

    }

    @Override
    public void stopImmediateAlert() {
        io.writeCharacteristic(Profile.UUID_SERVICE_VIBRATION, Profile.UUID_CHAR_VIBRATION, Protocol.STOP_VIBRATION, null);
    }
    /*==================== Immediate Alert END ====================*/

    /*==================== Normal Notification ====================*/
    @Override
    public void setNormalNotifyListener(NotifyListener listener) {
        io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_NOTIFICATION, listener);
    }

    @Override
    public void removeNormalNotifyListener() {

    }
    /*==================== Normal Notification END ====================*/

    /*==================== Sensor Data ====================*/
    @Override
    public void setSensorDataNotifyListener(final NotifyListener listener) {
        io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_SENSOR_DATA, new NotifyListener() {

            @Override
            public void onNotify(byte[] data) {
                listener.onNotify(data);
            }
        });
    }

    @Override
    public void removeSensorDataNotifyListener() {

    }

    @Override
    public void enableSensorDataNotify() {
        io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.ENABLE_SENSOR_DATA_NOTIFY, null);
    }

    @Override
    public void disableSensorDataNotify() {
        io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.DISABLE_SENSOR_DATA_NOTIFY, null);
    }
    /*==================== Sensor Data END ====================*/

    /*==================== Steps ====================*/
    @Override
    public void setRealtimeStepsNotifyListener(final RealtimeStepsNotifyListener listener) {
        io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_REALTIME_STEPS, new NotifyListener() {

            @Override
            public void onNotify(byte[] data) {
                Log.d(TAG, Arrays.toString(data));
                if (data.length == 4) {
                    int steps = data[3] << 24 | (data[2] & 0xFF) << 16 | (data[1] & 0xFF) << 8 | (data[0] & 0xFF);
                    listener.onNotify(steps);
                }
            }
        });
    }

    @Override
    public void removeRealtimeStepsNotifyListener() {

    }

    @Override
    public void enableRealtimeStepsNotify() {
        io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.ENABLE_REALTIME_STEPS_NOTIFY, null);
    }

    @Override
    public void disableRealtimeStepsNotify() {
        io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.DISABLE_REALTIME_STEPS_NOTIFY, null);
    }
    /*==================== Steps END ====================*/

    /*==================== Heart Rate ====================*/
    @Override
    public void setHeartRateScanListener(final HeartRateNotifyListener listener) {
        io.setNotifyListener(Profile.UUID_SERVICE_HEARTRATE, Profile.UUID_NOTIFICATION_HEARTRATE, new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                Log.d(TAG, Arrays.toString(data));
                if (data.length == 2 && data[0] == 6) {
                    int heartRate = data[1] & 0xFF;
                    listener.onNotify(heartRate);
                }
            }
        });
    }

    @Override
    public void removeHeartRateScanListener() {

    }

    @Override
    public void startHeartRateScan() {

        io.writeCharacteristic(Profile.UUID_SERVICE_HEARTRATE, Profile.UUID_CHAR_HEARTRATE, Protocol.START_HEART_RATE_SCAN, null);
    }

    @Override
    public void stopHeartRateScan() {

    }
    /*==================== Heart Rate END ====================*/

    /**
     * Set Led Color.
     * @param color - color
     */
    public void setLedColor(LedColor color) {
        byte[] protocal;
        switch (color) {
            case RED:
                protocal = Protocol.SET_COLOR_RED;
                break;
            case BLUE:
                protocal = Protocol.SET_COLOR_BLUE;
                break;
            case GREEN:
                protocal = Protocol.SET_COLOR_GREEN;
                break;
            case ORANGE:
                protocal = Protocol.SET_COLOR_ORANGE;
                break;
            default:
                return;
        }
        io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, protocal, null);
    }

}

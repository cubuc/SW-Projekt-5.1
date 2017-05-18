package kn.uni.inf.myapplication;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTING;
import static android.content.ContentValues.TAG;
import static kn.uni.inf.myapplication.TIUUIDs.UUID_ACC_DATA;
import static kn.uni.inf.myapplication.TIUUIDs.UUID_BAR_DATA;
import static kn.uni.inf.myapplication.TIUUIDs.UUID_GYR_DATA;
import static kn.uni.inf.myapplication.TIUUIDs.UUID_HUM_DATA;
import static kn.uni.inf.myapplication.TIUUIDs.UUID_IRT_DATA;
import static kn.uni.inf.myapplication.TIUUIDs.UUID_MAG_DATA;
import static kn.uni.inf.myapplication.TIUUIDs.UUID_OPT_DATA;

public class BluetoothLEService extends Service {
    private BluetoothGatt mGatt;
    private BluetoothAdapter mBtAdapter;
    private String mBtDeviceAddress;
    private int mConnectionState;
    private BluetoothManager mBtManager;

    public BluetoothLEService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
        super.onDestroy();
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBtManager == null) {
            mBtManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBtManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBtAdapter = mBtManager.getAdapter();
        if (mBtAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBtAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBtDeviceAddress != null && address.equals(mBtDeviceAddress)
                && mGatt != null) {
            Log.d(TAG, "Trying to use an existing mGatt for connection.");
            if (mGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBtDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBtAdapter == null || mGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mGatt.disconnect();
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());
            // TODO Intent
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            float[] data = null;
            switch (characteristic.getUuid().toString()) {
                case UUID_IRT_DATA:
                    data = Sensor.IR_TEMPERATURE.convert(characteristic.getValue());
                    break;
                case UUID_ACC_DATA:
                    data = Sensor.ACCELEROMETER.convert(characteristic.getValue());
                    break;
                case UUID_BAR_DATA:
                    data = Sensor.BAROMETER.convert(characteristic.getValue());
                    break;
                case UUID_GYR_DATA:
                    data = Sensor.GYROSCOPE.convert(characteristic.getValue());
                    break;
                case UUID_HUM_DATA:
                    data = Sensor.HUMIDITY.convert(characteristic.getValue());
                    break;
                case UUID_MAG_DATA:
                    data = Sensor.MAGNETOMETER.convert(characteristic.getValue());
                    break;
                case UUID_OPT_DATA:
                    data = Sensor.LUXMETER.convert(characteristic.getValue());
                    break;
                default:
                    Log.e("onCharacteristicsRead", "no valid uuid");
            }
            Log.i("onCharacteristicRead", Arrays.toString(data));
        }
    };
}

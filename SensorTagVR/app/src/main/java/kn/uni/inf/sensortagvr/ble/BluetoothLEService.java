package kn.uni.inf.sensortagvr.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTING;
import static android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED;
import static android.content.ContentValues.TAG;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_ACC_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_BAR_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_GYR_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_HUM_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_IRT_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_MAG_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_OPT_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.config;

public class BluetoothLEService extends Service {

    public final static String ACTION_GATT_CONNECTED =
            "kn.uni.inf.sensortagvr.ble.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "kn.uni.inf.sensortagvr.ble:ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "kn.uni.inf.sensortagvr.ble:ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "kn.uni.inf.sensortagvr.ble.ACTION_DATA_AVAILABLE";
    /* Data Contained in Intent.putExtra() when ACTION_DATA_AVAILABLE */
    public final static String EXTRA_SENSOR =
            "kn.uni.inf.sensortagvr.ble.EXTRA_SENSOR";
    public final static String EXTRA_DATA =
            "kn.uni.inf.sensortagvr.ble.EXTRA_DATA";
    private final IBinder mBinder = new LocalBinder();
    public boolean bound = false;


    LocalBroadcastManager mLocalBroadcastManager;
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    mLocalBroadcastManager.sendBroadcast(new Intent(ACTION_GATT_CONNECTED));
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    mLocalBroadcastManager.sendBroadcast(new Intent(ACTION_GATT_DISCONNECTED));
                    stopSelf();
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            ArrayList<BluetoothGattService> services = (ArrayList<BluetoothGattService>)
                    gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());
            mLocalBroadcastManager.sendBroadcast(new Intent(ACTION_GATT_SERVICES_DISCOVERED));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS)
                broadcastCharacteristic(characteristic);

            Log.i("onCharacteristicRead", Arrays.toString(characteristic.getValue()));
        }

        /**
         * Callback triggered as a result of a remote characteristic notification.
         *
         * @param gatt           GATT client the characteristic is associated with
         * @param characteristic Characteristic that has been updated as a result
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            broadcastCharacteristic(characteristic);
            Log.i("onCharacteristicChanged", "Sent ACTION_DATA_AVAILABLE");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
            intent.putExtra(EXTRA_SENSOR, "RSSI");
            intent.putExtra(EXTRA_DATA, new float[] {rssi,0,0});
            mLocalBroadcastManager.sendBroadcast(intent);
        }

    };
    private BluetoothGatt mGatt;
    private BluetoothAdapter mBtAdapter;
    private String mBtDeviceAddress;
    private int mConnectionState = STATE_DISCONNECTED;
    private BluetoothManager mBtManager;

    public BluetoothLEService() {
    }

    private void broadcastCharacteristic(BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
        switch (characteristic.getUuid().toString()) {
            /* IRT */
            case UUID_IRT_DATA:
                intent.putExtra(EXTRA_SENSOR, Sensor.IR_TEMPERATURE);
                intent.putExtra(EXTRA_DATA, Sensor.IR_TEMPERATURE.convert(characteristic.getValue()));
                break;
            case UUID_ACC_DATA:
                intent.putExtra(EXTRA_SENSOR, Sensor.ACCELEROMETER);
                intent.putExtra(EXTRA_DATA, Sensor.ACCELEROMETER.convert(characteristic.getValue()));
                break;
            case UUID_BAR_DATA:
                intent.putExtra(EXTRA_SENSOR, Sensor.BAROMETER);
                intent.putExtra(EXTRA_DATA, Sensor.BAROMETER.convert(characteristic.getValue()));
                break;
            case UUID_GYR_DATA:
                intent.putExtra(EXTRA_SENSOR, Sensor.GYROSCOPE);
                intent.putExtra(EXTRA_DATA, Sensor.GYROSCOPE.convert(characteristic.getValue()));
                break;
            case UUID_HUM_DATA:
                intent.putExtra(EXTRA_SENSOR, Sensor.HUMIDITY);
                intent.putExtra(EXTRA_DATA, Sensor.HUMIDITY.convert(characteristic.getValue()));
                break;
            case UUID_MAG_DATA:
                intent.putExtra(EXTRA_SENSOR, Sensor.MAGNETOMETER);
                intent.putExtra(EXTRA_DATA, Sensor.MAGNETOMETER.convert(characteristic.getValue()));
                break;
            case UUID_OPT_DATA:
                intent.putExtra(EXTRA_SENSOR, Sensor.LUXMETER);
                intent.putExtra(EXTRA_DATA, Sensor.LUXMETER.convert(characteristic.getValue()));
                break;
        }
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        bound = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (bound) {
            bound = false;
            return super.onUnbind(intent);
        }
        Log.e("TAG", "tried to unbind an unbound service");
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLocalBroadcastManager =
                LocalBroadcastManager.getInstance(this);
        mBtManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBtManager == null) {
            Log.e(TAG, "Unable to initialize BluetoothManager.");
        }
        mBtAdapter = mBtManager.getAdapter();
        /* Bluetooth enabled? If not enable */
        if (mBtAdapter == null) {
            Log.e(TAG, "No BluetoothAdapter detected");
        }

        if (!mBtAdapter.isEnabled()) {
            mBtAdapter.enable();
        }
/*        try {
            initThreadQueue();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        Log.i(TAG, "OnStart finished");
        return START_NOT_STICKY;
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
     * connects to the gatt server hosted on the bluetooth le device.
     *
     * @param address the device address of the destination device.
     * @return return true if the connection is initiated successfully. the connection result
     * is reported asynchronously through the
     * {@code bluetoothgattcallback#onconnectionstatechange(android.bluetooth.bluetoothgatt, int, int)}
     * callback.
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

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBtAdapter == null || mGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mGatt.readCharacteristic(characteristic);
    }

    public void enableSensor(BluetoothGattCharacteristic c) {
        if (config.contains(c.getUuid()))
            writeConfigCharacteristic(c, (byte) 0x01);
    }

    public void disableSensor(BluetoothGattCharacteristic c) {
        if (config.contains(c.getUuid()))
            writeConfigCharacteristic(c, (byte) 0x00);
    }

    public void writeConfigCharacteristic(
            BluetoothGattCharacteristic configCharacteristic, byte b) {
        byte[] val = new byte[1];
        val[0] = b;
        configCharacteristic.setValue(val);
        Log.i(TAG, "wrote Config Characteristic" + configCharacteristic.getService().toString());
        mGatt.writeCharacteristic(configCharacteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBtAdapter == null || mGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(
                UUID.fromString(TIUUIDs.UUID_CCC));
        Log.i(TAG, "got descriptor of "+ characteristic.getService().toString() + "named" + clientConfig.toString());
        if (enabled) {
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        mGatt.writeDescriptor(clientConfig);
        Log.i("SetCharacteristicNotify", "wrote Descriptor");

    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mGatt == null) return null;

        return mGatt.getServices();
    }

    class LocalBinder extends Binder {
        BluetoothLEService getService() {
            return BluetoothLEService.this;
        }
    }

}

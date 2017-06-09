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
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_ACC_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_BAR_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_GYR_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_HUM_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_IRT_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_MAG_DATA;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.UUID_OPT_DATA;

/**
 *
 */
public class BluetoothLEService extends Service {
    /* Broadcast intent actions that are sent;  */
    public final static String ACTION_GATT_CONNECTED =
            "kn.uni.inf.sensortagvr.ble.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "kn.uni.inf.sensortagvr.ble:ACTION_GATT_DISCONNECTED";
    public final static String ACTION_DATA_AVAILABLE =
            "kn.uni.inf.sensortagvr.ble.ACTION_DATA_AVAILABLE";
    /* Data Contained in Intent.putExtra() when ACTION_DATA_AVAILABLE */
    public final static String EXTRA_SENSOR =
            "kn.uni.inf.sensortagvr.ble.EXTRA_SENSOR";
    public final static String EXTRA_DATA =
            "kn.uni.inf.sensortagvr.ble.EXTRA_DATA";
    private final String TAG = "BluetoothLEService";
    private final IBinder mBinder = new LocalBinder();
    public boolean bound = false;
    public boolean isWriting = false;
    ArrayList<Sensor> enabledSensors = new ArrayList<>();
    ArrayList<Sensor> notifyingSensors = new ArrayList<>();
    LocalBroadcastManager mLocalBroadcastManager;
    private BluetoothGatt mGatt;
    private BluetoothAdapter mBtAdapter;
    private ConcurrentLinkedQueue<Object> mRWQueue = new ConcurrentLinkedQueue<>();
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /**
         * @param gatt The (dis-)connected GATT Server
         * @param status The status of the GATT Server according to BluetoothGatt.Status
         * @param newState The new State of the GATT Server. May be Connected or Disconnected,
         * all other states will cause an illegal state connection.
         */
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
                    throw new IllegalStateException();
            }

        }

        /**
         * Saves the Services of the GATT Server in an ArrayList after the gatt.discoverServices()
         *  method finished and broadcasts that the services were discovered.
         *
         * @param gatt The GATT Server of that who's services has been discovered
         * @param status The status of the GATT Server according to BluetoothGatt.Status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i("onServicesDiscovered", "");
            String mDeviceName = mGatt.getDevice().getName();
            if (mDeviceName != null && ((mDeviceName.equals("SensorTag2")) ||
                    (mDeviceName.equals("CC2650 SensorTag")))) {
                for (Sensor s : Sensor.SENSOR_LIST)
                    controlSensor(s, true, true);
            }
        }

        /**
         *
         * @param gatt GATT server the characteristic is associated with
         * @param characteristic the characteristic (e.g. sensor value, setting, ...) that was read.
         * @param status the status of the GATT server
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS)
                broadcastCharacteristic(characteristic);

            Log.i("onCharacteristicRead", Arrays.toString(characteristic.getValue()));
            nextWrite();
        }

        /**
         * Callback triggered as a result of a notification.
         *
         * @param gatt GATT server the characteristic is associated with
         * @param characteristic Characteristic that has been updated as a result
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            broadcastCharacteristic(characteristic);
            Log.i("onCharacteristicChanged", "Sent ACTION_DATA_AVAILABLE");
        }


        /**
         *
         * @param gatt GATT server the RSSI is associated with
         * @param rssi signal strength indicator
         * @param status status of the gatt server
         */
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
            intent.putExtra(EXTRA_SENSOR, "RSSI");
            intent.putExtra(EXTRA_DATA, new float[] {rssi,0,0});
            mLocalBroadcastManager.sendBroadcast(intent);
        }

        /**
         * @param gatt GATT server the characteristic is associated with
         * @param characteristic the characteristic that was written
         * @param status status of the gatt server
         */
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic, int status) {
            Log.v(TAG, "onCharacteristicWrite: " + status);
            isWriting = false;
            nextWrite();
        }

        /**
         * @param gatt GATT server the descriptor is associated with
         * @param descriptor the descriptor that was written
         * @param status status of the gatt server
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            Log.v(TAG, "onDescriptorWrite: " + status);
            isWriting = false;
            nextWrite();
        }

    };

    /**
     *  Empty constructor required because we're androids.
     */
    public BluetoothLEService() {
    }

    /**
     * Parses the raw data and sends broadcasts dependent on the type of sensor.
     *
     * @param characteristic The characteristic that has changed it's value or was read.
     */
    private void broadcastCharacteristic(BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
        intent.setClassName("kn.uni.inf.sensortagvr.stor", "StorageMainService");
        switch (characteristic.getUuid().toString()) {
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
                startService(intent);
                break;
            default:
                Log.i(TAG, "no valid data characteristic");
        }
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    /**
     *
     * @param intent The intent that the service was started with; explicitly:
     * bindService(new Intent(this, BluetoothLEService.class)
     */
    @Override
    public IBinder onBind(Intent intent) {
        bound = true;
        return mBinder;
    }

    /**
     * Called if the service is unbound (if unbindService(...) is called)
     * @param intent internally handled by Android
     */
    @Override
    public boolean onUnbind(Intent intent) {
        if (bound) {
            bound = false;
            return super.onUnbind(intent);
        }
        Log.e("TAG", "tried to unbind an unbound service");
        return false;
    }

    /**
     * Callback! Don't call it directly;
     * Initialization sequence
     *
     * params according to startService(...)
     * @param intent The intent this service was started with: (this, BluetoothLEService.class)
     * @param flags The flags this service was started with
     * @param startId ask the documentation, we actually don't care atm.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLocalBroadcastManager =
                LocalBroadcastManager.getInstance(this);
        BluetoothManager mBtManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBtManager != null) {
            mBtAdapter = mBtManager.getAdapter();
        } else {
            Log.e(TAG, "Unable to initialize BluetoothManager.");
        }

        if (mBtAdapter == null) {
            Log.e(TAG, "No BluetoothAdapter detected");
            stopSelf();
        }

        if (!mBtAdapter.isEnabled()) mBtAdapter.enable();

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
        for (Sensor s : Sensor.SENSOR_LIST)
            controlSensor(s, false, false);
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


        final BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
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
        mGatt.close();
    }

    /**
     * Read a BluetoothCharacteristic.
     *
     * @param characteristic Characteristic to read
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBtAdapter == null || mGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mGatt.readCharacteristic(characteristic);
    }

    /**
     * Read a sensor value, giving the sensor name as parameter instead of a characteristic.
     *
     * @param s A sensor, listed in the Sensor enum
     */
    public void readFromSensor(Sensor s) {
        if (mBtAdapter == null || mGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        BluetoothGattService sensorService = mGatt.getService(s.getServiceUUID());
        mGatt.readCharacteristic(sensorService.getCharacteristic(s.getDataUUID()));
    }

    /**
     * Basic sensor control: turn power and notifications on and off.
     *
     * @param s            Sensor enum: IR_TEMPERATURE || BAROMETER || LUXMETER || ...
     * @param power      true = on, false=off
     * @param notification true = enabled, false=off
     */
    public void controlSensor(Sensor s, boolean power, boolean notification) {
        if (Arrays.asList(Sensor.SENSOR_LIST).contains(s)) {

            byte[] val = new byte[1];
            val[0] = power ? s.getEnableSensorCode() : 0x00;
            BluetoothGattService sensorService = mGatt.getService(s.getServiceUUID());

            if (sensorService != null) {

                BluetoothGattCharacteristic sensorCharacteristic =
                        sensorService.getCharacteristic(s.getDataUUID());

                BluetoothGattCharacteristic sensorConf =
                        sensorService.getCharacteristic(s.getConfigUUID());

                if (sensorCharacteristic != null && sensorConf != null) {

                    BluetoothGattDescriptor config =
                            sensorCharacteristic.getDescriptor(UUID.fromString(TIUUIDs.UUID_CCC));

                    if (config != null) {

                        mGatt.setCharacteristicNotification(sensorCharacteristic, notification);
                        sensorConf.setValue(val);
                        write(sensorConf);


                        byte[] notify = notification ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;

                        config.setValue(notify);
                        write(config);

                        updateLists(s, power, notification);
                    }
                }
            }
        }
    }

    /**
     * @param s
     * @param power
     * @param notification
     */
    private void updateLists(Sensor s, boolean power, boolean notification) {
        if (power) {
            enabledSensors.add(s);
        } else {
            enabledSensors.remove(s);
            notifyingSensors.remove(s);
        }
        if (notification) {
            notifyingSensors.add(s);
        } else {
            notifyingSensors.remove(s);
        }
    }

    /**
     * Wrapper for a blocking write: Either execute if the Queue is empty and no write is currently
     * going on or enqueue the write task.
     *
     * @param o Either a BluetoothGATTCharacteristic (e.g. the config characteristic to turn the
     * sensor on and off) or a BluetoothGattDescriptor (e.g. the Client Characteristic
     * Config descriptor, to en-/disable the notifications).
     */
    private synchronized void write(Object o) {
        if (mRWQueue.isEmpty() && !isWriting) {
            doWrite(o);
        } else {
            mRWQueue.offer(o);
        }
    }

    /**
     * Function that is called if a write finishes or has illegal arguments
     */
    private synchronized void nextWrite() {
        if (!mRWQueue.isEmpty() && !isWriting) {
            doWrite(mRWQueue.poll());
        }
    }

    /**
     * Blocking write; Sets isWriting to true (onWriteCharacteristic/Descriptor set it to false when
     * finished)
     *
     * @param o Either a BluetoothGATTCharacteristic (e.g. the config characteristic to turn the
     * sensor on and off) or a BluetoothGattDescriptor (e.g. the Client Characteristic
     * Config descriptor, to en-/disable the notifications).
     */
    private synchronized void doWrite(Object o) {
        if (o instanceof BluetoothGattCharacteristic) {
            isWriting = true;
            mGatt.writeCharacteristic((BluetoothGattCharacteristic) o);
        } else if (o instanceof BluetoothGattDescriptor) {
            isWriting = true;
            mGatt.writeDescriptor((BluetoothGattDescriptor) o);
        } else {
            nextWrite();
        }
    }

    /**
     *
     */
    public ArrayList<Sensor> getEnabledSensors() {
        return enabledSensors;
    }

    /**
     *
     */
    public ArrayList<Sensor> getNotifyingSensors() {
        return notifyingSensors;
    }

    /**
     *
     */
    class LocalBinder extends Binder {
        /**
         * returns the reference to the service object
         */
        BluetoothLEService getService() {
            return BluetoothLEService.this;
        }
    }
}





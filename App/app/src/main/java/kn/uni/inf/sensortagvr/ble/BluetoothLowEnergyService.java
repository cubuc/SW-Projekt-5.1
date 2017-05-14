package kn.uni.inf.sensortagvr.ble;

import android.app.Service;
import static android.bluetooth.BluetoothProfile.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.*;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.util.List;
import java.util.UUID;

import static kn.uni.inf.sensortagvr.ble.TIUUIDs.*;
import static kn.uni.inf.sensortagvr.ble.Sensor.*;

public class BluetoothLowEnergyService extends Service {
    private final static String TAG = BluetoothLowEnergyService.class.getSimpleName();
   
    public BluetoothLowEnergyService() {
    }
    // TODO: Queueing & Threading, LocBroadcastListener, Parsing & Profiles
     /*  I/O   */

    /*
     *  Intent Codes
     */
    /* Requests */
    public final static String ACTION_START_SCAN =
            "kn.uni.inf.sensortagvr.ble.ACTION_START_SCAN";
    public final static String ACTION_DEVICE_FOUND =
            "kn.uni.inf.sensortagvr.ble.ACTION_DEVICE_FOUND";
    public final static String ACTION_DEVICE_CONNECT =
            "kn.uni.inf.sensortagvr.ble.ACTION_DEVICE_CONNECT";
    public final static String ACTION_DEVICE_DISCONNECT =
            "kn.uni.inf.sensortagvr.ble.ACTION_DEVICE_DISCONNECT";
    public final static String ACTION_SUBSCRIBE =
            "kn.uni.inf.sensortagvr.ble.ACTION_GET_DATA";
    public final static String ACTION_GET_CONFIG =
            "kn.uni.inf.sensortagvr.ble.ACTION_GET_CONFIG";
    public final static String WRITE_CONFIG =
            "kn.uni.inf.sensortagvr.ble.WRITE_CONFIG";
    public final static String ACTION_CALIBRATE =
            "kn.uni.inf.sensortagvr.ble.ACTION_CALIBRATE";

    /* Answers */
    public final static String ACTION_STATE_CHANGED =
            "kn.uni.inf.sensortagvr.ble.ACTION_STATE_CHANGED";
    public final static String ACTION_GATT_CONNECTED =
            "kn.uni.inf.sensortagvr.ble.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "kn.uni.inf.sensortagvr.ble:ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "kn.uni.inf.sensortagvr.ble:ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "kn.uni.inf.sensortagvr.ble.ACTION_DATA_AVAILABLE";

    /* Data Contained in Intent.putExtra() */
    public final static String EXTRA_SENSOR =
            "kn.uni.inf.sensortagvr.ble.EXTRA_SENSOR";
    public final static String EXTRA_DATA =
            "kn.uni.inf.sensortagvr.ble.EXTRA_DATA";
    public final static String EXTRA_ADDRESS =
            "kn.uni.inf.sensortagvr.ble.EXTRA_ADDRESS";
    public final static String EXTRA_STATUS =
            "kn.uni.inf.sensortagvr.ble.EXTRA_STATUS";



    LocalBroadcastManager mLocalBroadcastManager =
            LocalBroadcastManager.getInstance(this);
    private final IBinder binder = new LocalBinder();


    private void broadcastUpdate(String action, int rssi, int status) {
            final Intent intent = new Intent(action);
            intent.putExtra(EXTRA_SENSOR, "RSSI");
            intent.putExtra(EXTRA_DATA, rssi);
            intent.putExtra(EXTRA_STATUS, status);
            mLocalBroadcastManager.sendBroadcast(intent);
    }


    private void broadcastUpdate(String action,
                                 BluetoothGattCharacteristic characteristic,
                                 int status) {
        final Intent intent = new Intent(action);

        // TODO Parsing here w switch case which Sensor
        switch(characteristic.getUuid().toString()) {
            /* IRT */
            case "f000aa01-0451-4000-b000-000000000000":
                intent.putExtra(EXTRA_SENSOR, "IRT");
                intent.putExtra(EXTRA_DATA, Sensor.convert(characteristic.getValue()));
                intent.putExtra(EXTRA_STATUS, status);
                break;
        }
        intent.putExtra(EXTRA_DATA, characteristic.getValue());
        intent.putExtra(EXTRA_STATUS, status);
        mLocalBroadcastManager.sendBroadcast(intent);
    }



    private void broadcastUpdate(String action, String address, int status) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_ADDRESS, address);
        intent.putExtra(EXTRA_STATUS, status);
        mLocalBroadcastManager.sendBroadcast(intent);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return binder;
    }

    private class LocalBinder extends Binder {
        public BluetoothLowEnergyService getService() {
            return BluetoothLowEnergyService.this;
        }
    }

    /* Control flow */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mBtManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = mBtManager.getAdapter();
        /* Bluetooth enabled? If not enable */
        if (!mBtAdapter.isEnabled()) {
            mBtAdapter.enable();
        }
        //initThreadQueue();

        return START_NOT_STICKY;
    }

    // TODO
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /* Bluetooth Variable initialization */
    private static final long SCAN_PERIOD = 5000;
    BluetoothManager mBtManager;
    private BluetoothAdapter mBtAdapter;
    private BluetoothLeScanner mBleScanner;
    private BluetoothGatt mBtGatt = null;
    private boolean mScanning = false;
    private Handler mHandler;

    /*
     * BLE Scan
     */

    private void scanLeDevice(final boolean enable) {
        if (enable) {
             mBleScanner = mBtAdapter.getBluetoothLeScanner();
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBleScanner.stopScan(mScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBleScanner.startScan(mScanCallback);
        } else {
            mScanning = false;
            mBleScanner.stopScan(mScanCallback);
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();
            connectToDevice(btDevice);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private void connectToDevice(BluetoothDevice mBtDevice) {
        mBtGatt = mBtDevice.connectGatt(this, true, mGattCallback);
        mBtGatt.discoverServices();
    }

    /*
    * Connecting to Gatt server
    */

    private BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt,
                                            int status, int newState) {
            String intentAction;
            BluetoothDevice mBtDevice = gatt.getDevice();
            if (newState == STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction, mBtDevice.getAddress(), status);
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBtGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction, mBtDevice.getAddress(), status);
            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothDevice mBtDevice = gatt.getDevice();
            broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, mBtDevice.getAddress(),
                    status);
            /* Subscribe to the notifications */
            for ( BluetoothGattService mBtGattSvc : gatt.getServices() ) {
                if (mBtGattSvc != null) {
                    for (BluetoothGattCharacteristic ch : mBtGattSvc.getCharacteristics()) {
                        if (ch != null) {
                            gatt.setCharacteristicNotification(ch, true);
                            BluetoothGattDescriptor descriptor = ch.getDescriptor(UUID.fromString(UUID_CCC));
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                }
            }
        }

        // TODO
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic,0);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, rssi, status);
            }
        }
    };



   }
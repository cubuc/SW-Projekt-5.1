package kn.uni.inf.sensortagvr.ble;

import android.app.Service;
import static android.bluetooth.BluetoothProfile.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.*;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.util.LinkedList;
import java.util.List;




public class BluetoothLowEnergyService extends Service {
    private final static String TAG = BluetoothLowEnergyService.class.getSimpleName();
    /* Constants & Intent Codes */
    private static final long SCAN_PERIOD = 5000;


    public final static String ACTION_GATT_CONNECTED =
            "kn.uni.inf.sensortagvr.ble.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "kn.uni.inf.sensortagvr.ble:ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "kn.uni.inf.sensortagvr.ble:ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "kn.uni.inf.sensortagvr.ble.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "kn.uni.inf.sensortagvr.ble.EXTRA_DATA";
    public final static String EXTRA_ADDRESS =
            "kn.uni.inf.sensortagvr.ble.EXTRA_ADDRESS";
    public final static String EXTRA_STATUS =
            "kn.uni.inf.sensortagvr.ble.EXTRA_STATUS";
    public final static String EXTRA_UUID =
            "kn.uni.inf.sensortagvr.ble.EXTRA_UUID";

    /* Variable initialization */
    private final IBinder binder = new LocalBinder();
    LocalBroadcastManager lBcastManager =
            LocalBroadcastManager.getInstance(this);
    BluetoothManager mBtManager;
    private BluetoothAdapter mBtAdapter;
    private BluetoothLeScanner mBleScanner;
    private BluetoothGatt mBtGatt = null;
    private boolean mScanning = false;
    private Handler mHandler;
    private volatile LinkedList<bleRequest> blockQueue;
    private volatile LinkedList<bleRequest> nonBlockQueue;

    public BluetoothLowEnergyService() {
    }


    /* BLE Scan */
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



    /* Connecting to Gatt server */
    private void connectToDevice(BluetoothDevice mBtDevice) {
        mBtGatt = mBtDevice.connectGatt(this, true, mGattCallback);
        mBtGatt.discoverServices();
    }
    
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

        // TODO
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothDevice mBtDevice = gatt.getDevice();
            broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, mBtDevice.getAddress(),
                    status);
        }

        // TODO
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        // TODO
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        // TODO
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        // TODO
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }
    };

    // I/O

    private void broadcastUpdate(String action,
                                 BluetoothGattCharacteristic characteristic,
                                 int status) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
        intent.putExtra(EXTRA_DATA, characteristic.getValue());
        intent.putExtra(EXTRA_STATUS, status);
        lBcastManager.sendBroadcast(intent);
    }

    private void broadcastUpdate(String action, String address, int status) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_ADDRESS, address);
        intent.putExtra(EXTRA_STATUS, status);
        lBcastManager.sendBroadcast(intent);
    }

    /* Binding and control flow */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return binder;
    }

    public class LocalBinder extends Binder {
        public BluetoothLowEnergyService getService() {
            return BluetoothLowEnergyService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mBtManager =
        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
         mBtAdapter = mBtManager.getAdapter();
        /* Bluetooth enabled? If not enable */
        if (!mBtAdapter.isEnabled()) {
            mBtAdapter.enable();
        }
        initThreadQueue();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    /* (Multi-)Threadding-Queue */

    /**
     * All operations are blocking because we are using bluetooth connection
     * wr = write (for calibration purposes)
     * rd = read
     * ns = Notification set
     */
    public enum bleRequestOperation {
        wr,
        rd,
        ns,
    }

    public enum bleRequestStatus {
        not_queued,
        queued,
        processing,
        timeout,
        done,
        no_such_request,
        failed,
    }

    public class bleRequest {
        public int id;
        public BluetoothGattCharacteristic characteristic;
        public bleRequestOperation operation;
        public volatile bleRequestStatus status;
        public int timeout;
        public int curTimeout;
        public boolean notifyenable;
    }

    private void initThreadQueue(){

        nonBlockQueue = new LinkedList<bleRequest>();
        blockQueue = new LinkedList<bleRequest>();
        Thread queueThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    // TODO
                    executeQueue();
                    try {
                        Thread.sleep(0,100000);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        queueThread.start();
    }



}
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
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static kn.uni.inf.sensortagvr.ble.TIUUIDs.*;

public class BluetoothLowEnergyService extends Service {
    /*
     *  Intent Codes
     */
    /* Requests */
    // No extra Info
    public final static String ACTION_START_SCAN =
            "kn.uni.inf.sensortagvr.ble.ACTION_START_SCAN";
    // .putExtra(EXTRA_ADDRESS, from Intent received address);
    public final static String ACTION_DEVICE_CONNECT =
            "kn.uni.inf.sensortagvr.ble.ACTION_DEVICE_CONNECT";
    // TODO: Implement LeScan for API18, !Clean! Queueing & Threading, (Profiles)
     /*  I/O   */
    // .putExtra(EXTRA_ADDRESS, from Intent received address);
    public final static String ACTION_DEVICE_DISCONNECT =
            "kn.uni.inf.sensortagvr.ble.ACTION_DEVICE_DISCONNECT";
    // .putExtra(EXTRA_SENSOR, sensor);
    public final static String ACTION_GET_CONFIG =
            "kn.uni.inf.sensortagvr.ble.ACTION_GET_CONFIG";
    // .putExtra(EXTRA_SENSOR, sensor);
    public final static String WRITE_CONFIG =
            "kn.uni.inf.sensortagvr.ble.WRITE_CONFIG";
    // .putExtra(EXTRA_SENSOR, sensor);
    public final static String ACTION_CALIBRATE =
            "kn.uni.inf.sensortagvr.ble.ACTION_CALIBRATE";
    /* Answers */
    public final static String ACTION_SCAN_STARTED =
            "kn.uni.inf.sensortagvr.ble.ACTION_SCAN_STARTED";
    public final static String ACTION_DEVICE_FOUND =
            "kn.uni.inf.sensortagvr.ble.ACTION_DEVICE_FOUND";
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
    public final static String EXTRA_NAME =
            "kn.uni.inf.sensortagvr.ble.EXTRA_NAME";
    public final static String EXTRA_SERVICES =
            "kn.uni.inf.sensortagvr.ble.EXTRA_SERVICES";
    public final static String EXTRA_STATUS =
            "kn.uni.inf.sensortagvr.ble.EXTRA_STATUS";

    private final static String TAG = BluetoothLowEnergyService.class.getSimpleName();
    /* Bluetooth Variable initialization */
    private static final long SCAN_PERIOD = 5000;
    private final IBinder binder = new LocalBinder();
    LocalBroadcastManager mLocalBroadcastManager =
            LocalBroadcastManager.getInstance(this);
    BluetoothManager mBtManager;
    LinkedBlockingQueue<bleRequest> reqQueue;


    BroadcastReceiver scanAndConnectTest = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothLowEnergyService.ACTION_START_SCAN:
//                    bleRequest rq = new bleRequest();
//                    rq.op = bleReqOp.startScan;
//                    reqQueue.offer(rq);
                    scanLeDevice(true);
                    break;
                case BluetoothLowEnergyService.ACTION_DEVICE_CONNECT:
//                    bleRequest req = new bleRequest();
//                    req.address = intent.getStringExtra(EXTRA_ADDRESS);
//                    req.op = bleReqOp.connect;
//                    reqQueue.offer(req);
                    connect((BluetoothDevice) intent.getExtras().get("EXTRA_ADDRESS"));

                    break;
            }
        }
    };

    volatile bleRequest curRequest = null;
    int TIMEOUT = 150;
    private BluetoothAdapter mBtAdapter;
    private BluetoothLeScanner mBleScanner;


    /* Control flow */
    private BluetoothGatt mBtGatt = null;
    private boolean mScanning = false;
    private Handler mHandler = new Handler();

    private BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt,
                                                    int status, int newState) {
                    String intentAction;
                    BluetoothDevice mBtDevice = gatt.getDevice();
                    if (newState == STATE_CONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;
                        broadcastDevice(intentAction, mBtDevice.getAddress(), status);

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intentAction = ACTION_GATT_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                        broadcastDevice(intentAction, mBtDevice.getAddress(), status);
                    }
                }


                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    broadcastServices(ACTION_GATT_SERVICES_DISCOVERED, gatt.getServices());
                    bleRequest rq = new bleRequest();
                    rq.address = gatt.getDevice().getAddress();
                    rq.notify = true;
                    for (BluetoothGattService mBtGattSvc : gatt.getServices()) {
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


                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        // add gatt server
                        broadcastCharacteristic(ACTION_DATA_AVAILABLE, characteristic);
                        bleRequest charRead = new bleRequest();
                        charRead.address = gatt.getDevice().getAddress();
                        charRead.charac = characteristic;
                        charRead.op = bleReqOp.readCharac;
                        reqQueue.offer(charRead);
                    }
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    broadcastCharacteristic(ACTION_DATA_AVAILABLE, characteristic);
                }

                @Override
                public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                    final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
                    intent.putExtra(EXTRA_SENSOR, "RSSI");
                    intent.putExtra(EXTRA_DATA, rssi);
                    mLocalBroadcastManager.sendBroadcast(intent);
                }

            };

    public BluetoothLowEnergyService() {
    }

    private static IntentFilter makeFilter() {
        final IntentFilter fi = new IntentFilter();
        fi.addAction(BluetoothLowEnergyService.ACTION_START_SCAN);
        fi.addAction(BluetoothLowEnergyService.ACTION_DEVICE_CONNECT);
        return fi;
    }

    private void broadcastCharacteristic(String action,
                                         BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        switch (characteristic.getUuid().toString()) {
            /* IRT */
            case UUID_IRT_DATA:
                intent.putExtra(EXTRA_SENSOR, "IRT");
                intent.putExtra(EXTRA_DATA, Sensor.IR_TEMPERATURE.convert(characteristic.getValue()));
                break;
            case UUID_ACC_DATA:
                intent.putExtra(EXTRA_SENSOR, "ACC");
                intent.putExtra(EXTRA_DATA, Sensor.ACCELEROMETER.convert(characteristic.getValue()));
                break;
            case UUID_BAR_DATA:
                intent.putExtra(EXTRA_SENSOR, "BAR");
                intent.putExtra(EXTRA_DATA, Sensor.BAROMETER.convert(characteristic.getValue()));
                break;
            case UUID_GYR_DATA:
                intent.putExtra(EXTRA_SENSOR, "GYR");
                intent.putExtra(EXTRA_DATA, Sensor.GYROSCOPE.convert(characteristic.getValue()));
                break;
            case UUID_HUM_DATA:
                intent.putExtra(EXTRA_SENSOR, "HUM");
                intent.putExtra(EXTRA_DATA, Sensor.HUMIDITY.convert(characteristic.getValue()));
                break;
            case UUID_MAG_DATA:
                intent.putExtra(EXTRA_SENSOR, "MAG");
                intent.putExtra(EXTRA_DATA, Sensor.MAGNETOMETER.convert(characteristic.getValue()));
                break;
            case UUID_OPT_DATA:
                intent.putExtra(EXTRA_SENSOR, "OPT");
                intent.putExtra(EXTRA_DATA, Sensor.LUXMETER.convert(characteristic.getValue()));
                break;
        }

        mLocalBroadcastManager.sendBroadcast(intent);
    }

    private void broadcastDevice(String action, String address, int status) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_ADDRESS, address);
        intent.putExtra(EXTRA_STATUS, status);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    private void broadcastServices(String action, List<BluetoothGattService> services) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_SERVICES, (Serializable) services);
    }

    /*
     * BLE Scan
     */

    @Override
    public IBinder onBind(Intent intent) {

        return binder;
    }


    /*
    * Connecting to Gatt server
    */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLocalBroadcastManager.registerReceiver(scanAndConnectTest, makeFilter());
        mBtManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = mBtManager.getAdapter();
        /* Bluetooth enabled? If not enable */
        if (mBtAdapter == null) {
            Log.e(TAG, "No BluetoothAdapter detected");
        }

        if (!mBtAdapter.isEnabled()) {
            mBtAdapter.enable();
        }
        try {
            initThreadQueue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "OnStart finished");
        return START_NOT_STICKY;
    }

    // TODO
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void scanLeDevice(boolean enable) {
        final BluetoothLeScanner bluetoothLeScanner = mBtAdapter.getBluetoothLeScanner();

        final ScanCallback mLeScanCallback = new ScanCallback() {

            @Override
            public void onScanResult(int callbackType, ScanResult result) {

                super.onScanResult(callbackType, result);

                BluetoothDevice mBtDevice = result.getDevice();
                final Intent intent = new Intent(ACTION_DEVICE_FOUND);
                intent.putExtra(EXTRA_ADDRESS, mBtDevice);
                mLocalBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothLeScanner.stopScan(mLeScanCallback);

                }


            }, SCAN_PERIOD);

            mScanning = true;
            bluetoothLeScanner.startScan( mLeScanCallback);
        } else {
            mScanning = false;
            bluetoothLeScanner.stopScan(mLeScanCallback);
        }


        final Intent intent = new Intent(ACTION_SCAN_STARTED);
        mLocalBroadcastManager.sendBroadcast(intent);


    }

    private void connect(BluetoothDevice mBtDevice) {

        mBtGatt = mBtDevice.connectGatt(this, true, mGattCallback);
        Log.i(TAG, "Connected to GATT server.");
        mBtGatt.discoverServices();
    }

    private void disconnect(BluetoothDevice mBtDevice) {
        if (mBtAdapter == null) {
            Log.w(TAG, "disconnect: BluetoothAdapter not initialized");
            return;
        }
        int connectionState = mBtManager.getConnectionState(mBtDevice,
                BluetoothProfile.GATT);

        if (mBtGatt != null) {
            if (connectionState != BluetoothProfile.STATE_DISCONNECTED) {
                mBtGatt.disconnect();
                broadcastDevice(ACTION_GATT_DISCONNECTED, mBtDevice.getAddress(),
                        BluetoothProfile.STATE_DISCONNECTED);
            } else {
                Log.w(TAG, "Attempt to disconnect in state: " + connectionState);
            }
        }
    }

    private void initThreadQueue() {
        reqQueue = new LinkedBlockingQueue<>();
        Log.i(TAG, "init Blocking Queue");
        Thread queueThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    executeQueue();
                    try {
                        Thread.sleep(0, 100000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        queueThread.start();
    }

    private void executeQueue() {
        if (curRequest != null && curRequest.op != bleReqOp.startScan) {
            Log.d(TAG, "executeQueue, curBleRequest running");
            try {
                curRequest.curTimeout++;
                if (curRequest.curTimeout > TIMEOUT) {
                    curRequest.status = reqStatus.timeout;
                    curRequest = null;
                }
                Thread.sleep(10, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (reqQueue == null || reqQueue.size() == 0) {
            return;
        } else if (curRequest == null) {
            Log.d(TAG, "new ble request");
            if (reqQueue.size() > 0) {
                curRequest = reqQueue.peek();
                switch (curRequest.op) {

                    case startScan:
                        scanLeDevice(true);
                        break;

                    case connect:
                        connect(mBtAdapter.getRemoteDevice(curRequest.address));
                        break;

                    case disconnect:
                        disconnect(mBtAdapter.getRemoteDevice(curRequest.address));
                        break;

                    case readCharac:

                    case readConfig:

                    case writeConfig:

                    case notify:

                    /* Subscribe to the notifications */


                        break;

                    default:
                        Log.i(TAG, "No request in the queue");

                }
            }
        }
    }

    /*
    * Queueing & Threading
     */
    private enum bleReqOp {
        startScan,
        connect,
        disconnect,
        readCharac,
        readConfig,
        writeConfig,
        notify
    }

    private enum reqStatus {
        not_queued,
        queued,
        executing,
        done,
        timeout
    }

    private static class bleRequest {
        int id;
        String address;
        BluetoothGattCharacteristic charac;
        bleReqOp op;
        reqStatus status;
        int curTimeout;
        boolean notify;
    }

    private class LocalBinder extends Binder {
        public BluetoothLowEnergyService getService() {
            return BluetoothLowEnergyService.this;
        }
    }

}

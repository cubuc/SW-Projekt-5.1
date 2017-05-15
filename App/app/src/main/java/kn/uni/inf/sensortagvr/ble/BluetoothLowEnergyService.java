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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    // No extra Info
    public final static String ACTION_START_SCAN =
            "kn.uni.inf.sensortagvr.ble.ACTION_START_SCAN";
    // .putExtra(EXTRA_ADDRESS, from Intent received address);
    public final static String ACTION_DEVICE_CONNECT =
            "kn.uni.inf.sensortagvr.ble.ACTION_DEVICE_CONNECT";
    public final static String ACTION_DEVICE_DISCONNECT =
            "kn.uni.inf.sensortagvr.ble.ACTION_DEVICE_DISCONNECT";
    public final static String ACTION_GET_CONFIG =
            "kn.uni.inf.sensortagvr.ble.ACTION_GET_CONFIG";
    public final static String WRITE_CONFIG =
            "kn.uni.inf.sensortagvr.ble.WRITE_CONFIG";
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



    LocalBroadcastManager mLocalBroadcastManager =
            LocalBroadcastManager.getInstance(this);
    private final IBinder binder = new LocalBinder();




    private void broadcastCharacteristic(String action,
                                 BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // TODO Parsing here w switch case which Sensor
        switch(characteristic.getUuid().toString()) {
            /* IRT */
            case "f000aa01-0451-4000-b000-000000000000":
                intent.putExtra(EXTRA_SENSOR, "IRT");
                intent.putExtra(EXTRA_DATA, Sensor.convert(characteristic.getValue()));
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
        initThreadQueue();

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

    private void scanLeDevice() {
        if (!mScanning) {
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

            final Intent intent = new Intent(ACTION_SCAN_STARTED);
            mLocalBroadcastManager.sendBroadcast(intent);

        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();

            final Intent intent = new Intent(ACTION_DEVICE_FOUND);
            intent.putExtra(EXTRA_ADDRESS, btDevice.getAddress());
            intent.putExtra(EXTRA_NAME, btDevice.getName());
            mLocalBroadcastManager.sendBroadcast(intent);

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
                BluetoothDevice btDevice = sr.getDevice();

                final Intent intent = new Intent(ACTION_DEVICE_FOUND);
                intent.putExtra(EXTRA_ADDRESS, btDevice.getAddress());
                intent.putExtra(EXTRA_NAME, btDevice.getName());
                mLocalBroadcastManager.sendBroadcast(intent);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    /*
    * Connecting to Gatt server
    */

    private void connect(BluetoothDevice mBtDevice) {

        mBtGatt = mBtDevice.connectGatt(this, true, mGattCallback);
        Log.i(TAG, "Connected to GATT server.");
        broadcastDevice(ACTION_GATT_CONNECTED, mBtDevice.getAddress(),
                BluetoothProfile.STATE_CONNECTED);
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
            BluetoothDevice mBtDevice = gatt.getDevice();
            broadcastServices(ACTION_GATT_SERVICES_DISCOVERED, gatt.getServices());

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
                charRead.stat = bleRequestStat.not_queued;
                reqQueue.offer(charRead);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastCharacteristic(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status)  {
                final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
                intent.putExtra(EXTRA_SENSOR, "RSSI");
                intent.putExtra(EXTRA_DATA, rssi);
                mLocalBroadcastManager.sendBroadcast(intent);
            }

    };

    /*
    * Queueing & Threading
     */
    private static enum bleReqOp {
        startScan,
        connect,
        disconnect,
        calibrate,
        readCharac,
        readConfig,
        writeConfig,
        notify
    }

    public enum bleRequestStat {
        not_queued,
        queued,
        processing,
        timeout,
        done,
        failed,
    }

    private static class bleRequest {
        public int id;
        public String address;
        public BluetoothGattCharacteristic charac;
        public bleReqOp op;
        public volatile bleRequestStat stat;
        public boolean notify;
    }

    LinkedBlockingQueue<bleRequest> reqQueue;
    volatile bleRequest curRequest = null;

    private void initThreadQueue() {

        reqQueue = new LinkedBlockingQueue<>();

        Thread queueThread = new Thread() {
            @Override
            public void run() {
                while (true) {
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

    private void executeQueue(){
        if (curRequest == null) {
            bleRequest mbleReq = reqQueue.peek();
            switch (mbleReq.op){

                case startScan:
                    scanLeDevice();
                    break;

                case connect:
                    connect(mBtAdapter.getRemoteDevice(mbleReq.address));
                    break;

                case disconnect:
                    disconnect(mBtAdapter.getRemoteDevice(mbleReq.address));
                    break;

                case calibrate:
                    // TODO if Sensors connect properly, parsing is done, LocalBroadcastListeners instant.
                    break;

                case readCharac:

                case readConfig:

                case writeConfig:

                case notify:

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
                    break;

                default:
                    Log.e(TAG, "No vaild operation in request" + mbleReq.op);

            }
        }
    }

   }
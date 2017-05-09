package kn.uni.inf.sensortagvr.ble;

/* android.app */

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.LinkedList;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/* android.content */
/* android.os */
/* android.bluetoott */
/* java.util.concurrent */


public class BleService extends Service {
    // Intent action
    public final static String ACTION_GATT_CONNECTED = "com.example.ti.ble.common.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.ti.ble.common.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.ti.ble.common.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_READ = "com.example.ti.ble.common.ACTION_DATA_READ";
    public final static String ACTION_DATA_NOTIFY = "com.example.ti.ble.common.ACTION_DATA_NOTIFY";
    public final static String ACTION_DATA_WRITE = "com.example.ti.ble.common.ACTION_DATA_WRITE";
    public final static String EXTRA_DATA = "com.example.ti.ble.common.EXTRA_DATA";
    public final static String EXTRA_UUID = "com.example.ti.ble.common.EXTRA_UUID";
    public final static String EXTRA_STATUS = "com.example.ti.ble.common.EXTRA_STATUS";
    public final static String EXTRA_ADDRESS = "com.example.ti.ble.common.EXTRA_ADDRESS";
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static int GATT_TIMEOUT = 150;
    // WorkerThread - queuing
    static final String TAG = "BleService";
    private static BleService mThis = null;
    private final Lock lock = new ReentrantLock();
    private final IBinder binder = new LocalBinder();
    public Timer disconnectionTimer;
    // BLE
    private BluetoothManager mBluetoothManager = null;
    // private PreferenceWR mDevicePrefs = null;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothGatt mBluetoothGatt = null;
    private String mBluetoothDeviceAddress;
    private volatile boolean blocking = false;
    private volatile int lastGattStatus = 0; //Success
    private volatile bleRequest curBleRequest = null;
    private volatile LinkedList<bleRequest> procQueue;
    private volatile LinkedList<bleRequest> nonBlockQueue;

    public BleService() {
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        mThis = this;
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                // Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBtAdapter = mBluetoothManager.getAdapter();
        if (mBtAdapter == null) {
            // Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        procQueue = new LinkedList<bleRequest>();
        nonBlockQueue = new LinkedList<bleRequest>();


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
        return true;
    }

    /**
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     */
    public void close() {
        if (mBluetoothGatt != null) {
            // Log.i(TAG, "close");
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }




    /* ######### Set-Up ####### */

    private void executeQueue() {
        // Everything here is done on the queue
        lock.lock();
        if (curBleRequest != null) {
            Log.d(TAG, "executeQueue, curBleRequest running");
            try {
                curBleRequest.curTimeout++;
                if (curBleRequest.curTimeout > GATT_TIMEOUT) {
                    curBleRequest.status = bleRequestStatus.timeout;
                    curBleRequest = null;
                }
                Thread.sleep(10, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            lock.unlock();
            return;
        }
        if (procQueue == null) {
            lock.unlock();
            return;
        }
        if (procQueue.size() == 0) {
            lock.unlock();
            return;
        }
        bleRequest procReq = procQueue.removeFirst();

        switch (procReq.operation) {
            case read:
                //Read, do non blocking read
                break;
            case readBlocking:
                //Normal (blocking) read
                if (procReq.timeout == 0) {
                    procReq.timeout = GATT_TIMEOUT;
                }
                procReq.curTimeout = 0;
                curBleRequest = procReq;
                int stat = sendBlockingReadRequest(procReq);
                if (stat == -2) {
                    Log.d(TAG, "executeQueue rdBlocking: error, BLE was busy or device disconnected");
                    lock.unlock();
                    return;
                }
                break;
            case write:
                //Write, do non blocking write (Ex: OAD)
                nonBlockQueue.add(procReq);
                sendNonBlockingWriteRequest(procReq);
                break;
            case writeBlocking:
                //Normal (blocking) write
                if (procReq.timeout == 0) {
                    procReq.timeout = GATT_TIMEOUT;
                }
                curBleRequest = procReq;
                stat = sendBlockingWriteRequest(procReq);
                if (stat == -2) {
                    Log.d(TAG, "executeQueue wrBlocking: error, BLE was busy or device disconnected");
                    lock.unlock();
                    return;
                }
                break;
            case setNotifBlocking:
                if (procReq.timeout == 0) {
                    procReq.timeout = GATT_TIMEOUT;
                }
                curBleRequest = procReq;
                stat = sendBlockingNotifySetting(procReq);
                if (stat == -2) {
                    Log.d(TAG, "executeQueue nsBlocking: error, BLE was busy or device disconnected");
                    lock.unlock();
                    return;
                }
                break;
            default:
                break;

        }
        lock.unlock();
    }

    /* ######### GATT ####### */

    public void waitIdle(int timeout) {
        while (timeout-- > 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    /* ########### Queuing ########## */

    public boolean checkGatt() {
        if (mBtAdapter == null) {
            // Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        if (mBluetoothGatt == null) {
            // Log.w(TAG, "BluetoothGatt not initialized");
            return false;
        }
        if (this.blocking) {
            Log.d(TAG, "Cannot start operation : Blocked");
            return false;
        }
        return true;

    }

    public int sendNonBlockingWriteRequest(bleRequest request) {
        request.status = bleRequestStatus.processing;
        if (!checkGatt()) {
            request.status = bleRequestStatus.failed;
            return -2;
        }
        mBluetoothGatt.writeCharacteristic(request.characteristic);
        return 0;
    }

    public int sendBlockingWriteRequest(bleRequest request) {
        request.status = bleRequestStatus.processing;
        int timeout = 0;
        if (!checkGatt()) {
            request.status = bleRequestStatus.failed;
            return -2;
        }
        mBluetoothGatt.writeCharacteristic(request.characteristic);
        this.blocking = true; // Set read to be blocking
        while (this.blocking) {
            timeout++;
            waitIdle(1);
            if (timeout > GATT_TIMEOUT) {
                this.blocking = false;
                request.status = bleRequestStatus.timeout;
                return -1;
            }  //Read failed TODO: Fix this to follow connection interval !
        }
        request.status = bleRequestStatus.done;
        return lastGattStatus;
    }

    public int sendBlockingReadRequest(bleRequest request) {
        request.status = bleRequestStatus.processing;
        int timeout = 0;
        if (!checkGatt()) {
            request.status = bleRequestStatus.failed;
            return -2;
        }
        mBluetoothGatt.readCharacteristic(request.characteristic);
        this.blocking = true; // Set read to be blocking
        while (this.blocking) {
            timeout++;
            waitIdle(1);
            if (timeout > GATT_TIMEOUT) {
                this.blocking = false;
                request.status = bleRequestStatus.timeout;
                return -1;
            }  //Read failed TODO: Fix this to follow connection interval !
        }
        request.status = bleRequestStatus.done;
        return lastGattStatus;
    }

    public int sendBlockingNotifySetting(bleRequest request) {
        request.status = bleRequestStatus.processing;
        int timeout = 0;
        if (request.characteristic == null) {
            return -1;
        }
        if (!checkGatt())
            return -2;

        if (mBluetoothGatt.setCharacteristicNotification(request.characteristic, request.notifyenable)) {

            BluetoothGattDescriptor clientConfig = request.characteristic
                    .getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
            if (clientConfig != null) {

                if (request.notifyenable) {
                    // Log.i(TAG, "Enable notification: " +
                    // characteristic.getUuid().toString());
                    clientConfig
                            .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                } else {
                    // Log.i(TAG, "Disable notification: " +
                    // characteristic.getUuid().toString());
                    clientConfig
                            .setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                }
                mBluetoothGatt.writeDescriptor(clientConfig);
                // Log.i(TAG, "writeDescriptor: " +
                // characteristic.getUuid().toString());
                this.blocking = true; // Set read to be blocking
                while (this.blocking) {
                    timeout++;
                    waitIdle(1);
                    if (timeout > GATT_TIMEOUT) {
                        this.blocking = false;
                        request.status = bleRequestStatus.timeout;
                        return -1;
                    }  //Read failed TODO: Fix this to follow connection interval !
                }
                request.status = bleRequestStatus.done;
                return lastGattStatus;
            }
        }
        return -3; // Set notification to android was wrong ...
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that
        // BluetoothGatt.close() is called
        // such that resources are cleaned up properly. In this particular example,
        // close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private enum bleRequestOperation {
        writeBlocking,
        write,
        readBlocking,
        read,
        setNotifBlocking,
    }

    private enum bleRequestStatus {
        not_queued,
        queued,
        processing,
        timeout,
        done,
        no_such_request,
        failed,
    }

    private class bleRequest {
        public int id;
        public BluetoothGattCharacteristic characteristic;
        public bleRequestOperation operation;
        public volatile bleRequestStatus status;
        public int timeout;
        public int curTimeout;
        public boolean notifyenable;
    }

    /* ############ Binding ########### */
    private class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

}
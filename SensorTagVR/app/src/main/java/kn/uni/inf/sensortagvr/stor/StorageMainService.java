package kn.uni.inf.sensortagvr.stor;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import java.io.File;

import kn.uni.inf.sensortagvr.tracking.TrackingManagerService;


/**
 * Main Service that should get started with the App. It registers a broadcastReceiver for Sensor
 * Data.
 * <p>
 * TODO
 * <p>
 * Created by gero on 16.05.17.
 */

public class StorageMainService extends IntentService {

    // From the bleSensor actions
    public final static String ACTION_DATA_AVAILABLE =
            "kn.uni.inf.sensortagvr.ble.ACTION_DATA_AVAILABLE";
    // Action performed by the GUI. May be changed.
    public final static String ACTION_MEASURE_DATA =
            "kn.uni.inf.sensortagvr.stor.ACTION_MEASURE_DATA";

    // Create a custom broadcast receiver for the bluetooth broadcast
    public final StorageBroadcastReceiver bleReceiver  = new StorageBroadcastReceiver(StorageMainService.this);;

    // Creates a Binder, look at the onBind() method for more information
    private final IBinder binder = new StorageBinder();

    // Copied from the android dev guide for bound services
    TrackingManagerService mService;
    boolean mBound = false;

    /**
     * stores data received from the bleReceiver
     */
    private Intent lastReceivedData;

    // Create a path to the public directory of the app. Thereby the webVR process can access it.
    private File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);;


    public StorageMainService() {
        super("StorageMainService");
    }

    /**
     * On Creating the service, the broadcast receiver will registered with an proper intent filter
     * The TrackingManagerService gets bound to this service, thus the getCurrentLocation() can be
     * accessed.
     */
    @Override
    public void onCreate() {
        //Create and fill a Intent Filter to only receive data from the ble-broadcast
        // TODO filter in xml file
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_DATA_AVAILABLE);

        // Register the bleReceiver
        this.registerReceiver(bleReceiver, intentFilter);

        Intent test = new Intent(this, TrackingManagerService.class);
        bindService(test, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onHandleIntent(Intent intent) {

        // When started by StorageBroadcastReceiver
        if (intent.getAction() == ACTION_DATA_AVAILABLE)
            this.lastReceivedData = intent;

            // When started by GUI
        else if (intent.getAction() == ACTION_MEASURE_DATA) {
            // get data from 'lastReceived'
            //      Uri data = lastReceivedData.getData();

            // get Data from tracking module
            Location loc = mService.getCurrentPosition();

            // create new StorageDataSet
            //      StorageDataSet.createSet(xPos, yPos, data);

            // save the data set in the .json-file
        }
    }

    /**
     * Unregisters the bleReceiver and unbind trackingmngrService when the Service gets DESTROYED
     */
    @Override
    public void onDestroy() {
        this.unregisterReceiver(bleReceiver);

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

    }

    /**
     * the service is intended to be bound at the main activity or the recordActivity
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     *
     * For more detailed info look at  https://developer.android.com/guide/components/bound-services.html
     */
    public class StorageBinder extends Binder {
        StorageMainService getService() {
            return StorageMainService.this;
        }
    }


    // Copied from the android dev guide for bound services
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            TrackingManagerService.TrackingBinder binder = (TrackingManagerService.TrackingBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

}


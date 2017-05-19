package kn.uni.inf.sensortagvr.stor;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;


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
    public BroadcastReceiver bleReceiver;
    private Intent lastReceivedData;


    public StorageMainService() {
        super("StorageMainService");
    }

    /**
     * Register the receiver for sensor data
     */
    @Override
    public void onCreate() {
        //register a broadcast receiver for the bluetooth data
        bleReceiver = new StorageBroadcastReceiver(this);

        //Create and fill a Intent Filter to
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_DATA_AVAILABLE);

        // Register the bleReceiver
        this.registerReceiver(bleReceiver, intentFilter);
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

            // create new StorageDataSet
            //      StorageDataSet.createSet(xPos, yPos, data);

            // save the data set in the .json-file
        }
    }

    /**
     * Unregisters the bleReceiver when the Service gets DESTROYED
     */
    @Override
    public void onDestroy() {
        this.unregisterReceiver(bleReceiver);
    }
}


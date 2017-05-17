package kn.uni.inf.sensortagvr.stor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Just a custom Broadcast Receiver to get the Data from the bluetooth broadcast
 * Created by gero on 16.05.17.
 */

public class StorageBroadcastReceiver extends BroadcastReceiver {

    private StorageMainService mainService;

    /**
     * This construct enables this broadcast to start the main service when BLE data was sent
     *
     * @param service
     */
    public StorageBroadcastReceiver(StorageMainService service) {
        this.mainService = service;
    }

    /**
     * Starts the main service
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Specify the Component name
        intent.setClassName("kn.uni.inf.sensortagvr.stor", "StorageMainService");

        // Start the main service
        mainService.startService(intent);
    }
}



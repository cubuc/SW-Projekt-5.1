package kn.uni.inf.sensortagvr.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;



/**
 * 
 */
public class WifiScanBroadcastReceiver extends BroadcastReceiver {

    private final WifiTracker wifiTracker;

    /**
     * 
     * @param tracker 
     */
    WifiScanBroadcastReceiver(WifiTracker tracker) {
        wifiTracker = tracker;
    }

    /**
     * @param context
     * @param intent
     * {@inheritDoc}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        wifiTracker.update();

        //Log.e("TRACKING_MANAGER", "WifiScan completed!");
    }

}

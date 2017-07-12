package kn.uni.inf.sensortagvr.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;



public class WifiScanBroadcastReceiver extends BroadcastReceiver {

    private final WifiTracker wifiTracker;

    WifiScanBroadcastReceiver(WifiTracker tracker) {
        wifiTracker = tracker;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        wifiTracker.update();

    }

}
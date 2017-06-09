package kn.uni.inf.sensortagvr.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ojo on 08.06.17.
 */

public class WifiScanBroadcastReceiver extends BroadcastReceiver {

    private WifiTracker wifiTracker;

    WifiScanBroadcastReceiver(WifiTracker tracker) {
        wifiTracker = tracker;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        wifiTracker.update();

        //Log.e("TRACKING_MANAGER", "WifiScan completed!");
    }

}

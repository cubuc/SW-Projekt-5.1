package kn.uni.inf.sensortagvr.tracking;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by ojo on 09.05.17.
 */

public class TrackingManagerService extends Service {

    private final IBinder binder = new TrackingBinder();
    private BroadcastReceiver br;

    private WifiTracker wifiTracker;

    private Location origin = null;
    private Location lastGPSPosition = new Location("TRACKING_MANAGER");

    private LocationManager locationManager = null;
    private LocationListener locationListener = new LocationListener() {
        /**
         * @param location
         */
        public void onLocationChanged(Location location) {
            lastGPSPosition = location;
        }

        /**
         *
         * @param provider
         * @param status
         * @param extras
         */
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        /**
         *
         * @param provider
         */
        public void onProviderEnabled(String provider) {
        }

        /**
         *
         * @param provider
         */
        public void onProviderDisabled(String provider) {
        }
    };

    /**
     *
     */
    @Override
    public void onCreate(){
        wifiTracker = new WifiTracker((WifiManager) getApplication().getApplicationContext().getSystemService(Context.WIFI_SERVICE));

        FileInputStream inputStream;
        try {
            inputStream = openFileInput("AP_CONFIG.json");
            wifiTracker.readFromFile(inputStream);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        final Handler handler = new Handler();
        handler.post(new LocationUpdater(handler, wifiTracker));

        br = new WifiScanBroadcastReceiver(wifiTracker);
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        this.registerReceiver(br, filter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        try {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

            if(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null)
                lastGPSPosition = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            else
                Log.e("TrackingManager", "No last location!");
        } catch (SecurityException e) {
            Log.e("TrackingManager", "Error creating location manager: " + e.getLocalizedMessage());
            stopSelf();
        }

        return binder;
    }

    /**
     *
     * @param intent
     */
    @Override
    public boolean onUnbind(Intent intent) {
        if(locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        super.onUnbind(intent);

        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput("AP_CONFIG.json", Context.MODE_PRIVATE);
            wifiTracker.writeToFile(outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        unregisterReceiver(br);
    }

    public List<WifiAP> getWifiAPs() {
        return wifiTracker.getWifiAPs(false);
    }

    public boolean trackAP(WifiAP ap) {
        return wifiTracker.trackAP(ap);
    }

    //INTERFACE PROVIDED BY SERVICE:
    //get the current location of the device

    /**
     *
     */
    public PointF getRelativePosition() {
        PointF lastPostion = wifiTracker.calculateLocation();

        return lastPostion == null ? new PointF(0, 0) : lastPostion;
    }

    public Location getAbsolutePosition() throws Exception {
        //Earth’s radius, sphere
        final double R = 6378137.0;
        Location loc = new Location("TrackingManager");
        PointF lastPostion = getRelativePosition();

        if(origin == null)
            throw new Exception("No origin was set!");

        //Coordinate offsets in radians
        double dLat = lastPostion.x/R;
        double dLon = lastPostion.y/(R*Math.cos( Math.PI * origin.getLatitude() / 180.0));

        loc.setLatitude(origin.getLatitude() + dLat * 180.0 / Math.PI);
        loc.setLongitude(origin.getLongitude() + dLon * 180.0 / Math.PI );

        return loc;
    }

    public Location calibrateOrigin() throws Exception{
        if(lastGPSPosition != null)
            origin = lastGPSPosition;
        else
            throw new Exception("No position could be determined by GPS or network!");

        return lastGPSPosition;
    }

    public class TrackingBinder extends Binder {
        /**
         *
         */
        public TrackingManagerService getService() {
            return TrackingManagerService.this;
        }
    }

    class LocationUpdater implements Runnable {

        private Handler handler;
        private WifiTracker wifiTracker;

        public LocationUpdater(Handler handler, WifiTracker wifiTracker) {
            this.handler = handler;
            this.wifiTracker = wifiTracker;
        }

        @Override
        public void run() {
            this.handler.postDelayed(this, 500);

            wifiTracker.update();
        }
    }
}
package kn.uni.inf.sensortagvr.tracking;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

/**
 * Created by ojo on 09.05.17.
 */

public class TrackingManagerService extends Service {

    private final IBinder binder = new TrackingBinder();

    private Location customPosition = null;
    private Location lastPosition = new Location("TRACKING_MANAGER");

    private LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    private LocationListener locationListener = new LocationListener() {
        /**
         * @param location
         */
        public void onLocationChanged(Location location) {
            lastPosition = location;
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
     * @param intent
     */
    @Override
    public IBinder onBind(Intent intent) {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException e) {

        }

        return binder;
    }

    /**
     *
     * @param intent
     */
    @Override
    public boolean onUnbind(Intent intent) {
        locationManager.removeUpdates(locationListener);
        return false;
    }

    //INTERFACE PROVIDED BY SERVICE:
    //get the current location of the device

    /**
     *
     */
    public Location getCurrentPosition() {
        if (customPosition != null)
            return customPosition;

        return lastPosition;
    }

    //set a custom location to overwrite real device location

    /**
     *
     * @param newCustomPos
     */
    public void setCustomPosition(Location newCustomPos) {
        customPosition = newCustomPos;
    }

    //clear location overwrite

    /**
     *
     */
    public void clearCustomPosition() {
        customPosition = null;
    }

    /**
     *
     */
    public class TrackingBinder extends Binder {
        /**
         *
         */
        public TrackingManagerService getService() {
            return TrackingManagerService.this;
        }
    }
}



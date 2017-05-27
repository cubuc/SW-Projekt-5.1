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
import android.util.Log;

/**
 * Created by ojo on 09.05.17.
 */

public class TrackingManagerService extends Service {

    private final IBinder binder = new TrackingBinder();

    private Location customPosition = null;
    private Location lastPosition = new Location("TRACKING_MANAGER");

    private LocationManager locationManager = null;

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
     */
    @Override
    public void onCreate(){
        //locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        try {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

            if(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null)
                lastPosition = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
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
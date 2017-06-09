package kn.uni.inf.sensortagvr.tracking;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
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

    private Location origin = null;
    private PointF lastPostion = new PointF(0, 0);

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
        //locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     *
     * @param intent
     */
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

        return false;
    }

    //INTERFACE PROVIDED BY SERVICE:
    //get the current location of the device

    /**
     *
     */
    public PointF getRelativePosition() {
        return lastPostion;
    }

    /**
     * 
     */
    public Location getAbsolutePosition() throws Exception {
        //Earth’s radius, sphere
        final double R = 6378137.0;
        Location loc = new Location("TrackingManager");

        if(origin == null)
            throw new Exception("No origin was set!");

        //Coordinate offsets in radians
        double dLat = lastPostion.x/R;
        double dLon = lastPostion.y/(R*Math.cos( Math.PI * origin.getLatitude() / 180.0));

        loc.setLatitude(origin.getLatitude() + dLat * 180.0 / Math.PI);
        loc.setLongitude(origin.getLongitude() + dLon * 180.0 / Math.PI);

        return loc;
    }

    /**
     * 
     */
    public Location calibrateOrigin() throws Exception{
        if(lastGPSPosition != null)
            origin = lastGPSPosition;
        else
            throw new Exception("No position could be determined by GPS or network!");

        return lastGPSPosition;
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


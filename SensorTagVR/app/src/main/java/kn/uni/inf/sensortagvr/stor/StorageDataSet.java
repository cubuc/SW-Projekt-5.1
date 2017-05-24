package kn.uni.inf.sensortagvr.stor;

import android.location.Location;

/**
 * Basic data type to combine the sensor data with the tracking data
 * <p>
 * Created by gero on 16.05.17.
 */

public class StorageDataSet {

    public Location location;

    /* Position */
    public double xPos;
    public double yPos;
    public final double[] position = {xPos, yPos};

    /* Sensor Data, will be expanded */
    public float[] sensorData;

    /* Timestamp */
    public String timestamp;

    /**
     * Creates a timestamp for every instance of the class
     */
    public StorageDataSet() {
        Long ts = System.currentTimeMillis();
        this.timestamp = ts.toString();
    }


    /**
     * Creates a proper data set to work with
     *
     * @param xPos location data
     * @param yPos location data
     * @param data sensor data
     */
    public StorageDataSet(double xPos, double yPos, float[] data) {
        this();
        this.xPos = xPos;
        this.yPos = yPos;
        this.sensorData = data;
    }

    /**
     * Different approach to location storing
     * @param loc
     * @param data
     */
    public StorageDataSet(Location loc, float[] data) {
        this();
        this.location = loc;
        this.sensorData = data;
    }

    /**
     * Factory method to create data sets
     *
     * @param xPos
     * @param yPos
     * @param data
     * @return
     */
    public static kn.uni.inf.sensortagvr.stor.StorageDataSet createSet(double xPos, double yPos, float[] data) {
        return new kn.uni.inf.sensortagvr.stor.StorageDataSet(xPos, yPos, data);
    }

    public static kn.uni.inf.sensortagvr.stor.StorageDataSet createSet(Location loc, float[] data) {
        return new StorageDataSet(loc,data);
    }
}


package kn.uni.inf.sensortagvr.stor;

/**
 * Basic data type to combine the sensor data with the tracking data
 * <p>
 * Created by gero on 16.05.17.
 */

public class StorageDataSet {

    /* Position */
    public int xPos;
    public int yPos;
    public final int[] position = {xPos, yPos};

    /* Sensor Data, will be expanded */
    public int sensorData;

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
    public StorageDataSet(int xPos, int yPos, int data) {
        this();
        this.xPos = xPos;
        this.yPos = yPos;
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
    public static kn.uni.inf.sensortagvr.stor.StorageDataSet createSet(int xPos, int yPos, int data) {
        return new kn.uni.inf.sensortagvr.stor.StorageDataSet(xPos, yPos, data);
    }
}


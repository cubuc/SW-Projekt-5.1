package kn.uni.inf.sensortagvr.stor;

import java.util.Calendar;

/**
 * A CompactData Object stores a data set, which can be easily used by JavaScript
 *
 * Created by gero on 29.05.17.
 */

public class CompactData {

    /**
     * x: north/south
     * y: east/west
     *
     * z: measured data
     *
     * time: time data was recorded
     */
    private double x;
    private double y;
    private double z;

    private final String time;

    public CompactData(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.time = Calendar.getInstance().getTime().toString();
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @SuppressWarnings("unused")
    public String getTime() {
        return time;
    }
}

package kn.uni.inf.sensortagvr.stor;

import android.graphics.PointF;

import java.util.Calendar;

/**
 * A CompactData Object stores a data set, which can be easily used by JavaScript
 *
 * Created by gero on 29.05.17.
 */

class CompactData {

    /**
     * x: north/south
     * y: east/west
     *
     * z: scaled measured data
     *
     * time: time data was recorded
     */
    private final String time;
    private double x;
    private double y;
    private double z;
    private float data;

    CompactData() {
        this.time = Calendar.getInstance().getTime().toString();
    }


    /**
     * @param x
     * @param y
     * @param data
     */
    CompactData(double x, double y, float data) {
        this();

        this.x = x;
        this.y = y;

        this.data = data;
    }

    CompactData(PointF loc , float data) {
        this();

        this.x = loc.x;
        this.y = loc.y;

        this.data = data;
    }

    /**
     *
     */
    double getX() {
        return x;
    }

    /**
     *
     * @param x
     */
    void setX(double x) {
        this.x = x;
    }

    /**
     *
     */
    double getY() {
        return y;
    }

    /**
     *
     * @param y
     */
    void setY(double y) {
        this.y = y;
    }

    /**
     *
     */
    float getData() {
        return data;
    }

    /**
     *
     */
    public String getTime() {
        return time;
    }

    public String toString() {
        return "x: " + x + " , " + "y: " + y + " \n, " + "z: " + getZ() + " ,\n " + "data: " + data +
                "\n" + time;
    }

    double getZ() {
        return z;
    }

    /**
     *
     * @param z
     */
    void setZ(double z) {
        this.z = z;
    }
}



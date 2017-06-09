package kn.uni.inf.sensortagvr.stor;

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
    private float x;
    private float y;
    private float z;
    private float data;

    /**
     * @param x
     * @param y
     * @param data
     */
    CompactData(float x, float y, float data) {
        this.x = x;
        this.y = y;

        this.data = data;

        this.time = Calendar.getInstance().getTime().toString();
    }

    /**
     *
     */
    float getX() {
        return x;
    }

    /**
     *
     * @param x
     */
    void setX(float x) {
        this.x = x;
    }

    /**
     *
     */
    float getY() {
        return y;
    }

    /**
     *
     * @param y
     */
    void setY(float y) {
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

    public float getZ() {
        return z;
    }

    /**
     *
     * @param z
     */
    void setZ(float z) {
        this.z = z;
    }
}



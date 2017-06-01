package kn.uni.inf.sensortagvr.stor;

import java.util.Calendar;

/**
 * A CompactData Object stores a data set, which can be easily used by JavaScript
 *
 * Created by gero on 29.05.17.
 */

class CompactData {

    private final String time;
    /**
     * x: north/south
     * y: east/west
     *
     * z: scaled measured data
     *
     * time: time data was recorded
     */
    private double x;
    private double y;
    private double z;
    private double data;

    CompactData(double x, double y, double data) {
        this.x = x;
        this.y = y;

        this.data = data;

        this.time = Calendar.getInstance().getTime().toString();
    }

    double getX() {
        return x;
    }

    void setX(double x) {
        this.x = x;
    }

    double getY() {
        return y;
    }

    void setY(double y) {
        this.y = y;
    }

    void setZ(double z) {
        this.z = z;
    }

    double getData() {
        return data;
    }

    @SuppressWarnings("unused")
    public String getTime() {
        return time;
    }
}

package kn.uni.inf.sensortagvr.ble;

/**
 *
 */
public class DataListItem {
    private Sensor sensorName;
    private float data0;
    private float data1;
    private float data2;


    /**
     *
     */
    public float getData0() {
        return data0;
    }

    /**
     * @param data
     */
    public void setData(float[] data) {
        this.data0 = data[0];
        this.data1 = data[1];
        this.data2 = data[2];

    }

    /**
     *
     */
    public float getData1() {
        return data1;
    }

    /**
     *
     */
    public float getData2() {
        return data2;
    }

    /**
     *
     */
    public Sensor getSensorName() {
        return sensorName;
    }

    /**
     *
     * @param sensorName
     */
    void setSensorName(Sensor sensorName) {
        this.sensorName = sensorName;
    }


}


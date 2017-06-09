package kn.uni.inf.sensortagvr.ble;


import android.bluetooth.BluetoothDevice;

/**
 *
 */
public class ScanListItem {
    private BluetoothDevice device;

    /**
     *
     */
    public BluetoothDevice getDevice() {
        return device;
    }

    /**
     * @param device
     */
    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    /**
     *
     */
    public String getDeviceName() {
        return device.getName();
    }

    /**
     *
     */
    public String getDeviceAddress() {
        return device.getAddress();
    }
}



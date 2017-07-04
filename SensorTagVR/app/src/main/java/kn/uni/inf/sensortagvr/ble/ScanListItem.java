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
    BluetoothDevice getDevice() {
        return device;
    }

    /**
     * @param device set the device for a list entry in the ScanListActivity
     */
    void setDevice(BluetoothDevice device) {
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

   /* public void onClick(View view) {
        final BluetoothDevice device = this.getDevice();
        if (device == null) return;
        final Intent intent = new Intent(view.getContext(), LiveDataActivity.class);
        intent.putExtra(LiveDataActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(LiveDataActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        view.getContext().startActivity(intent);
    }*/
}



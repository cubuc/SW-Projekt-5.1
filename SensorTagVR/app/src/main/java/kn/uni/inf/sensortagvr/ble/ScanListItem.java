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
     * @param device set the device for a list entry in the ScanListActivity
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

    /**
     * called by the data binding library/ listitem device
     * @param view the view in that the item is
     */
/*     public void onClick(View view) {
         Log.i("onClicker", "in the onClick");
        if (device == null) return;
        final Intent intent = new Intent(view.getContext(), LiveDataActivity.class);
        intent.putExtra(LiveDataActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(LiveDataActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        view.getContext().startActivity(intent);
    }*/
}



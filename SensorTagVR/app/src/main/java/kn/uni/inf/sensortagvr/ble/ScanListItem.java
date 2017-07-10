package kn.uni.inf.sensortagvr.ble;


import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.view.View;

/**
 *
 */
public class ScanListItem implements View.OnClickListener {
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


    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), LiveDataActivity.class);
        intent.putExtra(LiveDataActivity.EXTRAS_DEVICE, device);
        if (device == null) return;
        v.getContext().startActivity(intent);
    }

}



package kn.uni.inf.sensortagvr.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import kn.uni.inf.sensortagvr.R;

import static kn.uni.inf.sensortagvr.R.layout.listitem_device;

/**
 * Created by fabian on 29.05.17.
 */

// Adapter for holding devices found through scanning.

/**
 *
 */
class LeDeviceListAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> mLeDevices;
    private LayoutInflater mInflator;

    /**
     *
     */
    LeDeviceListAdapter(Context con) {
        super();
        mLeDevices = new ArrayList<>();
        mInflator = LayoutInflater.from(con);
    }

    /**
     * @param device
     */
    void addDevice(BluetoothDevice device) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    /**
     * @param position
     */
    BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    /**
     *
     */
    void clear() {
        mLeDevices.clear();
    }

    /**
     *
     */
    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    /**
     * @param i
     */
    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    /**
     * @param i
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * @param i
     * @param view
     * @param viewGroup
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = mInflator.inflate(listitem_device, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = mLeDevices.get(i);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText(R.string.unknown_device);
        viewHolder.deviceAddress.setText(device.getAddress());

        return view;
    }

    /**
     *
     */
    private static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}

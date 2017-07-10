package kn.uni.inf.sensortagvr.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v4.util.ArraySet;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import kn.uni.inf.sensortagvr.R;
import kn.uni.inf.sensortagvr.databinding.ListitemDeviceBinding;


// Adapter for holding devices found through scanning.

/**
 *
 */
public class LeDeviceListAdapter extends RecyclerView.Adapter<LeDeviceListAdapter.ViewHolder> {
    private ArraySet<ScanListItem> deviceSet = new ArraySet<>();

    public void setDeviceSet(ArraySet<ScanListItem> deviceSet) {
        this.deviceSet = deviceSet;
        notifyDataSetChanged();
    }

    /**
     * @param device Bluetooth device to add
     */
    public void addDevice(BluetoothDevice device) {
        for (ScanListItem li : deviceSet) {
            if (li.getDeviceAddress().equals(device.getAddress()))
                return;
        }

        ScanListItem li = new ScanListItem();
        li.setDevice(device);
        this.deviceSet.add(li);
        notifyItemInserted(this.deviceSet.size());
        Log.i("devlistadapter", "addDevice");
    }


    /**
     *
     */
    public void clear() {
        deviceSet.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LeDeviceListAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final Context con = parent.getContext();

        LayoutInflater layoutInflater = LayoutInflater.from(con);

        final ListitemDeviceBinding binding = DataBindingUtil.inflate(layoutInflater,
                R.layout.listitem_device, parent, false);

        return new LeDeviceListAdapter.ViewHolder(binding);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(LeDeviceListAdapter.ViewHolder holder, int position) {
        holder.bind(deviceSet.valueAt(position));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return deviceSet.size();
    }

    /**
     *
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ListitemDeviceBinding binding;

        /**
         * @param binding
         */
        ViewHolder(ListitemDeviceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * @param li
         */
        void bind(ScanListItem li) {
            binding.setDeviceItem(li);
        }


    }
}
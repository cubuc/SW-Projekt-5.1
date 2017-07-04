package kn.uni.inf.sensortagvr.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v4.util.ArraySet;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kn.uni.inf.sensortagvr.R;
import kn.uni.inf.sensortagvr.databinding.ListitemDeviceBinding;


// Adapter for holding devices found through scanning.

/**
 *
 */
public class LeDeviceListAdapter extends RecyclerView.Adapter<LeDeviceListAdapter.ViewHolder> {
    private final ArraySet<ScanListItem> deviceSet = new ArraySet<>();

/*    *//*
    public void setDeviceSet(ArraySet<ScanListItem> deviceSet) {
        this.deviceSet = deviceSet;
        notifyDataSetChanged();
    }*/

    /**
     * @param device Bluetooth device to add
     */
    void addDevice(BluetoothDevice device) {
        for (ScanListItem li : deviceSet) {
            if (li.getDeviceAddress().equals(device.getAddress()))
                return;
        }

        ScanListItem li = new ScanListItem();
        li.setDevice(device);
        this.deviceSet.add(li);
        notifyItemInserted(this.deviceSet.size());
    }


    /**
     *
     */
    void clear() {
        deviceSet.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LeDeviceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListitemDeviceBinding binding = DataBindingUtil.inflate(layoutInflater,
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
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ListitemDeviceBinding binding;

        /**
         * @param binding fill the data in using the data binding library, therefore a generated
         *                binding class is used
         *
         *                for more information dive into the official documentation and have a look
         *                at the activity_scanlist.xml
         */
        ViewHolder(ListitemDeviceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * @param li a list item (in this case a device entry on the list) that shall be bound to
         *           the view by using the data binding library
         */
        void bind(ScanListItem li) {
            binding.setDeviceItem(li);
        }

        /**
         * {@inheritDoc}
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            final BluetoothDevice device = binding.getDeviceItem().getDevice();
            if (device == null) return;
            final Intent intent = new Intent(v.getContext(), LiveDataActivity.class);
            intent.putExtra(LiveDataActivity.EXTRAS_DEVICE_NAME, device.getName());
            intent.putExtra(LiveDataActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
            v.getContext().startActivity(intent);
        }
    }
}
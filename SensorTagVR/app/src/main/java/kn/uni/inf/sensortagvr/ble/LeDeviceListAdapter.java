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
class LeDeviceListAdapter extends RecyclerView.Adapter<LeDeviceListAdapter.ViewHolder> {
    private ArraySet<ScanListItem> deviceSet = new ArraySet<>();

    /**
     * @param deviceSet
     */
    public void setDeviceSet(ArraySet<ScanListItem> deviceSet) {
        this.deviceSet = deviceSet;
        notifyDataSetChanged();
    }

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
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     * @param viewType The view type of the new View.
     *                 an adapter position.
     *                 Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     *                 an item.
     *                 <p>
     *                 This new ViewHolder should be constructed with a new View that can represent the items
     *                 of the given type. You can either create a new View manually or inflate it from an XML
     *                 layout file.
     *                 <p>
     *                 The new ViewHolder will be used to display items of the adapter using
     *                 onBindViewHolder(ViewHolder, int, List). Since it will be re-used to display
     *                 different items in the data set, it is a good idea to cache references to sub views of
     *                 the View to avoid unnecessary {@link View#findViewById(int)} calls.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @Override
    public LeDeviceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListitemDeviceBinding binding = DataBindingUtil.inflate(layoutInflater,
                R.layout.listitem_device, parent, false);
        return new LeDeviceListAdapter.ViewHolder(binding);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the ViewHolder#itemView to reflect the item at the given
     * position.
     * <p>
     * Note that unlike ListView, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use ViewHolder#getAdapterPosition() which will
     * have the updated adapter position.
     * <p>
     * Override  #onBindViewHolder(ViewHolder, int, List) instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     * @param position The position of the item within the adapter's data set.
     *                 item at the given position in the data set.
     */
    @Override
    public void onBindViewHolder(LeDeviceListAdapter.ViewHolder holder, int position) {
        holder.bind(deviceSet.valueAt(position));
    }

    /**
     * @param i position of the item
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return deviceSet.size();
    }

    /**
     *
     */
    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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

        /**
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
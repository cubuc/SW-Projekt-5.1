package kn.uni.inf.sensortagvr.ble;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import kn.uni.inf.sensortagvr.R;
import kn.uni.inf.sensortagvr.databinding.ListitemDataBinding;

public class SensorDataListAdapter extends RecyclerView.Adapter<SensorDataListAdapter.ViewHolder> {

    List<DataListItem> dataList = new ArrayList<>();

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * onBindViewHolder(ViewHolder, int, List). Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(ViewHolder, int)
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListitemDataBinding binding = DataBindingUtil.inflate(layoutInflater,
                R.layout.listitem_data, parent, false);
        return new ViewHolder(binding);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override onBindViewHolder(ViewHolder, int, List) instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(dataList.get(position));
    }

    public void setDataList(List<DataListItem> list) {
        this.dataList = list;
        notifyDataSetChanged();
    }

    public void addItem(Sensor s, float[] data) {
        int k = 0;
        for (DataListItem i : dataList) {
            if (i.getSensorName().equals(s)) {
                i.setData(data);
                notifyItemChanged(k);
                return;
            }
            k++;
        }
        DataListItem i = new DataListItem();
        i.setSensorName(s);
        i.setData(data);
        dataList.add(i);
        notifyItemInserted(dataList.size() - 1);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ListitemDataBinding binding;

        public ViewHolder(ListitemDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DataListItem li) {
            binding.setSensoritem(li);
        }
    }
}

/*
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import kn.uni.inf.sensortagvr.R;

import static kn.uni.inf.sensortagvr.R.layout.listitem_data;

*/
/**
 * Created by fabian on 29.05.17.
 *//*


class SensorDataListAdapter extends BaseAdapter {
    private final String TAG = "SensorDataLAdapter";
    Sensor[] mSensors;
    LayoutInflater mInflater;

    SensorDataListAdapter(Context con) {
        super();
        mSensors = Sensor.SENSOR_LIST;
        mInflater = LayoutInflater.from(con);
    }

    */
/**
     * @param data
 *//*

    static void displayData(View view, float[] data) {
        if (data != null) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            if (viewHolder != null) {
                viewHolder.val0.setText(String.valueOf(data[0]));
                viewHolder.val1.setText(String.valueOf(data[1]));
                viewHolder.val2.setText(String.valueOf(data[2]));
            }
        }
    }

    */
/**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
 *//*

    @Override
    public int getCount() {
        int z = 0;
        for (Sensor s : Sensor.SENSOR_LIST) z++;
        return z;
    }

    */
/**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
 *//*

    @Override
    public Object getItem(int position) {
        return mSensors[position];
    }

    */
/**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
 *//*

    @Override
    public long getItemId(int position) {
        return position;
    }

    */
/**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position The position of the item within the adapter's data set of the item whose view
     *                 we want.
     * @param view     The old view to reuse, if possible. Note: You should check that this view
     *                 is non-null and of an appropriate type before using. If it is not possible to convert
     *                 this view to display the correct data, this method can create a new view.
     *                 Heterogeneous lists can specify their number of view types, so that this View is
     *                 always of the right type (see {@link #getViewTypeCount()} and
     *                 {@link #getItemViewType(int)}).
     * @param parent   The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
 *//*

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = mInflater.inflate(listitem_data, null);
            viewHolder = new ViewHolder();
            viewHolder.sensorName = (TextView) view.findViewById(R.id.sensor_name);
            viewHolder.val0 = (TextView) view.findViewById(R.id.sensor_data_0);
            viewHolder.val1 = (TextView) view.findViewById(R.id.sensor_data_1);
            viewHolder.val2 = (TextView) view.findViewById(R.id.sensor_data_2);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
            if (viewHolder == null) Log.e(TAG, "view.getTag failed");
        }

        Sensor sensor = (Sensor) getItem(position);
        final String sensorName = sensor.getName();

        if (sensorName != null && sensorName.length() > 0)
            viewHolder.sensorName.setText(sensorName);
        else
            viewHolder.sensorName.setText(R.string.unknown_sensor);

        displayData(view, new float[]{0, 0, 0});

        return view;
    }

    */
/**
     *
 *//*


}
*/

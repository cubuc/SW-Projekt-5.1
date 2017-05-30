package kn.uni.inf.sensortagvr.ble;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import kn.uni.inf.sensortagvr.R;

import static kn.uni.inf.sensortagvr.R.layout.listitem_data;

/**
 * Created by fabian on 29.05.17.
 */

class SensorDataListAdapter extends BaseAdapter {
    private final String TAG = "SensorDataLAdapter";
    Sensor[] mSensors;
    LayoutInflater mInflater;
    ArrayList<float[]> mData;

    SensorDataListAdapter(Context con) {
        super();
        mSensors = Sensor.SENSOR_LIST;
        mInflater = LayoutInflater.from(con);
        mData = new ArrayList<>();
    }

    /**
     * @param data
     */
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

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        int z = 0;
        for (Sensor s : Sensor.SENSOR_LIST) z++;
        return z;
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return mSensors[position];
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

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
     */
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            tview = mInflater.inflate(listitem_data, null);
            viewHolder = new ViewHolder();
            viewHolder.sensorName = (TextView) view.findViewById(R.id.sensor_name);
            viewHolder.val0 = (TextView) view.findViewById(R.id.data_value_0);
            viewHolder.val1 = (TextView) view.findViewById(R.id.data_value_1);
            viewHolder.val2 = (TextView) view.findViewById(R.id.data_value_2);
            tview.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) tview.getTag();
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

    /**
     *
     */
    private static class ViewHolder {
        TextView sensorName;
        TextView val0;
        TextView val1;
        TextView val2;
    }
}

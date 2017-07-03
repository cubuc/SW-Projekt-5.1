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

/**
 *
 */
public class SensorDataListAdapter extends RecyclerView.Adapter<SensorDataListAdapter.ViewHolder> {

    private List<DataListItem> dataList = new ArrayList<>();

    /**
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     * @param viewType The view type of the new View.
     * an adapter position.
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
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     * @param position The position of the item within the adapter's data set.
     * item at the given position in the data set.
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
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(dataList.get(position));
    }

    /**
     * @param list
     */
    public void setDataList(List<DataListItem> list) {
        this.dataList = list;
        notifyDataSetChanged();
    }

    /**
     *
     * @param s
     * @param data
     */
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

    /**
     *
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ListitemDataBinding binding;

        /**
         * @param binding
         */
        public ViewHolder(ListitemDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         *
         * @param li
         */
        public void bind(DataListItem li) {
            binding.setSensoritem(li);
        }
    }
}



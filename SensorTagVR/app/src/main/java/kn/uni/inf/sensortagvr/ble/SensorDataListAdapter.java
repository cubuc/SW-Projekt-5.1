package kn.uni.inf.sensortagvr.ble;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import kn.uni.inf.sensortagvr.R;
import kn.uni.inf.sensortagvr.databinding.ListitemDataBinding;

/**
 *
 */
class SensorDataListAdapter extends RecyclerView.Adapter<SensorDataListAdapter.ViewHolder> {

    private List<DataListItem> dataList = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListitemDataBinding binding = DataBindingUtil.inflate(layoutInflater,
                R.layout.listitem_data, parent, false);
        return new ViewHolder(binding);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(dataList.get(position));
    }

    /**
     * @param list list to be displayed be the recycler view in the LiveDataActivity
     */
    void setDataList(List<DataListItem> list) {
        this.dataList = list;
        notifyDataSetChanged();
    }

    /**
     *
     * @param s sensor to be added to the live data activity view
     * @param data data according to the sensor on the ti cc2650 mcu
     */
    void addItem(Sensor s, float data) {
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
     * {@inheritDoc}
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
         * @param binding Binding to be used to bind the data to the ui via the data binding library
         *                for more details see the data binding library documentation and the
         *                according .xml
         */
        ViewHolder(ListitemDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         *
         * @param li Data list item to be bound to the ui/recycler view in the LiveDataActivity
         */
        void bind(DataListItem li) {
            binding.setSensoritem(li);
        }
    }
}



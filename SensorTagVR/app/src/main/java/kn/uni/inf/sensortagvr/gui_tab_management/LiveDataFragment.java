package kn.uni.inf.sensortagvr.gui_tab_management;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;

import kn.uni.inf.sensortagvr.R;
import kn.uni.inf.sensortagvr.ble.BluetoothLEService;
import kn.uni.inf.sensortagvr.ble.DataListItem;
import kn.uni.inf.sensortagvr.ble.Sensor;
import kn.uni.inf.sensortagvr.ble.SensorDataListAdapter;
import kn.uni.inf.sensortagvr.tracking.TrackingManagerService;

import static kn.uni.inf.sensortagvr.ble.BluetoothLEService.EXTRA_SENSOR;


public class LiveDataFragment extends Fragment {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String TAG = kn.uni.inf.sensortagvr.gui_tab_management.LiveDataFragment
            .class.getSimpleName();
    LocalBroadcastManager mLocalBroadcastManager;
    SensorDataListAdapter adapter = new SensorDataListAdapter();
    private TextView mConnectionState;
    private TextView location;
    private String mDeviceAddress = "0";
    private TrackingManagerService mTrackingService;


    /**
     * Handles the connection to the LocationService
     */
    private final ServiceConnection mTrackSvcConnection = new ServiceConnection() {

        /**
         * @param className ComponentName
         * @param service IBinder
         */
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            TrackingManagerService.TrackingBinder binder = (TrackingManagerService.TrackingBinder) service;
            mTrackingService = binder.getService();
            mTrackingService.getRelativePosition();
        }

        /**
         * @param arg0 ComponentName
         */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mTrackingService = null;
        }
    };

    /**
     * Handles various events fired by the Service.
     * ACTION_GATT_CONNECTED: connected to a GATT server.
     * ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
     * ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
     * ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
     * or notification operations.
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        /**
         *
         * @param context the contect from which the intent was sent
         * @param intent the intent that was sent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BluetoothLEService.ACTION_GATT_CONNECTED:
                    updateConnectionState(R.string.connected);
                    break;
                case BluetoothLEService.ACTION_GATT_DISCONNECTED:
                    updateConnectionState(R.string.disconnected);
                    clearUI();
                    break;
                case BluetoothLEService.ACTION_DATA_AVAILABLE:
                    adapter.addItem(((Sensor) intent.getExtras().get(EXTRA_SENSOR)),
                            intent.getFloatArrayExtra(BluetoothLEService.EXTRA_DATA));
                    updateLocation(mTrackingService.getRelativePosition());
                    break;
                default:
                    Log.e(TAG, "invalid broadcast received");
            }
        }
    };


    public LiveDataFragment() {
        // Required empty public constructor
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void clearUI() {
        adapter.setDataList(Collections.<DataListItem>emptyList());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context con = getContext();
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_live_data, container, false);

        mLocalBroadcastManager =
                LocalBroadcastManager.getInstance(con);

        // Sets up UI references.
        ((TextView) v.findViewById(R.id.device_address)).setText(mDeviceAddress);
        RecyclerView mLiveDataList = (RecyclerView) v.findViewById(R.id.data_list);
        mConnectionState = (TextView) v.findViewById(R.id.connection_state);
        location = (TextView) v.findViewById(R.id.location);
        mLiveDataList.setAdapter(adapter);
        mLiveDataList.setLayoutManager(new LinearLayoutManager(con));

        Intent startintent = new Intent(con, BluetoothLEService.class);
        con.startService(startintent);
        return v;
    }

    public void onResume() {
        super.onResume();
        Context con = getContext();
        mLocalBroadcastManager.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        con.bindService(new Intent(con, TrackingManagerService.class), mTrackSvcConnection, 0);
    }


    /**
     * @param resourceId a id code for connected or disconnected set in res/values/strings
     */
    private void updateConnectionState(final int resourceId) {
        mConnectionState.setText(resourceId);
    }

    private void updateLocation(PointF loc) {
        location.setText(loc.toString());
    }
}

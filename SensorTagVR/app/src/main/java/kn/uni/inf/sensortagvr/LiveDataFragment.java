package kn.uni.inf.sensortagvr;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
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

import kn.uni.inf.sensortagvr.ble.BluetoothLEService;
import kn.uni.inf.sensortagvr.ble.DataListItem;
import kn.uni.inf.sensortagvr.ble.Sensor;
import kn.uni.inf.sensortagvr.ble.SensorDataListAdapter;
import kn.uni.inf.sensortagvr.tracking.TrackingManagerService;

import static kn.uni.inf.sensortagvr.ble.BluetoothLEService.EXTRA_SENSOR;


public class LiveDataFragment extends Fragment {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String TAG = LiveDataFragment
            .class.getSimpleName();
    LocalBroadcastManager mLocalBroadcastManager;
    SensorDataListAdapter adapter = new SensorDataListAdapter();
    private TextView mConnectionState;
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
                    //updateLocation(mTrackingService.getRelativePosition());
                    break;
                default:
                    Log.e(TAG, "invalid broadcast received");
            }
        }
    };
    private TextView location;
    private String mDeviceAddress = "0";
    private TrackingManagerService mTrackingService;
    /**
     * Handles the connection to the LocationService
     */
    private final ServiceConnection mTrackSvcConnection = new ServiceConnection() {

        /**
         * @param className ComponentName
         * @param service   IBinder
         */
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            TrackingManagerService.TrackingBinder binder = (TrackingManagerService.TrackingBinder) service;
            mTrackingService = binder.getService();
        }

        /**
         * @param arg0 ComponentName
         */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mTrackingService = null;
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

    /**
     * Called to do initial creation of a fragment.  This is called after
     * #onAttach(Activity) and before
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * <p>
     * <p>Note that this can be called while the fragment's activity is
     * still in the process of being created.  As such, you can not rely
     * on things like the activity's content view hierarchy being initialized
     * at this point.  If you want to do work once the activity itself is
     * created, see {@link #onActivityCreated(Bundle)}.
     * <p>
     * <p>Any restored child fragments will be created before the base
     * <code>Fragment.onCreate</code> method returns.</p>
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context con = getContext();
        mLocalBroadcastManager =
                LocalBroadcastManager.getInstance(con);

        Intent startintent = new Intent(con, BluetoothLEService.class);
        con.startService(startintent);
        con.bindService(new Intent(con, TrackingManagerService.class), mTrackSvcConnection, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_live_data, container, false);

        // Sets up UI references.
        ((TextView) v.findViewById(R.id.device_address)).setText(mDeviceAddress);
        RecyclerView mLiveDataList = (RecyclerView) v.findViewById(R.id.data_list);
        mConnectionState = (TextView) v.findViewById(R.id.connection_state);
        location = (TextView) v.findViewById(R.id.location);
        mLiveDataList.setAdapter(adapter);
        mLiveDataList.setLayoutManager(new LinearLayoutManager(getContext()));

        return v;
    }

   /* public void onResume() {
        super.onResume();
        mLocalBroadcastManager.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

    }*/


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

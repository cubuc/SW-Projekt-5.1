package kn.uni.inf.sensortagvr.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import kn.uni.inf.sensortagvr.R;
import kn.uni.inf.sensortagvr.tracking.TrackingManagerService;

import static android.bluetooth.BluetoothProfile.GATT_SERVER;
import static kn.uni.inf.sensortagvr.ble.BluetoothLEService.EXTRA_SENSOR;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLEService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class LiveDataActivity extends AppCompatActivity {
    public static final String EXTRAS_DEVICE = "DEVICE";
    private static final String TAG = LiveDataActivity.class.getSimpleName();
    private final SensorDataListAdapter adapter = new SensorDataListAdapter();
    private LocalBroadcastManager mLocalBroadcastManager;
    private TextView mConnectionState;
    private BluetoothDevice device;
    private BluetoothLEService mBluetoothLEService;
    /**
     * Handles the connection with the BluetoothLEService
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        /**
         * {@inheritDoc}
         * connects to the tapped device when the service connection is established.
         *
         * @param componentName not used
         * @param service       The service that is bound through this service connection.
         */
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLEService = ((BluetoothLEService.LocalBinder) service).getService();
            Log.i(TAG, "onServiceConnected");

            // Automatically connects to the device upon successful start-up initialization.
            if (mBluetoothLEService.getmGatt() != null) {

                device = mBluetoothLEService.getmGatt().getDevice();

            } else if (device != null) {

                final boolean result = mBluetoothLEService.connect(device.getAddress());
                Log.d(TAG, "Connect request result=" + result);

            } else {
                Toast.makeText(getApplicationContext(), "Please go to the settings, scan for and " +
                        "connect a device.", Toast.LENGTH_LONG).show();
                LiveDataActivity.this.finish();
            }
        }

        /** {@inheritDoc}
         *
         * @param componentName not used
         * settings the current {@link BluetoothLEService} instance to null if the service connection
         *  is shutdown
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLEService = null;
        }
    };
    private boolean mConnected = false;
    private TextView location;
    private TrackingManagerService mTrackingService;
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
         * @param resourceId either R.string.connected or R.string.disconnected
         */
        private void updateConnectionState(final int resourceId) {
            mConnectionState.setText(resourceId);
        }

        private void updateLocation(PointF loc) {
            location.setText(loc.toString());
        }

        /**
         *
         */
        private void clearUI() {
            adapter.setDataList(Collections.<DataListItem>emptyList());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BluetoothLEService.ACTION_GATT_CONNECTED:
                    mConnected = true;
                    updateConnectionState(R.string.connected);
                    break;
                case BluetoothLEService.ACTION_GATT_DISCONNECTED:
                    mConnected = false;
                    updateConnectionState(R.string.disconnected);
                    clearUI();
                    break;
                case BluetoothLEService.ACTION_DATA_AVAILABLE:
                    adapter.addItem(((Sensor) intent.getExtras().get(EXTRA_SENSOR)),
                            intent.getFloatExtra(BluetoothLEService.EXTRA_DATA, 0));
                    if (mTrackingService != null)
                        updateLocation(mTrackingService.getRelativePosition());
                    Log.i(getLocalClassName(), "ACTION DATA AVAIL received");
                    break;
                default:
                    Log.e(TAG, "invalid broadcast received");
            }
        }
    };
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

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livedata);


        mLocalBroadcastManager =
                LocalBroadcastManager.getInstance(this);

        BluetoothManager bm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (!bm.getConnectedDevices(GATT_SERVER).isEmpty()) {
            Log.i(getLocalClassName(), "looking for connected devices");
            List<BluetoothDevice> btDevices = bm.getConnectedDevices(GATT_SERVER);
            for (BluetoothDevice btDev : btDevices) {
                Log.i(getLocalClassName(), "checking device" + btDev.getName());
                if (btDev.getName().equals("CC2650 SensorTag")) device = btDev;
            }
        } else if (getIntent().getExtras() != null && getIntent().getExtras().get(EXTRAS_DEVICE) != null) {
            device = (BluetoothDevice) getIntent().getExtras().get(EXTRAS_DEVICE);
        }
        if (device != null) {
            // Sets up UI references.
            ((TextView) findViewById(R.id.device_address)).setText(device.getAddress());
        }
        RecyclerView mLiveDataList = (RecyclerView) findViewById(R.id.data_list);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        location = (TextView) findViewById(R.id.location);
        mLiveDataList.setAdapter(adapter);
        mLiveDataList.setLayoutManager(new LinearLayoutManager(this));


        if (mBluetoothLEService == null) {
            Intent startintent = new Intent(this, BluetoothLEService.class);
            startService(startintent);
            bindService(startintent, mServiceConnection, 0);
        }
        if (mTrackingService == null)
            bindService(new Intent(this, TrackingManagerService.class), mTrackSvcConnection, 0);

        Log.i(getLocalClassName(), "onCreate finished");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();
        mLocalBroadcastManager.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        if (mBluetoothLEService == null) {
            startService(new Intent(this, BluetoothLEService.class));
            bindService(new Intent(this, BluetoothLEService.class),
                    mServiceConnection, BIND_AUTO_CREATE);
        }
        if (mTrackingService == null)
            bindService(new Intent(this, TrackingManagerService.class), mTrackSvcConnection, BIND_AUTO_CREATE);

        Log.i(getLocalClassName(), "onResume finished");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        super.onPause();
        mLocalBroadcastManager.unregisterReceiver(mGattUpdateReceiver);
        if (mBluetoothLEService != null)
            unbindService(mServiceConnection);
        if (mTrackingService != null)
            unbindService(mTrackSvcConnection);


        Log.i(getLocalClassName(), "onPaused finished");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_connect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLEService.connect(device.getAddress());
                return true;
            case R.id.menu_disconnect:
                mBluetoothLEService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}





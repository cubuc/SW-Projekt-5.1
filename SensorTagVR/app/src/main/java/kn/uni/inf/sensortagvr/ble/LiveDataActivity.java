package kn.uni.inf.sensortagvr.ble;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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

import java.util.Collections;

import kn.uni.inf.sensortagvr.R;

import static kn.uni.inf.sensortagvr.ble.BluetoothLEService.EXTRA_SENSOR;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLEService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class LiveDataActivity extends AppCompatActivity {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String TAG = LiveDataActivity.class.getSimpleName();
    LocalBroadcastManager mLocalBroadcastManager;
    SensorDataListAdapter adapter = new SensorDataListAdapter();
    private TextView mConnectionState;
    private String mDeviceName;
    private String mDeviceAddress;
    private RecyclerView mLiveDataList;
    private BluetoothLEService mBluetoothLEService;
    /**
     * Handles the connection with the BluetoothLEService
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        /**
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
            final boolean result = mBluetoothLEService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }

        /**
         * @param componentName not used
         * set the current {@link BluetoothLEService} instance to null if the service connection
         *  is shutdown
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLEService = null;
        }
    };
    private boolean mConnected = false;
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
         * @param context
         * @param intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BluetoothLEService.ACTION_GATT_CONNECTED:
                    mConnected = true;
                    updateConnectionState(R.string.connected);
                    invalidateOptionsMenu();
                    break;
                case BluetoothLEService.ACTION_GATT_DISCONNECTED:
                    mConnected = false;
                    updateConnectionState(R.string.disconnected);
                    invalidateOptionsMenu();
                    clearUI();
                    break;
                case BluetoothLEService.ACTION_DATA_AVAILABLE:
                    adapter.addItem(((Sensor) intent.getExtras().get(EXTRA_SENSOR)),
                            intent.getFloatArrayExtra(BluetoothLEService.EXTRA_DATA));
                    break;
                default:
                    Log.e(TAG, "invalid broadcast received");
            }
        }
    };


    /**
     *
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /**
     *
     */
    private void clearUI() {
        adapter.setDataList(Collections.<DataListItem>emptyList());
    }

    /**
     * @param savedInstanceState used by OS to save the app state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livedata);


        mLocalBroadcastManager =
                LocalBroadcastManager.getInstance(this);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mLiveDataList = (RecyclerView) findViewById(R.id.data_list);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mLiveDataList.setAdapter(adapter);
        mLiveDataList.setLayoutManager(new LinearLayoutManager(this));


        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setTitle(mDeviceName);
            ab.setDisplayHomeAsUpEnabled(true);
        }
        Intent startintent = new Intent(this, BluetoothLEService.class);
        startService(startintent);
        bindService(startintent, mServiceConnection, 0);
    }

    /**
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        mLocalBroadcastManager.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLEService == null) {
            startService(new Intent(this, BluetoothLEService.class));
        }

        bindService(new Intent(this, BluetoothLEService.class),
                mServiceConnection, BIND_AUTO_CREATE);
    }

    /**
     *
     */
    @Override
    protected void onPause() {
        super.onPause();
        mLocalBroadcastManager.unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
    }

    /**
     *
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /** @param menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    /** @param item */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLEService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLEService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** @param resourceId */
    private void updateConnectionState(final int resourceId) {
        mConnectionState.setText(resourceId);
    }

}





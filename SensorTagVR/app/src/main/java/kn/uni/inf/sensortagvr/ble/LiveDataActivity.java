package kn.uni.inf.sensortagvr.ble;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kn.uni.inf.sensortagvr.R;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLEService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class LiveDataActivity extends Activity {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String TAG = LiveDataActivity.class.getSimpleName();
    LocalBroadcastManager mLocalBroadcastManager;
    private TextView mConnectionState;
    private TextView mDataField0;
    private TextView mDataField1;
    private TextView mDataField2;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
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
         *  set the current {@link BluetoothLEService} instance to null if the service connection
         *  is shutdown
         *
         * @param componentName not used
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLEService = null;
        }
    };
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                /**
                 *
                 * @param parent
                 * @param v
                 * @param groupPosition
                 * @param childPosition
                 * @param id
                 */
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLEService.controlSensor(Sensor.getSensorFromUuid(
                                        mNotifyCharacteristic.getUuid()), true, false);
                                mNotifyCharacteristic = null;
                            }
                        mBluetoothLEService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLEService.controlSensor(Sensor.getSensorFromUuid(
                                    mNotifyCharacteristic.getUuid()), true, true);
                        }
                        return true;
                    }
                    return false;
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
                case BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED:
                    Log.i(TAG, "received GATT Services Discovered");
                    // Show all the supported services and characteristics on the user interface.
                    displayGattServices(mBluetoothLEService.getSupportedGattServices());
                    if (mDeviceName != null && ((mDeviceName.equals("SensorTag2")) ||
                            (mDeviceName.equals("CC2650 SensorTag")))) {
                        for (Sensor s : Sensor.SENSOR_LIST)
                            mBluetoothLEService.controlSensor(s, true, true);
                    }
                    break;
                case BluetoothLEService.ACTION_DATA_AVAILABLE:
                    displayData(intent.getFloatArrayExtra(BluetoothLEService.EXTRA_DATA));
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
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /**
     * 
     */
    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField0.setText(R.string.no_data);
        mDataField1.setText(R.string.no_data);
        mDataField2.setText(R.string.no_data);
    }

    /**
     *
     * @param savedInstanceState used by OS to save the app state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);



        mLocalBroadcastManager =
                LocalBroadcastManager.getInstance(this);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField0 = (TextView) findViewById(R.id.data_value_0);
        mDataField1 = (TextView) findViewById(R.id.data_value_1);
        mDataField2 = (TextView) findViewById(R.id.data_value_2);

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

    /**
     *
     * @param menu
     */
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

    /**
     *
     * @param item
     */
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

    /**
     *
     * @param resourceId
     */
    private void updateConnectionState(final int resourceId) {
        mConnectionState.setText(resourceId);
    }

    /**
     *
     * @param data
     */
    private void displayData(float[] data) {
        if (data != null) {
            mDataField0.setText(String.valueOf(data[0]));
            mDataField0.setText(String.valueOf(data[1]));
            mDataField0.setText(String.valueOf(data[2]));
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.

    /**
     *
     * @param gattServices
     */
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<>();
        mGattCharacteristics = new ArrayList<>();

        // Loops through available GATT Services.
        String LIST_NAME = "NAME";
        String LIST_UUID = "UUID";
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, TIUUIDs.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, TIUUIDs.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }
}



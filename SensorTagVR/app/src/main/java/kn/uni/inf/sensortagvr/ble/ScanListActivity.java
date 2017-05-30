package kn.uni.inf.sensortagvr.ble;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kn.uni.inf.sensortagvr.R;


/**
 *
 */
public class ScanListActivity extends AppCompatActivity {
    private static final long SCAN_PERIOD = 5000;
    static ListView listView;
    private final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private BluetoothLeScanner mLEScanner;
    private boolean mScanning;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private final ScanCallback mScanCallback = new ScanCallback() {
        /**
         * @param callbackType
         * @param result
         */
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            runOnUiThread(new Runnable() {
                /**
                 *
                 */
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(result.getDevice());
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }

        /**
         *
         * @param results
         */
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("onBatchScanResult", sr.toString());
            }
        }

        /**
         *
         * @param errorCode
         */
        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar ab = getActionBar();
        if (ab != null)
            ab.setTitle(R.string.title_devices);

        mHandler = new Handler();

        // BLE available on this device?
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes a Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();


    }

    /**
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter != null && !(mBluetoothAdapter.isEnabled())) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        } else {
            if (Build.VERSION.SDK_INT >= 21 && mBluetoothAdapter != null) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                ScanFilter fi;

                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                        .build();

//                fi = new ScanFilter.Builder()
//                        .setServiceUuid(new ParcelUuid(UUID.fromString(TIUUIDs.UUID_DEVINFO_SERV)))
//                        .build();

                filters = new ArrayList<>();
//                filters.add(fi);
            }

            final Context con = this.getApplicationContext();
            setContentView(R.layout.activity_scanlist);
            listView = (ListView) findViewById(R.id.scanlist);
            mLeDeviceListAdapter = new LeDeviceListAdapter(con);
            if (mLeDeviceListAdapter != null && listView != null) {
                listView.setAdapter(mLeDeviceListAdapter);

                //Adds OnClickListener
                listView.setOnItemClickListener(new ListView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                        if (device == null) return;
                        final Intent intent = new Intent(con, LiveDataActivity.class);
                        intent.putExtra(LiveDataActivity.EXTRAS_DEVICE_NAME, device.getName());
                        intent.putExtra(LiveDataActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                        if (mScanning) {
                            mLEScanner.stopScan(mScanCallback);
                            mScanning = false;
                        }
                        startActivity(intent);
                    }
                });
                scanLeDevice(true);
            }
        }
    }

    /**
     * @param menu The options menu in which you place your items.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    /**
     * @param item The item that the user tapped on.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
            default:
                Log.i("ScanLActivity", "onOptionsItemSel.switch(getItemid):default");
        }
        return true;
    }

    /**
     *
     */
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            //Bluetooth not enabled.
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                /**
                 *
                 */
                @Override
                public void run() {
                    mScanning = false;
                    mLEScanner.stopScan(mScanCallback);

                }
            }, SCAN_PERIOD);
            mScanning = true;
            mLEScanner.startScan(filters, settings, mScanCallback);
        } else {
            mScanning = false;
            mLEScanner.stopScan(mScanCallback);
        }
        invalidateOptionsMenu();
    }


}



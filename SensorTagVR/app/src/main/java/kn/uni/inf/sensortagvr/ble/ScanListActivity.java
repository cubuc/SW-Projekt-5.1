package kn.uni.inf.sensortagvr.ble;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kn.uni.inf.sensortagvr.R;


/**
 *
 */
public class ScanListActivity extends AppCompatActivity {
    private static final long SCAN_PERIOD = 5000;
    private final int REQUEST_ENABLE_BT = 1;
    private BluetoothLeScanner mLEScanner;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private final ScanCallback mScanCallback = new ScanCallback() {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            runOnUiThread(new Runnable() {
                /**
                 * {@inheritDoc}
                 *
                 * adds a device to the recycler view if it was found
                 */
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(result.getDevice());
                }
            });
        }

        /**
         *
         * {@inheritDoc}
         */
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("onBatchScanResult", sr.toString());
                super.onBatchScanResults(results);
            }
        }

        /**
         *
         * {@inheritDoc}
         */
        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
            super.onScanFailed(errorCode);
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar ab = getActionBar();
        if (ab != null)
            ab.setTitle(R.string.title_devices);

        // Sets up UI references.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setContentView(R.layout.activity_scanlist);
        RecyclerView mDeviceList = (RecyclerView) findViewById(R.id.scanlist);
        mDeviceList.setAdapter(mLeDeviceListAdapter);
        mDeviceList.setLayoutManager(new LinearLayoutManager(this));

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
     * {@inheritDoc}
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

                fi = new ScanFilter.Builder()

                        .setDeviceName("CC2650 SensorTag")
                        .build();

                filters = new ArrayList<>();
                filters.add(fi);
            }

                scanLeDevice(true);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    /**
     * {@inheritDoc}
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
     * @param enable if true the scan will start and stop after 5 seconds
     *               if false the scan will stop immediately
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void run() {
                    mLEScanner.stopScan(mScanCallback);

                }
            }, SCAN_PERIOD);
            mLEScanner.startScan(filters, settings, mScanCallback);
        } else {
            mLEScanner.stopScan(mScanCallback);
        }
        invalidateOptionsMenu();
    }


}



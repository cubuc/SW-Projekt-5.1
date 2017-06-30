package kn.uni.inf.sensortagvr;


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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kn.uni.inf.sensortagvr.ble.LeDeviceListAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class BLEScanFragment extends Fragment {
    private static final long SCAN_PERIOD = 5000;
    private final int REQUEST_ENABLE_BT = 1;
    RecyclerView mDeviceList;
    BluetoothLeScanner mLEScanner;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
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
            getActivity().runOnUiThread(new Runnable() {
                /**
                 *
                 */
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(result.getDevice());
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

    public BLEScanFragment() {
        // Required empty public constructor
    }

    /**
     * Called to do initial creation of a fragment.  This is called after
     * {@link #onAttach(Activity)} and before
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
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mHandler = new Handler();

        // BLE available on this device?
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getContext(), "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        // Initializes a Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter != null && !(mBluetoothAdapter.isEnabled())) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Log.i("scanFrag", "finished onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i("scanFrag", "began onCrV");
        View v = inflater.inflate(R.layout.fragment_blescan, container, false);

        // Sets up UI references.
        // TODO Button for scanLeDevice(true);

        mDeviceList = (RecyclerView) v.findViewById(R.id.scanlist);
        mDeviceList.setAdapter(mLeDeviceListAdapter);
        mDeviceList.setLayoutManager(new LinearLayoutManager(getContext()));

        Log.i("scanFrag", "finished on Cre8View");
        return v;
    }

    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to Activity.onPause of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (mScanning) scanLeDevice(false);
        mLeDeviceListAdapter.clear();
        Log.i("scanFrag", "finished onPause");
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to Activity.onResume of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();

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
        Log.i("scanFrag", "finished onResume");
    }

    /**
     * Receive the result from a previous call to
     * {@link #startActivityForResult(Intent, int)}.  This follows the
     * related Activity API as described there in
     * Activity#onActivityResult(int, int, Intent).
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            //Bluetooth not enabled.
            getActivity().finish();
            return;
        }
        Log.i("scanFrag", "finished onActivityResult");

    }

    /**
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        Log.i("scanFrag", "finished entered Scan");
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
    }
}

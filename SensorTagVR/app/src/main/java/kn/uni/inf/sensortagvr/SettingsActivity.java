package kn.uni.inf.sensortagvr;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import kn.uni.inf.sensortagvr.ble.BluetoothLEService;
import kn.uni.inf.sensortagvr.ble.LiveDataActivity;
import kn.uni.inf.sensortagvr.ble.ScanListActivity;
import kn.uni.inf.sensortagvr.tracking.TrackingTestActivity;

/**
 * Activity for control flow reasons: Navigates to the set-ups of the tracking system and the ble set up
 */

public class SettingsActivity extends AppCompatActivity {
    private BluetoothLEService mBluetoothLEService = null;
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
            Log.i(getLocalClassName(), "onServiceConnected");
        }

        /**
         * {@inheritDoc}
         *
         * @param componentName not used
         *                      settings the current {@link BluetoothLEService} instance to null if the service connection
         *                      is shutdown
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLEService = null;
        }
    };
    private boolean mBound = false;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        startService(new Intent(this, BluetoothLEService.class));
        if (!mBound) {
            bindService(new Intent(this, BluetoothLEService.class), mServiceConnection, BIND_AUTO_CREATE);
            mBound = true;
        }

        Button trackButton = (Button) findViewById(R.id.button_track);
        trackButton.setOnClickListener(new View.OnClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), TrackingTestActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            }
        });


        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View v) {
                if (mBluetoothLEService.getmGatt() == null) {
                    if (mBound) {
                        unbindService(mServiceConnection);
                        mBound = false;
                    }
                    mBluetoothLEService = null;
                    startActivity(new Intent(getApplicationContext(),
                            ScanListActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                } else {
                    if (mBound) {
                        unbindService(mServiceConnection);
                        mBound = false;
                    }
                    startActivity(new Intent(getApplicationContext(),
                            LiveDataActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothLEService != null && mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
    }
}

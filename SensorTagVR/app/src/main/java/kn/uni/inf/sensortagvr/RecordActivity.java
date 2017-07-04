package kn.uni.inf.sensortagvr;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import kn.uni.inf.sensortagvr.stor.StorageMainService;
import kn.uni.inf.sensortagvr.tracking.TrackingManagerService;

/**
 * Activity that binds to tracking and storage service to calibrate the start position, record data
 * and view them in the WebVR environment via a web browser
 */

public class RecordActivity extends Activity {

    StorageMainService storageService;
    TrackingManagerService trackingService;

    private ServiceConnection storageConnection = new ServiceConnection() {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StorageMainService.StorageBinder binder = (StorageMainService.StorageBinder) service;
            storageService = binder.getService();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            storageService = null;
        }
    };

    private ServiceConnection trackingConnection = new ServiceConnection() {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TrackingManagerService.TrackingBinder binder = (TrackingManagerService.TrackingBinder) service;
            trackingService = binder.getService();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            trackingService = null;
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        final Button caliButton = (Button) findViewById(R.id.calibrate);
        caliButton.setOnClickListener(new View.OnClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View v) {
                if (trackingService != null)
                    try {
                        trackingService.calibrateOrigin();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        });


        final Button buttonMD = (Button) findViewById(R.id.measure);
        buttonMD.setOnClickListener(new View.OnClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View v) {
                if (storageService != null)
                    storageService.measureData();
                else
                    Toast.makeText(getApplicationContext(), "StorageService not connected", Toast.LENGTH_SHORT).show();
            }
        });

        final Button VRbutton = (Button) findViewById(R.id.start_VR);
        VRbutton.setOnClickListener(new View.OnClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View v) {
                String url = "http://localhost:12345";
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setPackage("com.android.chrome");
                startActivity(i);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();

        bindService(new Intent(this, StorageMainService.class), storageConnection, 0);
        bindService(new Intent(this, TrackingManagerService.class), trackingConnection, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        super.onPause();
        unbindService(storageConnection);
        unbindService(trackingConnection);
    }
}

package kn.uni.inf.sensortagvr;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import kn.uni.inf.sensortagvr.stor.StorageMainService;

/**
 * Activity that binds to tracking and storage service to calibrate the start position, record data
 * and view them in the WebVR environment via a web browser
 */

public class RecordActivity extends Activity {

    private StorageMainService storageService;

    private final ServiceConnection storageConnection = new ServiceConnection() {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StorageMainService.StorageBinder binder = (StorageMainService.StorageBinder) service;
            storageService = binder.getService();
            Log.i(getLocalClassName(), "connected to stor svc");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            storageService = null;
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        bindService(new Intent(this, StorageMainService.class), storageConnection, BIND_AUTO_CREATE);
        Log.i(getLocalClassName(), "bound Stor Svc");

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

        final Button vrButton = (Button) findViewById(R.id.start_VR);
        vrButton.setOnClickListener(new View.OnClickListener() {
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
    protected void onPause() {
        Log.i(getLocalClassName(), "unbound Stor Svc");
        unbindService(storageConnection);
        super.onPause();
    }
}

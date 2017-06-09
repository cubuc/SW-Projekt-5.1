package kn.uni.inf.sensortagvr;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import kn.uni.inf.sensortagvr.stor.StorageMainService;

/**
 * Created by lisa-maria on 21.05.17.
 */

public class RecordActivity extends AppCompatActivity {

    StorageMainService storageService;
    boolean storageServiceBound = false;
    private ServiceConnection storageConnection = new ServiceConnection() {
        /**
         * @param name
         * @param service
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StorageMainService.StorageBinder binder = (StorageMainService.StorageBinder) service;
            storageService = binder.getService();
            storageServiceBound = true;
        }

        /**
         *
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            storageServiceBound = false;
        }
    };

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Intent bindStorService = new Intent(this, StorageMainService.class);
        bindService(bindStorService, storageConnection, Context.BIND_AUTO_CREATE);

        final Button buttonCal = (Button) findViewById(R.id.callibrate);
        buttonCal.setOnClickListener(new View.OnClickListener() {
            /**
             *
             * @param v
             */
            public void onClick(View v) {
                if (storageServiceBound) {
                }
                //storageService.calibrate();
                else
                    Toast.makeText(getApplicationContext(), "StorageService not connected", Toast.LENGTH_SHORT).show();
            }
        });

        final Button buttonMD = (Button) findViewById(R.id.measure);
        buttonMD.setOnClickListener(new View.OnClickListener() {
            /**
             *
             * @param v
             */
            public void onClick(View v) {
                if (storageServiceBound)
                    storageService.measureData();
                else
                    Toast.makeText(getApplicationContext(), "StorageService not connected", Toast.LENGTH_SHORT).show();
            }
        });

        final Button writeButton = (Button) findViewById(R.id.write);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                storageService.closeMeasureSession();
                Toast.makeText(getApplicationContext(), "Write", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(storageConnection);
        stopService(new Intent(this, StorageMainService.class));
    }
}



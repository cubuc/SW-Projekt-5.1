package kn.uni.inf.sensortagvr;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import kn.uni.inf.sensortagvr.stor.StorageMainService;

/**
 * Created by lisa-maria on 21.05.17.
 */

public class RecordActivity extends Activity {

    StorageMainService storageService;
    boolean storageServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Intent bindStorService = new Intent(this, StorageMainService.class);
        bindService(bindStorService, storageConnection, Context.BIND_AUTO_CREATE);

        final Button buttonCal = (Button) findViewById(R.id.callibrate);
        buttonCal.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (storageServiceBound)
                    storageService.calibrate();
                else
                    Toast.makeText(getApplicationContext(), "StorageService not connected", Toast.LENGTH_SHORT).show();
            }
        });

        final Button buttonMD = (Button) findViewById(R.id.measure);
        buttonMD.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (storageServiceBound)
                    storageService.measureData();
                else
                    Toast.makeText(getApplicationContext(), "StorageService not connected", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private ServiceConnection storageConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StorageMainService.StorageBinder binder = (StorageMainService.StorageBinder) service;
            storageService = binder.getService();
            storageServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            storageServiceBound = false;
        }
    };

}

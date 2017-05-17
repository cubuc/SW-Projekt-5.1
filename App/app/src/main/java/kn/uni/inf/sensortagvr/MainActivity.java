package com.example.gero.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import kn.uni.inf.sensortagvr.ble.BluetoothLowEnergyService;

public class MainActivity extends AppCompatActivity {
    LocalBroadcastManager mLBM;
    Context con;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        con = getApplicationContext();
        mLBM = LocalBroadcastManager.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //startService(new Intent(this, BluetoothLowEnergyService.class));

        mLBM.registerReceiver(scanAndConnectTest, makeFilter());

        final Button button = (Button) findViewById(R.id.startVR);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://localhost:12345";
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setPackage("com.android.chrome");
                startActivity(i);
            }
        });

        final Button mButton = (Button) findViewById(R.id.settings);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(con, "Req Sent", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(BluetoothLowEnergyService.ACTION_START_SCAN);
                mLBM.sendBroadcast(intent);
            }
        });
    }


    private static IntentFilter makeFilter() {
        final IntentFilter fi = new IntentFilter();
        fi.addAction(BluetoothLowEnergyService.ACTION_SCAN_STARTED);
        fi.addAction(BluetoothLowEnergyService.ACTION_DEVICE_FOUND);
        fi.addAction(BluetoothLowEnergyService.ACTION_GATT_CONNECTED);
        return fi;
    }

    BroadcastReceiver scanAndConnectTest = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothLowEnergyService.ACTION_SCAN_STARTED:

                    Toast.makeText(con, "The scan has started", Toast.LENGTH_SHORT).show();

                    break;
                case BluetoothLowEnergyService.ACTION_DEVICE_FOUND:

                    Toast.makeText(con, "Found Device", Toast.LENGTH_SHORT).show();

                    Intent mIntent = new Intent(BluetoothLowEnergyService.ACTION_DEVICE_CONNECT);
                    mIntent.putExtra(BluetoothLowEnergyService.EXTRA_ADDRESS,
                            intent.getStringExtra(BluetoothLowEnergyService.EXTRA_ADDRESS));
                    mLBM.sendBroadcast(intent);
                    break;
                case BluetoothLowEnergyService.ACTION_GATT_CONNECTED:
                    final Context con = getApplicationContext();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(con, "Connection established", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case BluetoothLowEnergyService.ACTION_GATT_DISCONNECTED:
                case BluetoothLowEnergyService.ACTION_GATT_SERVICES_DISCOVERED:
                case BluetoothLowEnergyService.ACTION_DATA_AVAILABLE:
                    break;
            }
        }
    };
}

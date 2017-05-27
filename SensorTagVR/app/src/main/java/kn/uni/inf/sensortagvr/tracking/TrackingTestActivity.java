package kn.uni.inf.sensortagvr.tracking;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kn.uni.inf.sensortagvr.R;
import kn.uni.inf.sensortagvr.tracking.TrackingManagerService.TrackingBinder;

public class TrackingTestActivity extends AppCompatActivity {

    public final int FINE_LOCATION_PERMISSION_REQUEST = 0;

    private final double n = 3.5;

    TrackingManagerService mService = null;
    boolean mBound = false;
    WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_test);

        wifiManager = (WifiManager) getApplication().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        final Handler handler = new Handler();
        class LocationUpdater implements Runnable {
            private Handler handler;
            private TextView textView;
            private ListView list;
            private WifiManager wifiManager;
            private List<String> data = new ArrayList<String>();

            public LocationUpdater(Handler handler, TextView textView, ListView list, WifiManager wifiManager) {
                this.handler = handler;
                this.textView = textView;
                this.list = list;
                this.wifiManager = wifiManager;
            }
            @Override
            public void run() {
                this.handler.postDelayed(this, 500);

                if(mService != null) {
                    this.textView.setText(mService.getCurrentPosition().toString() + " " + mService.getCurrentPosition().getProvider());
                } else {
                    this.textView.setText("---");
                }

                List<ScanResult> results =  wifiManager.getScanResults();
                data.clear();
                for(ScanResult r : results) {
                    double d = Math.pow(10.0, (-50.0 - r.level) / (10.0 * n));
                    //double d = Math.pow(10.0, (r.level - 20.0 * Math.log10(4*Math.PI / 0.125)) / (10.0 * n));

                    data.add(r.SSID + "\n" + r.BSSID + "\nDistance:" + d);
                }

                list.setAdapter(new ArrayAdapter<String>(TrackingTestActivity.this,
                        android.R.layout.simple_list_item_1, data));
            }
        }
        handler.post(new LocationUpdater(handler, (TextView) findViewById(R.id.location), (ListView) findViewById(R.id.wifiList), wifiManager));
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Check for permissions and ask for them if need be
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            /*if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                finish();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        FINE_LOCATION_PERMISSION_REQUEST);
            }*/

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOCATION_PERMISSION_REQUEST);
        }

        Intent intent = new Intent(this, TrackingManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Log.e("TarckingManager", "Shutting down due to missing permissions!");
                    finish();
                }
                return;
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            TrackingBinder binder = (TrackingBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}

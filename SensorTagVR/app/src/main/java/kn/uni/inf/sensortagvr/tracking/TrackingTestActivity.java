package kn.uni.inf.sensortagvr.tracking;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Comparator;

import kn.uni.inf.sensortagvr.R;
import kn.uni.inf.sensortagvr.tracking.TrackingManagerService.TrackingBinder;

/**
 *
 */
public class TrackingTestActivity extends AppCompatActivity {

    public final int FINE_LOCATION_PERMISSION_REQUEST = 0;
    public final int AP_SETTINGS_REQUEST = 1;
    protected ListView lv;
    private TrackingManagerService mService = null;
    private boolean mBound = false;
    private WifiTracker wifiTracker;
    private ServiceConnection mConnection = new ServiceConnection() {

        /**
         * @param className
         * @param service
         */
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            TrackingBinder binder = (TrackingBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        /**
         * @param arg0
         */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_test);

        lv = (ListView) findViewById(R.id.wifiList);

        wifiTracker = new WifiTracker((WifiManager) getApplication().getApplicationContext().getSystemService(Context.WIFI_SERVICE));

        lv.setClickable(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             *
             * @param arg0
             * @param arg1
             * @param position
             * @param arg3
             */
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                WifiAP ap = (WifiAP) lv.getItemAtPosition(position);

                Intent intent = new Intent(getApplicationContext(), APSettingsActivity.class);
                intent.putExtra("ACCESS_POINT", ap);
                startActivityForResult(intent, AP_SETTINGS_REQUEST);
            }
        });

        FileInputStream inputStream;
        try {
            inputStream = openFileInput("AP_CONFIG.json");
            wifiTracker.readFromFile(inputStream);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
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

        final Handler handler = new Handler();
        /**
         *
         */
        class LocationUpdater implements Runnable {
            private Handler handler;
            private TextView textView;
            private ListView list;
            private WifiTracker wifiTracker;
            private WifiAPAdapter adapter;

            /**
             *
             * @param handler
             * @param textView
             * @param list
             * @param wifiTracker
             */
            public LocationUpdater(Handler handler, TextView textView, ListView list, WifiTracker wifiTracker) {
                this.handler = handler;
                this.textView = textView;
                this.list = list;
                this.wifiTracker = wifiTracker;

                adapter = new WifiAPAdapter(TrackingTestActivity.this, new ArrayList<WifiAP>());
                list.setAdapter(adapter);
            }

            /**
             *
             */
            @Override
            public void run() {
                this.handler.postDelayed(this, 500);

                if(mService != null) {
                    this.textView.setText(mService.getRelativePosition().toString());
                } else {
                    this.textView.setText("---");
                }

                adapter.clear();
                adapter.addAll(wifiTracker.getWifiAPs(true));
                adapter.sort(new Comparator<WifiAP>() {
                    /**
                     *
                     * @param lhs
                     * @param rhs
                     */
                    @Override
                    public int compare(WifiAP lhs, WifiAP rhs) {
                        // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                        if(lhs.isTracked()) {
                            if(rhs.isTracked())
                                return 0;
                            else
                                return -1;
                        }
                        else {
                            if(rhs.isTracked())
                                return 1;
                            else
                                return 0;
                        }
                    }
                });
            }
        }
        handler.post(new LocationUpdater(handler, (TextView) findViewById(R.id.location), lv, wifiTracker));
    }

    /**
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     *
     */
    protected void onPause() {
        super.onPause();

        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput("AP_CONFIG.json", Context.MODE_PRIVATE);
            wifiTracker.writeToFile(outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AP_SETTINGS_REQUEST) {
            if (resultCode == RESULT_OK) {
                WifiAP ap = data.getParcelableExtra("ACCESS_POINT");
                wifiTracker.trackAP(ap);

                if(ap.isTracked())
                    Toast.makeText(getApplicationContext(), "Now tracking:\n" + ap.toString(), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Stopped tracking:\n" + ap.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 
     */
    public class WifiAPAdapter extends ArrayAdapter<WifiAP> {

        /**
         *
         * @param context
         * @param aps
         */
        public WifiAPAdapter(Context context, ArrayList<WifiAP> aps) {
            super(context, 0, aps);
        }

        /**
         *
         * @param position
         * @param convertView
         * @param parent
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            WifiAP ap = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            TextView text = (TextView) convertView;

            if(ap.isTracked())
                text.setTextColor(Color.RED);
            else
                text.setTextColor(Color.BLACK);
            text.setText(ap.toString());

            return convertView;
        }
    }
}



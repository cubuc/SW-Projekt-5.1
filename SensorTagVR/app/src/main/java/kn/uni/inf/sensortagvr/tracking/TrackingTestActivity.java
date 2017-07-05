package kn.uni.inf.sensortagvr.tracking;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;

import kn.uni.inf.sensortagvr.R;
import kn.uni.inf.sensortagvr.tracking.TrackingManagerService.TrackingBinder;

public class TrackingTestActivity extends AppCompatActivity {

    private final int AP_SETTINGS_REQUEST = 1;
    protected ListView lv;
    private TrackingManagerService trackingService = null;
    private ServiceConnection mConnection = new ServiceConnection() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            TrackingBinder binder = (TrackingBinder) service;
            trackingService = binder.getService();

            final Handler handler = new Handler();
            handler.post(new LocationUpdater(handler, (TextView) findViewById(R.id.location), lv, trackingService));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            trackingService = null;
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_test);

        lv = (ListView) findViewById(R.id.wifiList);

        lv.setClickable(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                WifiAP ap = (WifiAP) lv.getItemAtPosition(position);

                Intent intent = new Intent(getApplicationContext(), APSettingsActivity.class);
                intent.putExtra("ACCESS_POINT", ap);
                startActivityForResult(intent, AP_SETTINGS_REQUEST);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, TrackingManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        stopService(new Intent(this, TrackingManagerService.class));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AP_SETTINGS_REQUEST) {
            if (resultCode == RESULT_OK) {
                WifiAP ap = data.getParcelableExtra("ACCESS_POINT");
                trackingService.trackAP(ap);

                if (ap.isTracked())
                    Toast.makeText(getApplicationContext(), "Now tracking:\n" + ap.toString(), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Stopped tracking:\n" + ap.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class WifiAPAdapter extends ArrayAdapter<WifiAP> {

        WifiAPAdapter(Context context, ArrayList<WifiAP> aps) {
            super(context, 0, aps);
        }

        /**
         * {@inheritDoc}
         */
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            WifiAP ap = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            TextView text = (TextView) convertView;

            if (ap != null) {
                if (ap.isTracked())
                    text.setTextColor(Color.RED);
                else
                    text.setTextColor(Color.BLACK);
                text.setText(ap.toString());
            }
            return convertView;
        }
    }

    private class LocationUpdater implements Runnable {
        private Handler handler;
        private TextView textView;
        private WifiAPAdapter adapter;
        private TrackingManagerService trackingService;

        LocationUpdater(Handler handler, TextView textView, ListView list, TrackingManagerService trackingService) {
            this.handler = handler;
            this.textView = textView;
            this.trackingService = trackingService;

            adapter = new WifiAPAdapter(TrackingTestActivity.this, new ArrayList<WifiAP>());
            list.setAdapter(adapter);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            this.handler.postDelayed(this, 500);

            if (TrackingTestActivity.this.trackingService != null) {
                this.textView.setText(TrackingTestActivity.this.trackingService.getRelativePosition().toString());
            } else {
                this.textView.setText("---");
            }

            adapter.clear();
            adapter.addAll(trackingService.getWifiAPs());
            adapter.sort(new Comparator<WifiAP>() {
                @Override
                public int compare(WifiAP lhs, WifiAP rhs) {
                    // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                    if (lhs.isTracked()) {
                        if (rhs.isTracked())
                            return 0;
                        else
                            return -1;
                    } else {
                        if (rhs.isTracked())
                            return 1;
                        else
                            return 0;
                    }
                }
            });
        }
    }
}
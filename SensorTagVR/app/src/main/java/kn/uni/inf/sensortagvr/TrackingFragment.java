package kn.uni.inf.sensortagvr;

/**
 * Created by Lisa-Maria on 03/07/2017.
 */

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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

import kn.uni.inf.sensortagvr.tracking.APSettingsActivity;
import kn.uni.inf.sensortagvr.tracking.TrackingManagerService;
import kn.uni.inf.sensortagvr.tracking.WifiAP;

import static android.app.Activity.RESULT_OK;

public class TrackingFragment extends Fragment {

    public final int FINE_LOCATION_PERMISSION_REQUEST = 0;
    public final int AP_SETTINGS_REQUEST = 1;

    private TrackingManagerService trackingService = null;
    private boolean mBound = false;

    protected ListView lv;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tracking, container, false);

        lv = (ListView) view.findViewById(R.id.wifiList);

        lv.setClickable(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                WifiAP ap = (WifiAP) lv.getItemAtPosition(position);

                Intent intent = new Intent(getActivity(), APSettingsActivity.class);
                intent.putExtra("ACCESS_POINT", ap);
                startActivityForResult(intent, AP_SETTINGS_REQUEST);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        //Check for permissions and ask for them if need be
        if (ContextCompat.checkSelfPermission(getActivity(),
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

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOCATION_PERMISSION_REQUEST);
        }

        Intent intent = new Intent(getActivity(), TrackingManagerService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    public void onResume() { super.onResume(); }

    public void onPause() {
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Log.e("TrackingManager", "Shutting down due to missing permissions!");
                    getActivity().finish();
                }
                return;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AP_SETTINGS_REQUEST) {
            if (resultCode == RESULT_OK) {
                WifiAP ap = data.getParcelableExtra("ACCESS_POINT");
                trackingService.trackAP(ap);

                if(ap.isTracked())
                    Toast.makeText(getActivity(), "Now tracking:\n" + ap.toString(), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "Stopped tracking:\n" + ap.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            TrackingManagerService.TrackingBinder binder = (TrackingManagerService.TrackingBinder) service;
            trackingService = binder.getService();
            mBound = true;

            final Handler handler = new Handler();
            handler.post(new TrackingFragment.LocationUpdater(handler, (TextView) view.findViewById(R.id.location), lv, trackingService));
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public class WifiAPAdapter extends ArrayAdapter<WifiAP> {

        public WifiAPAdapter(Context context, ArrayList<WifiAP> aps) {
            super(context, 0, aps);
        }

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

    class LocationUpdater implements Runnable {
        private Handler handler;
        private TextView textView;
        private TrackingFragment.WifiAPAdapter adapter;
        private TrackingManagerService trackingService;

        public LocationUpdater(Handler handler, TextView textView, ListView list, TrackingManagerService trackingService) {
            this.handler = handler;
            this.textView = textView;
            this.trackingService = trackingService;

            adapter = new TrackingFragment.WifiAPAdapter(getActivity(), new ArrayList<WifiAP>());
            list.setAdapter(adapter);
        }
        @Override
        public void run() {
            this.handler.postDelayed(this, 500);

            if(TrackingFragment.this.trackingService != null) {
                this.textView.setText(TrackingFragment.this.trackingService.getRelativePosition().toString());
            } else {
                this.textView.setText("---");
            }

            adapter.clear();
            adapter.addAll(trackingService.getWifiAPs());
            adapter.sort(new Comparator<WifiAP>() {
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
}

package kn.uni.inf.sensortagvr;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import kn.uni.inf.sensortagvr.ble.ScanListActivity;
import kn.uni.inf.sensortagvr.tracking.TrackingTestActivity;

/**
 * Activity for control flow reasons: Navigates to the set-ups of the tracking system and the ble set up
 */

public class SettingsActivity extends AppCompatActivity {
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button trackButton = (Button) findViewById(R.id.button_track);
        trackButton.setOnClickListener(new View.OnClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View v) {
                getApplicationContext().startActivity(new Intent(getApplicationContext(), TrackingTestActivity.class));
            }
        });
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View v) {
                getApplicationContext().startActivity(new Intent(getApplicationContext(), ScanListActivity.class));
            }
        });
    }
}

package kn.uni.inf.sensortagvr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import kn.uni.inf.sensortagvr.ble.ScanListActivity;

/**
 *
 */
public class MainActivity extends AppCompatActivity {

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Context con = getApplicationContext();


        final Button VRButton = (Button) findViewById(R.id.vr_view);
        VRButton.setOnClickListener(new View.OnClickListener() {
            /**
             * @param v
             */
            @Override
            public void onClick(View v) {
                startActivity(new Intent(con, VRActivity.class));
            }
        });


        final Button recordButton = (Button) findViewById(R.id.record);
        recordButton.setOnClickListener(new View.OnClickListener() {
            /**
             *
             * @param v
             */
            @Override
            public void onClick(View v) {
                startActivity(new Intent(con, RecordActivity.class));
            }
        });


        final Button settingsButton = (Button) findViewById(R.id.settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            /**
             *
             * @param v
             */
            @Override
            public void onClick(View v) {
                startActivity(new Intent(con, ScanListActivity.class));
            }
        });
    }
}



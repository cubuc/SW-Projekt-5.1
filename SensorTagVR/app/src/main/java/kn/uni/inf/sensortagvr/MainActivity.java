package kn.uni.inf.sensortagvr;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import kn.uni.inf.sensortagvr.ble.ScanListActivity;
import kn.uni.inf.sensortagvr.gui_tab_management.TabSample;
import kn.uni.inf.sensortagvr.tracking.TrackingTestActivity;

public class MainActivity extends AppCompatActivity {

    public static int firstActiveTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button recButt = (Button) findViewById(R.id.recordButton);
        Button vrButt = (Button) findViewById(R.id.vrButton);
        Button setButt = (Button) findViewById(R.id.settingsButton);

        recButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstActiveTab = 0;
                Intent myIntent = new Intent(MainActivity.this,
                        TabSample.class);
                startActivity(myIntent);
            }
        });

        vrButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstActiveTab = 1;
                Intent myIntent = new Intent(MainActivity.this,
                        TabSample.class);
                startActivity(myIntent);
            }
        });

        setButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstActiveTab = 2;
                Intent myIntent = new Intent(MainActivity.this,
                        TabSample.class);
                startActivity(myIntent);
            }
        });

    }
}


/*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Context con = getApplicationContext();


        final Button VRButton = (Button) findViewById(R.id.vr_view);
        VRButton.setOnClickListener(new View.OnClickListener() {
*/
            /**
             * @param v
             */
 /*           @Override
            public void onClick(View v) {
                startActivity(new Intent(con, VRActivity.class));
            }
        });


        final Button recordButton = (Button) findViewById(R.id.record);
        recordButton.setOnClickListener(new View.OnClickListener() {
 */
            /**
             *
             * @param v
             */
 /*           @Override
            public void onClick(View v) {
                startActivity(new Intent(con, RecordActivity.class));
            }
        });


        final Button settingsButton = (Button) findViewById(R.id.settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
 */
            /**
             *
             * @param v
             */
 /*           @Override
            public void onClick(View v) {
                startActivity(new Intent(con, ScanListActivity.class));
            }
        });

        final Button locationButton = (Button) findViewById(R.id.location);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(con, TrackingTestActivity.class));
            }
        });
    }
}
*/



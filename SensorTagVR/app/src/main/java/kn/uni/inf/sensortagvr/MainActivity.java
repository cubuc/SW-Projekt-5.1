package kn.uni.inf.sensortagvr;

/**
 * Created by Lisa-Maria on 24/06/2017.
 */


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import kn.uni.inf.sensortagvr.ble.ScanListActivity;
import kn.uni.inf.sensortagvr.stor.RecordActivity;

public class MainActivity extends AppCompatActivity {

    public static int firstActiveTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ImageButton recButt = (ImageButton) findViewById(R.id.recordButton);
        ImageButton vrButt = (ImageButton) findViewById(R.id.vrButton);
        ImageButton setButt = (ImageButton) findViewById(R.id.settingsButton);


        vrButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getApplicationContext().startActivity(new Intent(getApplicationContext(), VRActivity.class));
            }
        });

        recButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getApplicationContext().startActivity(new Intent(getApplicationContext(), RecordActivity.class));
            }
        });


        setButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getApplicationContext().startActivity(new Intent(getApplicationContext(), ScanListActivity.class));
            }
        });

    }
}
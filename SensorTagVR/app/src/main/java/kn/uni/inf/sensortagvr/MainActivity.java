package kn.uni.inf.sensortagvr;

/**
 * Created by Lisa-Maria on 24/06/2017.
 */


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import kn.uni.inf.sensortagvr.ble.LiveDataActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ImageButton vrButton = (ImageButton) findViewById(R.id.vrButton);
        ImageButton mLiveDataButton = (ImageButton) findViewById(R.id.LiveDataButton);
        ImageButton setButton = (ImageButton) findViewById(R.id.settingsButton);


        vrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getApplicationContext().startActivity(new Intent(getApplicationContext(), RecordActivity.class));
            }
        });

        mLiveDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getApplicationContext().startActivity(new Intent(getApplicationContext(), LiveDataActivity.class));
            }
        });


        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getApplicationContext().startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });

    }
}
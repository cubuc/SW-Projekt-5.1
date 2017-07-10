package kn.uni.inf.sensortagvr;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import kn.uni.inf.sensortagvr.ble.LiveDataActivity;

public class MainActivity extends AppCompatActivity {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton vrButton = (ImageButton) findViewById(R.id.vrButton);
        ImageButton mLiveDataButton = (ImageButton) findViewById(R.id.LiveDataButton);
        ImageButton setButton = (ImageButton) findViewById(R.id.settingsButton);


        vrButton.setOnClickListener(new View.OnClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View v) {
                getApplicationContext().startActivity(new Intent(getApplicationContext(), RecordActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        mLiveDataButton.setOnClickListener(new View.OnClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View v) {
                getApplicationContext().startActivity(new Intent(getApplicationContext(), LiveDataActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        setButton.setOnClickListener(new View.OnClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View v) {
                getApplicationContext().startActivity(new Intent(getApplicationContext(), SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

    }
}
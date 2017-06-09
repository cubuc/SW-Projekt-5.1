package kn.uni.inf.sensortagvr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import kn.uni.inf.sensortagvr.ble.ScanListActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        startActivity(new Intent(this, ScanListActivity.class));
    }

}

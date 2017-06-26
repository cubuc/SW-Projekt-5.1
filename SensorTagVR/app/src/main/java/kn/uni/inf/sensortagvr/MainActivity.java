package kn.uni.inf.sensortagvr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import kn.uni.inf.sensortagvr.gui_tab_management.TabActivity;

public class MainActivity extends AppCompatActivity {

    public static int firstActiveTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent startTabs = new Intent(MainActivity.this, TabActivity.class);

        ImageButton recButt = (ImageButton) findViewById(R.id.recordButton);
        ImageButton vrButt = (ImageButton) findViewById(R.id.vrButton);
        ImageButton setButt = (ImageButton) findViewById(R.id.settingsButton);

        recButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstActiveTab = 0;
                startActivity(startTabs);
            }
        });

        vrButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstActiveTab = 1;
                startActivity(startTabs);
            }
        });

        setButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstActiveTab = 2;
                startActivity(startTabs);
            }
        });

    }
}
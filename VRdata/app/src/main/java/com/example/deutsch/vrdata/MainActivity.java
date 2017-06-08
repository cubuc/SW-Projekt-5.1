package com.example.deutsch.vrdata;

/**
 * Created by Lisa-Maria on 06/06/2017.
 *
 * Defines the main activity. This appears right after the loading screen and offers the user to choose the ctivits they want to start next.
 * When one of the buttons is clicked, the activity TabSample opens the chosen tab.
 *
 * Icons from https://icons8.com/download-huge-windows8-set.
 */

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static com.example.deutsch.vrdata.R.id.recordButton;
import static com.example.deutsch.vrdata.R.id.settingsButton;
import static com.example.deutsch.vrdata.R.id.vrButton;

public class MainActivity extends AppCompatActivity {

    public static int firstActiveTab=0;

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
                firstActiveTab=0;
                Intent myIntent = new Intent(MainActivity.this,
                        TabSample.class);
                startActivity(myIntent);
            }
        });

        vrButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstActiveTab=1;
                Intent myIntent = new Intent(MainActivity.this,
                        TabSample.class);
                startActivity(myIntent);
            }
        });

        setButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstActiveTab=2;
                Intent myIntent = new Intent(MainActivity.this,
                        TabSample.class);
                startActivity(myIntent);
            }
        });

    }
}

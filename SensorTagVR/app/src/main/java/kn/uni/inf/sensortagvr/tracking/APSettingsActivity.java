package kn.uni.inf.sensortagvr.tracking;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import kn.uni.inf.sensortagvr.R;

/**
 *
 */
public class APSettingsActivity extends AppCompatActivity {

    private WifiAP wifiAP;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apsettings);

        wifiAP = getIntent().getParcelableExtra("ACCESS_POINT");

        ((TextView) findViewById(R.id.ssid)).setText(wifiAP.getSSID());
        ((TextView) findViewById(R.id.bssid)).setText(wifiAP.getBSSID());

        ((EditText) findViewById(R.id.posX)).setText(Double.toString(wifiAP.getLocation().x));
        ((EditText) findViewById(R.id.posY)).setText(Double.toString(wifiAP.getLocation().y));

        ((EditText) findViewById(R.id.varA)).setText(Double.toString(wifiAP.getA()));
        ((EditText) findViewById(R.id.varN)).setText(Double.toString(wifiAP.getN()));

        final Button trackButton = (Button) findViewById(R.id.track);
        trackButton.setOnClickListener(new View.OnClickListener() {
            /**
             * @param v
             */
            @Override
            public void onClick(View v) {
                end(true);
            }
        });

        final Button untrackButton = (Button) findViewById(R.id.untrack);
        untrackButton.setOnClickListener(new View.OnClickListener() {
            /**
             *
             * @param v
             */
            @Override
            public void onClick(View v) {
                end(false);
            }
        });
    }

    /**
     *
     * @param track
     */
    private void end(boolean track) {
        PointF loc = new PointF(Float.parseFloat( ((EditText) findViewById(R.id.posX)).getText().toString() ),
                Float.parseFloat( ((EditText) findViewById(R.id.posY)).getText().toString() ));

        WifiAP newAP = new WifiAP(wifiAP.getSSID(),
                wifiAP.getBSSID(),
                loc,
                Double.parseDouble( ((EditText) findViewById(R.id.varA)).getText().toString() ),
                Double.parseDouble( ((EditText) findViewById(R.id.varN)).getText().toString() ),
                track);

        Intent intent = new Intent();
        intent.putExtra("ACCESS_POINT", newAP);
        setResult(RESULT_OK , intent);
        finish();
    }
}


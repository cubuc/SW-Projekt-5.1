package kn.uni.inf.sensortagvr;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import kn.uni.inf.sensortagvr.ble.ScanListActivity;
import kn.uni.inf.sensortagvr.MainActivity;


/**
 * Created by lisa-maria on 21.05.17.
 */

public class VRActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr_view);
        final Context con = getApplicationContext();


        final Button VRbutton = (Button) findViewById(R.id.start_VR);
        VRbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://localhost:12345";
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setPackage("com.android.chrome");
                startActivity(i);
            }
        });


    }
}
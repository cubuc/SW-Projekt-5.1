package kn.uni.inf.sensortagvr;

import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import kn.uni.inf.sensortagvr.stor.NewSessionDialog;

/**
 * Created by gero on 05.07.17.
 */

public class RecordActivity extends FragmentActivity implements NewSessionDialog.NoticeDialogListener {

    Intent start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        start = new Intent(this, SessionActivity.class);

        final Button cont = (Button) findViewById(R.id.continueSession);
        cont.setOnClickListener(new View.OnClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View v) {
                getApplicationContext().startActivity(new Intent(getApplicationContext(), SessionActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("cont", true));
            }
        });

        final Button newSession = (Button) findViewById(R.id.newSession);
        newSession.setOnClickListener(new View.OnClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View v) {
                DialogFragment dialog = new NewSessionDialog();
                dialog.show(getFragmentManager(), "dialog");
            }
        });

        final Button vrButton = (Button) findViewById(R.id.start_VR);
        vrButton.setOnClickListener(new View.OnClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View v) {
                String url = "http://kim25.wwwdns.kim.uni-konstanz.de/sp2017_5_1/index.html";
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setPackage("org.mozilla.firefox");
                startActivity(i);
            }
        });

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        getApplicationContext().startActivity((new Intent(getApplicationContext(), SessionActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)).putExtra("cont", false));
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

}
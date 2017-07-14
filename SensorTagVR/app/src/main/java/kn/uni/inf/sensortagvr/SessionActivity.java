package kn.uni.inf.sensortagvr;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

import kn.uni.inf.sensortagvr.stor.StorageMainService;
import kn.uni.inf.sensortagvr.stor.UnsavedDataDialog;

/**
 *
 * Created by gero on 06.07.17.
 */

public class SessionActivity extends Activity implements UnsavedDataDialog.NoticeDialogListener {

    public boolean unsavedChanges = false;
    private StorageMainService storageService;
    private final ServiceConnection storageConnection = new ServiceConnection() {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StorageMainService.StorageBinder binder = (StorageMainService.StorageBinder) service;
            storageService = binder.getService();
            Log.i(getLocalClassName(), "connected to stor svc");

            if (getIntent().getBooleanExtra("cont", true) || unsavedChanges) {
                try{
                    storageService.continueSession();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Failed to continue Session", Toast.LENGTH_SHORT);
                    finish();
                }

            } else {
                storageService.createNewSession();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            storageService = null;
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        final Button buttonMD = (Button) findViewById(R.id.measure);
        buttonMD.setOnClickListener(new View.OnClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View v) {
                if (storageService != null) {
                    storageService.measureData();
                    unsavedChanges = true;
                } else
                    Toast.makeText(getApplicationContext(), "StorageService not connected", Toast.LENGTH_SHORT).show();
            }
        });

        final Button quit = (Button) findViewById(R.id.quit);
        quit.setOnClickListener(new View.OnClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        final Button upload = (Button) findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View v) {
                storageService.closeMeasureSession();
                storageService.save();
                storageService.uploadFile();
                unsavedChanges = false;
                try{
                    storageService.continueSession();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart() {
        bindService(new Intent(this, StorageMainService.class), storageConnection, BIND_AUTO_CREATE);
        Log.i(getLocalClassName(), "bound Stor Svc");
        super.onStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStop() {
        Log.i(getLocalClassName(), "unbound Stor Svc");
        if (storageService != null)
            unbindService(storageConnection);
        super.onStop();
    }


    public void showDialog() {
        if (unsavedChanges) {
            DialogFragment dialog = new UnsavedDataDialog();
            dialog.show(getFragmentManager(), "dialog");
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        showDialog();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        storageService.save();
        storageService.closeMeasureSession();
        finish();
    }

    @Override
    public void onDialogNeutralClick(DialogFragment dialog) {

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        storageService.closeMeasureSession();
        finish();
    }
}



package kn.uni.inf.sensortagvr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;


public class SplashActivity extends AppCompatActivity {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermissions();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    void checkPermissions() {
        String[] permissions = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        String[] deniedPermissions = new String[8];
        int i = -1;

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED)
                deniedPermissions[++i] = permission;
        }
        if (i > -1) {
            ActivityCompat.requestPermissions(this, deniedPermissions, 0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (!(grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

            Toast.makeText(this, "Not all necessary permissions were granted, " +
                    "please grant them to use the app!", Toast.LENGTH_LONG).show();
            finishAndRemoveTask();

        }
    }
}
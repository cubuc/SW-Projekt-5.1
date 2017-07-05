package kn.uni.inf.sensortagvr;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;


public class SplashActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String[] permissions = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_PRIVILEGED,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_ENABLE_BT = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(getLocalClassName(), "just b4 checkPermissions");
        checkPermissions();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED)
                    finishAndRemoveTask();
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private void checkPermissions() {

        final ArrayList<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission);

            } else Log.i(getLocalClassName(), "permission" + permission + "granted");
        }
        if (!deniedPermissions.isEmpty()) {
            final String[] denied = new String[deniedPermissions.size()];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ActivityCompat.requestPermissions(SplashActivity.this, deniedPermissions.toArray(denied), 0);
                }
            });
        } else {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));

                startActivity(new Intent(this, MainActivity.class));
            }
        }
    }
}
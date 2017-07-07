package kn.uni.inf.sensortagvr;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;


public class SplashActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String[] permissions = {android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.BLUETOOTH_PRIVILEGED,
            android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_WIFI_STATE,
            android.Manifest.permission.CHANGE_WIFI_STATE, android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

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
                    ActivityCompat.requestPermissions(this, permissions, 0);
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            checkPermissions();
        } else {
            Log.i(getLocalClassName(), "permission check failed");
            checkPermissions();
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_CANCELED) {
            Log.i(getLocalClassName(), "location not enabled.");
            finish();
            return;
        } else if (requestCode == 1 && resultCode == Activity.RESULT_CANCELED) {
            Log.i(getLocalClassName(), "bluetooth not enabled.");
            finish();
            return;
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkPermissions() {
        if (!(getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) ||
                !(getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)) ||
                !(getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS))) {
            Toast.makeText(this, "BLE or Location (either GPS or network) not Supported. \n" +
                            "Sorry you can't use this App without those features!",
                    Toast.LENGTH_LONG).show();
            finishAndRemoveTask();
        }

        final ArrayList<String> deniedPermissions = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);

            } else Log.i(getLocalClassName(), "permission" + permission + "granted");
        }
        if (!deniedPermissions.isEmpty()) {

            for (String p : deniedPermissions)
                Log.i(getLocalClassName(), "asking for permissions " + p);

            ActivityCompat.requestPermissions(SplashActivity.this, deniedPermissions.toArray(new String[]{}), 0);

        } else {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            BluetoothManager bm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

            if (!(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && lm.isProviderEnabled(LocationManager.GPS_PROVIDER)))
                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
            else if (!(bm.getAdapter().isEnabled())) {
                Log.i(getLocalClassName(), "BT is off. enabling bluetoothAdapter");
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
            } else {
                Log.i(getLocalClassName(), "BT & Location is already on. ");
                startActivity(new Intent(this, MainActivity.class));
            }


        }
    }
}
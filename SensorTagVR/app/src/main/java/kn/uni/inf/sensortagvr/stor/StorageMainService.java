package kn.uni.inf.sensortagvr.stor;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import kn.uni.inf.sensortagvr.tracking.TrackingManagerService;


/**
 * Main Service that should get started with the App. It registers a broadcastReceiver for Sensor
 * Data.
 * <p>
 * Created by Gero on 16.05.17.
 */

public class StorageMainService extends IntentService {

    /* From the bleSensor actions */

    public final static String ACTION_DATA_AVAILABLE =
            "kn.uni.inf.sensortagvr.ble.ACTION_DATA_AVAILABLE";

    // TODO make data scaling depend on the Sensor (not mandatory)
    //public final static String EXTRA_SENSOR = "kn.uni.inf.sensortagvr.ble.EXTRA_SENSOR";

    public final static String EXTRA_DATA =
            "kn.uni.inf.sensortagvr.ble.EXTRA_DATA";


    // Create a custom broadcast receiver for the bluetooth broadcast
    public final StorageBroadcastReceiver bleReceiver = new StorageBroadcastReceiver(StorageMainService.this);

    // Creates a Binder, look at the onBind() method for more information
    private final IBinder binder = new StorageBinder();

    // Copied from the android dev guide for bound services
    private TrackingManagerService trackingService = null;
    private boolean trackingServiceBound = false;

    //
    private Location nullPoint = null;

    // Saves all measured data in a session
    private ArrayList<CompactData> dataMeasured;
    private boolean sessionStarted = false;

    // Path a data.json file is saved to
    private String path;

    /**
     * stores data received from the bleReceiver
     */
    private Intent lastReceivedData;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        /**
         * @param className ComponentName
         * @param service IBinder
         */
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            TrackingManagerService.TrackingBinder binder = (TrackingManagerService.TrackingBinder) service;
            trackingService = binder.getService();
            trackingServiceBound = true;
        }

        /**
         * @param arg0 ComponentName
         */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            trackingServiceBound = false;
        }
    };


    /**
     *
     */
    public StorageMainService() {
        super("StorageMainService");
    }

    /**
     * On Creating the service, the broadcast receiver will registered with an proper intent filter
     * The TrackingManagerService gets bound to this service, thus the getCurrentLocation() can be
     * accessed.
     */
    @Override
    public void onCreate() {

        //Create and fill a Intent Filter to only receive data from the ble-broadcast
        // TODO filter in xml file
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_DATA_AVAILABLE);

        // Register the bleReceiver
        this.registerReceiver(bleReceiver, intentFilter);

        // Binding TrackingManagerService
        Intent bindTrackService = new Intent(this, TrackingManagerService.class);
        bindService(bindTrackService, mConnection, Context.BIND_AUTO_CREATE);

        // TODO make path to an external accessible file
        path = getFilesDir().getAbsolutePath();

        // Testing
        startMeasureSession();
        createDummyData();
        normalize(dataMeasured);
        finishMeasureSession();
    }

    /**
     * @param intent intent that shall be handled
     */
    @Override
    public void onHandleIntent(Intent intent) {

        // When started by StorageBroadcastReceiver
        if (ACTION_DATA_AVAILABLE.equals(intent.getAction()))
            this.lastReceivedData = intent;

        // Testing
        //Toast.makeText(getApplicationContext(), "Intend handled by StorageMainService", Toast.LENGTH_SHORT).show();
    }

    /**
     *
     */
    public void measureData() {
        // get data from 'lastReceived'
        float[] receivedData = {0};
        if (lastReceivedData != null)
            receivedData = lastReceivedData.getFloatArrayExtra(EXTRA_DATA);

        // Mock-up and suggestion for future Location gathering
        //double[2] xyLoc = trackingService.getLocation();

        // get Data from tracking module
        Location loc = trackingService.getCurrentPosition();

        double x = 0, y = 0;
        if (nullPoint != null) {
            x = loc.getLatitude() - nullPoint.getLatitude();
            y = loc.getLongitude() - nullPoint.getLongitude();
        }

        // receivedData should be scaled between -.5 and 1
        // TODO scale receivedData
        dataMeasured.add(new CompactData(x, y, receivedData[0]));

        Toast.makeText(getApplicationContext(), "Data received", Toast.LENGTH_SHORT).show();
    }

    /**
     * Unregisters the bleReceiver and unbind trackingmngrService when the Service gets DESTROYED
     */
    @Override
    public void onDestroy() {
        this.unregisterReceiver(bleReceiver);

        if (trackingServiceBound) {
            unbindService(mConnection);
            trackingServiceBound = false;
        }

        finishMeasureSession();
        // Testing
        Toast.makeText(getApplicationContext(), "StorageMainService destroyed", Toast.LENGTH_SHORT).show();
    }

    /**
     * Starts a new Measure Session. Be sure to close the session by calling {@see startMeasureSession}.
     */
    public void startMeasureSession() {
        //trackingService.calibrate;
        dataMeasured = new ArrayList<>();

        // Signals a recording session started
        sessionStarted = true;
    }

    /**
     *
     */
    public void finishMeasureSession() {
        if (sessionStarted) {
            try {
                writeInJsonFile(dataMeasured);

                // Signals a recording session ended
                sessionStarted = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Writes the given List to a data.json file using the gson-library. If a file already exists,
     * it gets overwritten.
     * Only one session can be saved this way. This may be extended in the future.
     *
     * @param list List to be saved
     */
    private void writeInJsonFile(ArrayList<CompactData> list) throws IOException {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        File dir = new File(path);

        // Check whether the directory exists, tries to create it, if it doesn't exist yet
        if (!dir.exists()) {
            if (dir.mkdirs())
                Toast.makeText(getApplicationContext(), "New Directories created", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Couldn't create Directories", Toast.LENGTH_SHORT).show();
        }

        File data = new File(dir, "data.json");

        // Check whether a file could be created.
        if (!data.createNewFile())
            Toast.makeText(getApplicationContext(), "data.json could not be created", Toast.LENGTH_SHORT).show();

        // Create a FileWriter, use gson to write to it and close it. This essentially creates the file.
        FileWriter writer = new FileWriter(data);
        gson.toJson(list, writer);
        writer.close();
    }

    /**
     * This method changes the values of the Location for a easier triangulation by webVR
     * Current Implementation: round all doubles.
     *
     * @param list List to be normalized
     */
    private void normalize(ArrayList<CompactData> list) {
        for (CompactData data : list) {
            data.setX(Math.round(data.getX()));
            data.setY(Math.round(data.getY()));
        }
    }

    /**
     * This sets the surface zero to the current Location
     * (Should be called at the start of the measurement)
     * <p>
     * Will probably be removed soon
     */
    public void calibrate() {

        //trackingService.calibrate();

        nullPoint = trackingService.getCurrentPosition();

        // Testing
        //Toast.makeText(getApplicationContext(), "StorageMainService calibrated: " + nullPoint.toString(), Toast.LENGTH_SHORT).show();
    }

    /**
     * the service is intended to be bound at the main activity or the recordActivity
     *
     * @param intent intent
     * @return binder
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    // Copied from the android dev guide for bound services

    public void createDummyData() {
        dataMeasured.add(new CompactData(1.1, 1.2, 1.3));
        dataMeasured.add(new CompactData(1.4, 1.5, 1.6));
        dataMeasured.add(new CompactData(1.7, 1.8, 1.9));
        dataMeasured.add(new CompactData(2.1, 2.2, 2.3));
        dataMeasured.add(new CompactData(3.1, 4.2, 5.3));
        dataMeasured.add(new CompactData(1.1, 1.2, 1.3));
    }

    /**
     * For more detailed info look at  https://developer.android.com/guide/components/bound-services.html
     */
    public class StorageBinder extends Binder {
        /**
         * @return current Instance of StorageMainService
         */
        public StorageMainService getService() {
            return StorageMainService.this;
        }
    }
}




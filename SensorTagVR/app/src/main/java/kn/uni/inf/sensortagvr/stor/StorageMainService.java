package kn.uni.inf.sensortagvr.stor;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PointF;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import kn.uni.inf.sensortagvr.tracking.TrackingManagerService;

import static kn.uni.inf.sensortagvr.ble.BluetoothLEService.ACTION_DATA_AVAILABLE;
import static kn.uni.inf.sensortagvr.ble.BluetoothLEService.EXTRA_DATA;


/**
 * Main Service that should get started with the App. It registers a broadcastReceiver for Sensor
 * Data.
 * <p>
 * Created by Gero on 16.05.17.
 */

public class StorageMainService extends IntentService {


    private static final int y[] = new int[]{0, 5, 7};
    // Sets the Scale factor for the measured Data
    private static final int SCALE_DATA = 2;
    private static final double SCALE_DATA_OFFSET = 1.0;
    // Sets the Scale factor for the location grid in dataMeasured
    private static final int SCALE_LOCATION = 10;
    IBinder binder = new StorageBinder();
    private int x = 0;
    private int yIndex = 0;
    private int xCount = 0;
    // instantiate a custom broadcast receiver for the bluetooth broadcast
    private TrackingManagerService trackingService = null;
    private boolean trackingServiceBound = false;
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
    // Saves all measured data in a session
    private ArrayList<CompactData> dataMeasured;
    private boolean sessionStarted = false;
    // Path a data.json file is saved to
    private String path;
    private Intent lastReceivedData;


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
        super.onCreate();

        // Binding TrackingManagerService
        Intent bindTrackService = new Intent(this, TrackingManagerService.class);
        bindService(bindTrackService, mConnection, Context.BIND_AUTO_CREATE);

        // File can be accessed by the phone itself
        // Path = /storage/emulated/=/Android/data/kn.uni.inf.sensortagvr/files
        if (isExternalStorageWritable())
            path = getExternalFilesDir(null).getAbsolutePath();
    }

    /** @param intent intent that shall be handled */
    @Override
    public void onHandleIntent(Intent intent) {

        // When started by StorageBroadcastReceiver
        if (ACTION_DATA_AVAILABLE.equals(intent.getAction()))
            this.lastReceivedData = intent;
    }

    /**
     * The service is bound by the RecordDataActivity for creating a measurement session.
     *
     * @param intent intent
     * @return binder
     */
    @Override
    public IBinder onBind(Intent intent) {
        //trackingService.calibrate;
        dataMeasured = new ArrayList<>();
        sessionStarted = true;
        return binder;
    }

    /**
     * Called when the RecordActivity unbound the interface. Scales all measured data and closes
     * the session
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     * @return Return true if you would like to have the service's
     * {@link #onRebind} method later called when new clients bind to it.
     */
    @Override
    public boolean onUnbind(Intent intent) {
        scaleAll(dataMeasured);
        return super.onUnbind(intent);
    }

    /**
     * Unregisters the bleReceiver and unbind trackingmngrService when the Service gets DESTROYED
     */
    @Override
    public void onDestroy() {

        if (trackingServiceBound) {
            unbindService(mConnection);
            trackingServiceBound = false;
        }
        super.onDestroy();
    }

    /**
     * Collects all data received by the ble-service and the loc-service, and saves them into the
     * ArrayList measured
     */
    public void measureData() {
        if (sessionStarted) {
            // get data from 'lastReceived'
            float[] receivedData = {0};
            if (lastReceivedData != null)
                receivedData = lastReceivedData.getFloatArrayExtra(EXTRA_DATA);

            // get Data from tracking module
            PointF loc = trackingService.getRelativePosition();

            // receivedData should be scaled between -.5 and 1
            dataMeasured.add(new CompactData(x, y[(++yIndex) % 3], receivedData[0]));
            xCount = (++xCount) % 3;

            if (xCount == 0) x++;

            Toast.makeText(getApplicationContext(), "Data received", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getApplicationContext(), "No measure session is started", Toast.LENGTH_SHORT).show();
    }

    /**
     *
     */
    public void closeMeasureSession() {
        if (sessionStarted) {
            try {
                writeInJsonFile(dataMeasured);

                // Signals a recording session ended
                sessionStarted = false;

                Toast.makeText(getApplicationContext(), "Session closed", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /* Checks if external storage is available for read and write */
    /**
     * 
     */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
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
        if (!data.isFile() && !data.createNewFile())
            Toast.makeText(getApplicationContext(), "data.json could not be created", Toast.LENGTH_SHORT).show();

        // Create a FileWriter, use gson to write to it and close it. This essentially creates the file.
        FileWriter writer = new FileWriter(data);
        gson.toJson(list, writer);
        writer.close();

    }

    /**
     * This method changes the values of the Location for a easier triangulation by webVR
     * Current Implementation: scales all variables to mach SCALE (the biggest value in list = SCALE
     * afterwards)
     *
     * @param list List to be normalized
     */
    private void scaleAll(ArrayList<CompactData> list) {
        double factor_loc = calculateLocationFactor(list);
        double factor_data = calculateDataFactor(list);
        double min_data = calculateMinData(list);

        // Scale the list values with the determined factors
        for (CompactData item : list) {
            item.setX(item.getX() / factor_loc);
            item.setY(item.getY() / factor_loc);
            // Data gets scaled to match a scale of 0 to 2, where the smallest data is 0 and the
            // biggest data is 2
            item.setZ((item.getData() - min_data) / factor_data - SCALE_DATA_OFFSET);
        }

    }

    /**
     *
     * @param list
     */
    private double calculateMinData(ArrayList<CompactData> list) {
        double min = Double.MAX_VALUE;

        for (CompactData item : list) {
            min = Math.min(min, item.getData());
        }

        return min;
    }

    /**
     *
     * @param list
     */
    private double calculateDataFactor(ArrayList<CompactData> list) {
        double max = Double.MIN_VALUE;

        // determine the biggest location parameter
        for (CompactData item : list) {
            max = Math.max(max, item.getData());
        }

        return max / SCALE_DATA;
    }

    /**
     * Determines the factor by which list should be scaled down
     *
     * @param list ist of location points
     * @return scaling factor to match SCALE
     */
    private double calculateLocationFactor(ArrayList<CompactData> list) {
        double max = Double.MIN_VALUE;

        // determine the biggest location parameter
        for (CompactData item : list) {
            max = Math.max(max, Math.max(Math.abs(item.getX()), Math.abs(item.getY())));
        }

        return max / SCALE_LOCATION;
    }

    /**
     * 
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






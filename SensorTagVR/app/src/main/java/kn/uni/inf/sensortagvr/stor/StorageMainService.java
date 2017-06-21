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
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import kn.uni.inf.sensortagvr.tracking.TrackingManagerService;

import static kn.uni.inf.sensortagvr.ble.BluetoothLEService.EXTRA_DATA;


/**
 * Main Service that should get started with the App. It registers a broadcastReceiver for Sensor
 * Data.
 * <p>
 * Created by Gero on 16.05.17.
 */

public class StorageMainService extends IntentService {


    // Sets the Scale factor for the location grid in dataMeasured
    private static final int SCALE_LOCATION = 10;

    IBinder binder = new StorageBinder();


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


    private double SCALEFACTOR_X = 30;
    private double SCALEFACTOR_Y = 20;
    private boolean REBASE = false;
    private boolean DISTORT = false;

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
        // Path = /storage/emulated/0/Android/data/kn.uni.inf.sensortagvr/files
        if (isExternalStorageWritable())
            path = getExternalFilesDir(null).getAbsolutePath();
    }

    /** @param intent intent that shall be handled */
    @Override
    public void onHandleIntent(Intent intent) {
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
        createDummyData();
        return binder;
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
            dataMeasured.add(new CompactData(loc, receivedData[0]));
            Log.d("StorMan", "Data " + receivedData[0]);
        } else
            Toast.makeText(getApplicationContext(), "No measure session is started", Toast.LENGTH_SHORT).show();
    }

    /**
     *
     */
    public void closeMeasureSession() {
        if (sessionStarted) {
            try {
                scaleAll(dataMeasured);
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
        for (CompactData item : list)
            Log.d("StorMan", item.toString());
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

        double maxData = calculateMaxDataVal(list);
        double minData = calculateMinDataVal(list);
        double dataFactor = maxData - minData;

        // Rebases the set of data points, thus no point has a negative X or Y value
        if (REBASE){
            double[] minVals = calculateMinValues(list);

            for (CompactData item : list) {
                item.setX(item.getX() - minVals[0]);
                item.setY(item.getY() - minVals[1]);
            }
        }

        // Given that the max distance may change if the Origin is changed, the max Distance to
        // any axis is calculated afterwards
        double[] maxDist = calculateMaxDistance(list);

        if (!DISTORT) {
            SCALEFACTOR_Y = SCALEFACTOR_X;
        }

        double factorX = maxDist[0] / SCALEFACTOR_X;
        double factorY = maxDist[1] / SCALEFACTOR_Y;


        // Scale the list values with the determined factors
        for (CompactData item : list) {

            item.setX( item.getX() / factorX); // = item.getX() / maxDist[0] * SCALEFACTOR_X
            item.setY( item.getY() / factorY);
            item.setZ( (item.getData() - minData) / dataFactor - .5);
            Log.d("StorMan", "z = " + item.getZ());
        }

    }

    /**
     *
     * @param list
     */
    private double calculateMinDataVal(ArrayList<CompactData> list) {
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
    private double calculateMaxDataVal(ArrayList<CompactData> list) {
        double max = Double.MIN_VALUE;

        // determine the biggest location parameter
        for (CompactData item : list) {
            max = Math.max(max, item.getData());
        }

        return max;
    }

    /**
     * Determines the biggest distance a point has to the nullpoint.
     *
     * If distortion is active, the fi
     *
     * @param list ist of location points
     * @return If distortion is active, the first value of the array is the biggest distance a point
     *  has on the x-axis and the second value is the biggest distance a point has on the y-axis.
     *  If distortion is not active, the first value is the biggest distance a point has to any axis.
     *  (the second value will be like with distortion active)
     */
    private double[] calculateMaxDistance(ArrayList<CompactData> list) {

        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (CompactData item : list) {
            maxX = Math.max(maxX, Math.abs(item.getX()));
            maxY = Math.max(maxY, Math.abs(item.getY()));
        }

        if (!DISTORT) {
            maxX = Math.max(maxX, maxY);
        }

        return DISTORT ? new double[]{maxX, maxY} : new double[]{maxX, maxX};

    }

    /**
     *
     * @param list
     * @return
     */
    private double[] calculateMinValues(ArrayList<CompactData> list) {

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;

        for (CompactData item : list) {
            minX = Math.min(minX, item.getX());
            minY = Math.min(minY, item.getY());
        }

        return new double[] {minX, minY};
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


    public void createDummyData() {
        dataMeasured.add(new CompactData(0,0, 23));
        dataMeasured.add(new CompactData(4,0, 25));
        dataMeasured.add(new CompactData(0,5, 20));
        dataMeasured.add(new CompactData(0,7, 24));
        dataMeasured.add(new CompactData(4,7, 22));
        dataMeasured.add(new CompactData(4,5, 20));
    }

}






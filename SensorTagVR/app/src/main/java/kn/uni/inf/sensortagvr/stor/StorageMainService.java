package kn.uni.inf.sensortagvr.stor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import kn.uni.inf.sensortagvr.ble.BluetoothLEService;
import kn.uni.inf.sensortagvr.ble.Sensor;
import kn.uni.inf.sensortagvr.tracking.TrackingManagerService;

import static kn.uni.inf.sensortagvr.ble.BluetoothLEService.ACTION_DATA_AVAILABLE;
import static kn.uni.inf.sensortagvr.ble.BluetoothLEService.EXTRA_DATA;
import static kn.uni.inf.sensortagvr.ble.BluetoothLEService.EXTRA_SENSOR;


/**
 * Main Service that should get started with the App. It registers a broadcastReceiver for Sensor
 * Data.
 * <p>
 * Created by Gero on 16.05.17.
 */

public class StorageMainService extends Service {

    private static final String TAG = "StorageMainService";
    // TODO Lifecycle
    private final IBinder binder = new StorageBinder();
    private final boolean DISTORT = false;
    // instantiate a custom broadcast receiver for the bluetooth broadcast
    private TrackingManagerService trackingService = null;
    private boolean mBound=false;
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            TrackingManagerService.TrackingBinder binder = (TrackingManagerService.TrackingBinder) service;
            trackingService = binder.getService();
            mBound = true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            trackingService = null;
            mBound = false;
        }
    };
    // Saves all measured data in a session
    private ArrayList<CompactData> dataMeasured;
    private boolean sessionStarted = false;
    // Path a data.json file is saved to
    private String path = null;
    private Intent lastReceivedData;
    private final BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BluetoothLEService.ACTION_GATT_DISCONNECTED:
                    break;
                case BluetoothLEService.ACTION_DATA_AVAILABLE:
                    Sensor mSensor = (Sensor) intent.getExtras().get(EXTRA_SENSOR);
                    if (mSensor != null && mSensor == Sensor.IR_TEMPERATURE) {
                        lastReceivedData = intent;
                        Log.i("StorMainSvc", "received irt broadcast");
                    }
                    break;
                default:
                    Log.i("StorMainSvc", "received any broadcast");
                    break;
            }
        }
    };
    private double SCALEFACTOR_Y = 20;
    private LocalBroadcastManager mLocalBroadcastManager;

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /**
     * {@inheritDoc}
     * On Creating the service, the broadcast receiver will registered with an proper intent filter
     * The TrackingManagerService gets bound to this service, thus the getCurrentLocation() can be
     * accessed.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        path = getFilesDir().getAbsolutePath() + File.separator + "data.json";
        Intent intent = new Intent(this, TrackingManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void onStop(){
        super.onDestroy();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    /**
     * The service is bound by the RecordDataActivity for creating a measurement session.
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     *
     */
    public void createNewSession() {
        mLocalBroadcastManager.registerReceiver(mUpdateReceiver, new IntentFilter(ACTION_DATA_AVAILABLE));
        dataMeasured = new ArrayList<>();
        sessionStarted = true;
        bindService(new Intent(this, TrackingManagerService.class), mConnection, 0);
        Toast.makeText(getApplicationContext(), "New Session created", Toast.LENGTH_SHORT).show();
    }

    /**
     *
     */
    public void continueSession() {

        File data = new File(path);
        JsonReader reader;
        FileReader fileReader;
        if (data.isFile()) {
            mLocalBroadcastManager.registerReceiver(mUpdateReceiver, makeGattUpdateIntentFilter());
            bindService(new Intent(this, TrackingManagerService.class), mConnection, 0);

            try {
                Gson gson = new Gson();
                fileReader = new FileReader(path);
                reader = new JsonReader(fileReader);

                CompactData[] datas = gson.fromJson(reader, CompactData[].class);
                dataMeasured = new ArrayList<>(Arrays.asList(datas));

                sessionStarted = true;

                fileReader.close();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else
            Toast.makeText(getApplicationContext(), "No old session found", Toast.LENGTH_SHORT).show();
    }

    /**
     * Collects all data received by the ble-service and the loc-service, and saves them into the
     * ArrayList measured
     */
    public void measureData() {

        if (sessionStarted) {
            // get data from 'lastReceived'
            float[] receivedData = {0, 0, 0};

            if (lastReceivedData != null)
                receivedData = lastReceivedData.getFloatArrayExtra(EXTRA_DATA);

            // get Data from tracking module
            if (trackingService != null) {
                Log.i(getClass().getSimpleName(), "l");
                PointF loc = trackingService.getRelativePosition();
                // receivedData should be scaled between -.5 and 1
                dataMeasured.add(new CompactData(loc, receivedData[0]));
                Log.i(getClass().getSimpleName(), loc.toString());
                Log.i(getClass().getSimpleName(), "la");
                Log.i(getClass().getSimpleName(), "la");
                Log.i(getClass().getSimpleName(), "la");
                Log.i(getClass().getSimpleName(), "la");
                Log.i(getClass().getSimpleName(), "la");
                Log.d(TAG,  "Data " + receivedData[0]);
            } else {
                Log.i(getClass().getSimpleName(), "trackManSvc == null");
            }
        } else
            Toast.makeText(getApplicationContext(), "No measure session is started", Toast.LENGTH_SHORT).show();

    }

    /**
     *
     */
    public void save() {
        scaleAll(dataMeasured);
        try {
            writeInJsonFile(dataMeasured);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public void closeMeasureSession() {
        if (sessionStarted) {
            mLocalBroadcastManager.unregisterReceiver(mUpdateReceiver);
            unbindService(mConnection);
            stopService(new Intent(this, TrackingManagerService.class));
            sessionStarted = false;
            Log.i(TAG, "unbound & stopped tracking manager");
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

        File file = new File(path);

        // Check whether a file could be created.
        if (!file.isFile() && !file.createNewFile())
            Toast.makeText(getApplicationContext(), "data.json could not be created", Toast.LENGTH_SHORT).show();
        for (CompactData item : list)
            Log.d(TAG,  item.toString());
        // Create a FileWriter, use gson to write to it and close it. This essentially creates the file.
        FileWriter writer = new FileWriter(file);
        gson.toJson(list, writer);
        writer.close();

        Toast.makeText(getApplicationContext(), "File saved", Toast.LENGTH_SHORT).show();

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

        // Rebases the settings of data points, thus no point has a negative X or Y value
        boolean REBASE = false;
        if (REBASE) {
            double[] minVals = calculateMinValues(list);

            for (CompactData item : list) {
                item.setX(item.getOriginalX() - minVals[0]);
                item.setY(item.getOriginalY() - minVals[1]);
            }
        }

        // Given that the max distance may change if the Origin is changed, the max Distance to
        // any axis is calculated afterwards
        double[] maxDist = calculateMaxDistance(list);

        double SCALEFACTOR_X = 30;
        if (!DISTORT) {
            //noinspection SuspiciousNameCombination
            SCALEFACTOR_Y = SCALEFACTOR_X;
        }

        double factorX = maxDist[0] / SCALEFACTOR_X;
        double factorY = maxDist[1] / SCALEFACTOR_Y;


        // Scale the list values with the determined factors
        for (CompactData item : list) {

            item.setX(item.getOriginalX() / factorX); // = item.getOriginal X / maxDist[0] * SCALEFACTOR_X
            item.setY(item.getOriginalY() / factorY);
            item.setZ((item.getData() - minData) / dataFactor - 1.5);
            Log.d(TAG,  "z = " + item.getZ());
        }

    }


    /**
     * Calculates the smallest data-Value in a ArrayList of CompactData and returns it.
     * @param list ArrayList of CompactData
     * @return smallest data-Value found as double
     */
    private double calculateMinDataVal(ArrayList<CompactData> list) {
        double min = Double.MAX_VALUE;

        for (CompactData item : list) {
            min = Math.min(min, item.getData());
        }

        return min;
    }

    /**
     * Calculates the biggest data-Value in a ArrayList of CompactData and returns it.
     * @param list ArrayList of CompactData
     * @return biggest data-Value found as double
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
     * <p>
     * If distortion is active, the fi
     *
     * @param list ist of location points
     * @return If distortion is active, the first value of the array is the biggest distance a point
     * has on the x-axis and the second value is the biggest distance a point has on the y-axis.
     * If distortion is not active, the first value is the biggest distance a point has to any axis.
     * (the second value will be like with distortion active)
     */
    private double[] calculateMaxDistance(ArrayList<CompactData> list) {

        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (CompactData item : list) {
            maxX = Math.max(maxX, Math.abs(item.getOriginalX()));
            maxY = Math.max(maxY, Math.abs(item.getOriginalY()));
        }

        if (!DISTORT) {
            maxX = Math.max(maxX, maxY);
        }

        return DISTORT ? new double[]{maxX, maxY} : new double[]{maxX, maxX};

    }

    /**
     * Calculates the smallest X and the smallest Y value in a ArrayList of CompactData.
     * The smallest X value and the smallest Y value may come from different CompactData-Objects.
     * @param list ArrayList of CompactData
     * @return the smallest X and the smallest Y value as an double array, in which the first item is the X value.
     */
    private double[] calculateMinValues(ArrayList<CompactData> list) {

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;

        for (CompactData item : list) {
            minX = Math.min(minX, item.getOriginalX());
            minY = Math.min(minY, item.getOriginalY());
        }

        return new double[]{minX, minY};
    }

    /**
     *  Uploads the File using Uploader. Note that this method will not save the current Data in the File,
     *  so be sure to call save() before you call this method.
     */
    public void uploadFile() {
        AsyncTask<String, Boolean, Integer> up = new Uploader();
        up.execute(path);
    }

    /**
     * {@inheritDoc}
     */
    public class StorageBinder extends Binder {
        /**
         * @return current Instance of StorageMainService
         */
        public StorageMainService getService() {
            return StorageMainService.this;
        }
    }

    /**
     *  {@inheritDoc}
     */
    private class Uploader extends AsyncTask<String, Boolean, Integer> {
        /**
         * {@inheritDoc}
         */
        @Override
        protected Integer doInBackground(String... strings) {
            FTPClient con;

            try {
                // establish a connection
                con = new FTPClient();
                con.connect("web.kim.uni-konstanz.de");

                // Try to log in to the server
                if (con.isConnected() && con.login("softwareproject17", "Cardboard51**")) {
                    con.enterLocalPassiveMode(); // important!
                    con.setFileType(FTP.ASCII_FILE_TYPE);

                    int reply = con.getReplyCode();

                    if (!FTPReply.isPositiveCompletion(reply))
                        return 1;

                    // Create the File that gets uploaded
                    File data = new File(strings[0]);

                    if (!data.isFile())
                        return 1;

                    // Upload the file
                    FileInputStream in = new FileInputStream(data);
                    boolean result = con.storeFile("/data.json", in);
                    in.close();
                    if (result) Log.v(TAG, "upload successful");
                    else return 1;
                    // close the connection
                    con.logout();
                    con.disconnect();
                } else
                    return 1;
            } catch (Exception e) {
                e.printStackTrace();
                return 1;
            }
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(Integer result) {
            if (result == 1)
                Toast.makeText(getApplicationContext(), "Upload not successful", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Upload successful", Toast.LENGTH_SHORT).show();
        }

    }

}

package kn.uni.inf.sensortagvr.tracking;

import android.graphics.PointF;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class WifiTracker {

    private WifiManager wifiManager;
    private Map<String, WifiAP> aps = new HashMap<>();

    /**
     * @param wifiManager
     */
    WifiTracker(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    /**
     *
     */
    void update() {
        List<ScanResult> results =  wifiManager.getScanResults();
        Map<String, WifiAP> newAPs = new HashMap<>();

        for(ScanResult r : results) {
            WifiAP ap = aps.get(r.BSSID);

            if(ap != null) {
                ap.update(r);
            }
            else {
                ap = new WifiAP(r);
            }

            newAPs.put(ap.getBSSID(), ap);
        }

        for(WifiAP ap : aps.values()) {
            if(ap.isTracked() && !newAPs.containsKey(ap.getBSSID()))
                newAPs.put(ap.getBSSID(), ap);
        }

        aps = newAPs;
        wifiManager.startScan();
    }

    /**
     *
     * @param ap
     */
    boolean trackAP(WifiAP ap) {
        if(!aps.containsKey(ap.getBSSID()))
            return false;

        aps.put(ap.getBSSID(), ap);
        return true;
    }

    /**
     *
     */
    PointF calculateLocation() {
        this.update();

        List<WifiAP> trackedAPs = new ArrayList<>();
        for(WifiAP ap : aps.values()) {
            if(ap.isTracked() && ap.getDistance() > 0.0)
                trackedAPs.add(ap);
        }

        if(trackedAPs.size() < 3) {
            Log.e("TRACKING_MANAGER", "Not enough tracked APs for position calculation!");
            return null;
        }

        /*Collections.sort(trackedAPs, new Comparator<WifiAP>() {
            @Override
            public int compare(WifiAP lhs, WifiAP rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.getDistance() <= rhs.getDistance() ? -1 : 1;
            }
        });*/

        double[][] positions = new double[trackedAPs.size()][2];
        double[] distances = new double[trackedAPs.size()];

        for(int i=0; i < trackedAPs.size(); i++) {
            WifiAP ap = trackedAPs.get(i);

            positions[i][0] = ap.getLocation().x;
            positions[i][1] = ap.getLocation().y;

            distances[i] = ap.getDistance();
        }

        NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(
                new TrilaterationFunction(positions, distances),
                new LevenbergMarquardtOptimizer());
        LeastSquaresOptimizer.Optimum optimum = solver.solve();

/*        // the answer
        double[] centroid = optimum.getPoint().toArray();

        // error and geometry information; may throw SingularMatrixException depending the threshold argument provided
        RealVector standardDeviation = optimum.getSigma(0);
        RealMatrix covarianceMatrix = optimum.getCovariances(0);*/


        return new PointF((float)optimum.getPoint().toArray()[0], (float)optimum.getPoint().toArray()[1]);
    }

    /**
     *
     * @param update
     */
    List<WifiAP> getWifiAPs(boolean update) {
        if(update)
            update();

        return new ArrayList<>(aps.values());
    }

    /**
     *
     * @param out
     */
    void writeToFile(FileOutputStream out) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out));
        writer.setIndent("  ");

        writer.beginArray();
        for(WifiAP ap : aps.values()) {
            ap.writeToJSON(writer);
        }
        writer.endArray();

        writer.close();
    }

    /**
     *
     * @param in
     */
    void readFromFile(FileInputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in));

        reader.beginArray();
        while (reader.hasNext()) {
            WifiAP ap = new WifiAP(reader);
            aps.put(ap.getBSSID(), ap);
        }
        reader.endArray();

        reader.close();
    }
}
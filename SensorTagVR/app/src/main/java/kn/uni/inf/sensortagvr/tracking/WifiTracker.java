package kn.uni.inf.sensortagvr.tracking;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ojo on 28.05.17.
 */

public class WifiTracker {

    private WifiManager wifiManager;
    private Map<String, WifiAP> aps = new HashMap<String, WifiAP>();

    public WifiTracker(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    public void update() {
        List<ScanResult> results =  wifiManager.getScanResults();
        Map<String, WifiAP> newAPs = new HashMap<String, WifiAP>();

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
    }

    public boolean trackAP(WifiAP ap) {
        if(!aps.containsKey(ap.getBSSID()))
            return false;

        aps.put(ap.getBSSID(), ap);
        return true;
    }

    public List<WifiAP> getWifiAPs(boolean update) {
        if(update)
            update();

        return new ArrayList<WifiAP>(aps.values());
    }

    public void writeToFile(FileOutputStream out) throws IOException{
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out));
        writer.setIndent("  ");

        writer.beginArray();
        for(WifiAP ap : aps.values()) {
            ap.writeToJSON(writer);
        }
        writer.endArray();

        writer.close();
    }

    public void readFromFile(FileInputStream in) throws IOException{
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

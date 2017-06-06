package kn.uni.inf.sensortagvr.tracking;

import android.graphics.PointF;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;

/**
 * Created by ojo on 28.05.17.
 */

public class WifiAP implements Parcelable {
    private String SSID;
    private final String BSSID;
    private double distance;
    private PointF location;

    private final double A;
    private final double n;

    private boolean tracked = false;

    public WifiAP(String BSSID, PointF location) {
        this.BSSID = BSSID;
        this.location = location;
        this.A = -50.0;
        this.n = 3.5;
    }

    public WifiAP(String SSID, String BSSID, PointF location, double A, double n, boolean tracked) {
        this.SSID = SSID;
        this.BSSID = BSSID;
        this.location = location;
        this.A = A;
        this.n = n;
        this.tracked = tracked;
    }

    public WifiAP(ScanResult scan) {
        this.BSSID = scan.BSSID;
        this.location = new PointF();
        this.A = -50.0;
        this.n = 3.5;

        update(scan);
    }

    public boolean update(ScanResult scan) {
        if(!BSSID.equals(scan.BSSID))
            return false;

        SSID = scan.SSID;
        distance = Math.pow(10.0, (A - scan.level) / (10.0 * n));

        return true;
    }

    public String getSSID() {
        return SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public double getDistance() {
        return distance;
    }

    public PointF getLocation() {
        return location;
    }

    public double getA() {
        return A;
    }

    public double getN() {
        return n;
    }

    public boolean isTracked() {
        return tracked;
    }

    public void setTracked(boolean tracked) {
        this.tracked = tracked;
    }

    @Override
    public String toString() {
        return SSID + "\n" + BSSID + "\nDistance:" + distance;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(SSID);
        dest.writeString(BSSID);
        dest.writeParcelable(location, flags);
        dest.writeDouble(A);
        dest.writeDouble(n);
        dest.writeValue(tracked);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public WifiAP createFromParcel(Parcel in) {
            return new WifiAP(in);
        }
        public WifiAP[] newArray(int size) {
            return new WifiAP[size];
        }
    };

    public WifiAP(Parcel in) {
        SSID = in.readString();
        BSSID = in.readString();
        location = in.readParcelable(Location.class.getClassLoader());
        A = in.readDouble();
        n = in.readDouble();
        tracked = (boolean) in.readValue(null);
    }

    public void writeToJSON(JsonWriter writer) throws IOException{
        writer.beginObject();
        writer.name("SSID").value(SSID);
        writer.name("BSSID").value(BSSID);
        writer.name("posX").value(location.x);
        writer.name("posY").value(location.y);
        writer.name("A").value(A);
        writer.name("n").value(n);
        writer.name("tracked").value(tracked);
        writer.endObject();
    }

    public WifiAP(JsonReader reader) throws IOException {
        float posX = 0;
        float posY = 0;
        String bssid = "";
        double varA = -50.0;
        double varN = 3.5;

        reader.beginObject();

        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("SSID")) {
                SSID = reader.nextString();
            } else if (name.equals("BSSID")) {
                bssid = reader.nextString();
            } else if (name.equals("latitude")) {
                posX = (float)reader.nextDouble();
            }
            else if (name.equals("longitude")) {
                posY = (float)reader.nextDouble();
            }
            else if (name.equals("A")) {
                varA = reader.nextDouble();
            }
            else if (name.equals("n")) {
                varN = reader.nextDouble();
            }
            else if (name.equals("tracked")) {
                tracked = reader.nextBoolean();
            } else {
                reader.skipValue();
            }
        }

        reader.endObject();

        location = new PointF(posX, posY);

        BSSID = bssid;
        A = varA;
        n = varN;
    }
}

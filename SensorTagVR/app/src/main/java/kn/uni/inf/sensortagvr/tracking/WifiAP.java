package kn.uni.inf.sensortagvr.tracking;

import android.graphics.PointF;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;


class WifiAP implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public WifiAP createFromParcel(Parcel in) {
            return new WifiAP(in);
        }

        public WifiAP[] newArray(int size) {
            return new WifiAP[size];
        }
    };
    private final String BSSID;
    private final double A;
    private final double n;
    private String SSID;
    private double distance;
    private PointF location;
    private boolean tracked = false;
    private int RSSI = 0;

    WifiAP(String SSID, String BSSID, PointF location, double A, double n, boolean tracked) {
        this.SSID = SSID;
        this.BSSID = BSSID;
        this.location = location;
        this.A = A;
        this.n = n;
        this.tracked = tracked;
    }

    WifiAP(ScanResult scan) {
        this.BSSID = scan.BSSID;
        this.location = new PointF();
        this.A = -50.0;
        this.n = 3.5;

        update(scan);
    }

    private WifiAP(Parcel in) {
        SSID = in.readString();
        BSSID = in.readString();
        location = in.readParcelable(Location.class.getClassLoader());
        A = in.readDouble();
        n = in.readDouble();
        tracked = (boolean) in.readValue(getClass().getClassLoader());
    }

    WifiAP(JsonReader reader) throws IOException {
        float posX = 0;
        float posY = 0;
        String bssid = "";
        double varA = -50.0;
        double varN = 3.5;

        reader.beginObject();

        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "SSID":
                    SSID = reader.nextString();
                    break;
                case "BSSID":
                    bssid = reader.nextString();
                    break;
                case "posX":
                    posX = (float) reader.nextDouble();
                    break;
                case "posY":
                    posY = (float) reader.nextDouble();
                    break;
                case "A":
                    varA = reader.nextDouble();
                    break;
                case "n":
                    varN = reader.nextDouble();
                    break;
                case "tracked":
                    tracked = reader.nextBoolean();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }

        reader.endObject();

        location = new PointF(posX, posY);

        BSSID = bssid;
        A = varA;
        n = varN;
    }

    void update(ScanResult scan) {
        if(!BSSID.equals(scan.BSSID))
            return;

        SSID = scan.SSID;
        RSSI = scan.level;
        distance = Math.pow(10.0, (A - scan.level) / (10.0 * n));

    }

    String getSSID() {
        return SSID;
    }

    String getBSSID() {
        return BSSID;
    }

    double getDistance() {
        return distance;
    }

    public PointF getLocation() {
        return location;
    }

    public double getA() {
        return A;
    }

    double getN() {
        return n;
    }

    boolean isTracked() {
        return tracked;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return SSID + "\n" + BSSID + "\nRSSI:" + RSSI + "\nDistance:" + distance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(SSID);
        dest.writeString(BSSID);
        dest.writeParcelable(location, flags);
        dest.writeDouble(A);
        dest.writeDouble(n);
        dest.writeValue(tracked);
    }

    void writeToJSON(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("SSID").value(SSID);
        writer.name("BSSID").value(BSSID);
        writer.name("posX").value((double)location.x);
        writer.name("posY").value((double)location.y);
        writer.name("A").value(A);
        writer.name("n").value(n);
        writer.name("tracked").value(tracked);
        writer.endObject();
    }
}
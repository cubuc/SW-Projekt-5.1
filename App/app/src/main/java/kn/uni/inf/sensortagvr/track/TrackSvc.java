package kn.uni.inf.sensortagvr.track;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TrackSvc extends Service {
    public TrackSvc() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

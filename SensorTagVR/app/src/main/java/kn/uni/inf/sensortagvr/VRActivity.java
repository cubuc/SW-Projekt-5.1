package kn.uni.inf.sensortagvr;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by lisa-maria on 21.05.17.
 */

public class VRActivity extends AppCompatActivity {

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr_view);

        final Button VRbutton = (Button) findViewById(R.id.start_VR);
        VRbutton.setOnClickListener(new View.OnClickListener() {
            /** @param v */
            @Override
            public void onClick(View v) {
                String url = "http://localhost:12345/index.html";
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setPackage("com.android.chrome");
                startActivity(i);
            }
        });


    }

    void downloadWebVR() {
        final WebVRDownloadTask downloadTask = new WebVRDownloadTask(this);
        String[] fileList = {"README.md", "webvr/css/main.css", "webvr/files/data.json",
                "webvr/icons/cardboard64.png",
                "webvr/index.html",
                "webvr/js/controls.js",
                "webvr/js/displayData.js",
                "webvr/js/img/box.png",
                "webvr/js/main.js",
                "webvr/js/sortData.js",
                "webvr/js/third-party/jquery/jquery-3.2.1.min.js",
                "webvr/js/third-party/three/three.min.js",
                "webvr/js/third-party/webVR-ui/webvr-ui.min.js",
                "webvr/js/third-party/webVR/VRControls.js",
                "webvr/js/third-party/webVR/VREffect.js",
                "webvr/js/third-party/webVR/WebVR.js",
                "webvr/js/third-party/webVR/webvr-polyfill.min.js",
                "webvr/js/vrWorld.js",
                "webvr/textures/ground.jpg",
                "webvr/textures/groundmaybe.jpg"};
        for (String file : fileList)
            downloadTask.execute(file);
    }

    // usually, subclasses of AsyncTask are declared inside the activity class.
    // that way, you can easily modify the UI thread from here
    private class WebVRDownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public WebVRDownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            String BaseUrl = "https://github.com/cubuc/SW-Projekt-5.1/blob/master/";
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BaseUrl + sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream("");

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
    }
}




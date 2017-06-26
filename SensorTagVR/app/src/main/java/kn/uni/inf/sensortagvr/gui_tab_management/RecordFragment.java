package kn.uni.inf.sensortagvr.gui_tab_management;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import kn.uni.inf.sensortagvr.MainActivity;
import kn.uni.inf.sensortagvr.R;
import kn.uni.inf.sensortagvr.RecordActivity;
import kn.uni.inf.sensortagvr.stor.StorageMainService;

public class RecordFragment extends Fragment implements View.OnClickListener{

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         view =  inflater.inflate(R.layout.fragment_record, container, false);

        final Button calibrateButton = (Button) view.findViewById(R.id.callibrate);
        calibrateButton.setOnClickListener(this);

       // final Button calibrateButton = (Button) view.findViewById(R.id.callibrate);
       // calibrateButton.setOnClickListener(this);

        Intent bindStorService = new Intent(getActivity(), StorageMainService.class);
        getActivity().bindService(bindStorService, storageConnection, Context.BIND_AUTO_CREATE);



       // final Button buttonCal = (Button) getView().findViewById(R.id.callibrate);
       // buttonCal.setOnClickListener(new View.OnClickListener() {
           //onclick
       // });

        final Button buttonMD = (Button) getView().findViewById(R.id.measure);
        buttonMD.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (storageServiceBound)
                    storageService.measureData();
                else
                    Toast.makeText(getActivity().getApplicationContext(), "StorageService not connected", Toast.LENGTH_SHORT).show();
            }
        });

      /*  final Button writeButton = (Button) getView().findViewById(R.id.write);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                storageService.closeMeasureSession();
                Toast.makeText(getActivity().getApplicationContext(), "Write", Toast.LENGTH_SHORT).show();
            }
        }); */

        return view;
    }

    public void onClick(View v) {
        if (storageServiceBound) {
        }
        //storageService.calibrate();
        else
            Toast.makeText(getActivity().getApplicationContext(), "StorageService not connected", Toast.LENGTH_SHORT).show();
    }


    StorageMainService storageService;
    boolean storageServiceBound = false;
    private ServiceConnection storageConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StorageMainService.StorageBinder binder = (StorageMainService.StorageBinder) service;
            storageService = binder.getService();
            storageServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            storageServiceBound = false;
        }
    };

   /* @Override
    public void onClick(View v) {

    }*/
}

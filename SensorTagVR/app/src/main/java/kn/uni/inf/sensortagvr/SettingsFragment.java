package kn.uni.inf.sensortagvr;

import android.content.Intent;
import android.os.Bundle;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import kn.uni.inf.sensortagvr.tracking.TrackingTestActivity;

//import android.app.FragmentTransaction;

public class SettingsFragment extends Fragment implements View.OnClickListener{

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        Button trackButton = (Button) view.findViewById(R.id.button_track);
        Button scanButton = (Button) view.findViewById(R.id.button_scan);

        trackButton.setOnClickListener(this);
        scanButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        Fragment fragment = null;
        FragmentManager fragmentManager = getFragmentManager();

        switch (view.getId()) {
            case R.id.button_track:
                fragment  = new EmptyFragment();
                Log.e(this.getTag(), "empty clicked");
                break;

            case R.id.button_scan:
                fragment  = new BLEScanFragment();
                break;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(((ViewGroup)getView().getParent()).getId(), fragment);
        transaction.addToBackStack(null);

        transaction.commit();
    }
}

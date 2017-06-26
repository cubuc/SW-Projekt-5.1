package kn.uni.inf.sensortagvr.gui_tab_management;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kn.uni.inf.sensortagvr.R;
import kn.uni.inf.sensortagvr.ble.ScanListActivity;

public class SettingsFragment extends Fragment {

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        startActivity(new Intent(getActivity(), ScanListActivity.class));

        return view;
    }
}

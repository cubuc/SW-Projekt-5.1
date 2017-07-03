package kn.uni.inf.sensortagvr;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by deutsch on 03/07/2017.
 */

public class EmptyFragment extends Fragment {

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_empty, container, false);

        final Button Butt = (Button) view.findViewById(R.id.button3);
        Log.e(this.getTag(), "in empty");

        return view;
    }


}

package kn.uni.inf.sensortagvr.gui_tab_management;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import kn.uni.inf.sensortagvr.R;

public class VRFragment extends Fragment implements View.OnClickListener{

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_vr, container, false);
        Button vrButton = (Button) view.findViewById(R.id.start_VR);
        vrButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        String url = "http://localhost:12345/index.html";
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setPackage("com.android.chrome");
        startActivity(i);
    }

}



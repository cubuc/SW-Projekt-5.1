package kn.uni.inf.sensortagvr.gui_tab_management;

import android.content.Intent;
import android.os.Bundle;

import kn.uni.inf.sensortagvr.VRActivity;

public class TabGroup2Activity extends TabGroupActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startChildActivity("VRActivity", new Intent(this,VRActivity.class));
    }
}

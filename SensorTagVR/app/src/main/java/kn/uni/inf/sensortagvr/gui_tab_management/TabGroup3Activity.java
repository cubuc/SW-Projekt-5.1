package kn.uni.inf.sensortagvr.gui_tab_management;

import android.content.Intent;
import android.os.Bundle;

import kn.uni.inf.sensortagvr.SettingsActivity;

public class TabGroup3Activity extends TabGroupActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startChildActivity("SettingsActivity", new Intent(this,SettingsActivity.class));
    }
}

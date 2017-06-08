package kn.uni.inf.sensortagvr.gui_tab_management;

import android.content.Intent;
import android.os.Bundle;

import kn.uni.inf.sensortagvr.RecordActivity;

public class TabGroup1Activity extends TabGroupActivity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startChildActivity("RecordActivity", new Intent(this,RecordActivity.class));
    }
}

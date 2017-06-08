package com.example.deutsch.vrdata;

import android.content.Intent;
import android.os.Bundle;

public class TabGroup2Activity extends TabGroupActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startChildActivity("VRActivity", new Intent(this,VRActivity.class));
    }
}

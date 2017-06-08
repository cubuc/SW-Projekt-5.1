package com.example.deutsch.vrdata;

import android.content.Intent;
import android.os.Bundle;

public class TabGroup3Activity extends TabGroupActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startChildActivity("SettingsActivity", new Intent(this,SettingsActivity.class));
    }
}

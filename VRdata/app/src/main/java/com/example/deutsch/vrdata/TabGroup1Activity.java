package com.example.deutsch.vrdata;

import android.content.Intent;
import android.os.Bundle;

public class TabGroup1Activity extends TabGroupActivity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startChildActivity("RecordActivity", new Intent(this,RecordActivity.class));
    }
}

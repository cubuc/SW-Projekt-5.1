package com.example.deutsch.vrdata;

import android.app.TabActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TabHost;
import android.content.Intent;

import static com.example.deutsch.vrdata.R.drawable.rec;
import static com.example.deutsch.vrdata.R.drawable.set;
import static com.example.deutsch.vrdata.R.drawable.vr;

public class TabSample extends TabActivity {
    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TabHost tabHost = getTabHost();

        /*ImageView img1 = null;
        img1.setImageResource(rec);

        ImageView img2 = null;
        img2.setImageResource(vr);

        ImageView img3 = null;
        img3.setImageResource(set);*/

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rec);
        Drawable d = new BitmapDrawable(getResources(), bitmap);

      tabHost.addTab(tabHost.newTabSpec("tab1")
              .setIndicator("record data",d)
              //.setIndicator(img1)
              .setContent(new Intent(this, TabGroup1Activity.class)));
      
      tabHost.addTab(tabHost.newTabSpec("tab2")
              .setIndicator("vr view")
              //.setIndicator(img2)
              .setContent(new Intent(this, TabGroup2Activity.class)));

      tabHost.addTab(tabHost.newTabSpec("tab3")
              .setIndicator("settings")
              //.setIndicator(img3)
              .setContent(new Intent(this, TabGroup3Activity.class)));

      tabHost.setCurrentTab(MainActivity.firstActiveTab);
    }
}
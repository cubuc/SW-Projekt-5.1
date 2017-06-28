package kn.uni.inf.sensortagvr.gui_tab_management;

/**
 * Created by Lisa-Maria on 24/06/2017.
 * Following instructions from http://www.truiton.com/2015/06/android-tabs-example-fragments-viewpager/
 */


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new RecordFragment();
            case 1:
                return new VRFragment();
            case 2:
                return new LiveDataFragment();
            case 3:
                return new SettingsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {

        return mNumOfTabs;
    }
}
package com.dtv.killerradio;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Handler;
import android.provider.CallLog;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dtv.killerradio.db.SQLiteHelper;
import com.dtv.killerradio.keyhandling.BackKeyHandlingFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    SQLiteHelper sqLiteHelper = new SQLiteHelper(this);
    private static final String TAG = "MainActivity";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        HashMap<Integer, BackKeyHandlingFragment> mPageReferenceMap = new HashMap<>(getCount());
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int position) {
            if (!mPageReferenceMap.containsKey(position)) {
                BackKeyHandlingFragment fragment;
                switch (position) {
                    case 0:
                        fragment = FakeCallLogFragment.newInstance();
                        break;
//                    case 1:
//                        fragment = CallLogSchedulesFragment.newInstance();
//                        break;
                    case 1:
                        fragment = EditCallLogFragment.newInstance();
                        break;
                    default:
                        return null;
                }
                mPageReferenceMap.put(position, fragment);
            }
            return mPageReferenceMap.get(position);
        }

        public void destroyItem (ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            mPageReferenceMap.remove(position);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "FAKE CALL LOG";
//                case 1:
//                    return "CALL LOG SCHEDULES";
                case 1:
                    return "EDIT CALL LOG";
            }
            return null;
        }
    }

    public SQLiteHelper getSqLiteHelper() {
        return sqLiteHelper;
    }

    /**
     * Launches the FakeCallLogFragment. Used to add new call log from Schedules list screen.
     * Check if there is any better option to launch a target fragment from another fragment.
     */
    public void launchFakeCallLogFragment() {
        mViewPager.setCurrentItem(0);
    }

    public void launchCallLogSchedulesFragment() {
        mViewPager.setCurrentItem(1);
    }

    @Override
    public void onBackPressed() {

        BackKeyHandlingFragment fragment = (BackKeyHandlingFragment) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem());
        Log.d(TAG, "About to handle back key in " + fragment);
        if (!fragment.handleBackKey()) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }
}

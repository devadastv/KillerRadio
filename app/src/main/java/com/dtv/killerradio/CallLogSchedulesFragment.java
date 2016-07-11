package com.dtv.killerradio;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dtv.killerradio.db.SQLiteHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class CallLogSchedulesFragment extends Fragment {

    private static final String TAG = "CallLogSchedules";
    private SimpleCursorAdapter mAdapter;

    public CallLogSchedulesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_call_log_schedules, container, false);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).launchFakeCallLogFragment();
            }
        });

        ListView mCallLogSchedulesList = (ListView) rootView.findViewById(R.id.call_log_schedule_list);
        mCallLogSchedulesList.setEmptyView(rootView.findViewById(android.R.id.empty));

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {SQLiteHelper.CALL_LOG_COLUMN_NUMBER, SQLiteHelper.CALL_LOG_COLUMN_CALL_DATE};
        int[] toViews = {android.R.id.text1, android.R.id.text2};

        Cursor cursor = ((MainActivity) getActivity()).getSqLiteHelper().getFilteredList(null, null, null);
        Log.d(TAG, "No of call log schedule entries = " + cursor.getCount());
        mAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_2, cursor,
                fromColumns, toViews, 0);

        mCallLogSchedulesList.setAdapter(mAdapter);
        mCallLogSchedulesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent contentDetailsIntent = new Intent(CustomerListActivity.this, CustomerDetailsActivity.class);
//                Bundle extras = new Bundle();
//                extras.putInt(SURVEY_ITEM_INDEX, position);
//                contentDetailsIntent.putExtras(extras);
//                startActivity(contentDetailsIntent);
            }
        });
        return rootView;
    }

    /**
     * This method updates the listview when this fragments becomes visible. We can not use onResume here since ViewPager is used
     * in the activity. See for details:  http://stackoverflow.com/questions/10024739/how-to-determine-when-fragment-becomes-visible-in-viewpager
     *
     * @param visible
     */

    @Override
    public void setMenuVisibility(final boolean visible) {
        Log.d(TAG, "Inside setMenuVisibility of CallLogSchedulesListFragment...");
        super.setMenuVisibility(visible);
        if (visible) {
            updateCurrentFilterCursor();
        }
    }

    public void updateCurrentFilterCursor() {
        Cursor cursor = ((MainActivity) getActivity()).getSqLiteHelper().getFilteredList(null, null, null);
        mAdapter.changeCursor(cursor);
    }
}

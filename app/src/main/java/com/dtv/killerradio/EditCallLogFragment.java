package com.dtv.killerradio;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dtv.killerradio.calllog.CallLogEntry;
import com.dtv.killerradio.calllog.CallLogUtility;
import com.dtv.killerradio.keyhandling.BackKeyHandlingFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditCallLogFragment extends CommonCallLogEntryFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "EditCallLogFragment";

    private CursorAdapter mAdapter;
    private ProgressBar mProgressBar;
    private ViewGroup rootView;
    private volatile boolean isEditViewVisible;

    public EditCallLogFragment() {
    }

    public static EditCallLogFragment newInstance() {
        EditCallLogFragment fragment = new EditCallLogFragment();
        return fragment;
    }

    @Override
    public View onCreateViewExtra(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Inside FakeCalllogFragment - onCreateView method");
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_edit_call_log, container, false);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.calllog_edit_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        initializeLogListComponents(rootView);
        initializeLogEditComponents(rootView);
        getLoaderManager().initLoader(0, null, this);
        return rootView;
    }

    private void initializeLogListComponents(final ViewGroup rootView) {
        ListView mEditCallLogList = (ListView) rootView.findViewById(R.id.calllog_list);
        mEditCallLogList.setEmptyView(rootView.findViewById(android.R.id.empty));
        mAdapter = new CallLogListCursorAdapter(this.getActivity(), null);
        mEditCallLogList.setAdapter(mAdapter);
        mEditCallLogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "The user clicked on log item: " + position);
                switchToEditScreen(rootView);
                displayLogDetailsInEditScreen(position);
            }
        });
    }

    private void initializeLogEditComponents(final ViewGroup rootView) {
        Button mDiscardButton = (Button) rootView.findViewById(R.id.discard_button);
        mDiscardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToListScreen(rootView);
            }
        });
    }

    @Override
    protected void postProcessSubmission() {
        switchToListScreen(rootView);
    }

    private void switchToEditScreen(View rootView) {
        LinearLayout listScreen = (LinearLayout) rootView.findViewById(R.id.callloglist_layout);
        listScreen.setVisibility(View.GONE);
        ScrollView editScreen = (ScrollView) rootView.findViewById(R.id.calllog_edit_layout);
        editScreen.setVisibility(View.VISIBLE);
        isEditViewVisible = true;
        Log.d(TAG, "On switchToEditScreen : isEditViewVisible = " + isEditViewVisible);
    }

    private void switchToListScreen(View rootView) {
        ScrollView editView = (ScrollView) rootView.findViewById(R.id.calllog_edit_layout);
        editView.setVisibility(View.GONE);
        LinearLayout listView = (LinearLayout) rootView.findViewById(R.id.callloglist_layout);
        listView.setVisibility(View.VISIBLE);
        isEditViewVisible = false;
        Log.d(TAG, "On switchToListScreen : isEditViewVisible = " + isEditViewVisible);
    }

    @Override
    protected boolean handleSubmit(CallLogEntry callLogEntry) {
        CallLogUtility.getInstance().deleteCallLogById(callLogEntry, getActivity());
        CallLogUtility.getInstance().addCallLog(callLogEntry, getActivity());
        return true;
    }

    private void displayLogDetailsInEditScreen(int position) {
        Cursor cursor = mAdapter.getCursor();
        if (cursor != null && cursor.moveToPosition(position)) {
            callLogEntry.updateValuesFromCallLogCursor(cursor);
            initFieldsToValuesInCallLogEntry();
        } else {
            Log.e(TAG, "Display of selected log in editor failed. Either cursor is null or index is out of range. Check: cursor = " + cursor);
            if (null != cursor) {
                Log.e(TAG, "Cursor count = " + cursor.getCount() + " and requested position = " + position);
            }
            Toast.makeText(getActivity(), getString(R.string.error_call_log_edit_message), Toast.LENGTH_LONG).show();
            switchToListScreen(rootView);
        }
    }

    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(getActivity(), CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC");
    }

    // Called when a previously created loader has finished loading
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        Log.d(TAG, "Inside onLoadFinished with cursor " + data);
        handleCursorUpdate(data);
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        handleCursorUpdate(null);
    }

    private void handleCursorUpdate(Cursor cursor) {
        mProgressBar.setVisibility(View.GONE);
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        Log.d(TAG, "Inside setMenuVisibility of EditCallLogFragment...");
        super.setMenuVisibility(visible);
        if (visible && (null != rootView)) {
            switchToListScreen(rootView);
        }
    }

    @Override
    public boolean handleBackKey() {
        Log.d(TAG, "isEditViewVisible " + isEditViewVisible);
        if (isEditViewVisible) {
            switchToListScreen(rootView);
            return true;
        } else {
            return false;
        }
    }
}

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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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
public class EditCallLogFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>  {

    private static final String TAG = "EditCallLogFragment";

    // This is the Adapter being used to display the list's data
    SimpleCursorAdapter mAdapter;

    private ProgressBar progressBar;

    public EditCallLogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Inside FakeCalllogFragment - onCreateView method");
        final ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_edit_call_log, container, false);

        progressBar = (ProgressBar) rootView.findViewById(R.id.calllog_edit_progressbar);
        progressBar.setVisibility(View.VISIBLE);

        ListView mEditCallLogList = (ListView) rootView.findViewById(R.id.calllog_list);
        mEditCallLogList.setEmptyView(rootView.findViewById(android.R.id.empty));

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {CallLog.Calls.NUMBER, CallLog.Calls.TYPE};
        int[] toViews = {android.R.id.text1, android.R.id.text2};

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        mAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_2, null,
                fromColumns, toViews, 0);
        mEditCallLogList.setAdapter(mAdapter);
        mEditCallLogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout layout1 = (LinearLayout) rootView.findViewById(R.id.callloglist_layout);
                layout1.setVisibility(View.GONE);
                ScrollView layout2 = (ScrollView) rootView.findViewById(R.id.calllog_edit_layout);
                layout2.setVisibility(View.VISIBLE);
//                Intent contentDetailsIntent = new Intent(CustomerListActivity.this, CustomerDetailsActivity.class);
//                Bundle extras = new Bundle();
//                extras.putInt(SURVEY_ITEM_INDEX, position);
//                contentDetailsIntent.putExtras(extras);
//                startActivity(contentDetailsIntent);
            }
        });

        Button mSubmitButton = (Button) rootView.findViewById(R.id.submit_button);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScrollView layout1 = (ScrollView) rootView.findViewById(R.id.calllog_edit_layout);
                layout1.setVisibility(View.GONE);
                LinearLayout layout2 = (LinearLayout) rootView.findViewById(R.id.callloglist_layout);
                layout2.setVisibility(View.VISIBLE);
            }
        });
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);

        return rootView;
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
        progressBar.setVisibility(View.GONE);
        mAdapter.swapCursor(data);
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        progressBar.setVisibility(View.GONE);
        mAdapter.swapCursor(null);
    }
}

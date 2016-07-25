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
import android.widget.ScrollView;
import android.widget.TimePicker;
import android.widget.Toast;

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
public class EditCallLogFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "EditCallLogFragment";

    private SimpleCursorAdapter mAdapter;

    private ProgressBar mProgressBar;

    private static EditText mTimeOfCall;
    private static EditText mDateOfCall;

    // Call log entry date and time
    private static int year;
    private static int month;
    private static int day;
    private static int hourOfDay; // Hour of day always - 24 hours
    private static int minute;

    private EditText mPhoneNumber;
    private EditText mCallDuration;
    private EditText mCallType;
    private int callTypeIndex;
    private ViewGroup rootView;
    private String selectedLogId;


    public EditCallLogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
                Log.d(TAG, "The user clicked on log item: " + position);
                switchToEditScreen(rootView);
                displayLogDetailsInEditScreen(position);
            }
        });
    }

    private void initializeLogEditComponents(final ViewGroup rootView) {
        mPhoneNumber = (EditText) rootView.findViewById(R.id.phone_number);
        mCallDuration = (EditText) rootView.findViewById(R.id.call_duration);

        mDateOfCall = (EditText) rootView.findViewById(R.id.date_of_call);
        mDateOfCall.setInputType(InputType.TYPE_NULL);
        mDateOfCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            }
        });

        mTimeOfCall = (EditText) rootView.findViewById(R.id.time_of_call);
        mTimeOfCall.setInputType(InputType.TYPE_NULL);
        mTimeOfCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
            }
        });

        ImageButton contactButton = (ImageButton) rootView.findViewById(R.id.contact_icon);
        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectContact();
            }
        });

        final String[] menuArray = getResources().getStringArray(R.array.call_types);
        mCallType = (EditText) rootView.findViewById(R.id.call_type);
        mCallType.setInputType(InputType.TYPE_NULL);
        mCallType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Call Type:");
                builder.setCancelable(true);
                AlertDialog dialog = builder.create();
                dialog.getListView();
                builder.setSingleChoiceItems(menuArray, callTypeIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("SurveyList", "User selected " + which);
                        callTypeIndex = which;
                        mCallType.setText(menuArray[which]);
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        Button mSubmitButton = (Button) rootView.findViewById(R.id.submit_button);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptDataSubmit();
                switchToListScreen(rootView);
            }
        });
    }

    private void switchToEditScreen(View rootView) {
        LinearLayout listScreen = (LinearLayout) rootView.findViewById(R.id.callloglist_layout);
        listScreen.setVisibility(View.GONE);
        ScrollView editScreen = (ScrollView) rootView.findViewById(R.id.calllog_edit_layout);
        editScreen.setVisibility(View.VISIBLE);
    }

    private void switchToListScreen(View rootView) {
        ScrollView editView = (ScrollView) rootView.findViewById(R.id.calllog_edit_layout);
        editView.setVisibility(View.GONE);
        LinearLayout listView = (LinearLayout) rootView.findViewById(R.id.callloglist_layout);
        listView.setVisibility(View.VISIBLE);
    }

    private boolean attemptDataSubmit() {
        // Reset errors.
        mPhoneNumber.setError(null);
        mCallDuration.setError(null);

        // Store values at the time of the login attempt.
        String phoneNumber = mPhoneNumber.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid name, if the user entered one.
        if (TextUtils.isEmpty(phoneNumber.trim())) {
            mPhoneNumber.setError("The phone number is empty");
            focusView = mPhoneNumber;
            cancel = true;
        }

        int duration = 0;
        if (!cancel) {
            try {
                Log.d(TAG, "mCallDuration.getText() = " + mCallDuration.getText());
                duration = Integer.parseInt(mCallDuration.getText().toString().trim());
            } catch (NumberFormatException e) {
                mCallDuration.setError("The duration should be a number");
                focusView = mCallDuration;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt submit and focus the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        } else {
            Calendar calendarForCallLog = Calendar.getInstance();
            Log.d(TAG, "Schedule : Cached values - year = " + year + ", month = " + month
                    + ", day = " + day + ", hourOfDay = " + hourOfDay + ", minute = " + minute);
            calendarForCallLog.set(year, month, day, hourOfDay, minute);
            Log.d(TAG, "Calendar for schedule = " + calendarForCallLog);

            int callTypeToSet;
            switch (callTypeIndex) {
                case 0:
                    callTypeToSet = CallLog.Calls.INCOMING_TYPE;
                    break;
                case 1:
                    callTypeToSet = CallLog.Calls.OUTGOING_TYPE;
                    break;
                case 2:
                    callTypeToSet = CallLog.Calls.MISSED_TYPE;
                    break;
                default:
                    callTypeToSet = CallLog.Calls.INCOMING_TYPE;
            }
            deleteSelectedCallLog();
            addFakeCallLog(phoneNumber, calendarForCallLog, duration, callTypeToSet);
            return true;
        }
    }

    private void deleteSelectedCallLog() {
        getActivity().getContentResolver().delete(CallLog.Calls.CONTENT_URI, CallLog.Calls._ID + " = ? ",
                new String[]{String.valueOf(selectedLogId)});
        Toast.makeText(getActivity(), "Call log at selectedLogId " + selectedLogId + " is deleted !", Toast.LENGTH_SHORT).show();
    }

    private void addFakeCallLog(String phoneNumber, Calendar calendarForCallLog, int duration, int callTypeToSet) {
        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, phoneNumber);
        values.put(CallLog.Calls.DATE, calendarForCallLog.getTimeInMillis());
        values.put(CallLog.Calls.DURATION, duration);
        values.put(CallLog.Calls.TYPE, callTypeToSet);
        values.put(CallLog.Calls.NEW, 1);
        values.put(CallLog.Calls.CACHED_NAME, "");
        values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
        values.put(CallLog.Calls.CACHED_NUMBER_LABEL, "");
        Log.d(TAG, "Inserting call log placeholder for " + phoneNumber);
        getActivity().getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
        Toast.makeText(getActivity(), "The fake call log is successfully added!", Toast.LENGTH_SHORT).show();
    }

    private void displayLogDetailsInEditScreen(int position) {
        Cursor cursor = mAdapter.getCursor();
        if (cursor != null && cursor.moveToPosition(position)) {
            cursor.moveToPosition(position);

            // Read values
            String phNumber = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            Calendar callDayTime = Calendar.getInstance();
            callDayTime.setTimeInMillis(Long.valueOf(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE))));
            String callDuration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));
            selectedLogId = cursor.getString(cursor.getColumnIndex(CallLog.Calls._ID));
            Log.d(TAG, "The id of the selected entry = " + selectedLogId);
            callTypeIndex = 0;
            int callType = Integer.parseInt(cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)));
            switch (callType) {
                case CallLog.Calls.INCOMING_TYPE:
                    callTypeIndex = 0;
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    callTypeIndex = 1;
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    callTypeIndex = 2;
                    break;
            }

            // Set values to UI
            mPhoneNumber.setText(phNumber);
            initTimeOfCall(callDayTime);
            mCallDuration.setText(callDuration);
            final String[] menuArray = getResources().getStringArray(R.array.call_types);
            mCallType.setText(menuArray[callTypeIndex]);
        } else {
            Log.e(TAG, "Display of selected log in editor failed. Either cursor is null or index is out of range. Check: cursor = " + cursor);
            if (null != cursor) {
                Log.e(TAG, "Cursor count = " + cursor.getCount() + " and requested position = " + position);
            }
        }
    }

    private void initTimeOfCall(Calendar calendar) {
        updateDateOfCall(calendar);
        updateTimeOfCall(calendar);
    }

    static final int PICK_CONTACT = 1;

    private void selectContact() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            final EditText phoneInput = mPhoneNumber; // (EditText) findViewById(R.id.phoneNumberInput);
            Cursor cursor = null;
            String phoneNumber = "";
            List<String> allNumbers = new ArrayList<String>();
            int phoneIdx = 0;
            try {
                Uri result = data.getData();
                String id = result.getLastPathSegment();
                cursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{id}, null);
                phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        phoneNumber = cursor.getString(phoneIdx);
                        allNumbers.add(phoneNumber);
                        cursor.moveToNext();
                    }
                } else {
                    Toast.makeText(getActivity(), "There are no phone number associated with this contact [1]", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Some error happened while getting details for this contact.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }

                final CharSequence[] items = allNumbers.toArray(new String[allNumbers.size()]);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Choose a number");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        String selectedNumber = items[item].toString();
                        selectedNumber = selectedNumber.replace("-", "").replace("(", "").replace(")", "").replace(" ", "");
                        phoneInput.setText(selectedNumber);
                    }
                });
                AlertDialog alert = builder.create();
                if (allNumbers.size() > 1) {
                    alert.show();
                } else {
                    String selectedNumber = phoneNumber.toString();
                    selectedNumber = selectedNumber.replace("-", "").replace("(", "").replace(")", "").replace(" ", "");
                    phoneInput.setText(selectedNumber);
                }

                if (phoneNumber.length() == 0) {
                    Toast.makeText(getActivity(), "There are no phone number associated with this contact [2]", Toast.LENGTH_SHORT).show();
                }
            }
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
        mProgressBar.setVisibility(View.GONE);
        mAdapter.swapCursor(data);
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mProgressBar.setVisibility(View.GONE);
        mAdapter.swapCursor(null);
    }


    @Override
    public void setMenuVisibility(final boolean visible) {
        Log.d(TAG, "Inside setMenuVisibility of CallLogSchedulesListFragment...");
        super.setMenuVisibility(visible);
        if (visible && (null != rootView)) {
            ScrollView layout1 = (ScrollView) rootView.findViewById(R.id.calllog_edit_layout);
            layout1.setVisibility(View.GONE);
            LinearLayout layout2 = (LinearLayout) rootView.findViewById(R.id.callloglist_layout);
            layout2.setVisibility(View.VISIBLE);
        }
    }

    private static void updateDateOfCall(Calendar calendar) {
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        mDateOfCall.setText(dateFormatter.format(calendar.getTime()));
    }

    private static void updateTimeOfCall(Calendar calendar) {
        hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm a", Locale.US);
        mTimeOfCall.setText(dateFormatter.format(calendar.getTime()));
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker if there is no date already set in DatePicker
            final Calendar c = Calendar.getInstance();
            if (year > 0 && month > 0 && day > 0) {
                c.set(year, month, day);
            }

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, day);
            EditCallLogFragment.updateDateOfCall(newDate);
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            if (hourOfDay > 0 && minute > 0) {
                c.set(year, month, day, hourOfDay, minute);
            }
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, day, hourOfDay, minute);
            EditCallLogFragment.updateTimeOfCall(newDate);
        }
    }
}

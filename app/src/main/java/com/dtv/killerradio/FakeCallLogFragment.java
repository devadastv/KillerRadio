package com.dtv.killerradio;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dtv.killerradio.calllog.CallLogEntry;
import com.dtv.killerradio.calllog.CallLogUtility;
import com.dtv.killerradio.keyhandling.BackKeyHandlingFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class FakeCallLogFragment extends BackKeyHandlingFragment {

    private static final String TAG = "FakeCallLogFragment";

    private EditText mPhoneNumber;
    private static EditText mTimeOfCall;
    private static EditText mDateOfCall;
    private EditText mCallDuration;
    private EditText mCallType;
    private static CheckBox mInsertTimeCheckBox;

    private static EditText mTimeOfInsertion;
    private static EditText mDateOfInsertion;

    // Call log entry insertion - Date and time
    private static int callLogInsertionYear;
    private static int callLogInsertionMonth;
    private static int callLogInsertionDay;
    private static int callLogInsertionHourOfDay; // Hour of day always - 24 hours
    private static int callLogInsertionMinute;

    private static CallLogEntry callLogEntry;

    public FakeCallLogFragment() {
    }

    public static FakeCallLogFragment newInstance() {
        FakeCallLogFragment myFragment = new FakeCallLogFragment();
        return myFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Inside FakeCalllogFragment - onCreateView method");

        View rootView = inflater.inflate(R.layout.fragment_fake_call_log, container, false);
        mPhoneNumber = (EditText) rootView.findViewById(R.id.phone_number);
        mCallDuration = (EditText) rootView.findViewById(R.id.call_duration);

        mCallDuration.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                callLogEntry.setCallDuration(mCallDuration.getText().toString());
                mCallDuration.setText(callLogEntry.getCallDurationTextForDisplay(true));
                return false;
            }
        });

        mCallDuration.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                callLogEntry.setCallDuration(mCallDuration.getText().toString());
                mCallDuration.setText(callLogEntry.getCallDurationTextForDisplay(hasFocus));
                if (hasFocus) {
                    v.performClick();
                }
            }
        });

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

        mCallType = (EditText) rootView.findViewById(R.id.call_type);
        mCallType.setInputType(InputType.TYPE_NULL); //TODO: Move to xml?
        mCallType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Call Type:");
                builder.setCancelable(true);
                AlertDialog dialog = builder.create();
                dialog.getListView();
                builder.setSingleChoiceItems(callLogEntry.getCallTypeStringArray(), callLogEntry.getCallType(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("SurveyList", "User selected " + which);
                        callLogEntry.setCallType(which);
                        mCallType.setText(callLogEntry.getCallTypeString());
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        mInsertTimeCheckBox = (CheckBox) rootView.findViewById(R.id.insert_time_checkbox);
        float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
        mInsertTimeCheckBox.setTextSize(mTimeOfCall.getTextSize() / scaledDensity);
        mInsertTimeCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInsertTimeCheckBox.isChecked()) {
                    initInsertTimeWithCurrentTime();
                }
            }
        });

        mDateOfInsertion = (EditText) rootView.findViewById(R.id.date_of_insertion);
        mDateOfInsertion.setInputType(InputType.TYPE_NULL);
        mDateOfInsertion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragmentInsertDate();
                newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            }
        });

        mTimeOfInsertion = (EditText) rootView.findViewById(R.id.time_of_insertion);
        mTimeOfInsertion.setInputType(InputType.TYPE_NULL);
        mTimeOfInsertion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragmentInsertTime();
                newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
            }
        });

        initInsertTimeWithCurrentTime();

        Button submitButton = (Button) rootView.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptDataSubmit();
            }
        });

        initFieldsToDefaultValues();
        return rootView;
    }

    private void initDateAndTimeOfCall() {
        updateDateOfCall();
        updateTimeOfCall();
    }

    private void initInsertTimeWithCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        updateDateOfInsertion(calendar, false);
        updateTimeOfInsertion(calendar, false);
    }

    private void initFieldsToDefaultValues() {
        callLogEntry = new CallLogEntry(getActivity());
        mPhoneNumber.setText(callLogEntry.getPhoneNumber());
        initDateAndTimeOfCall();
        mCallDuration.setText(callLogEntry.getCallDurationTextForDisplay());
        mCallType.setText(callLogEntry.getCallTypeString());
        mInsertTimeCheckBox.setChecked(true);
        initInsertTimeWithCurrentTime();
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
            Cursor cursor = null;
            String phoneNumber = "";
            List<String> allNumbers = new ArrayList<String>();
            int phoneIdx = 0;
            try {
                Uri result = data.getData();
                String id = result.getLastPathSegment();
                cursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{id}, null);
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
                        callLogEntry.setPhoneNumber(selectedNumber);
                        mPhoneNumber.setText(callLogEntry.getPhoneNumber());
                    }
                });
                AlertDialog alert = builder.create();
                if (allNumbers.size() > 1) {
                    alert.show();
                } else {
                    String selectedNumber = phoneNumber;
                    selectedNumber = selectedNumber.replace("-", "").replace("(", "").replace(")", "").replace(" ", "");

                    callLogEntry.setPhoneNumber(selectedNumber);
                    Log.d(TAG, "selectedNumber = " + callLogEntry.getPhoneNumber());
                    mPhoneNumber.requestFocus();
                    mPhoneNumber.setText(callLogEntry.getPhoneNumber());
                    Log.d(TAG, "gettext = " + mPhoneNumber.getText().toString());
                }

                if (phoneNumber.length() == 0) {
                    Toast.makeText(getActivity(), "There are no phone number associated with this contact [2]", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private static void updateDateOfCall() {
        mDateOfCall.setText(callLogEntry.getDateTextForDisplay());
    }

    private static void updateTimeOfCall() {
        mTimeOfCall.setText(callLogEntry.getTimeTextForDisplay());
    }

    private static void updateDateOfInsertion(Calendar calendar, boolean resetCheckBox) {
        if ((callLogInsertionYear != 0 && callLogInsertionYear != calendar.get(Calendar.YEAR) ||
                callLogInsertionMonth != 0 && callLogInsertionMonth != calendar.get(Calendar.MONTH) ||
                callLogInsertionDay != 0 && callLogInsertionDay != calendar.get(Calendar.DAY_OF_MONTH))
                && resetCheckBox) {
            mInsertTimeCheckBox.setChecked(false);
        }
        callLogInsertionYear = calendar.get(Calendar.YEAR);
        callLogInsertionMonth = calendar.get(Calendar.MONTH);
        callLogInsertionDay = calendar.get(Calendar.DAY_OF_MONTH);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        mDateOfInsertion.setText(dateFormatter.format(calendar.getTime()));
    }

    private static void updateTimeOfInsertion(Calendar calendar, boolean resetCheckBox) {
        if ((callLogInsertionHourOfDay != 0 && callLogInsertionHourOfDay != calendar.get(Calendar.HOUR_OF_DAY) ||
                callLogInsertionMinute != 0 && callLogInsertionMinute != calendar.get(Calendar.MINUTE))
                && resetCheckBox) {
            mInsertTimeCheckBox.setChecked(false);
        }
        callLogInsertionHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        callLogInsertionMinute = calendar.get(Calendar.MINUTE);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm a", Locale.US);
        mTimeOfInsertion.setText(dateFormatter.format(calendar.getTime()));
    }

    private void attemptDataSubmit() {

        boolean cancel = false;
        View focusView = null;

        // Reset errors.
        mPhoneNumber.setError(null);
        mCallDuration.setError(null);

        callLogEntry.setPhoneNumber(mPhoneNumber.getText().toString());
        if (callLogEntry.isPhoneNumberValid()) {
            mPhoneNumber.setError("The phone number is empty");
            focusView = mPhoneNumber;
            cancel = true;
        }
        if (!cancel) {
            callLogEntry.setCallDuration(mCallDuration.getText().toString());
            if (!callLogEntry.isDurationValid()) {
                mCallDuration.setError("The duration should be a number");
                focusView = mCallDuration;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt submit and focus the first
            // form field with an error.
            focusView.requestFocus();
            return;
        } else {

            // Insert Time
            Calendar insertTime = Calendar.getInstance();
            Log.d(TAG, "Insert time - year = " + callLogInsertionYear + ", month = " + callLogInsertionMonth
                    + ", day = " + callLogInsertionDay + ", hourOfDay = " + callLogInsertionHourOfDay
                    + ", minute = " + callLogInsertionMinute);
            insertTime.set(callLogInsertionYear, callLogInsertionMonth, callLogInsertionDay, callLogInsertionHourOfDay, callLogInsertionMinute);
            Log.d(TAG, "Insertion time = " + insertTime);
            if (mInsertTimeCheckBox.isChecked() || insertTime.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis()) {
                CallLogUtility.getInstance().addCallLog(callLogEntry, getActivity());
            } else {
                Toast.makeText(getActivity(), "The fake call log is added to the schedule", Toast.LENGTH_SHORT).show();
                ((MainActivity) getActivity()).getSqLiteHelper().createCallLogSchedule(callLogEntry.getPhoneNumber(),
                        callLogEntry.getCallLogTimeInMillis(), callLogEntry.getCallDurationToSet(), callLogEntry.getCallTypeToSet(), 1, "", 0, "");
                ((MainActivity) getActivity()).launchCallLogSchedulesFragment();
            }
            initFieldsToDefaultValues();
        }
    }

    @Override
    public boolean handleBackKey() {
        return false;
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker if there is no date already set in DatePicker
            final Calendar c = Calendar.getInstance();
            CallLogEntry callLogEntry = FakeCallLogFragment.callLogEntry;

            int year = callLogEntry.getYear();
            int month = callLogEntry.getMonth();
            int day = callLogEntry.getDay();
            if (year > 0 && month > 0 && day > 0) {
                c.set(year, month, day);
            }
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, day);
            FakeCallLogFragment.callLogEntry.updateDateOfCall(newDate);
            FakeCallLogFragment.updateDateOfCall();
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            CallLogEntry callLogEntry = FakeCallLogFragment.callLogEntry;
            int hourOfDay = callLogEntry.getHourOfDay();
            int minute = callLogEntry.getMinute();
            if (hourOfDay > 0 && minute > 0) {
                c.set(callLogEntry.getYear(), callLogEntry.getMonth(), callLogEntry.getDay(), hourOfDay, minute);
            }
            hourOfDay = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hourOfDay, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar newDate = Calendar.getInstance();
            CallLogEntry callLogEntry = FakeCallLogFragment.callLogEntry;
            newDate.set(callLogEntry.getYear(), callLogEntry.getMonth(),
                    callLogEntry.getDay(), hourOfDay, minute);
            FakeCallLogFragment.callLogEntry.updateTimeOfCall(newDate);
            FakeCallLogFragment.updateTimeOfCall();
        }
    }

    public static class DatePickerFragmentInsertDate extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker if there is no date already set in DatePicker
            final Calendar c = Calendar.getInstance();
            if (callLogInsertionYear > 0 && callLogInsertionMonth > 0 && callLogInsertionDay > 0) {
                c.set(callLogInsertionYear, callLogInsertionMonth, callLogInsertionDay);
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
            FakeCallLogFragment.updateDateOfInsertion(newDate, true);
        }
    }

    public static class TimePickerFragmentInsertTime extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            if (callLogInsertionHourOfDay > 0 && callLogInsertionMinute > 0) {
                c.set(callLogInsertionYear, callLogInsertionMonth, callLogInsertionDay, callLogInsertionHourOfDay, callLogInsertionMinute);
            }
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar newDate = Calendar.getInstance();
            newDate.set(callLogInsertionYear, callLogInsertionMonth, callLogInsertionDay, hourOfDay, minute);
            FakeCallLogFragment.updateTimeOfInsertion(newDate, true);
//            FakeCallLogFragment.callLogEntry.setMonth(4);
        }
    }
}

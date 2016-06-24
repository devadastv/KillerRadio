package com.dtv.killerradio;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class FakeCallLogFragment extends Fragment {

    private static final String TAG = "FakeCallLogFragment";
    private static EditText mTimeOfCall;
    private static EditText mDateOfCall;

    private static int year;
    private static int month;
    private static int day;
    private static int hourOfDay; // Hour of day always - 24 hours
    private static int minute;
    private static int am_pm;
    private EditText mPhoneNumber;

    public FakeCallLogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_fake_call_log, container, false);
        mPhoneNumber = (EditText) rootView.findViewById(R.id.phone_number);
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
        Calendar calendar = Calendar.getInstance();
        updateDateOfCall(calendar);
        updateTimeOfCall(calendar);

        Button clickButton = (Button) rootView.findViewById(R.id.submit_button);
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptDataSubmit();
            }
        });
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        year = month = day = hourOfDay = minute = 0;
    }

    private static void updateDateOfCall(Calendar calendar)
    {
        year = calendar.get(Calendar.YEAR);;
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.AM_PM);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        mDateOfCall.setText(dateFormatter.format(calendar.getTime()));
    }

    private static void updateTimeOfCall (Calendar calendar)
    {
        hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);;
        minute = calendar.get(Calendar.MINUTE);
        am_pm = calendar.get(Calendar.AM_PM);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm a", Locale.US);
        mTimeOfCall.setText(dateFormatter.format(calendar.getTime()));
    }

    private void attemptDataSubmit() {
        // Reset errors.
        mPhoneNumber.setError(null);

        // Store values at the time of the login attempt.
        String phoneNumber = mPhoneNumber.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid name, if the user entered one.
        if (TextUtils.isEmpty(phoneNumber.trim())) {
            mPhoneNumber.setError("The name is empty");
            focusView = mPhoneNumber;
            cancel = true;
        }

        // Check for a valid mobile number.
//        if (!cancel && TextUtils.isEmpty(mobileNumber) && !isPhoneNumberValid(mobileNumber)) {
//            mMobileNumber.setError("Mobile number should contain at least 10 digits");
//            focusView = mMobileNumber;
//            cancel = true;
//        }

        if (cancel) {
            // There was an error; don't attempt submit and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, hourOfDay, minute);
            ContentValues values = new ContentValues();
            values.put(CallLog.Calls.NUMBER, phoneNumber);
            values.put(CallLog.Calls.DATE, calendar.getTimeInMillis());
            values.put(CallLog.Calls.DURATION, 0);
            values.put(CallLog.Calls.TYPE, CallLog.Calls.OUTGOING_TYPE);
            values.put(CallLog.Calls.NEW, 1);
            values.put(CallLog.Calls.CACHED_NAME, "");
            values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
            values.put(CallLog.Calls.CACHED_NUMBER_LABEL, "");
            Log.d(TAG, "Inserting call log placeholder for " + phoneNumber);
            getActivity().getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
            Toast.makeText(getActivity(), "The fake call log is successfully added!", Toast.LENGTH_SHORT).show();
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

//        private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker if there is no date already set in DatePicker
            final Calendar c = Calendar.getInstance();
            if (year > 0 && month > 0 && day > 0)
            {
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
            FakeCallLogFragment.updateDateOfCall(newDate);
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            if (hourOfDay > 0 && minute > 0)
            {
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
            FakeCallLogFragment.updateTimeOfCall(newDate);
        }
    }
}

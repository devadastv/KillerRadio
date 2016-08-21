package com.dtv.killerradio;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class FakeCallLogFragment extends CommonCallLogEntryFragment {

    private static final String TAG = "FakeCallLogFragment";

    private CheckBox mInsertTimeCheckBox;
    private EditText mTimeOfInsertion;
    private EditText mDateOfInsertion;

    // TODO: Scheduling is not implemented and tested
    private Calendar insertTimeCalendar;

    static final int SELECT_INSERT_DATE = 4;
    static final int SELECT_INSERT_TIME = 5;

    public FakeCallLogFragment() {
    }

    public static FakeCallLogFragment newInstance() {
        FakeCallLogFragment myFragment = new FakeCallLogFragment();
        return myFragment;
    }

    @Override
    public View onCreateViewExtra(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Inside FakeCalllogFragment - onCreateView method");

        View rootView = inflater.inflate(R.layout.fragment_fake_call_log, container, false);

        if (AppConstants.CALLLOG_SCHEDULE_ENABLED) {
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

                    DialogFragment newFragment = DatePickerFragment.newInstance(insertTimeCalendar);
                    newFragment.setTargetFragment(FakeCallLogFragment.this, SELECT_INSERT_DATE);
                    newFragment.show(getActivity().getSupportFragmentManager(), "insertDatePicker");
                }
            });

            mTimeOfInsertion = (EditText) rootView.findViewById(R.id.time_of_insertion);
            mTimeOfInsertion.setInputType(InputType.TYPE_NULL);
            mTimeOfInsertion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment newFragment = TimePickerFragment.newInstance(insertTimeCalendar);
                    newFragment.setTargetFragment(FakeCallLogFragment.this, SELECT_INSERT_TIME);
                    newFragment.show(getActivity().getSupportFragmentManager(), "insertTimePicker");
                }
            });
            initFieldsToDefaultValues();
        }
        return rootView;
    }

    private void initInsertTimeWithCurrentTime() {
        if (AppConstants.CALLLOG_SCHEDULE_ENABLED) {
            // TODO: Implement when schedule is going to be enabled
        }
    }

    protected void initFieldsToDefaultValues() {
        super.initFieldsToDefaultValues();
        if (AppConstants.CALLLOG_SCHEDULE_ENABLED) {
            mInsertTimeCheckBox.setChecked(true);
            initInsertTimeWithCurrentTime();
        }
    }

    @Override
    public boolean handleBackKey() {
        return false;
    }
}

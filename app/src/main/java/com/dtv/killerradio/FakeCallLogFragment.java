package com.dtv.killerradio;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dtv.killerradio.calllog.CallLogEntry;
import com.dtv.killerradio.calllog.CallLogUtility;
import com.dtv.killerradio.keyhandling.BackKeyHandlingFragment;
import com.dtv.killerradio.util.ContactsUtil;
import com.dtv.killerradio.util.ImageLoader;
import com.dtv.killerradio.util.Utils;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    private RadioGroup callTypeRadioGroup;
    private ImageLoader mImageLoader;

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

        mCallDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callLogEntry.setCallDuration(mCallDuration.getText().toString());
                mCallDuration.setText(callLogEntry.getCallDurationTextForDisplay(true));
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

        if (AppConstants.CALLTYPE_SELECTION_USING_RADIO) {
            callTypeRadioGroup = (RadioGroup) rootView.findViewById(R.id.call_type_radiogroup);
            callTypeRadioGroup.setVisibility(View.VISIBLE);
            callTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int id) {
                    Log.d(TAG, "User selected radiobutton = " + id);
                    int callType;
                    switch (id) {
                        case R.id.incoming_type_radiobutton:
                            callType = 0;
                            break;
                        case R.id.outgoing_type_radiobutton:
                            callType = 1;
                            break;
                        case R.id.missedcall_type_radiobutton:
                            callType = 2;
                            break;
                        default:
                            callType = 0;
                    }
                    callLogEntry.setCallType(callType);
                }
            });
        } else {
            mCallType = (EditText) rootView.findViewById(R.id.call_type);
            mCallType.setVisibility(View.VISIBLE);
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
        }

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

        if (AppConstants.CONTACT_SELECTION_USING_DIALOG) {
            initContactSelectionWidgets(rootView);
        }
        initImageLoader();

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
        if (AppConstants.CALLTYPE_SELECTION_USING_RADIO) {
            callTypeRadioGroup.check(R.id.incoming_type_radiobutton);
        } else {
            mCallType.setText(callLogEntry.getCallTypeString());
        }
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
                if (allNumbers.size() > 1) {
                    final CharSequence[] items = allNumbers.toArray(new String[allNumbers.size()]);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getActivity().getString(R.string.choose_number));
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            String selectedNumber = items[item].toString();
                            selectedNumber = selectedNumber.replace("-", "").replace("(", "").replace(")", "").replace(" ", "");
                            callLogEntry.setPhoneNumber(selectedNumber);
                            if (null != mPhoneNumber) {
                                mPhoneNumber.setText(callLogEntry.getPhoneNumber());
                            }
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    String selectedNumber = phoneNumber;
                    selectedNumber = selectedNumber.replace("-", "").replace("(", "").replace(")", "").replace(" ", "");

                    callLogEntry.setPhoneNumber(selectedNumber);
                    Log.d(TAG, "selectedNumber = " + callLogEntry.getPhoneNumber());
                    mPhoneNumber.requestFocus();
                    if (null != mPhoneNumber) {
                        mPhoneNumber.setText(callLogEntry.getPhoneNumber());
                        Log.d(TAG, "gettext = " + mPhoneNumber.getText().toString());
                    }
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
        }
    }

    /**
     * Refactor the below methods to a new superclass since this will be common for EditLogFragment also
     */

    private ImageView mContactImage;
    private TextView mContactName;
    private TextView mContactNumber;
    private String[] contactTypeStringArray;

    // Handles loading the contact image in a background thread
    private ImageLoader mContactImageLoader;

    private void initContactSelectionWidgets(View rootView) {
        mContactImage = (ImageView) rootView.findViewById(R.id.contact_image);
        mContactName = (TextView) rootView.findViewById(R.id.contact_name);
        mContactNumber = (TextView) rootView.findViewById(R.id.contact_number);
        contactTypeStringArray = getActivity().getResources().getStringArray(R.array.contact_types);
        LinearLayout mContactSelectionLayout = (LinearLayout) rootView.findViewById(R.id.contact_selection_layout);
        mContactSelectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getActivity().getString(R.string.choose_contact));
                builder.setCancelable(true);
                AlertDialog dialog = builder.create();
                dialog.getListView();
                builder.setItems(contactTypeStringArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: // Select from Contacts
                                selectContact();
                                break;
                            case 1: // Select from Logs - TODO
                                Toast.makeText(getActivity(), "Selection from Logs is not supported currently", Toast.LENGTH_SHORT).show();
                                break;
                            case 2: // Enter Number
                                displayNumericEntryDialog();
                                break;
                            case 3: // Unknown Number
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }

    private void displayNumericEntryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getResources().getString(R.string.enter_number));
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Pass null as the parent view because its going in the dialog layout
        View rootView = inflater.inflate(R.layout.layout_enter_number, null);
        final EditText mPhoneNumberInput = (EditText) rootView.findViewById(R.id.phone_number);
        builder.setView(rootView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                processOnClickOnNumberEntry(mPhoneNumberInput.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void processOnClickOnNumberEntry(String contactNumber) {
        Log.d(TAG, "User entered number: " + contactNumber);
        if (TextUtils.isEmpty(contactNumber)) {
            resetContactDetails();
        } else {
            String photoUri = null;
            String name = null;
            Cursor contactCursor = ContactsUtil.getContactCursorForNumber(getActivity(), contactNumber);
            if (contactCursor.getCount() > 0) {
                contactCursor.moveToFirst();
                name = contactCursor.getString(ContactsUtil.DISPLAY_NAME);
                photoUri = contactCursor.getString(ContactsUtil.PHOTO_THUMBNAIL_DATA);
                if (AppConstants.DEBUG) {
                    Log.d(TAG, "name: " + name + " photoUri: " + photoUri);
                }
                contactCursor.close();
            }
            updateContactDetails(name, contactNumber, photoUri);
        }
    }

    private void resetContactDetails() {
        mImageLoader.loadImage(null, mContactImage);
        mContactName.setText(getActivity().getResources().getString(R.string.choose_contact));
        mContactNumber.setText(getActivity().getResources().getString(R.string.choose_number));
    }

    private void updateContactDetails(String name, String number, String photoUri) {
        mImageLoader.loadImage(photoUri, mContactImage);
        mContactName.setText(TextUtils.isEmpty(name) ? getActivity().getResources().getString(R.string.name_not_available) : name);
        mContactNumber.setText(number);
    }

    private int getContactImageSizeInPixels() {
        Resources r = getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, r.getDisplayMetrics()); //TODO: Get dimension from xml (50)
    }

    private void initImageLoader() {
        mImageLoader = new ImageLoader(getActivity(), getContactImageSizeInPixels()) {
            @Override
            protected Bitmap processBitmap(Object data) {
                // This gets called in a background thread and passed the data from
                // ImageLoader.loadImage().
                return loadContactPhotoThumbnail((String) data, getImageSize());
            }
        };

        mImageLoader.setLoadingImage(R.drawable.ic_contact);
        mImageLoader.addImageCache(getActivity().getSupportFragmentManager(), 0.1f);
    }


    /**
     * Decodes and scales a contact's image from a file pointed to by a Uri in the contact's data,
     * and returns the result as a Bitmap. The column that contains the Uri varies according to the
     * platform version.
     *
     * @param photoData For platforms prior to Android 3.0, provide the Contact._ID column value.
     *                  For Android 3.0 and later, provide the Contact.PHOTO_THUMBNAIL_URI value.
     * @param imageSize The desired target width and height of the output image in pixels.
     * @return A Bitmap containing the contact's image, resized to fit the provided image size. If
     * no thumbnail exists, returns null.
     */
    private Bitmap loadContactPhotoThumbnail(String photoData, int imageSize) {

        // Ensures the Fragment is still added to an activity. As this method is called in a
        // background thread, there's the possibility the Fragment is no longer attached and
        // added to an activity. If so, no need to spend resources loading the contact photo.
        if (!isAdded() || getActivity() == null) {
            return null;
        }

        // Instantiates an AssetFileDescriptor. Given a content Uri pointing to an image file, the
        // ContentResolver can return an AssetFileDescriptor for the file.
        AssetFileDescriptor afd = null;

        // This "try" block catches an Exception if the file descriptor returned from the Contacts
        // Provider doesn't point to an existing file.
        try {
            Uri thumbUri;
            // If Android 3.0 or later, converts the Uri passed as a string to a Uri object.
            if (Utils.hasHoneycomb()) {
                thumbUri = Uri.parse(photoData);
            } else {
                // For versions prior to Android 3.0, appends the string argument to the content
                // Uri for the Contacts table.
                final Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, photoData);

                // Appends the content Uri for the Contacts.Photo table to the previously
                // constructed contact Uri to yield a content URI for the thumbnail image
                thumbUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
            }
            // Retrieves a file descriptor from the Contacts Provider. To learn more about this
            // feature, read the reference documentation for
            // ContentResolver#openAssetFileDescriptor.
            afd = getActivity().getContentResolver().openAssetFileDescriptor(thumbUri, "r");

            // Gets a FileDescriptor from the AssetFileDescriptor. A BitmapFactory object can
            // decode the contents of a file pointed to by a FileDescriptor into a Bitmap.
            FileDescriptor fileDescriptor = afd.getFileDescriptor();

            if (fileDescriptor != null) {
                // Decodes a Bitmap from the image pointed to by the FileDescriptor, and scales it
                // to the specified width and height
                return ImageLoader.decodeSampledBitmapFromDescriptor(
                        fileDescriptor, imageSize, imageSize);
            }
        } catch (FileNotFoundException e) {
            // If the file pointed to by the thumbnail URI doesn't exist, or the file can't be
            // opened in "read" mode, ContentResolver.openAssetFileDescriptor throws a
            // FileNotFoundException.
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Contact photo thumbnail not found for contact " + photoData
                        + ": " + e.toString());
            }
        } finally {
            // If an AssetFileDescriptor was returned, try to close it
            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException e) {
                    // Closing a file descriptor might cause an IOException if the file is
                    // already closed. Nothing extra is needed to handle this.
                }
            }
        }

        // If the decoding failed, returns null
        return null;
    }

}

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
public abstract class CommonCallLogEntryFragment extends BackKeyHandlingFragment {

    private static final String TAG = "CommonCallLogEntry";

    private EditText mPhoneNumber;
    protected EditText mTimeOfCall; // Why protected? To give the child classes a reference to an EditText in case they need to use it as a reference for setting height etc for other widgets
    private EditText mDateOfCall;
    private EditText mCallDuration;
    private EditText mCallType;

    private RadioGroup callTypeRadioGroup;

    // UI elements used for contact selection using dialog
    private ImageView mContactImage;
    private TextView mContactName;
    private TextView mContactNumber;
    private String[] contactTypeStringArray;

    private CallLogEntry callLogEntry;
    private ImageLoader mImageLoader;


    static final int SELECT_CONTACT = 1;
    static final int SELECT_DATE = 2;
    static final int SELECT_TIME = 3;


    private static final String DATE_BUNDLE_ID = "DateOfCall";
    private static final String TIME_BUNDLE_ID = "TimeOfCall";

    public CommonCallLogEntryFragment() {
    }

    public abstract View onCreateViewExtra(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = onCreateViewExtra(inflater, container, savedInstanceState);
        if (null == rootView) {
            Log.e(TAG, "Rootview from the implementing class is null. App will crash. Please check");
            return null;
        }
        // Phone Number
        if (AppConstants.CONTACT_SELECTION_USING_DIALOG) {
            initContactSelectionWidgets(rootView);
        } else {
            mPhoneNumber = (EditText) rootView.findViewById(R.id.phone_number);
            ImageButton contactButton = (ImageButton) rootView.findViewById(R.id.contact_icon);
            contactButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectContact();
                }
            });
        }

        // Call Duration
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

        // Call Date
        mDateOfCall = (EditText) rootView.findViewById(R.id.date_of_call);
        mDateOfCall.setInputType(InputType.TYPE_NULL);
        mDateOfCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = DatePickerFragment.newInstance(callLogEntry.getCallDateAndTime());
                newFragment.setTargetFragment(CommonCallLogEntryFragment.this, SELECT_DATE);
                newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            }
        });

        // Call Time
        mTimeOfCall = (EditText) rootView.findViewById(R.id.time_of_call);
        mTimeOfCall.setInputType(InputType.TYPE_NULL);
        mTimeOfCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = TimePickerFragment.newInstance(callLogEntry.getCallDateAndTime());
                newFragment.setTargetFragment(CommonCallLogEntryFragment.this, SELECT_TIME);
                newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
            }
        });

        // Call Type
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
                            Log.d(TAG, "User selected " + which);
                            callLogEntry.setCallType(which);
                            mCallType.setText(callLogEntry.getCallTypeString());
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
            });
        }

        // Submit Call Log Entry
        Button submitButton = (Button) rootView.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptDataSubmit();
            }
        });
        initImageLoader();
        initFieldsToDefaultValues();
        return rootView;
    }


    private void initDateAndTimeOfCall() {
        updateDateOfCall();
        updateTimeOfCall();
    }

    protected void initFieldsToDefaultValues() {
        callLogEntry = new CallLogEntry(getActivity());
        if (AppConstants.CONTACT_SELECTION_USING_DIALOG) {
            resetContactDetails();
        } else {
            mPhoneNumber.setText(callLogEntry.getPhoneNumber());
        }
        initDateAndTimeOfCall();
        mCallDuration.setText(callLogEntry.getCallDurationTextForDisplay());
        if (AppConstants.CALLTYPE_SELECTION_USING_RADIO) {
            callTypeRadioGroup.check(R.id.incoming_type_radiobutton);
        } else {
            mCallType.setText(callLogEntry.getCallTypeString());
        }
    }


    private void selectContact() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        startActivityForResult(intent, SELECT_CONTACT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case SELECT_CONTACT:
                    processContactSelection(data);
                    break;
                case SELECT_DATE:
                    processDateSelection(data);
                    break;
                case SELECT_TIME:
                    processTimeSelection(data);
                    break;
                default:
                    Log.e(TAG, "Activity result obtained for unknown request code. Investigate [" + requestCode);
            }
        } else {
            Log.d(TAG, "Activity result was not Activity.RESULT_OK [Obtained: " + resultCode + "] for requestCode = " + requestCode);
        }
    }

    // TODO - ALSO THINK ABOUT USING CALENDAR AS THE OBJECT HERE
    private void processDateSelection(Intent data) {
        Calendar calendar = (Calendar) data.getSerializableExtra(DATE_BUNDLE_ID);
        Log.d(TAG, "Updated time after date selection : " + calendar);
        callLogEntry.setCallDateAndTime(calendar);
        updateDateOfCall();
    }

    // TODO - ALSO THINK ABOUT USING CALENDAR AS THE OBJECT HERE
    private void processTimeSelection(Intent data) {
        Calendar calendar = (Calendar) data.getSerializableExtra(TIME_BUNDLE_ID);
        Log.d(TAG, "Updated time after time selection : " + calendar);
        callLogEntry.setCallDateAndTime(calendar);
        updateTimeOfCall();
    }

    private void processContactSelection(Intent data) {
        Cursor cursor = null;
        String phoneNumber = "";
        final String contactName;
        final String photoUri;
        List<String> allNumbers = new ArrayList<>();
        try {
            Uri result = data.getData();
            String id = result.getLastPathSegment();
            cursor = ContactsUtil.getContactCursorForID(getActivity(), id);
            Log.d(TAG, "cursor count = " + cursor.getCount() + " column count = " + cursor.getColumnCount());
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(ContactsUtil.DISPLAY_NAME);
                photoUri = cursor.getString(ContactsUtil.PHOTO_THUMBNAIL_DATA);
                while (!cursor.isAfterLast()) {
                    phoneNumber = cursor.getString(ContactsUtil.PHONE_NUMBER_DATA);
                    Log.d(TAG, "name = " + cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)));
                    allNumbers.add(phoneNumber);
                    cursor.moveToNext();
                }
                if (allNumbers.size() > 1) {
                    displayNumberSelectionDialog(contactName, allNumbers, photoUri);
                } else {
                    updateContactDetails(contactName, phoneNumber, photoUri);
                }
                if (phoneNumber.length() == 0) {
                    resetContactDetails();
                    Toast.makeText(getActivity(), "There are no phone number associated with this contact [2]", Toast.LENGTH_SHORT).show();
                }
            } else {
                resetContactDetails();
                Toast.makeText(getActivity(), "There are no phone number associated with this contact [1]", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            resetContactDetails();
            Toast.makeText(getActivity(), "Some error happened while getting details for this contact.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void displayNumberSelectionDialog(final String contactName, List<String> allNumbers, final String photoUri) {
        final CharSequence[] items = allNumbers.toArray(new String[allNumbers.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getString(R.string.choose_number));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String selectedNumber = items[item].toString();
                updateContactDetails(contactName, selectedNumber, photoUri);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void updateDateOfCall() {
        mDateOfCall.setText(callLogEntry.getDateTextForDisplay());
    }

    private void updateTimeOfCall() {
        mTimeOfCall.setText(callLogEntry.getTimeTextForDisplay());
    }

    private void attemptDataSubmit() {
        boolean cancel = false;
        View focusView = null;

        // Reset errors.
        if (AppConstants.CONTACT_SELECTION_USING_DIALOG) {
            mContactName.setError(null);
        } else {
            mPhoneNumber.setError(null);
        }

        mCallDuration.setError(null);

        if (AppConstants.CONTACT_SELECTION_USING_DIALOG) {
            //TODO: Validate contact details. Show error if required
            if (!callLogEntry.isPhoneNumberValid()) {
                mContactName.setError(getString(R.string.error_message_empty_number));
                focusView = mContactName;
                cancel = true;
            }
        } else {
            callLogEntry.setPhoneNumber(mPhoneNumber.getText().toString());
            if (!callLogEntry.isPhoneNumberValid()) {
                mPhoneNumber.setError(getString(R.string.error_message_empty_number));
                focusView = mPhoneNumber;
                cancel = true;
            }
        }

        if (!cancel) {
            callLogEntry.setCallDuration(mCallDuration.getText().toString());
            if (!callLogEntry.isDurationValid()) {
                mCallDuration.setError(getString(R.string.error_message_invalid_duration));
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
            if (!handleSubmit(callLogEntry)) {
                CallLogUtility.getInstance().addCallLog(callLogEntry, getActivity());
            }
            initFieldsToDefaultValues();
        }
    }

    /**
     * This method can be extended to provide extra behavior to the datasubmit operation - like adding to a schedule.
     *
     * @param callLogEntry
     * @return
     */
    // TODO: Rename
    protected boolean handleSubmit(CallLogEntry callLogEntry) {
        return false;
    }


    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private static final String ARG_INITIAL_DATE = "initialDate";
        private Calendar calendar;

        static DatePickerFragment newInstance(Calendar initialDate) {
            DatePickerFragment fragment = new DatePickerFragment();
            Bundle args = new Bundle();
            args.putSerializable(ARG_INITIAL_DATE, initialDate);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Set the time passed in the arguments, as the initial value to DatePicker
            calendar = (Calendar) getArguments().getSerializable(ARG_INITIAL_DATE);
            return new DatePickerDialog(getActivity(), this, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(year, month, day);
            Intent intent = new Intent();
            intent.putExtra(DATE_BUNDLE_ID, calendar);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        }
    }


    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        private static final String ARG_INITIAL_DATE = "initialDate";
        private Calendar calendar;

        static TimePickerFragment newInstance(Calendar initialDate) {
            TimePickerFragment fragment = new TimePickerFragment();
            Bundle args = new Bundle();
            args.putSerializable(ARG_INITIAL_DATE, initialDate);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            calendar = (Calendar) getArguments().getSerializable(ARG_INITIAL_DATE);
            return new TimePickerDialog(getActivity(), this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            Intent intent = new Intent();
            intent.putExtra(TIME_BUNDLE_ID, calendar);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        }
    }

    /**
     * Refactor the below methods to a new superclass since this will be common for EditLogFragment also
     */


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
                mContactName.setError(null);
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

    private void updateContactDetails(String name, String selectedNumber, String photoUri) {
        selectedNumber = selectedNumber.replace("-", "").replace("(", "").replace(")", ""); //replace(" ", "");
        callLogEntry.setPhoneNumber(selectedNumber);
        if (AppConstants.CONTACT_SELECTION_USING_DIALOG) {
            mImageLoader.loadImage(photoUri, mContactImage);
            mContactName.setText(TextUtils.isEmpty(name) ? getActivity().getResources().getString(R.string.name_not_available) : name);
            mContactNumber.setText(callLogEntry.getPhoneNumber());
        } else {
            mPhoneNumber.setText(callLogEntry.getPhoneNumber());
        }
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

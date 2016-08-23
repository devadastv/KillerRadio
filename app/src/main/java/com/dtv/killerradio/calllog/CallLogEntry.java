package com.dtv.killerradio.calllog;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.dtv.killerradio.R;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

/**
 * Created by devadas.vijayan on 7/25/16.
 */
public class CallLogEntry implements Serializable {

    public static final String TAG = "CallLogEntry";

    public static final String SPACE = " ";

    public static final int INCOMING_TYPE = 0;
    public static final int OUTGOING_TYPE = 1;
    public static final int MISSED_TYPE = 2;

    private static final int RANDOM_DURATION_MIN_SECONDS = 10;
    private static final int RANDOM_DURATION_MAX_SECONDS = 300;

    public static final int CALL_DURATION_SET_SUCCESS = 1;
    public static final int CALL_DURATION_SET_FAILURE_MAX_EXCEEDED = 2;
    public static final int CALL_DURATION_SET_FAILURE_INVALID_VALUE = 3;

    private String phoneNumber;

    private Calendar callDateAndTime;

    private String dateTextForDisplay;
    private String timeTextForDisplay;

    private String callDuration;
    private String callDurationTextForDisplay;
    private String randomDurationText;

    private int callType;
    private String callTypeString;
    private String[] callTypeStringArray;

    private String selectedLogId;

    private Context context;

    public CallLogEntry(Context context) {
        this.context = context;
        initCallLogEntryWithDefaultValues();
    }

    private void initCallLogEntryWithDefaultValues() {
        initTimeOfCallWithCurrentTime();
        setRandomDurationText(context.getString(R.string.random_duration));
        setCallDuration("");
        initCallTypeStringArray();
        setCallType(INCOMING_TYPE);
    }

    private void initCallTypeStringArray() {
        callTypeStringArray = context.getResources().getStringArray(R.array.call_types);
    }

    public void setCallDateAndTime(Calendar updatedCallDateAndTime) {
        this.callDateAndTime = updatedCallDateAndTime;
        updateTimeAndDateDisplayTexts();
    }

    public Calendar getCallDateAndTime() {
        return callDateAndTime;
    }

    private void initTimeOfCallWithCurrentTime() {
        callDateAndTime = Calendar.getInstance();
        updateTimeAndDateDisplayTexts();
    }

    private void updateTimeAndDateDisplayTexts() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        setDateTextForDisplay(dateFormatter.format(callDateAndTime.getTime()));

        dateFormatter = new SimpleDateFormat("h:mm a", Locale.US);
        setTimeTextForDisplay(dateFormatter.format(callDateAndTime.getTime()));
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isPhoneNumberValid() {
        return !TextUtils.isEmpty(phoneNumber); // TODO: Is trim() really required? If so, user will not be able to add an empty number :-)
    }

    // Can either be the randomDurationText or empty string or the string representation of an integer duration
    public int setCallDuration(String callDuration) {
        callDuration = callDuration.trim();
        int status = CALL_DURATION_SET_SUCCESS;
        if (!callDuration.isEmpty()) { // This makes an empty string a valid duration and (re)sets duration to random duration
            try {
                long value = Long.parseLong(callDuration);
                Log.d(TAG, "call duration long value is " + value + " for input string of " + callDuration);
                if (value < 0 || callDuration.length() > 10) {
                    status = CALL_DURATION_SET_FAILURE_MAX_EXCEEDED;
                }
            } catch (NumberFormatException e) {
                status = CALL_DURATION_SET_FAILURE_INVALID_VALUE;
            }
        }
        if (status == CALL_DURATION_SET_SUCCESS) {
            this.callDuration = callDuration;
            updateCallDurationTextForDisplay();
        }
        return status;
    }

    public String getCallDurationTextForDisplay() {
        return callDurationTextForDisplay;
    }

    public String getCallDuration() {
        return callDuration;
    }

    public void updateCallDurationTextForDisplay() {
        if (null == callDuration || callDuration.trim().equals("")) {
            callDurationTextForDisplay = randomDurationText;
        } else {
            callDurationTextForDisplay = callDuration + SPACE + context.getString(R.string.seconds);
        }
    }

//    private int getDurationValidStatus(String durationString) {
//        int status;
//        if (durationString.equals("")) {
//            status = CALL_DURATION_SET_SUCCESS;
//        } else {
//            try {
//                Long.parseLong(durationString.trim());
//                valid = true;
//            } catch (NumberFormatException e) {
//                valid = false;
//            }
//        }
//        return valid;
//    }

    public long getCallDurationToSet() {
        long duration = -1;
        if (callDuration.equals("")) { // callDuration is always assumed to be valid at this point
            Random r = new Random();
            duration = r.nextInt(RANDOM_DURATION_MAX_SECONDS - RANDOM_DURATION_MIN_SECONDS) + RANDOM_DURATION_MIN_SECONDS;
        } else {
            try {
                Log.d(TAG, "callDuration = " + callDuration);
                duration = Long.parseLong(callDuration.trim());
            } catch (NumberFormatException e) {
            }
        }
        return duration;
    }

    private void setRandomDurationText(String randomDurationText) {
        this.randomDurationText = randomDurationText;
    }

    public void setCallType(int callType) {
        this.callType = callType;
        if (callType <= callTypeStringArray.length) {
            setCallTypeString(callTypeStringArray[callType]);
        } else {
            Log.e(TAG, "CallType is not at sync with values defined in callTypeStringArray. Please check !");
        }
    }

    public int getCallType() {
        return this.callType;
    }

    /**
     * This method returns the callType value to set for adding a new call log. This is not the same as
     * the value used for setCallType method.
     *
     * @return
     */
    public int getCallTypeToSet() {
        int callTypeToSet;
        switch (callType) {
            case INCOMING_TYPE:
                callTypeToSet = CallLog.Calls.INCOMING_TYPE;
                break;
            case OUTGOING_TYPE:
                callTypeToSet = CallLog.Calls.OUTGOING_TYPE;
                break;
            case MISSED_TYPE:
                callTypeToSet = CallLog.Calls.MISSED_TYPE;
                break;
            default:
                Log.e(TAG, "The callType is not defined. Defaulting it to value corresponds to INCOMING_TYPE. Please check: callType = " + callType);
                callTypeToSet = CallLog.Calls.INCOMING_TYPE;
        }
        return callTypeToSet;
    }

    public String getDateTextForDisplay() {
        return dateTextForDisplay;
    }

    private void setDateTextForDisplay(String dateTextForDisplay) {
        this.dateTextForDisplay = dateTextForDisplay;
    }

    public String getTimeTextForDisplay() {
        return timeTextForDisplay;
    }

    private void setTimeTextForDisplay(String timeTextForDisplay) {
        this.timeTextForDisplay = timeTextForDisplay;
    }

    public void setCallTypeString(String callTypeString) {
        this.callTypeString = callTypeString;
    }

    public String getCallTypeString() {
        if (null != callTypeString) {
            return callTypeString;
        }
        return "App Error. Report to developer";
    }

    public String[] getCallTypeStringArray() {
        return callTypeStringArray;
    }

    public String getSelectedLogId() {
        return selectedLogId;
    }

    public void setSelectedLogId(String selectedLogId) {
        this.selectedLogId = selectedLogId;
    }

    public void updateValuesFromCallLogCursor(Cursor cursor) {
        setPhoneNumber(cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)));
        Log.d(TAG, "Setting phone number as " + cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)));
        callDateAndTime.setTimeInMillis(Long.valueOf(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE))));
        updateTimeAndDateDisplayTexts();
//        Log.d(TAG, "Cached Name = " + cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)));
        setCallDuration(Integer.toString(cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION))));
        setSelectedLogId(cursor.getString(cursor.getColumnIndex(CallLog.Calls._ID)));
        Log.d(TAG, "The id of the selected entry = " + getSelectedLogId());
        int callTypeFromDb = Integer.parseInt(cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)));
        int callTypeIndex = 0;
        switch (callTypeFromDb) {
            case CallLog.Calls.INCOMING_TYPE:
                callTypeIndex = INCOMING_TYPE;
                break;
            case CallLog.Calls.OUTGOING_TYPE:
                callTypeIndex = OUTGOING_TYPE;
                break;
            case CallLog.Calls.MISSED_TYPE:
                callTypeIndex = MISSED_TYPE;
                break;
        }
        setCallType(callTypeIndex);
    }
}

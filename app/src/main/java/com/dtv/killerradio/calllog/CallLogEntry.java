package com.dtv.killerradio.calllog;

import android.content.Context;
import android.content.res.Resources;
import android.provider.CallLog;
import android.text.TextUtils;
import android.util.Log;

import com.dtv.killerradio.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

/**
 * Created by devadas.vijayan on 7/25/16.
 */
public class CallLogEntry {

    public static final String TAG = "CallLogEntry";

    public static final int INCOMING_TYPE = 0;
    public static final int OUTGOING_TYPE = 1;
    public static final int MISSED_TYPE = 2;

    private static final int RANDOM_DURATION_MIN_SECONDS = 10;
    private static final int RANDOM_DURATION_MAX_SECONDS = 300;

    private String phoneNumber;

    // Call log entry date and time
    private int year;
    private int month;
    private int day;
    private int hourOfDay; // Hour of day always - 24 hours
    private int minute;

    private String dateTextForDisplay;
    private String timeTextForDisplay;

    private String callDuration;
//    private String callDurationTextForDisplay;
    private String randomDurationText;

    private int callType;
    private String callTypeString;
    private String[] callTypeStringArray;

    private Context context;

    public CallLogEntry(Context context) {
        this.context = context;
        initCallLogEntryWithDefaultValues();
    }

    private void initCallLogEntryWithDefaultValues() {
        setPhoneNumber("");
        initTimeOfCallWithCurrentTime();
        setRandomDurationText(context.getString(R.string.random_duration));
        setCallDuration(getRandomDurationText());
        initCallTypeStringArray();
        setCallType(INCOMING_TYPE);
    }

    private void initCallTypeStringArray() {
        callTypeStringArray = context.getResources().getStringArray(R.array.call_types);
    }

    private void initTimeOfCallWithCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        updateDateOfCall(calendar);
        updateTimeOfCall(calendar);
    }

    public void updateDateOfCall(Calendar calendar) {
        setYear(calendar.get(Calendar.YEAR));
        setMonth(calendar.get(Calendar.MONTH));
        setDay(calendar.get(Calendar.DAY_OF_MONTH));

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        setDateTextForDisplay(dateFormatter.format(calendar.getTime()));
    }

    public void updateTimeOfCall(Calendar calendar) {
        setHourOfDay(calendar.get(Calendar.HOUR_OF_DAY));
        setMinute(calendar.get(Calendar.MINUTE));

        SimpleDateFormat dateFormatter = new SimpleDateFormat("h:mm a", Locale.US);
        setTimeTextForDisplay(dateFormatter.format(calendar.getTime()));
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public long getCallLogTimeInMillis() {
        Calendar calendarForCallLog = Calendar.getInstance();
        calendarForCallLog.set(getYear(), getMonth(), getDay(), getHourOfDay(), getMinute());
        return calendarForCallLog.getTimeInMillis();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isPhoneNumberValid() {
        return TextUtils.isEmpty(phoneNumber.trim());
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    // Can either be the randomDurationText or empty string or the string representation of an integer duration
    public void setCallDuration(String callDuration) {
        this.callDuration = callDuration;
    }

    public String getCallDurationTextForDisplay() {
        return callDuration;
    }

    public String getCallDurationTextForDisplay(boolean hasFocus) {
        String callDurationTextForDisplay = callDuration;
        if (null != callDuration) {
            if (hasFocus) {
                if (callDuration.equals(randomDurationText)) {
                    callDurationTextForDisplay = "";
                }
            } else {
                if (callDuration.trim().equals("")) {
                    callDurationTextForDisplay = randomDurationText;
                }
            }
        } else {
            callDurationTextForDisplay = randomDurationText;
        }
        return callDurationTextForDisplay;
    }

    public boolean isDurationValid() {
        boolean valid;
        if (callDuration.equals(randomDurationText) || callDuration.equals("")) {
            valid = true;
        } else {
            try {
                Integer.parseInt(callDuration.trim());
                valid = true;
            } catch (NumberFormatException e) {
                valid = false;
            }
        }
        return valid;
    }

    public int getCallDurationToSet() {
        int duration = -1;
        if (isDurationValid()) {
            if (callDuration.equals(randomDurationText) || callDuration.equals("")) {
                Random r = new Random();
                duration = r.nextInt(RANDOM_DURATION_MAX_SECONDS - RANDOM_DURATION_MIN_SECONDS) + RANDOM_DURATION_MIN_SECONDS;
            } else {
                try {
                    Log.d(TAG, "callDuration = " + callDuration);
                    duration = Integer.parseInt(callDuration.trim());
                } catch (NumberFormatException e) {
                }
            }
        }
        return duration;
    }

    private void setRandomDurationText(String randomDurationText) {
        this.randomDurationText = randomDurationText;
    }

    private String getRandomDurationText() {
        return this.randomDurationText;
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
}

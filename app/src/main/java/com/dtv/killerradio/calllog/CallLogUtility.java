package com.dtv.killerradio.calllog;

import android.content.ContentValues;
import android.content.Context;
import android.provider.CallLog;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by devadas.vijayan on 7/25/16.
 */
public class CallLogUtility {
    private static final String TAG = "CallLogUtility";
    private static CallLogUtility instance;

    private CallLogUtility() {
    }

    public static synchronized CallLogUtility getInstance() {
        if (null == instance) {
            instance = new CallLogUtility();
        }
        return instance;
    }

    public void addCallLog(CallLogEntry callLogEntry, Context context) {
        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, callLogEntry.getPhoneNumber());
        values.put(CallLog.Calls.DATE, callLogEntry.getCallLogTimeInMillis());
        values.put(CallLog.Calls.DURATION, callLogEntry.getCallDurationToSet());
        values.put(CallLog.Calls.TYPE, callLogEntry.getCallTypeToSet());
        values.put(CallLog.Calls.NEW, 1);
        values.put(CallLog.Calls.CACHED_NAME, "");
        values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
        values.put(CallLog.Calls.CACHED_NUMBER_LABEL, "");
        Log.d(TAG, "Inserting call log placeholder for " + callLogEntry);
        context.getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
        Toast.makeText(context, "The fake call log is successfully added!", Toast.LENGTH_SHORT).show();
    }
}

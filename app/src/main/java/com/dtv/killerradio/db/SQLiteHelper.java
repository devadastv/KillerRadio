package com.dtv.killerradio.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by devadas.vijayan on 7/5/16.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = "SQLiteHelper";
    private static final int database_VERSION = 2;
    public static final String DATABASE_NAME = "KillerRadioDB";
    public static final String CALL_LOG_TABLE_NAME = "FakeCallLogSchedule";
    public static final String CALL_LOG_COLUMN_ID = "_id";
    public static final String CALL_LOG_COLUMN_NUMBER = "number";
    public static final String CALL_LOG_COLUMN_CALL_DATE = "call_date";
    public static final String CALL_LOG_COLUMN_DURATION = "duration";
    public static final String CALL_LOG_COLUMN_CALL_TYPE = "call_type";
    public static final String CALL_LOG_COLUMN_ACK_STATUS = "ack_status";
    public static final String CALL_LOG_COLUMN_CACHED_NAME = "cached_name";
    public static final String CALL_LOG_COLUMN_CACHED_NUMBER_TYPE = "cached_number_type";
    public static final String CALL_LOG_COLUMN_CACHED_NUMBER_LABEL = "cached_number_label";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER ";
    private static final String COMMA_SEP = ",";


    private static final String SQL_CREATE_ENTRIES_CALL_LOG_SCHEDULE =
            "CREATE TABLE " + CALL_LOG_TABLE_NAME + " (" +
                    CALL_LOG_COLUMN_ID + INTEGER_TYPE + " PRIMARY KEY," +
                    CALL_LOG_COLUMN_NUMBER + TEXT_TYPE + COMMA_SEP +
                    CALL_LOG_COLUMN_CALL_DATE + INTEGER_TYPE + COMMA_SEP +
                    CALL_LOG_COLUMN_DURATION + INTEGER_TYPE + COMMA_SEP +
                    CALL_LOG_COLUMN_CALL_TYPE + INTEGER_TYPE + COMMA_SEP +
                    CALL_LOG_COLUMN_ACK_STATUS + INTEGER_TYPE + COMMA_SEP +
                    CALL_LOG_COLUMN_CACHED_NAME + TEXT_TYPE + COMMA_SEP +
                    CALL_LOG_COLUMN_CACHED_NUMBER_TYPE + INTEGER_TYPE + COMMA_SEP +
                    CALL_LOG_COLUMN_CACHED_NUMBER_LABEL + TEXT_TYPE +
                    ")";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, database_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating DB with command \n" + SQL_CREATE_ENTRIES_CALL_LOG_SCHEDULE);
        db.execSQL(SQL_CREATE_ENTRIES_CALL_LOG_SCHEDULE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + CALL_LOG_TABLE_NAME);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    public void createCallLogSchedule(String phoneNumber, long callLogTimeInMillis, int duration, int callType,
                                      int ackStatus, String cachedName, int cachedNumberType, String cachedNumberLabel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CALL_LOG_COLUMN_NUMBER, phoneNumber);
        values.put(CALL_LOG_COLUMN_CALL_DATE, callLogTimeInMillis);
        values.put(CALL_LOG_COLUMN_DURATION, duration);
        values.put(CALL_LOG_COLUMN_CALL_TYPE, callType);
        values.put(CALL_LOG_COLUMN_ACK_STATUS, ackStatus);
        values.put(CALL_LOG_COLUMN_CACHED_NAME, cachedName);
        values.put(CALL_LOG_COLUMN_CACHED_NUMBER_TYPE, cachedNumberType);
        values.put(CALL_LOG_COLUMN_CACHED_NUMBER_LABEL, cachedNumberLabel);
        db.insert(CALL_LOG_TABLE_NAME, null, values);
        db.close();
    }

    public int getNumberOfCallLogSchedules() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT  * FROM " + CALL_LOG_TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void deleteAllSurveyEntries() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + CALL_LOG_TABLE_NAME);
    }

    public Cursor getFilteredList(String selection, String[] selectionArgs, String orderBy) {
        return getFilteredList(null, selection, selectionArgs, orderBy);
    }

    public Cursor getFilteredList(String[] columns, String selection, String[] selectionArgs, String orderBy) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(CALL_LOG_TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy);
    }
}

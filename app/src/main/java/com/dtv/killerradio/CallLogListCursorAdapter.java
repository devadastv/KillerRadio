package com.dtv.killerradio;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by devadas.vijayan on 8/9/16.
 */
public class CallLogListCursorAdapter extends CursorAdapter {

    public static final String TAG = "CallLogCursorAdapter";
    private LayoutInflater cursorInflater;

    public CallLogListCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        cursorInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.layout_calllog_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor logCursor) {
        if (null != logCursor) {
            Cursor contactLookupCursor = getContactLookupCursor(logCursor, context);
            setNameAndNumber(view, logCursor, contactLookupCursor);
            setDateAndTime(view, logCursor);
            setContactImage(view, logCursor, contactLookupCursor);
            setCallTypeIcon(view, logCursor);
            if (contactLookupCursor != null && !contactLookupCursor.isClosed()) {
                contactLookupCursor.close();
            }
        }
    }

    private void setNameAndNumber(View view, Cursor logCursor, Cursor contactLookupCursor) {
        TextView mName = (TextView) view.findViewById(R.id.item_name);
        TextView mNumber = (TextView) view.findViewById(R.id.item_number);
        String number = logCursor.getString(logCursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
        String name = getContactName(contactLookupCursor); //logCursor.getString(logCursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME));
        if (TextUtils.isEmpty(name)) {
            name = number;
            number = mContext.getString(R.string.unsaved);
        }
        mName.setText(name);
        mNumber.setText(String.valueOf(number));
    }

    private void setDateAndTime(View view, Cursor logCursor) {
        TextView mDate = (TextView) view.findViewById(R.id.item_date);
        TextView mTime = (TextView) view.findViewById(R.id.item_time);
        Calendar now = Calendar.getInstance();
        Calendar callTimeCalendar = Calendar.getInstance();
        callTimeCalendar.setTimeInMillis(Long.valueOf(logCursor.getString(logCursor.getColumnIndex(CallLog.Calls.DATE))));
        SimpleDateFormat dateFormatter;
        if (now.get(Calendar.YEAR) == callTimeCalendar.get(Calendar.YEAR)) {
            dateFormatter = new SimpleDateFormat("dd-MMM", Locale.US);
        } else {
            dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        }
        String date = dateFormatter.format(callTimeCalendar.getTime());
        dateFormatter = new SimpleDateFormat("h:mm a", Locale.US);
        String time = dateFormatter.format(callTimeCalendar.getTime());

        mDate.setText(date);
        mTime.setText(time);
    }

    private void setContactImage(View view, Cursor logCursor, Cursor contactLookupCursor) {
        ImageView mContactImage = (ImageView) view.findViewById(R.id.contact_image);
        int contactID = getContactID(contactLookupCursor);
//        Log.d(TAG, "contactID = " + contactID + " for log item at index = " + logCursor.getPosition());
        if (contactID != -1) {
            Uri imageUri = getPhotoUri(contactID, mContext);
            if (imageUri != null) {
                    mContactImage.setImageURI(imageUri);
                if (null == mContactImage.getDrawable()) {
                    mContactImage.setImageResource(R.drawable.ic_contact);
                }
            } else {
                mContactImage.setImageResource(R.drawable.ic_contact);
            }
        } else {
            mContactImage.setImageResource(R.drawable.ic_contact);
        }
    }

    private void setCallTypeIcon(View view, Cursor logCursor) {
        ImageView mCallTypeImage = (ImageView) view.findViewById(R.id.item_type);
        int callType = logCursor.getInt(logCursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
        switch (callType) {
            case CallLog.Calls.INCOMING_TYPE:
                mCallTypeImage.setImageResource(R.drawable.incoming);
                break;
            case CallLog.Calls.OUTGOING_TYPE:
                mCallTypeImage.setImageResource(R.drawable.outgoing);
                break;
            case CallLog.Calls.MISSED_TYPE:
                mCallTypeImage.setImageResource(R.drawable.missed_call);
                break;
            default:
                mCallTypeImage.setVisibility(View.GONE);
        }
    }

    private Cursor getContactLookupCursor(Cursor logCursor, Context context) {
        String contactNumber = logCursor.getString(logCursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
        Cursor contactLookupCursor = context.getContentResolver().query(
                Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contactNumber)),
                new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
        return contactLookupCursor;
    }

    private int getContactID(Cursor contactLookupCursor) {
        int phoneContactID = -1;
        if (contactLookupCursor.moveToFirst()) {
            phoneContactID = contactLookupCursor.getInt(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
        }
        return phoneContactID;
    }

    private String getContactName(Cursor contactLookupCursor) {
        String name = null;
        if (contactLookupCursor.moveToFirst()) {
            name = contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
//        Log.d(TAG, "Inside getContactName = " + name);
        return name;
    }

    /**
     * @return the photo URI
     */
    public Uri getPhotoUri(int contactID, Context context) {
        try {
            Cursor cur = context.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID + "=" + contactID + " AND "
                            + ContactsContract.Data.MIMETYPE + "='"
                            + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", null,
                    null);
            if (cur != null) {
                if (!cur.moveToFirst()) {
                    cur.close();
                    return null; // no photo
                }
            } else {
                return null; // error in cursor process
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactID);
        return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
    }
}

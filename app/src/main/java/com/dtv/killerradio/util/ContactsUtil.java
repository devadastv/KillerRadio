package com.dtv.killerradio.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

/**
 * Created by devadas.vijayan on 8/19/16.
 */
public class ContactsUtil {

    // The query column numbers which map to each value in the projection
    public final static int ID = 0;
    public final static int DISPLAY_NAME = 1;
    public final static int PHOTO_THUMBNAIL_DATA = 2;

    public static Cursor getContactCursorForNumber(Context context, String contactNumber) {
        Cursor contactLookupCursor = context.getContentResolver().query(
                Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contactNumber)),
                new String[]{ContactsContract.PhoneLookup._ID,
                        Utils.hasHoneycomb() ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME,
                        Utils.hasHoneycomb() ? ContactsContract.Contacts.PHOTO_THUMBNAIL_URI : ContactsContract.Contacts._ID}, null, null, null);
        return contactLookupCursor;
    }
}

package com.dtv.killerradio;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.dtv.killerradio.calllog.CallLogEntry;
import com.dtv.killerradio.calllog.CallLogUtility;
import com.dtv.killerradio.util.ContactsUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditCallLogFragment extends CommonCallLogEntryFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "EditCallLogFragment";

    private CursorAdapter mAdapter;
    private ProgressBar mProgressBar;
    private ViewGroup rootView;
    private volatile boolean isEditViewVisible;

    public EditCallLogFragment() {
    }

    public static EditCallLogFragment newInstance() {
        EditCallLogFragment fragment = new EditCallLogFragment();
        return fragment;
    }

    @Override
    public View onCreateViewExtra(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "Inside FakeCalllogFragment - onCreateView method");
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_edit_call_log, container, false);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.calllog_edit_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        initializeLogListComponents(rootView);
        initializeLogEditComponents(rootView);
        getLoaderManager().initLoader(0, null, this);
        return rootView;
    }

    private void initializeLogListComponents(final ViewGroup rootView) {
        ListView mEditCallLogList = (ListView) rootView.findViewById(R.id.calllog_list);
        mEditCallLogList.setEmptyView(rootView.findViewById(android.R.id.empty));
        mAdapter = new CallLogListCursorAdapter(this.getActivity(), null);
        mEditCallLogList.setAdapter(mAdapter);
        mEditCallLogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "The user clicked on log item: " + position);
                switchToEditScreen(rootView);
                displayLogDetailsInEditScreen(position);
            }
        });
        mEditCallLogList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause image loader to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    mImageLoader.setPauseWork(true);
                } else {
                    mImageLoader.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
            }
        });
    }

    private void initializeLogEditComponents(final ViewGroup rootView) {
        Button mDiscardButton = (Button) rootView.findViewById(R.id.discard_button);
        mDiscardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToListScreen(rootView);
            }
        });
    }

    @Override
    protected void postProcessSubmission() {
        switchToListScreen(rootView);
    }

    private void switchToEditScreen(View rootView) {
        LinearLayout listScreen = (LinearLayout) rootView.findViewById(R.id.callloglist_layout);
        listScreen.setVisibility(View.GONE);
        ScrollView editScreen = (ScrollView) rootView.findViewById(R.id.calllog_edit_layout);
        editScreen.setVisibility(View.VISIBLE);
        isEditViewVisible = true;
        Log.d(TAG, "On switchToEditScreen : isEditViewVisible = " + isEditViewVisible);
    }

    private void switchToListScreen(View rootView) {
        ScrollView editView = (ScrollView) rootView.findViewById(R.id.calllog_edit_layout);
        editView.setVisibility(View.GONE);
        LinearLayout listView = (LinearLayout) rootView.findViewById(R.id.callloglist_layout);
        listView.setVisibility(View.VISIBLE);
        isEditViewVisible = false;
        Log.d(TAG, "On switchToListScreen : isEditViewVisible = " + isEditViewVisible);
    }

    @Override
    protected boolean handleSubmit(CallLogEntry callLogEntry) {
        CallLogUtility.getInstance().updateCallLogByID(callLogEntry, getActivity());
        Toast.makeText(getActivity(), getString(R.string.message_successful_edit_submission), Toast.LENGTH_SHORT).show();
        return true;
    }

    private void displayLogDetailsInEditScreen(int position) {
        Cursor cursor = mAdapter.getCursor();
        if (cursor != null && cursor.moveToPosition(position)) {
            callLogEntry.updateValuesFromCallLogCursor(cursor);
            initFieldsToValuesInCallLogEntry();
        } else {
            Log.e(TAG, "Display of selected log in editor failed. Either cursor is null or index is out of range. Check: cursor = " + cursor);
            if (null != cursor) {
                Log.e(TAG, "Cursor count = " + cursor.getCount() + " and requested position = " + position);
            }
            Toast.makeText(getActivity(), getString(R.string.error_call_log_edit_message), Toast.LENGTH_LONG).show();
            switchToListScreen(rootView);
        }
    }

    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(getActivity(), CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC");
    }

    // Called when a previously created loader has finished loading
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        Log.d(TAG, "Inside onLoadFinished with cursor " + data);
        handleCursorUpdate(data);
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        handleCursorUpdate(null);
    }

    private void handleCursorUpdate(Cursor cursor) {
        mProgressBar.setVisibility(View.GONE);
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        Log.d(TAG, "Inside setMenuVisibility of EditCallLogFragment...");
        super.setMenuVisibility(visible);
        if (visible && (null != rootView)) {
            switchToListScreen(rootView);
        }
    }

    @Override
    public boolean handleBackKey() {
        Log.d(TAG, "isEditViewVisible " + isEditViewVisible);
        if (isEditViewVisible) {
            switchToListScreen(rootView);
            return true;
        } else {
            return false;
        }
    }

    private class CallLogListCursorAdapter extends CursorAdapter {

        public static final String TAG = "CallLogCursorAdapter";
        private LayoutInflater cursorInflater;

        public CallLogListCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
            cursorInflater = LayoutInflater.from(context);
        }

        private int getContactImageSizeInPixels() {
            Resources r = mContext.getResources();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, r.getDisplayMetrics()); //TODO: Get dimension from xml (50)
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View itemLayout = cursorInflater.inflate(R.layout.layout_calllog_item_new, parent, false);

            // Creates a new ViewHolder in which to store handles to each view resource. This
            // allows bindView() to retrieve stored references instead of calling findViewById for
            // each instance of the layout.
            final ViewHolder holder = new ViewHolder();
            holder.mContactImage = (ImageView) itemLayout.findViewById(R.id.contact_image);
            holder.mContactName = (TextView) itemLayout.findViewById(R.id.item_name);
            holder.mContactNumber = (TextView) itemLayout.findViewById(R.id.item_number);
            holder.mCallType = (ImageView) itemLayout.findViewById(R.id.item_type);
            holder.mCallDate = (TextView) itemLayout.findViewById(R.id.item_date);
            holder.mCallTime = (TextView) itemLayout.findViewById(R.id.item_time);

            // Stores the resourceHolder instance in itemLayout. This makes resourceHolder
            // available to bindView and other methods that receive a handle to the item view.
            itemLayout.setTag(holder);

            // Returns the item layout view
            return itemLayout;
        }

        @Override
        public void bindView(View view, Context context, Cursor logCursor) {
            if (null != logCursor) {
                final ViewHolder holder = (ViewHolder) view.getTag();
                Cursor contactLookupCursor = getContactLookupCursor(logCursor, context);
                setNameAndNumber(holder, logCursor, contactLookupCursor);
                setDateAndTime(holder, logCursor);
                setContactImage(holder, logCursor, contactLookupCursor);
                setCallTypeIcon(holder, logCursor);
                if (contactLookupCursor != null && !contactLookupCursor.isClosed()) {
                    contactLookupCursor.close();
                }
            }
        }

        private void setNameAndNumber(ViewHolder holder, Cursor logCursor, Cursor contactLookupCursor) {
            String number = logCursor.getString(logCursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
            String name = getContactName(contactLookupCursor); //logCursor.getString(logCursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME));
            if (TextUtils.isEmpty(name)) {
                name = number;
                number = mContext.getString(R.string.unsaved);
            }
            holder.mContactName.setText(name);
            holder.mContactNumber.setText(String.valueOf(number));
        }

        private void setDateAndTime(ViewHolder holder, Cursor logCursor) {
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

            holder.mCallDate.setText(date);
            holder.mCallTime.setText(time);
        }

        private void setContactImage(ViewHolder holder, Cursor logCursor, Cursor contactLookupCursor) {
            ImageView mContactImage = holder.mContactImage;
            String photoUri = null;
            if (null != contactLookupCursor && contactLookupCursor.moveToFirst()) {
                photoUri = contactLookupCursor.getString(ContactsUtil.PHOTO_THUMBNAIL_DATA);
            }
            mImageLoader.loadImage(photoUri, mContactImage);
        }

        private void setCallTypeIcon(ViewHolder holder, Cursor logCursor) {
            ImageView mCallTypeImage = holder.mCallType;
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
                    Log.d(TAG, "Unknown call type: " + callType);
                    mCallTypeImage.setVisibility(View.GONE);
            }
        }

        private Cursor getContactLookupCursor(Cursor logCursor, Context context) {
            String contactNumber = logCursor.getString(logCursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
            if (contactNumber != null && !contactNumber.trim().isEmpty()) {
                return ContactsUtil.getContactCursorForNumber(context, contactNumber);
            }
            return null;
        }

        private String getContactName(Cursor contactLookupCursor) {
            String name = null;
            if (null != contactLookupCursor && contactLookupCursor.moveToFirst()) {
                name = contactLookupCursor.getString(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
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

        /**
         * A class that defines fields for each resource ID in the list item layout. This allows
         * ContactsAdapter.newView() to store the IDs once, when it inflates the layout, instead of
         * calling findViewById in each iteration of bindView.
         */
        private class ViewHolder {
            ImageView mContactImage;
            TextView mContactName;
            TextView mContactNumber;
            ImageView mCallType;
            TextView mCallDate;
            TextView mCallTime;
        }
    }
}

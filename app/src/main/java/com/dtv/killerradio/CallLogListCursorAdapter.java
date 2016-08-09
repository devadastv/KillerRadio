package com.dtv.killerradio;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by devadas.vijayan on 8/9/16.
 */
public class CallLogListCursorAdapter extends CursorAdapter {

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
    public void bindView(View view, Context context, Cursor cursor) {
        if (null != cursor)
        {
            TextView tvBody = (TextView) view.findViewById(R.id.item_name);
            TextView tvPriority = (TextView) view.findViewById(R.id.item_number);

            // Extract properties from cursor
            String body = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
            int priority = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
            tvBody.setText(body);
            tvPriority.setText(String.valueOf(priority));
        }
    }
}
}

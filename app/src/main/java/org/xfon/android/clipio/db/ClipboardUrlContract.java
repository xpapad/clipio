package org.xfon.android.clipio.db;

import android.provider.BaseColumns;

/**
 * Created by xenofon on 6/15/15.
 */
public final class ClipboardUrlContract {

    public static final String SQL_CREATE_CLIPBOARD_URL_ENTRIES[] = {
            "CREATE TABLE " + ClipboardUrlEntry.TABLE_NAME + " (" +
                    ClipboardUrlEntry._ID + " INTEGER PRIMARY KEY," +
                    ClipboardUrlEntry.COLUMN_NAME_CREATED_DATE + " UNSIGNED BIG INT, " +
                    ClipboardUrlEntry.COLUMN_NAME_STARRED + " TINYINT, " +
                    ClipboardUrlEntry.COLUMN_NAME_URL + " TEXT, " +
                    ClipboardUrlEntry.COLUMN_NAME_TITLE + " TEXT " + " )",
            "CREATE INDEX " + ClipboardUrlEntry.INDEX_URL_NAME +
                    " ON " + ClipboardUrlEntry.TABLE_NAME +
                    "(" + ClipboardUrlEntry.COLUMN_NAME_URL + ")"
    } ;

    public static final String SQL_DROP_CLIPBOARD_URL_ENTRIES =
            "DROP TABLE IF EXISTS " + ClipboardUrlEntry.TABLE_NAME;
    
    public ClipboardUrlContract() { }

    public static abstract class ClipboardUrlEntry implements BaseColumns {
        public static final String TABLE_NAME = "clipboard_url_entry";
        public static final String INDEX_URL_NAME = "url_idx";
        public static final String COLUMN_NAME_URL = "url";
        public static final String COLUMN_NAME_CREATED_DATE = "created";
        public static final String COLUMN_NAME_STARRED = "starred";
        public static final String COLUMN_NAME_TITLE = "title";
    }
}

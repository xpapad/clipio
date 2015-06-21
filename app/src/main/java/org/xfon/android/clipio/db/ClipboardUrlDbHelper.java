package org.xfon.android.clipio.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by xenofon on 6/15/15.
 */
public class ClipboardUrlDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ClipboardUrl.db";
    private static final String TAG = ClipboardUrlDbHelper.class.getName();

    public ClipboardUrlDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String sql: ClipboardUrlContract.SQL_CREATE_CLIPBOARD_URL_ENTRIES) {
            Log.d(TAG, sql);
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

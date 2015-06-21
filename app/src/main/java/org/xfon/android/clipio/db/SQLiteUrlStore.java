package org.xfon.android.clipio.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.xfon.android.clipio.ClipboardUrl;
import org.xfon.android.clipio.DateHelper;
import org.xfon.android.clipio.UrlStore;
import org.xfon.android.clipio.UrlTitleResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xenofon on 6/15/15.
 */
public class SQLiteUrlStore implements UrlStore, UrlTitleResolver.OnUrlTitleResolvedListener {
    private Context context;
    private SQLiteDatabase db;
    private UrlTitleResolver titleResolver;
    private static final String TAG = SQLiteUrlStore.class.getName();
    private static SQLiteUrlStore instance = null;

    private SQLiteUrlStore(Context context) {
        this.context = context;
        ClipboardUrlDbHelper helper = new ClipboardUrlDbHelper(context);
        db = helper.getWritableDatabase();
        titleResolver = new UrlTitleResolver();
    }

    public static synchronized  SQLiteUrlStore getInstance(Context context) {
        if (instance != null) return instance;
        instance = new SQLiteUrlStore(context);
        instance.titleResolver.setOnUrlTitleResolvedListener(instance);
        return instance;
    }

    @Override
    public void add(ClipboardUrl url) {
        synchronized (this) {
            int i = dbFind(url);
            if (i == 0) {
                dbWrite(url);
            }
            else {
                dbUpdate(i, url);
            }
            if (url.getTitle() == null) {
                titleResolver.resolveTitle(url.getUrl());
            }
        }
    }

    private int dbFind(ClipboardUrl url) {
        String[] projection = {
                ClipboardUrlContract.ClipboardUrlEntry._ID
        };
        String selection = ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_URL + " = ?";
        String[] selectionArgs = { url.getUrl() };
        Cursor c = null;
        try {
            c = db.query(ClipboardUrlContract.ClipboardUrlEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null, null, null);
            if (c.getCount() == 0) return 0;
            c.moveToFirst();
            int idx = c.getColumnIndexOrThrow(ClipboardUrlContract.ClipboardUrlEntry._ID);
            return c.getInt(idx);
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private void dbWrite(ClipboardUrl url) {
        ContentValues values = new ContentValues();
        values.put(ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_URL, url.getUrl());
        values.put(ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_CREATED_DATE, url.getEpoch());
        values.put(ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_STARRED, url.isStarred());
        values.put(ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_TITLE, url.getTitle());
        db.insert(ClipboardUrlContract.ClipboardUrlEntry.TABLE_NAME, null, values);
    }

    private void dbUpdate(int id, ClipboardUrl url) {
        String selection = ClipboardUrlContract.ClipboardUrlEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        ContentValues values = new ContentValues();
        values.put(ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_CREATED_DATE, url.getEpoch());
        values.put(ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_STARRED, url.isStarred());
        values.put(ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_TITLE, url.getTitle());
        db.update(ClipboardUrlContract.ClipboardUrlEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    @Override
    public void deleteUnstarred(boolean cleanupTodays) {
        synchronized (this) {
            String selection = ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_STARRED + " = 0";
            if (!cleanupTodays) {
                selection += " AND " + ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_CREATED_DATE + " < " + DateHelper.epochBeforeToday();
            }
            db.delete(ClipboardUrlContract.ClipboardUrlEntry.TABLE_NAME, selection, null);
        }
    }

    @Override
    public void delete(ClipboardUrl url) {
        synchronized (this) {
            String selection = ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_URL + " = ?";
            String[] selectionArgs = { url.getUrl() };
            db.delete(ClipboardUrlContract.ClipboardUrlEntry.TABLE_NAME, selection, selectionArgs);
        }
    }

    @Override
    public void setStarred(ClipboardUrl url, boolean starred) {
        String selection = ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_URL + " = ?";
        String[] selectionArgs = { url.getUrl() };
        ContentValues values = new ContentValues();
        values.put(ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_STARRED, starred);
        db.update(ClipboardUrlContract.ClipboardUrlEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    @Override
    public List<ClipboardUrl> getUrls() {
        List<ClipboardUrl> ret = new ArrayList<>();
        synchronized (this) {
            Cursor c = null;
            try {
                String orderBy = ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_CREATED_DATE + " DESC";
                c = db.query(ClipboardUrlContract.ClipboardUrlEntry.TABLE_NAME,
                        null, null, null, null, null, orderBy);
                if (c.getCount() == 0) return ret;
                c.moveToFirst();
                do {
                    int idUrl = c.getColumnIndexOrThrow(ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_URL);
                    int idTitle = c.getColumnIndexOrThrow(ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_TITLE);
                    int idCreated = c.getColumnIndexOrThrow(ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_CREATED_DATE);
                    int idStarred = c.getColumnIndexOrThrow(ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_STARRED);
                    ClipboardUrl url = new ClipboardUrl(
                            c.getString(idUrl),
                            c.getLong(idCreated),
                            c.getString(idTitle),
                            c.getInt(idStarred) > 0 ? true : false
                    );
                    if (url.getTitle() == null) {
                        titleResolver.resolveTitle(url.getUrl());
                    }
                    ret.add(url);
                } while (c.moveToNext());
                return ret;
            }
            finally {
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    @Override
    public void onUrlTitleResolved(String url, String title) {
        String selection = ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_URL + " = ?";
        String[] selectionArgs = { url };
        ContentValues values = new ContentValues();
        values.put(ClipboardUrlContract.ClipboardUrlEntry.COLUMN_NAME_TITLE, title);
        db.update(ClipboardUrlContract.ClipboardUrlEntry.TABLE_NAME, values, selection, selectionArgs);
    }
}

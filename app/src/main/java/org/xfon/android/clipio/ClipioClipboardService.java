package org.xfon.android.clipio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.xfon.android.clipio.db.SQLiteUrlStore;

/**
 * Created by xenofon on 6/13/15.
 */
public class ClipioClipboardService extends Service implements ClipboardManager.OnPrimaryClipChangedListener {

    private static final String TAG = ClipioClipboardService.class.getName();
    private final IBinder binder = new LocalBinder();
    private UrlStore store = null;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        if (store == null) store = SQLiteUrlStore.getInstance(this);
        ClipboardManager manager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        manager.addPrimaryClipChangedListener(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        ClipboardManager manager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        manager.removePrimaryClipChangedListener(this);
        super.onDestroy();
    }

    @Override
    public void onPrimaryClipChanged() {
        ClipboardManager manager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        ClipData data = manager.getPrimaryClip();
        if (data == null) return;
        CharSequence seq = data.getItemAt(0).getText();
        if (seq == null) return;
        String txt = seq.toString();
        if (!txt.startsWith("http")) return;
        ClipboardUrl url = new ClipboardUrl(txt);
        store.add(url);
        notifyUrlAdded(url);
    }

    private void notifyUrlAdded(ClipboardUrl url) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.link_added_notification))
                        .setContentText(url.getHostText());
        Intent intent = new Intent(this, ClipioActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }

    public class LocalBinder extends Binder {
        ClipioClipboardService getService() {
            return ClipioClipboardService.this;
        }
    }

    public UrlStore getUrlStore() {
        return store;
    }
}

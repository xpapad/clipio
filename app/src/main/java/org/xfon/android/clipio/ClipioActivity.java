package org.xfon.android.clipio;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class ClipioActivity extends ActionBarActivity {

    private SwipeRefreshLayout swipeContainer;
    private static final String TAG = ClipioActivity.class.getName();
    private static final boolean SHOW_ADS = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Activity started");
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        Intent intent = new Intent(this, ClipioClipboardService.class);
        startService(intent);
        setContentView(R.layout.activity_clipio);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });
        setupEmptyInfo();
        setupUrlDelete();
        setupAds();
    }

    private void setupEmptyInfo() {
        TextView emptyView = (TextView)findViewById(android.R.id.empty);
        String msg = getString(R.string.info);
        Log.d(TAG,"MSG: " + msg);
        emptyView.setText(Html.fromHtml(msg, new ImageGetter(), null));
    }

    private class ImageGetter implements Html.ImageGetter {
        public Drawable getDrawable(String source) {
            int id;
            switch (source) {
                case "delete.png":
                    id = R.drawable.ic_menu_delete;
                    break;
                case "logo.png":
                    id = R.drawable.logo;
                    break;
                default:
                    return null;
            }
            Drawable d = getResources().getDrawable(id);
            d.setBounds(0,0,d.getIntrinsicWidth(),d.getIntrinsicHeight());
            return d;
        }
    };


    private void setupUrlDelete() {
        final ListView listView = (ListView) findViewById(R.id.listView);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long id) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                ClipboardUrl url = (ClipboardUrl) listView.getItemAtPosition(position);
                                if (url == null) return;
                                deleteUrl(url);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(ClipioActivity.this);
                String msg = getString(R.string.confirm_delete);
                builder.setMessage(msg).setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();
                return true;
            }
        });
    }

    private void deleteUrl(final ClipboardUrl url) {
        Intent intent = new Intent(this, ClipioClipboardService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                ClipioClipboardService service = ((ClipioClipboardService.LocalBinder) binder).getService();
                UrlStore store = service.getUrlStore();
                store.delete(url);
                unbindService(this);
                refreshList();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);
    }

    private void setupAds() {
        /*AdView mAdView = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
        refreshList();
    }

    private void refreshList() {
        Intent intent = new Intent(this, ClipioClipboardService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d(TAG, "Receiving info from service");
                ClipioClipboardService service = ((ClipioClipboardService.LocalBinder) binder).getService();
                UrlStore store = service.getUrlStore();
                ListAdapter adapter = new ClipboardUrlAdapter(ClipioActivity.this, R.id.textLabel, store.getUrls(), store);
                ListView listView = (ListView) findViewById(R.id.listView);
                listView.setEmptyView(findViewById(android.R.id.empty));
                listView.setAdapter(adapter);
                unbindService(this);
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);
    }

    private void cleanupList() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ClipioActivity.this);
        final boolean cleanupToday = pref.getBoolean(SettingsActivity.KEY_PREF_CLEANUP_DELETES_TODAY, false);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteUrls(cleanupToday);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(ClipioActivity.this);
        String msg = getString(R.string.confirm_delete_all);
        if (!cleanupToday) {
            msg += getString(R.string.recent_not_deleted);
        }
        builder.setMessage(msg).setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    private void deleteUrls(final boolean cleanupTodays) {
        Intent intent = new Intent(this, ClipioClipboardService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                ClipioClipboardService service = ((ClipioClipboardService.LocalBinder) binder).getService();
                service.getUrlStore().deleteUnstarred(cleanupTodays);
                unbindService(this);
                refreshList();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_clipio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_cleanup:
                cleanupList();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

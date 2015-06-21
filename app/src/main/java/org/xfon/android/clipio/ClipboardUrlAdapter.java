package org.xfon.android.clipio;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by xenofon on 6/15/15.
 */
public class ClipboardUrlAdapter extends ArrayAdapter<ClipboardUrl> {
    private final Context context;
    private final List<ClipboardUrl> objects;
    private final UrlStore store;
    private static final String TAG = ClipboardUrlAdapter.class.getName();
    private final AtomicBoolean isLongClick = new AtomicBoolean(false);

    public ClipboardUrlAdapter(Context context, int resource, List<ClipboardUrl> objects, UrlStore store) {
        super(context, resource, objects);
        this.context = context;
        this.objects = objects;
        this.store = store;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ClipboardUrl url = objects.get(position);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.url_layout, parent, false);

        // Set Star button state
        ToggleButton toggleButton = (ToggleButton)rowView.findViewById(R.id.buttonStar);
        toggleButton.setChecked(url.isStarred());
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                store.setStarred(url, isChecked);
            }
        });

        // Set hostname
        TextView textHost = (TextView)rowView.findViewById(R.id.textHost);
        textHost.setText(url.getHostText());

        // Set label URL
        TextView textLabel = (TextView)rowView.findViewById(R.id.textLabel);
        Spanned sp = Html.fromHtml(url.getLabelHtml());
        textLabel.setText(sp);
        textLabel.setClickable(true);
        textLabel.setMovementMethod(LinkMovementMethod.getInstance());
        disableLongClick(textLabel);

        // Set create date indication
        TextView textCreated = (TextView)rowView.findViewById(R.id.textCreated);
        textCreated.setText(url.getCreateDateText());
        return rowView;
    }

    private void disableLongClick(TextView view) {
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isLongClick.set(true);
                return false;
            }
        });
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP && isLongClick.get()){
                    isLongClick.set(false);
                    return true;
                }
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    isLongClick.set(false);
                }
                return v.onTouchEvent(event);
            }
        });
    }
}

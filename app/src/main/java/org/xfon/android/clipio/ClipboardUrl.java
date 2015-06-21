package org.xfon.android.clipio;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xenofon on 6/14/15.
 */
public class ClipboardUrl {
    private final String url;
    private String title = null;
    private final Date createDate;
    private boolean starred = false;
    private String host = "Unknown";

    private static final Pattern hostPattern = Pattern.compile("https?://(.*?)(:|/|$)", Pattern.CASE_INSENSITIVE);

    public ClipboardUrl(String url) {
        this.url = url;
        this.createDate = GregorianCalendar.getInstance().getTime();
        this.starred = false;
        resolveHost();
    }

    public ClipboardUrl(String url, long epoch, String title, boolean starred) {
        this.url = url;
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeInMillis(epoch * 1000);
        this.createDate = cal.getTime();
        this.title = title;
        this.starred = starred;
        resolveHost();
    }

    private void resolveHost() {
        Matcher matcher = hostPattern.matcher(url);
        if (matcher.find()) {
            host = matcher.group(1);
        }
    }

    public String getUrl() { return url; }
    public String getTitle() { return title; }
    public boolean isStarred() { return starred; }
    public long getEpoch() {
        return createDate.getTime() / 1000;
    }

    @Override
    public String toString() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
        return df.format(createDate) + " - [" + title + "] - " + url;
    }

    public String getHostText() {
        return host;
    }
    public String getLabelText() {
        return title != null ? title : url;
    }

    public String getLabelHtml() {
        String html = "<html><a href=\"" + url + "\">" + getLabelText() + "</a></html>";
        return html;
    }

    public String getCreateDateText() {
        return DateHelper.getTimeSinceText(createDate);
    }
}

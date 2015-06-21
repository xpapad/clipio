package org.xfon.android.clipio;

import java.util.List;

/**
 * Created by xenofon on 6/14/15.
 */
public interface UrlStore {
    public void add(ClipboardUrl url);
    public void delete(ClipboardUrl url);
    public void setStarred(ClipboardUrl url, boolean starred);
    public void deleteUnstarred(boolean cleanupTodays);
    public List<ClipboardUrl> getUrls();
}

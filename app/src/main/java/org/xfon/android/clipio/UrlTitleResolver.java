package org.xfon.android.clipio;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xenofon on 6/14/15.
 */
public class UrlTitleResolver {
    private static final Pattern TITLE_TAG = Pattern.compile("\\<title>(.*?)\\</title>", Pattern.CASE_INSENSITIVE|Pattern.DOTALL|Pattern.MULTILINE);
    private static final int SCAN_CHARS_LIMIT = 12000;
    private static final String TAG = UrlTitleResolver.class.getName();
    private static final int MAX_THREADS = 3;
    //private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.81 Safari/537.36";
    private ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
    private OnUrlTitleResolvedListener listener = null;

    public interface OnUrlTitleResolvedListener {
        public void onUrlTitleResolved(String url, String title);
    }

    public void setOnUrlTitleResolvedListener(OnUrlTitleResolvedListener listener) {
        this.listener = listener;
    }

    public void resolveTitle(final String url) {
        Log.d(TAG, "Resolving title for: " + url);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    String title = getPageTitle(url);
                    if (title != null) {
                        listener.onUrlTitleResolved(url, title);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        };
        executor.submit(r);
    }

    private String getPageTitle(String url) throws IOException {
        Log.d(TAG, "Requesting title for: " + url);
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection)u.openConnection();
        //conn.setRequestProperty("User-Agent", USER_AGENT);

        ContentType contentType = getContentTypeHeader(conn);
        Log.d(TAG, "Content type: " + contentType.contentType);
        if (!contentType.contentType.equals("text/html"))
            return null; // don't continue if not HTML
        else {
            // determine the charset, or use the default
            Charset charset = getCharset(contentType);
            if (charset == null) charset = Charset.defaultCharset();
            Log.d(TAG, "Charset: " + charset);

            // read the response body, using BufferedReader for performance
            InputStream in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
            int n = 0, totalRead = 0;
            char[] buf = new char[1024];
            StringBuilder content = new StringBuilder();

            // read until EOF or first SCAN_CHARS_LIMIT characters
            while (totalRead < SCAN_CHARS_LIMIT && (n = reader.read(buf, 0, buf.length)) != -1) {
                content.append(buf, 0, n);
                totalRead += n;
            }
            reader.close();

            // extract the title
            Matcher matcher = TITLE_TAG.matcher(content);
            if (matcher.find()) {
                Log.d(TAG, "Title found!: " + matcher.group(1));
                return matcher.group(1).replaceAll("[\\s\\<>]+", " ").trim();
            }
            else {
                Log.d(TAG, "Title not found");
                Log.d(TAG, content.toString());
                return null;
            }
        }
    }

    private Charset getCharset(ContentType contentType) {
        if (contentType != null && contentType.charsetName != null && Charset.isSupported(contentType.charsetName))
            return Charset.forName(contentType.charsetName);
        else
            return null;
    }

    private ContentType getContentTypeHeader(HttpURLConnection conn) {
        String contentType = conn.getHeaderField("Content-Type");
        if (contentType != null) return new ContentType(contentType);
        return null;
    }

    private static final class ContentType {
        private static final Pattern CHARSET_HEADER = Pattern.compile("charset=([-_a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);

        private String contentType;
        private String charsetName;
        private ContentType(String headerValue) {
            if (headerValue == null)
                throw new IllegalArgumentException("ContentType must be constructed with a not-null headerValue");
            int n = headerValue.indexOf(";");
            if (n != -1) {
                contentType = headerValue.substring(0, n);
                Matcher matcher = CHARSET_HEADER.matcher(headerValue);
                if (matcher.find())
                    charsetName = matcher.group(1);
            }
            else {
                contentType = headerValue;
            }
        }
    }
}

package net.majorkernelpanic.streaming.rtsp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class RtspRequest {

    private static final String TAG = "RtspRequest";

    // Parse mMethod & uri
    static final Pattern regexMethod = Pattern.compile("(\\w+) (\\S+) RTSP",Pattern.CASE_INSENSITIVE);
    // Parse a request header
    static final Pattern regexHeader = Pattern.compile("(\\S+):(.+)",Pattern.CASE_INSENSITIVE);

    private String mMethod;
    private String mUri;
    private final HashMap<String,String> mHeaders = new HashMap<>();

    /** Parse the mMethod, uri & headers of a RTSP request */
    static RtspRequest parseRequest(BufferedReader input) throws IOException, IllegalStateException, SocketException {
        RtspRequest request = new RtspRequest();
        String line;
        Matcher matcher;

        // Parsing request method & uri
        if ((line = input.readLine()) == null) {
            throw new SocketException("Client disconnected");
        }
        matcher = regexMethod.matcher(line);
        matcher.find();
        request.mMethod = matcher.group(1);
        request.mUri = matcher.group(2);

        // Parsing headers of the request
        StringBuilder requestLines = new StringBuilder("");
        while ((line = input.readLine()) != null && line.length() > 3) {
            matcher = regexHeader.matcher(line);
            matcher.find();
            request.mHeaders.put(matcher.group(1).toLowerCase(Locale.US),matcher.group(2));

            requestLines.append(line).append("\n");
        }
//        if (line == null) {
//            throw new SocketException("Client disconnected");
//        }

        // It's not an error, it's just easier to follow what's happening in logcat with the request in red
        Log.e(TAG,request.mMethod + " " + request.mUri);
        Log.e(TAG,requestLines.toString());

        return request;
    }

    String getMethod() {
        return mMethod;
    }

    String getUri() {
        return mUri;
    }

    String getHeader(final String key) {
        return mHeaders.get(key);
    }
}

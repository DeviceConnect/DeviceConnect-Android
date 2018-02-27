package net.majorkernelpanic.streaming.rtsp;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;


class RtspResponse {

    private static final String TAG = "RtspResponse";

    static final String STATUS_OK = "200 OK";
    static final String STATUS_BAD_REQUEST = "400 Bad RtspRequest";
    static final String STATUS_NOT_FOUND = "404 Not Found";
    static final String STATUS_INTERNAL_SERVER_ERROR = "500 Internal Server Error";

    String status = STATUS_INTERNAL_SERVER_ERROR;
    String content = "";
    String attributes = "";
    String serverName = "";

    private final RtspRequest mRequest;

    RtspResponse(final RtspRequest request) {
        mRequest = request;
    }

    public void send(final OutputStream output) throws IOException {
        int seqid = -1;

        try {
            seqid = Integer.parseInt(mRequest.getHeader("cseq").replace(" ",""));
        } catch (Exception e) {
            Log.e(TAG,"Error parsing CSeq: "+(e.getMessage()!=null?e.getMessage():""));
        }

        String response = 	"RTSP/1.0 " + status + "\r\n" +
                "Server: " + serverName + "\r\n" +
                (seqid >= 0 ? ("Cseq: " + seqid + "\r\n") : "") +
                "Content-Length: " + content.length() + "\r\n" +
                attributes +
                "\r\n" +
                content;

        Log.d(TAG,response.replace("\r", ""));

        output.write(response.getBytes());
    }

}

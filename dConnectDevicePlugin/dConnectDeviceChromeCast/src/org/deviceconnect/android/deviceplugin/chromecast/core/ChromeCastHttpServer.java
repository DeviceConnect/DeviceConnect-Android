/*
 ChromeCastHttpServer.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.chromecast.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.deviceconnect.android.deviceplugin.chromecast.BuildConfig;

import fi.iki.elonen.NanoHTTPD;

/**
 * Chromecast HttpServer クラス.
 * 
 * <p>
 * HttpServer機能を提供<br/>
 * - 選択されたファイルのみ配信する<br/>
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class ChromeCastHttpServer extends NanoHTTPD {

    /** Local IP Address prefix.  */
    private static final String PREFIX_LOCAL_IP = "192.168.";

    /** Logger. */
    private final Logger mLogger = Logger.getLogger("chromecast.dplugin");

    /** The List of media files. */
    private final List<MediaFile> mFileList = new ArrayList<MediaFile>();

    /**
     * コンストラクタ.
     * 
     * @param host ipアドレス
     * @param port ポート番号
     */
    public ChromeCastHttpServer(final String host, final int port) {
        super(host, port);
    }

    /**
     * クライアントに応答する.
     * 
     * @param session セッション
     * @return レスポンス
     */
    public Response serve(final IHTTPSession session) {
        mLogger.info("Received HTTP request: " + session.getUri());

        Map<String, String> header = session.getHeaders();
        String uri = session.getUri();
        return respond(Collections.unmodifiableMap(header), session, uri);
    }

    /**
     * 指定されたファイルを公開する.
     *
     * @param file 公開するファイル
     * @return 公開用URI
     */
    public String exposeFile(final MediaFile file) {
        synchronized (mFileList) {
            mFileList.add(file);
        }
        String address = getIpAddress();
        if (address == null) {
            return null;
        }
        return "http://" + address + ":" + getListeningPort() + file.getPath();
    }

    /**
     * クライアントをチェックする.
     * 
     * @param headers ヘッダー
     * @return  有効か否か	(true: 有効, false: 無効)
     */
    private boolean checkRemote(final Map<String, String> headers) {
        String remoteAddr = headers.get("remote-addr");		
        try {
            InetAddress addr = InetAddress.getByName(remoteAddr);
            return addr.isSiteLocalAddress();
        } catch (UnknownHostException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * クライアントへのレスポンスを作成する.
     * 
     * @param   headers     ヘッダー
     * @param   session     セッション
     * @param   uri         ファイルのURI
     * @return  レスポンス
     */
    private Response respond(final Map<String, String> headers, final IHTTPSession session, final String uri) {
        if (!checkRemote(headers)) {
            return createResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "");
        }

        MediaFile mediaFile = findFile(uri);
        if (mediaFile == null) {
            mLogger.info("File not found: URI=" + uri);
            return createResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "");
        }
        mLogger.info("Found File: " + mediaFile.mFile.getAbsolutePath());

        Response response = serveFile(uri, headers, mediaFile.mFile, "");
        if (response == null) {
            return createResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "");
        }
        return response;
    }

    /**
     * 指定したURIで公開しているファイルを検索する.
     *
     * @param uri ファイル公開用URI
     * @return 検索により見つかったファイル. 見つからなかった場合は<code>null</code>
     */
    private MediaFile findFile(final String uri) {
        synchronized (mFileList) {
            for (MediaFile file : mFileList) {
                mLogger.info(" - " + file.getPath());
                if (uri.equals(file.getPath())) {
                    return file;
                }
            }
        }
        return null;
    }

    /**
     * レスポンスを作成する.
     * 
     * @param   status      ステータス
     * @param   mimeType    MIMEタイプ
     * @param   message     メッセージ (InputStream)
     * @return レスポンス
     */
    private Response createResponse(final Response.Status status, final String mimeType, final InputStream message) {
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    /**
     * レスポンスを作成する.
     * 
     * @param   status      ステータス
     * @param   mimeType    MIMEタイプ
     * @param   message     メッセージ (String)
     * @return レスポンス
     */
    private Response createResponse(final Response.Status status, final String mimeType, final String message) {
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    /**
     * IPアドレスを取得する.
     * 
     * @return  IPアドレス
     */
    private String getIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            LinkedList<InetAddress> localAddresses = new LinkedList<InetAddress>();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) networkInterfaces
                        .nextElement();
                Enumeration<InetAddress> ipAddrs = networkInterface
                        .getInetAddresses();
                while (ipAddrs.hasMoreElements()) {
                    InetAddress ip = (InetAddress) ipAddrs.nextElement();
                    String ipStr = ip.getHostAddress();

                    mLogger.info("Searching IP Address: Address=" + ipStr
                        + " isLoopback=" + ip.isLoopbackAddress()
                        + " isSiteLocal=" + ip.isSiteLocalAddress());

                    if (ipStr.startsWith(PREFIX_LOCAL_IP)) {
                        localAddresses.addFirst(ip);
                    }
                }
            }
            if (localAddresses.size() == 0) {
                return null;
            }
            return localAddresses.get(0).getHostAddress();
        } catch (SocketException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * ファイルのレスポンスを作成する.
     * 
     * @param   uri         ファイルのURI
     * @param   header      ヘッダー
     * @param   file        ファイル
     * @param   mime        MIMEタイプ
     * @return レスポンス
     */
    Response serveFile(final String uri, final Map<String, String> header, final File file, final String mime) {

        Response res;
        try {
            String etag = Integer.toHexString((file.getAbsolutePath()
                    + file.lastModified() + "" + file.length()).hashCode());

            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range
                                    .substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                        if (BuildConfig.DEBUG) {
                            ignored.printStackTrace();
                        }
                    }
                }
            }

            long fileLen = file.length();
            if (range != null && startFrom >= 0) {
                if (startFrom >= fileLen) {
                    res = createResponse(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    final long dataLen = newLen;
                    FileInputStream fis = new FileInputStream(file) {
                        @Override
                        public int available() throws IOException {
                            return (int) dataLen;
                        }
                    };
                    fis.skip(startFrom);

                    res = createResponse(Response.Status.PARTIAL_CONTENT, mime, fis);
                    res.addHeader("Content-Length", "" + dataLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {
                if (etag.equals(header.get("if-none-match"))) {
                    res = createResponse(Response.Status.NOT_MODIFIED, mime, "");
                } else {
                    res = createResponse(Response.Status.OK, mime, new FileInputStream(file));
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        } catch (IOException ioe) {
            res = createResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "");
        }

        return res;
    }
}

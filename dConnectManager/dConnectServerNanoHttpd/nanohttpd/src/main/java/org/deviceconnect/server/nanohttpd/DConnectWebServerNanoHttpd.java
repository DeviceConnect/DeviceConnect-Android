/*
 DConnectServerNanoHttpd.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.server.nanohttpd;

import android.content.Context;
import android.util.Log;

import org.deviceconnect.server.DConnectServerConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.net.ssl.SSLServerSocketFactory;

import fi.iki.elonen.NanoHTTPD;

/**
 * Web サーバー NanoHTTPD.
 *
 * @author NTT DOCOMO, INC.
 */
public class DConnectWebServerNanoHttpd {

    public interface Dispatcher {

        InputStream dispatch(String uri);
    }

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "NanoWeb";

    /**
     * Default Index file names.
     */
    @SuppressWarnings("serial")
    private static final List<String> INDEX_FILE_NAMES = new ArrayList<String>() {
        {
            add("index.html");
            add("index.htm");
        }
    };

    /**
     * {@link WebServer} のインスタンス.
     */
    private WebServer mWebServer;

    /**
     * Webサーバの設定.
     */
    private Config mConfig;

    /**
     * ファイル取得ロジック.
     */
    private Dispatcher mDispatcher = (uri) -> null;

    /**
     * コンストラクタ.
     *
     * @param config Webサーバを起動するためのコンフィグ
     */
    private DConnectWebServerNanoHttpd(final Config config) {
        mConfig = config;

        if (mConfig.mPort < 1024) {
            throw new IllegalArgumentException("port is invalid.");
        }

        if (mConfig.mDocRootList == null || mConfig.mDocRootList.isEmpty()) {
            throw new IllegalArgumentException("Document root is not set.");
        }

        for (String homeDir : mConfig.mDocRootList) {
            if (!isAssets(homeDir) && !new File(homeDir).isDirectory()) {
                throw new IllegalArgumentException("Document root is invalid.");
            }
        }
    }

    /**
     * Web サーバを開始します.
     *
     */
    public synchronized void start() {
        if (mWebServer != null) {
            throw new IllegalStateException("WebServer is already running.");
        }

        mWebServer = new WebServer(mConfig);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mWebServer.start();
                } catch (IOException e) {
                    // ignore.
                }
            }
        }).start();
    }

    /**
     * Web サーバを停止します.
     */
    public synchronized void stop() {
        if (mWebServer != null) {
            mWebServer.stop();
            mWebServer = null;
        }
    }

    public void setDispatcher(final Dispatcher dispatcher) {
        if (dispatcher == null) {
            throw new IllegalArgumentException("dispatcher is null");
        }
        mDispatcher = dispatcher;
    }

    /**
     * 指定されたパスが assets へのパスか確認します.
     *
     * @param path 確認するパス
     * @return assets へのパスの場合はtrue、それ以外はfalse
     */
    private boolean isAssets(final String path) {
        return path != null && path.startsWith(DConnectServerConfig.DOC_ASSETS);
    }

    /**
     * Web サーバ.
     */
    private class WebServer extends NanoHTTPD {

        /**
         * CORS で許可するオリジン.
         */
        private final String mCors;

        /**
         * バージョン.
         */
        private final String mVersion;

        /**
         * ドキュメントルートのリスト.
         */
        private List<String> mRootDirs;

        /**
         * コンテキスト.
         */
        private Context mContext;

        /**
         * コンストラクタ.
         *
         * @param config Webサーバを起動するためのコンフィグ
         */
        private WebServer(final Config config) {
            super(config.mHost, config.mPort);
            mContext = config.mContext;
            mCors = config.mCors;
            mRootDirs = new ArrayList<>(config.mDocRootList);
            mVersion = config.mVersion;

            // SSL設定
            final SSLServerSocketFactory factory = config.mServerSocketFactory;
            if (factory != null) {
                setServerSocketFactory(factory::createServerSocket);
            }

            try {
                mimeTypes();
            } catch (Exception e){
                // ignore
            }
        }

        @Override
        protected boolean useGzipWhenAccepted(final Response r) {
            return false;
        }

        @Override
        public Response serve(final IHTTPSession session) {
            Map<String, String> header = session.getHeaders();
            String uri = session.getUri();
            try {
                return respond(Collections.unmodifiableMap(header), session, uri);
            } catch (Exception e) {
                if (DEBUG) {
                    Log.w(TAG, "Internal Server Error", e);
                }
                return newInternalErrorResponse(e.getMessage());
            }
        }

        /**
         * 指定された URI の静的ファイルを返却します.
         *
         * @param headers リクエストヘッダー
         * @param session リクエストのセッション
         * @param uri リクエストURI
         * @return レスポンス
         */
        private Response respond(final Map<String, String> headers, final IHTTPSession session, final String uri) {
            Response r;
            if (mCors != null && Method.OPTIONS.equals(session.getMethod())) {
                // クロスドメイン対応としてOPTIONSがきたらDevice Connect で対応しているメソッドを返す
                // Device Connect 対応外のメソッドだがエラーにはしないのでここで処理を終了。
                r = newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "");
                r.addHeader("Access-Control-Allow-Methods", "GET");
            } else if (Method.GET.equals(session.getMethod())) {
                r = defaultRespond(headers, session, uri);
            } else {
                r = newMethodNotAllowedResponse();
            }

            if (mCors != null) {
                r = addCORSHeaders(headers, r, mCors);
            }
            return r;
        }

        /**
         * ファイルのレスポンス処理を行います.
         *
         * @param headers リクエストヘッダー
         * @param session HTTPセッション
         * @param uri リクエストURI
         * @return レスポンス
         */
        private Response defaultRespond(final Map<String, String> headers, final IHTTPSession session, String uri) {
            // Remove URL arguments
            uri = uri.trim().replace(File.separatorChar, '/');
            if (uri.indexOf('?') >= 0) {
                uri = uri.substring(0, uri.indexOf('?'));
            }

            // Prohibit getting out of current directory
            if (uri.contains("../")) {
                return newForbiddenResponse("Won't serve ../ for security reasons.");
            }

            InputStream in = mDispatcher.dispatch(session.getUri());
            if (in != null) {
                return serveInputStream(uri, session.getQueryParameterString(), headers, in);
            }

            boolean canServeUri = false;
            String homeDir = null;
            for (int i = 0; !canServeUri && i < mRootDirs.size(); i++) {
                homeDir = mRootDirs.get(i);
                canServeUri = canServeUri(uri, homeDir);
            }
            if (!canServeUri) {
                return newNotFoundResponse();
            }

            if (isAssets(homeDir)) {
                return serveAssets(homeDir + uri, session.getQueryParameterString(), headers, homeDir);
            } else {
                // Browsers get confused without '/' after the directory, send a redirect.
                File f = new File(homeDir, uri);
                if (f.isDirectory() && !uri.endsWith("/")) {
                    uri += "/";
                    Response res = newRedirectResponse(uri);
                    res.addHeader("Accept-Ranges", "bytes");
                    return res;
                }

                if (f.isDirectory()) {
                    // First look for index files (index.html, index.htm, etc) and if
                    // none found, list the directory if readable.
                    String indexFile = findIndexFileInDirectory(f);
                    if (indexFile == null) {
                        if (f.canRead()) {
                            // No index file, list the directory if it is readable
                            return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, listDirectory(uri, f));
                        } else {
                            return newForbiddenResponse("No directory listing.");
                        }
                    } else {
                        return respond(headers, session, uri + indexFile);
                    }
                }

                String mimeTypeForFile = getMimeTypeForFile(uri);
                Response response = serveFile(uri, session.getQueryParameterString(), headers, f, mimeTypeForFile);
                return response != null ? response : newNotFoundResponse();
            }
        }

        private Response serveInputStream(final String uri,
                                          final String queryString,
                                          final Map<String, String> header,
                                          final InputStream in) {
            Response retValue;

            String mime = header.get("content-type");
            // http の仕様より、content-type で MIME Type が特定できない場合は
            // URI から MIME Type を推測する。
            if (mime == null || !MIME_TYPES.containsValue(mime)) {
                mime = getMimeTypeFromURI(uri);
            }

            try {
                // If-None-Match対応
                String etag = createETag(uri, queryString);
                if (etag.equals(header.get("if-none-match"))) {
                    retValue = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                } else {
                    retValue = newFixedLengthResponse(Response.Status.OK, mime, in, in.available());
                    retValue.addHeader("Content-Length", "" + in.available());
                    retValue.addHeader("ETag", etag);
                }

                // ByteRangeへの対応は必須ではないため、noneを指定して対応しないことを伝える。
                // 対応が必要な場合はbyteを設定して実装すること。
                retValue.addHeader("Accept-Ranges", "none");
            } catch (IOException e) {
                if (DEBUG) {
                    Log.w(TAG, "Not Found a file.", e);
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e1) {
                        // ignore.
                    }
                }
                retValue = newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, Response.Status.NOT_FOUND.getDescription());
            }

            return retValue;
        }

        /**
         * Assets にあるファイルを読み込みます.
         *
         * @param uri URI
         * @param queryString クエリ
         * @param header ヘッダー
         * @param homeDir ドキュメントルート
         * @return HTTPレスポンス
         */
        private Response serveAssets(final String uri, final String queryString, final Map<String, String> header, final String homeDir) {
            Response retValue;

            String filePath = renameUriForAssets(uri, homeDir);

            String mime = header.get("content-type");
            // http の仕様より、content-type で MIME Type が特定できない場合は
            // URI から MIME Type を推測する。
            if (mime == null || !MIME_TYPES.containsValue(mime)) {
                mime = getMimeTypeFromURI(filePath);
            }

            InputStream in = null;
            try {
                in = mContext.getAssets().open(filePath);

                // If-None-Match対応
                String etag = createETag(filePath, queryString);
                if (etag.equals(header.get("if-none-match"))) {
                    retValue = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                } else {
                    retValue = newFixedLengthResponse(Response.Status.OK, mime, in, in.available());
                    retValue.addHeader("Content-Length", "" + in.available());
                    retValue.addHeader("ETag", etag);
                }

                // ByteRangeへの対応は必須ではないため、noneを指定して対応しないことを伝える。
                // 対応が必要な場合はbyteを設定して実装すること。
                retValue.addHeader("Accept-Ranges", "none");
            } catch (IOException e) {
                if (DEBUG) {
                    Log.w(TAG, "Not Found a file.", e);
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e1) {
                        // ignore.
                    }
                }
                retValue = newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, Response.Status.NOT_FOUND.getDescription());
            }

            return retValue;
        }

        /**
         * 静的ファイルを読み込みレスポンスに格納します.
         *
         * @param uri リクエストURI
         * @param header リクエストヘッダー
         * @param file ファイル
         * @param mime マイムタイプ
         * @return レスポンス
         */
        private Response serveFile(final String uri, final String queryString, final Map<String, String> header, final File file, final String mime) {
            Response res;
            try {
                // If-None-Match対応
                String etag = createETag(file, queryString);

                // Support (simple) skipping:
                long startFrom = 0;
                long endAt = -1;
                String range = header.get("range");
                if (range != null) {
                    if (range.startsWith("bytes=")) {
                        range = range.substring("bytes=".length());
                        int minus = range.indexOf('-');
                        try {
                            if (minus > 0) {
                                startFrom = Long.parseLong(range.substring(0, minus));
                                endAt = Long.parseLong(range.substring(minus + 1));
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }

                // get if-range header. If present, it must match etag or else we
                // should ignore the range request
                String ifRange = header.get("if-range");
                boolean headerIfRangeMissingOrMatching = (ifRange == null || etag.equals(ifRange));

                String ifNoneMatch = header.get("if-none-match");
                boolean headerIfNoneMatchPresentAndMatching = ifNoneMatch != null && ("*".equals(ifNoneMatch) || ifNoneMatch.equals(etag));

                // Change return code and add Content-Range header when skipping is
                // requested
                long fileLen = file.length();

                if (headerIfRangeMissingOrMatching && range != null && startFrom >= 0 && startFrom < fileLen) {
                    // range request that matches current etag
                    // and the startFrom of the range is satisfiable
                    if (headerIfNoneMatchPresentAndMatching) {
                        // range request that matches current etag
                        // and the startFrom of the range is satisfiable
                        // would return range from file
                        // respond with not-modified
                        res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                        res.addHeader("Accept-Ranges", "bytes");
                        res.addHeader("ETag", etag);
                    } else {
                        if (endAt < 0) {
                            endAt = fileLen - 1;
                        }
                        long newLen = endAt - startFrom + 1;
                        if (newLen < 0) {
                            newLen = 0;
                        }

                        FileInputStream fis = new FileInputStream(file);
                        fis.skip(startFrom);

                        res = newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, mime, fis, newLen);
                        res.addHeader("Accept-Ranges", "bytes");
                        res.addHeader("Content-Length", "" + newLen);
                        res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                        res.addHeader("ETag", etag);
                    }
                } else {

                    if (headerIfRangeMissingOrMatching && range != null && startFrom >= fileLen) {
                        // return the size of the file
                        // 4xx responses are not trumped by if-none-match
                        res = newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
                        res.addHeader("Accept-Ranges", "bytes");
                        res.addHeader("Content-Range", "bytes */" + fileLen);
                        res.addHeader("ETag", etag);
                    } else if (range == null && headerIfNoneMatchPresentAndMatching) {
                        // full-file-fetch request
                        // would return entire file
                        // respond with not-modified
                        res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                        res.addHeader("Accept-Ranges", "bytes");
                        res.addHeader("ETag", etag);
                    } else if (!headerIfRangeMissingOrMatching && headerIfNoneMatchPresentAndMatching) {
                        // range request that doesn't match current etag
                        // would return entire (different) file
                        // respond with not-modified
                        res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                        res.addHeader("Accept-Ranges", "bytes");
                        res.addHeader("ETag", etag);
                    } else {
                        // supply the file
                        res = newFixedFileResponse(file, mime);
                        res.addHeader("Content-Length", "" + fileLen);
                        res.addHeader("ETag", etag);
                    }
                }
            } catch (IOException ioe) {
                if (DEBUG) {
                    Log.w(TAG, "IOException occurred.", ioe);
                }
                res = newForbiddenResponse("Reading file failed.");
            }

            return res;
        }

        /**
         * Assets にあるファイルパスへリネームします.
         *
         * @param uri URI
         * @param homeDir ドキュメントルート
         * @return Assets にあるファイルパス
         */
        private String renameUriForAssets(final String uri, final String homeDir) {
            String filePath = uri;

            // パスに何も入力されていない場合には index.html に飛ばす
            if (filePath == null || filePath.isEmpty()) {
                filePath = "/index.html";
            } else if (filePath.endsWith("/")) {
                filePath = filePath + "index.html";
            }

            // assets フォルダのさらに下のフォルダをドキュメントルートにした場合
            if (homeDir.length() > DConnectServerConfig.DOC_ASSETS.length()) {
                filePath = filePath.substring(DConnectServerConfig.DOC_ASSETS.length());
            }

            // 先頭に / があるとファイルが開けないので削除
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1);
            }

            return filePath;
        }

        /**
         * URIからMIMEタイプを推測する.
         *
         * @param uri リクエストURI
         * @return MIMEタイプが推測できた場合MIMEタイプ文字列を、その他はnullを返す
         */
        private String getMimeTypeFromURI(final String uri) {
            int dot = uri.lastIndexOf('.');
            if (dot >= 0) {
                return MIME_TYPES.get(uri.substring(dot + 1).toLowerCase(Locale.ENGLISH));
            }
            return null;
        }

        /**
         * URL-encodes everything between "/"-characters. Encodes spaces as '%20'
         * instead of '+'.
         */
        private String encodeUri(final String uri) {
            String newUri = "";
            StringTokenizer st = new StringTokenizer(uri, "/ ", true);
            while (st.hasMoreTokens()) {
                String tok = st.nextToken();
                if ("/".equals(tok)) {
                    newUri += "/";
                } else if (" ".equals(tok)) {
                    newUri += "%20";
                } else {
                    try {
                        newUri += URLEncoder.encode(tok, "UTF-8");
                    } catch (UnsupportedEncodingException ignored) {
                    }
                }
            }
            return newUri;
        }

        /**
         * 指定したフォルダからindex.htmlを探します.
         *
         * @param directory index.htmlを検索するフォルダ
         * @return 発見した場合にはFileのインスタンス、発見できなかった場合はnullを返却します
         */
        private String findIndexFileInDirectory(final File directory) {
            for (String fileName : DConnectWebServerNanoHttpd.INDEX_FILE_NAMES) {
                File indexFile = new File(directory, fileName);
                if (indexFile.isFile()) {
                    return fileName;
                }
            }
            return null;
        }

        /**
         * リダイレクトする場合のレスポンスを返却します.
         *
         * @param uri リダイレクト先のURI
         * @return レスポンス
         */
        private Response newRedirectResponse(final String uri) {
            Response res = newFixedLengthResponse(Response.Status.REDIRECT, NanoHTTPD.MIME_HTML,
                    "<html><body>Redirected: <a href=\"" + uri + "\">" + uri + "</a></body></html>");
            res.addHeader("Location", uri);
            return res;
        }

        /**
         * アクセス拒否のレスポンスを返却します.
         *
         * @param s アクセス拒否のメッセージ
         * @return レスポンス
         */
        private Response newForbiddenResponse(final String s) {
            return newFixedLengthResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT,
                    "FORBIDDEN: " + s);
        }

        /**
         * インターナルサーバーエラーのレスポンスを返却します.
         *
         * @param s メッセージ
         * @return レスポンス
         */
        private Response newInternalErrorResponse(final String s) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                    "INTERNAL ERROR: " + s);
        }

        /**
         * ファイルが見つからなかった場合のレスポンスを返却します.
         *
         * @return レスポンス
         */
        private Response newNotFoundResponse() {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT,
                    "Error 404, file not found.");
        }

        /**
         * 許可されていないメソッドの場合のレスポンスを返却します.
         *
         * @return レスポンス
         */
        private Response newMethodNotAllowedResponse() {
            return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, NanoHTTPD.MIME_PLAINTEXT,
                    "Error 405, The method specified in the request is not allowed.");
        }

        /**
         * Accept-Rangesヘッダーを付加したレスポンスを作成します.
         *
         * @param file ファイル
         * @param mime マイムタイプ
         * @return レスポンス
         * @throws FileNotFoundException ファイルが存在しない場合
         */
        private Response newFixedFileResponse(final File file, final String mime) throws FileNotFoundException {
            Response res;
            res = newFixedLengthResponse(Response.Status.OK, mime, new FileInputStream(file), (int) file.length());
            res.addHeader("Accept-Ranges", "bytes");
            return res;
        }

        /**
         * 指定されたフォルダのファイルをリスト化したHTMLを返却します.
         *
         * @param uri URI
         * @param f フォルダ
         * @return ファイルをリスト化したHTML
         */
        private String listDirectory(final String uri, final File f) {
            String heading = "Directory " + uri;
            StringBuilder msg =
                    new StringBuilder("<html><head><title>" + heading +
                            "</title><style><!--\n"
                            + "span.dirname { font-weight: bold; }\n"
                            + "span.filesize { font-size: 75%; }\n"
                            + "// -->\n" + "</style>"
                            + "</head><body><h1>" + heading + "</h1>");

            String up = null;
            if (uri.length() > 1) {
                String u = uri.substring(0, uri.length() - 1);
                int slash = u.lastIndexOf('/');
                if (slash >= 0 && slash < u.length()) {
                    up = uri.substring(0, slash + 1);
                }
            }

            List<String> files = Arrays.asList(f.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isFile();
                }
            }));
            Collections.sort(files);
            List<String> directories = Arrays.asList(f.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isDirectory();
                }
            }));
            Collections.sort(directories);

            if (up != null || directories.size() + files.size() > 0) {
                msg.append("<ul>");
                if (up != null || directories.size() > 0) {
                    msg.append("<section class=\"directories\">");
                    if (up != null) {
                        msg.append("<li><a rel=\"directory\" href=\"").append(up).append("\"><span class=\"dirname\">..</span></a></li>");
                    }
                    for (String directory : directories) {
                        String dir = directory + "/";
                        msg.append("<li><a rel=\"directory\" href=\"").append(encodeUri(uri + dir)).append("\"><span class=\"dirname\">").append(dir).append("</span></a></li>");
                    }
                    msg.append("</section>");
                }
                if (files.size() > 0) {
                    msg.append("<section class=\"files\">");
                    for (String file : files) {
                        msg.append("<li><a href=\"").append(encodeUri(uri + file)).append("\"><span class=\"filename\">").append(file).append("</span></a>");
                        File curFile = new File(f, file);
                        long len = curFile.length();
                        msg.append("&nbsp;<span class=\"filesize\">(");
                        msg.append(getFileLengthString(len));
                        msg.append(")</span></li>");
                    }
                    msg.append("</section>");
                }
                msg.append("</ul>");
            }
            msg.append("</body></html>");
            return msg.toString();
        }

        /**
         * ETagを作成します.
         *
         * @param file ファイル
         * @param queryParameter クエリ
         * @return ETag
         */
        private String createETag(final File file, final String queryParameter) {
            int hashCode = 0;
            if (mVersion != null) {
                hashCode += mVersion.hashCode();
            }
            hashCode += (file.getAbsolutePath() + file.lastModified() + "" + file.length()).hashCode();
            if (queryParameter != null) {
                hashCode += queryParameter.hashCode();
            }
            return Integer.toHexString(hashCode);
        }

        /**
         * ETagを作成します.
         *
         * @param filePath ファイル
         * @param queryParameter クエリ
         * @return ETag
         */
        private String createETag(final String filePath, final String queryParameter) {
            int hashCode = 0;
            if (mVersion != null) {
                hashCode += mVersion.hashCode();
            }
            hashCode += filePath.hashCode();
            if (queryParameter != null) {
                hashCode += queryParameter.hashCode();
            }

            // If-None-Match対応
            return Integer.toHexString(hashCode);
        }

        /**
         * ファイルサイズを文字列に変換します.
         *
         * @param len ファイルサイズ
         * @return 変換された文字列
         */
        private String getFileLengthString(final long len) {
            if (len < 1024) {
                return len +" bytes";
            } else if (len < 1024 * 1024) {
                return (len / 1024) + "." + (len % 1024 / 10 % 100) + " KB";
            } else if (len < 1024 * 1024 * 1024) {
                return (len / (1024 * 1024)) + "." + (len % (1024 * 1024) / 10000 % 100) + " MB";
            } else {
                return (len / (1024 * 1024 * 1024)) + "." + (len % (1024 * 1024 * 1024) / 10000 % 100) + " GB";
            }
        }

        /**
         * 指定された URI が存在するか確認します.
         *
         * @param uri URI(ファイル名)
         * @param homeDir ドキュメントルート
         * @return 存在する場合はtrue、それ以外はfalse
         */
        private boolean canServeUri(final String uri, final String homeDir) {
            if (isAssets(homeDir)) {
                String filePath = renameUriForAssets(homeDir + uri, homeDir);
                InputStream in = null;
                try {
                    in = mContext.getAssets().open(filePath);
                    return true;
                } catch (IOException e) {
                    return false;
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // ignore.
                        }
                    }
                }
            } else {
                return new File(new File(homeDir), uri).exists();
            }
        }

        /**
         * レスポンスに CORS ヘッダーを付加します.
         *
         * @param queryHeaders 許可するメソッド一覧
         * @param resp レスポンス
         * @param cors 付加するCORS
         * @return レスポンス
         */
        private Response addCORSHeaders(final Map<String, String> queryHeaders, final Response resp, final String cors) {
            resp.addHeader("Access-Control-Allow-Origin", cors);

            // クロスドメインでアクセスする際にプリフライトリクエストで使用するHTTPヘッダを送信される。
            // このヘッダを受けた場合、Access-Control-Allow-Headersでそれらを許可する必要がある。
            // また、X-Requested-WithというヘッダーでXMLHttpRequestが使えるかの問い合わせがくる場合が
            // あるため、XMLHttpRequestを許可しておく。
            String requestHeaders = queryHeaders.get("access-control-request-headers");
            if (requestHeaders != null) {
                requestHeaders = "XMLHttpRequest, " + requestHeaders;
            } else {
                requestHeaders = "XMLHttpRequest";
            }

            resp.addHeader("Access-Control-Allow-Headers", requestHeaders);
            return resp;
        }
    }

    /**
     * Webサーバの設定クラス.
     */
    private static class Config {
        /**
         * ホスト名.
         */
        private String mHost;

        /**
         * ポート番号.
         */
        private int mPort;

        /**
         * SSL.
         */
        private boolean mSSL;

        /**
         * CORS で許可するオリジン.
         */
        private String mCors;

        /**
         * ドキュメントルート
         */
        private List<String> mDocRootList = new ArrayList<>();

        /**
         * ETag に使用するバージョン名.
         */
        private String mVersion;

        /**
         * コンテキスト.
         */
        private Context mContext;
        /**
         * SSLサーバーソケットファクトリ.
         */
        private SSLServerSocketFactory mServerSocketFactory;
    }

    /**
     * {@link DConnectWebServerNanoHttpd} 作成用ビルダークラス.
     */
    public static class Builder {
        /**
         * Webサーバの設定.
         */
        private Config mConfig = new Config();

        /**
         * コンテキストを設定します.
         *
         * @param context コンテキスト
         * @return Builder
         */
        public Builder context(final Context context) {
            mConfig.mContext = context;
            return this;
        }

        /**
         * ホスト名を設定します.
         *
         * @param host ホスト名
         * @return Builder
         */
        public Builder host(final String host) {
            mConfig.mHost = host;
            return this;
        }

        /**
         * ポート番号を設定します.
         *
         * @param port ポート番号
         * @return Builder
         */
        public Builder port(final int port) {
            mConfig.mPort = port;
            return this;
        }
        /**
         * SSL 有効化を設定します.
         *
         * @param ssl SSLを有効にする場合はtrue、それ以外はfalse
         * @return Builder
         */
        public Builder ssl(final boolean ssl) {
            mConfig.mSSL = ssl;
            return this;
        }
        /**
         * CORS の許可するオリジンを設定します.
         *
         * @param cors 許可するオリジン
         * @return Builder
         */
        public Builder cors(final String cors) {
            mConfig.mCors = cors;
            return this;
        }

        /**
         * バージョン名を設定します.
         * <p>
         * ETag の生成に使用します。
         * バージョンが上がったときに更新するようにします。
         * </p>
         * @param version バージョン名
         * @return Builder
         */
        public Builder version(final String version) {
            mConfig.mVersion = version;
            return this;
        }

        /**
         * ドキュメントルートを追加します.
         *
         * @param path ドキュメントルートへのパス
         * @return Builder
         */
        public Builder addDocumentRoot(final String path) {
            mConfig.mDocRootList.add(path);
            return this;
        }

        /**
         * SSL用の ServerSocket を作成するファクトリークラスを設定します.
         *
         * @param factory SSL用の ServerSocket を作成するファクトリークラス
         * @return Builder
         */
        public Builder serverSocketFactory(SSLServerSocketFactory factory) {
            mConfig.mServerSocketFactory = factory;
            return this;
        }

        /**
         * {@link DConnectWebServerNanoHttpd} のインスタンスを作成します.
         *
         * @return {@link DConnectWebServerNanoHttpd}
         */
        public DConnectWebServerNanoHttpd build() {
            return new DConnectWebServerNanoHttpd(mConfig);
        }
    }
}

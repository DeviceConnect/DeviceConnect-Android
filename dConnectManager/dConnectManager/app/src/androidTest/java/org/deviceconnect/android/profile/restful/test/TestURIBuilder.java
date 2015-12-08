/*
 TestURIBuilder.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.restful.test;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import org.deviceconnect.utils.URIBuilder;

/**
 * テスト用URIBuilder.
 * @author NTT DOCOMO, INC.
 */
public final class TestURIBuilder extends URIBuilder {

    /**
     * ポート番号: {@value} .
     */
    private static final int PORT = 4035;

    /**
     * コンストラクタ.
     */
    private TestURIBuilder() {
        super();
        setScheme("http");
        setHost("localhost");
        setPort(PORT);
    }

    /**
     * コンストラクタ.
     * @param uri URI
     */
    private TestURIBuilder(final URI uri) {
        super();
        setScheme(uri.getScheme());
        setHost(uri.getHost());
        setPort(uri.getPort());
        setPath(uri.getPath());

        try {
            String query = uri.getQuery();
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] splitted = param.split("=");
                    if (splitted != null && splitted.length == 2) {
                        addParameter(splitted[0], URLEncoder.encode(splitted[1], "UTF-8"));
                    } else {
                        addParameter(splitted[0], "");
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            // ここに入った場合はテストバグ
            throw new RuntimeException("Please check the specifined URI.");
        }
    }

    /**
     * テスト用URIBuilderを生成する.
     * 
     * @return テスト用URIBuilder
     */
    public static URIBuilder createURIBuilder() {
        return new TestURIBuilder();
    }

    /**
     * テスト用URIBuilderを生成する.
     * 
     * @param uri URI
     * @return テスト用URIBuilder
     */
    public static URIBuilder createURIBuilder(final URI uri) {
        return new TestURIBuilder(uri);
    }

}

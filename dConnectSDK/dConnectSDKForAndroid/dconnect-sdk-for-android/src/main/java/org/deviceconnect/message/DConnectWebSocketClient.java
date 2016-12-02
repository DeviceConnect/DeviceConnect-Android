/*
 DConnectWebSocketClient.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.message;

import java.util.HashMap;
import java.util.Map;

class DConnectWebSocketClient {

    private Map<String, HttpDConnectSDK.OnEventListener> mListenerMap = new HashMap<>();

    private HttpDConnectSDK.OnWebSocketListener mOnWebSocketListener;

    void setOnWebSocketListener(HttpDConnectSDK.OnWebSocketListener onWebSocketListener) {
        mOnWebSocketListener = onWebSocketListener;
    }

    void connect() {

    }

    void close() {

    }

    void addEventListener(String uri, final HttpDConnectSDK.OnEventListener listener) {
        mListenerMap.put(uri, listener);
    }

    void removeEventListener(String uri) {
        mListenerMap.remove(uri);
    }
}

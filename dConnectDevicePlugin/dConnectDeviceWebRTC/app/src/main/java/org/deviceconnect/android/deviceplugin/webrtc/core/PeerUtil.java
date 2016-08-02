/*
 PeerUtil.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.core;

import android.os.Build;

import org.deviceconnect.android.deviceplugin.webrtc.service.WebRTCService;
import org.json.JSONObject;
import org.webrtc.PeerConnection;

import java.io.File;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

/**
 *
 * @author NTT DOCOMO, INC.
 */
public final class PeerUtil {

    private static final String TOKEN_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * Defines the scheme of HTTP.
     */
    public static final String SCHEME_HTTP = "http";

    /**
     * Defines the scheme of HTTPS.
     */
    public static final String SCHEME_HTTPS = "https";

    /**
     * Defines the scheme of STUN.
     */
    public static final String SCHEME_STUN = "stun";

    /**
     * Defines the scheme of TURN.
     */
    public static final String SCHEME_TURN = "turn";

    /**
     * Defines the scheme of TURN-UDP.
     */
    public static final String SCHEME_TURN_UDP = "turn-udp";

    /**
     * Defines the scheme of TURN-TCP.
     */
    public static final String SCHEME_TURN_TCP = "turn-tcp";

    /**
     * Defines the scheme of TURNS.
     */
    public static final String SCHEME_TURNS = "turns";

    /**
     * Defines the name of SkyWay host.
     */
    public static final String SKYWAY_HOST = "skyway.io";

    /**
     * Defines the number of SkyWay port.
     */
    public static final int SKYWAY_PORT = 443;

    /**
     * Defines the name of stun host.
     */
    public static final String SKYWAY_STUN_HOST = "stun.skyway.io";

    /**
     * Defines the number of stun port.
     */
    public static final int SKYWAY_STUN_PORT = 3478;
    public static final String SKYWAY_TURN_HOST = "turn.skyway.io";
    public static final int SKYWAY_TURN_PORT = 3478;
    public static final int SKYWAY_TURNS_PORT = 443;

    /**
     * Defines the name of peerjs host.
     */
    public static final String PEERJS_HOST = "0.peerjs.com";

    /**
     * Defines the number of peerjs port.
     */
    public static final int PEERJS_PORT = 9000;
    public static final String PEERJS_STUN_HOST = "stun.l.google.com";
    public static final int PEERJS_STUN_PORT = 19302;

    /**
     * Defines the user-agent.
     */
    public static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.76 Safari/537.36";

    private PeerUtil() {
    }

    /**
     * Creates the token.
     * @param length number of the token
     * @return token
     */
    public static String randomToken(final int length) {
        int letterLen = TOKEN_STRING.length();
        StringBuilder builder = new StringBuilder();
        Random rand = new Random(System.currentTimeMillis());
        for (int i = 0; i < length; i++) {
            builder.append(TOKEN_STRING.charAt(rand.nextInt(letterLen)));
        }
        return builder.toString();
    }

    /**
     * Gets the ts value.
     * @return ts value
     */
    public static String getTSValue() {
        Calendar calSince = Calendar.getInstance(TimeZone.getDefault());
        calSince.clear();
        calSince.set(1970, 0, 1);
        long sinceValue = calSince.getTimeInMillis() / 1000L;
        Calendar calNow = Calendar.getInstance(TimeZone.getDefault());
        long nowValue = calNow.getTimeInMillis() / 1000L;
        long value = nowValue - sinceValue;
        String timeValue = String.format("%d", value);
        double randomValue = Math.random();
        String strRandomValue = String.format("%.16f", randomValue);
        return String.format("ts=%s%s", timeValue, strRandomValue);
    }

    /**
     * Checks whether device supports hardware codec.
     * @return true if device supports hardware codec, false otherwise
     */
    public static boolean validateHWCodec() {
        boolean validate = true;
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String match = String.format("%s,%s", manufacturer, model);
        String[] list = {
                "LGE", "Nexus 5"
        };
        for (String item : list) {
            if (match.equalsIgnoreCase(item)) {
                validate = false;
                break;
            }
        }
        return validate;
    }

    /**
     * Gets the list os IceServer.
     * @return the list of IceServer
     */
    public static List<PeerConnection.IceServer> getSkyWayIceServer() {
        LinkedList<PeerConnection.IceServer> list = new LinkedList<>();
        StringBuilder build = new StringBuilder();
        build.append(SCHEME_STUN);
        build.append(File.pathSeparator);
        build.append(SKYWAY_STUN_HOST);
        build.append(File.pathSeparator);
        build.append(SKYWAY_STUN_PORT);
        String uri = build.toString();
        PeerConnection.IceServer stun = new PeerConnection.IceServer(uri);
        list.add(stun);
        return list;
    }

    /**
     * Gets the text from the json.
     * <p>
     * If key does not exists in json, return defaultValue.
     * </p>
     *
     * @param json json
     * @param key key
     * @param defaultValue default value
     * @return text
     */
    public static String getJSONString(final JSONObject json, final String key, final String defaultValue) {
        if (json == null) {
            return defaultValue;
        }
        String value = json.optString(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Gets the service id by Peer.
     * @param peer peer
     * @return service id
     */
    public static String getServiceId(final Peer peer) {
        return WebRTCService.PLUGIN_ID + peer.getMyAddressId();
    }
}

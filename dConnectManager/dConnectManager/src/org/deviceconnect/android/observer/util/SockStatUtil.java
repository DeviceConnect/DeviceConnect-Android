/*
 SockStatUtil.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.observer.util;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Androidが使用しているSocketの状態を持つファイルを解析するユーティリティクラス.
 *
 * @author NTT DOCOMO, INC.
 *
 */
public final class SockStatUtil {
    /**
     * Sockstatファイルのローカルポート.
     */
    private static final int LOCAL_IP_PORT = 1;
    /**
     * Sockstatファイルのリモートポート.
     */
    private static final int REMOTE_IP_PORT = 2;
    /**
     * Sockstatファイルの接続状況.
     */
    private static final int CONNECTION_STATE = 3;
    /**
     * SockstatファイルのUID.
     */
    private static final int UID = 7;
    /**
     * 16進数.
     */
    private static final int HEX = 16;
    /**
     * IPの16進数の文字列長.
     */
    private static final int IP_HEX_LENGTH = 8;
    /**
     * IPの10進数の文字列長.
     */
    private static final int IP_DECIMAL_LENGTH = 4;
    /**
     * コンストラクタ.
     */
    private SockStatUtil() {
    }

    /**
     * SockstatファイルのSocketのリストを取得する.
     * @param context context
     * @return socket list
     */
    public static ArrayList<AndroidSocket> getSocketList(final Context context) {
        ArrayList<AndroidSocket> allSocketlist = new ArrayList<AndroidSocket>();
        if (context == null) {
            return allSocketlist;
        }
        allSocketlist.addAll(parseProcNetFile("/proc/net/tcp6", SocketType.TCP));
        allSocketlist.addAll(parseProcNetFile("/proc/net/tcp", SocketType.TCP));
        allSocketlist.addAll(parseProcNetFile("/proc/net/udp6", SocketType.UDP));
        allSocketlist.addAll(parseProcNetFile("/proc/net/udp", SocketType.UDP));
        processAppInfo(context, allSocketlist);
        return allSocketlist;
    }

    /**
     * 指定されたSockstatファイルの解析を行う.
     * @param procFileName 解析するファイル名
     * @param socketType 解析するSocketのタイプ
     * @return Socketリスト
     */
    private static ArrayList<AndroidSocket> parseProcNetFile(final String procFileName, final SocketType socketType) {
        Scanner scanner;
        ArrayList<AndroidSocket> sockets = new ArrayList<AndroidSocket>();
        if (procFileName == null) {
            return sockets;
        }
        File file = new File(procFileName);
        StringTokenizer stringtokenizer;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException filenotfoundexception) {
            filenotfoundexception.printStackTrace();
            return sockets;
        }
        scanner.nextLine();
        while (scanner.hasNext()) {
            AndroidSocket aSocket = new AndroidSocket();
            aSocket.setType(socketType);
            String nextLine = scanner.nextLine();
            stringtokenizer = new StringTokenizer(nextLine);
            int tokenNo = 0;
            do {
                String token = stringtokenizer.nextToken();
                switch (tokenNo) {
                    case LOCAL_IP_PORT:  //Analysis of Local IP and Port number
                        String[] localAddress = token.split(":");
                        aSocket.setLocalAddress(convertIPtoString(localAddress[0]));

                        aSocket.setLocalPort(Integer.parseInt(localAddress[1], HEX));
                        break;
                    case REMOTE_IP_PORT: //Analysis of Remote IP and Port number
                        String[] remoteAddress = token.split(":");
                        aSocket.setRemoteAddress(convertIPtoString(remoteAddress[0]));
                        aSocket.setRemotePort(Integer.parseInt(remoteAddress[1], HEX));
                        break;
                    case CONNECTION_STATE: //Connection status
                        if (socketType == SocketType.TCP) {
                            int l = Integer.parseInt(token, HEX);
                            if (l > SocketState.values().length || l < 0) {
                                l = 0;
                            }
                            aSocket.setState(SocketState.values()[l]);
                        } else {
                            aSocket.setState(SocketState.UDP_LISTEN);
                        }
                        break;
                    case UID: //Uid of occupancy to the program
                        aSocket.setUid(Integer.parseInt(token));
                        break;
                    default:
                }
                tokenNo++;
            } while (stringtokenizer.hasMoreElements());
            sockets.add(aSocket);
        }
        scanner.close();
        return sockets;
    }
    /**
     * IP アドレスの解析.
     * @param ipString 16進数文字列のIPアドレス
     * @return IPv4のIPアドレス
     */
    private static String convertIPtoString(final String ipString) {
        String s1 = "";
        if (ipString == null || ipString.length() < IP_HEX_LENGTH) {
            return s1;
        }
        int i = ipString.length();
        int j = 0;
        do {
            StringBuilder stringbuilder;
            String s2;
            stringbuilder = (new StringBuilder(String.valueOf(s1)))
                    .append(Integer.parseInt(ipString.substring(i - 2, i), HEX));
            if (j < IP_DECIMAL_LENGTH - 1) {
                s2 = ".";
            } else {
                s2 = "";
            }
            s1 = stringbuilder.append(s2).toString();
            i -= 2;
        } while (i >= 2 && ++j <= IP_DECIMAL_LENGTH);
        return s1;
    }
    /**
     * Socksocketの解析結果からアプリのパッケージ名を取得する.
     * @param context context
     * @param arraylist socketリスト
     */
    private static void processAppInfo(final Context context, final ArrayList<AndroidSocket> arraylist) {
        if (arraylist == null) {
            return;
        }
        Iterator<AndroidSocket> iterator = arraylist.iterator();
        while (iterator.hasNext()) {
            AndroidSocket androidsocket = iterator.next();
            String[] as = context.getPackageManager().getPackagesForUid(androidsocket.getUid());
            if (as != null) {
                androidsocket.setAppName(as[0]);
            } else {
                androidsocket.setAppName("unknown");
            }
        }
    }
}

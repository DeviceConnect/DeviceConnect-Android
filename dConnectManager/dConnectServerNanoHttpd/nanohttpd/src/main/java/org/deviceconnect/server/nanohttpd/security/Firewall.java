/*
 Firewall.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.server.nanohttpd.security;

import org.deviceconnect.server.nanohttpd.BuildConfig;
import org.deviceconnect.server.nanohttpd.logger.AndroidHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * ファイアウォール.
 * 
 * @author NTT DOCOMO, INC.
 */
public final class Firewall {

    /** ログ用タグ. */
    private static final String TAG = "Firewall";

    /** IPの許可リスト. */
    private List<String> mIPAllowList;

    /** ロガー. */
    private final Logger mLogger = Logger.getLogger("dconnect.server");

    /**
     * ファイアウォールを生成する.
     */
    public Firewall() {
        this(null);
    }

    /**
     * 接続元のIP制限リストを指定してファイアウォールを生成する.
     * 
     * @param ipList IPの許可リスト。
     */
    public Firewall(final List<String> ipList) {
        mIPAllowList = ipList;
        if (BuildConfig.DEBUG) {
            Handler handler = new AndroidHandler(TAG);
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);
            mLogger.addHandler(handler);
            mLogger.setLevel(Level.WARNING);
        }
    }

    /**
     * 指定されたIPが許可リストに含まれるかを調査する.
     * 
     * @param ip 調査対象となるIPアドレス。("127.0.0.1"などの文字列。)
     * @return 許可リストに含まれる場合true、その他はfalseを返す。
     */
    public boolean isAllowIP(final String ip) {
        if (mIPAllowList == null || mIPAllowList.size() == 0) {
            // IPのリストがない場合は制限なしと判断し、すべてtrueにする。
            return true;
        }

        // TODO 必要ならば後々ワイルドカードや正規表現を利用できるようにする。
        // 192.168.0.*など
        for (String allow : mIPAllowList) {
            if (allow.equals(ip)) {
                return true;
            }
        }

        mLogger.warning("Firewall#isAllowIP(). Not allowed IP : " + ip);
        return false;
    }

    /**
     * IPの許可リストを設定する.
     * 
     * @param ipList IPの許可リスト。
     */
    public void setIPAllowList(final ArrayList<String> ipList) {
        mIPAllowList = ipList;
    }

}

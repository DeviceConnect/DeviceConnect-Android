/*
 ShowMenuFragment.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.app.simplebot.fragment;

/**
 * メニュー表示用のインターフェイス
 */
public interface ShowMenuFragment {

    /**
     * メニューを表示する
     */
    void showMenu();

    /**
     * Fragmentが可視状態かを返す
     * @return trueで可視状態
     */
    boolean isVisible();
}

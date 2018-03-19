/*
 IlluminationData.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking.data;

import org.deviceconnect.android.deviceplugin.linking.util.ByteUtil;

public class IlluminationData {

    private static final byte[] HEADER = {
            (byte) 0xB1, (byte) 0x04, (byte) 0x00, (byte) 0x00
    };

    private byte[] mSource;

    // ヘッダ。固定値(0xB1,0x04,0x00,0x00)
    private byte[] mHeader = new byte[4];
    // LED項目ID。固定値(0x10)
    private byte mLedId;
    // LED子項目数。固定値(0x02)
    private int mLedChildCount;
    // LEDデフォルト設定ID。子項目の中でのデフォルト設定IDを示す。
    private byte mLedDefaultSettingId;
    // 項目名言語数。"1"固定。
    private int mLedNameLangCount;
    private Name[] mLedNames;
    private Setting mColor;
    private Setting mPattern;

    public IlluminationData(final byte[] illuminance) {
        mSource = illuminance;

        LinkingScanner scanner = new LinkingScanner(illuminance);

        mHeader[0] = scanner.readByte();
        mHeader[1] = scanner.readByte();
        mHeader[2] = scanner.readByte();
        mHeader[3] = scanner.readByte();
        for (int i = 0; i < HEADER.length; i++) {
            if (mHeader[i] != HEADER[i]) {
                throw new IllegalArgumentException("Header is invalid.");
            }
        }

        // LED項目ID
        mLedId = scanner.readByte();
        if (mLedId != 0x10) {
            throw new IllegalArgumentException("LED項目ID is invalid.");
        }

        // LED子項目数
        mLedChildCount = scanner.readByte();
        if (mLedChildCount != 0x02) {
            throw new IllegalArgumentException("LED子項目数 is invalid.");
        }

        // LEDデフォルト設定ID
        mLedDefaultSettingId = scanner.readByte();

        // LED項目名言語数
        mLedNameLangCount = scanner.readByte();

        // LED項目名
        mLedNames = new Name[mLedNameLangCount];
        for (int i = 0; i < mLedNameLangCount; i++) {
            mLedNames[i] = scanner.readName();
        }

        // LEDパターン
        mPattern = scanner.readSetting();

        // LED色
        mColor = scanner.readSetting();
    }

    public Setting getColor() {
        return mColor;
    }

    public Setting getPattern() {
        return mPattern;
    }

    @Override
    public String toString() {
        return new StringBuilder("{mHeader: ")
                .append(ByteUtil.binaryToHex(mHeader))
                .append(", mLedId: ")
                .append(ByteUtil.byteToHex(mLedId))
                .append(", mLedChildCount: ")
                .append(mLedChildCount)
                .append(", mLedDefaultSettingId: ")
                .append(ByteUtil.byteToHex(mLedDefaultSettingId))
                .append(", mLedNameLangCount: ")
                .append(mLedNameLangCount)
                .append(", mLedNames: ")
                .append(toString(mLedNames))
                .append(", mPattern: ")
                .append(mPattern.toString())
                .append(", mColor: ")
                .append(mColor.toString())
                .append("}")
                .toString();
    }

    private String toString(final Object[] list) {
        if (list == null) {
            return "";
        }
        StringBuilder s = new StringBuilder();
        s.append("[");
        for (Object obj : list) {
            s.append(obj.toString()).append(" ");
        }
        s.append("]");
        return s.toString();
    }
}

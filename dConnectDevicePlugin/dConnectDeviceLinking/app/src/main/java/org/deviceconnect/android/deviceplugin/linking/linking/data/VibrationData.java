/*
 VibrationData.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking.data;

import org.deviceconnect.android.deviceplugin.linking.util.ByteUtil;

public class VibrationData {

    private static final byte[] HEADER = {
            (byte) 0xB1, (byte) 0x03, (byte) 0x00, (byte) 0x00
    };

    private byte[] mSource;

    // ヘッダ。固定値(0xB1,0x03,0x00,0x00)
    private byte[] mHeader = new byte[4];
    // バイブレーション項目ID。固定値(0x10)
    private byte mVibrationId;
    // バイブレーションパターン選択数
    private int mVibrationChildCount;
    // バイブレーションデフォルト設定ID。子項目の中でのデフォルト設定IDを示す。
    private byte mVibrationDefaultSettingId;
    // 項目名言語数。"2"固定。
    private int mVibrationNameLangCount;
    private Name[] mVibrationNames;
    private Setting mPattern;

    public VibrationData(final byte[] vibration) {
        mSource = vibration;

        LinkingScanner scanner = new LinkingScanner(vibration);

        mHeader[0] = scanner.readByte();
        mHeader[1] = scanner.readByte();
        mHeader[2] = scanner.readByte();
        mHeader[3] = scanner.readByte();

        for (int i = 0; i < HEADER.length; i++) {
            if (mHeader[i] != HEADER[i]) {
                throw new IllegalArgumentException("Header is invalid.");
            }
        }

        // バイブレーション項目ID
        mVibrationId = scanner.readByte();
        if (mVibrationId != 0x10) {
            throw new IllegalArgumentException("バイブレーション項目ID is invalid.");
        }

        // バイブレーションの項目数
        mVibrationChildCount = scanner.readByte();

        // バイブレーションのデフォルト設定ID
        mVibrationDefaultSettingId = scanner.readByte();

        // バイブレーションの項目名言語数
        mVibrationNameLangCount = scanner.readByte();

        // バイブレーションの項目名
        mVibrationNames = new Name[mVibrationNameLangCount];
        for (int i = 0; i < mVibrationNameLangCount; i++) {
            mVibrationNames[i] = scanner.readName();
        }

        // バイブレーション詳細情報
        mPattern = new Setting();
        mPattern.mChildCount = mVibrationChildCount;
        mPattern.mChildren = new Setting[mVibrationChildCount];
        for (int i = 0; i < mVibrationChildCount; i++) {
            mPattern.mChildren[i] = scanner.readDetail();
        }
    }

    public Setting getPattern() {
        return mPattern;
    }

    @Override
    public String toString() {
        return new StringBuilder("{mHeader: ")
                .append(ByteUtil.binaryToHex(mHeader))
                .append(", mVibrationId: ")
                .append(ByteUtil.byteToHex(mVibrationId))
                .append(", mVibrationChildCount: ")
                .append(mVibrationChildCount)
                .append(", mVibrationDefaultSettingId: ")
                .append(ByteUtil.byteToHex(mVibrationDefaultSettingId))
                .append(", mVibrationNameLangCount: ")
                .append(mVibrationNameLangCount)
                .append(", mVibrationNames: ")
                .append(toString(mVibrationNames))
                .append(", pattern: ")
                .append(mPattern.toString())
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

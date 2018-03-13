/*
 IlluminationData.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking.data;

import org.deviceconnect.android.deviceplugin.linking.util.ByteUtil;

class LinkingScanner {
    private byte[] mSource;
    private int mOffset;

    LinkingScanner(byte[] source) {
        mSource = source;
    }

    byte readByte() {
        if (mOffset >= mSource.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return (byte) (mSource[mOffset++] & 0xFF);
    }

    void read(byte[] buf) {
        read(buf, 0, buf.length);
    }

    void read(byte[] buf, int offset, int size) {
        for (int i = 0; i < size; i++) {
            buf[i + offset] = readByte();
        }
    }

    Name readName() {
        Name name = new Name();
        byte[] tmp = new byte[2];
        tmp[0] = readByte();
        tmp[1] = readByte();
        name.mNameLang = ByteUtil.binaryToString(tmp);

        int size = readByte();
        tmp = new byte[size];
        read(tmp);
        name.mSize = size;
        name.mName = ByteUtil.binaryToString(tmp);
        return name;
    }

    Setting readSetting() {
        Setting setting = new Setting();
        setting.mId = readByte();
        setting.mChildCount = readByte();
        setting.mDefaultSettingId = readByte();

        setting.mNameLangCount = readByte();
        setting.mNames = new Name[setting.mNameLangCount];
        for (int i = 0; i < setting.mNameLangCount; i++) {
            setting.mNames[i] = readName();
        }

        setting.mChildren = new Setting[setting.mChildCount];
        for (int i = 0; i < setting.mChildCount; i++) {
            setting.mChildren[i] = readDetail();
        }

        return setting;
    }

    // 詳細設定項目フォーマット
    Setting readDetail() {
        Setting setting = new Setting();
        setting.mId = readByte();
        setting.mNameLangCount = readByte();
        setting.mNames = new Name[setting.mNameLangCount];
        for (int i = 0; i < setting.mNameLangCount; i++) {
            setting.mNames[i] = readName();
        }
        return setting;
    }
}

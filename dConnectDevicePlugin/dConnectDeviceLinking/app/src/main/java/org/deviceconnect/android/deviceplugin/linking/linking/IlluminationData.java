/*
 IlluminationData.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import org.deviceconnect.android.deviceplugin.linking.util.ByteUtil;

public class IlluminationData {

    private static final byte[] HEADER = {
            (byte) 0xB1, (byte) 0x04, (byte) 0x00, (byte) 0x00
    };

    private byte[] mSource;
    private byte[] mHeader = new byte[4];//ヘッダ。固定値(0xB1,0x04,0x00,0x00)

    private byte mLedId;//LED項目ID。固定値(0x10)
    private int mLedChildCount;//LED子項目数。固定値(0x02)
    private byte mLedDefaultSettingId;//LEDデフォルト設定ID。子項目の中でのデフォルト設定IDを示す。
    private int mLedNameLangCount;//項目名言語数。"1"固定。
    private Name[] mLedNames;
    private Setting mColor;
    private Setting mPattern;

    public Setting getColor() {
        return mColor;
    }

    public Setting getPattern() {
        return mPattern;
    }

    public class Setting {
        private byte mId;//ID
        private int mChildCount;//子項目数
        private byte mDefaultSettingId;//デフォルト設定ID
        private int mNameLangCount;//項目名言語数
        private Name[] mNames;
        private Setting[] mChildren;

        public byte getId() {
            return mId;
        }

        public int getChildCount() {
            return mChildCount;
        }

        public byte getDefaultSettingId() {
            return mDefaultSettingId;
        }

        public int getNameLangCount() {
            return mNameLangCount;
        }

        public Name getName(int index) {
            return mNames[index];
        }

        public Name[] getNames() {
            return mNames;
        }

        public Setting getChild(int index) {
            return mChildren[index];
        }

        public Setting[] getChildren() {
            return mChildren;
        }

        @Override
        public String toString() {
            return Setting.class.getSimpleName() + ":{mId:" + ByteUtil.byteToHex(mId) + ", mChildCount:" + mChildCount + ", mDefaultSettingId:" + ByteUtil.byteToHex(mDefaultSettingId) + "," +
                    " mNameLangCount:" + mNameLangCount + ", mNames:" + toString(mNames) + ", mChildren:" + toString(mChildren) + "}";
        }

        private String toString(Object[] list) {
            if (list == null) {
                return "";
            }
            String s = "[";
            for (Object obj : list) {
                s += obj.toString() + " ";
            }
            s += "]";
            return s;
        }
    }

    public class Name {
        private String mNameLang;//言語。"ja"固定。
        private int mSize;//名前のサイズ。
        private String mName;//項目名(UTF-8)。

        public String getNameLang() {
            return mNameLang;
        }

        public int getSize() {
            return mSize;
        }

        public String getName() {
            return mName;
        }

        @Override
        public String toString() {
            return Name.class.getSimpleName() + ":{mNameLang:" + mNameLang + ", mSize:" + mSize + ", mName:" + mName + "}";
        }
    }

    public IlluminationData(final byte[] illuminance) {
        mSource = illuminance;

        int index = 0;
        mHeader[0] = illuminance[index++];
        mHeader[1] = illuminance[index++];
        mHeader[2] = illuminance[index++];
        mHeader[3] = illuminance[index++];
        for (int i = 0; i < HEADER.length; i++) {
            if (mHeader[i] != HEADER[i]) {
                throw new IllegalArgumentException("Header is invalid.");
            }
        }

        mLedId = illuminance[index++];
        if (mLedId != 0x10) {
            throw new IllegalArgumentException("LED項目ID is invalid.");
        }

        mLedChildCount = illuminance[index++];
        if (mLedChildCount != 0x02) {
            throw new IllegalArgumentException("LED子項目数 is invalid.");
        }

        mLedDefaultSettingId = illuminance[index++];
        mLedNameLangCount = illuminance[index++] & 0xFF;

        mLedNames = new Name[mLedNameLangCount];
        index = extractName(illuminance, index, mLedNameLangCount, mLedNames);

        Setting pattern = new Setting();
        index = makeSetting(pattern, illuminance, index, false);
        mPattern = pattern;

        Setting color = new Setting();
        makeSetting(color, illuminance, index, false);
        mColor = color;
    }

    private int makeSetting(Setting setting, byte[] source, int offset, boolean isChild) {
        setting.mId = source[offset++];
        setting.mChildCount = source[offset++] & 0xFF;
        if (!isChild) {
            setting.mDefaultSettingId = source[offset++];
        }
        setting.mNameLangCount = source[offset++] & 0xFF;
        setting.mNames = new Name[setting.mNameLangCount];
        offset = extractName(source, offset, setting.mNameLangCount, setting.mNames);

        if (setting.mChildCount > 0) {
            setting.mChildren = new Setting[setting.mChildCount];
            for (int i = 0; i < setting.mChildCount; i++) {
                Setting child = new Setting();
                offset = makeSetting(child, source, offset, true);
                setting.mChildren[i] = child;
            }
        }
        return offset;
    }

    private int extractName(byte[] source, int offset, int count, Name[] names) {
        for (int i = 0; i < count; i++) {
            names[i] = new Name();
            byte[] tmp = new byte[2];
            tmp[0] = source[offset++];
            tmp[1] = source[offset++];
            names[i].mNameLang = ByteUtil.binaryToString(tmp);
            int size = source[offset++] & 0xFF;
            names[i].mSize = size;
            tmp = new byte[size];
            for (int j = 0; j < size; j++) {
                tmp[j] = source[offset++];
            }
            names[i].mName = ByteUtil.binaryToString(tmp);
        }
        return offset;
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
        String s = "[";
        for (Object obj : list) {
            s += obj.toString() + " ";
        }
        s += "]";
        return s;
    }
}

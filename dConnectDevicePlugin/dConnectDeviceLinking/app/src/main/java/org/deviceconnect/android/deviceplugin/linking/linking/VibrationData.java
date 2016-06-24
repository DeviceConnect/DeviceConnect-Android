/*
 VibrationData.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import org.deviceconnect.android.deviceplugin.linking.util.ByteUtil;

public class VibrationData {

    private byte[] mSource;
    private byte[] mHeader = new byte[4];//ヘッダ。固定値(0xB1,0x03,0x00,0x00)

    private byte mVibrationId;//バイブレーション項目ID。固定値(0x10)
    private int mVibrationChildCount;//バイブレーションパターン選択数
    private byte mVibrationDefaultSettingId;//バイブレーションデフォルト設定ID。子項目の中でのデフォルト設定IDを示す。
    private int mVibrationNameLangCount;//項目名言語数。"2"固定。
    private Name[] mVibrationNames;
    private Setting mPattern;

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

    public VibrationData(final byte[] vibration) {
        mSource = vibration;

        int index = 0;
        mHeader[0] = vibration[index++];
        mHeader[1] = vibration[index++];
        mHeader[2] = vibration[index++];
        mHeader[3] = vibration[index++];
        mVibrationId = vibration[index++];
        mVibrationChildCount = vibration[index++];
        mVibrationDefaultSettingId = vibration[index++];
        mVibrationNameLangCount = vibration[index++] & 0xFF;

        mVibrationNames = new Name[mVibrationNameLangCount];
        index = extractName(vibration, index, mVibrationNameLangCount, mVibrationNames);

        Setting pattern = new Setting();

        if (mVibrationChildCount > 0) {
            pattern.mChildren = new Setting[mVibrationChildCount];
            for (int i = 0; i < mVibrationChildCount; i++) {
                Setting child = new Setting();
                index = makeSetting(child, mSource, index, true);
                pattern.mChildren[i] = child;
            }
        }

        this.mPattern = pattern;
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

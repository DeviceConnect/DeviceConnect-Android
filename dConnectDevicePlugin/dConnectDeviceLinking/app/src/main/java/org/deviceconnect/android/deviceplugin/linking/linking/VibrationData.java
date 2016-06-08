/*
 VibrationData.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import org.deviceconnect.android.deviceplugin.linking.util.ByteUtil;

public class VibrationData {

    byte[] source;
    byte[] header = new byte[4];//ヘッダ。固定値(0xB1,0x03,0x00,0x00)

    byte vibrationId;//バイブレーション項目ID。固定値(0x10)
    int vibrationChildCount;//バイブレーションパターン選択数
    byte vibrationDefaultSettingId;//バイブレーションデフォルト設定ID。子項目の中でのデフォルト設定IDを示す。
    int vibrationNameLangCount;//項目名言語数。"2"固定。
    Name[] vibrationNames;
    public Setting mPattern;

    public class Setting {
        public byte id;//ID
        public int childCount;//子項目数
        public byte defaultSettingId;//デフォルト設定ID
        public int nameLangCount;//項目名言語数
        public Name[] names;
        public Setting[] children;

        @Override
        public String toString() {
            return Setting.class.getSimpleName() + ":{id:" + ByteUtil.byteToHex(id) + ", childCount:" + childCount + ", defaultSettingId:" + ByteUtil.byteToHex(defaultSettingId) + "," +
                    " nameLangCount:" + nameLangCount + ", names:" + toString(names) + ", children:" + toString(children) + "}";
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
        public String nameLang;//言語。"ja"固定。
        public int size;//名前のサイズ。
        public String name;//項目名(UTF-8)。

        @Override
        public String toString() {
            return Name.class.getSimpleName() + ":{nameLang:" + nameLang + ", size:" + size + ", name:" + name + "}";
        }
    }

    public VibrationData(byte[] vibration) {
        source = vibration;
        int index = 0;
        header[0] = vibration[index++];
        header[1] = vibration[index++];
        header[2] = vibration[index++];
        header[3] = vibration[index++];
        vibrationId = vibration[index++];
        vibrationChildCount = vibration[index++];
        vibrationDefaultSettingId = vibration[index++];
        vibrationNameLangCount = vibration[index++] & 0xFF;

        vibrationNames = new Name[vibrationNameLangCount];
        index = extractName(vibration, index, vibrationNameLangCount, vibrationNames);

        Setting pattern = new Setting();

        if (vibrationChildCount > 0) {
            pattern.children = new Setting[vibrationChildCount];
            for (int i = 0; i < vibrationChildCount; i++) {
                Setting child = new Setting();
                index = makeSetting(child, source, index, true);
                pattern.children[i] = child;
            }
        }

        this.mPattern = pattern;
    }

    private int makeSetting(Setting setting, byte[] source, int offset, boolean isChild) {
        setting.id = source[offset++];
        setting.childCount = source[offset++] & 0xFF;
        if (!isChild) {
            setting.defaultSettingId = source[offset++];
        }
        setting.nameLangCount = source[offset++] & 0xFF;
        setting.names = new Name[setting.nameLangCount];
        offset = extractName(source, offset, setting.nameLangCount, setting.names);
        return offset;
    }

    private int extractName(byte[] source, int offset, int count, Name[] names) {
        for (int i = 0; i < count; i++) {
            names[i] = new Name();
            byte[] tmp = new byte[2];
            tmp[0] = source[offset++];
            tmp[1] = source[offset++];
            names[i].nameLang = ByteUtil.binaryToString(tmp);
            int size = source[offset++] & 0xFF;
            names[i].size = size;
            tmp = new byte[size];
            for (int j = 0; j < size; j++) {
                tmp[j] = source[offset++];
            }
            names[i].name = ByteUtil.binaryToString(tmp);
        }
        return offset;
    }

    @Override
    public String toString() {
        return new StringBuilder("{header: ")
                .append(ByteUtil.binaryToHex(header))
                .append(", vibrationId: ")
                .append(ByteUtil.byteToHex(vibrationId))
                .append(", vibrationChildCount: ")
                .append(vibrationChildCount)
                .append(", vibrationDefaultSettingId: ")
                .append(ByteUtil.byteToHex(vibrationDefaultSettingId))
                .append(", vibrationNameLangCount: ")
                .append(vibrationNameLangCount)
                .append(", vibrationNames: ")
                .append(toString(vibrationNames))
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

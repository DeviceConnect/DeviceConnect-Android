/*
 IlluminationData.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import org.deviceconnect.android.deviceplugin.linking.util.ByteUtil;

public class IlluminationData {

    byte[] source;
    byte[] header = new byte[4];//ヘッダ。固定値(0xB4,0x04,0x00,0x00)

    byte ledId;//LED項目ID。固定値(0x10)
    int ledChildCount;//LED子項目数。固定値(0x02)
    byte ledDefaultSettingId;//LEDデフォルト設定ID。子項目の中でのデフォルト設定IDを示す。
    int ledNameLangCount;//項目名言語数。"1"固定。
    Name[] ledNames;
    public Setting mPattern;
    Setting mColor;

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

    public IlluminationData(byte[] illuminance) {
        source = illuminance;
        int index = 0;
        header[0] = illuminance[index++];
        header[1] = illuminance[index++];
        header[2] = illuminance[index++];
        header[3] = illuminance[index++];
        ledId = illuminance[index++];
        ledChildCount = illuminance[index++];
        ledDefaultSettingId = illuminance[index++];
        ledNameLangCount = illuminance[index++] & 0xFF;

        ledNames = new Name[ledNameLangCount];
        index = extractName(illuminance, index, ledNameLangCount, ledNames);

        Setting pattern = new Setting();
        index = makeSetting(pattern, illuminance, index, false);
        this.mPattern = pattern;

        Setting color = new Setting();
        makeSetting(color, illuminance, index, false);
        this.mColor = color;
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

        if (setting.childCount > 0) {
            setting.children = new Setting[setting.childCount];
            for (int i = 0; i < setting.childCount; i++) {
                Setting child = new Setting();
                offset = makeSetting(child, source, offset, true);
                setting.children[i] = child;
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
                .append(", ledId: ")
                .append(ByteUtil.byteToHex(ledId))
                .append(", ledChildCount: ")
                .append(ledChildCount)
                .append(", ledDefaultSettingId: ")
                .append(ByteUtil.byteToHex(ledDefaultSettingId))
                .append(", ledNameLangCount: ")
                .append(ledNameLangCount)
                .append(", ledNames: ")
                .append(toString(ledNames))
                .append(", pattern: ")
                .append(mPattern.toString())
                .append(", color: ")
                .append(mColor.toString())
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

/*
 KadecotGeneralLighting
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.kadecot.kadecotdevice;

import java.util.ArrayList;

/**
 * Kadecot General Lighting.
 *
 * @author NTT DOCOMO, INC.
 */
public class KadecotGeneralLighting {
    /** JSON string list. */
    ArrayList<KadecotJsonString> mJsonStringList = new ArrayList<>();

    /** Define : Get power status. */
    public static final int POWERSTATE_GET = 0x80800000;
    /** Define : Set power status on. */
    public static final int POWERSTATE_ON = 0x00800030;
    /** Define : Set power status off. */
    public static final int POWERSTATE_OFF = 0x00800031;

    /** Define : Get procedure. */
    public static final String PROC_GET = "get";
    /** Define : Set procedure. */
    public static final String PROC_SET = "set";

    /** Define : Operation status property. */
    public static final String PROP_OPERATIONSTATUS = "OperationStatus";

    /** Constructor. */
    public KadecotGeneralLighting() {
        mJsonStringList.add(new KadecotJsonString(POWERSTATE_GET, PROC_GET, PROP_OPERATIONSTATUS));
        mJsonStringList.add(new KadecotJsonString(POWERSTATE_ON, PROC_SET, PROP_OPERATIONSTATUS, 0x30));
        mJsonStringList.add(new KadecotJsonString(POWERSTATE_OFF, PROC_SET, PROP_OPERATIONSTATUS, 0x31));
    }

    /**
     * Get Object count.
     *
     * @return Object count.
     */
    public int getObjectCount() {
        if (mJsonStringList != null) {
            return mJsonStringList.size();
        } else {
            return 0;
        }
    }

    /**
     * Exchange JSON string.
     *
     * @param deviceId DeviceId.
     * @param index Index.
     * @return JSON string.
     */
    public String exchangeJsonString(final String deviceId, final int index) {
        for (int i = 0; i < mJsonStringList.size(); i++) {
            KadecotJsonString jsonStr = mJsonStringList.get(i);
            if (jsonStr.getIndex() == index) {
                return jsonStr.getJsonString(deviceId);
            }
        }
        return null;
    }

    /**
     * Exchange JSON string.
     *
     * @param deviceId DeviceId.
     * @param index Index.
     * @param value Value.
     * @return JSON string.
     */
    public String exchangeJsonString(final String deviceId, final int index, final int value) {
        for (int i = 0; i < mJsonStringList.size(); i++) {
            KadecotJsonString jsonStr = mJsonStringList.get(i);
            if (jsonStr.getIndex() == index) {
                return jsonStr.getJsonString(deviceId, value);
            }
        }
        return null;
    }
}

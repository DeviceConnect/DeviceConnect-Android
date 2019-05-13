/*
 AbstractSpec.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.models;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ベンダー拡張を格納するオブジェクト.
 *
 * @author NTT DOCOMO, INC.
 */
public abstract class AbstractSpec implements DConnectSpec {
    /**
     * ベンダー拡張.
     */
    private Map<String, Object> mVendorExtensions;

    /**
     * ベンダー拡張を取得します.
     *
     * @return ベンダー拡張
     */
    public Map<String, Object> getVendorExtensions() {
        return mVendorExtensions;
    }

    /**
     * ベンダー拡張を設定します.
     *
     * @param vendorExtensions ベンダー拡張
     */
    public void setVendorExtensions(Map<String, Object> vendorExtensions) {
        mVendorExtensions = vendorExtensions;
    }

    /**
     * ベンダー拡張をマップに追加します.
     *
     * @param key キー
     * @param value 値
     */
    public void addVendorExtension(String key, Object value) {
        if (mVendorExtensions == null) {
            mVendorExtensions = new HashMap<>();
        }
        mVendorExtensions.put(key,  value);
    }

    /**
     * ベンダー拡張をマップから削除します.
     *
     * @param key キー
     * @return 削除したオブジェクト
     */
    public Object removeVendorExtension(String key) {
        if (mVendorExtensions != null) {
            return mVendorExtensions.remove(key);
        }
        return null;
    }

    /**
     * Number の値を Bundle に格納します.
     *
     * @param bundle Number の値を格納する Bundle
     * @param key キー
     * @param number 格納する値
     */
    protected void copyNumber(Bundle bundle, String key, Number number) {
        if (number instanceof Byte) {
            bundle.putByte(key, number.byteValue());
        } else if (number instanceof Short) {
            bundle.putShort(key, number.shortValue());
        } else if (number instanceof Integer) {
            bundle.putInt(key, number.intValue());
        } else if (number instanceof Long) {
            bundle.putLong(key, number.longValue());
        } else if (number instanceof Float) {
            bundle.putFloat(key, number.floatValue());
        } else if (number instanceof Double) {
            bundle.putDouble(key, number.doubleValue());
        }
    }

    /**
     * Enumeration の値を Bundle に格納します.
     *
     * @param bundle Enumeration の値を格納する Bundle
     * @param type Enumerationのタイプ
     * @param format Enumerationのフォーマット
     * @param objects 値
     */
    protected void copyEnum(Bundle bundle, DataType type, DataFormat format, List<Object> objects) {
        switch (type) {
            case INTEGER:
                if (format == null || format == DataFormat.INT32) {
                    int[] enums = new int[objects.size()];
                    for (int i = 0; i < objects.size(); i++) {
                        if (objects.get(i) instanceof Number) {
                            enums[i] = ((Number) objects.get(i)).intValue();
                        } else if (objects.get(i) instanceof String) {
                            enums[i] = Integer.valueOf((String) objects.get(i));
                        }
                    }
                    bundle.putIntArray("enum", enums);
                } else if (format == DataFormat.INT64) {
                    long[] enums = new long[objects.size()];
                    for (int i = 0; i < objects.size(); i++) {
                        if (objects.get(i) instanceof Number) {
                            enums[i] = ((Number) objects.get(i)).longValue();
                        } else if (objects.get(i) instanceof String) {
                            enums[i] = Long.valueOf((String) objects.get(i));
                        }
                    }
                    bundle.putLongArray("enum", enums);
                } else {
                    // TODO フォーマットエラー
                }
                break;
            case NUMBER:
                if (format == null || format == DataFormat.FLOAT) {
                    float[] enums = new float[objects.size()];
                    for (int i = 0; i < objects.size(); i++) {
                        if (objects.get(i) instanceof Number) {
                            enums[i] = ((Number) objects.get(i)).floatValue();
                        } else if (objects.get(i) instanceof String) {
                            enums[i] = Float.valueOf((String) objects.get(i));
                        }
                    }
                    bundle.putFloatArray("enum", enums);
                } else if (format == DataFormat.DOUBLE) {
                    double[] enums = new double[objects.size()];
                    for (int i = 0; i < objects.size(); i++) {
                        if (objects.get(i) instanceof Number) {
                            enums[i] = ((Number) objects.get(i)).doubleValue();
                        } else if (objects.get(i) instanceof String) {
                            enums[i] = Double.valueOf((String) objects.get(i));
                        }
                    }
                    bundle.putDoubleArray("enum", enums);
                } else {
                    // TODO フォーマットエラー
                }
                break;
            case STRING: {
                String[] enums = new String[objects.size()];
                for (int i = 0; i < objects.size(); i++) {
                    enums[i] = String.valueOf(objects.get(i));
                }
                bundle.putStringArray("enum", enums);
            }   break;
            case BOOLEAN: {
                boolean[] enums = new boolean[objects.size()];
                for (int i = 0; i < objects.size(); i++) {
                    if (objects.get(i) instanceof String) {
                        enums[i] = Boolean.valueOf((String) objects.get(i));
                    } else if (objects.get(i) instanceof Boolean) {
                        enums[i] = (boolean) objects.get(i);
                    }
                }
                bundle.putBooleanArray("enum", enums);
            }   break;
        }
    }

    /**
     * ベンダー拡張を Bundle に格納します.
     *
     * @param bundle ベンダー拡張を格納するBundle
     */
    @SuppressWarnings("unchecked")
    protected void copyVendorExtensions(Bundle bundle) {
        if (mVendorExtensions != null && !mVendorExtensions.isEmpty()) {
            for (String key : mVendorExtensions.keySet()) {
                Object object = mVendorExtensions.get(key);
                if (object instanceof Integer) {
                    bundle.putInt(key, (Integer) object);
                } else if (object instanceof int[]) {
                    bundle.putIntArray(key, (int[]) object);
                } else if (object instanceof Long) {
                    bundle.putLong(key, (Long) object);
                } else if (object instanceof long[]) {
                    bundle.putLongArray(key, (long[]) object);
                } else if (object instanceof Short) {
                    bundle.putShort(key, (Short) object);
                } else if (object instanceof short[]) {
                    bundle.putShortArray(key, (short[]) object);
                } else if (object instanceof Byte) {
                    bundle.putByte(key, (Byte) object);
                } else if (object instanceof byte[]) {
                    bundle.putByteArray(key, (byte[]) object);
                } else if (object instanceof Boolean) {
                    bundle.putBoolean(key, (Boolean) object);
                } else if (object instanceof String) {
                    bundle.putString(key, (String) object);
                } else if (object instanceof String[]) {
                    bundle.putStringArray(key, (String[]) object);
                } else if (object instanceof Bundle) {
                    bundle.putParcelable(key, (Bundle) object);
                } else if (object instanceof ArrayList) {
                    bundle.putParcelableArrayList(key, (ArrayList) object);
                }
            }
        }
    }
}

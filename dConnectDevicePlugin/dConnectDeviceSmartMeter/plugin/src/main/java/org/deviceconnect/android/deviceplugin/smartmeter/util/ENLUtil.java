/*
 ENLUtil.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.smartmeter.util;

import android.util.Log;

import org.deviceconnect.android.deviceplugin.smartmeter.BuildConfig;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ECHONET Lite 低圧スマート電力量メータクラス関連ユーティリティの定義クラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class ENLUtil {
    /** Tag. */
    private final static String TAG = "SMARTMETER_PLUGIN";

    /** EHD. */
    static final byte EHD1 = (byte) 0x10;
    static final byte EHD2 = (byte) 0x81;

    /** EOJ(Controller). */
    static final byte EOJ_CNT_1 = (byte) 0x05;
    static final byte EOJ_CNT_2 = (byte) 0xFF;
    static final byte EOJ_CNT_3 = (byte) 0x01;
    /** EOJ(Smart Meter). */
    static final byte EOJ_SMC_1 = (byte) 0x02;
    static final byte EOJ_SMC_2 = (byte) 0x88;
    static final byte EOJ_SMC_3 = (byte) 0x01;

    /** ESV(Request). */
    static final int ESV_SETI = 0x60;
    static final int ESV_SETC = 0x61;
    static final int ESV_GET = 0x62;
    static final int ESV_INF_REQ = 0x63;
    static final int ESV_SETGET = 0x6E;

    /** ESV(Response). */
    public static final int ESV_SETI_SNA = 0x50;
    public static final int ESV_SETC_SNA = 0x51;
    public static final int ESV_GET_SNA = 0x52;
    static final int ESV_INF_SNA = 0x53;
    static final int ESV_SETGET_SNA = 0x5E;
    public static final int ESV_SET_RES = 0x71;
    public static final int ESV_GET_RES = 0x72;
    public static final int ESV_INF = 0x73;
    public static final int ESV_INFC = 0x74;
    static final int ESV_INFC_RES = 0x7A;
    public static final int ESV_SETGET_RES = 0x7E;

    /** Index. */
    static final int IDX_EHD1 = 0;
    static final int IDX_EHD2 = 1;
    static final int IDX_TID1 = 2;
    static final int IDX_TID2 = 3;
    static final int IDX_SEOJ1 = 4;
    static final int IDX_SEOJ2 = 5;
    static final int IDX_SEOJ3 = 6;
    static final int IDX_DEOJ1 = 7;
    static final int IDX_DEOJ2 = 8;
    static final int IDX_DEOJ3 = 9;
    static final int IDX_ESV = 10;
    static final int IDX_OPC = 11;

    /** EPC. */
    static final int EPC_OPERATION_STATUS = 0x80;

    /** Transaction ID. */
    private int mTransactionId = 0;

    /** ECHONET Property Map Table. */
    private int mapTable[][] = new int[16][8];
    /** Announce Support EPC Map. */
    private List<Integer> announceMap;
    /** Set Support EPC Map. */
    private List<Integer> setMap;
    /** Get Support EPC Map. */
    private List<Integer> getMap;

    /** Coefficient Value. */
    private int mCoeff = 1;

    /** Unit value. */
    private float mUnitValue = 0;

    /** 積算電力量計測値（正方向）. */
    private double[] mNormalValue = new double[48];
    /** 積算電力量計測値（逆方向）. */
    private double[] mReverseValue = new double[48];

    /**
     * コンストラクタ.
     */
    public ENLUtil() {
        initMapTable();
    }

    /**
     * 16進数ASCIIをバイナリーに変換する.
     * @param data ECHONET Lite 受信パケット
     * @return 変換バイナリーデータ.
     */
    public byte[] convertHex2Bin(final byte[] data) {
        String strLength = new String(data, 118, 4);
        Integer length = Integer.decode("0x" + strLength);
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "strLength:" + strLength + ", length: " + length);
        }

        String hex = new String(data, 123, length * 2);

        byte[] bytes = new byte[hex.length() / 2];
        for (int index = 0; index < bytes.length; index++) {
            bytes[index] = (byte) Integer.parseInt(hex.substring(index * 2, (index + 1) * 2), 16);
        }
        return bytes;
    }

    /**
     * 参照Property Map Table初期化.
     */
    private void initMapTable() {
        int code;
        for (int i = 0; i < 16; i++) {
            code = 0xF0;
            for (int j = 0; j < 8; j++) {
                mapTable[i][j] = code + i;
                code -= 0x10;
            }
        }
    }

    /**
     * 指定したPropertyが指定したタイプをサポートしているかを判定する.
     * @param epc ECHONET Property コード.
     * @param type 種別.
     * @return true : Support. / false : Not support.
     */
    public boolean isDefineProperty(final int epc, final String type) {

        List<Integer> map;
        switch (type) {
            case "announce":
                map = announceMap;
                break;
            case "get":
                map = getMap;
                break;
            case "set":
                map = setMap;
                break;
            default:
                return false;
        }
        for (Integer value : map) {
            if (value == epc) {
                return true;
            }
        }
        return false;
    }

    /**
     * Property map 取得.
     * @param buf Map data.
     * @return サポートEPC一覧.
     */
    private List<Integer> getPropertyMap(final byte[] buf) {
        int count;
        int pos = 1;

        // 格納数判定.
        if ((buf[0] & 0xff) < 16) {
            // 16個未満はそのまま実数が格納されている.
            count = buf[0] & 0xff;
        } else {
            // 16個以上はMapTableのビットフィールドを解析する.
            count = 0;
            for (int i = 1; i < 17; i++) {
                for (int j = 0; j < 8; j++) {
                    if ((((buf[i] & 0xFF) >> j) & 0x01) == 0x01) {
                        count++;
                    }
                }
            }
        }

        // Property map 構築.
        List<Integer> result = new ArrayList<>(count);
        if (count <= 16) {
            // 16個未満はそのままEPCが格納されている.
            for (int n = 0; n < count; n++) {
                result.add(buf[pos++] & 0xFF);
            }
        } else {
            // 16個以上はMapTableのビットフィールドを解析する.
            for (int i = 1; i < 17; i++) {
                for (int j = 0; j < 8; j++) {
                    if ((((buf[i] & 0xFF) >> j) & 0x01) == 0x01) {
                        result.add(mapTable[i-1][7-j]);
                    }
                }
            }
        }

        // ソート実施.
        Collections.sort(result);

        if (BuildConfig.DEBUG) {
            for (int i = 0; i < result.size(); i++) {
                Log.i(TAG, "Result[" + i +"]: " + String.format("0x%02X", result.get(i)));
            }
        }
        return result;
    }

    /**
     * ECHONET Lite Result Data Class.
     */
    public class ResultData {
        /** EPC. */
        public int mEpc;
        /** PDC. */
        public int mPdc;
        /** EDT. */
        public byte[] mEdt;

        /** Constructor. */
        ResultData(final int epc, final int pdc, final byte[] edt) {
            mEpc = epc;
            mPdc = pdc;
            mEdt = edt;
        }
    }

    /**
     * ECHONET Lite 要求パケット生成.
     * @param command 生成要求コマンド.
     * @param data パラメータデータ.
     * @return ECHONET Lite 要求パケット.
     */
    public byte[] makeEchonetLitePacket(final String command, final byte[] data) {

        // トランザクションID更新.
        mTransactionId++;
        // カウンターリミット判定.
        if (mTransactionId > 0xFFFF) {
            mTransactionId = 0;
        }
        // トランザクションID設定.
        byte tId1 = (byte) ((mTransactionId & 0xFF00) >> 8);
        byte tId2 = (byte) (mTransactionId & 0x00FF);
        // EPC設定.
        byte epc = (byte) 0x01;
//        // PDC設定.
//        byte pdc = (byte) 0x01;

        // コマンド生成判定.
        switch (command) {
            case "INIT1":
                return new byte[] {
                        EHD1,   // EHD1
                        EHD2,   // EHD2
                        tId1, tId2,     // TID
                        EOJ_CNT_1, EOJ_CNT_2, EOJ_CNT_3,    // SEOJ
                        EOJ_SMC_1, EOJ_SMC_2, EOJ_SMC_3,    // DEOJ
                        (byte) ESV_GET, // ESV (Get)
                        (byte) 0x03,    // OPC (3個)
                        (byte) 0x9D,    // EPC (状変アナウンスプロパティマップ)
                        (byte) 0x00,    // PDC
                        (byte) 0x9E,    // EPC (Setプロパティマップ)
                        (byte) 0x00,    // PDC
                        (byte) 0x9F,    // EPC (Getプロパティマップ)
                        (byte) 0x00     // PDC
                };
            case "INIT2":
                if (getMap.size() != 0) {
                    if (getMap.indexOf(0xD3) != -1) {
                        return new byte[] {
                                EHD1,   // EHD1
                                EHD2,   // EHD2
                                tId1, tId2,     // TID
                                EOJ_CNT_1, EOJ_CNT_2, EOJ_CNT_3,    // SEOJ
                                EOJ_SMC_1, EOJ_SMC_2, EOJ_SMC_3,    // DEOJ
                                (byte) ESV_GET, // ESV (Get)
                                (byte) 0x03,    // OPC (3個)
                                (byte) 0x80,    // EPC (動作状態)
                                (byte) 0x00,    // PDC
                                (byte) 0xD3,    // EPC (係数)
                                (byte) 0x00,    // PDC
                                (byte) 0xE1,    // EPC (電力量単位)
                                (byte) 0x00     // PDC
                        };
                    }
                }
                return new byte[] {
                        EHD1,   // EHD1
                        EHD2,   // EHD2
                        tId1, tId2,     // TID
                        EOJ_CNT_1, EOJ_CNT_2, EOJ_CNT_3,    // SEOJ
                        EOJ_SMC_1, EOJ_SMC_2, EOJ_SMC_3,    // DEOJ
                        (byte) ESV_GET, // ESV (Get)
                        (byte) 0x02,    // OPC (2個)
                        (byte) 0x80,    // EPC (動作状態)
                        (byte) 0x00,    // PDC
                        (byte) 0xE1,    // EPC (電力量単位)
                        (byte) 0x00     // PDC
                };

            case "GET_80":
            case "GET_E0":
            case "GET_E1":
            case "GET_E2":
            case "GET_E3":
            case "GET_E4":
            case "GET_E5":
            case "GET_E7":
            case "GET_E8":
            case "GET_EA":
            case "GET_EB":
            case "GET_EC":
            case "GET_ED":
                switch (command) {
                    case "GET_80":  epc = (byte) 0x80;  break;
                    case "GET_E0":  epc = (byte) 0xE0;  break;
                    case "GET_E1":  epc = (byte) 0xE1;  break;
                    case "GET_E2":  epc = (byte) 0xE2;  break;
                    case "GET_E3":  epc = (byte) 0xE3;  break;
                    case "GET_E4":  epc = (byte) 0xE4;  break;
                    case "GET_E5":  epc = (byte) 0xE5;  break;
                    case "GET_E7":  epc = (byte) 0xE7;  break;
                    case "GET_E8":  epc = (byte) 0xE8;  break;
                    case "GET_EA":  epc = (byte) 0xEA;  break;
                    case "GET_EB":  epc = (byte) 0xEB;  break;
                    case "GET_EC":  epc = (byte) 0xEC;  break;
                    case "GET_ED":  epc = (byte) 0xED;  break;
                }
                return new byte[] {
                        EHD1,   // EHD1
                        EHD2,   // EHD2
                        tId1, tId2,     // TID
                        EOJ_CNT_1, EOJ_CNT_2, EOJ_CNT_3,    // SEOJ
                        EOJ_SMC_1, EOJ_SMC_2, EOJ_SMC_3,    // DEOJ
                        (byte) 0x62,    // ESV (Get)
                        (byte) 0x01,    // OPC (1個)
                        epc,    // EPC
                        (byte) 0x00     // PDC
                };
            case "SET_E5":
                return new byte[] {
                        EHD1,   // EHD1
                        EHD2,   // EHD2
                        tId1, tId2,     // TID
                        EOJ_CNT_1, EOJ_CNT_2, EOJ_CNT_3,    // SEOJ
                        EOJ_SMC_1, EOJ_SMC_2, EOJ_SMC_3,    // DEOJ
                        (byte) ESV_SETC,    // ESV (Set)
                        (byte) 0x01,    // OPC (1個)
                        (byte) 0xE5,    // EPC
                        (byte) 0x01,    // PDC
                        data[0]         // PDT
                };
            case "SET_ED":
                return new byte[] {
                        EHD1,   // EHD1
                        EHD2,   // EHD2
                        tId1, tId2,     // TID
                        EOJ_CNT_1, EOJ_CNT_2, EOJ_CNT_3,    // SEOJ
                        EOJ_SMC_1, EOJ_SMC_2, EOJ_SMC_3,    // DEOJ
                        (byte) ESV_SETC,    // ESV (Set)
                        (byte) 0x01,    // OPC (1個)
                        (byte) 0xED,    // EPC
                        (byte) 0x07,    // PDC
                        data[0], data[1],   // year
                        data[2],            // month
                        data[3],            // day
                        data[4],            // hour
                        data[5],            // minute
                        data[6]             // count
                };
            default:
                return null;
        }
    }

    /**
     * ESV取得.
     * @param buf ECHONET Lite 受信パケット.
     * @return ESV値.
     */
    public int checkEsv(final byte[] buf) {
        // 受信パケット長判定.
        int dataLength = buf.length;
        if (dataLength < 10) {
            return 0xFF;
        }

        // Check ECHONET Packet.
        if (buf[IDX_EHD1] == EHD1 && buf[IDX_EHD2] == EHD2) {
            // Check TID.
            byte tId1 = (byte) ((mTransactionId & 0xFF00) >> 8);
            byte tId2 = (byte) (mTransactionId & 0x00FF);
            if (buf[IDX_ESV] != (byte) ESV_INF && buf[IDX_TID1] == tId1 && buf[IDX_TID2] == tId2 ||
                    buf[IDX_ESV] == (byte) ESV_INF) {
                // Check SEOJ-DEOJ.
                if (buf[IDX_SEOJ1] == EOJ_SMC_1 && buf[IDX_SEOJ2] == EOJ_SMC_2 && buf[IDX_SEOJ3] == EOJ_SMC_3 &&
                        buf[IDX_DEOJ1] == EOJ_CNT_1 && buf[IDX_DEOJ2] == EOJ_CNT_2 && buf[IDX_DEOJ3] == EOJ_CNT_3) {
                    // Check ESV.
                    return (buf[IDX_ESV] & 0xFF);
                }
            }
        }
        return 0xFF;
    }

    /**
     * Split Result Data.
     * @param buf Receive Data.
     * @return ResultData.
     */
    public ResultData[] splitResultData(final byte[] buf) {
        int count = buf[IDX_OPC];
        int pos = IDX_OPC + 1;
        if (count == 0) {
            return null;
        }
        ResultData[] resultData = new ResultData[count];

        for (int i = 0; i < count; i++) {
            int epc = (buf[pos++]) & 0xFF;
            int pdc = (buf[pos++]) & 0xFF;
            byte[] pdt;

            if (pdc != 0) {
                pdt = new byte[pdc];
                System.arraycopy(buf, pos, pdt, 0, pdc);
                pos += pdc;
            } else {
                pdt = null;
            }
            resultData[i] = new ResultData(epc, pdc, pdt);
        }
        return resultData;
    }

    /**
     * ECHONET Lite パケット解析.
     * @param buf ECHONET Lite 受信パケット.
     */
    public void analysisEchonetLitePacket(final byte[] buf) {

        int esv = checkEsv(buf);
        if (esv == 0xFF) {
            return;
        }
        switch (esv) {
            case ESV_SETI_SNA:
                break;
            case ESV_SETC_SNA:
                break;
//            case ESV_INF_SNA:
//                break;
//            case ESV_SETGET_SNA:
//                break;
            case ESV_SET_RES:

                break;
            case ESV_GET_RES:
            case ESV_GET_SNA:
                ResultData[] rd = splitResultData(buf);
                for (ResultData result : rd) {
                    analysisResultData(esv, result);
                }
                break;
            case ESV_INF:
                break;
            case ESV_INFC:
                break;
            case ESV_SETGET_RES:
                break;
            default:
                break;
        }
    }

    /**
     * ResultData解析ルーチン.
     * @param esv ECHONET Lite Service.
     * @param data ResultData.
     */
    private void analysisResultData(final int esv, final ResultData data) {
        int pos = 0;
        byte[] byTmp = new byte[4];

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "  EPC : " + String.format("0x%02X", data.mEpc));
        }
        if ((esv == ESV_GET_RES || esv == ESV_GET_SNA) && data.mPdc == 0) {
            // データ受信エラー.
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "  EPC : " + String.format("0x%02X", data.mEpc) + " - Error Response.");
            }
            return;
        }

        switch (data.mEpc) {
            case 0x9D:
                announceMap = getPropertyMap(data.mEdt);
                break;
            case 0x9E:
                setMap = getPropertyMap(data.mEdt);
                break;
            case 0x9F:
                getMap = getPropertyMap(data.mEdt);
                break;
            case 0x80:
                // ON/OFF応答返却
                switch (esv) {
                    case ESV_GET_RES:
                    case ESV_GET_SNA:
                        break;
                    case ESV_SET_RES:
                    case ESV_SETC_SNA:
                    case ESV_SETI_SNA:
                        break;
                }
                break;
            case 0xD3:
                // 係数
                switch (esv) {
                    case ESV_GET_RES:
                        mCoeff = ByteBuffer.wrap(data.mEdt).asIntBuffer().get();
                        break;
                    case ESV_GET_SNA:
                        break;
                }
                break;
            case 0xD7:
                // 有効桁数
                switch (esv) {
                    case ESV_GET_RES:
                        int effectiveDigit = (data.mEdt[0]) & 0xFF;
                        if (BuildConfig.DEBUG) {
                            Log.i(TAG, "  effectiveDigit = " + effectiveDigit);
                        }
                        break;
                    case ESV_GET_SNA:
                        break;
                }
                break;
            case 0xE0:
                // 積算電力量計測値（正）
                switch (esv) {
                    case ESV_GET_RES:
                        int totalNormalValue = ByteBuffer.wrap(data.mEdt).asIntBuffer().get();
                        totalNormalValue *= mCoeff;
                        if (BuildConfig.DEBUG) {
                            Log.i(TAG, "  totalNormalValue = " + totalNormalValue);
                        }
                        break;
                    case ESV_GET_SNA:
                        break;
                }
                break;
            case 0xE1:
                // 積算電力量単位.
                switch (esv) {
                    case ESV_GET_RES:
                        switch (data.mEdt[0]) {
                            case 0x00:
                                mUnitValue = 1f;
                                break;
                            case 0x01:
                                mUnitValue = 0.1f;
                                break;
                            case 0x02:
                                mUnitValue = 0.01f;
                                break;
                            case 0x03:
                                mUnitValue = 0.001f;
                                break;
                            case 0x04:
                                mUnitValue = 0.0001f;
                                break;
                            case 0x0A:
                                mUnitValue = 10f;
                                break;
                            case 0x0B:
                                mUnitValue = 100f;
                                break;
                            case 0x0C:
                                mUnitValue = 1000f;
                                break;
                            case 0x0D:
                                mUnitValue = 10000f;
                                break;
                            default:
                                mUnitValue = 0f;
                                break;
                        }
                        if (BuildConfig.DEBUG) {
                            Log.i(TAG, "  mUnitValue = " + mUnitValue);
                        }
                        break;
                    case ESV_GET_SNA:
                        break;
                }
                break;
            case 0xE2:
                // 積算電力量計測値履歴1（正）
                switch (esv) {
                    case ESV_GET_RES:
                        pos = 0;
                        int normalDays = (data.mEdt[pos++] << 8) & 0xFF00 |
                                (data.mEdt[pos++])      & 0x00FF;
                        for (int n = 0; n < 48; n++) {
                            System.arraycopy(data.mEdt, pos, byTmp, 0, 4);
                            pos += 4;
                            int tmp = ByteBuffer.wrap(byTmp).asIntBuffer().get();
                            if (tmp == 0xFFFFFFFE || mUnitValue == 0) {
                                mNormalValue[n] = 0xFFFFFFFE;
                            } else {
                                mNormalValue[n] = tmp * mCoeff * mUnitValue;
                            }
                        }
                        if (BuildConfig.DEBUG) {
                            Log.i(TAG, "  normalDays = " + normalDays);
                            for (double value : mNormalValue) {
                                Log.i(TAG, "  normalValue = " + value);
                            }
                        }
                        break;
                    case ESV_GET_SNA:
                        break;
                }
                break;
            case 0xE3:
                // 積算電力量計測値（逆）
                switch (esv) {
                    case ESV_GET_RES:
                        int totalReverseValue = ByteBuffer.wrap(data.mEdt).asIntBuffer().get();
                        totalReverseValue *= mCoeff;
                        if (BuildConfig.DEBUG) {
                            Log.i(TAG, "  totalReverseValue = " + totalReverseValue);
                        }
                        break;
                    case ESV_GET_SNA:
                        break;
                }
                break;
            case 0xE4:
                // 積算電力量計測値履歴1（逆）
                switch (esv) {
                    case ESV_GET_RES:
                        pos = 0;
                        int reverseDays = (data.mEdt[pos++] << 8) & 0xFF00 |
                                (data.mEdt[pos++])      & 0x00FF;
                        for (int n = 0; n < 48; n++) {
                            System.arraycopy(data.mEdt, pos, byTmp, 0, 4);
                            pos += 4;
                            int tmp = ByteBuffer.wrap(byTmp).asIntBuffer().get();
                            if (tmp == 0xFFFFFFFE || mUnitValue == 0) {
                                mReverseValue[n] = 0xFFFFFFFE;
                            } else {
                                mReverseValue[n] = tmp * mCoeff * mUnitValue;
                            }
                        }
                        if (BuildConfig.DEBUG) {
                            Log.i(TAG, "  reverseDays = " + reverseDays);
                            for (double value : mReverseValue) {
                                Log.i(TAG, "  reverseValue = " + value);
                            }
                        }
                        break;
                    case ESV_GET_SNA:
                        break;
                }
                break;
            case 0xE5:
                // 積算履歴収集日１
                switch (esv) {
                    case ESV_GET_RES:
                        int collectionDays1 = (data.mEdt[0]) & 0xFF;
                        if (BuildConfig.DEBUG) {
                            Log.i(TAG, "  collectionDays1 = " + collectionDays1);
                        }
                        break;
                    case ESV_GET_SNA:
                    case ESV_SET_RES:
                    case ESV_SETC_SNA:
                    case ESV_SETI_SNA:
                        break;
                }
                break;
            case 0xE7:
                // 瞬時電力計測値
                switch (esv) {
                    case ESV_GET_RES:
                        int energy = ByteBuffer.wrap(data.mEdt).asIntBuffer().get();
                        if (BuildConfig.DEBUG) {
                            Log.i(TAG, "  energy = " + energy);
                        }
                        break;
                    case ESV_GET_SNA:
                        break;
                }
                break;
            case 0xE8:
                // 瞬時電流計測値
                switch (esv) {
                    case ESV_GET_RES:
                        short rPhase = (short)((data.mEdt[pos++] << 8) & 0xFF00 |
                                (data.mEdt[pos++])      & 0x00FF);
                        short tPhase = (short)((data.mEdt[pos++] << 8) & 0xFF00 |
                                (data.mEdt[pos])        & 0x00FF);
                        float effectiveRPhase = rPhase * 0.1f;
                        float effectiveTPhase = tPhase * 0.1f;
                        if (BuildConfig.DEBUG) {
                            Log.i(TAG, "  effectiveRPhase = " + effectiveRPhase);
                            Log.i(TAG, "  effectiveTPhase = " + effectiveTPhase);
                        }
                        break;
                    case ESV_GET_SNA:
                        break;
                }
                break;
            case 0xEA:
                // 定時積算電力量計測値（正）
                switch (esv) {
                    case ESV_GET_RES:
                        pos = 0;
                        int normalYear = (data.mEdt[pos++] << 8) & 0xFF00 |
                                (data.mEdt[pos++])      & 0x00FF;
                        int normalMonth = (data.mEdt[pos++]) & 0xFF;
                        int normalDay = (data.mEdt[pos++]) & 0xFF;
                        int normalHour = (data.mEdt[pos++]) & 0xFF;
                        int normalMinute = (data.mEdt[pos++]) & 0xFF;
                        int normalSecond = (data.mEdt[pos++]) & 0xFF;
                        System.arraycopy(data.mEdt, pos, byTmp, 0, 4);
                        int normalElectricEnergy = ByteBuffer.wrap(byTmp).asIntBuffer().get();
                        if (BuildConfig.DEBUG) {
                            Log.i(TAG, "  normalYear = " + normalYear);
                            Log.i(TAG, "  normalMonth = " + normalMonth);
                            Log.i(TAG, "  normalDay = " + normalDay);
                            Log.i(TAG, "  normalHour = " + normalHour);
                            Log.i(TAG, "  normalMinute = " + normalMinute);
                            Log.i(TAG, "  normalSecond = " + normalSecond);
                            Log.i(TAG, "  normalElectricEnergy = " + normalElectricEnergy);
                        }
                        break;
                    case ESV_GET_SNA:
                        break;
                }
                break;
            case 0xEB:
                // 定時積算電力量計測値（逆）
                switch (esv) {
                    case ESV_GET_RES:
                        pos = 0;
                        int reverseYear = (data.mEdt[pos++] << 8) & 0xFF00 |
                                (data.mEdt[pos++])      & 0x00FF;
                        int reverseMonth = (data.mEdt[pos++]) & 0xFF;
                        int reverseDay = (data.mEdt[pos++]) & 0xFF;
                        int reverseHour = (data.mEdt[pos++]) & 0xFF;
                        int reverseMinute = (data.mEdt[pos++]) & 0xFF;
                        int reverseSecond = (data.mEdt[pos++]) & 0xFF;
                        System.arraycopy(data.mEdt, pos, byTmp, 0, 4);
                        int reverseElectricEnergy = ByteBuffer.wrap(byTmp).asIntBuffer().get();
                        if (BuildConfig.DEBUG) {
                            Log.i(TAG, "  reverseYear = " + reverseYear);
                            Log.i(TAG, "  reverseMonth = " + reverseMonth);
                            Log.i(TAG, "  reverseDay = " + reverseDay);
                            Log.i(TAG, "  reverseHour = " + reverseHour);
                            Log.i(TAG, "  reverseMinute = " + reverseMinute);
                            Log.i(TAG, "  reverseSecond = " + reverseSecond);
                            Log.i(TAG, "  reverseElectricEnergy = " + reverseElectricEnergy);
                        }
                        break;
                    case ESV_GET_SNA:
                        break;
                }
                break;
            case 0xEC:
                // 積算電力量計測値履歴２
                switch (esv) {
                    case ESV_GET_RES:
                        pos = 0;
                        int history2Year = (data.mEdt[pos++] << 8) & 0xFF00 |
                                (data.mEdt[pos++])      & 0x00FF;
                        int history2Month = (data.mEdt[pos++]) & 0xFF;
                        int history2Day = (data.mEdt[pos++]) & 0xFF;
                        int history2Hour = (data.mEdt[pos++]) & 0xFF;
                        int history2Minute = (data.mEdt[pos++]) & 0xFF;
                        int history2Count = (data.mEdt[pos++]) & 0xFF;

                        double[] history2Value = new double[history2Count * 2];
                        for (int n = 0; n < history2Count * 2; n++) {
                            System.arraycopy(data.mEdt, pos, byTmp, 0, 4);
                            pos += 4;
                            int tmp = ByteBuffer.wrap(byTmp).asIntBuffer().get();
                            if (tmp == 0xFFFFFFFE || mUnitValue == 0) {
                                history2Value[n] = 0xFFFFFFFE;
                            } else {
                                history2Value[n] = tmp * mCoeff * mUnitValue;
                            }
                        }
                        if (BuildConfig.DEBUG) {
                            Log.i(TAG, "  history2Year = " + history2Year);
                            Log.i(TAG, "  history2Month = " + history2Month);
                            Log.i(TAG, "  history2Day = " + history2Day);
                            Log.i(TAG, "  history2Hour = " + history2Hour);
                            Log.i(TAG, "  history2Minute = " + history2Minute);
                            Log.i(TAG, "  history2Count = " + history2Count);
                            for (double value : history2Value) {
                                Log.i(TAG, "  history2Value = " + value);
                            }
                        }
                        break;
                    case ESV_GET_SNA:
                        break;
                }
                break;
            case 0xED:
                // 積算履歴収集日２
                switch (esv) {
                    case ESV_GET_RES:
                        pos = 0;
                        int collectionDays2Year = (data.mEdt[pos++] << 8) & 0xFF00 |
                                (data.mEdt[pos++])      & 0x00FF;
                        int collectionDays2Month = (data.mEdt[pos++]) & 0xFF;
                        int collectionDays2Day = (data.mEdt[pos++]) & 0xFF;
                        int collectionDays2Hour = (data.mEdt[pos++]) & 0xFF;
                        int collectionDays2Minute = (data.mEdt[pos++]) & 0xFF;
                        int collectionDays2Count = (data.mEdt[pos]) & 0xFF;
                        if (BuildConfig.DEBUG) {
                            Log.i(TAG, "  collectionDays2Year = " + collectionDays2Year);
                            Log.i(TAG, "  collectionDays2Month = " + collectionDays2Month);
                            Log.i(TAG, "  collectionDays2Day = " + collectionDays2Day);
                            Log.i(TAG, "  collectionDays2Hour = " + collectionDays2Hour);
                            Log.i(TAG, "  collectionDays2Minute = " + collectionDays2Minute);
                            Log.i(TAG, "  collectionDays2Count = " + collectionDays2Count);
                        }
                        break;
                    case ESV_GET_SNA:
                    case ESV_SET_RES:
                    case ESV_SETC_SNA:
                    case ESV_SETI_SNA:
                        break;
                }
                break;
        }
    }

    /**
     * Get unit value.
     * @return Unit value.
     */
    public float getUnitValue() {
        return mUnitValue;
    }

    /**
     * Get coefficient value.
     * @return Coeff value.
     */
    public int getCoeffValue() {
        return mCoeff;
    }
}

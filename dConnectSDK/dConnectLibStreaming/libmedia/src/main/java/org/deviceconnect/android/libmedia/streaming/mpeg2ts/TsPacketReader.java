package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import org.deviceconnect.android.libmedia.BuildConfig;
import org.deviceconnect.android.libmedia.streaming.util.HexUtil;

public class TsPacketReader {
    /**
     * パケット情報ログの出力フラグ.
     */
    private static final boolean INFO = false;

    /**
     * デバッグ用フラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "TS-READER";

    /**
     * TS パケットサイズの定義.
     */
    private static final int TS_PACKET_SIZE = 188;

    /**
     * TS パケットヘッダ.
     */
    private static final byte SYNC_BYTE = 0x47;

    /**
     * Program Association Table (PAT) の定義.
     */
    private static final int PAT = 0x00;

    /**
     * Conditional Access Table (CAT) の定義.
     */
    private static final int CAT = 0x01;

    /**
     * Transport Stream Description Table (TSDT) の定義.
     */
    private static final int TSDT = 0x02;

    /**
     * IPMP Control Information Table の定義.
     */
    private static final int IPMP = 0x03;

    private static final int STREAM_ID_PROGRAM_STREAM_MAP = 0b10111100;
    private static final int STREAM_ID_PRIVATE_STREAM_1 = 0b10111101;
    private static final int STREAM_ID_PADDING_STREAM = 0b10111110;
    private static final int STREAM_ID_PRIVATE_STREAM_2 = 0b10111111;
    private static final int STREAM_ID_ECM_STREAM = 0b11110000;
    private static final int STREAM_ID_EMM_STREAM = 0b11110001;
    private static final int STREAM_ID_DSMCC_STREAM = 0b11110010;
    private static final int STREAM_ID_PROGRAM_STREAM_DIRECTORY = 0b11111111;
    private static final int STREAM_ID_H222_STREAM = 0b11111000;

    /**
     * TSパケットの連続性を確認するための情報を格納するマップ.
     *
     * <p>
     * TSパケットは PID ごとに continuityCounter の値が 0-15 を順番に送られてきます。
     * この値がずれていないか確認するために使用します。
     * </p>
     */
    private SparseArray<Continuity> mContinuityCounterMap = new SparseArray<>();

    /**
     * TS パケットデータを格納するくデータソース.
     */
    private final Buffer mPacketData = new Buffer(TS_PACKET_SIZE);

    /**
     * PAT の情報を格納するクラス.
     */
    private final PAT mPAT = new PAT();

    /**
     * PMT の情報を格納するクラス.
     */
    private final SparseArray<PMT> mPMTMap = new SparseArray<>();

    /**
     * 送られてきたデータを格納するバッファ.
     */
    private final PES mPES = new PES();

    /**
     * 取り出したストリームデータを通知するコールバック.
     */
    private Callback mCallback;

    /**
     * TS パケットから取得したストリームデータを通知するコールバックを設定します.
     *
     * @param callback コールバック
     */
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * TS パケットデータを読み込みます.
     *
     * @param data データ
     * @param length データサイズ
     */
    public void readPacket(byte[] data, int length) {
        parseTS(new Buffer(data, length));
    }

    /**
     * TS パケットデータを読み込みます.
     *
     * @param buffer パケットデータ
     */
    void readPacket(Buffer buffer) {
        while (buffer.remaining() - TS_PACKET_SIZE >= 0) {
            mPacketData.reset();
            int size = buffer.read(mPacketData.mData, 0, TS_PACKET_SIZE);
            if (size == TS_PACKET_SIZE && mPacketData.mData[0] == SYNC_BYTE) {
                parseTS(mPacketData);
            } else {
                // 同期バイト以外の場合はパケットが壊れているので、パケットを破棄します。
                // 1328 のサイズで常に送られてきて、足りない部分は 0x00 で埋め尽くされています。
                break;
            }
        }
    }

    /**
     * TSパケットのヘッダーを格納するためのバッファ.
     */
    private final byte[] mTsPacketHeader = new byte[4];

    /**
     * TSパケットを解析して、 Byte stream format (Annex B) を抜き出してリスナーに通知します.
     *
     * @param packetData TSパケットが格納されたデータソース
     */
    private void parseTS(Buffer packetData) {
        packetData.read(mTsPacketHeader, 0, 4);

        // TS パケットのヘッダー解析
        int syncByte = mTsPacketHeader[0];
        if (syncByte != SYNC_BYTE) {
            return;
        }

        boolean transportErrorIndicator = ((0x80 & mTsPacketHeader[1]) != 0);
        boolean payloadUnitStartIndicator = ((0x40 & mTsPacketHeader[1]) != 0);
        boolean transportPriority = ((0x20 & mTsPacketHeader[1]) != 0);
        int pid = ((mTsPacketHeader[1] & 0x1F) << 8) | ((mTsPacketHeader[2] & 0xFF));
        int transportScramblingControl = ((mTsPacketHeader[3] & 0xC0) >> 6);
        int adaptationFieldControl = ((mTsPacketHeader[3] & 0x30) >> 4);
        int continuityCounter = (mTsPacketHeader[3] & 0x0F);

        if (INFO) {
            Log.d(TAG, "transportErrorIndicator=" + transportErrorIndicator
                    + ", payloadUnitStartIndicator=" + payloadUnitStartIndicator
                    + ", transportPriority=" + transportPriority + ", pid=" + pid
                    + ", transportScramblingControl=" + Integer.toBinaryString(transportScramblingControl)
                    + ", adaptationFieldControl=" + Integer.toBinaryString(adaptationFieldControl)
                    + ", continuityCounter=" + continuityCounter);
        }


        Continuity continuity = getContinuity(pid);
        // continuityCounter の値が連続になっていない場合はエラーとします
        if (continuity.mCounter != -1 && (continuity.mCounter + 1) % 16 != continuityCounter) {
            continuity.hasError = true;
            if (DEBUG) {
                Log.e(TAG, "hasError PID=" + pid + " counter=[" + continuity.mCounter + " " + continuityCounter + "]");
            }
        }
        continuity.mCounter = continuityCounter;

        // Adaptation Field
        if (adaptationFieldControl == 0b10 || adaptationFieldControl == 0b11) {
            parseAdaptationField(packetData);
        }

        // Payload data
        if (adaptationFieldControl == 0b01 || adaptationFieldControl == 0b11) {

            // PES、 PSI のどちらかのペイロードが data_byte には格納されます。
            // PID で、PAT を判別し解析を行い PSI の PID を取得します。
            // PSI の場合は pointer_field (0x00) が先頭に入ります。
            // PES の場合は 0x000001 が先頭に入ります。

            if (pid == PAT) {
                parsePAT(packetData, payloadUnitStartIndicator);
            } else if (pid == CAT) {
                if (INFO) {
                    Log.w(TAG, " #### CAT");
                }
            } else if (pid == TSDT) {
                if (INFO) {
                    Log.w(TAG, " #### TSDT");
                }
            } else if (pid == IPMP) {
                if (INFO) {
                    Log.w(TAG, " #### IPMP");
                }
            } else if (pid <= 0xF) {
                // Reserved for future use
                if (INFO) {
                    Log.w(TAG, " #### Reserved for future use. pid=" + pid);
                }
            } else if (pid <= 0x1F) {
                if (INFO) {
                    Log.w(TAG, " #### etc... pid=" + pid);
                }
            } else {
                if (mPAT.containPMT(pid)) {
                    parsePMT(packetData, payloadUnitStartIndicator, pid);
                } else {
                    parsePES(packetData, payloadUnitStartIndicator, pid, continuity);
                }
            }
        }
    }

    /**
     * Program Association Tables (PAT) を解析します.
     *
     * @param packetData データソース
     * @param payloadUnitStartIndicator 開始フラグ
     */
    private void parsePAT(Buffer packetData, boolean payloadUnitStartIndicator) {
        if (payloadUnitStartIndicator) {
            int pointerField = packetData.read();
            if (pointerField != 0x00) {
                if (DEBUG) {
                    Log.e(TAG, "## " + HexUtil.hexToString(packetData.mData, 48));
                    Log.e(TAG, "!!!!!!!! ERROR PAT.");
                }
                return;
            }

            int tableId = packetData.read();
            int sectionLength = (((packetData.read() & 0x0F) << 8)) | ((packetData.read() & 0xFF));
            int transportStreamId = ((packetData.read() & 0xFF) | ((packetData.read() & 0xFF) << 8));
            int b = packetData.read() & 0xFF;
            int versionNumber = ((b >> 1) & 0x1F);
            boolean currentNextIndicator = ((b & 0x01) != 0);
            int sectionNumber = packetData.read() & 0xFF;
            int lastSectionNumber = packetData.read() & 0xFF;

            if (INFO) {
                Log.w(TAG, " PAT: ");
                Log.w(TAG, " tableId: " + tableId);
                Log.w(TAG, " sectionLength: " + sectionLength);
                Log.w(TAG, " transportStreamId: " + transportStreamId);
                Log.w(TAG, " versionNumber: " + versionNumber);
                Log.w(TAG, " currentNextIndicator: " + currentNextIndicator);
                Log.w(TAG, " sectionNumber: " + sectionNumber);
                Log.w(TAG, " lastSectionNumber: " + lastSectionNumber);
            }

            for (int i = 0; i < sectionLength - 4 - 5; i += 4) {
                int programNumber = (((packetData.read() & 0xFF) << 8) | ((packetData.read() & 0xFF)));
                int programMapPid = (((packetData.read() & 0x1F) << 8) | ((packetData.read() & 0xFF)));
                if (INFO) {
                    Log.w(TAG, "   programNumber: " + programNumber);
                    Log.w(TAG, "   programMapPid: " + programMapPid);
                }
                mPAT.put(programMapPid, programNumber);
            }
        } else {
            // TODO: PAT が複数パケットに分かれている場合の処理を行うこと。
        }
    }

    /**
     * PMT (Program Map Table) を解析します.
     *
     * @param packetData データソース
     * @param payloadUnitStartIndicator 開始フラグ
     * @param pid pid
     */
    private void parsePMT(Buffer packetData, boolean payloadUnitStartIndicator, int pid) {
        if (payloadUnitStartIndicator) {
            int pointerField = packetData.read();
            if (pointerField != 0x00) {
                if (DEBUG) {
                    Log.e(TAG, "## " + HexUtil.hexToString(packetData.mData, 48));
                    Log.e(TAG, "!!!!!!!! ERROR PMT.");
                }
                return;
            }

            PMT pmt = mPMTMap.get(pid);
            if (pmt == null) {
                pmt = new PMT();
                mPMTMap.put(pid, pmt);
            } else {
                pmt.clear();
            }

            int tableId = packetData.read();
            int sectionLength = (((packetData.read() & 0x0F) << 8)) | ((packetData.read() & 0xFF));
            int programNumber = ((packetData.read() & 0xFF) | ((packetData.read() & 0xFF) << 8));
            int b = packetData.read() & 0xFF;
            int versionNumber = ((b >> 1) & 0x1F);
            boolean currentNextIndicator = ((b & 0x01) != 0);
            int sectionNumber = packetData.read() & 0xFF;
            int lastSectionNumber = packetData.read() & 0xFF;
            int pcrPID = (((packetData.read() & 0x1F) << 8)) | ((packetData.read() & 0xFF));
            int programInfoLength = (((packetData.read() & 0x0F) << 8)) | ((packetData.read() & 0xFF));
            packetData.skip(programInfoLength);

            if (INFO) {
                Log.w(TAG, " PMT: " + pid);
                Log.w(TAG, " tableId: " + tableId);
                Log.w(TAG, " sectionLength: " + sectionLength);
                Log.w(TAG, " programNumber: " + programNumber);
                Log.w(TAG, " versionNumber: " + versionNumber);
                Log.w(TAG, " currentNextIndicator: " + currentNextIndicator);
                Log.w(TAG, " sectionNumber: " + sectionNumber);
                Log.w(TAG, " lastSectionNumber: " + lastSectionNumber);
                Log.w(TAG, " pcrPID: " + pcrPID);
                Log.w(TAG, " programInfoLength: " + programInfoLength);
            }

            for (int i = 0; i < sectionLength - programInfoLength - 13;) {
                int streamType = packetData.read() & 0xFF;
                int elementaryPID = (((packetData.read() & 0x1F) << 8) | ((packetData.read() & 0xFF)));
                int ES_info_length = (((packetData.read() & 0x0F) << 8) | ((packetData.read() & 0xFF)));
                packetData.skip(ES_info_length);

                pmt.put(elementaryPID, streamType);

                if (INFO) {
                    Log.w(TAG, "   streamType: " + streamType);
                    Log.w(TAG, "   elementaryPID: " + elementaryPID);
                    Log.w(TAG, "   ES_info_length: " + ES_info_length);
                }

                i += (5 + ES_info_length);
            }
        } else {
            // TODO: PMT が複数パケットに分かれている場合の処理を行うこと。
        }
    }

    /**
     * Packetized Elementary Stream (PES) を解析します.
     *
     * @param packetData                データソース
     * @param payloadUnitStartIndicator 開始フラグ
     * @param pid                       PID
     * @param continuity                連続性確認用カウンター
     */
    private void parsePES(Buffer packetData, boolean payloadUnitStartIndicator, int pid, Continuity continuity) {
        if (payloadUnitStartIndicator) {
            // payload unit start indicator が true の場合はデータの先頭になる
            // その場合は、溜め込んでいたデータを通知する。
            if (mPES.size() > 0 && !continuity.hasError) {
                postByteStream(mPES);
            }

            mPES.reset();

            // PES のスタートコード
            if (!(packetData.read() == 0x00 && packetData.read() == 0x00 && packetData.read() == 0x01)) {
                if (DEBUG) {
                    Log.e(TAG, "## " + HexUtil.hexToString(packetData.mData, 48));
                    Log.e(TAG, "!!!!!!!! ERROR PES Packet.");
                }
                return;
            }

            // ストリームのタイプ: Audio streams (0xC0-0xDF), Video streams (0xE0-0xEF)
            int streamId = packetData.read() & 0xFF;
            int pesPacketLength = ((packetData.read() & 0xFF) << 8) | (packetData.read() & 0xFF);
            long pts = 0;
            long dts = 0;

            if (INFO) {
                Log.w(TAG, " streamId=" + streamId);
                Log.w(TAG, " pesPacketLength=" + pesPacketLength);
            }

            if (streamId != STREAM_ID_PROGRAM_STREAM_MAP &&
                    streamId != STREAM_ID_PADDING_STREAM &&
                    streamId != STREAM_ID_PRIVATE_STREAM_2 &&
                    streamId != STREAM_ID_ECM_STREAM &&
                    streamId != STREAM_ID_EMM_STREAM &&
                    streamId != STREAM_ID_PROGRAM_STREAM_DIRECTORY &&
                    streamId != STREAM_ID_DSMCC_STREAM &&
                    streamId != STREAM_ID_H222_STREAM) {

                byte optionalPESHeader0 = (byte) packetData.read();
                byte optionalPESHeader1 = (byte) packetData.read();
                byte optionalPESHeader2 = (byte) packetData.read();

                int optionPrefix = ((optionalPESHeader0 & 0xFF) >> 6);
                if (optionPrefix != 0x02) {
                    if (DEBUG) {
                        Log.e(TAG, "!!!!!!!! ERROR PES Option Header.");
                    }
                    return;
                }

                int scramblingControl = (optionalPESHeader0 & 0x30) >> 4;
                int priority = optionalPESHeader0 & 0x08;
                int dataAlignmentIndicator = optionalPESHeader0 & 0x04;
                int copyright = optionalPESHeader0 & 0x02;
                int originalOrCopy = optionalPESHeader0 & 0x01;
                int ptsDtsFlag = (optionalPESHeader1 & 0xC0) >> 6;
                boolean escrFlag = (optionalPESHeader1 & 0x20) != 0;
                boolean esRateFlag = (optionalPESHeader1 & 0x10) != 0;
                boolean dsmTrickModeFlag = (optionalPESHeader1 & 0x08) != 0;
                boolean additionalCopyInfoFlag = (optionalPESHeader1 & 0x04) != 0;
                boolean crcFlag = (optionalPESHeader1 & 0x02) != 0;
                boolean extensionFlag = (optionalPESHeader1 & 0x01) != 0;
                int pesHeaderLength = optionalPESHeader2 & 0xFF;
                int startPosOfPESHeader = packetData.mPosition;

                if (INFO) {
                    Log.d(TAG, "  optional pes header");
                    Log.d(TAG, "    scramblingControl: " + scramblingControl);
                    Log.d(TAG, "    priority: " + priority);
                    Log.d(TAG, "    dataAlignmentIndicator: " + dataAlignmentIndicator);
                    Log.d(TAG, "    copyright: " + copyright);
                    Log.d(TAG, "    originalOrCopy: " + originalOrCopy);
                    Log.d(TAG, "    ptsDtsFlag: " + ptsDtsFlag);
                    Log.d(TAG, "    escrFlag: " + escrFlag);
                    Log.d(TAG, "    esRateFlag: " + esRateFlag);
                    Log.d(TAG, "    dsmTrickModeFlag: " + dsmTrickModeFlag);
                    Log.d(TAG, "    additionalCopyInfoFlag: " + additionalCopyInfoFlag);
                    Log.d(TAG, "    crcFlag: " + crcFlag);
                    Log.d(TAG, "    extensionFlag: " + extensionFlag);
                }

                if (ptsDtsFlag == 0b10) {
                    pts = parsePtsDts(packetData);

                    if (INFO) {
                        Log.d(TAG, "PTS: " + (pts / (float) 90000));
                    }
                } else if (ptsDtsFlag == 0b11) {
                    pts = parsePtsDts(packetData);
                    dts = parsePtsDts(packetData);

                    if (INFO) {
                        Log.d(TAG, "PTS: " + (pts / (float) 90000));
                        Log.d(TAG, "DTS: " + (dts / (float) 90000));
                    }
                }

                if (escrFlag) {
                    packetData.skip(6);
                }

                if (esRateFlag) {
                    packetData.skip(3);
                }

                if (dsmTrickModeFlag) {
                    packetData.skip(1);
                }

                if (additionalCopyInfoFlag) {
                    packetData.skip(1);
                }

                if (crcFlag) {
                    packetData.skip(2);
                }

                if (extensionFlag) {
                    byte b = (byte) packetData.read();

                    boolean privateDataFlag = (b & 0x80) != 0;
                    boolean packHeaderFieldFlag = (b & 0x40) != 0;
                    boolean programPacketSequenceCounterFlag = (b & 0x20) != 0;
                    boolean PSTDBufferFlag = (b & 0x10) != 0;
                    boolean extensionFlag2 = (b & 0x01) != 0;

                    if (privateDataFlag) {
                        packetData.skip(16);
                    }

                    if (packHeaderFieldFlag) {
                        packetData.skip(1);
                    }

                    if (programPacketSequenceCounterFlag) {
                        packetData.skip(2);
                    }

                    if (PSTDBufferFlag) {
                        packetData.skip(2);
                    }

                    if (extensionFlag2) {
                        byte e = (byte) packetData.read();
                        int extensionFieldLength = e & 0x7F;
                        packetData.skip(extensionFieldLength);
                    }
                }

                // skip stuffing_byte
                int skip = pesHeaderLength - (packetData.mPosition - startPosOfPESHeader);
                if (skip > 0) {
                    packetData.skip(skip);
                }
            } else if (streamId == STREAM_ID_PROGRAM_STREAM_MAP ||
                    streamId == STREAM_ID_PRIVATE_STREAM_2 ||
                    streamId == STREAM_ID_ECM_STREAM ||
                    streamId == STREAM_ID_EMM_STREAM ||
                    streamId == STREAM_ID_PROGRAM_STREAM_DIRECTORY ||
                    streamId == STREAM_ID_DSMCC_STREAM ||
                    streamId == STREAM_ID_H222_STREAM) {
            } else {
                // STREAM_ID_PADDING_STREAM
                if (pesPacketLength == 0) {
                    packetData.skip(packetData.remaining());
                } else {
                    packetData.skip(pesPacketLength);
                }
                return;
            }

            // payloadUnitStartIndicator が true なので新規ペイロードが開始されたので
            // エラーフラグを解除しておきます。
            continuity.reset();

            mPES.setPID(pid);
            mPES.setStreamId(streamId);
            mPES.setPts(pts);
            mPES.setDts(dts);
            mPES.write(packetData.mData, packetData.mPosition, packetData.remaining());
        } else {
            // payload unit start indicator が false の場合はデータの後発になる
            // その場合は、データを溜め込みます。
            mPES.write(packetData.mData, packetData.mPosition, packetData.remaining());
        }
    }

    /**
     * PTS, DTS を解析します.
     *
     * @param packetData パケットデータ
     * @return PTS or DTS の値
     */
    private long parsePtsDts(Buffer packetData) {
        return ((long) ((packetData.read() & 0x0E) << 29)) | ((packetData.read() & 0xFF) << 22) |
                ((packetData.read() & 0xFE) << 14) | ((packetData.read() & 0xFF) << 7) |
                ((packetData.read() & 0xFE) >> 1);
    }

    /**
     * PCR、OPCR を解析します.
     *
     * @param packetData パケットデータ
     * @return PCR or OPCR の値
     */
    private long parsePcrOpcr(Buffer packetData) {
        byte[] b = {
                (byte) packetData.read(),
                (byte) packetData.read(),
                (byte) packetData.read(),
                (byte) packetData.read(),
                (byte) packetData.read(),
                (byte) packetData.read(),
        };

        int base = (b[0] << 24) | (b[1] << 16) | (b[2] << 8) | b[3];
        base <<= 1;
        base |= ((b[4] >> 7) & 0x01);
        int ext = (b[4] << 8) & 0x100;
        ext = (ext | b[5]);
        return base * 300 + ext;
    }

    /**
     * Adaptation Field の解析を行います.
     *
     * <p>
     * 解析を行いバイト数は取っているが処理は、各パラメータに対する処理は行なっていません。
     * </p>
     *
     * @param packetData データソース
     */
    private void parseAdaptationField(Buffer packetData) {
        int adaptationFieldLength = packetData.read() & 0xFF;
        if (adaptationFieldLength > 0) {
            int startPosOfAdaptationField = packetData.mPosition;

            int flag = packetData.read();
            boolean discontinuityIndicator = ((flag & 0x80) != 0);
            boolean randomAccessIndicator = ((flag & 0x40) != 0);
            boolean elementaryStreamPriorityIndicator = ((flag & 0x20) != 0);
            boolean PCRFlag = ((flag & 0x10) != 0);
            boolean OPCRFlag = ((flag & 0x08) != 0);
            boolean splicingPointFlag = ((flag & 0x04) != 0);
            boolean transportPrivateDataFlag = ((flag & 0x02) != 0);
            boolean adaptationFieldExtensionFlag = ((flag & 0x01) != 0);

            if (INFO) {
                Log.d(TAG, " AdaptationField ");
                Log.d(TAG, "   adaptationFieldLength: " + adaptationFieldLength);
                Log.d(TAG, "   discontinuityIndicator: " + discontinuityIndicator);
                Log.d(TAG, "   randomAccessIndicator: " + randomAccessIndicator);
                Log.d(TAG, "   elementaryStreamPriorityIndicator: " + elementaryStreamPriorityIndicator);
                Log.d(TAG, "   PCRFlag: " + PCRFlag);
                Log.d(TAG, "   OPCRFlag: " + OPCRFlag);
                Log.d(TAG, "   splicingPointFlag: " + splicingPointFlag);
                Log.d(TAG, "   transportPrivateDataFlag: " + transportPrivateDataFlag);
                Log.d(TAG, "   adaptationFieldExtensionFlag: " + adaptationFieldExtensionFlag);
            }

            long PCR;
            long OPCR;
            int spliceCountDown;
            int transportPrivateDataLength;

            if (PCRFlag) {
                PCR = parsePcrOpcr(packetData);

                if (INFO) {
                    Log.d(TAG, "   PCR: " + PCR + " : " + (PCR / (float) 27000000));
                }
            }

            if (OPCRFlag) {
                OPCR = parsePcrOpcr(packetData);

                if (INFO) {
                    Log.d(TAG, "   OPCR " + OPCR + " : " + (OPCR / (float) 27000000));
                }
            }

            if (splicingPointFlag) {
                spliceCountDown = packetData.read() & 0xFF;
                if (INFO) {
                    Log.d(TAG, "   spliceCountDown " + spliceCountDown);
                }
            }

            if (transportPrivateDataFlag) {
                transportPrivateDataLength = packetData.read() & 0xFF;

                if (INFO) {
                    Log.d(TAG, "   transportPrivateDataLength " + transportPrivateDataLength);
                }

                for (int i = 0; i < transportPrivateDataLength; i++) {
                    packetData.read();
                }
            }

            if (adaptationFieldExtensionFlag) {
                int adaptationExtensionLength = packetData.read() & 0xFF;
                int startPosOfAdaptationExtension = packetData.mPosition;

                int a = packetData.read();
                boolean legalTimeWindowFlag = (a & 0x80) != 0;
                boolean piecewiseRateFlag = (a & 0x40) != 0;
                boolean seamlessSpliceFlag = (a & 0x20) != 0;
                int reserved = (a & 0x1F);

                if (INFO) {
                    Log.d(TAG, "   adaptationExtensionLength: " + adaptationExtensionLength);
                    Log.d(TAG, "   legalTimeWindowFlag: " + legalTimeWindowFlag);
                    Log.d(TAG, "   piecewiseRateFlag: " + piecewiseRateFlag);
                    Log.d(TAG, "   seamlessSpliceFlag: " + seamlessSpliceFlag);
                    Log.d(TAG, "   reserved: " + reserved);
                }

                if (legalTimeWindowFlag) {
                    packetData.skip(2);
                }

                if (piecewiseRateFlag) {
                    packetData.skip(3);
                }

                if (seamlessSpliceFlag) {
                    packetData.skip(5);
                }

                // skip reserved
                int skip = adaptationExtensionLength - (packetData.mPosition - startPosOfAdaptationExtension);
                if (skip > 0) {
                    packetData.skip(skip);
                }
            }

            // skip stuffing_byte
            int skip = adaptationFieldLength - (packetData.mPosition - startPosOfAdaptationField);
            if (skip > 0) {
                packetData.skip(skip);
            }
        }
    }

    /**
     * ByteStream をリスナーに通知します.
     *
     * @param pes ストリームタイプ
     */
    private void postByteStream(PES pes) {
        if (mCallback != null) {
            int streamType = getStreamType(pes);
            int streamId = pes.getStreamId();
            byte[] data = pes.toByteArray();
            int dataLength = pes.size();
            long pts = pes.getPts();
            mCallback.onByteStream(streamId, data, dataLength, pts);
        }
    }

    /**
     * PES のストリームタイプを取得します.
     *
     * @param pes PES
     * @return ストリームタイプ
     */
    private int getStreamType(PES pes) {
        for (int i = 0; i < mPMTMap.size(); i++) {
            PMT pmt = mPMTMap.get(mPMTMap.keyAt(i));
            if (pmt != null) {
                int streamType = pmt.getStreamType(pes.mPID);
                if (streamType != -1) {
                    return streamType;
                }
            }
        }
        return -1;
    }

    /**
     * PIDに対応した Continuity クラスを取得します.
     *
     * <p>
     * PID に対応した Continuity が存在しない場合には、内部で作成して返却しますs。
     * </p>
     *
     * @param pid PID
     * @return PIDに対応した Continuity のインスタンス
     */
    private Continuity getContinuity(int pid) {
        Continuity continuity = mContinuityCounterMap.get(pid);
        if (continuity == null) {
            continuity = new Continuity();
            mContinuityCounterMap.put(pid, continuity);
        }
        return continuity;
    }

    /**
     * continuity_counter の値を保持するクラス.
     */
    private class Continuity {
        /**
         * 前回送られてきた continuity_counter の値を保持する変数.
         */
        int mCounter = -1;

        /**
         * エラー状態.
         * <p>
         * continuity_counter の値が連続になっていない場合はtrue、それ以外はfalse
         * </p>
         */
        boolean hasError;

        /**
         * continuity_counterの値を初期化します.
         */
        void reset() {
            hasError = false;
            mCounter = -1;
        }
    }

    /**
     * PAT の情報を格納するクラス.
     */
    private static class PAT {
        /**
         * Program Map Tables.
         *
         * <p>
         * PID が、どの PMT に対応するかを格納します。
         * </p>
         */
        private SparseIntArray mPMT = new SparseIntArray();

        /**
         * 指定された PID が PAT に含まれているか確認します.
         *
         * 含まれている場合は、PMT として処理を行います。
         *
         * @param pid PID
         * @return 含まれている場合はtrue、それ以外はfalse;
         */
        boolean containPMT(int pid) {
            return mPMT.indexOfKey(pid) >= 0;
        }

        /**
         * PID と program number を登録します.
         *
         * @param pid PID
         * @param programNumber プログラムナンバー
         */
        void put(int pid, int programNumber) {
            mPMT.put(pid, programNumber);
        }
    }

    /**
     * PMT の情報を格納するクラス.
     */
    private static class PMT {
        /**
         * PMT 情報を格納する Map.
         *
         * <p>
         * PES が、どのストリームタイプに対応するかを格納します。
         *
         * - key: PID (PESを識別するID)
         * - value: ストリームタイプ
         * </p>
         */
        private SparseIntArray mStreamType = new SparseIntArray();

        /**
         * PMT に PID とストリームタイプを登録します.
         *
         * @param pid PID
         * @param streamType ストリームタイプ
         */
        void put(int pid, int streamType) {
            mStreamType.put(pid, streamType);
        }

        /**
         * PID に対応するストリームタイプを取得します.
         *
         * @param pid PID
         * @return ストリームタイプ
         */
        int getStreamType(int pid) {
            return mStreamType.get(pid, -1);
        }

        /**
         * PMT のデータをクリアします.
         */
        void clear() {
            mStreamType.clear();
        }
    }

    /**
     * PES データを格納しておくクラス.
     */
    private static class PES {
        private int mPID;
        private int mStreamId;
        private long mPts;
        private long mDts;
        private byte[] mData;
        private int mDataLength;

        PES() {
            mData = new byte[4096];
        }

        /**
         * PES データの PID を取得します.
         *
         * @return PID
         */
        int getPID() {
            return mPID;
        }

        /**
         * PES データの PID を設定します.
         *
         * @param PID PID
         */
        void setPID(int PID) {
            mPID = PID;
        }

        /**
         * ストリームIDを取得します.
         *
         * @return ストリームID
         */
        int getStreamId() {
            return mStreamId;
        }

        /**
         * ストリームIDを設定します.
         *
         * @param streamId ストリームID
         */
        void setStreamId(int streamId) {
            mStreamId = streamId;
        }

        /**
         * Presentation timestamp を取得します.
         *
         * @return Presentation timestamp
         */
        long getPts() {
            return mPts;
        }

        /**
         * Presentation timestamp を設定します.
         *
         * @param pts Presentation timestamp
         */
        void setPts(long pts) {
            mPts = pts;
        }

        /**
         * Decode timestamp を取得します.
         *
         * @return Decode timestamp
         */
        long getDts() {
            return mDts;
        }

        /**
         * Decode timestamp を設定します.
         *
         * @param dts Decode timestamp
         */
        void setDts(long dts) {
            mDts = dts;
        }

        /**
         * 格納していたデータをリセットします.
         */
        void reset() {
            mDataLength = 0;
            mPts = 0;
        }

        /**
         * 格納したデータサイズを取得します.
         *
         * @return 格納したデータサイズ
         */
        int size() {
            return mDataLength;
        }

        /**
         * 格納したデータを取得します.
         *
         * @return 格納したデータ
         */
        byte[] toByteArray() {
            return mData;
        }

        /**
         * データを追加します.
         *
         * @param data   追加するデータ
         * @param offset 追加するデータのオフセット
         * @param length 追加するデータのサイズ
         */
        void write(byte[] data, int offset, int length) {
            int newLength = mDataLength + length;
            if (mData.length < newLength) {
                byte[] newData = new byte[newLength];
                System.arraycopy(mData, 0, newData, 0, mDataLength);
                mData = newData;
            }
            System.arraycopy(data, offset, mData, mDataLength, length);
            mDataLength = newLength;
        }
    }

    // TODO 他の PSI（Program Specific Information）も通知する必要がある

    /**
     * TS パケットに格納されていたストリームデータを通知するコールバック.
     */
    public interface Callback {
        /**
         * ストリームデータを通知します.
         *
         * @param streamId ストリーム ID
         * @param data データ
         * @param dataLength データサイズ
         * @param pts PTS
         */
        void onByteStream(int streamId, byte[] data, int dataLength, long pts);
    }
}

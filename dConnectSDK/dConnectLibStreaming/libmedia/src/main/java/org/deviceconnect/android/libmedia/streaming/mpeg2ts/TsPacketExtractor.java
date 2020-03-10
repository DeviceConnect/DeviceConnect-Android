package org.deviceconnect.android.libmedia.streaming.mpeg2ts;

import org.deviceconnect.android.libmedia.streaming.util.QueueThread;

/**
 * TS パケットからストリームを取り出すためのクラス.
 */
public class TsPacketExtractor extends QueueThread<Buffer> {
    /**
     * TS パケットから取得したストリームを通知するコールバック.
     */
    private Callback mCallback;

    /**
     * 停止フラグ.
     */
    private boolean mStopFlag;

    /**
     * コンストラクタ.
     */
    public TsPacketExtractor() {
        setName("TS-EXTRACTOR");
    }

    /**
     * TS パケットから取得したストリームを通知するコールバックを設定します.
     *
     * @param callback コールバック
     */
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * TS パケットのデータを追加します.
     *
     * @param data データ
     * @param dataLength データサイズ
     */
    public void add(byte[] data, int dataLength) {
        // TODO 常に new しているので、GC が発生しやすい。
        //      オブジェクトを使い回すようにすること。
        add(new Buffer(data, dataLength));
    }

    /**
     * TS パケットの解析を終了します.
     */
    public void terminate() {
        mStopFlag = true;

        interrupt();

        try {
            join(200);
        } catch (InterruptedException e) {
            // ignore.
        }
    }

    @Override
    public void run() {
        try {
            TsPacketReader packetReader = new TsPacketReader();
            packetReader.setCallback(mCallback);

            while (!mStopFlag) {
                packetReader.readPacket(get());
            }
        } catch (Exception e) {
            // ignore.
        }
    }

    /**
     * ストリームから抽出した TS パケット通知するリスナー.
     */
    public interface Callback extends TsPacketReader.Callback {
    }
}

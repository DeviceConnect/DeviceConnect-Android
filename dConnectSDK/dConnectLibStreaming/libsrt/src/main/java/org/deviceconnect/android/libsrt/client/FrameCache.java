package org.deviceconnect.android.libsrt.client;

import android.util.Log;

import org.deviceconnect.android.libsrt.BuildConfig;

import java.util.ArrayList;
import java.util.List;

public class FrameCache {
    /**
     * デバッグ用フラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * デバッグ用タグ.
     */
    private static final String TAG = "SRT-PLAYER";

    /**
     * 使い回す Frame のリスト.
     */
    private final List<Frame> mCacheFrames = new ArrayList<>();

    /**
     * Frame を格納するリストを初期化します.
     */
    public void initFrames() {
        synchronized (mCacheFrames) {
            mCacheFrames.clear();
            for (int i = 0; i < 150; i++) {
                mCacheFrames.add(new Frame(i));
            }
        }
    }

    /**
     * 未使用の Frame にデータを設定して取得します.
     * <p>
     * 未使用の Frame が見つからない場合は、null を返却します。
     * </p>
     * @param data データ
     * @param dataLength データサイズ
     * @param pts PTS
     * @return 未使用の Frame
     */
    public Frame getFrame(byte[] data, int dataLength, long pts) {
        Frame frame = getFreeFrame(dataLength);
        if (frame == null) {
            return null;
        }
        frame.setData(data, dataLength);
        frame.setPTS(pts);
        return frame;
    }

    /**
     * 未使用の Frame にデータを設定して取得します.
     * <p>
     * 未使用の Frame が見つからない場合は、null を返却します。
     * </p>
     * @param data データ
     * @param offset オフセット
     * @param dataLength データサイズ
     * @param pts PTS
     * @return 未使用の Frame
     */
    public Frame getFrame(byte[] data, int offset, int dataLength, long pts) {
        Frame frame = getFreeFrame(dataLength);
        if (frame == null) {
            return null;
        }
        frame.setData(data, offset, dataLength);
        frame.setPTS(pts);
        return frame;
    }

    /**
     * 未使用の Frame を取得します.
     * <p>
     * 未使用の Frame が見つからない場合は、null を返却します。
     * </p>
     * @param length データサイズ
     * @return 未使用の Frame
     */
    private Frame getFreeFrame(int length) {
        synchronized (mCacheFrames) {
            Frame selectFrame = null;

            for (Frame frame : mCacheFrames) {
                if (!frame.isConsumable() && frame.getBufferLength() > length) {
                    if (selectFrame == null || frame.getBufferLength() < selectFrame.getBufferLength()) {
                        selectFrame = frame;
                    }
                }
            }

            if (selectFrame != null) {
                selectFrame.consume();
                return selectFrame;
            }

            // バッファサイズよりも大きい Frame がない場合には
            // とりあえず、未使用の Frame を返却します
            for (Frame frame : mCacheFrames) {
                if (!frame.isConsumable()) {
                    frame.consume();
                    return frame;
                }
            }
        }

        if (DEBUG) {
            Log.e(TAG, "No free frame.");
        }
        return null;
    }

    /**
     * 全てのフレームの使用フラグを false に設定します.
     */
    public void releaseAllFrames() {
        synchronized (mCacheFrames) {
            for (Frame frame : mCacheFrames) {
                frame.release();
            }
        }
    }

    /**
     * Frame を格納するリストを解放します.
     */
    public void freeFrames() {
        synchronized (mCacheFrames) {
            mCacheFrames.clear();
        }
    }
}

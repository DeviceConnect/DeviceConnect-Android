package org.deviceconnect.android.libuvc;

import java.util.ArrayList;
import java.util.List;

/**
 * フレームバッファを提供するクラス.
 */
class FrameProvider {
    /**
     * プレビューのfレームバッファを格納するバッファ.
     */
    private final List<Frame> mFrames = new ArrayList<>();

    /**
     * フレームバッファを初期化します.
     *
     * @param parameter フレームバッファに渡すパラメータ
     */
    void init(Parameter parameter) {
        init(parameter, 10);
    }

    /**
     * フレームバッファを初期化します.
     *
     * @param parameter フレームバッファに渡すパラメータ
     * @param size フレームバッファの個数
     */
    void init(Parameter parameter, int size) {
        synchronized (mFrames) {
            mFrames.clear();
            for (int i = 0; i < size; i++) {
                mFrames.add(new Frame(parameter, i));
            }
        }
    }

    /**
     * 使用されていないフレームバッファを取得します.
     *
     * 全てのフレームバッファが使用されている場合は null を返却します。
     *
     * @param length フレームバッファのサイズ
     * @return フレームバッファ
     */
    Frame getFreeFrame(int length) {
        synchronized (mFrames) {
            for (Frame frame : mFrames) {
                if (!frame.isConsumable()) {
                    // フレームバッファのサイズが足りない場合はリサイズ
                    frame.resizeBuffer(length);
                    frame.consume();
                    return frame;
                }
            }
        }
        return null;
    }

    /**
     * 指定された ID のフレームバッファを取得します.
     *
     * @param id フレームバッファのID
     * @return フレームバッファ
     */
    Frame getFrameById(int id) {
        synchronized (mFrames) {
            return mFrames.get(id);
        }
    }

    /**
     * フレームバッファを解放します.
     */
    void clear() {
        mFrames.clear();
        System.gc();
    }

    /**
     * フレームバッファが空か確認します.
     *
     * @return 空の場合は true、それ以外はfalse
     */
    boolean isEmpty() {
        return mFrames.isEmpty();
    }
}

package org.deviceconnect.android.deviceplugin.host.recorder.util;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * オーバーレイに表示する View の管理を行うクラス.
 */
public class OverlayManager {

    /**
     * このクラスが属するコンテキスト.
     */
    private Context mContext;

    /**
     * Android の Window 管理クラス.
     */
    private WindowManager mWindowManager;

    /**
     * ディスプレイの解像度.
     */
    private final Point mDisplaySize = new Point();

    /**
     * ディスプレイの横幅の半分の値.
     */
    private int mDisplayWidthHalf;

    /**
     * ディスプレイの縦幅の半分の値.
     */
    private int mDisplayHeightHalf;

    /**
     * オーバーレイに追加した View を格納するマップ.
     */
    private final List<View> mViewList = new ArrayList<>();

    /**
     * コンストラクタ.
     *
     * @param context このクラスが属するコンテキスト
     */
    public OverlayManager(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (mWindowManager == null) {
            throw new RuntimeException("WindowManager is not supported.");
        }
        update();
    }

    /**
     * コンテキストを取得します.
     * @return コンテキスト
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * 画面サイズの更新処理を行います.
     *
     * <p>
     * 画面が回転した場合などに呼び出して、画面サイズを更新します。
     * </p>
     */
    public void update() {
        Display display = mWindowManager.getDefaultDisplay();
        display.getSize(mDisplaySize);

        mDisplayWidthHalf = mDisplaySize.x / 2;
        mDisplayHeightHalf = mDisplaySize.y / 2;
    }

    /**
     * ディスプレイの横幅を取得します.
     *
     * @return ディスプレイの横幅
     */
    public int getDisplayWidth() {
        return mDisplaySize.x;
    }

    /**
     * ディスプレイの縦幅を取得します.
     *
     * @return ディスプレイの縦幅
     */
    public int getDisplayHeight() {
        return mDisplaySize.y;
    }

    /**
     * 指定されたタグ名の View が存在するか確認します.
     *
     * @param name タグ名
     * @return 存在する場合はtrue、それ以外はfalse
     */
    public boolean hasViewByTag(String name) {
        return getViewByTag(name) != null;
    }

    /**
     * タグ名が一致する View を取得します.
     *
     * @param name タグ名
     * @return タグ名が一致した View
     */
    public View getViewByTag(String name) {
        synchronized (mViewList) {
            for (View view : mViewList) {
                OverlayTag overlayTag = (OverlayTag) view.getTag();
                if (name.equals(overlayTag.mName)) {
                    return view;
                }
            }
        }
        return null;
    }

    /**
     * View をオーバーレイに追加します.
     *
     * <p>
     * 同じタグ名の View が追加されている場合には、
     * 以前の View を削除してから View を追加します。
     * </p>
     *
     * @param view 追加する View
     * @param x    追加する View を配置する x 座標
     * @param y    追加する View を配置する y 座標
     * @param w    追加する View の横幅
     * @param h    追加する View の縦幅
     * @param name 追加する View のタグ名
     */
    public void addView(View view, int x, int y, int w, int h, String name) {
        if (hasViewByTag(name)) {
            removeView(name);
        }

        WindowManager.LayoutParams params = createLayoutParams(w, h);

        params.x = x - mDisplayWidthHalf + w / 2;
        params.y = y - mDisplayHeightHalf + h / 2;

        view.setTag(new OverlayTag(name, x, y, w, h));

        mWindowManager.addView(view, params);

        synchronized (mViewList) {
            mViewList.add(view);
        }
    }

    /**
     * タグ名に対応する View を配置を更新します.
     *
     * @param name 配置を変更する View のタグ名
     * @param x 新しい x 座標
     * @param y 新しい y 座標
     * @param w 新しい横幅
     * @param h 新しい縦幅
     */
    public void updateView(String name, int x, int y, int w, int h) {
        updateView(getViewByTag(name), x, y, w, h);
    }

    /**
     * View を配置を更新します.
     *
     * @param view 配置を変更する View
     * @param x 新しい x 座標
     * @param y 新しい y 座標
     * @param w 新しい横幅
     * @param h 新しい縦幅
     */
    public void updateView(View view, int x, int y, int w, int h) {
        if (view == null) {
            return;
        }

        WindowManager.LayoutParams params = createLayoutParams(w, h);

        params.x = x - mDisplayWidthHalf + w / 2;
        params.y = y - mDisplayHeightHalf + h / 2;

        OverlayTag overlayTag = (OverlayTag) view.getTag();
        overlayTag.update(x, y, w, h);

        mWindowManager.updateViewLayout(view, params);
    }

    /**
     * タグ名に対応する View をオーバーレイから削除します.
     *
     * @param name 削除する View のタグ名
     */
    public void removeView(String name) {
       removeView(getViewByTag(name));
    }

    /**
     * View をオーバーレイから削除します.
     *
     * @param view 削除する View
     */
    public void removeView(View view) {
        if (view == null) {
            return;
        }

        synchronized (mViewList) {
            mViewList.remove(view);
        }

        try {
            mWindowManager.removeViewImmediate(view);
        } catch (Exception e) {
            // ignore.
        }
    }

    /**
     * オーバーレイに追加されている全ての View を削除します。
     */
    public void removeAllViews() {
        List<View> views = getViewList();
        for (View view : views) {
            removeView(view);
        }
        synchronized (mViewList) {
            mViewList.clear();
        }
    }

    /**
     * オーバーレイに追加されている View のリストを取得します.
     *
     * @return View のリスト
     */
    private List<View> getViewList() {
        synchronized (mViewList) {
            return new ArrayList<>(mViewList);
        }
    }

    /**
     * オーバーレイ用の WindowManager.LayoutParams を作成します.
     *
     * @param w 横幅
     * @param h 縦幅
     * @return WindowManager.LayoutParams のインスタンス
     */
    private WindowManager.LayoutParams createLayoutParams(int w, int h) {
        int type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        return new WindowManager.LayoutParams(
                w, h, type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
    }

    private static class OverlayTag {
        private String mName;
        private int mX;
        private int mY;
        private int mW;
        private int mH;

        OverlayTag(String name, int x, int y, int w, int h) {
            mName = name;
            mX = x;
            mY = y;
            mW = w;
            mH = h;
        }
        void update(int x, int y, int w, int h) {
            mX = x;
            mY = y;
            mW = w;
            mH = h;
        }
    }
}

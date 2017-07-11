package org.deviceconnect.android.deviceplugin.hogp.activity;

import android.os.Bundle;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ToggleButton;

import org.deviceconnect.android.deviceplugin.hogp.BuildConfig;
import org.deviceconnect.android.deviceplugin.hogp.HOGPMessageService;
import org.deviceconnect.android.deviceplugin.hogp.R;
import org.deviceconnect.android.deviceplugin.hogp.server.MouseHOGPServer;
import org.deviceconnect.android.deviceplugin.hogp.util.KeyboardCode;

import java.util.HashMap;

public class HOGPControlActivity extends HOGPBaseActivity {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "HOGP";

    /**
     * ジェスチャー検出器.
     */
    private GestureDetector mGestureDetector;

    /**
     * ドラッグ状態.
     * <p>
     * ドラッグ中の場合はtrue、それ以外はfalse
     * </p>
     */
    private boolean mDragFlag;

    /**
     * 最後にタッチされたMotionEventの座標.
     */
    private float mLastX, mLastY;

    /**
     * MotionEventの最大個数.
     */
    private int mMaxPointerCount;

    /**
     * HOGPサーバ.
     */
    private MouseHOGPServer mMouseHOGPServer;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        mGestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(final MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(final MotionEvent e) {
            }

            @Override
            public boolean onSingleTapUp(final MotionEvent e) {
                if (mMouseHOGPServer != null) {
                    mMouseHOGPServer.movePointer(0, 0, 0, true, false, false);
                    mMouseHOGPServer.movePointer(0, 0, 0, false, false, false);
                }
                return false;
            }

            @Override
            public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(final MotionEvent e) {
                mDragFlag = true;

                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(80);

                if (mMouseHOGPServer != null) {
                    mMouseHOGPServer.movePointer((int) (e.getX() - mLastX), (int) (e.getY() - mLastY), 0, true, false, false);
                }

                mLastX = e.getX();
                mLastY = e.getY();
            }

            @Override
            public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
                return false;
            }
        });

        findViewById(R.id.activity_control_mouse).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent motionEvent) {
                mGestureDetector.onTouchEvent(motionEvent);
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        mDragFlag = false;
                        mMaxPointerCount = motionEvent.getPointerCount();
                        mLastX = motionEvent.getX();
                        mLastY = motionEvent.getY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        mMaxPointerCount = Math.max(mMaxPointerCount, motionEvent.getPointerCount());
                        if (mMouseHOGPServer != null) {
                            mMouseHOGPServer.movePointer((int) (motionEvent.getX() - mLastX), (int) (motionEvent.getY() - mLastY), 0, mDragFlag, false, false);
                        }
                        mLastX = motionEvent.getX();
                        mLastY = motionEvent.getY();
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        mDragFlag = false;
                        if (mMouseHOGPServer != null) {
                            mMouseHOGPServer.movePointer((int) (motionEvent.getX() - mLastX), (int) (motionEvent.getY() - mLastY), 0, false, false, false);
                        }
                        mLastX = motionEvent.getX();
                        mLastY = motionEvent.getY();
                        return true;
                }
                return true;
            }
        });


        final HashMap<Integer, String> map = new HashMap<Integer, String>() {
            { put(R.id.activity_control_key_0, "0"); }
            { put(R.id.activity_control_key_1, "1"); }
            { put(R.id.activity_control_key_2, "2"); }
            { put(R.id.activity_control_key_3, "3"); }
            { put(R.id.activity_control_key_4, "4"); }
            { put(R.id.activity_control_key_5, "5"); }
            { put(R.id.activity_control_key_6, "6"); }
            { put(R.id.activity_control_key_7, "7"); }
            { put(R.id.activity_control_key_8, "8"); }
            { put(R.id.activity_control_key_9, "9"); }
            { put(R.id.activity_control_key_a, "a"); }
            { put(R.id.activity_control_key_b, "b"); }
            { put(R.id.activity_control_key_c, "c"); }
            { put(R.id.activity_control_key_d, "d"); }
            { put(R.id.activity_control_key_e, "e"); }
            { put(R.id.activity_control_key_f, "f"); }
            { put(R.id.activity_control_key_g, "g"); }
            { put(R.id.activity_control_key_h, "h"); }
            { put(R.id.activity_control_key_i, "i"); }
            { put(R.id.activity_control_key_j, "j"); }
            { put(R.id.activity_control_key_k, "k"); }
            { put(R.id.activity_control_key_l, "l"); }
            { put(R.id.activity_control_key_m, "m"); }
            { put(R.id.activity_control_key_n, "n"); }
            { put(R.id.activity_control_key_o, "o"); }
            { put(R.id.activity_control_key_p, "p"); }
            { put(R.id.activity_control_key_q, "q"); }
            { put(R.id.activity_control_key_r, "r"); }
            { put(R.id.activity_control_key_s, "s"); }
            { put(R.id.activity_control_key_t, "t"); }
            { put(R.id.activity_control_key_u, "u"); }
            { put(R.id.activity_control_key_v, "v"); }
            { put(R.id.activity_control_key_w, "w"); }
            { put(R.id.activity_control_key_x, "x"); }
            { put(R.id.activity_control_key_y, "y"); }
            { put(R.id.activity_control_key_z, "z"); }
        };

        for (final Integer key : map.keySet()) {
            findViewById(key).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    String code = map.get(key);
                    if (mMouseHOGPServer != null) {
                        mMouseHOGPServer.sendKeyDown(caps(), KeyboardCode.keyCode(code));
                        mMouseHOGPServer.sendKeyUp();
                    }
                }
            });
        }

        findViewById(R.id.activity_control_key_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mMouseHOGPServer != null) {
                    mMouseHOGPServer.sendKeyDown((byte) 0, (byte) 0x2A);
                    mMouseHOGPServer.sendKeyUp();
                }
            }
        });

        findViewById(R.id.activity_control_key_space).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mMouseHOGPServer != null) {
                    mMouseHOGPServer.sendKeyDown((byte) 0, (byte) 0x2C);
                    mMouseHOGPServer.sendKeyUp();
                }
            }
        });

        findViewById(R.id.activity_control_key_enter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mMouseHOGPServer != null) {
                    mMouseHOGPServer.sendKeyDown((byte) 0, (byte) 0x28);
                    mMouseHOGPServer.sendKeyUp();
                }
            }
        });

    }
    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mMouseHOGPServer != null) {
                mMouseHOGPServer.sendKeyDown((byte) 0, (byte) 0x29);
                mMouseHOGPServer.sendKeyUp();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    void onServiceConnected() {
        mMouseHOGPServer = getHOGPServer();
    }

    @Override
    void onServiceDisconnected() {
        mMouseHOGPServer = null;
    }

    /**
     * Capsロックを確認します.
     * @return Capsロック
     */
    private byte caps() {
        ToggleButton toggle = (ToggleButton) findViewById(R.id.activity_control_key_caps);
        return toggle.isChecked() ? (byte) KeyboardCode.MODIFIER_KEY_SHIFT : 0;
    }

    /**
     * HOGPサーバを取得します.
     * <p>
     * HOGPサーバが開始されていない場合にはnullを返却します.
     * </p>
     * @return HOGPサーバ
     */
    private MouseHOGPServer getHOGPServer() {
        HOGPMessageService s = getHOGPMessageService();
        if (s != null) {
            return (MouseHOGPServer) s.getHOGPServer();
        }
        return null;
    }
}

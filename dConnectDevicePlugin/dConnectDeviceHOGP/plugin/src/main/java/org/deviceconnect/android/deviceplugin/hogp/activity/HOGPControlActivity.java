package org.deviceconnect.android.deviceplugin.hogp.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ToggleButton;

import org.deviceconnect.android.deviceplugin.hogp.R;
import org.deviceconnect.android.deviceplugin.hogp.server.HOGPServer;
import org.deviceconnect.android.deviceplugin.hogp.util.KeyboardCode;

/**
 * コントローラ画面用Activity.
 */
public class HOGPControlActivity extends HOGPBaseActivity {
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
    private HOGPServer mHOGPServer;

    /**
     * MotionEventを送信した時間.
     */
    private long mTime;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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
                if (!mDragFlag && mHOGPServer != null) {
                    mHOGPServer.movePointer(0, 0, 0, true, false, false);
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    mHOGPServer.movePointer(0, 0, 0, false, false, false);
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

                if (mHOGPServer != null) {
                    mHOGPServer.movePointer((int) (e.getX() - mLastX), (int) (e.getY() - mLastY), 0, true, false, false);
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
                        mTime = 0;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (System.currentTimeMillis() - mTime < 10) {
                            return true;
                        }
                        mMaxPointerCount = Math.max(mMaxPointerCount, motionEvent.getPointerCount());
                        if (mHOGPServer != null) {
                            mHOGPServer.movePointer(
                                    (int) (motionEvent.getX() - mLastX),
                                    (int) (motionEvent.getY() - mLastY),
                                    0,
                                    mDragFlag, false, false);
                        }
                        mLastX = motionEvent.getX();
                        mLastY = motionEvent.getY();
                        mTime = System.currentTimeMillis();
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        mDragFlag = false;
                        if (mHOGPServer != null) {
                            mHOGPServer.movePointer(
                                    (int) (motionEvent.getX() - mLastX),
                                    (int) (motionEvent.getY() - mLastY),
                                    0,
                                    false, false, false);
                        }
                        mLastX = motionEvent.getX();
                        mLastY = motionEvent.getY();
                        return true;
                }
                return true;
            }
        });

        final int[][] keyMap = {
                { R.id.activity_control_key_1, 0, 0x00, 0x1E },
                { R.id.activity_control_key_2, 0, 0x00, 0x1F },
                { R.id.activity_control_key_3, 0, 0x00, 0x20 },
                { R.id.activity_control_key_4, 0, 0x00, 0x21 },
                { R.id.activity_control_key_5, 0, 0x00, 0x22 },
                { R.id.activity_control_key_6, 0, 0x00, 0x23 },
                { R.id.activity_control_key_7, 0, 0x00, 0x24 },
                { R.id.activity_control_key_8, 0, 0x00, 0x25 },
                { R.id.activity_control_key_9, 0, 0x00, 0x26 },
                { R.id.activity_control_key_0, 0, 0x00, 0x27 },

                { R.id.activity_control_key_a, 0, 0x00, 0x04 },
                { R.id.activity_control_key_b, 0, 0x00, 0x05 },
                { R.id.activity_control_key_c, 0, 0x00, 0x06 },
                { R.id.activity_control_key_d, 0, 0x00, 0x07 },
                { R.id.activity_control_key_e, 0, 0x00, 0x08 },
                { R.id.activity_control_key_f, 0, 0x00, 0x09 },
                { R.id.activity_control_key_g, 0, 0x00, 0x0A },
                { R.id.activity_control_key_h, 0, 0x00, 0x0B },
                { R.id.activity_control_key_i, 0, 0x00, 0x0C },
                { R.id.activity_control_key_j, 0, 0x00, 0x0D },
                { R.id.activity_control_key_k, 0, 0x00, 0x0E },
                { R.id.activity_control_key_l, 0, 0x00, 0x0F },
                { R.id.activity_control_key_m, 0, 0x00, 0x10 },
                { R.id.activity_control_key_n, 0, 0x00, 0x11 },
                { R.id.activity_control_key_o, 0, 0x00, 0x12 },
                { R.id.activity_control_key_p, 0, 0x00, 0x13 },
                { R.id.activity_control_key_q, 0, 0x00, 0x14 },
                { R.id.activity_control_key_r, 0, 0x00, 0x15 },
                { R.id.activity_control_key_s, 0, 0x00, 0x16 },
                { R.id.activity_control_key_t, 0, 0x00, 0x17 },
                { R.id.activity_control_key_u, 0, 0x00, 0x18 },
                { R.id.activity_control_key_v, 0, 0x00, 0x19 },
                { R.id.activity_control_key_w, 0, 0x00, 0x1A },
                { R.id.activity_control_key_x, 0, 0x00, 0x1B },
                { R.id.activity_control_key_y, 0, 0x00, 0x1C },
                { R.id.activity_control_key_z, 0, 0x00, 0x1D },

                { R.id.activity_control_key_esc,    1, 0x00, 0x29 },
                { R.id.activity_control_key_up,     1, 0x00, 0x52 },
                { R.id.activity_control_key_down,   1, 0x00, 0x51 },
                { R.id.activity_control_key_left,   1, 0x00, 0x50 },
                { R.id.activity_control_key_right,  1, 0x00, 0x4F },
                { R.id.activity_control_key_delete, 1, 0x00, 0x2A },
                { R.id.activity_control_key_space,  1, 0x00, 0x2C },
                { R.id.activity_control_key_enter,  1, 0x00, 0x28 },

                { R.id.activity_control_key_mode,   1, 0x02, 0x2C },
                { R.id.activity_control_key_num,    1, 0x04, 0x2C },
        };

        for (int[] map : keyMap) {
            final boolean useShift = (map[1] == 0);
            final byte modifier = (byte) (map[2] & 0xFF);
            final byte keyCode = (byte) (map[3] & 0xFF);
            findViewById(map[0]).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mHOGPServer != null) {
                        if (useShift) {
                            mHOGPServer.sendKeyDown(caps(), keyCode);
                        } else {
                            mHOGPServer.sendKeyDown(modifier, keyCode);
                        }
                        mHOGPServer.sendKeyUp();
                    }
                }
            });
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    void onServiceConnected() {
        mHOGPServer = (HOGPServer) getHOGPServer();
    }

    @Override
    void onServiceDisconnected() {
        mHOGPServer = null;
    }

    /**
     * Capsロックを確認します.
     * @return Capsロック
     */
    private byte caps() {
        ToggleButton toggle = (ToggleButton) findViewById(R.id.activity_control_key_caps);
        return toggle.isChecked() ? (byte) KeyboardCode.MODIFIER_KEY_SHIFT : 0;
    }
}

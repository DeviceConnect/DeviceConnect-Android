/*
 HOGPControlActivity.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import org.deviceconnect.android.deviceplugin.hogp.R;
import org.deviceconnect.android.deviceplugin.hogp.server.HOGPServer;
import org.deviceconnect.android.deviceplugin.hogp.util.KeyboardCode;

import static android.view.MotionEvent.ACTION_MOVE;
import static org.deviceconnect.android.deviceplugin.hogp.server.HOGPServer.ABSOLUTE_MOUSE_SIZE;

/**
 * コントローラ画面用Activity.
 *
 * @author NTT DOCOMO, INC.
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
                    switch (mHOGPServer.getMouseMode()) {
                        case RELATIVE:
                            mHOGPServer.movePointer(0, 0, 0, true, false, false);
                            mHOGPServer.movePointer(0, 0, 0, false, false, false);
                            break;
                        case ABSOLUTE:
                            mHOGPServer.movePointer((int) mLastX, (int) mLastY, 0, true, false, false);
                            mHOGPServer.movePointer((int) mLastX, (int) mLastY, 0, false, false, false);
                            break;
                    }
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
            }

            @Override
            public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
                return false;
            }
        });

        findViewById(R.id.activity_control_mouse).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent motionEvent) {
                switch (mHOGPServer.getMouseMode()) {
                    case RELATIVE:
                        return moveRelativeMouse(motionEvent);
                    case ABSOLUTE:
                        return moveAbsoluteMouse(view, motionEvent);
                    default:
                        return true;
                }
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

                { R.id.activity_control_key_at_mark, 1, 0x00, 0x2F },
                { R.id.activity_control_key_hash,    1, 0x02, 0x20 },
                { R.id.activity_control_key_percent, 1, 0x02, 0x22 },
                { R.id.activity_control_key_yen,     1, 0x00, 0x89 },
                { R.id.activity_control_key_and,     1, 0x02, 0x23 },
                { R.id.activity_control_key_parentheses_start, 1, 0x02, 0x25 },
                { R.id.activity_control_key_parentheses_end,   1, 0x02, 0x26 },
                { R.id.activity_control_key_hyphen,    1, 0x00, 0x2D },
                { R.id.activity_control_key_slash,     1, 0x00, 0x38 },
                { R.id.activity_control_key_colon,     1, 0x00, 0x34 },
                { R.id.activity_control_key_semicolon, 1, 0x00, 0x33 },
                { R.id.activity_control_key_hat,       1, 0x00, 0x2E },
                { R.id.activity_control_key_exclamation_mark, 1, 0x02, 0x1E },
                { R.id.activity_control_key_question_mark, 1, 0x02, 0x38 },

                { R.id.activity_control_key_asterisk,      1, 0x02, 0x34 },
                { R.id.activity_control_key_add,           1, 0x02, 0x33 },
                { R.id.activity_control_key_lt,            1, 0x02, 0x36 },
                { R.id.activity_control_key_gt,            1, 0x02, 0x37 },
                { R.id.activity_control_key_bracket_start, 1, 0x00, 0x30 },
                { R.id.activity_control_key_bracket_end,   1, 0x00, 0x32 },
                { R.id.activity_control_key_under,         1, 0x02, 0x87 },
        };

        for (int[] map : keyMap) {
            final boolean useShift = (map[1] == 0);
            final byte modifier = (byte) (map[2] & 0xFF);
            final byte keyCode = (byte) (map[3] & 0xFF);
            findViewById(map[0]).setOnClickListener((v) -> {
                if (mHOGPServer != null) {
                    if (useShift) {
                        mHOGPServer.sendKeyDown(caps(), keyCode);
                    } else {
                        mHOGPServer.sendKeyDown(modifier, keyCode);
                    }
                    mHOGPServer.sendKeyUp();
                }
            });
        }

        findViewById(R.id.activity_control_key_num).setOnClickListener((v) -> {
            changeKeyboard();
        });

        ToggleButton toggle = findViewById(R.id.activity_control_key_caps);
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            changeUppercase(isChecked);
        });
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
     * Absolute入力モードのマウスの移動を処理します.
     * @param view タッチされたView
     * @param motionEvent タッチイベント
     * @return
     */
    private boolean moveAbsoluteMouse(final View view, final MotionEvent motionEvent) {
        float width = view.getWidth();
        float height = view.getHeight();

        mLastX = (int) (ABSOLUTE_MOUSE_SIZE * motionEvent.getX() / width);
        mLastY = (int) (ABSOLUTE_MOUSE_SIZE * motionEvent.getY() / height);

        mGestureDetector.onTouchEvent(motionEvent);
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                mDragFlag = false;
                if (mHOGPServer != null) {
                    mHOGPServer.movePointer(
                            (int) (ABSOLUTE_MOUSE_SIZE * motionEvent.getX() / width),
                            (int) (ABSOLUTE_MOUSE_SIZE * motionEvent.getY() / height),
                            0,
                            mDragFlag, false, false);
                }
                return true;

            case ACTION_MOVE:
                if (mHOGPServer != null) {
                    mHOGPServer.movePointer(
                            (int) (ABSOLUTE_MOUSE_SIZE * motionEvent.getX() / width),
                            (int) (ABSOLUTE_MOUSE_SIZE * motionEvent.getY() / height),
                            0,
                            mDragFlag, false, false);
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mDragFlag = false;
                if (mHOGPServer != null) {
                    mHOGPServer.movePointer(
                            (int) (ABSOLUTE_MOUSE_SIZE * motionEvent.getX() / width),
                            (int) (ABSOLUTE_MOUSE_SIZE * motionEvent.getY() / height),
                            0,
                            mDragFlag, false, false);
                }
                return true;
        }

        return false;
    }

    /**
     * Relative入力モードのマウスの移動を処理します.
     * @param motionEvent タッチイベント
     * @return
     */
    private boolean moveRelativeMouse(final MotionEvent motionEvent) {
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

            case ACTION_MOVE:
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

    /**
     * Capsロックを確認します.
     * @return Capsロック
     */
    private byte caps() {
        ToggleButton toggle = findViewById(R.id.activity_control_key_caps);
        return toggle.isChecked() ? (byte) KeyboardCode.MODIFIER_KEY_SHIFT : 0;
    }

    private void changeUppercase(final boolean flag) {
        int[] ids = {
                R.id.activity_control_key_a,
                R.id.activity_control_key_b,
                R.id.activity_control_key_c,
                R.id.activity_control_key_d,
                R.id.activity_control_key_e,
                R.id.activity_control_key_f,
                R.id.activity_control_key_g,
                R.id.activity_control_key_h,
                R.id.activity_control_key_i,
                R.id.activity_control_key_j,
                R.id.activity_control_key_k,
                R.id.activity_control_key_l,
                R.id.activity_control_key_m,
                R.id.activity_control_key_n,
                R.id.activity_control_key_o,
                R.id.activity_control_key_p,
                R.id.activity_control_key_q,
                R.id.activity_control_key_r,
                R.id.activity_control_key_s,
                R.id.activity_control_key_t,
                R.id.activity_control_key_u,
                R.id.activity_control_key_v,
                R.id.activity_control_key_w,
                R.id.activity_control_key_x,
                R.id.activity_control_key_y,
                R.id.activity_control_key_z,
        };

        for (int id : ids) {
            Button btn = (Button) findViewById(id);
            String t = btn.getText().toString();
            if (flag) {
                t = t.toUpperCase();
            } else {
                t = t.toLowerCase();
            }
            btn.setText(t);
        }
    }

    private void changeKeyboard() {
        View v1 = findViewById(R.id.activity_control_keyboard_a);
        View v2 = findViewById(R.id.activity_control_keyboard_s);
        if (v1.getVisibility() == View.GONE) {
            v1.setVisibility(View.VISIBLE);
            v2.setVisibility(View.GONE);
        } else {
            v1.setVisibility(View.GONE);
            v2.setVisibility(View.VISIBLE);
        }
    }
}

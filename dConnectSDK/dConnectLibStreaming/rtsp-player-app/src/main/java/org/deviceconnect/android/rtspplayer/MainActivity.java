package org.deviceconnect.android.rtspplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.deviceconnect.android.libmedia.streaming.rtsp.player.RtspPlayer;


public class MainActivity extends AppCompatActivity {

    private RtspPlayer mRtspPlayer;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener((View view) ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show());

        EditText editText = findViewById(R.id.edit_text_url);
        editText.setText("rtsp://192.168.1.75:10000");

        findViewById(R.id.btn_start).setOnClickListener((v) -> {
            hideSoftKeyboard(v);
            startPlayer();
        });

        findViewById(R.id.btn_stop).setOnClickListener((v) -> {
            hideSoftKeyboard(v);
            stopPlayer();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void hideSoftKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void startPlayer() {
        if (mRtspPlayer != null) {
            return;
        }

        if (mMediaPlayer != null) {
            return;
        }

        EditText urlEditText = findViewById(R.id.edit_text_url);
        String url = urlEditText.getText().toString();

        SurfaceView surfaceView = findViewById(R.id.surface_view);

//        try {
//            mMediaPlayer = new MediaPlayer();
//            mMediaPlayer.setDataSource(url);
//            mMediaPlayer.setSurface(surfaceView.getHolder().getSurface());
//            mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
//            mMediaPlayer.setOnPreparedListener((MediaPlayer mp) -> mMediaPlayer.start());
//            mMediaPlayer.prepare();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        mRtspPlayer = new RtspPlayer(url);
        mRtspPlayer.setSurface(surfaceView.getHolder().getSurface());
        mRtspPlayer.setOnEventListener(new RtspPlayer.OnEventListener() {
            @Override
            public void onError(Exception e) {
                Log.e("ABC", "###", e);
            }

            @Override
            public void onOpened() {
                Log.i("ABC", "### OPENED");
            }

            @Override
            public void onSizeChanged(int width, int height) {
                Log.e("ABC", "onSizeChanged: " + width + "x" + height);
                runOnUiThread(() -> {
                    changePlayerSize(width, height);
                });
            }
        });
        mRtspPlayer.start();
    }

    private void changePlayerSize(int pictureWidth, int pictureHeight) {
        View root = findViewById(R.id.root);
        SurfaceView surfaceView = findViewById(R.id.surface_view);

        Size changeSize;
        Size viewSize = new Size(root.getWidth(), root.getHeight());
        changeSize = calculateViewSize(pictureWidth, pictureHeight, viewSize);

        ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
        layoutParams.width = changeSize.getWidth();
        layoutParams.height = changeSize.getHeight();
        surfaceView.setLayoutParams(layoutParams);
    }

    protected Size calculateViewSize(int width, int height, Size maxSize) {
        int h = maxSize.getWidth() * height / width;
        if (h % 2 != 0) {
            h--;
        }
        if (maxSize.getHeight() < h) {
            int w = maxSize.getHeight() * width / height;
            if (w % 2 != 0) {
                w--;
            }
            return new Size(w, maxSize.getHeight());
        }
        return new Size(maxSize.getWidth(), h);
    }

    private void stopPlayer() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        if (mRtspPlayer != null) {
            mRtspPlayer.stop();
            mRtspPlayer = null;
        }
    }
}

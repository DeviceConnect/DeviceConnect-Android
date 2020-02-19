package org.deviceconnect.android.srt_player_app;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.deviceconnect.android.libsrt.client.SRTPlayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private SRTPlayer mSRTPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
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

    @Override
    protected void onPause() {
        stopSRTPlayer();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startSRTPlayer();
    }

    private void startSRTPlayer() {
        if (mSRTPlayer != null) {
            return;
        }

        SurfaceView surfaceView = findViewById(R.id.surface_view);

        mSRTPlayer = new SRTPlayer();
        mSRTPlayer.setSurface(surfaceView.getHolder().getSurface());
        mSRTPlayer.setOnEventListener(new SRTPlayer.OnEventListener() {
            @Override
            public void onSizeChanged(int width, int height) {

            }

            @Override
            public void onError(Exception e) {

            }
        });
        mSRTPlayer.start("192.168.1.79", 23456);
    }

    private void stopSRTPlayer() {
        if (mSRTPlayer != null) {
            mSRTPlayer.stop();
            mSRTPlayer = null;
        }
    }
}

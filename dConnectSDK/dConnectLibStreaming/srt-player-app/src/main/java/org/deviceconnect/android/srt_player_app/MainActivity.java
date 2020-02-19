package org.deviceconnect.android.srt_player_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private SRTSetting mSRTSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_srt_play).setOnClickListener((v) -> {
            EditText et = findViewById(R.id.edittext_srt_server_url);
            String uri = et.getText().toString();

            if (uri.isEmpty()) {
                return;
            }

            if (!uri.startsWith("srt://")) {
                return;
            }

            mSRTSetting.setServerUrl(uri);

            gotoSRTPlayer(uri);
        });

        mSRTSetting = new SRTSetting(this);

        String srtUrl = mSRTSetting.getServerUrl();
        if (srtUrl != null && !srtUrl.isEmpty()) {
            EditText et = findViewById(R.id.edittext_srt_server_url);
            et.setText(srtUrl);
        }
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

    private void gotoSRTPlayer(String uri) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), SRTPlayerActivity.class);
        intent.putExtra(SRTPlayerActivity.EXTRA_URI, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}

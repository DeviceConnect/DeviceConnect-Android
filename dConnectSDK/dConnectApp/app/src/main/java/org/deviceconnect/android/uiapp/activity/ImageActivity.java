package org.deviceconnect.android.uiapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;

import org.deviceconnect.android.uiapp.R;
import org.deviceconnect.message.DConnectMessage;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageActivity extends BasicActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        if (intent != null) {
            final String uri = intent.getStringExtra("uri");
            if (uri != null) {
                AsyncTask<Void, Bitmap, Bitmap> task = new AsyncTask<Void, Bitmap, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(Void... params) {
                        return createBitmap(uri);
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        if (bitmap != null) {
                            ImageView imageView = (ImageView) findViewById(R.id.image);
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                };
                task.execute();
            }
        }
    }

    private String getOrigin() {
        return getPackageName();
    }

    private Bitmap createBitmap(String uri) {
        return getBitmapFormUri(uri);
    }

    private Bitmap getBitmapFormUri(String uri) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(uri).openConnection();
            conn.setConnectTimeout(10 * 1000);
            conn.setReadTimeout(10 * 1000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(false);

            if (getOrigin() != null) {
                conn.setRequestProperty(DConnectMessage.HEADER_GOTAPI_ORIGIN, getOrigin());
            }

            if (Build.VERSION.SDK_INT > 13 && Build.VERSION.SDK_INT < 19) {
                conn.setRequestProperty("Connection", "close");
            }

            conn.connect();

            int resp = conn.getResponseCode();
            if (resp == 200) {
                return BitmapFactory.decodeStream(conn.getInputStream());
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}

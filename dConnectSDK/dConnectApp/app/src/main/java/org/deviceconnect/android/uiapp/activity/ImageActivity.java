package org.deviceconnect.android.uiapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;

import org.deviceconnect.android.uiapp.R;
import org.deviceconnect.android.uiapp.utils.MixedReplaceMediaClient;

import java.io.IOException;
import java.io.InputStream;

public class ImageActivity extends BasicActivity {

    private MixedReplaceMediaClient mMixedReplaceMediaClient;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if (intent != null) {
            final String uri = intent.getStringExtra("uri");
            if (uri != null) {
                getBitmap(uri);
            }
        }
    }

    @Override
    protected void onPause() {
        if (mMixedReplaceMediaClient != null) {
            mMixedReplaceMediaClient.stop();
        }
        super.onPause();
    }

    private String getOrigin() {
        return getPackageName();
    }

    private void getBitmap(final String uri) {
        if (uri.startsWith("content://")) {
            getBitmapForContentProvider(uri);
        } else {
            test(uri);
        }
    }

    private void getBitmapForContentProvider(final String uri) {
        AsyncTask<Void, Bitmap, Bitmap> task = new AsyncTask<Void, Bitmap, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                try {
                    return MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(uri));
                } catch (IOException e) {
                    return null;
                }
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

    private void test(final String uri) {
        mMixedReplaceMediaClient = new MixedReplaceMediaClient(uri);
        mMixedReplaceMediaClient.setOrigin(getOrigin());
        mMixedReplaceMediaClient.setOnMixedReplaceMediaListener(new MixedReplaceMediaClient.OnMixedReplaceMediaListener() {
            @Override
            public void onConnected() {
            }

            @Override
            public void onReceivedData(final InputStream in) {
                final Bitmap bitmap = BitmapFactory.decodeStream(in);
                if (bitmap != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ImageView imageView = (ImageView) findViewById(R.id.image);
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                }
            }

            @Override
            public void onError(MixedReplaceMediaClient.MixedReplaceMediaError error) {
            }
        });
        mMixedReplaceMediaClient.start();
    }
}

package org.deviceconnect.android.deviceplugin.theta.profile;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewRenderer;
import org.deviceconnect.android.deviceplugin.theta.utils.BitmapUtils;
import org.deviceconnect.message.DConnectMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


public class ImageViewer extends Viewer {

    private final String mOrigin;

    private Bitmap mTexture;

    public ImageViewer(final Context context) {
        mOrigin = context.getPackageName();
    }

    public void setImage(final String uri) throws IOException {
        InputStream is = null;
        try {
            URLConnection conn = new URL(uri).openConnection();
            conn.setRequestProperty(DConnectMessage.HEADER_GOTAPI_ORIGIN, mOrigin);
            is = conn.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            mTexture = BitmapUtils.resize(bitmap, 2048, 1024);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    @Override
    public void start() {
        SphericalViewRenderer renderer = mProjector.getRenderer();
        if (renderer == null) {
            throw new IllegalStateException("Renderer is not set.");
        }
        renderer.setTexture(mTexture);
        super.start();
    }
}

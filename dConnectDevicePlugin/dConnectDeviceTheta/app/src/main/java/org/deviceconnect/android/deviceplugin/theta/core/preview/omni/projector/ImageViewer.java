package org.deviceconnect.android.deviceplugin.theta.core.preview.omni.projector;


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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class ImageViewer extends Viewer {

    private final String mOrigin;

    private Bitmap mTexture;


    public ImageViewer(final Context context) {
        mOrigin = context.getPackageName();
    }

    public void setImage(final String uri) throws IOException {
        InputStream is = null;
        try {
            if (uri.startsWith("https")) {
                SSLContext sslContext = null;
                try {
                    //httpsの場合は証明書の確認を無視する
                    TrustManager[] tm = {
                            new X509TrustManager() {
                                public X509Certificate[] getAcceptedIssuers() {
                                    return null;
                                }
                                @Override
                                public void checkClientTrusted(X509Certificate[] chain,
                                                               String authType) throws CertificateException {
                                }
                                @Override
                                public void checkServerTrusted(X509Certificate[] chain,
                                                               String authType) throws CertificateException {
                                }
                            }
                    };
                    sslContext = SSLContext.getInstance("SSL");
                    sslContext.init(null, tm, null);
                    //ホスト名の検証ルール　何が来てもtrueを返す
                    HttpsURLConnection.setDefaultHostnameVerifier(
                            (hostname, session) -> {
                                return true;
                            }
                    );
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                HttpsURLConnection conn = (HttpsURLConnection) new URL(uri).openConnection();
                conn.setSSLSocketFactory(sslContext.getSocketFactory());
                conn.setRequestProperty(DConnectMessage.HEADER_GOTAPI_ORIGIN, mOrigin);
                is = conn.getInputStream();
            } else {
                URLConnection conn = new URL(uri).openConnection();
                conn.setRequestProperty(DConnectMessage.HEADER_GOTAPI_ORIGIN, mOrigin);
                is = conn.getInputStream();
            }

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

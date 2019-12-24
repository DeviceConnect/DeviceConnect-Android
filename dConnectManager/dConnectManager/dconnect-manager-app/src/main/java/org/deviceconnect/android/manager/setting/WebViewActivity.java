/*
 WebViewActivity.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.setting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import org.deviceconnect.android.manager.BuildConfig;
import org.deviceconnect.android.manager.R;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;

/**
 * サービス確認用のWebページを開くためのActivity.
 *
 * @author NTT DOCOMO, INC.
 */
public class WebViewActivity extends AppCompatActivity {

    /**
     * デバッグフフラグ.
     */
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * 表示するHTMLへのURLを格納するExtraのキーを定義する.
     */
    public static final String EXTRA_URL = "url";

    /**
     * 表示するタイトルを格納するExtraのキーを定義する.
     */
    public static final String EXTRA_TITLE = "title";

    /**
     * SSL通信フラグを格納するExtraのキーを定義する.
     */
    public static final String EXTRA_SSL = "ssl";

    /**
     * サービスIDを格納するExtraのキーを定義する.
     */
    public static final String EXTRA_SERVICE_ID = "serviceId";

    /**
     * プラグインIDを格納するExtraのキーを定義する.
     */
    public static final String EXTRA_PLUGIN_ID = "pluginId";

    /**
     * ファイル選択できるMimeTypeを定義する.
     */
    private static final String TYPE_IMAGE = "image/*";

    /**
     * ファイル選択が行われた時にリクエストコードを定義する.
     */
    private static final int INPUT_FILE_REQUEST_CODE = 1;

    /**
     * Javascriptファイルからファイル選択が行われた時のリクエストコードを定義する.
     */
    private static final int REQUEST_CODE_FROM_JS = 2;

    /**
     * SSL通信を有効にすることを示す定数.
     */
    public static final String SSL_ON = "on";

    /**
     * SSL通信を無効にすることを示す定数.
     */
    public static final String SSL_OFF = "off";

    /**
     * ファイル選択用のコールバック.
     * <p>
     * Android OS 5.0未満の端末では、こちらを使う。
     * </p>
     */
    private ValueCallback<Uri> mUploadMessage;

    /**
     * ファイル選択用のコールバック.
     * <p>
     * Android OS 5.0以上の端末では、こちらを使う。
     * </p>
     */
    private ValueCallback<Uri[]> mFilePathCallback;

    /**
     * Webページを開くためのWebView.
     */
    private WebView mWebView;

    /**
     * 一時中断フラグ.
     */
    private boolean mPauseFlag;

    /**
     * SSL通信フラグ.
     */
    private String mSSL;

    /**
     * ロガー.
     */
    private final Logger mLogger = Logger.getLogger("dconnect.manager");

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        String url = intent.getStringExtra(EXTRA_URL);
        if (url == null) {
            finish();
            return;
        }

        String title = intent.getStringExtra(EXTRA_TITLE);
        if (title != null) {
            setTitle(title);
        }

        boolean ssl = intent.getBooleanExtra(EXTRA_SSL, false);
        mSSL = ssl ? SSL_ON : SSL_OFF;

        mWebView = (WebView) findViewById(R.id.activity_web_view);
        if (mWebView != null) {
            mWebView.setWebViewClient(mWebViewClient);
            mWebView.setWebChromeClient(mChromeClient);
            mWebView.setVerticalScrollBarEnabled(false);
            mWebView.setHorizontalScrollBarEnabled(false);
            mWebView.addJavascriptInterface(new JavaScriptInterface(), "Android");

            WebSettings webSettings = mWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setDatabaseEnabled(true);
            webSettings.setAllowFileAccess(true);
            webSettings.setAppCacheEnabled(false);
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

            // Android 4.0以上では、chromeからデバッグするための機能が存在する
            // ここでは、DEBUGビルドの場合のみ使えるように設定している。
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (DEBUG) {
                    if (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
                        WebView.setWebContentsDebuggingEnabled(true);
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                webSettings.setAllowFileAccessFromFileURLs(true);
                webSettings.setAllowUniversalAccessFromFileURLs(true);
            }

            loadUrl(mWebView, url);
        }
    }

    private void loadUrl(final WebView view, final String url) {
        Uri uri = Uri.parse(url);
        if ("file".equals(uri.getScheme())) {
            uri = appendSSL(uri);
        }
        view.loadUrl(uri.toString());
    }

    private Uri appendSSL(final Uri uri) {
        try {
            Uri.Builder builder = uri.buildUpon();
            builder.appendQueryParameter("ssl", mSSL);
            return builder.build();
        } catch (UnsupportedOperationException e) {
            return uri;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.resumeTimers();
        }
        mPauseFlag = false;
    }

    @Override
    protected void onPause() {
        if (mWebView != null) {
            mWebView.pauseTimers();
        }
        mPauseFlag = true;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        Intent intent = getIntent();
        if (intent != null && intent.getStringExtra(EXTRA_SERVICE_ID) != null) {
            getMenuInflater().inflate(R.menu.activity_web_view, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            if (mWebView != null && mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                finish();
            }
            return true;
        } else if (R.id.activity_webview_menu_settings == item.getItemId()) {
            openPluginSettings();
        } else if (R.id.activity_webview_menu_top == item.getItemId()) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == INPUT_FILE_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mFilePathCallback == null) {
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }

                Uri[] results = null;
                if (resultCode == RESULT_OK) {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[] { Uri.parse(dataString) };
                    }
                }

                mFilePathCallback.onReceiveValue(results);
                mFilePathCallback = null;
            } else {
                if (mUploadMessage == null) {
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }

                Uri result = null;
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        result = data.getData();
                    }
                }

                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        } else if(requestCode == REQUEST_CODE_FROM_JS) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                cursor.close();
            }

            String imgPath = getPath(selectedImage);

            BitmapFactory.Options options;
            options = new BitmapFactory.Options();
            options.inSampleSize = 3;
            Bitmap bitmap = BitmapFactory.decodeFile(imgPath, options);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] b = stream.toByteArray();
            String encodedString = Base64.encodeToString(b, Base64.DEFAULT);
            mWebView.loadUrl("javascript:chooseImgResult(" + encodedString + ")");
        } else {
            super.onActivityResult(requestCode,resultCode, data);
        }
    }

    /**
     * プラグインの設定画面を開く.
     */
    private void openPluginSettings() {
        String pluginId = getIntent().getStringExtra(EXTRA_PLUGIN_ID);

        Intent intent = new Intent();
        intent.setClass(this, DevicePluginInfoActivity.class);
        intent.putExtra(DevicePluginInfoActivity.EXTRA_PLUGIN_ID, pluginId);
        startActivity(intent);
    }

    /**
     * ファイルパスを取得する.
     * @param uri URI
     * @return ファイルパス
     */
    private static String getPath(final Uri uri) {
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private final WebViewClient mWebViewClient = new WebViewClient() {
        /**
         * 画面のスケール計算を行うインターバルを定義する.
         */
        private static final long STABLE_SCALE_CALCULATION_DURATION = 1000;

        /**
         * 画面スケール計算を行った時間.
         */
        private long mStableScaleCalculationStart = System.currentTimeMillis();

        /**
         * 画面スケール.
         */
        private String mStableScale;

        /**
         * 画面スケールを戻す時の開始時間.
         */
        private long mRestoringScaleStart;

        @Override
        public void onReceivedSslError(final WebView view, final SslErrorHandler handler, final SslError error) {
            int primaryError = error.getPrimaryError();
            String url = error.getUrl();
            SslCertificate cert = error.getCertificate();
            mLogger.warning("onReceivedSslError: error = " + primaryError
                    + ", url = " + url + ", certificate = " + cert);

            if (primaryError == SslError.SSL_UNTRUSTED && url != null && cert != null) {
                SslCertificate.DName subjectName = cert.getIssuedTo();
                if (subjectName != null
                        && "localhost".equals(subjectName.getCName())
                        && url.startsWith("https://localhost") ) {
                    handler.proceed();
                    mLogger.warning("SSL Proceeded: url = " + url);
                    return;
                }
            }
            handler.cancel();
            mLogger.severe("SSL Canceled: url = " + url);
        }

        // 画面のスケールがユーザによって変えられた時に、元のスケールに戻す処理を行う.
        // Webページの方で、スケールが切り替えられないようにしているが、一部の端末(OS)で、
        // その設定が無視されることがあるので、この処理を入れる。
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
            if (url.startsWith("market://")) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }
            loadUrl(view, url);
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onScaleChanged(final WebView view, float oldScale, float newScale) {
            final long now = System.currentTimeMillis();
            if (mStableScale == null || (now - mStableScaleCalculationStart) < STABLE_SCALE_CALCULATION_DURATION) {
                mStableScale = "" + newScale;
            } else if (!mStableScale.equals("" + newScale)) {
                boolean zooming = (now - mRestoringScaleStart) < STABLE_SCALE_CALCULATION_DURATION;
                if (!zooming) {
                    mRestoringScaleStart = now;
                    view.zoomOut();
                    view.postDelayed(view::zoomOut
                    , STABLE_SCALE_CALCULATION_DURATION);
                }
            }
        }
    };

    private final WebChromeClient mChromeClient = new WebChromeClient() {
        @Override
        public boolean onJsAlert(final WebView view, final String url, final String message, final JsResult result) {
            if (mPauseFlag) {
                return super.onJsAlert(view, url, message, result);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
            builder.setTitle(R.string.app_name)
                    .setMessage(message)
                    .setCancelable(true)
                    .setPositiveButton(R.string.webview_js_alert_positive_btn, (dialog, which) -> {
                        dialog.dismiss();
                    });
            AlertDialog dialog = builder.create();
            dialog.setOnDismissListener((dialogs) -> {
                result.confirm();
            });
            dialog.show();
            return true;
        }

        @Override
        public boolean onJsConfirm(final WebView view, final String url, final String message, final JsResult result) {
            if (mPauseFlag) {
                return super.onJsConfirm(view, url, message, result);
            }
            final boolean[] flag = new boolean[1];
            AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
            builder.setTitle(R.string.app_name)
                    .setMessage(message)
                    .setCancelable(true)
                    .setPositiveButton(R.string.webview_js_alert_positive_btn, (dialog, which) -> {
                        flag[0] = true;
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.webview_js_alert_negative_btn, (dialog, which) -> {
                        flag[0] = false;
                        dialog.dismiss();
                    });
            AlertDialog dialog = builder.create();
            dialog.setOnDismissListener((dialogs) -> {
                if (flag[0]) {
                    result.confirm();
                } else {
                    result.cancel();
                }
            });
            dialog.show();
            return true;
        }

        @Override
        public boolean onJsPrompt(final WebView view, final String url, final String message, final String defaultValue, final JsPromptResult result) {
            if (mPauseFlag) {
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }
            final EditText editText = new EditText(WebViewActivity.this);
            final boolean[] flag = new boolean[1];
            AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
            builder.setTitle(R.string.app_name)
                    .setView(editText)
                    .setCancelable(true)
                    .setPositiveButton(R.string.webview_js_alert_positive_btn, (dialog, whichButton) -> {
                        flag[0] = true;
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.webview_js_alert_negative_btn, (dialog, whichButton) -> {
                        flag[0] = false;
                        dialog.dismiss();
                    });
            AlertDialog dialog = builder.create();
            dialog.setOnDismissListener((dialogs) -> {
                if (flag[0]) {
                    result.confirm(editText.getText().toString());
                } else {
                    result.cancel();
                }
            });
            dialog.show();
            return true;
        }

        // For Android 5.0+
        @Override
        public boolean onShowFileChooser(final WebView webView, final ValueCallback<Uri[]> filePathCallback,
                                         final FileChooserParams fileChooserParams) {
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePathCallback;

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(TYPE_IMAGE);
            startActivityForResult(intent, INPUT_FILE_REQUEST_CODE);

            return true;
        }
    };

    /**
     * Javascriptと連携するためのクラス.
     */
    private class JavaScriptInterface {
        /**
         * cookie保存用クラス.
         */
        private SharedPreferences mPref;

        JavaScriptInterface() {
            mPref = getSharedPreferences("__cookie.dat", Context.MODE_PRIVATE);
        }

        @JavascriptInterface
        public void setCookie(final String name, final String value) {
            SharedPreferences.Editor editor = mPref.edit();
            editor.putString(name, value);
            editor.commit();
        }

        @JavascriptInterface
        public String getCookie(final String name) {
            return mPref.getString(name, null);
        }

        @JavascriptInterface
        public void deleteCookie(final String name) {
            SharedPreferences.Editor editor = mPref.edit();
            editor.remove(name);
            editor.commit();
        }
    }
}

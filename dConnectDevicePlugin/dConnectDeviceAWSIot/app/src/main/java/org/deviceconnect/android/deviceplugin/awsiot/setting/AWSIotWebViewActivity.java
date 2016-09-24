/*
 AWSIotWebViewActivity.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.awsiot.setting;

import org.deviceconnect.android.deviceplugin.awsiot.remote.BuildConfig;
import org.deviceconnect.android.deviceplugin.awsiot.remote.R;

import android.app.ActionBar;
import android.app.Activity;
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
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;



import java.io.ByteArrayOutputStream;

public class AWSIotWebViewActivity extends Activity {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "Manager";

    public static final String EXTRA_URL = "url";
    public static final String EXTRA_TITLE = "title";

    private static final String TYPE_IMAGE = "image/*";
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final int REQUEST_CODE_FROM_JS = 2;

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mFilePathCallback;

    private WebView mWebView;

    private boolean mPauseFlag;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        ActionBar actionBar = getActionBar();
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

            mWebView.loadUrl(url);
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
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            finish();
            return true;
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

            String imgPath = getPath(this, selectedImage);

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

    private static String getPath(final Context context, final Uri uri) {
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private final WebViewClient mWebViewClient = new WebViewClient() {
        private static final long STABLE_SCALE_CALCULATION_DURATION = 1000;
        private long mStableScaleCalculationStart = System.currentTimeMillis();
        private String mStableScale;
        private long mRestoringScaleStart;

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
                    view.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            view.zoomOut();
                        }
                    }, STABLE_SCALE_CALCULATION_DURATION);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(AWSIotWebViewActivity.this);
            builder.setTitle(R.string.app_name)
                    .setMessage(message)
                    .setCancelable(true)
                    .setPositiveButton(R.string.webview_js_alert_positive_btn, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    result.confirm();
                }
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
            AlertDialog.Builder builder = new AlertDialog.Builder(AWSIotWebViewActivity.this);
            builder.setTitle(R.string.app_name)
                    .setMessage(message)
                    .setCancelable(true)
                    .setPositiveButton(R.string.webview_js_alert_positive_btn, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            flag[0] = true;
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.webview_js_alert_negative_btn, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            flag[0] = false;
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (flag[0]) {
                        result.confirm();
                    } else {
                        result.cancel();
                    }
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
            final EditText editText = new EditText(AWSIotWebViewActivity.this);
            final boolean[] flag = new boolean[1];
            AlertDialog.Builder builder = new AlertDialog.Builder(AWSIotWebViewActivity.this);
            builder.setTitle(R.string.app_name)
                    .setView(editText)
                    .setCancelable(true)
                    .setPositiveButton(R.string.webview_js_alert_positive_btn, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            flag[0] = true;
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.webview_js_alert_negative_btn, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            flag[0] = false;
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (flag[0]) {
                        result.confirm(editText.getText().toString());
                    } else {
                        result.cancel();
                    }
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

    private class JavaScriptInterface {
        private SharedPreferences mPref;

        JavaScriptInterface() {
            mPref = getSharedPreferences("__cookie.dat", Context.MODE_PRIVATE);
        }

        @JavascriptInterface
        public void setCookie(final String name, final String value) {
            SharedPreferences.Editor editor = mPref.edit();
            editor.putString(name, value);
            editor.apply();
        }

        @JavascriptInterface
        public String getCookie(final String name) {
            return mPref.getString(name, null);
        }

        @JavascriptInterface
        public void deleteCookie(final String name) {
            SharedPreferences.Editor editor = mPref.edit();
            editor.remove(name);
            editor.apply();
        }
    }
}

package org.deviceconnect.android.manager.setting;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import org.deviceconnect.android.manager.BuildConfig;
import org.deviceconnect.android.manager.R;

public class WebViewActivity extends Activity {
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "Manager";

    public static final String EXTRA_URL = "url";
    public static final String EXTRA_TITLE = "title";

    private WebView mWebView;

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
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
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
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            final boolean[] flag = new boolean[1];
            AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
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
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
            final EditText editText = new EditText(WebViewActivity.this);
            final boolean[] flag = new boolean[1];
            AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
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
    };
}

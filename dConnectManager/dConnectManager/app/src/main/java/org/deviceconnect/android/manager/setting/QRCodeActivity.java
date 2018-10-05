package org.deviceconnect.android.manager.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.deviceconnect.android.manager.R;
import org.deviceconnect.android.manager.util.NetworkUtil;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * QRコードを表示するアプリ.
 * ※「QRコード」は株式会社デンソーウェーブ様の登録商標で、JIS、ISOで規格されています。
 */
public class QRCodeActivity extends AppCompatActivity {
    /** TAG名. */
    private static final String TAG = "QRCodeActivity";
    /**
     * ネットワークの接続状態の変化を受け取るレシーバー.
     */
    private final BroadcastReceiver mWiFiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            setQRCode();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_qrcode);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mWiFiReceiver, filter);
        setQRCode();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mWiFiReceiver);
        super.onPause();
    }
    /**
     * IPアドレスをQRコード化して画像として表示する。
     */
    private void setQRCode() {
        //IPアドレスをQRコード化
        String ip = getIPAddress();
        if (ip.equals(getString(R.string.no_ip))) {
            ErrorDialogFragment fragment = new ErrorDialogFragment();
            Bundle args = new Bundle();
            args.putString(ErrorDialogFragment.EXTRA_TITLE, getString(R.string.dconnect_error_offline_title));
            args.putString(ErrorDialogFragment.EXTRA_MESSAGE, getString(R.string.dconnect_error_offline_message));
            fragment.setArguments(args);
            fragment.show(getSupportFragmentManager(), "error_dialog");
            return;
        }
        int size = 500;
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            //QRコードをBitmapで作成
            Bitmap bitmap = barcodeEncoder.encodeBitmap(ip, BarcodeFormat.QR_CODE, size, size);

            //作成したQRコードを画面上に配置
            ImageView imageViewQrCode = findViewById(R.id.thisIpAddressQR);
            imageViewQrCode.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
        // IPアドレスの表示
        TextView textViewIp = findViewById(R.id.thisIpAddress);
        textViewIp.setText(ip);

    }

    /**
     * この端末のIPアドレスを取得する.
     * @return Returns ip address
     */
    private String getIPAddress() {
        Context appContext = getApplicationContext();
        ConnectivityManager cManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cManager.getActiveNetworkInfo();
        String en0Ip = null;
        if (network != null) {
            switch (network.getType()) {
                case ConnectivityManager.TYPE_ETHERNET:
                    try {
                        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                            NetworkInterface intf = en.nextElement();
                            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                                InetAddress inetAddress = enumIpAddr.nextElement();
                                if (inetAddress instanceof Inet4Address
                                        && !inetAddress.getHostAddress().equals("127.0.0.1")) {
                                    en0Ip = inetAddress.getHostAddress();
                                    break;
                                }
                            }
                        }
                    } catch (SocketException e) {
                        Log.e(TAG, "Get Ethernet IP Error", e);
                    }
            }
        }
        if (en0Ip != null) {
            return en0Ip;
        } else {
            return NetworkUtil.getIpAddress();  //テザリング時のIPアドレス表示
        }
    }

}

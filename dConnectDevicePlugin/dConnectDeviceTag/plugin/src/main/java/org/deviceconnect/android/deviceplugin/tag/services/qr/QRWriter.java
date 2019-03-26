/*
 QRWriter.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.services.qr;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.Map;

/**
 * QR書き込み用クラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class QRWriter {
    /**
     * 白色を定義.
     */
    private static final int WHITE = 0xFFFFFFFF;

    /**
     * 黒色を定義.
     */
    private static final int BLACK = 0xFF000000;

    /**
     * BitMatrix から Bitmap を作成します.
     *
     * @param matrix QR読み込み結果
     * @return Bitmap
     */
    private Bitmap createBitmap(final BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = matrix.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * テキストをQRコードにエンコードします.
     *
     * @param contents テキスト
     * @param width 横幅
     * @param height 縦幅
     * @param hints ヒント
     * @return エンコード結果
     * @throws WriterException 書き込みに失敗した場合に発生
     */
    private BitMatrix encode(final String contents, final int width, final int height, final Map<EncodeHintType, ?> hints) throws WriterException {
        try {
            return new MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, width, height, hints);
        } catch (WriterException e) {
            throw e;
        } catch (Exception e) {
            throw new WriterException(e);
        }
    }

    /**
     * テキストをQRコードの Bitmap に書き込みます.
     *
     * @param contents 書き込むテキスト
     * @param width 横幅
     * @param height 縦幅
     * @return QRコードを書き込んだ Bitmap
     * @throws WriterException QRコードの書き込みに失敗した場合に発生
     */
    public Bitmap write(final String contents, final int width, final int height) throws WriterException {
        return write(contents, width, height, null);
    }

    /**
     * テキストをQRコードの Bitmap に書き込みます.
     *
     * @param contents 書き込むテキスト
     * @param width 横幅
     * @param height 縦幅
     * @param hints ヒント
     * @return QRコードを書き込んだ Bitmap
     * @throws WriterException QRコードの書き込みに失敗した場合に発生
     */
    public Bitmap write(final String contents, final int width, final int height, final Map<EncodeHintType, ?> hints) throws WriterException {
        return createBitmap(encode(contents, width, height, hints));
    }
}

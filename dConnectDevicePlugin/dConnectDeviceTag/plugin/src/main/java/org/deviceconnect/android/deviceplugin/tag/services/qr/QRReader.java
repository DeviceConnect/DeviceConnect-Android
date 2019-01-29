/*
 QRReader.java
 Copyright (c) 2019 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.tag.services.qr;

import android.graphics.Bitmap;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import org.deviceconnect.android.deviceplugin.tag.services.TagInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * QRコードを読み込むためのクラス.
 *
 * @author NTT DOCOMO, INC.
 */
public class QRReader {
    /**
     * ZXing の QR 読み込み用クラス.
     */
    private MultiFormatReader mReader = new MultiFormatReader();

    /**
     * {@link Bitmap} から QR を解析します.
     *
     * @param bitmap 解析を行う画像
     * @return QR解析結果
     * @throws NotFoundException 画像にQRコードが存在しない場合に発生
     */
    public TagInfo read(final Bitmap bitmap) throws NotFoundException {
        Result result = readQR(bitmap);

        Map<String, Object> tag = new HashMap<>();
        tag.put("text", result.getText());

        TagInfo tagInfo = new TagInfo();
        tagInfo.getList().add(tag);

        return tagInfo;
    }

    /**
     * {@link Bitmap} から QR を解析します.
     *
     * @param bitmap 解析を行う画像
     * @return QR解析結果
     * @throws NotFoundException 画像にQRコードが存在しない場合に発生
     */
    private Result readQR(final Bitmap bitmap) throws NotFoundException {
        LuminanceSource source = createLuminanceSource(bitmap);
        BinaryBitmap b = new BinaryBitmap(new HybridBinarizer(source));
        return mReader.decode(b);
    }

    /**
     * {@link Bitmap} から {@link LuminanceSource} を作成します.
     *
     * @param bitmap カメラのプレビュー画像
     * @return {@link LuminanceSource} のインスタンス
     */
    private LuminanceSource createLuminanceSource(final Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] intArray = new int[w * h];
        bitmap.getPixels(intArray, 0, w, 0, 0, w, h);
        return new RGBLuminanceSource(w, h, intArray);
    }
}

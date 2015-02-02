/*
 CanvasProfileConstants.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;



/**
 * Canvas Profile API 定数群.<br/>
 * Canvas Profile API のパラメータ名、インタフェース名、属性名、プロファイル名を定義する。
 * 
 * @author NTT DOCOMO, INC.
 */
public interface CanvasProfileConstants extends DConnectProfileConstants {

    /**
     * プロファイル名: {@value} .
     */
    String PROFILE_NAME = "canvas";

    /**
     * 属性: {@value} .
     */
    String ATTRIBUTE_DRAW_IMAGE = "drawimage";

    /**
     * パス: {@value}.
     */
    String PATH_PROFILE = PATH_ROOT + SEPARATOR + PROFILE_NAME;

    /**
     * パス: {@value} .
     */
    String PATH_DRAWIMAGE = PATH_PROFILE + SEPARATOR + ATTRIBUTE_DRAW_IMAGE;
    
    /**
     * パラメータ: {@value} .
     */
    String PARAM_MIME_TYPE = "mimeType";
    
    /**
     * パラメータ: {@value} .
     */
    String PARAM_DATA = "data";
    
    /**
     * パラメータ: {@value} .
     */
    String PARAM_X = "x";
    
    /**
     * パラメータ: {@value} .
     */
    String PARAM_Y = "y";
    
    /** 
     * パラメータ: {@value} .
     */
    String PARAM_MODE = "mode";
    
    
    /**
     * 画像表示モードを定義する.
     */
    enum Mode {
        
        /**
         * nullのときは等倍描画モード。座標(x, y)に画像の左上隅がくるように描画する.
         */
        
        /**
         * スケールモード。アスペクト比を保持して最大限に拡大して画面中央に描画する。x, y座標は無効.
         */
        SCALES("scales"),

        /**
         * フィルモード。等倍の画像を並べて画面全体に敷き詰めるように描画する。x, y座標は無効.
         */
        FILLS("fills");

        /**
         * 定義値.
         */
        private String mValue;

        /**
         * 指定された定義値をもつ定数を宣言します.
         * 
         */
        private Mode() {
            this.mValue = "";
        }

        /**
         * 指定された定義値をもつ定数を宣言します.
         * 
         * @param value 定義値
         */
        private Mode(final String value) {
            this.mValue = value;
        }

        /**
         * 定義値を取得する.
         * 
         * @return 定義値
         */
        public String getValue() {
            return mValue;
        }

        /**
         * 指定された文字列に対応するModeを取得する.
         * 指定された文字列に対応するModeが存在しない場合はnullを返却する.
         * @param value 文字列
         * @return Status
         */
        public static Mode getInstance(final String value) {
            for (Mode v : values()) {
                if (v.mValue.equals(value)) {
                    return v;
                }
            }
            return null;
        }
    };
}

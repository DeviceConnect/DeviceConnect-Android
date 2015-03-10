/*
 AuthorizationProfileConstants.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.profile;


/**
 * Authorization Profile API 定数群.<br/>
 * Authorization Profile API のパラメータ名、インタフェース名、属性名、プロファイル名を定義する。
 *
 * @author NTT DOCOMO, INC.
 */
public interface AuthorizationProfileConstants extends DConnectProfileConstants {

    /**
     * プロファイル名: {@value}.
     */
    String PROFILE_NAME = "authorization";

    /** 
     * 属性: {@value}.
     */
    String ATTRIBUTE_GRANT = "grant";

    /** 
     * 属性: {@value}.
     */
    String ATTRIBUTE_ACCESS_TOKEN = "accesstoken";

    /**
     * パス: {@value}.
     */
    String PATH_PROFILE = PATH_ROOT + SEPARATOR + PROFILE_NAME;

    /**
     * パス: {@value}.
     */
    String PATH_REQUEST_GRANT = PATH_PROFILE + SEPARATOR + ATTRIBUTE_GRANT;

    /**
     * パス: {@value}.
     */
    String PATH_ACCESS_TOKEN = PATH_PROFILE + SEPARATOR + ATTRIBUTE_ACCESS_TOKEN;

    /**
     * パラメータ: {@value}.
     */
    String PARAM_PACKAGE = "package";

    /**
     * パラメータ: {@value}.
     */
    String PARAM_ORIGIN = "origin";

    /**
     * パラメータ: {@value}.
     */
    String PARAM_CLIENT_ID = "clientId";

    /**
     * パラメータ: {@value}.
     */
    String PARAM_SCOPE = "scope";

    /**
     * パラメータ: {@value}.
     */
    String PARAM_SCOPES = "scopes";

    /**
     * パラメータ: {@value}.
     */
    String PARAM_APPLICATION_NAME = "applicationName";

    /**
     * パラメータ: {@value}.
     */
    String PARAM_ACCESS_TOKEN = "accessToken";

    /**
     * パラメータ: {@value}.
     */
    String PARAM_EXPIRE_PERIOD = "expirePeriod";

    /**
     * パラメータ: {@value}.
     * <p>
     * NOTE: GotAPI 1.0上で定義されているレスポンスパラメータ.
     * </p>
     */
    String PARAM_EXPIRE = "expire";

}

/*
 HOGPServiceInformation.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hogp.profiles;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import org.deviceconnect.android.deviceplugin.hogp.HOGPMessageService;
import org.deviceconnect.android.deviceplugin.hogp.HOGPSetting;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.api.GetApi;

/**
 * ServiceInformationプロファイル.
 *
 * @author NTT DOCOMO, INC.
 */
public class HOGPServiceInformation extends ServiceInformationProfile {

    /**
     * Constructor.
     */
    public HOGPServiceInformation() {
        addApi(new GetApi() {
            @Override
            public boolean onRequest(final Intent request, final Intent response) {
                appendServiceInformation(response);
                convertParameters(response);
                return true;
            }
        });
    }

    /**
     * mouseプロファイルのパラメータをAbsolute,Relativeに合わせて変更します.
     * <p>
     * AbsoluteとRelativeでは、minimumとmaximumの範囲が異なるので、そのパラメータを変更しています。
     * </p>
     * @param response 変更するレスポンス
     */
    private void convertParameters(final Intent response) {
        Bundle supportApis = response.getBundleExtra("supportApis");
        if (supportApis != null) {
            Bundle mouse = supportApis.getBundle("mouse");
            if (mouse != null) {
                Bundle pathsObj = mouse.getBundle("paths");
                if (pathsObj != null) {
                    Bundle path = pathsObj.getBundle("/");
                    if (path != null) {
                        Bundle post = path.getBundle("post");
                        if (post != null) {
                            Parcelable[] parameters = post.getParcelableArray("parameters");
                            if (parameters != null) {
                                for (Parcelable p : parameters) {
                                    Bundle param = (Bundle) p;
                                    String name = param.getString("name");
                                    if ("x".equalsIgnoreCase(name) || "y".equalsIgnoreCase(name)) {
                                        switch (getHOGPSetting().getMouseMode()) {
                                            case ABSOLUTE:
                                                param.putDouble("minimum", 0.0);
                                                param.putDouble("maximum", 1.0);
                                                break;
                                            case RELATIVE:
                                                param.putDouble("minimum", -1.0);
                                                param.putDouble("maximum", 1.0);
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * HOGPSettingを取得します.
     * @return HOGPSettingのインスタンス
     */
    private HOGPSetting getHOGPSetting() {
        HOGPMessageService service = (HOGPMessageService) getContext();
        return service.getHOGPSetting();
    }
}

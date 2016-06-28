package org.deviceconnect.android.profile;


import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class ServiceInformationProfile extends DConnectProfile implements ServiceInformationProfileConstants {

    public static class GetInformation extends Api {

        @Override
        public String getName() {
            return "Service Information API";
        }

        @Override
        public Method getMethod() {
            return Method.GET;
        }

        @Override
        public String getPath() {
            return "/gotapi/serviceinformation";
        }

        @Override
        public RequestParam[] getDefinedRequestParams() {
            return new RequestParam[0];
        }

        @Override
        public boolean onRequest(final DConnectServiceEndPoint service,
                                 final Intent request, final Intent response) {

            // ServiceInformation APIのレスポンスを作成
            List<Bundle> supports = new ArrayList<>();
            for (Api api : service.getApiList()) {
                Bundle support = new Bundle();
                setApiName(support, api.getName());
                setApiMethod(support, api.getMethod());
                setApiPath(support, api.getPath());

                // サポートしているリクエストパラメータの情報を返す.
                List<Bundle> params = new ArrayList<>();
                for (RequestParam supportedParam : api.getSupportedRequestParams()) {
                    Bundle param = new Bundle();
                    setRequestParamName(param, supportedParam.getName());
                    setRequestParamType(param, supportedParam.getType());
                    setRequestParamMandatory(param, supportedParam.isMandatory());
                    params.add(param);
                }
                setRequestParams(support, params);

                supports.add(support);
            }
            setSupports(response, supports);

            return true;
        }
    }

    public static void setRequestParams(final Bundle supportedApi, final Bundle[] params) {
        supportedApi.putParcelableArray(PARAM_REQUEST_PARAMS, params);
    }

    public static void setRequestParams(final Bundle supportedApi, final List<Bundle> params) {
        setRequestParams(supportedApi, params.toArray(new Bundle[params.size()]));
    }

    public static void setRequestParamName(final Bundle param, final String name) {
        param.putString(PARAM_NAME, name);
    }

    public static void setRequestParamType(final Bundle param, final String type) {
        param.putString(PARAM_TYPE, type);
    }

    public static void setRequestParamType(final Bundle param, final RequestParam.Type type) {
        setRequestParamType(param, type.toString());
    }

    public static void setRequestParamMandatory(final Bundle param, final boolean isMandatory) {
        param.putBoolean(PARAM_MANDATORY, isMandatory);
    }

    public static void setApiName(final Bundle supportedApi, final String apiName) {
        supportedApi.putString(PARAM_NAME, apiName);
    }

    public static void setApiPath(final Bundle supportedApi, final String apiPath) {
        supportedApi.putString(PARAM_NAME, apiPath);
    }

    public static void setApiMethod(final Bundle supportedApi, final Api.Method apiMethod) {
        supportedApi.putString(PARAM_NAME, apiMethod.toString());
    }

    public static void setSupports(final Intent response, final Bundle[] supports) {
        response.putExtra(PARAM_SUPPORTS, supports);
    }

    public static void setSupports(final Intent response, final List<Bundle> supports) {
        setSupports(response, supports.toArray(new Bundle[supports.size()]));
    }

}

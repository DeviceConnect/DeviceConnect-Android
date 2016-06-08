package org.deviceconnect.android.api;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ApiSpecDictionary {

    private final List<ApiSpec> mApiSpecList = new ArrayList<ApiSpec>();

    public ApiSpecDictionary() {}


    public void load(final InputStream is) throws IOException {
        // TODO JSONファイルからAPI定義を取得する.
    }

    private void addApiSpec(final ApiSpec apiSpec) {
        mApiSpecList.add(apiSpec);
    }

    public ApiSpec findApiSpec(final String method, final String path) {
        for (ApiSpec spec : mApiSpecList) {
            if (spec.getMethod().equals(method) && spec.getPath().equals(path)) {
                return spec;
            }
        }
        return null;
    }

    public List<ApiSpec> getApiSpecList(final EndPoint endPoint) {
        List<ApiSpec> supportedApis = new ArrayList<ApiSpec>();
        for (EndPoint.ApiIdentifier api : endPoint.getSupportedApiList()) {
            ApiSpec spec = findApiSpec(api.mMethod, api.mPath);
            if (spec != null) {
                supportedApis.add(spec);
            }
        }
        return supportedApis;
    }
}

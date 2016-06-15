package org.deviceconnect.android.profile.spec;


import org.json.JSONObject;

public class StringRequestParamSpec extends DConnectRequestParamSpec {

    public StringRequestParamSpec(final String name, final boolean isRequired) {
        super(name, Type.STRING, isRequired);
    }

    @Override
    void loadJson(final JSONObject json) {
        // TODO パラメータに対する制限を読み込む
    }
}

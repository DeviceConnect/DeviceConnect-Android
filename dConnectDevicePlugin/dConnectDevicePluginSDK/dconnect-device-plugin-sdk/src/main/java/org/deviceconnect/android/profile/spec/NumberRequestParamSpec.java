package org.deviceconnect.android.profile.spec;


import org.json.JSONObject;

public class NumberRequestParamSpec extends DConnectRequestParamSpec {

    public NumberRequestParamSpec(final String name, final boolean isRequired) {
        super(name, Type.NUMBER, isRequired);
    }

    @Override
    void loadJson(final JSONObject json) {
        // TODO パラメータに対する制限を読み込む
    }
}

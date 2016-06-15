package org.deviceconnect.android.profile.spec;


import org.json.JSONObject;

public class IntegerRequestParamSpec extends DConnectRequestParamSpec {

    public IntegerRequestParamSpec(final String name, final boolean isRequired) {
        super(name, Type.INTEGER, isRequired);
    }

    @Override
    void loadJson(final JSONObject json) {
        // TODO パラメータに対する制限を読み込む
    }
}

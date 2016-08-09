package org.deviceconnect.android.profile;

import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public abstract class DConnectProfile {

    public String getVersion() {
        return null;
    }

    protected boolean onRequest(final Intent request, final Intent response) {
        return true;
    }

    protected boolean onGetRequest(final Intent request, final Intent response) {
        return true;
    }

    protected boolean onPutRequest(final Intent request, final Intent response) {
        return true;
    }

    protected boolean onPostRequest(final Intent request, final Intent response) {
        return true;
    }

    protected boolean onDeleteRequest(final Intent request, final Intent response) {
        return true;
    }

    public abstract static class Api {

        public abstract String getName();

        public abstract Method getMethod();

        public abstract String getPath();

        public abstract RequestParam[] getDefinedRequestParams();

        public final RequestParam[] getSupportedRequestParams() {
            String[] names = getSupportedRequestParamNames();
            RequestParam[] definedParams = getDefinedRequestParams();
            if (names == null || names.length == 0 ||
                definedParams == null || definedParams.length == 0) {
                return new RequestParam[0];
            }
            List<RequestParam> supportedParams = new ArrayList<>();
            for (String name : names) {
                for (RequestParam definedParam : definedParams) {
                    if (definedParam.getName().equals(name)) {
                        supportedParams.add(definedParam);
                    }
                }
            }
            return supportedParams.toArray(new RequestParam[supportedParams.size()]);
        }

        public String[] getSupportedRequestParamNames() {
            return new String[0];
        }

        public boolean onRequest(final DConnectServiceEndPoint service,
                                 final Intent request, final Intent response) {
            return true; // TODO デフォルトではNotSupportedエラーを返す
        }

        public enum Method {
            GET,
            PUT,
            POST,
            DELETE
        }

    }

    public static class RequestParam {

        private final String mName;

        private final Type mType;

        private final boolean mIsMandatory;

        public RequestParam(final String name, final Type type, final boolean isMandatory) {
            mName = name;
            mType = type;
            mIsMandatory = isMandatory;
        }

        enum Type {

            STRING,
            INTEGER,
            DOUBLE,
            BOOLEAN;

            @Override
            public String toString() {
                return super.name().toLowerCase();
            }
        }

        public final String getName() {
            return mName;
        }

        public final Type getType() {
            return mType;
        }

        public final boolean isMandatory() {
            return mIsMandatory;
        }

    }

    public static class RequestStringParam extends RequestParam {

        public RequestStringParam(final String name, final boolean isMandatory) {
            super(name, Type.STRING, isMandatory);
        }

    }

    public static class RequestIntegerParam extends RequestParam {

        public RequestIntegerParam(final String name, final boolean isMandatory) {
            super(name, Type.INTEGER, isMandatory);
        }

    }

    public static class RequestDoubleParam extends RequestParam {

        public RequestDoubleParam(final String name, final boolean isMandatory) {
            super(name, Type.DOUBLE, isMandatory);
        }

    }

    public static class RequestBooleanParam extends RequestParam {

        public RequestBooleanParam(final String name, final boolean isMandatory) {
            super(name, Type.BOOLEAN, isMandatory);
        }

    }

}

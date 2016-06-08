package org.deviceconnect.android.api;


import java.util.ArrayList;
import java.util.List;

public class EndPoint {

    private final String mId;

    private final List<ApiIdentifier> mSupportedApiList;

    private EndPoint(final String id, final List<ApiIdentifier> supportedApiList) {
        mId = id;
        mSupportedApiList = supportedApiList;
    }

    List<ApiIdentifier> getSupportedApiList() {
        return mSupportedApiList;
    }

    public String getId() {
        return mId;
    }

    public static class Builder {

        private String mId;

        private final List<ApiIdentifier> mApiList = new ArrayList<ApiIdentifier>();

        public Builder() {
        }

        public Builder setId(final String id) {
            mId = id;
            return this;
        }

        public Builder addApi(final String method, final String path) {
            if (method == null) {
                throw new IllegalArgumentException("method is null.");
            }
            if (path == null) {
                throw new IllegalArgumentException("path is null.");
            }
            mApiList.add(new ApiIdentifier(method, path));
            return this;
        }

        public EndPoint build() {
            if (mId == null) {
                throw new IllegalStateException("An identifier of end point is null.");
            }
            return new EndPoint(mId, mApiList);
        }

    }

    static class ApiIdentifier {

        final String mMethod;

        final String mPath;

        ApiIdentifier(final String method, final String path) {
            mMethod = method;
            mPath = path;
        }

    }

}

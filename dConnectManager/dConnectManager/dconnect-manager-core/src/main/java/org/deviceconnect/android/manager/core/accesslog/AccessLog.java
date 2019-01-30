package org.deviceconnect.android.manager.core.accesslog;

public class AccessLog {
    private int mId = -1;

    private String mDate;

    private String mRemoteIpAddress;
    private String mRemoteHostName;
    private long mRequestReceivedTime;
    private String mRequestMethod;
    private String mRequestPath;
    private String mRequestBody;

    private long mResponseSendTime;
    private int mResponseStatusCode;
    private String mResponseContentType;
    private String mResponseBody;

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getRemoteIpAddress() {
        return mRemoteIpAddress;
    }

    public void setRemoteIpAddress(String remoteIpAddress) {
        mRemoteIpAddress = remoteIpAddress;
    }

    public String getRemoteHostName() {
        return mRemoteHostName;
    }

    public void setRemoteHostName(String remoteHostName) {
        mRemoteHostName = remoteHostName;
    }

    public long getRequestReceivedTime() {
        return mRequestReceivedTime;
    }

    public void setRequestReceivedTime(long requestReceivedTime) {
        mRequestReceivedTime = requestReceivedTime;
    }

    public String getRequestMethod() {
        return mRequestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        mRequestMethod = requestMethod;
    }

    public String getRequestPath() {
        return mRequestPath;
    }

    public void setRequestPath(String requestPath) {
        mRequestPath = requestPath;
    }

    public String getRequestBody() {
        return mRequestBody;
    }

    public void setRequestBody(String requestBody) {
        mRequestBody = requestBody;
    }

    public long getResponseSendTime() {
        return mResponseSendTime;
    }

    public void setResponseSendTime(long responseSendTime) {
        mResponseSendTime = responseSendTime;
    }

    public int getResponseStatusCode() {
        return mResponseStatusCode;
    }

    public void setResponseStatusCode(int responseStatusCode) {
        mResponseStatusCode = responseStatusCode;
    }

    public String getResponseContentType() {
        return mResponseContentType;
    }

    public void setResponseContentType(String responseContentType) {
        mResponseContentType = responseContentType;
    }

    public String getResponseBody() {
        return mResponseBody;
    }

    public void setResponseBody(String responseBody) {
        mResponseBody = responseBody;
    }
}

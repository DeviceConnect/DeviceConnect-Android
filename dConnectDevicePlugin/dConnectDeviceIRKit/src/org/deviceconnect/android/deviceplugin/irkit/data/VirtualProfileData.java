package org.deviceconnect.android.deviceplugin.irkit.data;

/**
 * Virtual Profile.
 */
public class VirtualProfileData {
    /**
     * ID.
     */
    private int mId;
    /**
     * 赤外線データ.
     */
    private String mIr;
    /**
     * URI.
     */
    private String mUri;
    /**
     * HTTP Method.
     */
    private String mMethod;
    /**
     * サービスID.
     */
    private String mServiceId;
    /**
     * API名.
     */
    private String mName;
    /**
     * Profile名.
     */
    private String mProfile;

    /**
     * ID の取得.
     * @return ID
     */
    public int getId() {
        return mId;
    }

    /**
     * 赤外線データの取得.
     * @return 赤外線データ
     */
    public String getIr() {
        return mIr;
    }

    /**
     * Uri の取得.
     * @return Uri
     */
    public String getUri() {
        return mUri;
    }

    /**
     * HTTP Method を取得する.
     * @return HTTP Method
     */
    public String getMethod() {
        return mMethod;
    }

    /**
     * サービスID の取得.
     * @return サービスID
     */
    public String getServiceId() {
        return mServiceId;
    }

    /**
     * API 名の取得.
     * @return API名
     */
    public String getName() {
        return mName;
    }

    /**
     * Profile 名の取得.
     * @return Profile名
     */
    public String getProfile() {
        return mProfile;
    }

    /**
     * ID を設定する.
     * @param id ID
     */
    public void setId(final int id) {
        mId = id;
    }

    /**
     * 赤外線データを設定する.
     * @param ir 赤外線データ
     */
    public void setIr(final String ir) {
        mIr = ir;
    }

    /**
     * Uri を設定する.
     * @param uri uri
     */
    public void setUri(final String uri) {
        mUri = uri;
    }

    /**
     * HTTP Method を設定する.
     * @param method HTTP Method
     */
    public void setMethod(final String method) {
        mMethod = method;
    }

    /**
     * サービスID を設定する.
     * @param serviceId サービスID
     */
    public void setServiceId(final String serviceId) {
        mServiceId = serviceId;
    }

    /**
     * API 名を設定する.
     * @param name API 名
     */
    public void setName(final String name) {
        mName = name;
    }

    /**
     * Profile 名を設定する.
     * @param profile Profil名
     */
    public void setProfile(final String profile) {
        mProfile = profile;
    }
}

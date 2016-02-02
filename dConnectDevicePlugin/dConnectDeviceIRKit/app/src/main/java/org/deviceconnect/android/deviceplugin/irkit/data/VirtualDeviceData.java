package org.deviceconnect.android.deviceplugin.irkit.data;

/**
 * Virtual Device.
 */
public class VirtualDeviceData {
    /**
     * サービスID.
     */
    private String mServiceId;
    /**
     * デバイス名.
     */
    private String mDeviceName;
    /**
     * カテゴリ名.
     */
    private String mCategoryName;

    /**
     * サービスIDの取得.
     * @return サービスID
     */
    public String getServiceId() {
        return mServiceId;
    }

    /**
     * デバイス名の取得.
     * @return デバイス名
     */
    public String getDeviceName() {
        return mDeviceName;
    }

    /**
     * カテゴリ名の取得.
     * @return カテゴリ名
     */
    public String getCategoryName() {
        return mCategoryName;
    }

    /**
     * サービスIDの設定.
     * @param serviceId サービスID
     */
    public void setServiceId(final String serviceId) {
        mServiceId = serviceId;
    }

    /**
     * デバイス名の設定.
     * @param deviceName デバイス名
     */
    public void setDeviceName(final String deviceName) {
        mDeviceName = deviceName;
    }

    /**
     * カテゴリ名の設定.
     * @param categoryName カテゴリ名
     */
    public void setCategoryName(final String categoryName) {
        mCategoryName = categoryName;
    }
}

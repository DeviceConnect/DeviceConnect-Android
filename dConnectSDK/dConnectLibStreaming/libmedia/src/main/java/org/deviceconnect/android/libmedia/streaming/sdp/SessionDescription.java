package org.deviceconnect.android.libmedia.streaming.sdp;

import java.util.ArrayList;
import java.util.List;

/**
 *  SDP: セッション記述プロトコル.
 *
 * @see <a href="http://www.softfront.co.jp/tech/ietfdoc/trans/rfc4566j.txt">RFC 4566</a>
 */
public class SessionDescription {
    /**
     * 改行コードを定義します.
     */
    private static final String NEW_LINE = "\r\n";

    /**
     * プロトコルのバージョン.
     * <p>
     * 必須.
     * </p>
     */
    private Integer mVersion = 0;

    /**
     * 発信元およびセッション識別子.
     * <p>
     * 必須.
     * </p>
     */
    private Origin mOrigin;

    /**
     * セッション名.
     * <p>
     * 必須.
     * </p>
     */
    private SessionName mSessionName;

    /**
     * セッション情報.
     * <p>
     * OPTIONAL.
     * </p>
     */
    private Information mInformation;

    /**
     * セッション情報.
     * <p>
     * OPTIONAL.
     * すべてのメディアに含まれる場合は必要なし。
     * </p>
     */
    private Connection mConnection;

    /**
     * 記述のURI.
     * <p>
     * OPTIONAL.
     * </p>
     */
    private Url mUrl;

    /**
     * 電話番号.
     * <p>
     * OPTIONAL.
     * </p>
     */
    private PhoneNumber mPhoneNumber;

    /**
     * 電子メールアドレス.
     * <p>
     * OPTIONAL.
     * </p>
     */
    private EMail mEMail;

    /**
     * 暗号化キー.
     * <p>
     * OPTIONAL.
     * </p>
     */
    private EncryptionKey mEncryptionKey;

    /**
     * セッション属性.
     * <p>
     * OPTIONAL.
     * </p>
     */
    private List<Attribute> mAttributes = new ArrayList<>();

    /**
     * 帯域情報.
     * <p>
     * OPTIONAL.
     * </p>
     */
    private ArrayList<Bandwidth> mBandwidths = new ArrayList<>();

    /**
     * セッションがアクティブな時間.
     * <p>
     * 必須.
     * </p>
     */
    private List<Time> mTimes = new ArrayList<>();

    /**
     * メディア記述.
     * <p>
     * 必須.
     * </p>
     */
    private List<MediaDescription> mMediaDescriptions = new ArrayList<>();

    public Integer getVersion() {
        return mVersion;
    }

    public void setVersion(Integer version) {
        mVersion = version;
    }

    public Origin getOrigin() {
        return mOrigin;
    }

    public void setOrigin(Origin origin) {
        mOrigin = origin;
    }

    public SessionName getSessionName() {
        return mSessionName;
    }

    public void setSessionName(SessionName sessionName) {
        mSessionName = sessionName;
    }

    public Information getInformation() {
        return mInformation;
    }

    public void setInformation(Information information) {
        mInformation = information;
    }

    public Connection getConnection() {
        return mConnection;
    }

    public void setConnection(Connection connection) {
        mConnection = connection;
    }

    public Url getUrl() {
        return mUrl;
    }

    public void setUrl(Url url) {
        mUrl = url;
    }

    public PhoneNumber getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(PhoneNumber phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public EMail getEMail() {
        return mEMail;
    }

    public void setEMail(EMail EMail) {
        mEMail = EMail;
    }

    public EncryptionKey getEncryptionKey() {
        return mEncryptionKey;
    }

    public void setEncryptionKey(EncryptionKey encryptionKey) {
        mEncryptionKey = encryptionKey;
    }

    public void addAttribute(Attribute attribute) {
        mAttributes.add(attribute);
    }

    public List<Attribute> getAttributes() {
        return mAttributes;
    }

    public void addTime(Time time) {
        mTimes.add(time);
    }

    public List<Time> getTimes() {
        return mTimes;
    }

    public void addBandwidth(Bandwidth bandwith) {
        mBandwidths.add(bandwith);
    }

    public ArrayList<Bandwidth> getBandwidths() {
        return mBandwidths;
    }

    public void addMediaDescriptions(MediaDescription media) {
        mMediaDescriptions.add(media);
    }

    public List<MediaDescription> getMediaDescriptions() {
        return mMediaDescriptions;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("v=").append(mVersion).append(NEW_LINE);
        builder.append(mOrigin.toString()).append(NEW_LINE);
        builder.append(mSessionName.toString()).append(NEW_LINE);
        if (mInformation != null) {
            builder.append(mInformation.toString()).append(NEW_LINE);
        }
        if (mUrl != null) {
            builder.append(mUrl.toString()).append(NEW_LINE);
        }
        if (mEMail != null) {
            builder.append(mEMail.toString()).append(NEW_LINE);
        }
        if (mPhoneNumber != null) {
            builder.append(mPhoneNumber.toString()).append(NEW_LINE);
        }
        if (mEncryptionKey != null) {
            builder.append(mEncryptionKey.toString()).append(NEW_LINE);
        }
        if (mConnection != null) {
            builder.append(mConnection.toString()).append(NEW_LINE);
        }
        for (Time time : mTimes) {
            builder.append(time.toString()).append(NEW_LINE);
        }
        for (Attribute attr : mAttributes) {
            builder.append(attr.toString()).append(NEW_LINE);
        }
        for (Bandwidth bandwidth : mBandwidths) {
            builder.append(bandwidth.toString()).append(NEW_LINE);
        }
        for (MediaDescription media : mMediaDescriptions) {
            builder.append(media.toString());
        }
        return builder.toString();
    }
}

package org.deviceconnect.message.entity;

/**
 * バイナリーコンテンツを送信するためのエンティティ.
 *
 * @author NTT DOCOMO, INC.
 */
public class BinaryEntity implements Entity {
    private byte[] mContent;
    private String mName;
    /**
     * BinaryEntityを生成する.
     * @param content 送信するコンテンツ
     * @throws NullPointerException contentが{@code null}の場合に発生
     */
    public BinaryEntity(final byte[] content) {
        if (content == null) {
            throw new NullPointerException("content is null.");
        }
        mContent = content;
    }

    /**
     * BinaryEntityを生成する.
     * <p>
     * マルチパートに指定する場合には、こちらのコンストラクタを使用して、コンテンツ名を設定する。<br>
     * </p>
     * @param content 送信するコンテンツ
     * @param name マルチパートのfilenameに設定される名前
     * @throws NullPointerException contentもしくはnameが{@code null}の場合に発生
     */
    public BinaryEntity(final byte[] content, final String name) {
        if (content == null) {
            throw new NullPointerException("content is null.");
        }
        if (name == null) {
            throw new NullPointerException("name is null.");
        }
        mContent = content;
        mName = name;
    }

    /**
     * コンテンツ名を取得する.
     * @return コンテンツ名
     */
    public String getName() {
        return mName;
    }

    /**
     * 設定されいるコンテンツを取得する.
     * @return コンテンツ
     */
    public byte[] getContent() {
        return mContent;
    }
}

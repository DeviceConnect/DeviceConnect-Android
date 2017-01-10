package org.deviceconnect.message.entity;

/**
 * バイナリーコンテンツを送信するためのエンティティ.
 *
 * @author NTT DOCOMO, INC.
 */
public class BinaryEntity implements Entity {
    private byte[] mContent;
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
     * 設定されいるコンテンツを取得する.
     * @return コンテンツ
     */
    public byte[] getContent() {
        return mContent;
    }
}

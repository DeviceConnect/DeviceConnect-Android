package org.deviceconnect.message.entity;

/**
 * 文字列のコンテンツを送信するためのエンティティ.
 *
 * @author NTT DOCOMO, INC.
 */
public class StringEntity implements Entity {
    private String mContent;

    /**
     * StringEntityを生成する.
     * @param content 送信するコンテンツ
     * @throws NullPointerException contentが{@code null}の場合に発生
     */
    public StringEntity(final String content) {
        if (content == null) {
            throw new NullPointerException("content is null.");
        }
        mContent = content;
    }

    /**
     * 設定されいるコンテンツを取得する.
     * @return コンテンツ
     */
    public String getContent() {
        return mContent;
    }
}

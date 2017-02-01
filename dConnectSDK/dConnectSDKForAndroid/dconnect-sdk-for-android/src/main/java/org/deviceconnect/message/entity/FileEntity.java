package org.deviceconnect.message.entity;

import java.io.File;

/**
 * ファイルコンテンツを送信するためのエンティティ.
 *
 * @author NTT DOCOMO, INC.
 */
public class FileEntity implements Entity {
    private File mContent;
    /**
     * FileEntityを生成する.
     * @param content 送信するコンテンツ
     * @throws NullPointerException contentが{@code null}の場合に発生
     */
    public FileEntity(final File content) {
        if (content == null) {
            throw new NullPointerException("content is null.");
        }
        mContent = content;
    }

    /**
     * 設定されいるコンテンツを取得する.
     * @return コンテンツ
     */
    public File getContent() {
        return mContent;
    }
}

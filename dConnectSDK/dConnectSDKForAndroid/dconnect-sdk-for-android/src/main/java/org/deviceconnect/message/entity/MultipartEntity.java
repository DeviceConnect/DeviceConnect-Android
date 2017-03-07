package org.deviceconnect.message.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * マルチパートのデータを送信するためのエンティティ.
 *
 * @author NTT DOCOMO, INC.
 */
public class MultipartEntity implements Entity {
    private Map<String, Entity> mContent = new HashMap<>();

    /**
     * MultipartEntityを生成する.
     */
    public MultipartEntity() {
    }

    /**
     * マルチパートにデータを追加する.
     * @param key キー
     * @param entity 追加するデータ
     * @throws NullPointerException keyが{@code null}もしくは、entityが{@code null}の場合に発生
     * @throws IllegalArgumentException entityが{@link BinaryEntity}、{@link StringEntity}、{@link FileEntity}以外の場合に発生
     */
    public void add(final String key, final Entity entity) {
        if (!(entity instanceof BinaryEntity) && !(entity instanceof StringEntity) && !(entity instanceof FileEntity)) {
            throw new IllegalArgumentException("entity is invalid.");
        }
        mContent.put(key, entity);
    }

    /**
     * 設定されいるコンテンツを取得する.
     * @return コンテンツ
     */
    public Map<String, Entity> getContent() {
        return mContent;
    }
}

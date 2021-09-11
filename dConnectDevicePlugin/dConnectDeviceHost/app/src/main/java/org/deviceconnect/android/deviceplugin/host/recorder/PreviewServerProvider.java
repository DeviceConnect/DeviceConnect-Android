/*
 PreviewServerProvider.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.host.recorder;

import java.util.List;

public interface PreviewServerProvider extends LiveStreamingProvider {
    /**
     * プレビューで配信するマイムタイプを取得します.
     *
     * @return プレビューで配信するマイムタイプ
     */
    List<String> getSupportedMimeType();
}

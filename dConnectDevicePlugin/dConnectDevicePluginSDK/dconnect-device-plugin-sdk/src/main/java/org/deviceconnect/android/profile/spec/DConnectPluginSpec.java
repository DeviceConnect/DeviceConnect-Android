package org.deviceconnect.android.profile.spec;


import org.deviceconnect.android.profile.spec.parser.DConnectProfileSpecJsonParser;
import org.deviceconnect.android.profile.spec.parser.DConnectProfileSpecJsonParserFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * プラグインのサポートする仕様を保持するクラス.
 * <p>
 * プラグインのサポートするプロファイルのリストを持つ.
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class DConnectPluginSpec {

    private static final int BUFFER_SIZE = 1024;

    private final DConnectProfileSpecJsonParser mJsonParser
        = DConnectProfileSpecJsonParserFactory.getDefaultFactory().createParser();

    private final Map<String, DConnectProfileSpec> mProfileSpecs
        = new ConcurrentHashMap<String, DConnectProfileSpec>();

    public void addProfileSpec(final String profileName, final InputStream in)
        throws IOException, JSONException {
        mProfileSpecs.put(profileName, mJsonParser.parseJson(new JSONObject(loadFile(in))));
    }

    private static String loadFile(final InputStream in) throws IOException {
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            int len;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((len = in.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }
            return new String(baos.toByteArray());
        } finally {
            in.close();
        }
    }

    public DConnectProfileSpec findProfileSpec(final String profileName) {
        return mProfileSpecs.get(profileName);
    }

}

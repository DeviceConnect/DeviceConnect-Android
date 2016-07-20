package org.deviceconnect.android.profile.spec;


import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * プロファイルについての仕様を保持するクラス.
 * <p>
 * 当該プロファイル上で定義されるAPIの仕様のリストを持つ.
 * </p>
 * @author NTT DOCOMO, INC.
 */
public class DConnectProfileSpec implements DConnectApiSpecConstants {

    private BundleFactory mFactory;

    private Map<String, Map<Method, DConnectApiSpec>> mAllApiSpecs;

    private DConnectProfileSpec() {
    }

    void setBundleFactory(final BundleFactory factory) {
        mFactory = factory;
    }

    void setApiSpecs(final Map<String, Map<Method, DConnectApiSpec>> apiSpecs) {
        mAllApiSpecs = apiSpecs;
    }

    public List<DConnectApiSpec> getApiSpecList() {
        List<DConnectApiSpec> list = new ArrayList<DConnectApiSpec>();
        if (mAllApiSpecs == null) {
            return list;
        }
        for (Map<Method, DConnectApiSpec> apiSpecs : mAllApiSpecs.values()) {
            for (DConnectApiSpec apiSpec : apiSpecs.values()) {
                list.add(apiSpec);
            }
        }
        return list;
    }

    public Map<Method, DConnectApiSpec> findApiSpecs(final String path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }
        return mAllApiSpecs.get(path.toLowerCase());
    }

    public DConnectApiSpec findApiSpec(final String path, final Method method) {
        if (method == null) {
            throw new IllegalArgumentException("method is null.");
        }
        Map<Method, DConnectApiSpec> apiSpecsOfPath = findApiSpecs(path);
        if (apiSpecsOfPath == null) {
            return null;
        }
        return apiSpecsOfPath.get(method);
    }

    public Bundle toBundle() {
        return toBundle(null);
    }

    public Bundle toBundle(final DConnectApiSpecFilter filter) {
        if (mFactory == null) {
            return null;
        }
        return mFactory.createBundle(this, filter);
    }

    public static class Builder {

        private final Map<String, Map<Method, DConnectApiSpec>> mAllApiSpecs =
            new HashMap<String, Map<Method, DConnectApiSpec>>();

        private BundleFactory mFactory;

        public void addApiSpec(final String path, final Method method,
                               final DConnectApiSpec apiSpec) {
            String pathKey = path.toLowerCase();
            Map<Method, DConnectApiSpec> apiSpecs = mAllApiSpecs.get(pathKey);
            if (apiSpecs == null) {
                apiSpecs = new HashMap<Method, DConnectApiSpec>();
                mAllApiSpecs.put(pathKey, apiSpecs);
            }
            apiSpecs.put(method, apiSpec);
        }

        public void setBundleFactory(final BundleFactory factory) {
            mFactory = factory;
        }

        public DConnectProfileSpec build() {
            DConnectProfileSpec profileSpec = new DConnectProfileSpec();
            profileSpec.setApiSpecs(mAllApiSpecs);
            profileSpec.setBundleFactory(mFactory);
            return profileSpec;
        }
    }

    public interface BundleFactory {

        Bundle createBundle(final DConnectProfileSpec profileSpec,
                            final DConnectApiSpecFilter filter);

    }
}

package org.deviceconnect.android.profile.spec;


import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DConnectProfileSpec implements DConnectApiSpecConstants {

    private static final String KEY_PATHS = "paths";

    private Bundle mSpec;

    private final Map<String, Map<Method, DConnectApiSpec>> mApiSpecs
        = Collections.synchronizedMap(new HashMap<String, Map<Method, DConnectApiSpec>>());

    private DConnectProfileSpec() {
    }

    public static DConnectProfileSpec fromJson(final JSONObject json) throws JSONException {
        DConnectProfileSpec spec = new DConnectProfileSpec();
        spec.mSpec = toBundle(json);

        JSONObject pathsObj = json.getJSONObject(KEY_PATHS);
        for (Iterator<String> it = pathsObj.keys(); it.hasNext(); ) {
            String path = it.next();
            JSONObject pathObj = pathsObj.getJSONObject(path);

            Map<Method, DConnectApiSpec> apiSpecsOfPath = new HashMap<Method, DConnectApiSpec> ();
            for (Method method : Method.values()) {
                JSONObject methodObj = pathObj.optJSONObject(method.getName().toLowerCase());
                if (methodObj == null) {
                    continue;
                }
                DConnectApiSpec apiSpec = DConnectApiSpec.fromJson(methodObj);
                if (apiSpec != null) {
                    apiSpecsOfPath.put(method, apiSpec);
                }
            }

            spec.mApiSpecs.put(path.toLowerCase(), apiSpecsOfPath);
        }

        return spec;
    }

    private static Bundle toBundle(final JSONObject jsonObj) throws JSONException {
        Bundle bundle = new Bundle();
        for (Iterator<String> it = jsonObj.keys(); it.hasNext(); ) {
            String name = it.next();
            Object value = jsonObj.get(name);
            if (value instanceof JSONArray) {
                putArray(bundle, name, (JSONArray) value);
            } else if (value instanceof JSONObject) {
                bundle.putBundle(name, toBundle((JSONObject) value));
            } else if (value instanceof Serializable) {
                bundle.putSerializable(name, (Serializable) value);
            }
        }
        return bundle;
    }

    private static void traceBundle(final Bundle target) {
        StringBuilder result = new StringBuilder();
        traceBundle(target, result, 0);
    }

    private static void traceBundle(final Bundle target, final StringBuilder result, final int level) {
        String indent = "";
        for (int i = 0; i < level; i++) {
            indent += "    ";
        }

        for (String key : target.keySet()) {
            Object value = target.get(key);
            result.append(indent);
            result.append(key);
            result.append(": ");
            if (value == null) {
                result.append("null");
            } else if (value instanceof Bundle) {
                result.append("\n");
                traceBundle((Bundle) value, result, level + 1);
            } else {
                result.append(value.toString());
            }
            result.append("\n");
        }
    }

    private static void putArray(final Bundle bundle, final String name, final JSONArray jsonArray)
        throws JSONException {
        if (jsonArray.length() == 0) {
            bundle.putParcelableArray(name, new Bundle[0]);
        } else {
            final Class base = getBaseClass(jsonArray);
            final int length = jsonArray.length();
            if (base == Integer.class) {
                int[] array = new int[length];
                for (int i = 0; i < length; i++) {
                    array[i] = jsonArray.getInt(i);
                }
                bundle.putIntArray(name, array);
            } else if (base == Long.class) {
                long[] array = new long[length];
                for (int i = 0; i < length; i++) {
                    array[i] = jsonArray.getLong(i);
                }
                bundle.putLongArray(name, array);
            } else if (base == Double.class) {
                double[] array = new double[length];
                for (int i = 0; i < length; i++) {
                    array[i] = jsonArray.getDouble(i);
                }
                bundle.putDoubleArray(name, array);
            } else if (base == String.class) {
                String[] array = new String[length];
                for (int i = 0; i < length; i++) {
                    array[i] = jsonArray.getString(i);
                }
                bundle.putStringArray(name, array);
            } else if (base == JSONObject.class) {
                Bundle[] array = new Bundle[length];
                for (int i = 0; i < length; i++) {
                    array[i] = toBundle(jsonArray.getJSONObject(i));
                }
                bundle.putParcelableArray(name, array);
            }
        }
    }

    private static Class getBaseClass(final JSONArray array) throws JSONException {
        Class cls = array.get(0).getClass();
        for (int i = 1; i < array.length(); i++) {
            if (cls != array.get(i).getClass()) {
                return null;
            }
        }
        return cls;
    }

    public Map<Method, DConnectApiSpec> findApiSpecs(final String path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null.");
        }
        return mApiSpecs.get(path.toLowerCase());
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
        return toBundle((DConnectApiSpecFilter) null);
    }

    public Bundle toBundle(final DConnectApiSpecFilter filter) {
        Bundle profileSpec = new Bundle(mSpec);
        if (filter == null) {
            return profileSpec;
        }
        Bundle pathsObj = profileSpec.getBundle(KEY_PATHS);
        if (pathsObj == null) {
            return profileSpec;
        }
        Set<String> pathNames = pathsObj.keySet();
        if (pathNames == null) {
            return profileSpec;
        }
        for (String pathName : pathNames) {
            Bundle pathObj = pathsObj.getBundle(pathName);
            if (pathObj == null) {
                continue;
            }
            for (Method method : Method.values()) {
                String methodName = method.getName().toLowerCase();
                Bundle methodObj = pathObj.getBundle(methodName);
                if (methodObj == null) {
                    continue;
                }
                if (!filter.filter(pathName, method)) {
                    pathObj.remove(methodName);
                }
            }
//            if (pathObj.size() == 0) {
//                pathsObj.remove(pathName);
//            }
        }
        return profileSpec;
    }
}

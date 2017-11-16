/*
 SwaggerJsonParser.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.parser;


import android.os.Bundle;

import org.deviceconnect.android.profile.spec.ArrayDataSpec;
import org.deviceconnect.android.profile.spec.ArrayParameterSpec;
import org.deviceconnect.android.profile.spec.BooleanDataSpec;
import org.deviceconnect.android.profile.spec.BooleanParameterSpec;
import org.deviceconnect.android.profile.spec.DConnectApiSpec;
import org.deviceconnect.android.profile.spec.DConnectDataSpec;
import org.deviceconnect.android.profile.spec.DConnectParameterSpec;
import org.deviceconnect.android.profile.spec.DConnectProfileSpec;
import org.deviceconnect.android.profile.spec.DConnectSpecConstants;
import org.deviceconnect.android.profile.spec.FileParameterSpec;
import org.deviceconnect.android.profile.spec.IntegerDataSpec;
import org.deviceconnect.android.profile.spec.IntegerParameterSpec;
import org.deviceconnect.android.profile.spec.NumberDataSpec;
import org.deviceconnect.android.profile.spec.NumberParameterSpec;
import org.deviceconnect.android.profile.spec.StringDataSpec;
import org.deviceconnect.android.profile.spec.StringParameterSpec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class SwaggerJsonParser implements DConnectProfileSpecJsonParser, DConnectSpecConstants {

    private static final String KEY_BASE_PATH = "basePath";
    private static final String KEY_PATHS = "paths";

    private static final OperationObjectParser OPERATION_OBJECT_PARSER = new OperationObjectParser() {
        @Override
        public DConnectApiSpec parseJson(final Method method, final JSONObject opObj) throws JSONException {
            DConnectSpecConstants.Type type
                = DConnectSpecConstants.Type.parse(opObj.getString(KEY_X_TYPE));
            JSONArray parameters = opObj.getJSONArray(KEY_PARAMETERS);

            List<DConnectParameterSpec> paramSpecList = new ArrayList<DConnectParameterSpec>();
            for (int i = 0; i < parameters.length(); i++) {
                JSONObject paramObj = parameters.getJSONObject(i);
                ParameterObjectParser parser = getParameterParser(paramObj);
                DConnectParameterSpec paramSpec = parser.parseJson(paramObj);
                paramSpecList.add(paramSpec);
            }

            return new DConnectApiSpec.Builder()
                .setType(type)
                .setMethod(method)
                .setRequestParamList(paramSpecList)
                .build();
        }
    };

    private static final ItemsObjectParser ARRAY_ITEMS_PARSER = new ArrayItemsObjectParser();
    private static final ItemsObjectParser BOOLEAN_ITEMS_PARSER = new BooleanItemsObjectParser();
    private static final ItemsObjectParser INTEGER_ITEMS_PARSER = new IntegerItemsObjectParser();
    private static final ItemsObjectParser NUMBER_ITEMS_PARSER = new NumberItemsObjectParser();
    private static final ItemsObjectParser STRING_ITEMS_PARSER = new StringItemsObjectParser();

    private static final ParameterObjectParser ARRAY_PARAM_PARSER = new ArrayParameterParser();
    private static final ParameterObjectParser BOOLEAN_PARAM_PARSER = new BooleanParameterParser();
    private static final ParameterObjectParser FILE_PARAM_PARSER = new FileParameterParser();
    private static final ParameterObjectParser INTEGER_PARAM_PARSER = new IntegerParameterParser();
    private static final ParameterObjectParser NUMBER_PARAM_PARSER = new NumberParameterParser();
    private static final ParameterObjectParser STRING_PARAM_PARSER = new StringParameterParser();

    @Override
    public DConnectProfileSpec parseJson(final JSONObject json) throws JSONException {
        DConnectProfileSpec.Builder builder = new DConnectProfileSpec.Builder();
        builder.setBundle(toBundle(json));

        String basePath = json.optString(KEY_BASE_PATH, null);
        if (basePath != null) {
            String[] parts = basePath.split("/");
            if (parts.length != 3) {
                throw new JSONException("basePath is invalid: " + basePath);
            }
            builder.setApiName(parts[1]);
            builder.setProfileName(parts[2]);
        }
        JSONObject pathsObj = json.getJSONObject(KEY_PATHS);
        for (Iterator<String> it = pathsObj.keys(); it.hasNext(); ) {
            String path = it.next();
            JSONObject pathObj = pathsObj.getJSONObject(path);
            for (DConnectSpecConstants.Method method : DConnectSpecConstants.Method.values()) {
                JSONObject opObj = pathObj.optJSONObject(method.getName().toLowerCase());
                if (opObj == null) {
                    continue;
                }
                DConnectApiSpec apiSpec = OPERATION_OBJECT_PARSER.parseJson(method, opObj);
                if (apiSpec != null) {
                    builder.addApiSpec(path, method, apiSpec);
                }
            }
        }
        return builder.build();
    }

    private Bundle toBundle(final JSONObject jsonObj) throws JSONException {
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

    private void putArray(final Bundle bundle, final String name, final JSONArray jsonArray)
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
            } else if (base == Boolean.class) {
                boolean[] array = new boolean[length];
                for (int i = 0; i < length; i++) {
                    array[i] = jsonArray.getBoolean(i);
                }
                bundle.putBooleanArray(name, array);
            } else if (base == JSONObject.class) {
                Bundle[] array = new Bundle[length];
                for (int i = 0; i < length; i++) {
                    array[i] = toBundle(jsonArray.getJSONObject(i));
                }
                bundle.putParcelableArray(name, array);
            }
        }
    }

    private Class getBaseClass(final JSONArray array) throws JSONException {
        Class cls = array.get(0).getClass();
        for (int i = 1; i < array.length(); i++) {
            if (cls != array.get(i).getClass()) {
                return null;
            }
        }
        return cls;
    }

    private static ParameterObjectParser getParameterParser(final JSONObject json) throws JSONException {
        String type = json.getString(ParameterObjectParser.KEY_TYPE);
        DataType paramType =  DataType.fromName(type);
        if (paramType == null) {
            throw new JSONException("Unknown parameter type '" + type + "' is specified.");
        }
        switch (paramType) {
            case BOOLEAN:
                return BOOLEAN_PARAM_PARSER;
            case INTEGER:
                return INTEGER_PARAM_PARSER;
            case NUMBER:
                return NUMBER_PARAM_PARSER;
            case STRING:
                return STRING_PARAM_PARSER;
            case FILE:
                return FILE_PARAM_PARSER;
            case ARRAY:
                return ARRAY_PARAM_PARSER;
            default:
                throw new IllegalArgumentException();
        }
    }

    private static ParameterObjectParser getItemParser(final JSONObject json) throws JSONException {
        String type = json.getString(ParameterObjectParser.KEY_TYPE);
        DataType paramType =  DataType.fromName(type);
        if (paramType == null) {
            throw new JSONException("Unknown parameter type '" + type + "' is specified.");
        }
        switch (paramType) {
            case BOOLEAN:
                return BOOLEAN_PARAM_PARSER;
            case INTEGER:
                return INTEGER_PARAM_PARSER;
            case NUMBER:
                return NUMBER_PARAM_PARSER;
            case STRING:
                return STRING_PARAM_PARSER;
            case FILE:
                return FILE_PARAM_PARSER;
            case ARRAY:
                return ARRAY_PARAM_PARSER;
            default:
                throw new IllegalArgumentException();
        }
    }

    private interface OperationObjectParser {

        String KEY_X_TYPE = "x-type";
        String KEY_PARAMETERS = "parameters";

        DConnectApiSpec parseJson(Method method, JSONObject json) throws JSONException;
    }

    private interface ParameterObjectParser {

        String KEY_NAME = "name";
        String KEY_REQUIRED = "required";
        String KEY_TYPE = "type";

        DConnectParameterSpec parseJson(JSONObject json) throws JSONException;
    }

    private static class ArrayParameterParser implements ParameterObjectParser {

        @Override
        public DConnectParameterSpec parseJson(final JSONObject json) throws JSONException {
            ArrayDataSpec dataSpec = (ArrayDataSpec) ARRAY_ITEMS_PARSER.parseJson(json);

            ArrayParameterSpec.Builder builder = new ArrayParameterSpec.Builder();
            builder.setName(json.getString(KEY_NAME));
            if (json.has(KEY_REQUIRED)) {
                builder.setRequired(json.getBoolean(KEY_REQUIRED));
            }
            builder.setItemsSpec(dataSpec.getItemsSpec());
            builder.setMaxLength(dataSpec.getMaxLength());
            builder.setMinLength(dataSpec.getMinLength());
            return builder.build();
        }
    }

    private static class BooleanParameterParser implements ParameterObjectParser {

        @Override
        public DConnectParameterSpec parseJson(final JSONObject json) throws JSONException {
            BooleanDataSpec dataSpec = (BooleanDataSpec) BOOLEAN_ITEMS_PARSER.parseJson(json);

            BooleanParameterSpec.Builder builder = new BooleanParameterSpec.Builder();
            builder.setName(json.getString(KEY_NAME));
            if (json.has(KEY_REQUIRED)) {
                builder.setRequired(json.getBoolean(KEY_REQUIRED));
            }
            builder.setEnum(dataSpec.getEnum());
            return builder.build();
        }
    }

    private static class IntegerParameterParser implements ParameterObjectParser {

        @Override
        public DConnectParameterSpec parseJson(final JSONObject json) throws JSONException {
            IntegerDataSpec dataSpec = (IntegerDataSpec) INTEGER_ITEMS_PARSER.parseJson(json);

            IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
            builder.setName(json.getString(KEY_NAME));
            if (json.has(KEY_REQUIRED)) {
                builder.setRequired(json.getBoolean(KEY_REQUIRED));
            }
            builder.setFormat(dataSpec.getFormat());
            builder.setMaximum(dataSpec.getMaximum());
            builder.setMinimum(dataSpec.getMinimum());
            builder.setExclusiveMaximum(dataSpec.isExclusiveMaximum());
            builder.setExclusiveMinimum(dataSpec.isExclusiveMinimum());
            builder.setEnum(dataSpec.getEnum());
            return builder.build();
        }
    }

    private static class NumberParameterParser implements ParameterObjectParser {

        @Override
        public DConnectParameterSpec parseJson(final JSONObject json) throws JSONException {
            NumberDataSpec dataSpec = (NumberDataSpec) NUMBER_ITEMS_PARSER.parseJson(json);

            NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
            builder.setName(json.getString(KEY_NAME));
            if (json.has(KEY_REQUIRED)) {
                builder.setRequired(json.getBoolean(KEY_REQUIRED));
            }
            builder.setFormat(dataSpec.getFormat());
            builder.setMaximum(dataSpec.getMaximum());
            builder.setMinimum(dataSpec.getMinimum());
            builder.setExclusiveMaximum(dataSpec.isExclusiveMaximum());
            builder.setExclusiveMinimum(dataSpec.isExclusiveMinimum());
            builder.setEnum(dataSpec.getEnum());
            return builder.build();
        }
    }

    private static class StringParameterParser implements ParameterObjectParser {

        @Override
        public DConnectParameterSpec parseJson(final JSONObject json) throws JSONException {
            StringDataSpec dataSpec = (StringDataSpec) STRING_ITEMS_PARSER.parseJson(json);

            StringParameterSpec.Builder builder = new StringParameterSpec.Builder();
            builder.setName(json.getString(KEY_NAME));
            if (json.has(KEY_REQUIRED)) {
                builder.setRequired(json.getBoolean(KEY_REQUIRED));
            }
            builder.setFormat(dataSpec.getFormat());
            builder.setMaxLength(dataSpec.getMaxLength());
            builder.setMinLength(dataSpec.getMinLength());
            builder.setEnum(dataSpec.getEnum());
            return builder.build();
        }
    }

    private static class FileParameterParser implements ParameterObjectParser {

        @Override
        public DConnectParameterSpec parseJson(final JSONObject json) throws JSONException {
            FileParameterSpec.Builder builder = new FileParameterSpec.Builder();
            builder.setName(json.getString(KEY_NAME));
            builder.setRequired(json.getBoolean(KEY_REQUIRED));
            return builder.build();
        }
    }

    private interface ItemsObjectParser {

        String KEY_FORMAT = "format";
        String KEY_MAXIMUM = "maximum";
        String KEY_MINIMUM = "minimum";
        String KEY_EXCLUSIVE_MAXIMUM = "exclusiveMaximum";
        String KEY_EXCLUSIVE_MINIMUM = "exclusiveMinimum";
        String KEY_MAX_LENGTH = "maxLength";
        String KEY_MIN_LENGTH = "minLength";
        String KEY_ENUM = "enum";
        String KEY_ITEMS = "items";

        DConnectDataSpec parseJson(JSONObject json) throws JSONException;
    }

    private static class ArrayItemsObjectParser implements ItemsObjectParser {

        @Override
        public DConnectDataSpec parseJson(final JSONObject json) throws JSONException {
            ArrayDataSpec.Builder builder = new ArrayDataSpec.Builder();

            JSONObject itemsObj = json.getJSONObject(KEY_ITEMS);
            ItemsObjectParser parser = getItemsParser(itemsObj);
            DConnectDataSpec itemSpec = parser.parseJson(itemsObj);
            builder.setItemsSpec(itemSpec);

            if (json.has(KEY_MAX_LENGTH)) {
                builder.setMaxLength(json.getInt(KEY_MAX_LENGTH));
            }
            if (json.has(KEY_MIN_LENGTH)) {
                builder.setMinLength(json.getInt(KEY_MIN_LENGTH));
            }
            return builder.build();
        }

        public ItemsObjectParser getItemsParser(final JSONObject json) throws JSONException {
            String type = json.getString(ParameterObjectParser.KEY_TYPE);
            DataType paramType =  DataType.fromName(type);
            if (paramType == null) {
                throw new JSONException("Unknown parameter type '" + type + "' is specified.");
            }
            switch (paramType) {
                case BOOLEAN:
                    return BOOLEAN_ITEMS_PARSER;
                case INTEGER:
                    return INTEGER_ITEMS_PARSER;
                case NUMBER:
                    return NUMBER_ITEMS_PARSER;
                case STRING:
                    return STRING_ITEMS_PARSER;
                case ARRAY:
                    return this;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    private static class BooleanItemsObjectParser implements ItemsObjectParser {

        @Override
        public DConnectDataSpec parseJson(final JSONObject json) throws JSONException {
            BooleanDataSpec.Builder builder = new BooleanDataSpec.Builder();
            if (json.has(KEY_ENUM)) {
                JSONArray array = json.getJSONArray(KEY_ENUM);
                Boolean[] enumList = new Boolean[array.length()];
                for (int i = 0; i < array.length(); i++) {
                    enumList[i] = array.getBoolean(i);
                }
                builder.setEnum(enumList);
            }
            return builder.build();
        }
    }

    private static class IntegerItemsObjectParser implements ItemsObjectParser {

        @Override
        public DConnectDataSpec parseJson(final JSONObject json) throws JSONException {
            IntegerDataSpec.Builder builder = new IntegerDataSpec.Builder();
            if (json.has(KEY_FORMAT)) {
                DataFormat format = DataFormat.fromName(json.optString(KEY_FORMAT));
                if (format == null) {
                    throw new IllegalArgumentException("format is invalid: " + json.optString(KEY_FORMAT));
                }
                builder.setFormat(format);
            }
            if (json.has(KEY_MAXIMUM)) {
                builder.setMaximum(json.getLong(KEY_MAXIMUM));
            }
            if (json.has(KEY_MINIMUM)) {
                builder.setMinimum(json.getLong(KEY_MINIMUM));
            }
            if (json.has(KEY_EXCLUSIVE_MAXIMUM)) {
                builder.setExclusiveMaximum(json.getBoolean(KEY_EXCLUSIVE_MAXIMUM));
            }
            if (json.has(KEY_EXCLUSIVE_MINIMUM)) {
                builder.setExclusiveMinimum(json.getBoolean(KEY_EXCLUSIVE_MINIMUM));
            }
            if (json.has(KEY_ENUM)) {
                JSONArray array = json.getJSONArray(KEY_ENUM);
                Long[] enumList = new Long[array.length()];
                for (int i = 0; i < array.length(); i++) {
                    enumList[i] = array.getLong(i);
                }
                builder.setEnum(enumList);
            }
            return builder.build();
        }
    }

    private static class NumberItemsObjectParser implements ItemsObjectParser {

        @Override
        public DConnectDataSpec parseJson(final JSONObject json) throws JSONException {
            NumberDataSpec.Builder builder = new NumberDataSpec.Builder();
            if (json.has(KEY_FORMAT)) {
                DataFormat format = DataFormat.fromName(json.optString(KEY_FORMAT));
                if (format == null) {
                    throw new IllegalArgumentException("format is invalid: " + json.optString(KEY_FORMAT));
                }
                builder.setFormat(format);
            }
            if (json.has(KEY_MAXIMUM)) {
                builder.setMaximum(json.getDouble(KEY_MAXIMUM));
            }
            if (json.has(KEY_MINIMUM)) {
                builder.setMinimum(json.getDouble(KEY_MINIMUM));
            }
            if (json.has(KEY_EXCLUSIVE_MAXIMUM)) {
                builder.setExclusiveMaximum(json.getBoolean(KEY_EXCLUSIVE_MAXIMUM));
            }
            if (json.has(KEY_EXCLUSIVE_MINIMUM)) {
                builder.setExclusiveMinimum(json.getBoolean(KEY_EXCLUSIVE_MINIMUM));
            }
            if (json.has(KEY_ENUM)) {
                JSONArray array = json.getJSONArray(KEY_ENUM);
                Double[] enumList = new Double[array.length()];
                for (int i = 0; i < array.length(); i++) {
                    enumList[i] = array.getDouble(i);
                }
                builder.setEnum(enumList);
            }
            return builder.build();
        }
    }

    private static class StringItemsObjectParser implements ItemsObjectParser {

        @Override
        public DConnectDataSpec parseJson(final JSONObject json) throws JSONException {
            StringDataSpec.Builder builder = new StringDataSpec.Builder();
            if (json.has(KEY_FORMAT)) {
                DataFormat format = DataFormat.fromName(json.getString(KEY_FORMAT));
                if (format == null) {
                    throw new IllegalArgumentException("format is invalid: " + json.getString(KEY_FORMAT));
                }
                builder.setFormat(format);
            }
            if (json.has(KEY_MAX_LENGTH)) {
                builder.setMaxLength(json.getInt(KEY_MAX_LENGTH));
            }
            if (json.has(KEY_MIN_LENGTH)) {
                builder.setMinLength(json.getInt(KEY_MIN_LENGTH));
            }
            if (json.has(KEY_ENUM)) {
                JSONArray array = json.getJSONArray(KEY_ENUM);
                String[] enumList = new String[array.length()];
                for (int i = 0; i < array.length(); i++) {
                    enumList[i] = array.getString(i);
                }
                builder.setEnum(enumList);
            }
            return builder.build();
        }
    }

}

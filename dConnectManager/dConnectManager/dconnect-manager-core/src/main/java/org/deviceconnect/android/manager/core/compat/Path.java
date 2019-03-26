/*
 Path.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.core.compat;


/**
 * リクエストパス.
 *
 * @author NTT DOCOMO, INC.
 */
class Path {

    static final String SEPARATOR = "/";
    final String mExpression;
    final String mProfileName;
    final String mInterfaceName;
    final String mAttributeName;

    static Path parsePath(final String pathExpression) {
        String[] array = pathExpression.split(SEPARATOR);
        if (array.length == 2) {
            return new Path(array[1]);
        } else if (array.length == 3) {
            return new Path(array[1], array[2]);
        } else if (array.length == 4) {
            return new Path(array[1], array[2], array[3]);
        } else {
            throw new IllegalArgumentException();
        }
    }

    Path(final String profileName, final String interfaceName, final String attributeName) {
        mProfileName = profileName;
        mInterfaceName = interfaceName;
        mAttributeName = attributeName;
        if (profileName != null && interfaceName != null && attributeName != null) {
            mExpression = "/" + profileName + "/" + interfaceName + "/" + attributeName;
        } else if (profileName != null && interfaceName == null && attributeName != null) {
            mExpression = "/" + profileName + "/" + attributeName;
        } else if (profileName != null) {
            mExpression = "/" + profileName;
        } else {
            throw new IllegalArgumentException();
        }
        toLowerCase();
    }

    Path(final String profileName, final String attributeName) {
        this(profileName, null, attributeName);
    }

    Path(final String profileName) {
        this(profileName, null, null);
    }

    private void toLowerCase() {
        mExpression.toLowerCase();
        mProfileName.toLowerCase();
        if (mInterfaceName != null) {
            mInterfaceName.toLowerCase();
        }
        if (mAttributeName != null) {
            mAttributeName.toLowerCase();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Path)) {
            return false;
        }
        return mExpression.equals(((Path) o).mExpression);
    }

    public boolean matches(final Path path) {
        if (!mProfileName.equalsIgnoreCase(path.mProfileName)) {
            return false;
        }
        if (mInterfaceName != null && !mInterfaceName.equalsIgnoreCase(path.mInterfaceName)) {
            return false;
        }
        if (mAttributeName != null && !mAttributeName.equalsIgnoreCase(path.mAttributeName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return mExpression;
    }

    @Override
    public int hashCode() {
        return mExpression.hashCode();
    }
}

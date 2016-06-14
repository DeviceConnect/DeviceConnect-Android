package org.deviceconnect.android.manager.compat;


class Path {

    static final String SEPARATOR = "/";
    final String mExpression;
    final String mProfileName;
    final String mInterfaceName;
    final String mAttributeName;

    Path(final String pathExpression) {
        mExpression = pathExpression;
        String[] array = pathExpression.split(SEPARATOR);
        if (array.length == 1) {
            mProfileName = array[0];
            mInterfaceName = null;
            mAttributeName = null;
        } else if (array.length == 2) {
            mProfileName = array[0];
            mInterfaceName = null;
            mAttributeName = array[1];
        } else if (array.length == 3) {
            mProfileName = array[0];
            mInterfaceName = array[1];
            mAttributeName = array[2];
        } else {
            throw new IllegalArgumentException();
        }
        toLowerCase();
    }

    Path(final String profileName, final String interfaceName, final String attributeName) {
        mProfileName = profileName;
        mInterfaceName = interfaceName;
        mAttributeName = attributeName;
        if (profileName != null && interfaceName != null && attributeName != null) {
            mExpression = profileName + "/" + interfaceName + "/" + attributeName;
        } else if (profileName != null && interfaceName == null && attributeName != null) {
            mExpression = profileName + "/" + attributeName;
        } else if (profileName != null) {
            mExpression = profileName;
        } else {
            throw new IllegalArgumentException();
        }
        toLowerCase();
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

    @Override
    public String toString() {
        return mExpression;
    }

    @Override
    public int hashCode() {
        return mExpression.hashCode();
    }
}

package org.deviceconnect.android.manager.util;


public class VersionName implements Comparable {

    private final int[] mVersion;

    private final String mExpression;

    private VersionName(final int[] version) {
        mVersion = version;

        StringBuilder exp = new StringBuilder();
        for (int i = 0; i < mVersion.length; i++) {
            if (i > 0) {
                exp.append(".");
            }
            exp.append(Integer.toString(mVersion[i]));
        }
        mExpression = exp.toString();
    }

    public static VersionName parse(final String versionName) {
        if (versionName == null) {
            return null;
        }
        String[] array = versionName.split("\\.");
        if (array.length != 3) {
            return null;
        }
        try {
            int[] version = new int[array.length];
            for (int i = 0; i < version.length; i++) {
                version[i] = Integer.parseInt(array[i]);
                if (version[i] < 0) {
                    return null;
                }
            }
            return new VersionName(version);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VersionName)) {
            return false;
        }
        return this.compareTo(o) == 0;
    }

    @Override
    public int compareTo(final Object another) {
        VersionName that = (VersionName) another;
        for (int i = 0; i < mVersion.length; i++) {
            if (mVersion[i] > that.mVersion[i]) {
                return 1;
            } else if (mVersion[i] < that.mVersion[i]) {
                return -1;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return mExpression;
    }
}

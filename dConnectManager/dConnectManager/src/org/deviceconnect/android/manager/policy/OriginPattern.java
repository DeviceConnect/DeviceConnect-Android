/*
 OriginPattern.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.policy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pattern of origin.
 * @author NTT DOCOMO, INC.
 */
public class OriginPattern {

    /**
     * ID.
     */
    final long mId;

    /**
     * Pattern.
     */
    final Pattern mPattern;

    /**
     * The string expression of origin pattern.
     */
    final String mPatternExpression;

    /**
     * Constructor.
     * 
     * @param id ID
     * @param pattern Pattern expression
     */
    public OriginPattern(final long id, final String pattern) {
        mId = id;
        mPatternExpression = pattern;
        mPattern = Pattern.compile(convertGlobToRegEx(pattern));
    }

    /**
     * Check whether the specified origin matches this pattern.
     * @param origin Origin of request
     * @return <code>true</code> if the specified origin matches this pattern,
     *      otherwise <code>false</code>
     */
    public boolean matches(final String origin) {
        Matcher matcher = mPattern.matcher(origin);
        return matcher.matches();
    }

    /**
     * Convert glob to regular expression.
     * @param glob Glob
     * @return Regular expression
     */
    private String convertGlobToRegEx(final String glob) {
        String out = "^";
        for (int i = 0; i < glob.length(); i++) {
            final char c = glob.charAt(i);
            switch (c) {
            case '*': out += ".*"; break;
            case '?': out += '.'; break;
            case '.': out += "\\."; break;
            case '\\': out += "\\\\"; break;
            default: out += c; break;
            }
        }
        out += '$';
        return out;
    }

    /**
     * Returns the glob expression of this pattern.
     * @return Glob expression
     */
    public String getGlob() {
        return mPatternExpression;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof OriginPattern)) {
            return false;
        }
        return o.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
        return (int) mId;
    }

}

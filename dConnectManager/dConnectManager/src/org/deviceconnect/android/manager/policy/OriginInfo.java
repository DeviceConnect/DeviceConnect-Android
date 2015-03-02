/*
 OriginInfo.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.manager.policy;


/**
 * Information of origin.
 * 
 * @author NTT DOCOMO, INC.
 */
public class OriginInfo {

    /**
     * ID.
     */
    final long mId;

    /**
     * The origin.
     */
    final Origin mOrigin;

    /**
     * The title.
     */
    final String mTitle;

    /**
     * The registration date.
     */
    final long mDate;

    /**
     * Constructor.
     * 
     * @param id row ID in database.
     * @param origin the origin
     * @param title the title of origin
     * @param date the date that the origin is added to whitelist.
     */
    public OriginInfo(final long id, final Origin origin, final String title, final long date) {
        mId = id;
        mOrigin = origin;
        mTitle = title;
        mDate = date;
    }

    /**
     * Check whether the specified origin matches this origin.
     * @param origin Origin of request
     * @return <code>true</code> if the specified origin matches this origin,
     *      otherwise <code>false</code>
     */
    public boolean matches(final Origin origin) {
        return mOrigin.matches(origin);
    }

    /**
     * Gets origin.
     * @return origin
     */
    public Origin getOrigin() {
        return mOrigin;
    }

    /**
     * Gets the title of origin.
     * @return the title of origin
     */
    public String getTitle() {
        return mTitle;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof OriginInfo)) {
            return false;
        }
        return o.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
        return (int) mId;
    }

}

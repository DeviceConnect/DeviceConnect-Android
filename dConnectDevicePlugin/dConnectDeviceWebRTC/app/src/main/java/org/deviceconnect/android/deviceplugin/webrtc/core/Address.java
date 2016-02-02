/*
 Address.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.core;

import org.deviceconnect.android.profile.VideoChatProfileConstants;

/**
 * Address.
 *
 * @author NTT DOCOMO, INC.
 */
public class Address {
    /**
     * Name.
     */
    private String mName;

    /**
     * Id of address.
     */
    private String mAddressId;

    /**
     * State.
     */
    private VideoChatProfileConstants.State mState = VideoChatProfileConstants.State.IDLE;

    /**
     * Constructor.
     */
    public Address() {
    }

    /**
     * Get a name.
     * @return name
     */
    public String getName() {
        return mName;
    }

    /**
     * Set a name.
     * @param name name of address
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Get a id of address.
     * @return id
     */
    public String getAddressId() {
        return mAddressId;
    }

    /**
     * Set a id of address.
     * @param addressId id
     */
    public void setAddressId(String addressId) {
        mAddressId = addressId;
    }

    /**
     * Get a state.
     * @return state
     */
    public VideoChatProfileConstants.State getState() {
        return mState;
    }

    /**
     * Set a state.
     * @param state state
     */
    public void setState(VideoChatProfileConstants.State state) {
        mState = state;
    }

    @Override
    public String toString() {
        StringBuilder build = new StringBuilder();
        build.append("Address { name: '" + mName + "', address:'" + mAddressId + "'}");
        return build.toString();
    }
}

/*
 HumanDetectEventUtils.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.humandetect;

import java.util.List;

/** 
 * HVC Device Event Utils.
 * 
 * @author NTT DOCOMO, INC.
 */
public final class HumanDetectEventUtils {

    /**
     * Constructor.
     */
    private HumanDetectEventUtils() {
        
    }
    
    /**
     * search event data by detectKind and sessionKey.
     * @param eventArray event array
     * @param detectKind detect kind
     * @param sessionKey session key
     * @return not null: found event data / null: not found
     */
    public static HumanDetectEvent search(final List<HumanDetectEvent> eventArray,
            final HumanDetectKind detectKind, final String sessionKey) {
        for (HumanDetectEvent event : eventArray) {
            if (event.getKind() == detectKind
            &&  event.getSessionKey().equals(sessionKey)) {
                return event;
            }
            
        }
        return null;
    }

    /**
     * remove event data by detectKind and sessionKey.
     * @param eventArray event array
     * @param detectKind detect kind
     * @param sessionKey session key
     */
    public static void remove(final List<HumanDetectEvent> eventArray, final HumanDetectKind detectKind,
            final String sessionKey) {
        int count = eventArray.size();
        for (int index = (count - 1); index >= 0; index--) {
            HumanDetectEvent event = eventArray.get(index);
            if (detectKind == event.getKind() && sessionKey.equals(event.getSessionKey())) {
                eventArray.remove(index);
            }
        }
    }

}

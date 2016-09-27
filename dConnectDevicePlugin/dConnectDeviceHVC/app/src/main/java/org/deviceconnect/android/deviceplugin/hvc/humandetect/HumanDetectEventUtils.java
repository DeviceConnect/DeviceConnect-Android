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
     * search event data by detectKind and origin.
     * @param eventArray event array
     * @param detectKind detect kind
     * @param origin origin
     * @return not null: found event data / null: not found
     */
    public static HumanDetectEvent search(final List<HumanDetectEvent> eventArray,
            final HumanDetectKind detectKind, final String origin) {
        for (HumanDetectEvent event : eventArray) {
            if (event.getKind() == detectKind
            &&  event.getOrigin().equals(origin)) {
                return event;
            }
        }
        return null;
    }

    /**
     * remove event data by detectKind and origin.
     * @param eventArray event array
     * @param detectKind detect kind
     * @param origin session key
     */
    public static void remove(final List<HumanDetectEvent> eventArray, final HumanDetectKind detectKind,
            final String origin) {
        int count = eventArray.size();
        for (int index = (count - 1); index >= 0; index--) {
            HumanDetectEvent event = eventArray.get(index);
            if (detectKind == event.getKind() && origin.equals(event.getOrigin())) {
                eventArray.remove(index);
            }
        }
    }

    /**
     * remove event data by origin.
     * @param eventArray event array
     * @param origin session key
     */
    public static void remove(final List<HumanDetectEvent> eventArray, final String origin) {
        int count = eventArray.size();
        for (int index = (count - 1); index >= 0; index--) {
            HumanDetectEvent event = eventArray.get(index);
            if (origin.equals(event.getOrigin())) {
                eventArray.remove(index);
            }
        }
    }

}

/*
 SocketState.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.observer.util;

/**
 * AndroidのSocketの状態を持つ.
 * @author NTT DOCOMO, INC.
 */
public enum SocketState {
    /**
     * TCP_UNKNWON.
     */
    TCP_UNKNOW,
    /**
     * TCP_ESTABLISHED.
     */
    TCP_ESTABLISHED,
    /**
     * TCP_SYN_SEND.
     */
    TCP_SYN_SEND,
    /**
     * TCP_SYN_RECV.
     */
    TCP_SYN_RECV,
    /**
     * TCP_FIN_WAIT1.
     */
    TCP_FIN_WAIT1,
    /**
     * TCP_FIN_WAIT2.
     */
    TCP_FIN_WAIT2,
    /**
     * TCP_TIME_WAIT.
     */
    TCP_TIME_WAIT,
    /**
     * TCP_CLOSE.
     */
    TCP_CLOSE,
    /**
     * TCP_CLOSE_WAIT.
     */
    TCP_CLOSE_WAIT,
    /**
     * TCP_LAST_ACK.
     */
    TCP_LAST_ACK,
    /**
     * TCP_LISTEN.
     */
    TCP_LISTEN,
    /**
     * TCP_CLOSING.
     */
    TCP_CLOSING,
    /**
     * TCP_MAX_STATES.
     */
    TCP_MAX_STATES,
    /**
     * UDP_LISTEN.
     */
    UDP_LISTEN;

}

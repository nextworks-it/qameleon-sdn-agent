/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.netconf.util.messages;

/**
 * Known NETCONF framing mechanisms.
 */
public enum FramingMechanism {
    /**
     * Chunked framing mechanism.
     *
     * @see <a href="http://tools.ietf.org/html/rfc6242#section-4.2">Chunked
     *      framing mechanism</a>
     */
    CHUNK,
    /**
     * End-of-Message framing mechanism.
     *
     * @see <a
     *      href="http://tools.ietf.org/html/rfc6242#section-4.3">End-of-message
     *      framing mechanism</a>
     */
    EOM
}

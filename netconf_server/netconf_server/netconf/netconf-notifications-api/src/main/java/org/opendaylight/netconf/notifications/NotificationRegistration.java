/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.netconf.notifications;

/**
 * Generic registration, used as a base for other registration types.
 */
public interface NotificationRegistration extends AutoCloseable {

    // Overriden close does not throw any kind of checked exception

    /**
     * Close the registration.
     */
    @Override
    void close();
}

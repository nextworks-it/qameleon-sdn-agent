/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netconf.impl.osgi;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.util.*;

import org.checkerframework.checker.units.qual.K;
import org.opendaylight.netconf.api.DocumentedException;
import org.opendaylight.netconf.api.monitoring.NetconfMonitoringService;
import org.opendaylight.netconf.api.xml.XmlElement;
import org.opendaylight.netconf.api.xml.XmlNetconfConstants;
import org.opendaylight.netconf.api.xml.XmlUtil;
import org.opendaylight.netconf.impl.NetconfServerSession;
import org.opendaylight.netconf.impl.mapping.operations.DefaultCloseSession;
import org.opendaylight.netconf.impl.mapping.operations.DefaultNetconfOperation;
import org.opendaylight.netconf.impl.mapping.operations.DefaultStartExi;
import org.opendaylight.netconf.impl.mapping.operations.DefaultStopExi;
import org.opendaylight.netconf.mapping.api.HandlingPriority;
import org.opendaylight.netconf.mapping.api.NetconfOperation;
import org.opendaylight.netconf.mapping.api.NetconfOperationChainedExecution;
import org.opendaylight.netconf.mapping.api.NetconfOperationService;
import org.opendaylight.netconf.mapping.api.SessionAwareNetconfOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class NetconfOperationRouterImpl implements NetconfOperationRouter {

    private static final Logger LOG = LoggerFactory.getLogger(NetconfOperationRouterImpl.class);
    private final NetconfOperationService netconfOperationServiceSnapshot;
    private final Collection<NetconfOperation> allNetconfOperations;
    private ArrayList<Document> documentsRecevied=new ArrayList<Document>();

    public NetconfOperationRouterImpl(final NetconfOperationService netconfOperationServiceSnapshot,
                                      final NetconfMonitoringService netconfMonitoringService, final String sessionId) {
        this.netconfOperationServiceSnapshot = Preconditions.checkNotNull(netconfOperationServiceSnapshot);

        final Set<NetconfOperation> ops = new HashSet<>();
        ops.add(new DefaultCloseSession(sessionId, this));
        ops.add(new DefaultStartExi(sessionId));
        ops.add(new DefaultStopExi(sessionId));

        ops.addAll(netconfOperationServiceSnapshot.getNetconfOperations());
        allNetconfOperations = ImmutableSet.copyOf(ops);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public Document onNetconfMessage(final Document message, final NetconfServerSession session) throws
            DocumentedException {

        Preconditions.checkNotNull(allNetconfOperations, "Operation router was not initialized properly");

        final NetconfOperationExecution netconfOperationExecution;
        try {
            netconfOperationExecution = getNetconfOperationWithHighestPriority(message, session);
        } catch (IllegalArgumentException | IllegalStateException e) {
            final String messageAsString = XmlUtil.toString(message);
            LOG.warn("Unable to handle rpc {} on session {}", messageAsString, session, e);

            final DocumentedException.ErrorTag tag;
            if (e instanceof IllegalArgumentException) {
                tag = DocumentedException.ErrorTag.OPERATION_NOT_SUPPORTED;
            } else {
                tag = DocumentedException.ErrorTag.OPERATION_FAILED;
            }

            throw new DocumentedException(
                    String.format("Unable to handle rpc %s on session %s", messageAsString, session),
                    e, DocumentedException.ErrorType.APPLICATION,
                    tag, DocumentedException.ErrorSeverity.ERROR,
                    Collections.singletonMap(tag.toString(), e.getMessage()));
        } catch (final RuntimeException e) {
            throw handleUnexpectedEx("sort", e);
        }

        try {
            return executeOperationWithHighestPriority(message, netconfOperationExecution);
        } catch (final RuntimeException e) {
            throw handleUnexpectedEx("execution", e);
        }
    }

    @Override
    public void close() {
        netconfOperationServiceSnapshot.close();
    }

    private static DocumentedException handleUnexpectedEx(final String op, final Exception exception) {
        LOG.error("Unexpected exception during netconf operation {}", op, exception);
        return new DocumentedException("Unexpected error",
                DocumentedException.ErrorType.APPLICATION,
                DocumentedException.ErrorTag.OPERATION_FAILED,
                DocumentedException.ErrorSeverity.ERROR,
                Collections.singletonMap(DocumentedException.ErrorSeverity.ERROR.toString(), exception.toString()));
    }

    private static Document executeOperationWithHighestPriority(final Document message,
            final NetconfOperationExecution netconfOperationExecution) throws DocumentedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Forwarding netconf message {} to {}", XmlUtil.toString(message), netconfOperationExecution.netconfOperation);
        }

        return netconfOperationExecution.execute(message);
    }

    private NetconfOperationExecution getNetconfOperationWithHighestPriority(
            final Document message, final NetconfServerSession session) throws DocumentedException {

        final NavigableMap<HandlingPriority, NetconfOperation> sortedByPriority = getSortedNetconfOperationsWithCanHandle(message, session);

        if (sortedByPriority.isEmpty()) {
            throw new IllegalArgumentException(String.format("No %s available to handle message %s",
                    NetconfOperation.class.getName(), XmlUtil.toString(message)));
        }

        return NetconfOperationExecution.createExecutionChain(sortedByPriority, sortedByPriority.lastKey());
    }

    private TreeMap<HandlingPriority, NetconfOperation> getSortedNetconfOperationsWithCanHandle(
            final Document message, final NetconfServerSession session) throws DocumentedException {
        final TreeMap<HandlingPriority, NetconfOperation> sortedPriority = new TreeMap<>();

        for (final NetconfOperation netconfOperation : allNetconfOperations) {
            LOG.info("NetconfOperation.class.getName()"+NetconfOperation.class.getName());
            final HandlingPriority handlingPriority = netconfOperation.canHandle(message);
            if (netconfOperation instanceof DefaultNetconfOperation) {
                ((DefaultNetconfOperation) netconfOperation).setNetconfSession(session);
            }
            if (netconfOperation instanceof SessionAwareNetconfOperation) {
                ((SessionAwareNetconfOperation) netconfOperation).setSession(session);
            }
            if (!handlingPriority.equals(HandlingPriority.CANNOT_HANDLE)) {

                Preconditions.checkState(!sortedPriority.containsKey(handlingPriority),
                        "Multiple %s available to handle message %s with priority %s, %s and %s",
                        NetconfOperation.class.getName(), message, handlingPriority, netconfOperation, sortedPriority.get(handlingPriority));
                sortedPriority.put(handlingPriority, netconfOperation);
            }
        }
        return sortedPriority;
    }




    /*-----------------------------------------------------------------------------------------------------------------------*/
    private static final class NetconfOperationExecution implements NetconfOperationChainedExecution {
        private final NetconfOperation netconfOperation;
        private final NetconfOperationChainedExecution subsequentExecution;

        private NetconfOperationExecution(final NetconfOperation netconfOperation,
                                          final NetconfOperationChainedExecution subsequentExecution) {
            this.netconfOperation = netconfOperation;
            this.subsequentExecution = subsequentExecution;
        }

        @Override
        public boolean isExecutionTermination() {
            return false;
        }

        @Override
        public Document execute(final Document message) throws DocumentedException {
            LOG.info("\nClass name of netconfOperation: "+ netconfOperation.getClass().getName());
            LOG.info("message into NetconfOperationExecution: "+XmlUtil.toString(message));
            LOG.info("\n");
            return netconfOperation.handle(message, subsequentExecution);
        }

        public static NetconfOperationExecution createExecutionChain(
                final NavigableMap<HandlingPriority, NetconfOperation> sortedByPriority,
                final HandlingPriority handlingPriority) {
            final NetconfOperation netconfOperation = sortedByPriority.get(handlingPriority);
            final HandlingPriority subsequentHandlingPriority = sortedByPriority.lowerKey(handlingPriority);

            NetconfOperationChainedExecution subsequentExecution = null;

            if (subsequentHandlingPriority != null) {
                subsequentExecution = createExecutionChain(sortedByPriority, subsequentHandlingPriority);
            } else {
                subsequentExecution = EXECUTION_TERMINATION_POINT;
            }

            return new NetconfOperationExecution(netconfOperation, subsequentExecution);
        }
    }

    @Override
    public String toString() {
        return "NetconfOperationRouterImpl{" + "netconfOperationServiceSnapshot=" + netconfOperationServiceSnapshot
                + '}';
    }
}

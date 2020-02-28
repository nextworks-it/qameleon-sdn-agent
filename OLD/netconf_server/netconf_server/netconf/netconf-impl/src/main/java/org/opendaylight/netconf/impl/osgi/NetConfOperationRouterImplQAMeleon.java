package org.opendaylight.netconf.impl.osgi;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.opendaylight.netconf.api.DocumentedException;
import org.opendaylight.netconf.api.monitoring.NetconfMonitoringService;
import org.opendaylight.netconf.impl.NetconfServerSession;
import org.opendaylight.netconf.impl.mapping.operations.DefaultCloseSession;
import org.opendaylight.netconf.impl.mapping.operations.DefaultStartExi;
import org.opendaylight.netconf.impl.mapping.operations.DefaultStopExi;
import org.opendaylight.netconf.mapping.api.NetconfOperation;
import org.opendaylight.netconf.mapping.api.NetconfOperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NetConfOperationRouterImplQAMeleon implements NetconfOperationRouter{

    private static final Logger LOG = LoggerFactory.getLogger(NetConfOperationRouterImplQAMeleon.class);
    private NetconfOperationService netconfOperationServiceSnapshot;
    private Collection<NetconfOperation> allNetconfOperations;
    private ArrayList<Document> documentsRecevied=new ArrayList<Document>();



    public NetConfOperationRouterImplQAMeleon(final NetconfOperationService netconfOperationServiceSnapshot,
                                      final NetconfMonitoringService netconfMonitoringService, final String sessionId) {
        this.netconfOperationServiceSnapshot = Preconditions.checkNotNull(netconfOperationServiceSnapshot);

        final Set<NetconfOperation> ops = new HashSet<>();
        ops.add(new DefaultCloseSession(sessionId, this));
        ops.add(new DefaultStartExi(sessionId));
        ops.add(new DefaultStopExi(sessionId));

        ops.addAll(netconfOperationServiceSnapshot.getNetconfOperations());

        allNetconfOperations = ImmutableSet.copyOf(ops);
    }

    @Override
    public Document onNetconfMessage(Document message, NetconfServerSession session) throws DocumentedException {
        return null;
    }

    @Override
    public void close() throws Exception {
        {
            netconfOperationServiceSnapshot.close();
        }
    }
}

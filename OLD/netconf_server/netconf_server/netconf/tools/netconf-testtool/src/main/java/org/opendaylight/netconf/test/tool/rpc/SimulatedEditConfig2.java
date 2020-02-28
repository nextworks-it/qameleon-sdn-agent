package org.opendaylight.netconf.test.tool.rpc;

import org.opendaylight.netconf.api.DocumentedException;
import org.opendaylight.netconf.api.xml.XmlElement;
import org.opendaylight.netconf.util.mapping.AbstractLastNetconfOperation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SimulatedEditConfig2  extends AbstractLastNetconfOperation {

    protected SimulatedEditConfig2(String netconfSessionIdForReporting) {
        super(netconfSessionIdForReporting);
    }

    @Override
    protected String getOperationName() {
        return "edit-config";
    }

    @Override
    protected Element handleWithNoSubsequentOperations(Document document, XmlElement operationElement) throws DocumentedException {
        return null;
    }
}

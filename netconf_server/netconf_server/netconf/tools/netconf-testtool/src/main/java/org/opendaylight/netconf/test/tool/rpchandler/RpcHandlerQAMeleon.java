package org.opendaylight.netconf.test.tool.rpchandler;

import org.opendaylight.netconf.api.xml.XmlElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.util.Optional;

public class RpcHandlerQAMeleon implements RpcHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RpcHandlerDefault.class);

    @Override
    public Optional<Document> getResponse(XmlElement rpcElement) {
        LOG.info("getResponse: {}", rpcElement.toString());
        return Optional.empty();
    }
}

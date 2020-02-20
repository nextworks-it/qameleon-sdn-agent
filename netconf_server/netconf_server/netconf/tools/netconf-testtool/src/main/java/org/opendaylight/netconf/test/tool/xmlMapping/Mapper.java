package org.opendaylight.netconf.test.tool.xmlMapping;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.opendaylight.netconf.api.DocumentedException;
import org.opendaylight.netconf.api.xml.XmlElement;
import org.opendaylight.netconf.api.xml.XmlUtil;
import org.opendaylight.yang.gen.v1.http.org.nextworks.qameleon.tpa.rev180508.TpaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.IOException;

public class Mapper {

    private static final Logger LOG = LoggerFactory.getLogger(Mapper.class);

    public Object mapXmltoObject(Document requestMsg){
        final XmlElement requestElement = XmlElement.fromDomDocument(requestMsg);
        //printMessageConent(requestElement);
        LOG.info("data store target is "+getTarget(requestElement));
        LOG.info("Object ot operate on : "+XmlUtil.toString(getConfigObject(requestElement)));
        String xmlString = XmlUtil.toString(getConfigObject(requestElement));
        XmlMapper xmlMapper = new XmlMapper();
        try {
            TpaBuilder tpaBuilder = xmlMapper.readValue(xmlString, TpaBuilder.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private String getTarget(XmlElement requestElement){
        try {
            requestElement=requestElement.getOnlyChildElement();
            XmlElement targetXmlElement = requestElement.getOnlyChildElement("target");
            return targetXmlElement.getChildElements().get(0).getName();
        } catch (DocumentedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private XmlElement getConfigObject(XmlElement requestElement){
        try {
            requestElement=requestElement.getOnlyChildElement();
            XmlElement targetXmlElement = requestElement.getOnlyChildElement("config");
            return targetXmlElement.getChildElements().get(0);
        } catch (DocumentedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void printMessaggeConent(XmlElement requestElement){
        LOG.info("Printing XML message");
        XmlElement editConfig = null;
        try {
            editConfig = requestElement.getOnlyChildElement();
            XmlElement target = editConfig.getChildElements().get(0);
            XmlElement config = editConfig.getChildElements().get(1);
            XmlElement tpa = config.getChildElements().get(0);

            LOG.info(editConfig.toString());
            LOG.info(target.toString());
            LOG.info(config.toString());
            LOG.info(tpa.toString());
            XmlElement xmlElement=tpa;

            while(xmlElement.getChildElements().size()>0){
                boolean childNotSet=true;
                for(XmlElement xmlElementChild: xmlElement.getChildElements()){
                    LOG.info("XML element: "+xmlElementChild.toString());
                    LOG.info("Text content: "+xmlElementChild.getTextContent());
                    if(xmlElementChild.getChildElements().size()>0 && childNotSet==true){
                        xmlElement=xmlElementChild;
                        childNotSet=false;
                    }
                }
                if(childNotSet==true){
                    break;
                }
            }

        } catch (DocumentedException e) {
            e.printStackTrace();
        }

    }

}

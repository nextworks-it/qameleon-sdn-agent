package org.opendaylight.netconf.test.tool.xmlMapping;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.onosproject.yang.gen.v1.http.org.nextworks.qameleon.tpa.rev20180508.tpa.DefaultTpa;
import org.opendaylight.netconf.api.DocumentedException;
import org.opendaylight.netconf.api.xml.XmlElement;
import org.opendaylight.netconf.api.xml.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Mapper {

    private static final Logger LOG = LoggerFactory.getLogger(Mapper.class);


    public Object mapXmltoObject(Document requestMsg){

        final XmlElement requestElement = XmlElement.fromDomDocument(requestMsg);
        printMessaggeConent(requestElement);
        LOG.info("---------------");

        //Explicit mapping: TODO not delete
        //tpaBuilder.setTpaId(Long.valueOf(requestMsg.getElementsByTagName("tpa-id").item(0).getTextContent()));
        //LOG.info(requestMsg.getElementsByTagName("port-frequency-info").item(0).getTextContent());
        //LOG.info("data store target is "+getTarget(requestElement));


        String myString = XmlUtil.toString(getConfigObject(requestElement));
        LOG.info("Object to operate on : "+myString);
        String result = setCamelCaseTagName(myString);

        LOG.info(result);
        XmlMapper xmlMapper = new XmlMapper();

        try {
            xmlMapper.readValue(result, DefaultTpa.TpaBuilder.class);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private String setCamelCaseTagName(String input){
        boolean capitalizeNext = false;
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char thisChar = input.charAt(i);
            if (thisChar == '-') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                output.append(String.valueOf(thisChar).toUpperCase());
                capitalizeNext = false;
            } else {
                output.append(thisChar);
                capitalizeNext = false;
            }
        }
        return output.toString();
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
                        String sampleStr = setCamelCaseTagName(xmlElementChild.getName())+"Builder";
                        sampleStr=sampleStr.substring(0, 1).toUpperCase() + sampleStr.substring(1);
                        LOG.info("Going to create "+sampleStr+" object");
                        Class<?> clazz = Class.forName("org.opendaylight.yang.gen.v1.http.org.nextworks.qameleon.tpa.rev180508.tpa.config."+sampleStr);
                        Constructor<?> ctor = clazz.getConstructor();
                        Object object = ctor.newInstance();
                        xmlElement=xmlElementChild;
                        childNotSet=false;
                    }
                }
                if(childNotSet==true){
                    break;
                }
            }

        } catch (DocumentedException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

}

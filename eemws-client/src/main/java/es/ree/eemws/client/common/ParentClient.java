/*
 * Copyright 2014 Red Eléctrica de España, S.A.U.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation, version 3 of the license.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTIBIILTY or FITNESS FOR A PARTICULAR PURPOSE. See GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 *
 * Any redistribution and/or modification of this program has to make
 * reference to Red Eléctrica de España, S.A.U. as the copyright owner of
 * the program.
 */
package es.ree.eemws.client.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import org.w3c.dom.Element;

import _504.iec62325.wss._1._0.MsgFaultMsg;
import _504.iec62325.wss._1._0.PortTFEDIType;
import _504.iec62325.wss._1._0.ServiceEME;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.OptionType;
import ch.iec.tc57._2011.schema.message.PayloadType;
import ch.iec.tc57._2011.schema.message.RequestMessage;
import ch.iec.tc57._2011.schema.message.ResponseMessage;
import es.ree.eemws.client.exception.ClientException;
import es.ree.eemws.client.handler.SendHandler;
import es.ree.eemws.core.utils.xml.XMLElementUtil;
import es.ree.eemws.core.utils.xml.XMLUtil;


/**
 * Parent class of messages that communicate with the Web Service EME.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public abstract class ParentClient {

    /** Service name of the Web Service EME. */
    private static final QName SERVICE_NAME = new QName("urn:iec62325.504:wss:1:0", "ServiceEME"); //$NON-NLS-1$ //$NON-NLS-2$

    /** WSDL file name. This avoids connection to the remote server just to retrieve it. */
    private static final String WSDL_FILE = "urn-iec62325-504-wss-1-0.wsdl"; //$NON-NLS-1$

    /** URL of the end point of the web service. */
    private URL endPoint = null;

    /** Check to sign the request. */
    private boolean signRequest = true;

    /** Check to verify the response. */
    private boolean verifyResponse = true;

    /** Certificate to sign request. */
    private X509Certificate certificate = null;

    /** Private key of the certificate. */
    private PrivateKey privateKey = null;

    /** Data of the message. */
    private MessageData messageData = null;

    /**
     * This method sets the URL of the end point of the web service.
     * @param url URL of the end point of the web service.
     * @throws MalformedURLException if the given address is not valid.
     */
    public final void setEndPoint(final String url) throws MalformedURLException {

        endPoint = new URL(url);
    }

    /**
     * This method sets the URL of the end point of the web service.
     * @param url URL of the end point of the web service.
     */
    public final void setEndPoint(final URL url) {

        endPoint = url;
    }

    /**
     * This method sets whether signs the request.
     * @param check Check to sign the request.
     */
    public final void setSignRequest(final boolean check) {

        signRequest = check;
    }

    /**
     * This method sets whether checks the response signature.
     * @param check Check to verify the response.
     */
    public final void setVerifyResponse(final boolean check) {

        verifyResponse = check;
    }

    /**
     * This method sets the certificate to sign request.
     * @param inCertificate Certificate to sign request.
     */
    public final void setCertificate(final X509Certificate inCertificate) {

        certificate = inCertificate;
    }

    /**
     * This method sets the private key of the certificate.
     * @param inPrivateKey Private key of the certificate.
     */
    public final void setPrivateKey(final PrivateKey inPrivateKey) {

        privateKey = inPrivateKey;
    }

    /**
     * This method gets the data of the message.
     * @return Data of the message.
     */
    public final MessageData getMessageData() {

        return messageData;
    }

    /**
     * This method sends a message.
     *
     * @param message Message to send.
     * @return Response to the message.
     * @throws MsgFaultMsg Fault message.
     */
    @SuppressWarnings("rawtypes")
    protected final ResponseMessage sendMessage(final RequestMessage message) throws MsgFaultMsg {

        ServiceEME service = new ServiceEME(getClass().getClassLoader().getResource(WSDL_FILE), SERVICE_NAME);
        PortTFEDIType port = service.getServiceEMEPort();
        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint.toString());

        messageData = new MessageData();

        Binding binding = bindingProvider.getBinding();
        List<Handler> handlerList = binding.getHandlerChain();
        handlerList.add(new SendHandler(signRequest, verifyResponse, messageData, certificate, privateKey));
        binding.setHandlerChain(handlerList);

        return port.request(message);
    }

    /**
     * This method creates the header of the message.
     * @param verb Verb of the message.
     * @param noun Noun of the message.
     * @return Header of the message.
     */
    protected final HeaderType createHeader(final String verb, final String noun) {

        HeaderType header = new HeaderType();
        header.setVerb(verb);
        header.setNoun(noun);
        return header;
    }

    /**
     * This method creates a new option.
     * @param name Name of the new option.
     * @param value Value of the new option. Value is optional, so its value can be <code>null</code> if not set.
     * @return New option.
     */
    protected final OptionType createOption(final String name, final String value) {

        OptionType option = new OptionType();
        option.setName(name);
        
        if (value != null) {
        	option.setValue(value);
        }
        
        return option;
    }

    /**
     * Gets the pretty print version of the response payload.
     * For performance considere to use: <code>getMessageData().getPayload()</code> instead.
     * @param responseMessage Response message.
     * @return String with the payload message.
     * @throws ClientException If the response has no payload or if the relation between payload and header is not fulfilled.
     */
    protected final String getPrettyPrintPayloadMessage(final ResponseMessage responseMessage)
            throws ClientException {

        HeaderType header = responseMessage.getHeader();
        PayloadType payload = responseMessage.getPayload();
        String verb = header.getVerb();
        String noun = header.getNoun();
        String retValue = null;
        
        if (payload == null) {
        	throw new ClientException(Messages.getString("NO_PAYLOAD", "Payload load is null")); //$NON-NLS-1$ //$NON-NLS-2$
        }
            
        Element message = payload.getAnies().get(0);
            
        if (message == null) {
            	
        	throw new ClientException(Messages.getString("PAYLOAD_EMPTY")); //$NON-NLS-1$
        }
            
        String rootTag = message.getLocalName();
            	
        if (!ConstantMessage.RESPONSE_VERB.equals(verb) || !rootTag.equals(noun)) {
                	
        	throw new ClientException(Messages.getString("INVALID_HEADER", verb, noun, ConstantMessage.RESPONSE_VERB, rootTag)); //$NON-NLS-1$
        }
       
       
       try {
               
    	   retValue = XMLUtil.prettyPrint(XMLUtil.removeNameSpaces(XMLElementUtil.element2String(message)).toString()).toString();
                
       } catch (TransformerException | ParserConfigurationException e) {

           	throw new ClientException(Messages.getString("NO_PAYLOAD", e.getMessage()), e); //$NON-NLS-1$
       }
       
       return retValue;
    
    }
}

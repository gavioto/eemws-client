/*
 * Copyright 2016 Red Eléctrica de España, S.A.U.
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
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import org.w3c.dom.Element;

import _504.iec62325.wss._1._0.MsgFaultMsg;
import _504.iec62325.wss._1._0.PortTFEDIType;
import _504.iec62325.wss._1._0.ServiceEME;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.PayloadType;
import ch.iec.tc57._2011.schema.message.RequestMessage;
import ch.iec.tc57._2011.schema.message.ResponseMessage;
import es.ree.eemws.core.utils.error.EnumErrorCatalog;
import es.ree.eemws.core.utils.error.ErrorMessages;
import es.ree.eemws.core.utils.iec61968100.EnumNoun;
import es.ree.eemws.core.utils.iec61968100.EnumVerb;
import es.ree.eemws.core.utils.iec61968100.MessageMetaData;
import es.ree.eemws.core.utils.operations.HandlerException;

/**
 * Parent class for all client's operations.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.1 10/01/2016
 */
public abstract class ParentClient {

    /** Service name of the Web Service EME. */
    private static final QName SERVICE_NAME = new QName("urn:iec62325.504:wss:1:0", "ServiceEME"); //$NON-NLS-1$ //$NON-NLS-2$

    /** WSDL file name in order to avoid connection to the remote server just to retrieve it. */
    private static final String WSDL_FILE = "urn-iec62325-504-wss-1-0.wsdl"; //$NON-NLS-1$

    /** Web service's URL endpoint . */
    private URL endPoint = null;

    /** Flag to sign the request. */
    private boolean signRequest = true;

    /** Flag to verify response's signature. */
    private boolean verifyResponse = true;

    /** Certificate to sign request. */
    private X509Certificate certificate = null;

    /** Private key of the certificate. */
    private PrivateKey privateKey = null;

    /** Message metadata. */
    private MessageMetaData messageMetaData =  new MessageMetaData();

    /**
     * Sets the URL of the end point of the web service.
     * @param url URL of the end point of the web service.
     * @throws MalformedURLException if the given address is not valid.
     */
    public final void setEndPoint(final String url) throws MalformedURLException {

        endPoint = new URL(url);
    }

    /**
     * Sets the URL of the end point of the web service.
     * @param url URL of the end point of the web service.
     */
    public final void setEndPoint(final URL url) {

        endPoint = url;
    }

    /**
     * Sets whether the request has to be signed.
     * @param flag <code>true</code> if request has to be signed.
     */
    public final void setSignRequest(final boolean flag) {

        signRequest = flag;
    }

    /**
     * Sets whether the response's signature has to be validated.
     * @param flag <code>true</code> if the response's signature has to be validated.
     */
    public final void setVerifyResponse(final boolean flag) {

        verifyResponse = flag;
    }

    /**
     * Sets the certificate to sign the request.
     * @param inCertificate Certificate to sign request.
     */
    public final void setCertificate(final X509Certificate inCertificate) {

        certificate = inCertificate;
    }

    /**
     * Sets the private key to sign the request.
     * @param inPrivateKey Private key to sign the request.
     */
    public final void setPrivateKey(final PrivateKey inPrivateKey) {

        privateKey = inPrivateKey;
    }

    /**
     * Gets message's metadata. Metadata holds information that is not
     * present neither in the request nor in the response such as certificate information or status.
     * @return Message's metadata.
     */
    public final MessageMetaData getMessageMetaData() {
        return messageMetaData;
    }

    /**
     * Sends the given request message to the configured URL.
     * @param message Message to send.
     * @return Response to the message.
     * @throws HandlerException If it is not possible to send the message or if the received response has errors.
     */
    @SuppressWarnings("rawtypes")
    protected final ResponseMessage sendMessage(final RequestMessage message) throws HandlerException {

        ResponseMessage retValue = null;

        try {
            ServiceEME service = new ServiceEME(getClass().getClassLoader().getResource(WSDL_FILE), SERVICE_NAME);
            PortTFEDIType port = service.getServiceEMEPort();
            BindingProvider bindingProvider = (BindingProvider) port;
            bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint.toString());

            /* sets a handler in order to sign and verify signature. The handler is useful also for debug. */
            messageMetaData = new MessageMetaData();
            Binding binding = bindingProvider.getBinding();
            List<Handler> handlerList = binding.getHandlerChain();
            handlerList.add(new SendHandler(signRequest, verifyResponse, messageMetaData, certificate, privateKey));
            binding.setHandlerChain(handlerList);

            retValue = port.request(message);

            /* Throws exception if the retrieved message has an invalid signature. */
            MessageMetaData metadata = getMessageMetaData();
            if (metadata.getException() != null) {
                throw (HandlerException) metadata.getException();
            }

        } catch (MsgFaultMsg ex) {
            
            throw new HandlerException(EnumErrorCatalog.ERR_HAND_010, ex);
        
        } catch (RuntimeException ex) {
            
            /* Translate into plain English most common connection issues: */
            String errStr = ex.getMessage();
            
            if (errStr.indexOf("trustAnchors") != -1 || errStr.indexOf("PKIX path building failed") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
                throw new HandlerException(EnumErrorCatalog.ERR_HAND_013);
            } else if (errStr.indexOf("No subject alternative") != -1) {  //$NON-NLS-1$
                throw new HandlerException(EnumErrorCatalog.ERR_HAND_014);
            } else if (errStr.indexOf("UnknownHostException") != -1) { //$NON-NLS-1$
                throw new HandlerException(EnumErrorCatalog.ERR_HAND_015);
            } else if (errStr.indexOf("The server sent HTTP status code 404:") != -1) { //$NON-NLS-1$
                throw new HandlerException(EnumErrorCatalog.ERR_HAND_016);
            } else if (errStr.indexOf("The server sent HTTP status code 403:") != -1) { //$NON-NLS-1$
                throw new HandlerException(EnumErrorCatalog.ERR_HAND_017);
            } else if (errStr.indexOf("The server sent HTTP status code 401:") != -1) { //$NON-NLS-1$
                throw new HandlerException(EnumErrorCatalog.ERR_HAND_018);
            } else if (errStr.indexOf("The server sent HTTP status code 200:") != -1) { //$NON-NLS-1$
                throw new HandlerException(EnumErrorCatalog.ERR_HAND_019);
            } else if (errStr.indexOf("Connection refused") != -1 || errStr.indexOf("Connection timed out") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
                throw new HandlerException(EnumErrorCatalog.ERR_HAND_020);
            } else if (errStr.indexOf("The server sent HTTP status code 400:") != -1) { //$NON-NLS-1$ 
                throw new HandlerException(EnumErrorCatalog.ERR_HAND_021);
            } else if (errStr.indexOf("unrecognized_name") != -1) { //$NON-NLS-1$
                throw new HandlerException(EnumErrorCatalog.ERR_HAND_023);
            } else {
                
                /* Do no throw a RuntimeException ! */
                throw new HandlerException(EnumErrorCatalog.ERR_HAND_022, ex);
            }
        }

        return retValue;
    }

    /**
     * Validates the received response.
     * @param responseMessage Received response.
     * @param expectedNoun Expected noun, according to the operation.
     * @throws HandlerException If the response is not valid.
     */
    protected void validateResponse(final ResponseMessage responseMessage, final String expectedNoun) throws HandlerException {
        validateResponse(responseMessage, expectedNoun, false);
    }

    /**
     * Validates the received response.
     * @param responseMessage Received response.
     * @param canBeEmpty <code>true</code> if the response can be empty.
     * @throws HandlerException If the response is not valid.
     */
    protected void validateResponse(final ResponseMessage responseMessage, final boolean canBeEmpty) throws HandlerException {
        validateResponse(responseMessage, null, canBeEmpty);
    }

    /**
     * Validates the received response.
     * <li>Must have a header.
     * <li>Must have a payload
     * <li>The verb must be "reply"
     * <li>Noun must match with the given one and if no noun is provided, must match with the root tag.
     * @param responseMessage Received response.
     * @param expectedNoun Expected noun, according to the operation.
     * @param canBeEmpty <code>true</code> if the server can return an empty response (only for "put" operations)
     * @throws HandlerException If the response is not valid (has no header, noun + verb missmatch, etc.)
     */
    private void validateResponse(final ResponseMessage responseMessage, final String expectedNoun, final boolean canBeEmpty) throws HandlerException {

        HeaderType header = responseMessage.getHeader();

        if (header == null) {
            throw new HandlerException(EnumErrorCatalog.ERR_HAND_002, ErrorMessages.NO_HEADER);
        }

        String verb = header.getVerb();
        String noun = header.getNoun();

        if (expectedNoun != null && !expectedNoun.equals(noun)) {
            throw new HandlerException(EnumErrorCatalog.ERR_HAND_012, verb, noun, EnumVerb.REPLY.toString(), expectedNoun);
        }

        PayloadType payload = responseMessage.getPayload();

        if (payload == null) {
            throw new HandlerException(EnumErrorCatalog.ERR_HAND_011);
        }

        List<Element> anies = payload.getAnies();
        boolean compressed = payload.getCompressed() != null;
        boolean empty = anies.isEmpty() && !compressed;

        if (empty) {
            if (canBeEmpty) {
                if (!EnumVerb.REPLY.toString().equals(verb)) {
                    throw new HandlerException(EnumErrorCatalog.ERR_HAND_012, verb, ErrorMessages.NO_NOUN_EXPECTED, EnumVerb.REPLY.toString(), ErrorMessages.NO_NOUN_EXPECTED);
                }
            } else {
                throw new HandlerException(EnumErrorCatalog.ERR_HAND_011);
            }
        } else {

            String rootTag;
            if (compressed) {
                rootTag = EnumNoun.COMPRESSED.toString();
            } else {

                Element message = anies.get(0);
                if (message == null) {
                    throw new HandlerException(EnumErrorCatalog.ERR_HAND_011);
                }
                rootTag = message.getLocalName();
            }

            if (!EnumVerb.REPLY.toString().equals(verb) || !rootTag.equals(noun)) {
                throw new HandlerException(EnumErrorCatalog.ERR_HAND_012, verb, noun, EnumVerb.REPLY.toString(), rootTag);
            }
        }
    }

}

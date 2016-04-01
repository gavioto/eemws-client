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
package es.ree.eemws.client.put;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import ch.iec.tc57._2011.schema.message.PayloadType;
import ch.iec.tc57._2011.schema.message.RequestMessage;
import ch.iec.tc57._2011.schema.message.ResponseMessage;
import es.ree.eemws.client.common.ParentClient;
import es.ree.eemws.core.utils.error.EnumErrorCatalog;
import es.ree.eemws.core.utils.iec61968100.EnumMessageFormat;
import es.ree.eemws.core.utils.iec61968100.EnumVerb;
import es.ree.eemws.core.utils.iec61968100.MessageUtil;
import es.ree.eemws.core.utils.operations.HandlerException;
import es.ree.eemws.core.utils.operations.put.PutOperationException;
import es.ree.eemws.core.utils.xml.XMLElementUtil;
import es.ree.eemws.core.utils.xml.XMLUtil;

/**
 * Send a message to the server for  processing.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.1 10/01/2016
 */
public final class PutMessage extends ParentClient {

    /** Put request messages are signed by default. */
    private static final boolean SIGN_REQUEST = true;

    /** Put response messages signature are validated by default. */
    private static final boolean VERIFY_RESPONSE_SIGNATURE = true;
    
    /** System property name that sets the threshold in characteres from which the document is sent compressed. */
    private static final String COMPRESS_XML_THRESHOLD_CHARS_KEY = "XML_TO_BINARY_THRESHOLD_CHARS"; //$NON-NLS-1$
    
    /** Compress XML threshold in characters. If not null, payloads with size > compressXmlThresholdChars will be sent compressed. */                                
    private static Long compressXmlThresholdChars = Long.getLong(COMPRESS_XML_THRESHOLD_CHARS_KEY); 
        
    /**
     * Constructor.
     */
    public PutMessage() {

        setSignRequest(SIGN_REQUEST);
        setVerifyResponse(VERIFY_RESPONSE_SIGNATURE);
    }

    /**
     * Sends an XML message as binary to the server for further processing (format = null).
     * @param name Name of the binary file.
     * @param data Binary content.
     * @return String with the server's response. <code>null</code> if the response has no payload.
     * @throws PutOperationException If the retrieved message has an invalid format or the application cannot handle it
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public String put(final String name, final byte[] data) throws PutOperationException {

        return put(name, data, null);
    }

    /**
     * Sends a binary message to the server for further processing.
     * @param name Name of the binary file.
     * @param data Binary content.
     * @param format Message's format. Can be <code>null</code> to use default format.
     * @return String with the server's response. <code>null</code> if the response has no payload.
     * @throws PutOperationException If the retrieved message has an invalid format or the application cannot handle it
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public String put(final String name, final byte[] data, final EnumMessageFormat format) throws PutOperationException {

        String retValue = null;
        try {
            RequestMessage requestMessage = MessageUtil.createRequestWithBinaryPayload(name, data, format);
            ResponseMessage response = sendMessage(requestMessage);
            validateResponse(response, true);
            retValue = responsePayload2String(response);
        } catch (TransformerException | ParserConfigurationException e) {
            throw new PutOperationException(EnumErrorCatalog.ERR_PUT_014, e, e.getMessage());
        } catch (HandlerException e) {
            throw new PutOperationException(e);
        }
        
        return retValue;
       
    }

    /**
     * Sends a binary message to the server for further processing.
     * @param xmlMessage Xml message to be sent.
     * @return String with the XML response message. <code>null</code> if the response has no payload.
     * @throws PutOperationException If the retrieved message has an invalid format or the application cannot handle it
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public String put(final StringBuilder xmlMessage) throws PutOperationException {
        
        String retValue = null;
        EnumErrorCatalog err = EnumErrorCatalog.ERR_PUT_014;
        
        try {
            RequestMessage requestMessage;
            
            if (compressXmlThresholdChars != null && xmlMessage.length() > compressXmlThresholdChars.longValue()) {

                requestMessage = MessageUtil.createRequestWithCompressedXmlPayload(xmlMessage);
                
            } else {
                
                String noun = XMLUtil.getRootTag(xmlMessage);
                requestMessage = MessageUtil.createRequestWithPayload(EnumVerb.CREATE.toString(), noun, xmlMessage);
            }
            
            ResponseMessage response = sendMessage(requestMessage);
            validateResponse(response, true);
            
            err = EnumErrorCatalog.ERR_PUT_015;                
            retValue = responsePayload2String(response);

        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {

            throw new PutOperationException(err, e, e.getMessage());
        
        }  catch (HandlerException e) {
        
            throw new PutOperationException(e);
        }
        
        return retValue;
    }
        
    /**
     * Returns the operation response's payload content as a String.
     * @param responseMessage Response message received from server.
     * @return Operation response (tipically an acknowledgement) as string. 
     * If the servers has returned no payload (for asynchronous communication) <code>null</code> is returned. 
     * @throws TransformerException If the response cannot be transformed as an String.
     * @throws ParserConfigurationException If the response cannot be transformed as an string.
     */
    private String responsePayload2String(final ResponseMessage responseMessage) throws TransformerException, ParserConfigurationException {
        String retValue = null;
        
        PayloadType payload = responseMessage.getPayload();

        List<Element> anies = payload.getAnies();
        
        if (!anies.isEmpty()) { 
        
            retValue = XMLElementUtil.element2String(payload.getAnies().get(0));
            
        }

        return retValue;
    }
   
}

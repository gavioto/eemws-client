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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import ch.iec.tc57._2011.schema.message.RequestMessage;
import ch.iec.tc57._2011.schema.message.ResponseMessage;
import es.ree.eemws.client.common.ParentClient;
import es.ree.eemws.core.utils.error.EnumErrorCatalog;
import es.ree.eemws.core.utils.iec61968100.EnumMessageFormat;
import es.ree.eemws.core.utils.iec61968100.EnumVerb;
import es.ree.eemws.core.utils.iec61968100.MessageUtil;
import es.ree.eemws.core.utils.operations.HandlerException;
import es.ree.eemws.core.utils.operations.put.PutOperationException;
import es.ree.eemws.core.utils.xml.XMLUtil;

/**
 * Send a message to the server for  processing.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.2 17/06/2016
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
     * @see #putWithResponseMessage(String, byte[])
     * @throws PutOperationException If the retrieved message has an invalid format or the application cannot handle it
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public String put(final String name, final byte[] data) throws PutOperationException {

        return put(name, data, null);
    }
    
    /**
     * Sends an XML message as binary to the server for further processing (format = null).
     * @param name Name of the binary file.
     * @param data Binary content.
     * @return ResponseMessage with the server's response.
     * @see #put(String, byte[])
     * @throws PutOperationException If the retrieved message has an invalid format or the application cannot handle it
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public ResponseMessage putWithResponseMessage(final String name, final byte[] data) throws PutOperationException {
        return putWithResponseMessage(name, data, null);
    }

    /**
     * Sends a binary message (can be used to send XML as binary) to the server for further processing.
     * @param name Name of the binary file.
     * @param data Binary content.
     * @param format Message's format. Can be <code>null</code> to use default format.
     * @return String with the server's response. <code>null</code> if the response has no payload.
     * @see #putWithResponseMessage(String, byte[], EnumMessageFormat)
     * @throws PutOperationException If the retrieved message has an invalid format or the application cannot handle it
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public String put(final String name, final byte[] data, final EnumMessageFormat format) throws PutOperationException {

        String retValue = null;

        try {
            retValue = MessageUtil.responsePayload2String(putWithResponseMessage(name, data, format));
        } catch (TransformerException | ParserConfigurationException e) {
            throw new PutOperationException(EnumErrorCatalog.ERR_PUT_014, e, e.getMessage());
        }
        
        return retValue;
    }

    /**
     * Sends a binary message (can be used to send XML as binary) to the server for further processing.
     * @param name Name of the binary file.
     * @param data Binary content.
     * @param format Message's format. Can be <code>null</code> to use default format.
     * @return ResponseMessage with the server's response.
     * @see #put(String, byte[], EnumMessageFormat)
     * @throws PutOperationException If the retrieved message has an invalid format or the application cannot handle it
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public ResponseMessage putWithResponseMessage(final String name, final byte[] data, final EnumMessageFormat format) throws PutOperationException {

        ResponseMessage retValue = null;
        
        try {
            RequestMessage requestMessage = MessageUtil.createRequestWithBinaryPayload(name, data, format);
            retValue = sendMessage(requestMessage);
            validateResponse(retValue, true);
        } catch (HandlerException e) {
            throw new PutOperationException(e);
        }
        
        return retValue;
    }    
    
    /**
     * Sends a binary message to the server for further processing.
     * @param xmlMessage Xml message to be sent.
     * @return String with the XML response message. <code>null</code> if the response has no payload.
     * @see #putWithResponseMessage(StringBuilder)
     * @throws PutOperationException If the retrieved message has an invalid format or the application cannot handle it
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public String put(final StringBuilder xmlMessage) throws PutOperationException {
        
        String retValue = null;
        
        try {
            
            retValue = MessageUtil.responsePayload2String(putWithResponseMessage(xmlMessage));
        
        } catch (TransformerException | ParserConfigurationException e) {
            
            throw new PutOperationException(EnumErrorCatalog.ERR_PUT_015, e, e.getMessage());
        }
        
        return retValue;
    }
      
    /**
     * Sends a binary message to the server for further processing.
     * @param xmlMessage Xml message to be sent.
     * @return ResponseMessage with the server's response. 
     * @see #put(StringBuilder)
     * @throws PutOperationException If the retrieved message has an invalid format or the application cannot handle it
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public ResponseMessage putWithResponseMessage(final StringBuilder xmlMessage) throws PutOperationException {
        
        ResponseMessage retValue = null;
                
        try {
            RequestMessage requestMessage;
            
            if (compressXmlThresholdChars != null && xmlMessage.length() > compressXmlThresholdChars.longValue()) {

                requestMessage = MessageUtil.createRequestWithCompressedXmlPayload(xmlMessage);
                
            } else {
                
                String noun = XMLUtil.getRootTag(xmlMessage);
                requestMessage = MessageUtil.createRequestWithPayload(EnumVerb.CREATE.toString(), noun, xmlMessage);
            }
            
            retValue = sendMessage(requestMessage);
            validateResponse(retValue, true);
          
        } catch (ParserConfigurationException | SAXException | IOException e) {

            throw new PutOperationException(EnumErrorCatalog.ERR_PUT_014, e, e.getMessage());
        
        }  catch (HandlerException e) {
        
            throw new PutOperationException(e);
        }
        
        return retValue;
    }
    
}

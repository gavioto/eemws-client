/*
 * Copyright 2015 Red Eléctrica de España, S.A.U.
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

import org.xml.sax.SAXException;

import _504.iec62325.wss._1._0.MsgFaultMsg;
import ch.iec.tc57._2011.schema.message.RequestMessage;
import ch.iec.tc57._2011.schema.message.ResponseMessage;
import es.ree.eemws.client.common.ClientException;
import es.ree.eemws.client.common.ParentClient;
import es.ree.eemws.core.utils.iec61968100.EnumMessageFormat;
import es.ree.eemws.core.utils.iec61968100.EnumVerb;
import es.ree.eemws.core.utils.iec61968100.MessageUtil;
import es.ree.eemws.core.utils.xml.XMLUtil;

/**
 * The Put Message service is used to send a message to the server for further processing
 * following the rules of the European Energy Markets for Electricity.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/02/2015
 */
public final class PutMessage extends ParentClient {

    /** Put request messages are signed by default. */
    private static final boolean SIGN_REQUEST = true;

    /** Put response messages signature are validated by default. */
    private static final boolean VERIFY_RESPONSE = true;

    /**
     * Constructor.
     */
    public PutMessage() {

        setSignRequest(SIGN_REQUEST);
        setVerifyResponse(VERIFY_RESPONSE);
    }

    /**
     * This method is used to send a message to the server for further processing
     * following the rules of the European Energy Markets for Electricity.
     * @param name Name of the binary file.
     * @param data Binary content.
     * @return String with the XML response message. <code>null</code> if the response has no payload.
     * @throws ClientException Exception with the error.
     */
    public String put(final String name, final byte[] data) throws ClientException {

        return put(name, data, null);
    }

    /**
     * This method is used to send a message to the server for further processing
     * following the rules of the European Energy Markets for Electricity.
     * @param name Name of the binary file.
     * @param data Binary content.
     * @param format Hint to format of payload.
     * @return String with the XML response message. <code>null</code> if the response has no payload.
     * @throws ClientException Exception with the error.
     */
    public String put(final String name, final byte[] data, final EnumMessageFormat format) throws ClientException {
        
        String retValue = null;
        
        try {

            RequestMessage requestMessage = MessageUtil.createRequestWithBinaryPayload(name, data, format);
            ResponseMessage responseMessage = sendMessage(requestMessage);
            
            if (!isPayloadEmpty(responseMessage)) {
                retValue = getPrettyPrintPayloadMessage(responseMessage);
            }

        } catch (MsgFaultMsg e) {

            throw new ClientException(e.getMessage(), e);
        }
        
        return retValue;
    }

    /**
     * Sends a XML message to the server for further processing following the rules of the European Energy Markets for Electricity.
     * @param xmlMessage The xml message that is being sent to the server.
     * @return String with the XML response message. <code>null</code> if the response has no payload.
     * @throws ClientException Exception with the error.
     */
    public String put(final StringBuilder xmlMessage) throws ClientException {
        
        String retValue = null;
        
        try {
            String noun = XMLUtil.getRootTag(xmlMessage);
            RequestMessage requestMessage = MessageUtil.createRequestWithPayload(EnumVerb.CREATE.toString(), noun, xmlMessage);
            ResponseMessage responseMessage = sendMessage(requestMessage);
            
            if (!isPayloadEmpty(responseMessage)) {
                retValue = getPrettyPrintPayloadMessage(responseMessage);
            }

        } catch (MsgFaultMsg | ParserConfigurationException | SAXException | IOException e) {

            throw new ClientException(e.getMessage(), e);
        }
        
        return retValue;
    }
    
    /**
     * Checks weather the response menssaje has payload. 
     * In certaint cases, a put message could receive an empty response. 
     * @param response Response menssaje.
     * @return <code>true</code> if the response has no payload. <code>false</code> otherwise.
     */
    private boolean isPayloadEmpty(final ResponseMessage response) {
        
        return response.getPayload() == null || response.getPayload().getAnies().get(0) == null;
    }
}

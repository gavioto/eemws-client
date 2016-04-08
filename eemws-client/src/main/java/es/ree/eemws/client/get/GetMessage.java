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

package es.ree.eemws.client.get;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import ch.iec.tc57._2011.schema.message.RequestMessage;
import ch.iec.tc57._2011.schema.message.ResponseMessage;
import es.ree.eemws.client.common.ParentClient;
import es.ree.eemws.core.utils.iec61968100.EnumFilterElement;
import es.ree.eemws.core.utils.iec61968100.EnumNoun;
import es.ree.eemws.core.utils.iec61968100.EnumVerb;
import es.ree.eemws.core.utils.iec61968100.MessageUtil;
import es.ree.eemws.core.utils.operations.HandlerException;
import es.ree.eemws.core.utils.operations.get.GetOperationException;
import es.ree.eemws.core.utils.operations.get.GetRequestMessageValidator; 

/**
 * Retrieves the message according to the given parameters (filters).
 * 
 * @author Red Eléctrica de España S.A.U.
 * @version 1.1 10/01/2016
 */
public final class GetMessage extends ParentClient {

    /** Get request is not signed. */
    private static final boolean SIGN_REQUEST = false;

    /** Get response's signature is validated. */
    private static final boolean VERIFY_RESPONSE_SIGNATURE = true;

    /**
     * Constructor.
     */
    public GetMessage() {
        setSignRequest(SIGN_REQUEST);
        setVerifyResponse(VERIFY_RESPONSE_SIGNATURE);
    }

    /**
     * Gets the message with the given identification and version.
     * @param messageIdentification Message's identification.
     * @param messageVersion Message's version. Can be <code>null</code> if no version applies.
     * @return Response message wrapper with the server's response.
     * @throws GetOperationException If the retrieved message has an invalid format or the application cannot handle it
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public RetrievedMessage get(final String messageIdentification, final Integer messageVersion) throws GetOperationException {

        return get(messageIdentification, messageVersion, null, null);
    }

    /**
     * Gets the message with the given code.
     * @param code Message's code.
     * @return Response message wrapper with the server's response.
     * @throws GetOperationException If the retrieved message has an invalid format or the application cannot handle it
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     * @see #get(Long)
     */
    public RetrievedMessage get(final BigInteger code) throws GetOperationException {

        return get(null, null, code.longValue(), null);
    }
   
    
    /**
     * Gets the message with the given code.
     * @param code Message's code.
     * @return Response message wrapper with the server's response.
     * @throws GetOperationException If the retrieved message has an invalid format or the application cannot handle it
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     * @see #get(BigInteger)
     */
    public RetrievedMessage get(final Long code) throws GetOperationException {

        return get(null, null, code, null);
    }

    /**
     * Gets the message from the queue.
     * @param queue Queue value.
     * @return Response message wrapper with the server's response.
     * @throws GetOperationException If the retrieved message has an invalid format or the application cannot handle it
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public RetrievedMessage get(final String queue) throws GetOperationException {

        return get(null, null, null, queue);
    }

    /**
     * Gets the message using the given parameters.
     * @param msgOptions List options as a Map which key must be on the EnumFilterElement list.
     * @return Response message wrapper with the server's response.
     * @throws GetOperationException If the retrieved message has an invalid format or the application cannot handle it
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public RetrievedMessage get(final Map<String, String> msgOptions) throws GetOperationException {
        RetrievedMessage retValue = new RetrievedMessage();

        try {
            
            retValue.setMsgIdentification(msgOptions.get(EnumFilterElement.MESSAGE_IDENTIFICATION.toString()));
            retValue.setMsgVersion(msgOptions.get(EnumFilterElement.MESSAGE_VERSION.toString()));
            retValue.setMsgIdentification(msgOptions.get(EnumFilterElement.CODE.toString()));
   
            RequestMessage requestMessage = MessageUtil.createRequestWithOptions(EnumVerb.GET, EnumNoun.ANY, msgOptions);
            GetRequestMessageValidator.validate(requestMessage);
            ResponseMessage response = sendMessage(requestMessage);
            validateResponse(response, false);
            retValue.setMessage(response);

        } catch (HandlerException e) {
            throw new GetOperationException(e);
        }  
        
        return retValue;
    }

    /**
     * Gets the message using the given parameters.
     * @param messageIdentification Message's identification.
     * @param messageVersion Message's version. Can be <code>null</code> if no version applies.   
     * @param code Message's code.
     * @param queue Queue value.
     * @return Response message wrapper with the server's response.
     * @throws GetOperationException If the retrieved message has an invalid format or the application cannot handle it
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public RetrievedMessage get(final String messageIdentification, final Integer messageVersion, final Long code, final String queue) throws GetOperationException {

        Map<String, String> msgOptions = new HashMap<>();

        if (messageIdentification != null) {
            msgOptions.put(EnumFilterElement.MESSAGE_IDENTIFICATION.toString(), messageIdentification);
        }

        if (messageVersion != null) {
            msgOptions.put(EnumFilterElement.MESSAGE_VERSION.toString(), messageVersion.toString());
        }

        if (code != null) {
            msgOptions.put(EnumFilterElement.CODE.toString(), code.toString());
        }

        if (queue != null) {
            msgOptions.put(EnumFilterElement.QUEUE.toString(), queue);
        }

        return get(msgOptions);
    }
}

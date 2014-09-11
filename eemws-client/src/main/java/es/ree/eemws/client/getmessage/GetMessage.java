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
package es.ree.eemws.client.getmessage;

import java.util.List;

import _504.iec62325.wss._1._0.MsgFaultMsg;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.OptionType;
import ch.iec.tc57._2011.schema.message.RequestMessage;
import ch.iec.tc57._2011.schema.message.RequestType;
import ch.iec.tc57._2011.schema.message.ResponseMessage;
import es.ree.eemws.client.common.Messages;
import es.ree.eemws.client.common.ParentClient;
import es.ree.eemws.client.exception.ClientException;


/**
 * Obtain the message associated to the given parameter (filter).
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class GetMessage extends ParentClient {

    /** Verb of the action. */
    private static final String VERB = "get"; //$NON-NLS-1$

    /** Name of the MessageIdentification option. */
    private static final String MESSAGE_IDENTIFICATION_OPTION = "MessageIdentification"; //$NON-NLS-1$

    /** Name of the MessageVersion option. */
    private static final String MESSAGE_VERSION_OPTION = "MessageVersion"; //$NON-NLS-1$

    /** Name of the Code option. */
    private static final String CODE_OPTION = "Code"; //$NON-NLS-1$

    /** Name of the Queue option. */
    private static final String QUEUE_OPTION = "Queue"; //$NON-NLS-1$

    /** Queue value should be alwyas "NEXT". */
	private static final String QUEUE_NEXT_VALUE = "NEXT"; //$NON-NLS-1$

    /**
     * Constructor.
     */
    public GetMessage() {

        setSignRequest(false);
        setVerifyResponse(true);
    }

    /**
     * This method obtain the message associated to the given parameter (filter).
     * @param noun Specifies the Message Type of the requested message.
     * @param messageIdentification Specifies the Message Identification of the requested message.
     * @param messageVersion Specifies the Message Version of the requested message. If more than one message in the server have the same
     *                       MessageIdentification and MessageVersion, the most recent one will be returned.
     * @return String with the XML message.
     * @throws ClientException Exception with the error.
     */
    public String get(final String noun, final String messageIdentification, final Integer messageVersion)
            throws ClientException {

        return get(noun, messageIdentification, messageVersion, null, null);
    }

    /**
     * This method obtain the message associated to the given parameter (filter).
     * @param noun Specifies the Message Type of the requested message.
     * @param code Specifies the internal identification number of the requested message.
     * @return String with the XML message.
     * @throws ClientException Exception with the error.
     */
    public String get(final String noun, final Long code)
            throws ClientException {

        return get(noun, null, null, code, null);
    }

    /**
     * This method obtain the message associated to the given parameter (filter).
     * @param noun Specifies the Message Type of the requested message.
     * @param queue Indicates that the server will decide which message will be returned. Its value must be <code>NEXT</code> or <code>null</code> 
     * @return String with the XML message.
     * @throws ClientException Exception with the error.
     */
    public String get(final String noun, final String queue) throws ClientException {
    	
    	if (queue != null && !QUEUE_NEXT_VALUE.equals(queue)) {
    		
    		throw new ClientException(Messages.getString("INVALID_QUEUE", QUEUE_NEXT_VALUE, queue)); //$NON-NLS-1$
    	}
    	
        return get(noun, null, null, null, QUEUE_NEXT_VALUE);
    }

    /**
     * This method obtain the message associated to the given parameter (filter).
     * @param noun Specifies the Message Type of the requested message.
     * @param messageIdentification Specifies the Message Identification of the requested message.
     * @param messageVersion Specifies the Message Version of the requested message. If more than one message in the server have the same
     *                       MessageIdentification and MessageVersion, the most recent one will be returned.
     * @param code Specifies the internal identification number of the requested message.
     * @param queue Indicates that the server will decide which message will be returned. Its value shall be “NEXT”.
     * @return String with the XML message.
     * @throws ClientException Exception with the error.
     */
    private String get(final String noun,
            final String messageIdentification,
            final Integer messageVersion,
            final Long code,
            final String queue) throws ClientException {

        try {

            RequestMessage requestMessage = createRequest(noun, messageIdentification, messageVersion, code, queue);
            ResponseMessage responseMessage = sendMessage(requestMessage);
            return getPrettyPrintPayloadMessage(responseMessage);

        } catch (MsgFaultMsg e) {

            throw new ClientException(e);
        }
    }

    /**
     * This method create the request message.
     * @param noun Specifies the Message Type of the requested message.
     * @param messageIdentification Specifies the Message Identification of the requested message.
     * @param messageVersion Specifies the Message Version of the requested message. If more than one message in the server have the same
     *                       MessageIdentification and MessageVersion, the most recent one will be returned.
     * @param code Specifies the internal identification number of the requested message.
     * @param queue Indicates that the server will decide which message will be returned. Its value shall be “NEXT”.
     * @return Request message.
     */
    private RequestMessage createRequest(final String noun,
            final String messageIdentification,
            final Integer messageVersion,
            final Long code,
            final String queue) {

        RequestMessage message = new RequestMessage();

        HeaderType header = createHeader(VERB, noun);
        message.setHeader(header);

        RequestType resquest = new RequestType();

        List<OptionType> options = resquest.getOptions();
        if (messageIdentification != null) {

            OptionType option = createOption(MESSAGE_IDENTIFICATION_OPTION, messageIdentification);
            options.add(option);
        }

        if (messageVersion != null) {

            OptionType option = createOption(MESSAGE_VERSION_OPTION, String.valueOf(messageVersion));
            options.add(option);
        }

        if (code != null) {

            OptionType option = createOption(CODE_OPTION, String.valueOf(code));
            options.add(option);
        }

        if (queue != null) {

            OptionType option = createOption(QUEUE_OPTION, queue);
            options.add(option);
        }

        message.setRequest(resquest);
        return message;
    }
}

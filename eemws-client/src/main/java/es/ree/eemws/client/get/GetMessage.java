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
package es.ree.eemws.client.get;

import java.util.HashMap;
import java.util.Map;

import _504.iec62325.wss._1._0.MsgFaultMsg;
import ch.iec.tc57._2011.schema.message.RequestMessage;
import ch.iec.tc57._2011.schema.message.ResponseMessage;
import es.ree.eemws.client.common.ClientException;
import es.ree.eemws.client.common.Messages;
import es.ree.eemws.client.common.ParentClient;
import es.ree.eemws.core.utils.iec61968100.EnumFilterElement;
import es.ree.eemws.core.utils.iec61968100.EnumNoun;
import es.ree.eemws.core.utils.iec61968100.EnumVerb;
import es.ree.eemws.core.utils.iec61968100.MessageUtil;

/**
 * Retrieves the message according to the given parameters (filters).
 * 
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class GetMessage extends ParentClient {

	/** Queue value should be alwyas "NEXT". */
	private static final String QUEUE_NEXT_VALUE = "NEXT"; //$NON-NLS-1$

	/** Get request messages are not signed. */
	private static final boolean SIGN_REQUEST = false;

	/** Get response messages signature are validated. */
	private static final boolean VERIFY_RESPONSE = true;

	/**
	 * Constructor.
	 */
	public GetMessage() {

		setSignRequest(SIGN_REQUEST);
		setVerifyResponse(VERIFY_RESPONSE);
	}

	/**
	 * This method obtain the message associated to the given parameter (filter).
	 * @param messageIdentification Specifies the Message Identification of the requested message.
	 * @param messageVersion Specifies the Message Version of the requested message. If more than one message in the
	 * server have the same MessageIdentification and MessageVersion, the most recent one will be returned.
	 * @return String with the XML message.
	 * @throws ClientException Exception with the error.
	 */
	public String get(final String messageIdentification, final Integer messageVersion) throws ClientException {

		return get(messageIdentification, messageVersion, null, null);
	}

	/**
	 * This method obtain the message associated to the given parameter (filter).
	 * @param code Specifies the internal identification number of the requested message.
	 * @return String with the XML message.
	 * @throws ClientException Exception with the error.
	 */
	public String get(final Long code) throws ClientException {

		return get(null, null, code, null);
	}

	/**
	 * This method obtain the message associated to the given parameter (filter).
	 * @param queue Indicates that the server will decide which message will be returned. Its value must be
	 * <code>NEXT</code> or <code>null</code>
	 * @return String with the XML message.
	 * @throws ClientException Exception with the error.
	 */
	public String get(final String queue) throws ClientException {

		if (queue != null && !QUEUE_NEXT_VALUE.equals(queue)) {

			throw new ClientException(Messages.getString("INVALID_QUEUE", QUEUE_NEXT_VALUE, queue)); //$NON-NLS-1$
		}

		return get(null, null, null, QUEUE_NEXT_VALUE);
	}

	/**
	 * This method obtain the message associated to the given parameter (filter).
	 * @param messageIdentification Specifies the Message Identification of the requested message.
	 * @param messageVersion Specifies the Message Version of the requested message. If more than one message in the
	 * server have the same MessageIdentification and MessageVersion, the most recent one will be returned.
	 * @param code Specifies the internal identification number of the requested message.
	 * @param queue Indicates that the server will decide which message will be returned. Its value shall be
	 * <code>NEXT</code>.
	 * @return String with the XML message.
	 * @throws ClientException Exception with the error.
	 */
	private String get(final String messageIdentification, final Integer messageVersion, final Long code, final String queue) throws ClientException {

		try {

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
		    
			RequestMessage requestMessage = MessageUtil.createRequestWithOptions(EnumVerb.GET, EnumNoun.ANY, msgOptions); 
			ResponseMessage responseMessage = sendMessage(requestMessage);
			return getPrettyPrintPayloadMessage(responseMessage);

		} catch (MsgFaultMsg e) {

			throw new ClientException(e);
		}
	}
}

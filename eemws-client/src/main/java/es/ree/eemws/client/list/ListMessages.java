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
package es.ree.eemws.client.list;

import java.math.BigInteger;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.w3c.dom.Element;

import _504.iec62325.messages._1._0.MessageList;
import _504.iec62325.messages._1._0.MessageList.Message;
import _504.iec62325.messages._1._0.StatusType;
import _504.iec62325.messages._1._0.TimeIntervalType;
import _504.iec62325.wss._1._0.MsgFaultMsg;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.PayloadType;
import ch.iec.tc57._2011.schema.message.RequestMessage;
import ch.iec.tc57._2011.schema.message.ResponseMessage;
import es.ree.eemws.client.common.ClientException;
import es.ree.eemws.client.common.Messages;
import es.ree.eemws.client.common.ParentClient;
import es.ree.eemws.core.utils.iec61968100.EnumFilterElement;
import es.ree.eemws.core.utils.iec61968100.EnumIntervalTimeType;
import es.ree.eemws.core.utils.iec61968100.EnumNoun;
import es.ree.eemws.core.utils.iec61968100.EnumVerb;
import es.ree.eemws.core.utils.iec61968100.MessageUtil;
import es.ree.eemws.core.utils.xml.XMLElementUtil;

/**
 * Obtains a list of available messages for the client according to a given filter.
 * 
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class ListMessages extends ParentClient {

    /** List request messages are not signed by default. */
    private static final boolean SIGN_REQUEST = false;

    /** List response messages signature are not validated by default. */
    private static final boolean VERIFY_RESPONSE = false;

    /**
     * Constructor.
     */
    public ListMessages() {

        setSignRequest(SIGN_REQUEST);
        setVerifyResponse(VERIFY_RESPONSE);
    }

    /**
     * This method obtain a list of available messages for the client according to a given filter.
     * @param startTime Specifies that the list of messages returned should only include messages whose end of their
     * Application TimeInterval (Document TimeInterval) or Server Timestamp comes after the provided date.
     * @param endTime Specifies that the list of messages returned should only include messages whose start of their
     * Application TimeInterval or ServerTimestamp (when the message was received or published in the server) comes
     * before the provided date.
     * @return List of data message.
     * @throws ClientException Exception with the error.
     */
    public List<MessageListEntry> list(final Date startTime, final Date endTime) throws ClientException {

        return list(startTime, endTime, null, null, null, null);
    }

    /**
     * This method obtain a list of available messages for the client according to a given filter.
     * @param startTime Specifies that the list of messages returned should only include messages whose end of their
     * Application TimeInterval (Document TimeInterval) or Server Timestamp comes after the provided date.
     * @param endTime Specifies that the list of messages returned should only include messages whose start of their
     * Application TimeInterval or ServerTimestamp (when the message was received or published in the server) comes
     * before the provided date.
     * @param intervalType Indicates whether the StartTime and EndTime refer to Application TimeInterval or to Server
     * Timestamp. Permitted values: "Application" (default), "Server".
     * @return List of data message.
     * @throws ClientException Exception with the error.
     */
    public List<MessageListEntry> list(final Date startTime, final Date endTime, final EnumIntervalTimeType intervalType) throws ClientException {

        return list(startTime, endTime, intervalType, null, null, null);
    }

    /**
     * This method obtain a list of available messages for the client according to a given filter.
     * @param startTime Specifies that the list of messages returned should only include messages whose end of their
     * Application TimeInterval (Document TimeInterval) or Server Timestamp comes after the provided date.
     * @param endTime Specifies that the list of messages returned should only include messages whose start of their
     * Application TimeInterval or ServerTimestamp (when the message was received or published in the server) comes
     * before the provided date.
     * @param intervalType Indicates whether the StartTime and EndTime refer to Application TimeInterval or to Server
     * Timestamp. Permitted values: "Application" (default), "Server".
     * @param messageIdentification Specifies that the list of messages returned should only include messages whose
     * Message Identification is compliant with the pattern provided in this parameter. ("*" can be used as a wildcard).
     * @param msgType Specifies that the list of messages returned should only include messages of the provided type.
     * @param owner Specifies that the list of messages returned should only include messages belonging to the provided
     * Owner.
     * @return List of data message.
     * @throws ClientException Exception with the error.
     */
    public List<MessageListEntry> list(final Date startTime, final Date endTime, final EnumIntervalTimeType intervalType, final String messageIdentification, final String msgType, final String owner) throws ClientException {

        return list(null, startTime, endTime, intervalType, messageIdentification, msgType, owner);
    }

    /**
     * This method obtain a list of available messages for the client according to a given filter.
     * @param code Specifies that the list of messages returned should only include messages with an internal
     * identification number higher than the provided code. This means that the list will contain messages that are
     * newer to the given one. For optimization purposes, if this filter is used, only messages available since the
     * 00.00 of D-1 (day before) are guaranteed to be included in the response list
     * @return List of data message.
     * @throws ClientException Exception with the error.
     */
    public List<MessageListEntry> list(final Long code) throws ClientException {

        return list(code, null, null, null);
    }

    /**
     * This method obtain a list of available messages for the client according to a given filter.
     * @param code Specifies that the list of messages returned should only include messages with an internal
     * identification number higher than the provided code. This means that the list will contain messages that are
     * newer to the given one. For optimization purposes, if this filter is used, only messages available since the
     * 00.00 of D-1 (day before) are guaranteed to be included in the response list
     * @param messageIdentification Specifies that the list of messages returned should only include messages whose
     * Message Identification is compliant with the pattern provided in this parameter. ("*" can be used as a wildcard).
     * @param msgType Specifies that the list of messages returned should only include messages of the provided type.
     * @param owner Specifies that the list of messages returned should only include messages belonging to the provided
     * Owner.
     * @return List of data message.
     * @throws ClientException Exception with the error.
     */
    public List<MessageListEntry> list(final Long code, final String messageIdentification, final String msgType, final String owner) throws ClientException {

        return list(code, null, null, null, messageIdentification, msgType, owner);
    }

    /**
     * This method obtain a list of available messages for the client according to a given filter.
     * @param code Specifies that the list of messages returned should only include messages with an internal
     * identification number higher than the provided code. This means that the list will contain messages that are
     * newer to the given one. For optimization purposes, if this filter is used, only messages available since the
     * 00.00 of D-1 (day before) are guaranteed to be included in the response list
     * @param startTime Specifies that the list of messages returned should only include messages whose end of their
     * Application TimeInterval (Document TimeInterval) or Server Timestamp comes after the provided date.
     * @param endTime Specifies that the list of messages returned should only include messages whose start of their
     * Application TimeInterval or ServerTimestamp (when the message was received or published in the server) comes
     * before the provided date.
     * @param intervalType Indicates whether the StartTime and EndTime refer to Application TimeInterval or to Server
     * Timestamp. Permitted values: "Application" (default), "Server".
     * @param messageIdentification Specifies that the list of messages returned should only include messages whose
     * Message Identification is compliant with the pattern provided in this parameter. ("*" can be used as a wildcard).
     * @param msgType Specifies that the list of messages returned should only include messages of the provided type.
     * @param owner Specifies that the list of messages returned should only include messages belonging to the provided
     * Owner.
     * @return List of data message.
     * @throws ClientException Exception with the error.
     */
    private List<MessageListEntry> list(final Long code, final Date startTime, final Date endTime, final EnumIntervalTimeType intervalType, final String messageIdentification, final String msgType, final String owner) throws ClientException {

        try {

            Map<String, String> msgOptions = new HashMap<>();

            if (code != null && (startTime != null || endTime != null || intervalType != null)) {

                throw new ClientException(Messages.getString("LIST_INVALID_FILTER")); //$NON-NLS-1$
            }

            if (code != null) {

                if (code < 0) {
                    throw new ClientException(Messages.getString("LIST_INVALID_CODE")); //$NON-NLS-1$
                }

                msgOptions.put(EnumFilterElement.CODE.toString(), Long.toString(code));

            } else if (startTime != null && endTime != null) {
                
                DateFormat df = DateFormat.getInstance();
                
                msgOptions.put(EnumFilterElement.START_TIME.toString(), df.format(startTime));
                msgOptions.put(EnumFilterElement.END_TIME.toString(), df.format(endTime));

                /* Interval type only applies with time intervals. */
                if (intervalType == null) {
                    msgOptions.put(EnumFilterElement.INTERVAL_TYPE.toString(), EnumIntervalTimeType.DEFAULT_INTERVAL_TYPE.toString());
                } else {
                    msgOptions.put(EnumFilterElement.INTERVAL_TYPE.toString(), intervalType.toString());
                }
            }

            if (messageIdentification != null) {
                msgOptions.put(EnumFilterElement.MESSAGE_IDENTIFICATION.toString(), messageIdentification);
            }

            if (msgType != null) {
                msgOptions.put(EnumFilterElement.MESSAGE_TYPE.toString(), msgType);
            }

            if (owner != null) {
                msgOptions.put(EnumFilterElement.OWNER.toString(), owner);
            }

            RequestMessage requestMessage = MessageUtil.createRequestWithOptions(EnumVerb.GET, EnumNoun.MESSAGE_LIST, msgOptions);

            ResponseMessage responseMessage = sendMessage(requestMessage);
            return getListMessageData(responseMessage);

        } catch (MsgFaultMsg e) {

            throw new ClientException(e);
        }
    }

    /**
     * This method process the response message.
     * @param responseMessage Response message.
     * @return List of data message.
     * @throws ClientException Exception with the error.
     */
    private List<MessageListEntry> getListMessageData(final ResponseMessage responseMessage) throws ClientException {

        HeaderType header = responseMessage.getHeader();
        checkHeaderResponse(header);

        PayloadType payload = responseMessage.getPayload();
        return processPayload(payload);
    }

    /**
     * Checks the header of the response.
     * @param header Header of the response.
     * @throws ClientException Exception with the error.
     */
    private void checkHeaderResponse(final HeaderType header) throws ClientException {

        if (header == null) {
            throw new ClientException(Messages.getString("NO_HEADER")); //$NON-NLS-1$
        }

        String verb = header.getVerb();
        String noun = header.getNoun();

        if (!EnumVerb.REPLY.toString().equals(verb) || !EnumNoun.MESSAGE_LIST.toString().equals(noun)) {

            throw new ClientException(Messages.getString("INVALID_HEADER", verb, noun, EnumVerb.REPLY.toString(), EnumNoun.MESSAGE_LIST.toString())); //$NON-NLS-1$
        }
    }

    /**
     * This method process the payload of the message.
     * @param payload Payload of the message.
     * @return List of data message.
     * @throws ClientException Exception with the error.
     */
    private List<MessageListEntry> processPayload(final PayloadType payload) throws ClientException {

        List<MessageListEntry> listMessageData = new ArrayList<MessageListEntry>();

        try {

            Element message = payload.getAnies().get(0);
            MessageList messageList = (MessageList) XMLElementUtil.elment2Obj(message, MessageList.class);

            List<Message> listaMensajes = messageList.getMessages();
            for (Message msg : listaMensajes) {

                MessageListEntry messageData = new MessageListEntry();
                messageData.setCode(msg.getCode());
                messageData.setMessageIdentification(msg.getMessageIdentification());

                BigInteger version = msg.getMessageVersion();
                if (version != null) {

                    messageData.setVersion(version);
                }

                StatusType status = msg.getStatus();
                if (status != null) {

                    messageData.setStatus(status.value());
                }

                TimeIntervalType interval = msg.getApplicationTimeInterval();
                if (interval != null) {

                    if (interval.getStart() != null) {
                        
                        messageData.setApplicationStartTime(interval.getStart().toGregorianCalendar());    
                    }
                    

                    if (interval.getEnd() != null) {

                        messageData.setApplicationEndTime(interval.getEnd().toGregorianCalendar());
                    }
                }

                messageData.setServerTimestamp(msg.getServerTimestamp().toGregorianCalendar());
                messageData.setType(msg.getType());
                messageData.setOwner(msg.getOwner());

                listMessageData.add(messageData);
            }

        } catch (JAXBException e) {

            throw new ClientException(Messages.getString("NO_PAYLOAD", e.getMessage()), e); //$NON-NLS-1$
        }

        return listMessageData;
    }
}

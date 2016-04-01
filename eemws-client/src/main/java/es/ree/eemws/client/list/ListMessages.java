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
import ch.iec.tc57._2011.schema.message.RequestMessage;
import ch.iec.tc57._2011.schema.message.ResponseMessage;
import es.ree.eemws.client.common.ParentClient;
import es.ree.eemws.core.utils.error.EnumErrorCatalog;
import es.ree.eemws.core.utils.iec61968100.EnumFilterElement;
import es.ree.eemws.core.utils.iec61968100.EnumIntervalTimeType;
import es.ree.eemws.core.utils.iec61968100.EnumNoun;
import es.ree.eemws.core.utils.iec61968100.EnumVerb;
import es.ree.eemws.core.utils.iec61968100.MessageUtil;
import es.ree.eemws.core.utils.operations.HandlerException;
import es.ree.eemws.core.utils.operations.list.ListOperationException;
import es.ree.eemws.core.utils.operations.list.ListRequestMessageValidator;
import es.ree.eemws.core.utils.xml.XMLElementUtil;

/**
 * Obtains a list of available messages according to filters.
 * 
 * @author Red Eléctrica de España S.A.U.
 * @version 1.1 10/01/2016
 */
public final class ListMessages extends ParentClient {

    /** List request messages are not signed by default. */
    private static final boolean SIGN_REQUEST = false;

    /** List response messages signature are not validated by default. */
    private static final boolean VERIFY_RESPONSE_SIGNATURE = false;

    /**
     * Constructor.
     */
    public ListMessages() {

        setSignRequest(SIGN_REQUEST);
        setVerifyResponse(VERIFY_RESPONSE_SIGNATURE);
    }

    /**
     * Obtains a list of available messages for the client according to a given filter.
     * @param startTime Specifies that the list of messages returned should only include messages whose end of their
     * Application TimeInterval (Document TimeInterval) or Server Timestamp comes after the provided date.
     * @param endTime Specifies that the list of messages returned should only include messages whose start of their
     * Application TimeInterval or ServerTimestamp (when the message was received or published in the server) comes
     * before the provided date.
     * @return A list of <code>MessageListEntry</code> elements.
     * @throws ListOperationException If the retrieved message has an invalid format or the application cannot handle it 
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public List<MessageListEntry> list(final Date startTime, final Date endTime) throws ListOperationException {

        return list(startTime, endTime, null, null, null, null);
    }

    /**
     * Obtains a list of available messages for the client according to a given filter.
     * @param startTime Specifies that the list of messages returned should only include messages whose end of their
     * Application TimeInterval (Document TimeInterval) or Server Timestamp comes after the provided date.
     * @param endTime Specifies that the list of messages returned should only include messages whose start of their
     * Application TimeInterval or ServerTimestamp (when the message was received or published in the server) comes
     * before the provided date.
     * @param intervalType Indicates whether the StartTime and EndTime refer to Application TimeInterval or to Server Timestamp.
     * @return A list of <code>MessageListEntry</code> elements.
     * @throws ListOperationException If the retrieved message has an invalid format or the application cannot handle it 
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
    */
    public List<MessageListEntry> list(final Date startTime, final Date endTime, final EnumIntervalTimeType intervalType) throws ListOperationException {

        return list(startTime, endTime, intervalType, null, null, null);
    }

    /**
     * Obtains a list of available messages for the client according to a given filter.
     * @param startTime Specifies that the list of messages returned should only include messages whose end of their
     * Application TimeInterval (Document TimeInterval) or Server Timestamp comes after the provided date.
     * @param endTime Specifies that the list of messages returned should only include messages whose start of their
     * Application TimeInterval or ServerTimestamp (when the message was received or published in the server) comes
     * before the provided date.
     * @param intervalType Indicates whether the StartTime and EndTime refer to Application TimeInterval or to Server
     * Timestamp. Permitted values: "Application" (default), "Server".
     * @param messageIdentification Specifies that the list of messages returned should only include messages whose
     * Message Identification is compliant with the pattern provided in this parameter. ("*" can be used as a wildcard).
     * @param msgType Specifies that the list of messages returned should only include messages of the given type.
     * @param owner Specifies that the list of messages returned should only include messages belonging to the given Owner.
     * @return A list of <code>MessageListEntry</code> elements.
     * @throws ListOperationException If the retrieved message has an invalid format or the application cannot handle it 
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public List<MessageListEntry> list(final Date startTime, final Date endTime, final EnumIntervalTimeType intervalType, 
            final String messageIdentification, final String msgType, final String owner) throws ListOperationException {

        return list(null, startTime, endTime, intervalType, messageIdentification, msgType, owner);
    }

    /**
     * Obtains a list of available messages for the client according to a given filter.
     * @param code Specifies that the list of messages returned should only include messages with an internal
     * identification number higher than the provided code. This means that the list will contain messages that are
     * newer to the given one. For optimization purposes, if this filter is used, only messages available since the
     * 00.00 of D-1 (day before) are guaranteed to be included in the response list
     * @return A list of <code>MessageListEntry</code> elements.
     * @throws ListOperationException If the retrieved message has an invalid format or the application cannot handle it 
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public List<MessageListEntry> list(final Long code) throws ListOperationException {

        return list(code, null, null, null);
    }

    /**
     * Obtains a list of available messages for the client according to a given filter.
     * @param code Specifies that the list of messages returned should only include messages with an internal
     * identification number higher than the provided code. This means that the list will contain messages that are
     * newer to the given one. For optimization purposes, if this filter is used, only messages available since the
     * 00.00 of D-1 (day before) are guaranteed to be included in the response list
     * @param messageIdentification Specifies that the list of messages returned should only include messages whose
     * Message Identification is compliant with the pattern provided in this parameter. ("*" can be used as a wildcard).
     * @param msgType Specifies that the list of messages returned should only include messages of the given type.
     * @param owner Specifies that the list of messages returned should only include messages belonging to the given Owner.
     * @return A list of <code>MessageListEntry</code> elements.
     * @throws ListOperationException If the retrieved message has an invalid format or the application cannot handle it 
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public List<MessageListEntry> list(final Long code, final String messageIdentification, final String msgType, final String owner) throws ListOperationException {

        return list(code, null, null, null, messageIdentification, msgType, owner);
    }
    
    
    /**
     * Obtains a list of available messages for the client according to a given filter.
     * @param msgOptions List options as a Map which key must be on the EnumFilterElement list.
     * @return A list of <code>MessageListEntry</code> elements.
     * @throws ListOperationException If the retrieved message has an invalid format or the application cannot handle it 
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    public List<MessageListEntry> list(final Map<String, String> msgOptions) throws ListOperationException {

        List<MessageListEntry> retValue = null;
                
        try {
            RequestMessage requestMessage = MessageUtil.createRequestWithOptions(EnumVerb.GET, EnumNoun.MESSAGE_LIST, msgOptions);
            ListRequestMessageValidator.validate(requestMessage);
            ResponseMessage response = sendMessage(requestMessage);
            validateResponse(response, EnumNoun.MESSAGE_LIST.toString());
            retValue = processPayload(response);

        } catch (HandlerException e) {

            throw new ListOperationException(e);

        }

        return retValue;
    }

    /**
     * Obtains a list of available messages for the client according to a given filter.
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
     * @param msgType Specifies that the list of messages returned should only include messages of the given type.
     * @param owner Specifies that the list of messages returned should only include messages belonging to the given Owner.
     * @return A list of <code>MessageListEntry</code> elements.
     * @throws ListOperationException If the retrieved message has an invalid format or the application cannot handle it 
     * or if the retrieved message has invalid signature or is not valid (has no header, invalid verb, etc.)
     */
    private List<MessageListEntry> list(final Long code, final Date startTime, final Date endTime, final EnumIntervalTimeType intervalType, 
            final String messageIdentification, final String msgType, final String owner) throws ListOperationException {

        Map<String, String> msgOptions = new HashMap<>();

        if (code != null) {
            msgOptions.put(EnumFilterElement.CODE.toString(), Long.toString(code));
        }

        DateFormat df = DateFormat.getInstance();

        if (startTime != null) {
            msgOptions.put(EnumFilterElement.START_TIME.toString(), df.format(startTime));
        }

        if (endTime != null) {
            msgOptions.put(EnumFilterElement.END_TIME.toString(), df.format(endTime));
        }

        if (intervalType != null) {
            msgOptions.put(EnumFilterElement.INTERVAL_TYPE.toString(), intervalType.toString());
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

        return list(msgOptions);
    }

    /**
     * Process the list response creating a list of <code>MessageListEntry</code> elements. 
     * Note that this method does not perform any kind of validation on the returned elements.
     * @param responseMessage Response message from the server with the list values.
     * @return A list of <code>MessageListEntry</code> elements.
     * @throws ListOperationException If the method cannot create a <code>MessageList</code> object from the given payload.
     */
    private List<MessageListEntry> processPayload(final ResponseMessage responseMessage) throws ListOperationException {

        List<MessageListEntry> listMessageData = new ArrayList<>();

        try {

            Element message = responseMessage.getPayload().getAnies().get(0);
            MessageList messageList = (MessageList) XMLElementUtil.element2Obj(message, MessageList.class);

            List<Message> lstMsgs = messageList.getMessages();

            for (Message msg : lstMsgs) {

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

        } catch (JAXBException ex) {

            throw new ListOperationException(EnumErrorCatalog.ERR_LST_019, ex, ex.getMessage());
        }

        return listMessageData;
    }
}

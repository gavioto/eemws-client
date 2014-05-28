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
package es.ree.eemws.client.listmessages;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Element;

import _504.iec62325.messages._1._0.MessageList;
import _504.iec62325.messages._1._0.MessageList.Message;
import _504.iec62325.messages._1._0.StatusType;
import _504.iec62325.messages._1._0.TimeIntervalType;
import _504.iec62325.wss._1._0.MsgFaultMsg;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.OptionType;
import ch.iec.tc57._2011.schema.message.PayloadType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.schema.message.RequestMessage;
import ch.iec.tc57._2011.schema.message.RequestType;
import ch.iec.tc57._2011.schema.message.RequestType.ID;
import ch.iec.tc57._2011.schema.message.ResponseMessage;
import es.ree.eemws.client.common.ConstantMessage;
import es.ree.eemws.client.common.ErrorText;
import es.ree.eemws.client.common.ParentClient;
import es.ree.eemws.client.exception.ClientException;
import es.ree.eemws.core.utils.xml.XMLElementUtil;
import es.ree.eemws.core.utils.xml.XMLGregorianCalendarFactory;


/**
 * Obtain a list of available messages for the client according to a given filter.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class ListMessages extends ParentClient {

    /** Verb of the action. */
    private static final String REQUEST_VERB = "get";

    /** Noun of the action. */
    private static final String REQUEST_NOUN = "MessageList";

    /** Name of the IntervalType option. */
    private static final String REQUEST_INTERVAL_TYPE_OPTION = "IntervalType";

    /** Name of the MessageIdentification option. */
    private static final String REQUEST_MESSAGE_IDENTIFICATION_OPTION = "MessageIdentification";

    /** Name of the MsgType option. */
    private static final String REQUEST_MSG_TYPE_OPTION = "MsgType";

    /** Name of the Owner option. */
    private static final String REQUEST_OWNER_OPTION = "Owner";

    /** Noun of the action. */
    private static final String RESPONSE_NOUN = "MessageList";

    /**
     * Constructor.
     */
    public ListMessages() {

        setSignRequest(false);
        setVerifyResponse(true);
    }

    /**
     * This method obtain a list of available messages for the client according to a given filter.
     * @param startTime Specifies that the list of messages returned should only include messages whose end of their
     *                  Application TimeInterval (Document TimeInterval) or Server Timestamp comes after the provided date.
     * @param endTime Specifies that the list of messages returned should only include messages whose start of their Application TimeInterval
     *                or ServerTimestamp (when the message was received or published in the server) comes before the provided date.
     * @return List of data message.
     * @throws ClientException Exception with the error.
     */
    public List<MessageData> list(final Date startTime, final Date endTime)
            throws ClientException {

        return list(startTime, endTime, null, null, null, null);
    }

    /**
     * This method obtain a list of available messages for the client according to a given filter.
     * @param startTime Specifies that the list of messages returned should only include messages whose end of their
     *                  Application TimeInterval (Document TimeInterval) or Server Timestamp comes after the provided date.
     * @param endTime Specifies that the list of messages returned should only include messages whose start of their Application TimeInterval
     *                or ServerTimestamp (when the message was received or published in the server) comes before the provided date.
     * @param intervalType Indicates whether the StartTime and EndTime refer to Application TimeInterval or to Server Timestamp.
     *                     Permitted values: "Application" (default), "Server".
     * @return List of data message.
     * @throws ClientException Exception with the error.
     */
    public List<MessageData> list(final Date startTime, final Date endTime, final String intervalType)
            throws ClientException {

        return list(startTime, endTime, intervalType, null, null, null);
    }

    /**
     * This method obtain a list of available messages for the client according to a given filter.
     * @param startTime Specifies that the list of messages returned should only include messages whose end of their
     *                  Application TimeInterval (Document TimeInterval) or Server Timestamp comes after the provided date.
     * @param endTime Specifies that the list of messages returned should only include messages whose start of their Application TimeInterval
     *                or ServerTimestamp (when the message was received or published in the server) comes before the provided date.
     * @param intervalType Indicates whether the StartTime and EndTime refer to Application TimeInterval or to Server Timestamp.
     *                     Permitted values: "Application" (default), "Server".
     * @param messageIdentification Specifies that the list of messages returned should only include messages whose Message Identification
     *                              is compliant with the pattern provided in this parameter. (“*” can be used as a wildcard).
     * @param msgType Specifies that the list of messages returned should only include messages of the provided type.
     * @param owner Specifies that the list of messages returned should only include messages belonging to the provided Owner.
     * @return List of data message.
     * @throws ClientException Exception with the error.
     */
    public List<MessageData> list(final Date startTime, final Date endTime, final String intervalType, final String messageIdentification, final String msgType, final String owner)
            throws ClientException {

        return list(null, startTime, endTime, intervalType, messageIdentification, msgType, owner);
    }

    /**
     * This method obtain a list of available messages for the client according to a given filter.
     * @param code Specifies that the list of messages returned should only include messages with an internal identification number higher than the provided code.
     *             This means that the list will contain messages that are newer to the given one.
     *             For optimization purposes, if this filter is used, only messages available since the 00.00 of D-1 (day before) are guaranteed to be included in the response list
     * @return List of data message.
     * @throws ClientException Exception with the error.
     */
    public List<MessageData> list(final Long code)
            throws ClientException {

        return list(code, null, null, null);
    }

    /**
     * This method obtain a list of available messages for the client according to a given filter.
     * @param code Specifies that the list of messages returned should only include messages with an internal identification number higher than the provided code.
     *             This means that the list will contain messages that are newer to the given one.
     *             For optimization purposes, if this filter is used, only messages available since the 00.00 of D-1 (day before) are guaranteed to be included in the response list
     * @param messageIdentification Specifies that the list of messages returned should only include messages whose Message Identification
     *                              is compliant with the pattern provided in this parameter. (“*” can be used as a wildcard).
     * @param msgType Specifies that the list of messages returned should only include messages of the provided type.
     * @param owner Specifies that the list of messages returned should only include messages belonging to the provided Owner.
     * @return List of data message.
     * @throws ClientException Exception with the error.
     */
    public List<MessageData> list(final Long code, final String messageIdentification, final String msgType, final String owner)
            throws ClientException {

        return list(code, null, null, null, messageIdentification, msgType, owner);
    }

    /**
     * This method obtain a list of available messages for the client according to a given filter.
     * @param code Specifies that the list of messages returned should only include messages with an internal identification number higher than the provided code.
     *             This means that the list will contain messages that are newer to the given one.
     *             For optimization purposes, if this filter is used, only messages available since the 00.00 of D-1 (day before) are guaranteed to be included in the response list
     * @param startTime Specifies that the list of messages returned should only include messages whose end of their
     *                  Application TimeInterval (Document TimeInterval) or Server Timestamp comes after the provided date.
     * @param endTime Specifies that the list of messages returned should only include messages whose start of their Application TimeInterval
     *                or ServerTimestamp (when the message was received or published in the server) comes before the provided date.
     * @param intervalType Indicates whether the StartTime and EndTime refer to Application TimeInterval or to Server Timestamp.
     *                     Permitted values: "Application" (default), "Server".
     * @param messageIdentification Specifies that the list of messages returned should only include messages whose Message Identification
     *                              is compliant with the pattern provided in this parameter. (“*” can be used as a wildcard).
     * @param msgType Specifies that the list of messages returned should only include messages of the provided type.
     * @param owner Specifies that the list of messages returned should only include messages belonging to the provided Owner.
     * @return List of data message.
     * @throws ClientException Exception with the error.
     */
    private List<MessageData> list(final Long code,
            final Date startTime,
            final Date endTime,
            final String intervalType,
            final String messageIdentification,
            final String msgType,
            final String owner) throws ClientException {

        try {

            RequestMessage requestMessage = createRequest(code, startTime, endTime, intervalType, messageIdentification, msgType, owner);
            ResponseMessage responseMessage = sendMessage(requestMessage);
            return getListMessageData(responseMessage);

        } catch (MsgFaultMsg e) {

            throw new ClientException(e);
        }
    }

    /**
     * This method create the request message.
     * @param code Specifies that the list of messages returned should only include messages with an internal identification number higher than the provided code.
     *             This means that the list will contain messages that are newer to the given one.
     *             For optimization purposes, if this filter is used, only messages available since the 00.00 of D-1 (day before) are guaranteed to be included in the response list
     * @param startTime Specifies that the list of messages returned should only include messages whose end of their
     *                  Application TimeInterval (Document TimeInterval) or Server Timestamp comes after the provided date.
     * @param endTime Specifies that the list of messages returned should only include messages whose start of their Application TimeInterval
     *                or ServerTimestamp (when the message was received or published in the server) comes before the provided date.
     * @param intervalType Indicates whether the StartTime and EndTime refer to Application TimeInterval or to Server Timestamp.
     *                     Permitted values: "Application" (default), "Server".
     * @param messageIdentification Specifies that the list of messages returned should only include messages whose Message Identification
     *                              is compliant with the pattern provided in this parameter. (“*” can be used as a wildcard).
     * @param msgType Specifies that the list of messages returned should only include messages of the provided type.
     * @param owner Specifies that the list of messages returned should only include messages belonging to the provided Owner.
     * @return Request message.
     */
    private RequestMessage createRequest(final Long code,
            final Date startTime,
            final Date endTime,
            final String intervalType,
            final String messageIdentification,
            final String msgType,
            final String owner) {

        RequestMessage requestMessage = new RequestMessage();

        HeaderType header = createHeader(REQUEST_VERB, REQUEST_NOUN);
        requestMessage.setHeader(header);

        RequestType resquest = new RequestType();

        if (code != null) {

            ID id = new ID();
            id.setValue(code.toString());
            List<ID> ids = resquest.getIDS();
            ids.add(id);

        } else if (startTime != null && endTime != null) {

            XMLGregorianCalendar xmlStartTime = XMLGregorianCalendarFactory.getInstance(startTime);
            resquest.setStartTime(xmlStartTime);

            XMLGregorianCalendar xmlEndTime = XMLGregorianCalendarFactory.getInstance(endTime);
            resquest.setEndTime(xmlEndTime);
        }

        List<OptionType> options = resquest.getOptions();
        if (intervalType != null) {

            OptionType option = createOption(REQUEST_INTERVAL_TYPE_OPTION, intervalType);
            options.add(option);
        }

        if (messageIdentification != null) {

            OptionType option = createOption(REQUEST_MESSAGE_IDENTIFICATION_OPTION, messageIdentification);
            options.add(option);
        }

        if (msgType != null) {

            OptionType option = createOption(REQUEST_MSG_TYPE_OPTION, msgType);
            options.add(option);
        }

        if (owner != null) {

            OptionType option = createOption(REQUEST_OWNER_OPTION, owner);
            options.add(option);
        }

        requestMessage.setRequest(resquest);
        return requestMessage;
    }

    /**
     * This method process the response message.
     * @param responseMessage Response message.
     * @return List of data message.
     * @throws ClientException Exception with the error.
     */
    private List<MessageData> getListMessageData(final ResponseMessage responseMessage)
            throws ClientException {

        HeaderType header = responseMessage.getHeader();
        checkHeaderResponse(header);

        ReplyType reply = responseMessage.getReply();
        checkReplyResponse(reply);

        PayloadType payload = responseMessage.getPayload();
        return processPayload(payload);
    }

    /**
     * This method check the header of the response.
     * @param header Header of the response.
     * @throws ClientException Exception with the error.
     */
    private void checkHeaderResponse(final HeaderType header) throws ClientException {

        String verb = header.getVerb();
        String noun = header.getNoun();

        boolean error = !ConstantMessage.RESPONSE_VERB.equals(verb) || !RESPONSE_NOUN.equals(noun);
        if (error) {

            Object[] paramsText = {verb, noun, ConstantMessage.RESPONSE_VERB, RESPONSE_NOUN};
            String errorText = MessageFormat.format(ErrorText.ERROR_TEXT_001, paramsText);
            throw new ClientException(errorText);
        }
    }

    /**
     * This method process the payload of the message.
     * @param payload Payload of the message.
     * @return List of data message.
     * @throws ClientException Exception with the error.
     */
    private List<MessageData> processPayload(final PayloadType payload) throws ClientException {

        List<MessageData> listMessageData = new ArrayList<MessageData>();

        try {

            Element message = payload.getAnies().get(0);
            MessageList messageList = (MessageList) XMLElementUtil.elment2Obj(message, MessageList.class);

            List<Message> listaMensajes = messageList.getMessages();
            for (Message msg : listaMensajes) {

                MessageData messageData = new MessageData();
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

                    messageData.setApplicationStartTime(interval.getStart().toGregorianCalendar());

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

            Object[] paramsText = {e.getMessage()};
            String errorText = MessageFormat.format(ErrorText.ERROR_TEXT_003, paramsText);
            throw new ClientException(errorText);
        }

        return listMessageData;
    }
}

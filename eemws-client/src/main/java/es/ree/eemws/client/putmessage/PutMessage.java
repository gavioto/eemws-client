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
package es.ree.eemws.client.putmessage;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import _504.iec62325.wss._1._0.MsgFaultMsg;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.PayloadType;
import ch.iec.tc57._2011.schema.message.RequestMessage;
import ch.iec.tc57._2011.schema.message.RequestType;
import ch.iec.tc57._2011.schema.message.RequestType.ID;
import ch.iec.tc57._2011.schema.message.ResponseMessage;
import es.ree.eemws.client.common.ErrorText;
import es.ree.eemws.client.common.ParentClient;
import es.ree.eemws.client.exception.ClientException;
import es.ree.eemws.core.utils.file.GZIPUtil;
import es.ree.eemws.core.utils.xml.XMLElementUtil;


/**
 * The Put Message service is used to send a message to the server for further processing
 * following the rules of the European Energy Markets for Electricity.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class PutMessage extends ParentClient {

    /** Verb of the action. */
    private static final String VERB = "create";

    /** Name of the name of the binary file. */
    private static final String NAME_OPTION = "name";

    /**
     * Constructor.
     */
    public PutMessage() {

        setSignRequest(true);
        setVerifyResponse(true);
    }

    /**
     * This method is used to send a message to the server for further processing
     * following the rules of the European Energy Markets for Electricity.
     * @param noun Noun.
     * @param name Name of the binary file.
     * @param data Binary content.
     * @return String with the XML response message.
     * @throws ClientException Exception with the error.
     */
    public String put(final String noun, final String name, final byte[] data)
            throws ClientException {

        return put(noun, name, data, null);
    }

    /**
     * This method is used to send a message to the server for further processing
     * following the rules of the European Energy Markets for Electricity.
     * @param noun Noun.
     * @param name Name of the binary file.
     * @param data Binary content.
     * @param format Hint as to format of payload.
     * @return String with the XML response message.
     * @throws ClientException Exception with the error.
     */
    public String put(final String noun, final String name, final byte[] data, final String format)
            throws ClientException {

        try {

            RequestMessage requestMessage = createRequest(noun, name, data, format);
            ResponseMessage responseMessage = sendMessage(requestMessage);
            return processResponse(responseMessage);

        } catch (MsgFaultMsg | IOException e) {

            throw new ClientException(e.getMessage(), e);
        }
    }

    /**
     * This method create the request message.
     * @param noun Noun.
     * @param name Name of the binary file.
     * @param data Binary content.
     * @param format Hint as to format of payload.
     * @return Request message.
     * @throws IOException Exception with the error.
     */
    private RequestMessage createRequest(final String noun, final String name, final byte[] data, final String format)
            throws IOException {

        RequestMessage message = new RequestMessage();

        HeaderType header = createHeader(VERB, noun);
        message.setHeader(header);

        RequestType resquest = new RequestType();

        ID id = new ID();
        id.setIdType(NAME_OPTION);
        id.setValue(name);

        List<ID> ids = resquest.getIDS();
        ids.add(id);

        message.setRequest(resquest);

        PayloadType payload = new PayloadType();
        byte[] dataCompress = GZIPUtil.compress(data);
        String dataBase64 = DatatypeConverter.printBase64Binary(dataCompress);
        payload.setCompressed(dataBase64);
        if (format != null) {
            payload.setFormat(format);
        }
        message.setPayload(payload);

        return message;
    }

    /**
     * This method is used to send a message to the server for further processing
     * following the rules of the European Energy Markets for Electricity.
     * @param xmlMessage The xml message that is being sent to the server.
     * @return String with the XML response message.
     * @throws ClientException Exception with the error.
     */
    public String put(final String xmlMessage) throws ClientException {

        try {

            RequestMessage requestMessage = createRequest(xmlMessage);
            ResponseMessage responseMessage = sendMessage(requestMessage);
            return processResponse(responseMessage);

        } catch (MsgFaultMsg e) {

            throw new ClientException(e.getMessage(), e);
        }
    }

    /**
     * This method create the request message.
     * @param xmlMessage The xml message that is being sent to the server.
     * @return Request message.
     * @throws ClientException Exception with the error.
     */
    private RequestMessage createRequest(final String xmlMessage)
            throws ClientException {

        try {

            Element xml = XMLElementUtil.string2Element(xmlMessage);

            RequestMessage message = new RequestMessage();

            HeaderType header = createHeader(VERB, xml.getLocalName());
            message.setHeader(header);

            PayloadType payload = new PayloadType();
            payload.getAnies().add(xml);
            message.setPayload(payload);

            return message;

        } catch (ParserConfigurationException | SAXException | IOException e) {

            Object[] paramsText = {e.getMessage()};
            String errorText = MessageFormat.format(ErrorText.ERROR_TEXT_004, paramsText);
            throw new ClientException(errorText, e);
        }
    }
}

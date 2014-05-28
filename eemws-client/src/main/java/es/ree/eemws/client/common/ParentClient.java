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
package es.ree.eemws.client.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import org.w3c.dom.Element;

import _504.iec62325.wss._1._0.MsgFaultMsg;
import _504.iec62325.wss._1._0.PortTFEDIType;
import _504.iec62325.wss._1._0.ServiceEME;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.OptionType;
import ch.iec.tc57._2011.schema.message.PayloadType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.schema.message.RequestMessage;
import ch.iec.tc57._2011.schema.message.ResponseMessage;
import es.ree.eemws.client.exception.ClientException;
import es.ree.eemws.client.handler.SendHandler;
import es.ree.eemws.core.utils.xml.XMLElementUtil;


/**
 * Parent class of messages that communicate with the Web Service EME.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public abstract class ParentClient {

    /** Service name of the Web Service EME. */
    private static final QName SERVICE_NAME = new QName("urn:iec62325.504:wss:1:0", "ServiceEME");

    /** WSDL extension. */
    private static final String WSDL_EXTENSION = "?WSDL";

    /** URL of the end point of the web service. */
    private URL endPoint = null;

    /** Check to sign the request. */
    private boolean signRequest = true;

    /** Check to verify the response. */
    private boolean verifyResponse = true;

    /**
     * This method set the URL of the end point of the web service.
     * @param url URL of the end point of the web service.
     */
    public final void setEndPoint(final String url) {

        try {

            endPoint = new URL(url + WSDL_EXTENSION);

        } catch (MalformedURLException e) {

            endPoint = null;
        }
    }

    /**
     * This method set the check to sign the request.
     * @param check Check to sign the request.
     */
    public final void setSignRequest(final boolean check) {

        signRequest = check;
    }

    /**
     * This method set the check to verify the response.
     * @param check Check to verify the response.
     */
    public final void setVerifyResponse(final boolean check) {

        verifyResponse = check;
    }

    /**
     * This method send a message.
     * @param message Message to send.
     * @return Response to the message.
     * @throws MsgFaultMsg Fault message.
     */
    protected final ResponseMessage sendMessage(final RequestMessage message) throws MsgFaultMsg {

        ServiceEME service = new ServiceEME(endPoint, SERVICE_NAME);
        PortTFEDIType port = service.getServiceEMEPort();

        Binding binding = ((BindingProvider) port).getBinding();
        @SuppressWarnings("rawtypes")
		List<Handler> handlerList = binding.getHandlerChain();
        handlerList.add(new SendHandler(signRequest, verifyResponse));
        binding.setHandlerChain(handlerList);

        return port.request(message);
    }

    /**
     * This method create the header of the message.
     * @param verb Verb of the message.
     * @param noun Noun of the message.
     * @return Header of the message.
     */
    protected final HeaderType createHeader(final String verb, final String noun) {

        HeaderType header = new HeaderType();
        header.setVerb(verb);
        header.setNoun(noun);
        return header;
    }

    /**
     * This method create a new option.
     * @param name Name of the new option.
     * @param value Value of the new option.
     * @return New option.
     */
    protected final OptionType createOption(final String name, final String value) {

        OptionType option = new OptionType();
        option.setName(name);
        option.setValue(value);
        return option;
    }

    /**
     * This method process the response message.
     * @param responseMessage Response message.
     * @return String with the XML message.
     * @throws ClientException Exception with the error.
     */
    protected final String processResponse(final ResponseMessage responseMessage)
            throws ClientException {

        HeaderType header = responseMessage.getHeader();
        ReplyType reply = responseMessage.getReply();
        PayloadType payload = responseMessage.getPayload();

        checkHeaderResponse(header, payload);
        checkReplyResponse(reply);

        return processPayload(payload);
    }

    /**
     * This method check the header of the response.
     * @param header Header of the response.
     * @param payload Payload with the XML message.
     * @throws ClientException Exception with the error.
     */
    protected final void checkHeaderResponse(final HeaderType header, final PayloadType payload)
            throws ClientException {

        String verb = header.getVerb();
        String noun = header.getNoun();

        Element xml = payload.getAnies().get(0);
        String rootTag = xml.getLocalName();

        boolean error = !ConstantMessage.RESPONSE_VERB.equals(verb) || !rootTag.equals(noun);
        if (error) {

            Object[] paramsText = {verb, noun, ConstantMessage.RESPONSE_VERB, rootTag};
            String errorText = MessageFormat.format(ErrorText.ERROR_TEXT_001, paramsText);
            throw new ClientException(errorText);
        }
    }

    /**
     * This method check the reply of the response.
     * @param reply Reply of the response.
     * @throws ClientException Exception with the error.
     */
    protected final void checkReplyResponse(final ReplyType reply) throws ClientException {

        String result = reply.getResult();
        boolean error = !ConstantMessage.RESPONSE_REPLY_RESULT.equals(result);
        if (error) {

            Object[] paramsText = {result};
            String errorText = MessageFormat.format(ErrorText.ERROR_TEXT_002, paramsText);
            throw new ClientException(errorText);
        }
    }

    /**
     * This method get the XML in the payload of the message.
     * @param payload Payload of the message.
     * @return XML in the payload of the message.
     * @throws ClientException Exception with the error.
     */
    private String processPayload(final PayloadType payload) throws ClientException {

        try {

            Element message = payload.getAnies().get(0);
            return XMLElementUtil.element2String(message);

        } catch (TransformerException | ParserConfigurationException e) {

            Object[] paramsText = {e.getMessage()};
            String errorText = MessageFormat.format(ErrorText.ERROR_TEXT_003, paramsText);
            throw new ClientException(errorText, e);
        }
    }
}

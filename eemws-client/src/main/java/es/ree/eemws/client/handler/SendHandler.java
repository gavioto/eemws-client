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
package es.ree.eemws.client.handler;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import es.ree.eemws.core.utils.security.SignatureManager;
import es.ree.eemws.core.utils.security.SignatureManagerException;
import es.ree.eemws.core.utils.soap.SOAPUtil;
import es.ree.eemws.core.utils.xml.XMLUtil;


/**
 * This class handle the send of message to the web service.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class SendHandler implements SOAPHandler<SOAPMessageContext> {

    /** Tag that contain the message IEC 61968-100. */
    private static final String TAG_MSG_IEC_100 = "Body";

    /** Sign request. */
    private boolean signRequest;

    /** Verify response. */
    private boolean verifyResponse;

    /** Log messages. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Constructor.
     * @param bSignRequest Sign request.
     * @param bVerifyResponse Verify response.
     */
    public SendHandler(final boolean bSignRequest, final boolean bVerifyResponse) {

        signRequest = bSignRequest;
        verifyResponse = bVerifyResponse;
    }

    /**
     * This method get the headers that the handler process.
     * @return Empty set.
     */
    @Override
    public Set<QName> getHeaders() {

        return Collections.emptySet();
    }

    /**
     * This method close the communication.
     * @param mc Message context.
     */
    @Override
    public void close(final MessageContext mc) {

        /* This method should not be implemented. */
    }

    /**
     * This method handle fault messages.
     * @param messageContext Message context.
     * @return <code>true</code>.
     */
    @Override
    public boolean handleFault(final SOAPMessageContext messageContext) {

        return true;
    }

    /**
     * This method handle message of the client web service.
     * @param messageContext Message context.
     * @return <code>true</code> if the flow is to continue
     *         <code>false</code> if the flow is to stop.
     */
    @Override
    public boolean handleMessage(final SOAPMessageContext messageContext) {

        boolean returnValue = true;

        try {

            boolean output = ((Boolean) messageContext.get(SOAPMessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue();

            if ((output && signRequest) || (!output && verifyResponse)) {

                SOAPMessage message = messageContext.getMessage();
                StringBuilder soapBody = new StringBuilder(XMLUtil.getNodeValue(TAG_MSG_IEC_100, SOAPUtil.soapMessage2String(message)));

                if (output && signRequest) {

                    SignatureManager.signString(soapBody);
                    SOAPUtil.setSOAPMessage(message, soapBody);

                } else if (!output && verifyResponse) {

                    SignatureManager.verifyString(soapBody);
                }
            }

        } catch (SOAPException | SignatureManagerException e) {

            returnValue = false;
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return returnValue;
    }
}

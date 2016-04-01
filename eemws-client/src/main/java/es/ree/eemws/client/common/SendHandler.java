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
package es.ree.eemws.client.common;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.xml.sax.SAXException;

import es.ree.eemws.core.utils.error.EnumErrorCatalog;
import es.ree.eemws.core.utils.iec61968100.EnumMessageStatus;
import es.ree.eemws.core.utils.iec61968100.MessageMetaData;
import es.ree.eemws.core.utils.iec61968100.StringBuilderMessage;
import es.ree.eemws.core.utils.operations.HandlerException;
import es.ree.eemws.core.utils.security.SignatureManager;
import es.ree.eemws.core.utils.security.SignatureManagerException;
import es.ree.eemws.core.utils.security.SignatureSyntaxException;
import es.ree.eemws.core.utils.security.SignatureVerificationException;
import es.ree.eemws.core.utils.xml.XMLUtil;

/**
 * Implements a SOAP handler in order to sign requests and verify responses' signatures.
 * This class is also used for debug purposes.
 * 
 * @author Red Eléctrica de España S.A.U.
 * @version 1.1 10/01/2016
 */
public final class SendHandler implements SOAPHandler<SOAPMessageContext> {

    /** Flag that sets whether sign the request. */
    private boolean flagSignRequest;

    /** Flag that sets whether verify response's signature. */
    private boolean flagVerifyResponseSignature;

    /** Certificate to sign request. */
    private X509Certificate certificate = null;

    /** Private key of the certificate. */
    private PrivateKey privateKey = null;

    /** Message's metadata. */ 
    private MessageMetaData messageData = null;

    /** Log (only for debug). */
    private static final Logger LOGGER = Logger.getLogger(SendHandler.class.getName());

    /**
     * Constructor. Creates a new handler with the given configuration.
     * @param flgSignRequest <code>true</code> if the request has to be signed.
     * @param flgVerifyRespSign <code>true</code> if the response's signature has to be validated.
     * @param msgMetaData Message meta data. Contains values form the message context such as certificates.
     * @param inCertificate Certificate to sign request.
     * @param inPrivateKey Private key of the certificate.
     */
    public SendHandler(final boolean flgSignRequest, final boolean flgVerifyRespSign, final MessageMetaData msgMetaData, 
            final X509Certificate inCertificate, final PrivateKey inPrivateKey) {

        flagSignRequest = flgSignRequest;
        flagVerifyResponseSignature = flgVerifyRespSign; 
        messageData = msgMetaData;
        certificate = inCertificate;
        privateKey = inPrivateKey;
    }

    /**
     * Gets this handler headers.
     * @return An empty set.
     */
    @Override
    public Set<QName> getHeaders() {

        return Collections.emptySet();
    }

    /**
     * Called at the end of a message interchange.
     * @param mc Message context.
     */
    @Override
    public void close(final MessageContext mc) {

        /* This method should not be implemented. */
    }

    /**
     * Handles fault message.
     * Message status is set to "FAILED" and the message fault is stored in the metadata.
     * Note: Applications will receive a general Exception with details, the programmer 
     * is free to retrieve the "raw" fault message.
     * @param messageContext Message context.
     * @return <code>true</code>.
     */
    @Override
    public boolean handleFault(final SOAPMessageContext messageContext) {
        
        try {
            messageData.setStatus(EnumMessageStatus.FAILED);
            messageData.setServerTimestamp(Calendar.getInstance());
            messageData.setRejectText(new StringBuilderMessage(messageContext).getStringMessage().toString());
            
            logSoapConversation(messageContext);

        } catch (SOAPException e) {
        
            LOGGER.log(Level.FINE, "Unable to convert soap message to xml...", e); //$NON-NLS-1$
        
        }
        
        return true;
    }

    /**
     * Handles the message. Performs signature for outgoing messages and signature validation for incoming messages.
     * @param messageContext Message context.
     * @return <code>true</code> if the flow can continue (no error). <code>false</code> otherwise.
     */
    @Override
    public boolean handleMessage(final SOAPMessageContext messageContext) {

        boolean returnValue = true;
        boolean output = ((Boolean) messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue();

        try {

            if (output && flagSignRequest) {

                StringBuilderMessage sbm = new StringBuilderMessage(messageContext);

                if (certificate != null && privateKey != null) {
                    SignatureManager.signString(sbm.getStringMessage(), privateKey, certificate);
                } else {
                    SignatureManager.signString(sbm.getStringMessage());
                }

                messageContext.getMessage().getSOAPBody().removeContents();
                messageContext.getMessage().getSOAPBody().addDocument(XMLUtil.string2Document(sbm.getStringMessage()));

            } else if (!output) {
                
                StringBuilderMessage sbm = new StringBuilderMessage(messageContext);
                
                messageData.setStatus(sbm.getStatus());
                messageData.setServerTimestamp(Calendar.getInstance());
                
                if (flagVerifyResponseSignature) {
                    X509Certificate x509Certificate = SignatureManager.verifyString(sbm.getStringMessage());
                    messageData.setSignatureCertificate(x509Certificate);
                }
            }

        } catch (SignatureVerificationException e) {
            returnValue = false;
            messageData.setSignatureCertificate(e.getDetails().getSignatureCertificate());
            messageData.setException(new HandlerException(EnumErrorCatalog.ERR_HAND_007, e));

        } catch (SignatureSyntaxException e) {
            returnValue = false;
            messageData.setException(new HandlerException(EnumErrorCatalog.ERR_HAND_008, e));

        } catch (SignatureManagerException e) {
            returnValue = false;
            messageData.setException(new HandlerException(EnumErrorCatalog.ERR_HAND_009, e));

        } catch (ParserConfigurationException | SAXException | IOException | SOAPException e) {
            returnValue = false;
            messageData.setException(new HandlerException(EnumErrorCatalog.ERR_HAND_004, e));

        } finally {

            logSoapConversation(messageContext);
        }

        return returnValue;
    }

    /**
     * Logs the current SOAP conversation. Log only appears in debug (FINE) mode.
     * @param messageContext Current soap context.
     */
    private void logSoapConversation(final SOAPMessageContext messageContext) {

        if (LOGGER.isLoggable(Level.FINE)) {
            boolean output = ((Boolean) messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue();

            if (output) {
                LOGGER.fine(">>> out message >>>"); //$NON-NLS-1$
            } else {
                LOGGER.fine("<<< input message <<<"); //$NON-NLS-1$
            }

            try {
                LOGGER.fine(new StringBuilderMessage(messageContext).getStringMessage().toString());
                LOGGER.fine("---- end of message ---- "); //$NON-NLS-1$
            } catch (SOAPException e) {
                LOGGER.log(Level.FINE, "Unable to convert soap message to xml...", e); //$NON-NLS-1$
            }
        }
    }
}

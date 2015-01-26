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

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import es.ree.eemws.core.utils.iec61968100.GenericCodedException;
import es.ree.eemws.core.utils.iec61968100.MessageMetaData;
import es.ree.eemws.core.utils.iec61968100.StringBuilderMessage;
import es.ree.eemws.core.utils.security.SignatureManager;
import es.ree.eemws.core.utils.security.SignatureManagerException;
import es.ree.eemws.core.utils.security.SignatureVerificationException;
import es.ree.eemws.core.utils.soap.SOAPUtil;

/**
 * This class handle the send of message to the web service.
 * 
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class SendHandler implements SOAPHandler<SOAPMessageContext> {
	
	/** Sign request. */
	private boolean signRequest;

	/** Verify response. */
	private boolean verifyResponse;

	/** Certificate to sign request. */
	private X509Certificate certificate = null;

	/** Private key of the certificate. */
	private PrivateKey privateKey = null;

	/** Data of the message. */
	private MessageMetaData messageData = null;
	
	/** Message as String. */
	private StringBuilderMessage stringBuilderMess = null;

	/** Log messages. */
	private Logger logger = Logger.getLogger(getClass().getName());

	/**
	 * Constructor.
	 * @param bSignRequest Sign request.
	 * @param bVerifyResponse Verify response.
	 * @param inMessageData Data of the message.
	 */
	public SendHandler(final boolean bSignRequest, final boolean bVerifyResponse, final MessageMetaData inMessageData, final StringBuilderMessage sbm) {

		this(bSignRequest, bVerifyResponse, inMessageData, sbm, null, null);
	}

	/**
	 * Constructor.
	 * @param bSignRequest Sign request.
	 * @param bVerifyResponse Verify response.
	 * @param inMessageData Data of the message.
	 * @param inCertificate Certificate to sign request.
	 * @param inPrivateKey Private key of the certificate.
	 */
	public SendHandler(final boolean bSignRequest, final boolean bVerifyResponse, final MessageMetaData inMessageData, final StringBuilderMessage sbm, final X509Certificate inCertificate, final PrivateKey inPrivateKey) {

		signRequest = bSignRequest;
		verifyResponse = bVerifyResponse;
		messageData = inMessageData;
		stringBuilderMess = sbm;
		certificate = inCertificate;
		privateKey = inPrivateKey;
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

		logSoapConversation(messageContext);

		return true;
	}

	/**
	 * This method handle message of the client web service.
	 * @param messageContext Message context.
	 * @return <code>true</code> if the flow is to continue <code>false</code> if the flow is to stop.
	 */
	@Override
	public boolean handleMessage(final SOAPMessageContext messageContext) {

		boolean returnValue = true;
		boolean output = ((Boolean) messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue();

		try {

			if ((output && signRequest) || !output) {
				
				if (output && signRequest) {

				    StringBuilderMessage sbm = new StringBuilderMessage(messageContext);
				    
					if (certificate != null && privateKey != null) {

						SignatureManager.signString(sbm.getStringMessage(), privateKey, certificate);

					} else {

						SignatureManager.signString(sbm.getStringMessage());
					}
					
					SOAPUtil.setSOAPMessage(messageContext.getMessage(), sbm.getStringMessage());

				} else if (!output) {
				    
				    stringBuilderMess.setStringMessage(new StringBuilderMessage(messageContext));
					messageData.setServerTimestamp(Calendar.getInstance());
					
					if (verifyResponse) {
						X509Certificate x509Certificate = SignatureManager.verifyString(stringBuilderMess.getStringMessage());
						messageData.setSignatureCertificate(x509Certificate);
					}
				}
			}

		} catch (SignatureVerificationException e) {
			returnValue = false;
			messageData.setSignatureCertificate(e.getDetails().getSignatureCertificate());
			logger.log(Level.SEVERE, e.getMessage() + " Details: " + e.getDetails().toString()); //$NON-NLS-1$

		} catch (GenericCodedException | SOAPException | SignatureManagerException e) {

			returnValue = false;
			logger.log(Level.SEVERE, e.getMessage(), e);

		} finally {

			logSoapConversation(messageContext);

		}

		return returnValue;
	}

	/**
	 * Logs the current SOAP conversation. This method is only called in debug mode.
	 * @param messageContext Current soap context.
	 */
	private void logSoapConversation(final SOAPMessageContext messageContext) {
		if (logger.isLoggable(Level.FINE)) {
			boolean output = ((Boolean) messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue();

			if (output) {
				logger.fine(">>> out message >>>"); //$NON-NLS-1$
			} else {
				logger.fine("<<< input message <<<"); //$NON-NLS-1$
			}
			try {
				logger.fine(new StringBuilderMessage(messageContext).getStringMessage().toString());
				logger.fine("---- end of message ---- "); //$NON-NLS-1$
			} catch (GenericCodedException e) {
				logger.log(Level.FINE, "Unable to convert soap message to xml...", e); //$NON-NLS-1$
			}
		}
	}
}

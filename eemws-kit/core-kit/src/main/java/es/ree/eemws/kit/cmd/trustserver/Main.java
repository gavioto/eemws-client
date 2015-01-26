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

package es.ree.eemws.kit.cmd.trustserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.xml.ws.WebServiceException;

import es.ree.eemws.client.common.ClientException; 
import es.ree.eemws.client.get.GetMessage;
import es.ree.eemws.client.list.ListMessages;
import es.ree.eemws.client.list.MessageListEntry;
import es.ree.eemws.core.utils.config.ConfigException; 
import es.ree.eemws.core.utils.iec61968100.MessageMetaData;
import es.ree.eemws.core.utils.security.X509Util;
import es.ree.eemws.kit.cmd.ParentMain;
import es.ree.eemws.kit.common.Messages;

/**
 * Adds the given server to the list of trusted servers.
 * 
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class Main extends ParentMain {

	/** Name of the command. */
	private static final String COMMAND_NAME = "trustserver"; //$NON-NLS-1$

	/** Sets text for parameter <code>url</code>. */
	private static final String PARAMETER_URL = Messages.getString("PARAMETER_URL"); //$NON-NLS-1$

	/** Sets text for parameter <code>force</code>. */
    private static final String PARAMETER_FORCE = Messages.getString("TRUSTSERVER_FORCE"); //$NON-NLS-1$
	
	/** Log messages. */
	private static final Logger LOGGER = Logger.getLogger(COMMAND_NAME);

	/** Default https port. */
	private static final int DEFAULT_HTTPS_PORT = 443;

	/** Https protocol string. */
	private static final String HTTPS_PROTOCOL = "https"; //$NON-NLS-1$

	/** TSL protocol string. */
	private static final String TSL_PROTOCOL = "TLS"; //$NON-NLS-1$

	/** SO_TIMEOUT value. */
	private static final int SO_TIMEOUT = 10000;

	/** Java system property to get the trust store password. */
	private static final String LOCAL_TRUST_STORE_PASSWORD_KEY = "javax.net.ssl.trustStorePassword"; //$NON-NLS-1$

	/** Java system property to get the trust store file. */
	private static final String LOCAL_TRUST_STORE_FILE_KEY = "javax.net.ssl.trustStore"; //$NON-NLS-1$



	/** Local trust store. */
	private static KeyStore localTrustStore;

	/** A list of added certificates avoid trying to add twice the same certificate. */
	private static List<X509Certificate> addedCertificates = new ArrayList<X509Certificate>();

	/**
	 * Main. Opens a connection to the remote server and adds its certificates to the local trust store.
	 * @param args command line arguments.
	 */
	public static void main(final String[] args) {

		String urlEndPoint = ""; //$NON-NLS-1$
		try {

			List<String> arguments = new ArrayList<>(Arrays.asList(args));

			urlEndPoint = readParameter(arguments, PARAMETER_URL);
				
			boolean force = false; 
			if (arguments.contains(PARAMETER_FORCE)) {
			    force = true;
			    arguments.remove(PARAMETER_FORCE);
			}
			
			if (!arguments.isEmpty()) {
				throw new IllegalArgumentException(Messages.getString("UNKNOWN_PARAMETERS", arguments.toString())); //$NON-NLS-1$
			}

			urlEndPoint = setConfig(urlEndPoint);

			URL url = new URL(urlEndPoint);

			if (!HTTPS_PROTOCOL.equals(url.getProtocol())) {
				throw new IllegalArgumentException(Messages.getString("TRUSTSERVER_ONLY_HTTPS")); //$NON-NLS-1$
			}

			String localTrustFile = System.getProperty(LOCAL_TRUST_STORE_FILE_KEY);
			if (localTrustFile == null) {
				throw new IllegalArgumentException(Messages.getString("TRUSTSERVER_NO_TRUST_STORE")); //$NON-NLS-1$
			}
			
			String passwd = System.getProperty(LOCAL_TRUST_STORE_PASSWORD_KEY);
			if (passwd == null) {
				passwd = ""; //$NON-NLS-1$
			}			
			
			loadLocalTrustStore(localTrustFile, passwd.toCharArray());

			LOGGER.info(Messages.getString("TRUSTSERVER_TRUST_SIZE", localTrustStore.size())); //$NON-NLS-1$
			
			LOGGER.info(""); //$NON-NLS-1$
			LOGGER.info(Messages.getString("TRUSTSERVER_GETTING_SERVER_CERTICATES")); //$NON-NLS-1$
			
			boolean certAdded  = addServerCerts(urlEndPoint, force);
			
			if (certAdded) {
				LOGGER.info(Messages.getString("TRUSTSERVER_RERUN_COMMAND")); //$NON-NLS-1$
			} else {
				LOGGER.info(""); //$NON-NLS-1$
				LOGGER.info(Messages.getString("TRUSTSERVER_GETTING_SIGNATURE_CERTICATES")); //$NON-NLS-1$
				addSignatureCert(urlEndPoint);
			} 

			storeLocalTrustStore(localTrustFile, passwd.toCharArray());

		} catch (MalformedURLException e) {
			LOGGER.severe(Messages.getString("INVALID_URL", urlEndPoint)); //$NON-NLS-1$
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		} catch (ConfigException e) {

			LOGGER.severe(Messages.getString("INVALID_CONFIGURATION", e.getMessage())); //$NON-NLS-1$

			/* Shows stack trace only for debug. Don't bother the user with this details. */
			LOGGER.log(Level.FINE, Messages.getString("INVALID_CONFIGURATION", e.getMessage()), e); //$NON-NLS-1$
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			LOGGER.log(Level.SEVERE, Messages.getString("TRUSTSERVER_BAD_KEYSTORE"), e.getMessage()); //$NON-NLS-1$
		} catch (IllegalArgumentException e) {
			LOGGER.info(e.getMessage());
			LOGGER.info(Messages.getString("TRUSTSERVER_USAGE", PARAMETER_URL, PARAMETER_FORCE)); //$NON-NLS-1$
		}

	}

	/**
	 * Loads the local trust store	
	 * @param filePath Full trust store file path.
	 * @param passwd Trust store password. 
	 * @throws IOException if it's no possible to open the configured trust store. 
	 */
	private static void loadLocalTrustStore(final String filePath, final char[] passwd) throws IOException {
		File localCacert = new File (filePath);
		try (InputStream in = new FileInputStream(localCacert)) {
			localTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			localTrustStore.load(in, passwd);
		} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
			throw new IOException(Messages.getString("TRUSTSERVER_UNABLE_TO_LOAD"), e); //$NON-NLS-1$
		}
	}

	/**
	 * Saves (stores) the local trust store	
	 * @param filePath Full trust store file path.
	 * @param passwd Trust store password. 
	 * @throws IOException if it's no possible to open the configured trust store. 
	 */
	private static void storeLocalTrustStore(final String filePath, final char[] passwd) throws IOException {
		File localCacert = new File (filePath);
		try (OutputStream out = new FileOutputStream(localCacert)) {
			localTrustStore.store(out, passwd);
		} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException ex) {
			throw new IOException(Messages.getString("TRUSTSERVER_UNABLE_TO_SAVE"), ex); //$NON-NLS-1$
		}
	}

	/**
	 * Adds the certificate used for signature to the current trust store.
	 * This method perform a list + get process to get a signed message. 
	 * @param urlEndPoint Web service url.
	 * @throws KeyStoreException If the method is unable to add the retrieved certificate.
	 * @throws MalformedURLException If the current url is incorrecto (impossible at this point) 
	 */
	private static void addSignatureCert(final String urlEndPoint) throws KeyStoreException, MalformedURLException {

		URL url = new URL(urlEndPoint);
		GetMessage get = null;
		try {
			LOGGER.info(Messages.getString("TRUSTSERVER_GETTING_MSG_LIST")); //$NON-NLS-1$
			
			ListMessages list = new ListMessages();
			list.setEndPoint(url);
			List<MessageListEntry> listMsg = list.list(0L);
			
			if (listMsg.isEmpty()) {
				LOGGER.warning(Messages.getString("TRUSTSERVER_NO_MESSAGES_TO_LIST")); //$NON-NLS-1$
			} else {
				LOGGER.info(Messages.getString("TRUSTSERVER_MSG_LIST", listMsg.size())); //$NON-NLS-1$
				get = new GetMessage();
				get.setEndPoint(url);
				
				LOGGER.info(Messages.getString("TRUSTSERVER_MSG_GET", String.valueOf(listMsg.get(0).getCode().longValue()))); //$NON-NLS-1$
				get.get(listMsg.get(0).getCode().longValue());
			}
		} catch (ClientException ce) {
			
			LOGGER.log(Level.FINE, Messages.getString("TRUSTSERVER_UNABLE_TO_CONNECT_WITH_SERVER", ce.getMessage()), ce); //$NON-NLS-1$
		} catch (WebServiceException ce) {
			LOGGER.warning(Messages.getString("TRUSTSERVER_UNABLE_TO_CONNECT_WITH_SERVER", ce.getMessage())); //$NON-NLS-1$
			LOGGER.log(Level.FINE, Messages.getString("TRUSTSERVER_UNABLE_TO_CONNECT_WITH_SERVER", ce.getMessage()), ce); //$NON-NLS-1$
			
		} finally {
			if (get != null) {
				MessageMetaData md = get.getMessageMetaData();
				if (md != null) {
					X509Certificate certificate = md.getSignatureCertificate();
					if (certificate != null) {
						addCertificate(url.getHost() + " (" + certificate.getSubjectDN().getName() + ") (" + Messages.getString("TRUSTSERVER_SIGNATURE") + ") ", certificate); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$		
					}
				}
			}
		}
	}

	/**
	 * Adds the all the certificates in the server's certificate chain to the local trust store.
	 * @param urlEndPoint Url to connect with in order to retrieve the certificate chain.
	 * @param force If the certificate should be added even if it's not a CA certificate.
	 * @return <code>true</code> if a least one certificate is added.
	 * @throws MalformedURLException If the current url is incorrecto (impossible at this point) 
	 * @throws KeyStoreException If the method is unable to add the retrieved certificate.
	 * @throws NoSuchAlgorithmException If the system is unable to deal with TSL protocol (Is this possible by the way?)
	 * @throws KeyManagementException If it is not possible to initialize the SSL context with the local trust store.
	 */
	private static boolean addServerCerts(final String urlEndPoint, final boolean force) throws MalformedURLException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

		boolean certsAdded = false;
		
		URL url = new URL(urlEndPoint);
		String host = url.getHost();
		int port = url.getPort();
		if (port == -1) {
			port = DEFAULT_HTTPS_PORT;
		}

		ProxyTrustManager tm = new ProxyTrustManager();
		SSLContext context = SSLContext.getInstance(TSL_PROTOCOL);
		context.init(null, new TrustManager[] { tm }, null);
		SSLSocketFactory factory = context.getSocketFactory();

		LOGGER.info(Messages.getString("TRUSTSERVER_OPENING_CONNECTION", urlEndPoint)); //$NON-NLS-1$

		try (SSLSocket socket = (SSLSocket) factory.createSocket(host, port)) {
			socket.setSoTimeout(SO_TIMEOUT);
			socket.startHandshake();
		} catch (UnknownHostException uhe) {
			LOGGER.severe((Messages.getString("TRUSTSERVER_UNKNOW_HOST", urlEndPoint))); //$NON-NLS-1$
		} catch (ConnectException ce) {
			LOGGER.severe((Messages.getString("TRUSTSERVER_CANNOT_CONNECT", urlEndPoint))); //$NON-NLS-1$
		} catch (IOException ioe) {
			LOGGER.log(Level.FINE, Messages.getString("TRUSTSERVER_CANNOT_CONNECT", urlEndPoint), ioe); //$NON-NLS-1$
		} finally {

			X509Certificate[] chain = tm.getServerCertificateChain();

			if (chain != null && chain.length > 0) {
				
				for (X509Certificate certificate : chain) {
					String subjectDN = certificate.getSubjectDN().getName();
					String issuer = certificate.getIssuerDN().getName();
   
					if (force || issuer.equals(subjectDN)) {
						if (addCertificate(host + " (" + subjectDN + ") ", certificate)) { //$NON-NLS-1$ //$NON-NLS-2$
							certsAdded = true;
						}						
					} else {
						LOGGER.info(Messages.getString("TRUSTSERVER_SKIPPING_NO_ROOT_CERTIFICATE", subjectDN)); //$NON-NLS-1$
					}
				}
			}
		}
		
		return certsAdded;
	}

	/**
	 * Adds a certificate into the local trust store. Note that only valid certificates are added.
	 * @param alias Alias Alias used to store the certificate.
	 * @param certificate Certificate to be added
	 * @return <code>true</code> if the certificate is added to the trust store. <code>false</code> otherwise.
	 * @throws KeyStoreException If the method cannot modify the local trust store.
	 */
	private static boolean addCertificate(String alias, X509Certificate certificate) throws KeyStoreException {
		boolean certAdded = false;
		String subjectDN = certificate.getSubjectDN().getName();
		try {
			X509Util.checkCertificate(certificate);
			LOGGER.info(Messages.getString("TRUSTSERVER_SKIPPING_CERTIFICATE", subjectDN)); //$NON-NLS-1$
		} catch (CertificateExpiredException e) {
			LOGGER.info(Messages.getString("TRUSTSERVER_SKIPPING_EXPIRED", subjectDN)); //$NON-NLS-1$
		} catch (CertificateNotYetValidException e) {
			LOGGER.info(Messages.getString("TRUSTSERVER_SKIPPING_NOT_YET_VALID", subjectDN)); //$NON-NLS-1$
		} catch (CertificateException e1) {
			if (addedCertificates.contains(certificate)) {
				LOGGER.info(Messages.getString("TRUSTSERVER_SKIPPING_ALREADY_ADDED", subjectDN)); //$NON-NLS-1$
			} else {
				LOGGER.info(Messages.getString("TRUSTSERVER_ADDING_CERTIFICATE", subjectDN)); //$NON-NLS-1$
				addedCertificates.add(certificate);
				certAdded = true;
				localTrustStore.setCertificateEntry(alias, certificate);
			}
		}
		
		return certAdded;
	}

	/**
	 * Implements a proxy trust manager that will store the server's certificate chain.
	 */
	private static class ProxyTrustManager implements X509TrustManager {

		/** Default trust manager (real trust manager) */
		private final X509TrustManager defaultTrustManager;

		/** Server certificates chain. */
		private X509Certificate[] serverCertificateChain;

		/**
		 * Creates a new ProxyTrustManager using the given keystore as trusted server's CAs.
		 * @param ks Trust store with all the trusted server's CAs.
		 * @throws NoSuchAlgorithmException If it's not possible to get a TrustManagerFactory instance for the default
		 * algorithm.
		 * @throws KeyStoreException if it's not possible to inicialize the trust manager with the given key store.
		 */
		public ProxyTrustManager() throws NoSuchAlgorithmException, KeyStoreException {

			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(localTrustStore);
			defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
		}

		/**
		 * Returns the retrieved server's certificate chain.
		 * @return An array with the retrieved certificate chain.
		 */
		public X509Certificate[] getServerCertificateChain() {
			return serverCertificateChain;
		}

		/**
		 * Returns the accepted issuers.
		 */
		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return defaultTrustManager.getAcceptedIssuers();
		}

		/**
		 * Not supported. There is no need to implement this method, but it's necessary according to the
		 * X509TrustManager interface.
		 * @throws UnsupportedOperationException
		 */
		@Override
		public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
			throw new UnsupportedOperationException("Not supported"); //$NON-NLS-1$
		}

		/**
		 * Stores the server's certificate chain. This method nevers returns CertificateException: All connections are
		 * trusted.
		 * @param chain Server's certificate chain
		 * @param authType Authentification type.
		 * @throws CertificateException Never, all connections are trusted.
		 */
		@Override
		public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
			serverCertificateChain = chain;
		}
	}

}

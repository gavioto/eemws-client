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

package es.ree.eemws.kit.folders;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.client.common.ClientException;
import es.ree.eemws.client.get.GetMessage;
import es.ree.eemws.client.list.ListMessages;
import es.ree.eemws.client.list.MessageListEntry;
import es.ree.eemws.core.utils.file.FileUtil;

/**
 * Execute a list + get loop to retrieve messages.
 * 
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 29/05/2014
 * 
 */
public final class OutputTask implements Runnable {

	/** Filename extension. */
	private static final String XML_FILE_EXTENSION = "xml"; //$NON-NLS-1$

	/** Output folder. */
	private String outputFolder;

	/** Code for the last listed message. */
	private long lastListCode;

	/** Object which locks message for group working. */
	private LockHandler lh;

	/** List messages. */
	private ListMessages list;

	/** Retrieve messages. */
	private GetMessage get;

	/** List of message types to retrieve. */
	private List<String> typesToRetrieveList = null;

	/** Log system. */
	private static final Logger LOGGER = Logger.getLogger(OutputTask.class.getName());

	/** Temporary file prefix. */
	private static final String TMP_PREFIX = "_tmp_out_"; //$NON-NLS-1$

	/**
	 * Constructor. Initializes parameters for detection thread.
	 * @param lockHandler Lock Manager.
	 * @param config Module configuration.
	 */
	public OutputTask(final LockHandler lockHandler, final Configuration config) {

		outputFolder = config.getOutputFolder();
		URL endPoint = config.getUrlEndPoint();
		typesToRetrieveList = config.getMessagesTypeList();
		lastListCode = 0;
		lh = lockHandler;

		list = new ListMessages();
		list.setEndPoint(endPoint);

		get = new GetMessage();
		get.setEndPoint(endPoint);

		
		StringBuilder msg = new StringBuilder();
		msg.append("\n").append(Messages.getString("MF_CONFIG_OUTPUT_FOLDER", outputFolder)); //$NON-NLS-1$ //$NON-NLS-2$
		msg.append("\n").append(Messages.getString("MF_CONFIG_OUTPUT_URL_O", endPoint.toString())); //$NON-NLS-1$ //$NON-NLS-2$
		msg.append("\n").append(Messages.getString("MF_CONFIG_DELAY_TIME_O", config.getSleepTimeOutput()));  //$NON-NLS-1$//$NON-NLS-2$
		
		LOGGER.info(msg.toString());
	}

	/**
	 * Retrieves and stores a message.
	 * @param mle List element retrieved in detection process.
	 */
	private void retrieveAndStore(final MessageListEntry mle) {

		String fileName = getMessageFileName(mle);

		boolean lockFile = lh.tryLock(fileName);
		long code = mle.getCode().longValue();

		if (lockFile && !FileUtil.exists(outputFolder + File.separator + fileName)) {

			StringBuilder messageIdVersionCode = new StringBuilder();
			messageIdVersionCode.append(mle.getMessageIdentification());
			
			
			try {
				if (mle.getVersion() == null) {
					LOGGER.log(Level.SEVERE, Messages.getString("MF_RETRIEVING_MESSAGE_WO_VERSION", String.valueOf(code), mle.getMessageIdentification())); //$NON-NLS-1$
				} else {
					LOGGER.log(Level.SEVERE, Messages.getString("MF_RETRIEVING_MESSAGE", String.valueOf(code), mle.getMessageIdentification(), mle.getVersion())); //$NON-NLS-1$
				}
				
				String response = get.get(code);
				
				if (mle.getVersion() == null) {
					LOGGER.log(Level.SEVERE, Messages.getString("MF_RETRIEVED_MESSAGE_WO_VERSION", String.valueOf(code), mle.getMessageIdentification())); //$NON-NLS-1$
				} else {
					LOGGER.log(Level.SEVERE, Messages.getString("MF_RETRIEVED_MESSAGE", String.valueOf(code), mle.getMessageIdentification(), mle.getVersion())); //$NON-NLS-1$
				}
				
				File tmpFile;
				tmpFile = File.createTempFile(TMP_PREFIX, null, new File(outputFolder));
				FileUtil.write(tmpFile.getAbsolutePath(), response);
				tmpFile.renameTo(new File(outputFolder + File.separator + fileName));

			} catch (ClientException e) {
				if (mle.getVersion() == null) {
					LOGGER.log(Level.SEVERE, Messages.getString("MF_UNABLE_TO_GET_WO_VERSION", String.valueOf(code), mle.getMessageIdentification()), e); //$NON-NLS-1$
				} else {
					LOGGER.log(Level.SEVERE, Messages.getString("MF_UNABLE_TO_GET", String.valueOf(code), mle.getMessageIdentification(), mle.getVersion()), e); //$NON-NLS-1$
				}
			} catch (IOException e) {
				if (mle.getVersion() == null) {
					LOGGER.log(Level.SEVERE, Messages.getString("MF_UNABLE_TO_SAVE_WO_VERSION", String.valueOf(code), mle.getMessageIdentification()), e); //$NON-NLS-1$
				} else {
					LOGGER.log(Level.SEVERE, Messages.getString("MF_UNABLE_SAVE_GET", String.valueOf(code), mle.getMessageIdentification(), mle.getVersion()), e); //$NON-NLS-1$
				}
			}
		}

		lh.releaseLock(fileName);
	}

	/**
	 * Creates a filename for the given message list entry.
	 * @param messageListElement A message list entry.
	 * @return File name build as <identification>.<version>.xml or <identification>.xml if the message has no version
	 * information available.
	 */
	private String getMessageFileName(final MessageListEntry messageListElement) {
		StringBuilder fileName = new StringBuilder();
		fileName.append(messageListElement.getMessageIdentification());

		if (messageListElement.getVersion() != null) {
			fileName.append('.');
			fileName.append(messageListElement.getVersion());
		}

		fileName.append(XML_FILE_EXTENSION);

		return fileName.toString();
	}

	/**
	 * Gets a message list using the last list code.
	 * @return A message list. <code>null</code> if the client cannot connect with the server.
	 */
	private List<MessageListEntry> listMessages() {
		List<MessageListEntry> messageList = null;

		try {
			messageList = list.list(lastListCode);
		} catch (ClientException ex) {
			LOGGER.log(Level.SEVERE, Messages.getString("MF_UNABLE_TO_LIST"), ex); //$NON-NLS-1$
		}

		return messageList;
	}

	/**
	 * Detection cycle.
	 */
	public void run() {
		try {
			List<MessageListEntry> messageList = listMessages();

			if (messageList != null) {
				int len = messageList.size();

				if (len > 1) {
					StatusIcon.setBusy();

					for (final MessageListEntry message : messageList) {

						if (typesToRetrieveList == null || typesToRetrieveList.contains(message.getType())) {
							retrieveAndStore(message);
						}

						/* Take the highest message code to start listing form this one. */
						int msgCode = message.getCode().intValue();
						if (msgCode > lastListCode) {
							lastListCode = msgCode;
						}
					}

					StatusIcon.setIdle();
				}
			}
		} catch (Exception ex) {

			// Defensive exception, if runnable task ends with exception won't be exectued againg!
			LOGGER.log(Level.SEVERE, Messages.getString("MF_UNEXPECTED_ERROR"), ex); //$NON-NLS-1$
		}
	}

}

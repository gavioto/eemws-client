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
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.client.common.ClientException;
import es.ree.eemws.client.put.PutMessage;
import es.ree.eemws.core.utils.file.FileUtil;

/**
 * InputTask. Checks for files in the input folder, read them and send to the server.
 * 
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 29/05/2014
 */
public final class InputTask implements Runnable {

	/** Prefix added to identify response files. */
	private static final String RESPONSE_ID_PREFIX = "ack_"; //$NON-NLS-1$

	/** Input folder. */
	private final String inputFolderPath;

	/** Processed folder. */
	private final String processedFolderPath;

	/** Response folder. */
	private final String responseFolderPath;

	/** Sending web service module. */
	private final PutMessage putMessage;

	/** Object for checking messages sent by another module. */
	private final LockHandler lh;

	/** Thread log system. */
	private static final Logger LOGGER = Logger.getLogger(InputTask.class.getName());

	/** Sufix to indicata that a input file couldn't be send. */
	private static final String UNABLE_TO_SEND = "--CANNOT-SEND"; //$NON-NLS-1$

	/** Number of millisconds between file size checks. */ 
	private static final long SLEEP_BETWEEN_READS = 500;

	/**
	 * Constructor. Initializes parameters for stopping thread.
	 * @param lockHandler Lock handler.
	 * @param config Module settings.
	 * @throws MalformedURLException If cannot obtain an URL to access attachment services.
	 */
	public InputTask(final LockHandler lockHandler, final Configuration config) throws MalformedURLException {

		inputFolderPath = config.getInputFolder();
		responseFolderPath = config.getResponseFolder();
		processedFolderPath = config.getProcessedFolder();
		String endPoint = config.getUrlEndPoint().toString();

		lh = lockHandler;

		putMessage = new PutMessage();

		putMessage.setEndPoint(endPoint);

		
		StringBuilder msg = new StringBuilder();
		msg.append("\n").append(Messages.getString("MF_CONFIG_INPUT_FOLDER", inputFolderPath));  //$NON-NLS-1$//$NON-NLS-2$
			
		if (responseFolderPath != null) {
			msg.append("\n").append(Messages.getString("MF_CONFIG_ACK_FOLDER", responseFolderPath));  //$NON-NLS-1$//$NON-NLS-2$
		}
			
		if (processedFolderPath != null) {
			msg.append("\n").append(Messages.getString("MF_CONFIG_PROCESSED_FOLDER", processedFolderPath));  //$NON-NLS-1$//$NON-NLS-2$
		}

		msg.append("\n").append(Messages.getString("MF_CONFIG_DELAY_TIME_I", config.getSleepTimeInput()));  //$NON-NLS-1$//$NON-NLS-2$
		msg.append("\n").append(Messages.getString("MF_CONFIG_URL_I", endPoint.toString()));  //$NON-NLS-1$//$NON-NLS-2$
			
		LOGGER.info(msg.toString());
	}

	/**
	 * Check whether the current file is complete. Checks file size, waits for a while and checks
	 * its size again. If the size remains the same, the file is complete.
	 * @param filePath Absolute path to to file.
	 * @return <code>true</code> When complete, <code>false</code> otherwise.
	 */
	private boolean isComplete(final String filePath) {
		File file = new File(filePath);
		long initialSize = file.length();
		long finalSize = initialSize;
		boolean complete = false;

		if (initialSize > 0) {
			try {
				Thread.sleep(SLEEP_BETWEEN_READS);
			} catch (InterruptedException e) {
				LOGGER.finer("Wait interrupted"); // Don't mind! //$NON-NLS-1$
			}
			finalSize = file.length();

			complete = (finalSize == initialSize);
		}

		return complete;
	}

	/**
	 * Detect files in input folder. If files are processable (name ends with *) are added into the processable file
	 * list, otherwise are added into a non-processable files list. A file is considered as non-processable if is still
	 * being written by FTP system (has not an 'end-of-file' yet).
	 */
	@Override
	public void run() {
		try {
			File f = new File(inputFolderPath);
			File[] files = f.listFiles();

			if (files != null) {
				String filePath;
				String fileName;
				boolean lockFile;

				StatusIcon.setBusy();

				for (File file : files) {
					filePath = file.getAbsolutePath();
					fileName = file.getName();
					if (!fileName.endsWith(UNABLE_TO_SEND)) {
						lockFile = lh.tryLock(fileName);

						if (lockFile) {
							process(filePath, fileName);
						}

						lh.releaseLock(fileName);
					}
				}

				StatusIcon.setIdle();
			}
		} catch (Exception ex) {
			
			// Defensive exception, if runnable task ends with exception won't be exectued againg!
			LOGGER.log(Level.SEVERE, Messages.getString("MF_UNEXPECTED_ERROR"), ex); //$NON-NLS-1$
		}
	} 

	/**
	 * Process file with name and contents passed as arguments.
	 * @param fullFileName Absolute path to file.
	 * @param fileName Name of file.
	 */
	private void process(final String fullFileName, final String fileName) {

		String execContext = ""; //$NON-NLS-1$

		if (isComplete(fullFileName)) {

			try {
				
				LOGGER.info(Messages.getString("MF_SENDING_MESSAGE", fileName)); //$NON-NLS-1$
				String response = putMessage.put(FileUtil.readUTF8(fullFileName));
				LOGGER.info(Messages.getString("MF_SENT_MESSAGE", fileName)); //$NON-NLS-1$
				
				/* Incoming message is saved in Processed folder. */
				String processedFilePath = processedFolderPath + File.separator + fileName;
				execContext = Messages.getString("MF_SAVING_PROCESS_FOLDER", fullFileName, processedFilePath); //$NON-NLS-1$
				if (processedFolderPath != null) {
					FileUtil.writeUTF8(processedFilePath, FileUtil.readUTF8(fullFileName));
				}

				/* Once processed, the original file is deleted. */
				execContext = Messages.getString("MF_UNABLE_TO_DELETE_INPUT_FILE", fullFileName); //$NON-NLS-1$
				File f = new File(fullFileName);

				if (!f.delete()) {
					LOGGER.warning(execContext);
				}

				/* Response is saved in response folder. */
				String ackFilePath = responseFolderPath + File.separator + RESPONSE_ID_PREFIX + fileName;
				execContext = Messages.getString("MF_SAVING_ACK_FOLDER", fullFileName, ackFilePath); //$NON-NLS-1$
				if (responseFolderPath != null) {
					FileUtil.writeUTF8(ackFilePath, response);
				}

			} catch (ClientException ex) {
				File forigen = new File(fullFileName);
				File fdestino = new File(fullFileName + UNABLE_TO_SEND);
				boolean renamed = forigen.renameTo(fdestino);

				if (renamed) {
					LOGGER.severe(Messages.getString("MF_UNABLE_TO_SEND", fullFileName, fdestino.getAbsolutePath(), ex.getMessage())); //$NON-NLS-1$
				} else {
					LOGGER.severe(Messages.getString("MF_UNABLE_TO_SEND_NO_RENAME", fullFileName, ex.getMessage())); //$NON-NLS-1$
				}
			} catch (IOException ex) {
				LOGGER.log(Level.SEVERE, execContext, ex);
			}
		} 
	}
}

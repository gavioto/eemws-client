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
package es.ree.eemws.kit.cmd.get;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.ree.eemws.client.common.ClientException;
import es.ree.eemws.client.get.GetMessage;
import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.core.utils.file.FileUtil;
import es.ree.eemws.kit.cmd.ParentMain;
import es.ree.eemws.kit.common.Messages;

/**
 * Main class to get messages.
 * 
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class Main extends ParentMain {

	/** Name of the command. */
	private static final String COMMAND_NAME = "get"; //$NON-NLS-1$

	/** Log messages. */
	private static final Logger LOGGER = Logger.getLogger(COMMAND_NAME);

	/** Sets text for parameter <code>code</code>. */
	private static final String PARAMETER_CODE = Messages.getString("PARAMETER_CODE"); //$NON-NLS-1$

	/** Sets text for parameter <code>queue</code>. */
	private static final String GET_PARAMETER_QUEUE = Messages.getString("GET_PARAMETER_QUEUE"); //$NON-NLS-1$

	/** Sets text for parameter <code>msgId</code>. */
	private static final String PARAMETER_MSG_ID = Messages.getString("PARAMETER_MSG_ID"); //$NON-NLS-1$

	/** Sets text for parameter <code>msgVer</code>. */
	private static final String GET_PARAMETER_MSG_VER = Messages.getString("GET_PARAMETER_MSG_VER"); //$NON-NLS-1$

	/** Sets text for parameter <code>url</code>. */
	private static final String PARAMETER_URL = Messages.getString("PARAMETER_URL"); //$NON-NLS-1$

	/** Sets text for parameter <code>msgVer</code>. */
	private static final String PARAMETER_OUT_FILE = Messages.getString("PARAMETER_OUT_FILE"); //$NON-NLS-1$
	
	/**
	 * Main. Execute the get command.
	 * @param args command line arguments. 
	 */
	public static void main(final String[] args) {

		String urlEndPoint = ""; //$NON-NLS-1$
		String outputFile = ""; //$NON-NLS-1$
		
		try {
			Long lCode = null;
			Integer iMessageVersion = null;

			List<String> arguments = new ArrayList<>(Arrays.asList(args));
			
			String code = readParameter(arguments, PARAMETER_CODE);
			String messageId = readParameter(arguments, PARAMETER_MSG_ID);
			String messageVersion = readParameter(arguments, GET_PARAMETER_MSG_VER);
			String queue = readParameter(arguments, GET_PARAMETER_QUEUE);
			outputFile = readParameter(arguments, PARAMETER_OUT_FILE);
			urlEndPoint = readParameter(arguments, PARAMETER_URL);

			if (!arguments.isEmpty()) {
				throw new IllegalArgumentException(Messages.getString("UNKNOWN_PARAMETERS", arguments.toString())); //$NON-NLS-1$
			}
			
			if (code != null) {

				if (queue != null || messageId != null || messageVersion != null) {

					throw new IllegalArgumentException(Messages.getString("GET_INCORRECT_PARAMETERS_2", PARAMETER_CODE, GET_PARAMETER_QUEUE, PARAMETER_MSG_ID, GET_PARAMETER_MSG_VER)); //$NON-NLS-1$
				}

				try {
					lCode = Long.valueOf(code);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(Messages.getString("INCORRECT_CODE", code)); //$NON-NLS-1$
				}

			} else if (queue != null) {
				
				if (messageId != null || messageVersion != null) {

					throw new IllegalArgumentException(Messages.getString("GET_INCORRECT_PARAMETERS_2", PARAMETER_CODE, GET_PARAMETER_QUEUE, PARAMETER_MSG_ID, GET_PARAMETER_MSG_VER)); //$NON-NLS-1$
				}

			} else {

				if (messageId == null || messageVersion == null) {

					throw new IllegalArgumentException(Messages.getString("GET_INCORRECT_PARAMETERS_4", PARAMETER_CODE, GET_PARAMETER_QUEUE, PARAMETER_MSG_ID, GET_PARAMETER_MSG_VER)); //$NON-NLS-1$
				}

				try {
					iMessageVersion = Integer.valueOf(messageVersion);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(Messages.getString("GET_INCORRECT_PARAMETERS_5", messageVersion)); //$NON-NLS-1$
				}
			}

			if (outputFile != null) {
				File f = new File(outputFile);
				if (f.isDirectory()) {
					throw new IllegalArgumentException(Messages.getString("UNABLE_TO_WRITE", outputFile)); //$NON-NLS-1$
				}
			}
			
			urlEndPoint = setConfig(urlEndPoint);

			GetMessage get = new GetMessage();
			get.setEndPoint(urlEndPoint);

			long init = System.currentTimeMillis();
			 
			String response = null;
			
			if (lCode != null) {

				response = get.get(lCode);

			} else if (queue != null) {

				response = get.get(queue);

			} else {

				response = get.get(messageId, iMessageVersion);
			}
			
			if (response.length() > 0) {

				if (outputFile == null) {
					LOGGER.info(response);

					/* Don't output performance values here, the user could pipe the ouput to other process. */

				} else {

					FileUtil.writeUTF8(outputFile, response);
					long end = System.currentTimeMillis();
					LOGGER.info(Messages.getString("EXECUTION_TIME", getPerformance(init, end))); //$NON-NLS-1$
				}

			} else {

				LOGGER.info(Messages.getString("GET_NO_MESSAGE")); //$NON-NLS-1$
			}

		} catch (ClientException e) {

			LOGGER.severe(e.getMessage());
			
		} catch (MalformedURLException e) {
			
			LOGGER.severe(Messages.getString("INVALID_URL", urlEndPoint)); //$NON-NLS-1$

		} catch (ConfigException e) {
			
			LOGGER.severe(Messages.getString("INVALID_CONFIGURATION", e.getMessage())); //$NON-NLS-1$
            
        	/* Shows stack trace only for debug. Don't bother the user with this details. */
			LOGGER.log(Level.FINE, Messages.getString("INVALID_CONFIGURATION", e.getMessage()), e); //$NON-NLS-1$
			
		} catch (IllegalArgumentException e) {

			LOGGER.info(e.getMessage());
			LOGGER.info(Messages.getString("GET_USAGE", PARAMETER_MSG_ID, GET_PARAMETER_MSG_VER, PARAMETER_CODE, GET_PARAMETER_QUEUE, PARAMETER_OUT_FILE, PARAMETER_URL, new Date())); //$NON-NLS-1$
		
		} catch (IOException e) {
			
			LOGGER.severe(Messages.getString("UNABLE_TO_WRITE", outputFile)); //$NON-NLS-1$
		}

	}
}

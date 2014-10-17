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
package es.ree.eemws.kit.cmd.query;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.ree.eemws.client.common.ClientException;
import es.ree.eemws.client.querydata.QueryData;
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
	private static final String COMMAND_NAME = "query"; //$NON-NLS-1$

	/** Log messages. */
	private static final Logger LOGGER = Logger.getLogger(COMMAND_NAME);

	/** Sets text for parameter <code>startTime</code>. */
	private static final String PARAMETER_START_TIME = Messages.getString("PARAMETER_START_TIME"); //$NON-NLS-1$

	/** Sets text for parameter <code>endTime</code>. */
	private static final String PARAMETER_END_TIME = Messages.getString("PARAMETER_END_TIME"); //$NON-NLS-1$

	/** Sets text for parameter <code>url</code>. */
	private static final String PARAMETER_URL = Messages.getString("PARAMETER_URL"); //$NON-NLS-1$

	/** Sets text for parameter <code>out</code>. */
	private static final String PARAMETER_OUT_FILE = Messages.getString("PARAMETER_OUT_FILE"); //$NON-NLS-1$

	/** Sets text for parameter <code>id</code>. */
	private static final String QUERY_PARAMETER_ID = Messages.getString("QUERY_PARAMETER_ID"); //$NON-NLS-1$

	/** Token that specifies that the value is a parameter name, not a value. */
	private static final String PARAMETER_PREFIX = "-"; //$NON-NLS-1$
	
	/**
	 * Main. Execute the query action.
	 * @param args command line arguments.
	 */
	public static void main(final String[] args) {

		String outputFile = null;
		String urlEndPoint = null;

		try {

			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN);

			List<String> arguments = new ArrayList<>(Arrays.asList(args));

			
			if (arguments.isEmpty()) {
				throw new IllegalArgumentException(Messages.getString("INCORRECT_PARAMETERS_1")); //$NON-NLS-1$
			}
						
			String startTime = readParameter(arguments, PARAMETER_START_TIME);
			String endTime = readParameter(arguments, PARAMETER_END_TIME);
			String dataType = readParameter(arguments, QUERY_PARAMETER_ID);
			urlEndPoint = readParameter(arguments, PARAMETER_URL);
			outputFile = readParameter(arguments, PARAMETER_OUT_FILE);
		
			if (dataType == null) {
				throw new IllegalArgumentException(Messages.getString("QUERY_INCORRECT_QUERY_ID", QUERY_PARAMETER_ID)); //$NON-NLS-1$
			}
			
			HashMap<String, String> others = new HashMap<>();
			
			int len = arguments.size();
			for (int cont = 0; cont < len; cont++) {
				
				String key = arguments.get(cont);
				if (key.startsWith(PARAMETER_PREFIX)) {
					key = key.substring(1); // remove prefix, keep just the name
					if (key.length() == 0) { 
						throw new IllegalArgumentException(Messages.getString("QUERY_INCORRECT_PARAMETER_ID")); //$NON-NLS-1$
					}
					
					if ((cont + 1) < len) {
						String value = arguments.get(cont + 1);

						if (value.startsWith(PARAMETER_PREFIX)) { // Next value is other parameter not a value
							others.put(key, null);
						} else {
							others.put(key, value);
							cont++;
						}
					} else { // last parameter, with no value
						others.put(key, null);
					}

				} else {
					throw new IllegalArgumentException(Messages.getString("QUERY_INCORRECT_PARAMETER_LIST", key)); //$NON-NLS-1$
				}
			}
	 
			Date dateStartTime = null;
			if (startTime != null) {
				try {
					dateStartTime = sdf.parse(startTime);
				} catch (ParseException e) {
					throw new IllegalArgumentException(Messages.getString("INCORRECT_DATE_FORMAT", PARAMETER_START_TIME, startTime, DATE_FORMAT_PATTERN)); //$NON-NLS-1$
				}
			}

			Date dateEndTime = null;
			if (endTime != null) {
				try {
					dateEndTime = sdf.parse(endTime);
				} catch (ParseException e) {
					throw new IllegalArgumentException(Messages.getString("INCORRECT_DATE_FORMAT", PARAMETER_END_TIME, endTime, DATE_FORMAT_PATTERN)); //$NON-NLS-1$
				}
			}

			urlEndPoint = setConfig(urlEndPoint);

			QueryData query = new QueryData();
			query.setEndPoint(urlEndPoint);

			long init = System.currentTimeMillis();

			
			String response = query.query(dataType, dateStartTime, dateEndTime, others);

			if (outputFile == null) {
				LOGGER.info(response);

				/* Don't output performance values here, the user could pipe the ouput to other process. */

			} else {

				FileUtil.writeUTF8(outputFile, response);
				long end = System.currentTimeMillis();
				LOGGER.info(Messages.getString("EXECUTION_TIME", getPerformance(init, end))); //$NON-NLS-1$
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
			LOGGER.info(Messages.getString("QUERY_USAGE", QUERY_PARAMETER_ID, PARAMETER_START_TIME, PARAMETER_END_TIME, PARAMETER_OUT_FILE, PARAMETER_URL, new Date())); //$NON-NLS-1$

		} catch (IOException e) {

			LOGGER.severe(Messages.getString("UNABLE_TO_WRITE", outputFile)); //$NON-NLS-1$

		}

	}
}

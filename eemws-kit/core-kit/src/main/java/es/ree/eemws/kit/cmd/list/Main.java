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
package es.ree.eemws.kit.cmd.list;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.ree.eemws.client.exception.ClientException;
import es.ree.eemws.client.listmessages.ListMessages;
import es.ree.eemws.client.listmessages.MessageListEntry;
import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.kit.cmd.ParentMain;
import es.ree.eemws.kit.common.Messages;

/**
 * Main class to list messages.
 * 
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */

public final class Main extends ParentMain {

	/** Name of the command. */
	private static final String COMMAND_NAME = "list"; //$NON-NLS-1$

	/** Log messages. */
	private static final Logger LOGGER = Logger.getLogger(COMMAND_NAME);
	
	/** Sets text for parameter <code>code</code>. */
	private static final String PARAMETER_CODE = Messages.getString("PARAMETER_CODE"); //$NON-NLS-1$
	
	/** Sets text for parameter <code>startTime</code>. */
	private static final String PARAMETER_START_TIME = Messages.getString("PARAMETER_START_TIME"); //$NON-NLS-1$
	
	/** Sets text for parameter <code>endTime</code>. */
	private static final String PARAMETER_END_TIME = Messages.getString("PARAMETER_END_TIME"); //$NON-NLS-1$
	
	/** Sets text for parameter <code>intervalTime</code>. */
	private static final String LIST_PARAMETER_INTERVAL_TYPE = Messages.getString("LIST_PARAMETER_INTERVAL_TYPE"); //$NON-NLS-1$
	
	/** Sets text for parameter <code>msgId</code>. */
	private static final String PARAMETER_MSG_ID = Messages.getString("PARAMETER_MSG_ID"); //$NON-NLS-1$
	
	/** Sets text for parameter <code>msgType</code>. */
	private static final String LIST_PARAMETER_MSG_TYPE = Messages.getString("LIST_PARAMETER_MSG_TYPE"); //$NON-NLS-1$
	
	/** Sets text for parameter <code>owner</code>. */
	private static final String LIST_PARAMETER_MSG_OWNER = Messages.getString("LIST_PARAMETER_MSG_OWNER"); //$NON-NLS-1$
	
	/** Sets text for parameter <code>url</code>. */
	private static final String PARAMETER_URL = Messages.getString("PARAMETER_URL"); //$NON-NLS-1$

    /** Size of the field Code. */
    private static final int CODE_SIZE = 20;

    /** Size of the field  MessageIdentification.Version. */
    private static final int MESSAGE_IDENTIFICATION_VERSION_SIZE = 40;

    /** Size of the field Status. */
    private static final int STATUS_SIZE = 6;

    /** Size of the field Type. */
    private static final int TYPE_SIZE = 50;

    /** Size of the field Owner. */
    private static final int OWNER_SIZE = 20;

    /** Empty string for string formart. */
    private static final String EMPTY_STRING;

	/** Date format with minutes. */
    private static final String DATE_FORMAT_MINUTES = "dd-MM-yyyy HH:mm"; //$NON-NLS-1$
	
    /** Date format with seconds. */
    private static final String DATE_FORMAT_SECONDS = "dd-MM-yyyy HH:mm:ss"; //$NON-NLS-1$
    
    /* Inicialices empty string. */
    static {
    	
    	int maxSize = Math.max(Math.max(Math.max(Math.max(CODE_SIZE,  MESSAGE_IDENTIFICATION_VERSION_SIZE), STATUS_SIZE), TYPE_SIZE), OWNER_SIZE);
    	
    	StringBuilder sb = new StringBuilder(maxSize);
    	for (int cont = 0; cont < maxSize; cont++) {
    		sb.append(' ');
    	}
    	EMPTY_STRING = sb.toString();
    }
    
    
	/**
	 * Main. Execute the list command.
	 * @param args command line arguments.
	 */
	public static void main(final String[] args) {

		String urlEndPoint = ""; //$NON-NLS-1$
		
		try {

			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN);
			Date dateStartTime = null;
			Date dateEndTime = null;
			String intervalType = null;
			Long lCode = null;

			List<String> arguments = new ArrayList<>(Arrays.asList(args));
			
			String startTime = readParameter(arguments, PARAMETER_START_TIME);
			String endTime = readParameter(arguments, PARAMETER_END_TIME);
			intervalType = readParameter(arguments, LIST_PARAMETER_INTERVAL_TYPE);
			String code = readParameter(arguments, PARAMETER_CODE);
			String msgId = readParameter(arguments, PARAMETER_MSG_ID);
			String msgType = readParameter(arguments, LIST_PARAMETER_MSG_TYPE);
			String owner = readParameter(arguments, LIST_PARAMETER_MSG_OWNER);
			urlEndPoint = readParameter(arguments, PARAMETER_URL);

			if (!arguments.isEmpty()) {
				throw new IllegalArgumentException(Messages.getString("UNKNOWN_PARAMETERS", arguments.toString())); //$NON-NLS-1$
			}
			
			if (code == null) {

				String sSysdate = sdf.format(Calendar.getInstance().getTime());
				if (startTime == null) {
					startTime = sSysdate;
				}

				if (endTime == null) {
					endTime = sSysdate;
				}

				try {
					dateStartTime = sdf.parse(startTime);
					dateEndTime = sdf.parse(endTime);

				} catch (ParseException e) {

					throw new IllegalArgumentException(Messages.getString("LIST_INCORRECT_PARAMETERS_4", startTime, endTime, DATE_FORMAT_PATTERN)); //$NON-NLS-1$
				}

				if (dateStartTime.after(dateEndTime)) {

					throw new IllegalArgumentException(Messages.getString("LIST_INCORRECT_PARAMETERS_3", startTime, endTime)); //$NON-NLS-1$
				}

			} else {

				if (startTime != null || endTime != null || intervalType != null) {

					throw new IllegalArgumentException(Messages.getString("LIST_INCORRECT_PARAMETERS_1", PARAMETER_CODE, PARAMETER_START_TIME, PARAMETER_END_TIME, LIST_PARAMETER_INTERVAL_TYPE)); //$NON-NLS-1$
				}

				try {
					lCode = Long.valueOf(code);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(Messages.getString("LIST_INCORRECT_PARAMETERS_2", code)); //$NON-NLS-1$
				}
			}

			urlEndPoint = setConfig(urlEndPoint);

			ListMessages list = new ListMessages();
			list.setEndPoint(urlEndPoint);

			long init = System.currentTimeMillis();

			List<MessageListEntry> response = null;
			if (lCode != null) {

				response = list.list(lCode, msgId, msgType, owner);

			} else {

				response = list.list(dateStartTime, dateEndTime, intervalType, msgId, msgType, owner);
			}

			showResults(response);

			long end = System.currentTimeMillis();
			LOGGER.info(Messages.getString("EXECUTION_TIME", getPerformance(init, end))); //$NON-NLS-1$

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
			LOGGER.info(Messages.getString("LIST_USAGE", PARAMETER_CODE, PARAMETER_START_TIME, PARAMETER_END_TIME, LIST_PARAMETER_INTERVAL_TYPE,  //$NON-NLS-1$
					PARAMETER_MSG_ID, LIST_PARAMETER_MSG_TYPE, LIST_PARAMETER_MSG_OWNER, PARAMETER_URL, new Date()));
		}
	}

	/**
	 * This method shows the results.
	 * @param response List of the message listings.
	 */
	private static void showResults(final List<MessageListEntry> response) {

		if (response != null && !response.isEmpty()) {
			
			
		    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_MINUTES);
		    
		    SimpleDateFormat sdfTimestamp = new SimpleDateFormat(DATE_FORMAT_SECONDS);
		  
			StringBuilder sb = new StringBuilder();
			sb.append(Messages.getString("LIST_OUTPUT_HEADER")); //$NON-NLS-1$

			int numMessages = 0;
			long maxCode = -1;

			for (MessageListEntry msgData : response) {

				sb.append("\n"); //$NON-NLS-1$
				sb.append(completeStringWithBlanks(msgData.getCode().toString(), CODE_SIZE));
				sb.append(" "); //$NON-NLS-1$
				sb.append(completeStringWithBlanks(msgData.getMessageIdentification() + "." + msgData.getVersion(), MESSAGE_IDENTIFICATION_VERSION_SIZE)); //$NON-NLS-1$
				sb.append(" "); //$NON-NLS-1$
				sb.append(completeStringWithBlanks(msgData.getStatus(), STATUS_SIZE));
				sb.append(" "); //$NON-NLS-1$
				sb.append(sdf.format(msgData.getApplicationStartTime().getTime()));
				sb.append(" - "); //$NON-NLS-1$
				sb.append(sdf.format(msgData.getApplicationEndTime().getTime()));
				sb.append(" "); //$NON-NLS-1$
				sb.append(sdfTimestamp.format(msgData.getServerTimestamp().getTime()));
				sb.append(" "); //$NON-NLS-1$
				sb.append(completeStringWithBlanks(msgData.getType(), TYPE_SIZE));
				sb.append(" "); //$NON-NLS-1$
				sb.append(completeStringWithBlanks(msgData.getOwner(), OWNER_SIZE));

				numMessages++;
				maxCode = Math.max(msgData.getCode().longValue(), maxCode);
			}

			sb.append("\n"); //$NON-NLS-1$
			sb.append(numMessages);
			sb.append(" "); //$NON-NLS-1$
			sb.append(Messages.getString("LIST_NUM_OF_MESSAGES")); //$NON-NLS-1$
			sb.append(" "); //$NON-NLS-1$
			sb.append(Messages.getString("LIST_MAX_CODE", String.valueOf(maxCode))); //$NON-NLS-1$
			

			LOGGER.info(sb.toString());

		} else {

			LOGGER.info(Messages.getString("LIST_NO_MESSAGES")); //$NON-NLS-1$
		}
	}
	 
    /**
     * This method completes string with blanks.
     * @param value Value of the string.
     * @param size Size of the final string.
     * @return String with blanks.
     */
    private static StringBuilder completeStringWithBlanks(final String value, final int size) {

        StringBuilder sb = new StringBuilder(value);
        sb.append(EMPTY_STRING);
        sb.setLength(size);
        
        return sb;
    }
}

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
package es.ree.eemws.kit.cmd.list;

import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.ree.eemws.client.list.ListMessages;
import es.ree.eemws.client.list.MessageListEntry;
import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.core.utils.error.EnumErrorCatalog;
import es.ree.eemws.core.utils.iec61968100.EnumFilterElement;
import es.ree.eemws.core.utils.iec61968100.EnumIntervalTimeType;
import es.ree.eemws.core.utils.operations.list.ListOperationException;
import es.ree.eemws.kit.cmd.ParentMain;
import es.ree.eemws.kit.common.Messages;

/**
 * Lists messages using the command line.
 * 
 * @author Red Eléctrica de España S.A.U.
 * @version 1.1 13/02/2016
 */

public final class Main extends ParentMain {

    /** Log messages. */
    private static final Logger LOGGER = Logger.getLogger("list"); //$NON-NLS-1$

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

    /** Format pattern for code. */
    private static final String CODE_FORMAT = "%20s"; //$NON-NLS-1$

    /** Format pattern for MessageIdentification.Version. */
    private static final String MESSAGE_IDENTIFICATION_VERSION_FORMAT = "%40s"; //$NON-NLS-1$

    /** Format pattern for Status. */
    private static final String STATUS_FORMAT = "%6s"; //$NON-NLS-1$

    /** Format pattern for Message Type. */
    private static final String MSG_TYPE_FORMAT = "%50s"; //$NON-NLS-1$

    /** Format pattern for Owner. */
    private static final String OWNER_FORMAT = "%20s"; //$NON-NLS-1$

    /** Date format with minutes. */
    private static final String DATE_FORMAT_MINUTES = "dd-MM-yyyy HH:mm"; //$NON-NLS-1$

    /** Date format with seconds. */
    private static final String DATE_FORMAT_SECONDS = "dd-MM-yyyy HH:mm:ss"; //$NON-NLS-1$

    /** Format pattern for date. */
    private static final String DATE_FORMAT = "%" + DATE_FORMAT_MINUTES.length() + "s"; //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Main. Executes the list command.
     * @param args command line arguments.
     */
    public static void main(final String[] args) {

        String urlEndPoint = ""; //$NON-NLS-1$

        try {

            /* Reads command line parameters, store its values. */
            List<String> arguments = new ArrayList<>(Arrays.asList(args));

            /* If the list has duplicates must stop the execution. */
            String dup = findDuplicates(arguments, PARAMETER_START_TIME, PARAMETER_END_TIME, LIST_PARAMETER_INTERVAL_TYPE, PARAMETER_CODE, PARAMETER_MSG_ID,
                    LIST_PARAMETER_MSG_TYPE, LIST_PARAMETER_MSG_OWNER, PARAMETER_URL);
            
            if (dup != null) {
                throw new ListOperationException(EnumErrorCatalog.ERR_LST_010, Messages.getString("PARAMETER_REPEATED", dup)); //$NON-NLS-1$
            }
            
            String startTime = readParameter(arguments, PARAMETER_START_TIME);
            String endTime = readParameter(arguments, PARAMETER_END_TIME);
            String intervalType = readParameter(arguments, LIST_PARAMETER_INTERVAL_TYPE);
            String code = readParameter(arguments, PARAMETER_CODE);
            String msgId = readParameter(arguments, PARAMETER_MSG_ID);
            String msgType = readParameter(arguments, LIST_PARAMETER_MSG_TYPE);
            String owner = readParameter(arguments, LIST_PARAMETER_MSG_OWNER);
            urlEndPoint = readParameter(arguments, PARAMETER_URL);
            
            /* If the list is not empty means that user has put at least one "unknown" or repeated parameter. Show only first. */
            if (!arguments.isEmpty()) {
                throw new ListOperationException(EnumErrorCatalog.ERR_LST_011, arguments.get(0));
            }

            /* Creates a request with all the parameters. Do not make any validation here. */
            Map<String, String> msgOptions = new HashMap<>();

            if (code != null) {
                msgOptions.put(EnumFilterElement.CODE.toString(), code);
            }

            if (startTime != null) {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN);
                DateFormat df = DateFormat.getInstance();

                Date dateStartTime = null;
                try {
                    dateStartTime = sdf.parse(startTime);
                    msgOptions.put(EnumFilterElement.START_TIME.toString(), df.format(dateStartTime));
                } catch (ParseException e) {
                    throw new ListOperationException(EnumErrorCatalog.ERR_LST_010, Messages.getString("LIST_INVALID_DATE_FORMAT", startTime, DATE_FORMAT_PATTERN)); //$NON-NLS-1$
                }

                /* By default, endTime = end of the day of startTime */
                Date dateEndTime;
                if (endTime == null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dateStartTime);
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    cal.add(Calendar.SECOND, -1);
                    dateEndTime = cal.getTime();
                } else {
                    try {
                        dateEndTime = sdf.parse(endTime);
                    } catch (ParseException e) {
                        throw new ListOperationException(EnumErrorCatalog.ERR_LST_010, Messages.getString("LIST_INVALID_DATE_FORMAT", endTime, DATE_FORMAT_PATTERN)); //$NON-NLS-1$
                    }
                }

                msgOptions.put(EnumFilterElement.END_TIME.toString(), df.format(dateEndTime));
            }

            if (intervalType != null) {
                msgOptions.put(EnumFilterElement.INTERVAL_TYPE.toString(), intervalType);
            }

            if (msgId != null) {
                msgOptions.put(EnumFilterElement.MESSAGE_IDENTIFICATION.toString(), msgId);
            }

            if (msgType != null) {
                msgOptions.put(EnumFilterElement.MESSAGE_TYPE.toString(), msgType);
            }

            if (owner != null) {
                msgOptions.put(EnumFilterElement.OWNER.toString(), owner);
            }

            /* Sets the url, if no url is provided by arguments, use the one configured. */
            urlEndPoint = setConfig(urlEndPoint);

            /* Creates and set up a get object. */
            ListMessages list = new ListMessages();
            list.setEndPoint(urlEndPoint);

            /* Send the request (list operation will validate at this point the parameters) */
            long init = System.currentTimeMillis();
            showResults(list.list(msgOptions));
            long end = System.currentTimeMillis();

            /* Writes performance values on screen. */
            LOGGER.info(Messages.getString("EXECUTION_TIME", getPerformance(init, end))); //$NON-NLS-1$

        } catch (ListOperationException e) {

            String code = e.getCode();

            if (code.equals(EnumErrorCatalog.ERR_HAND_010.getCode())) {
                
                LOGGER.log(Level.SEVERE, e.getCode() + ": " + e.getMessage() + " " + e.getCause().getMessage()); //$NON-NLS-1$ //$NON-NLS-2$

            } else {
                
                LOGGER.log(Level.SEVERE, e.getCode() + ": " + e.getMessage(), e.getCause()); //$NON-NLS-1$

                /* Bad parameters? show usage! */

                if (code.equals(EnumErrorCatalog.ERR_LST_010.getCode()) 
                        || code.equals(EnumErrorCatalog.ERR_LST_005.getCode()) 
                        || code.equals(EnumErrorCatalog.ERR_LST_011.getCode())) {

                    LOGGER.info(Messages.getString("LIST_USAGE", PARAMETER_CODE, PARAMETER_START_TIME, PARAMETER_END_TIME, LIST_PARAMETER_INTERVAL_TYPE, //$NON-NLS-1$
                            PARAMETER_MSG_ID, LIST_PARAMETER_MSG_TYPE, LIST_PARAMETER_MSG_OWNER, PARAMETER_URL, new Date(), 
                            EnumIntervalTimeType.APPLICATION.toString(), EnumIntervalTimeType.SERVER.toString()));
                }
            }
            
            /* Show full stack trace in debug. */
            LOGGER.log(Level.FINE, "", e); //$NON-NLS-1$

        } catch (MalformedURLException e) {

            LOGGER.severe(Messages.getString("INVALID_URL", urlEndPoint)); //$NON-NLS-1$

        } catch (ConfigException e) {

            LOGGER.severe(Messages.getString("INVALID_CONFIGURATION", e.getMessage())); //$NON-NLS-1$

            /* Shows stack trace only for debug. Don't bother the user with this details. */
            LOGGER.log(Level.FINE, Messages.getString("INVALID_CONFIGURATION", e.getMessage()), e); //$NON-NLS-1$

        }
    }

    /**
     * This method shows the results.
     * @param response List of the message listings.
     * @throws ClientException If the server returns an invalid message list entry.
     */
    private static void showResults(final List<MessageListEntry> response) {

        if (response != null && !response.isEmpty()) {

            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_MINUTES);

            SimpleDateFormat sdfTimestamp = new SimpleDateFormat(DATE_FORMAT_SECONDS);

            StringBuilder error = new StringBuilder();
            StringBuilder sb = new StringBuilder();
            sb.append(Messages.getString("LIST_OUTPUT_HEADER")); //$NON-NLS-1$

            int numMessages = 0;
            long maxCode = -1;

            for (MessageListEntry msgData : response) {

                int bufferPos = sb.length();

                try {

                    sb.append("\n"); //$NON-NLS-1$
                    sb.append(String.format(CODE_FORMAT, msgData.getCode().toString()));
                    sb.append(" "); //$NON-NLS-1$

                    if (msgData.getVersion() == null) { // Version is optional
                        sb.append(String.format(MESSAGE_IDENTIFICATION_VERSION_FORMAT, msgData.getMessageIdentification()));
                    } else {
                        sb.append(String.format(MESSAGE_IDENTIFICATION_VERSION_FORMAT, msgData.getMessageIdentification() + "." + msgData.getVersion())); //$NON-NLS-1$
                    }

                    sb.append(" "); //$NON-NLS-1$

                    if (msgData.getStatus() == null) { // Status is optional
                        sb.append(String.format(STATUS_FORMAT, "")); //$NON-NLS-1$ 
                    } else {
                        sb.append(String.format(STATUS_FORMAT, msgData.getStatus()));
                    }

                    sb.append(" "); //$NON-NLS-1$
                    sb.append(sdf.format(msgData.getApplicationStartTime().getTime()));
                    sb.append(" - "); //$NON-NLS-1$

                    if (msgData.getApplicationEndTime() == null) { // End date is optional
                        sb.append(String.format(DATE_FORMAT, "")); //$NON-NLS-1$
                    } else {
                        sb.append(sdf.format(msgData.getApplicationEndTime().getTime()));
                    }

                    sb.append(" "); //$NON-NLS-1$
                    sb.append(sdfTimestamp.format(msgData.getServerTimestamp().getTime()));

                    sb.append(" "); //$NON-NLS-1$
                    sb.append(String.format(MSG_TYPE_FORMAT, msgData.getType()));

                    sb.append(" "); //$NON-NLS-1$
                    sb.append(String.format(OWNER_FORMAT, msgData.getOwner()));

                    numMessages++;
                    maxCode = Math.max(msgData.getCode().longValue(), maxCode);

                } catch (NullPointerException npe) {

                    /*
                     * Don't stop the loop if the server sends one (or more) non-valid entries. Put these entries
                     * information into a special error buffer.
                     */
                    sb.delete(bufferPos, sb.length());

                    try {
                        msgData.checkMandatoryElements();
                    } catch (ListOperationException e) {
                        error.append(e.toString());
                        error.append("\n"); //$NON-NLS-1$
                    }
                }
            }

            sb.append("\n"); //$NON-NLS-1$
            sb.append(numMessages);
            sb.append(" "); //$NON-NLS-1$
            sb.append(Messages.getString("LIST_NUM_OF_MESSAGES")); //$NON-NLS-1$
            sb.append(" "); //$NON-NLS-1$
            sb.append(Messages.getString("LIST_MAX_CODE", String.valueOf(maxCode))); //$NON-NLS-1$

            LOGGER.info(sb.toString());

            if (error.length() > 0) {
                LOGGER.severe(error.toString());
            }

        } else {

            LOGGER.info(Messages.getString("LIST_NO_MESSAGES")); //$NON-NLS-1$
        }

    }

}

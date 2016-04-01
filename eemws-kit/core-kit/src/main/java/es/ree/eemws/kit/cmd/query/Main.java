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
package es.ree.eemws.kit.cmd.query;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.ree.eemws.client.querydata.QueryData;
import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.core.utils.error.EnumErrorCatalog;
import es.ree.eemws.core.utils.file.FileUtil;
import es.ree.eemws.core.utils.iec61968100.EnumFilterElement;
import es.ree.eemws.core.utils.operations.query.QueryOperationException;
import es.ree.eemws.kit.cmd.ParentMain;
import es.ree.eemws.kit.common.Messages;

/**
 * Sends a query request.
 * 
 * @author Red Eléctrica de España S.A.U.
 * @version 1.1 13/02/2016
 */
public final class Main extends ParentMain {

    /** Log messages. */
    private static final Logger LOGGER = Logger.getLogger("query"); //$NON-NLS-1$

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

        QueryData query = null;
        String outputFile = null;
        String urlEndPoint = null;
        long init = -1;

        try {

            /* Reads command line parameters, store its values. */
            List<String> arguments = new ArrayList<>(Arrays.asList(args));
            
            /* If the list has duplicates must stop the execution. */
            String dup = findDuplicates(arguments, PARAMETER_START_TIME, PARAMETER_END_TIME, QUERY_PARAMETER_ID, PARAMETER_URL, PARAMETER_OUT_FILE);
            if (dup != null) {
                throw new QueryOperationException(EnumErrorCatalog.ERR_QRY_010, Messages.getString("PARAMETER_REPEATED",  dup)); //$NON-NLS-1$
            }  

            String startTime = readParameter(arguments, PARAMETER_START_TIME);
            String endTime = readParameter(arguments, PARAMETER_END_TIME);
            String dataType = readParameter(arguments, QUERY_PARAMETER_ID);
            urlEndPoint = readParameter(arguments, PARAMETER_URL);
            outputFile = readParameter(arguments, PARAMETER_OUT_FILE);
            
            /* Creates a request with all the parameters. */
            HashMap<String, String> msgOptions = new HashMap<>();

            if (dataType != null) {
                msgOptions.put(EnumFilterElement.DATA_TYPE.toString(), dataType);
            }

            DateFormat df = DateFormat.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN);
            if (startTime != null) {
                try {
                    Date dateStartTime = sdf.parse(startTime);
                    msgOptions.put(EnumFilterElement.START_TIME.toString(), df.format(dateStartTime));
                } catch (ParseException e) {
                    throw new QueryOperationException(EnumErrorCatalog.ERR_QRY_009, PARAMETER_START_TIME, DATE_FORMAT_PATTERN);
                }
            }

            if (endTime != null) {
                try {
                    Date dateEndTime = sdf.parse(endTime);
                    msgOptions.put(EnumFilterElement.END_TIME.toString(), df.format(dateEndTime));
                } catch (ParseException e) {
                    throw new QueryOperationException(EnumErrorCatalog.ERR_QRY_009, PARAMETER_END_TIME, DATE_FORMAT_PATTERN);
                }
            }

            /* Retrieves dinamic parameters from the command line. */
            int len = arguments.size();
            for (int cont = 0; cont < len; cont++) {

                String key = arguments.get(cont);
                if (key.startsWith(PARAMETER_PREFIX)) {
                    key = key.substring(1); // remove prefix, keep just the name
                    if (key.length() == 0) {
                        throw new QueryOperationException(EnumErrorCatalog.ERR_QRY_010, Messages.getString("QUERY_INCORRECT_PARAMETER_ID")); //$NON-NLS-1$
                    }

                    if ((cont + 1) < len) {
                        String value = arguments.get(cont + 1);

                        if (value.startsWith(PARAMETER_PREFIX)) { // Next value is other parameter not a value
                            msgOptions.put(key, null);
                        } else {
                            msgOptions.put(key, value);
                            cont++;
                        }
                    } else { // last parameter, with no value
                        msgOptions.put(key, null);
                    }

                } else {
                    throw new QueryOperationException(EnumErrorCatalog.ERR_QRY_010, Messages.getString("QUERY_INCORRECT_PARAMETER_LIST", key)); //$NON-NLS-1$
                }
            }

            /* Sets the url, if no url is provided by arguments, use the one configured. */
            urlEndPoint = setConfig(urlEndPoint);

            /* Creates and set up a query object. */
            query = new QueryData();
            query.setEndPoint(urlEndPoint);

            init = System.currentTimeMillis();

            String response = query.query(msgOptions);

            writeResponse(outputFile, init, response);
           
        } catch (QueryOperationException e) {
 
            String code = e.getCode();
            
            if (code.equals(EnumErrorCatalog.ERR_HAND_010.getCode())) {
                
                LOGGER.log(Level.SEVERE, e.getCode() + ": " + e.getMessage() + " " + e.getCause().getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
                
                /* Server returns fault and user wants a response file? Write the fault as server response. */
                if (query != null && outputFile != null) {
                    String faultStr = query.getMessageMetaData().getRejectText();
                    writeResponse(outputFile, init, faultStr);
                }

            } else {

                LOGGER.log(Level.SEVERE, e.getCode() + ": " + e.getMessage(), e.getCause()); //$NON-NLS-1$

                /* Bad parameters? show usage! */
                if (code.equals(EnumErrorCatalog.ERR_QRY_009.getCode()) 
                        || code.equals(EnumErrorCatalog.ERR_QRY_001.getCode())
                        || code.equals(EnumErrorCatalog.ERR_QRY_010.getCode())) {
                    
                    LOGGER.info(Messages.getString("QUERY_USAGE", QUERY_PARAMETER_ID, PARAMETER_START_TIME,  //$NON-NLS-1$
                            PARAMETER_END_TIME, PARAMETER_OUT_FILE, PARAMETER_URL, new Date())); 
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
     * Writes server's response.
     * @param outputFile Out abosolute file path.
     * @param init Request start time (to print performance) 
     * @param response Server response.
     */
    private static void writeResponse(final String outputFile, final long init, final String response) {
        try {
            if (outputFile == null) {
                LOGGER.info(response);
            } else {
                FileUtil.writeUTF8(outputFile, response);
                LOGGER.info(Messages.getString("EXECUTION_TIME", getPerformance(init, System.currentTimeMillis()))); //$NON-NLS-1$
            }
        } catch (IOException ioe) {
            LOGGER.severe(Messages.getString("UNABLE_TO_WRITE", outputFile)); //$NON-NLS-1$
        }
    }
}

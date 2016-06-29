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
package es.ree.eemws.kit.cmd.get;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.ree.eemws.client.get.GetMessage;
import es.ree.eemws.client.get.RetrievedMessage;
import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.core.utils.error.EnumErrorCatalog;
import es.ree.eemws.core.utils.file.FileUtil;
import es.ree.eemws.core.utils.iec61968100.EnumFilterElement;
import es.ree.eemws.core.utils.operations.get.GetOperationException;
import es.ree.eemws.kit.cmd.ParentMain;
import es.ree.eemws.kit.common.Messages;

/**
 * Gets a message using the command line.
 * 
 * @author Red Eléctrica de España S.A.U.
 * @version 1.1 13/02/2016
 */
public final class Main extends ParentMain {
    
    /** Log messages. */
    private static final Logger LOGGER = Logger.getLogger("get"); //$NON-NLS-1$

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
     * Main. Executes the get command.
     * @param args command line arguments. 
     */
    public static void main(final String[] args) {

        String urlEndPoint = ""; //$NON-NLS-1$
        String outputFile = ""; //$NON-NLS-1$

        try {
            
            /* Reads command line parameters, store its values. */
            List<String> arguments = new ArrayList<>(Arrays.asList(args));
            
            /* If the list has duplicates must stop the execution. */
            String dup = findDuplicates(arguments, PARAMETER_CODE, PARAMETER_MSG_ID, GET_PARAMETER_MSG_VER, GET_PARAMETER_QUEUE, PARAMETER_OUT_FILE, PARAMETER_URL);
            if (dup != null) {
                throw new GetOperationException(EnumErrorCatalog.ERR_GET_011, Messages.getString("PARAMETER_REPEATED", dup)); //$NON-NLS-1$
            }

            String code = readParameter(arguments, PARAMETER_CODE);
            String messageId = readParameter(arguments, PARAMETER_MSG_ID);
            String messageVersion = readParameter(arguments, GET_PARAMETER_MSG_VER);
            String queue = readParameter(arguments, GET_PARAMETER_QUEUE);
            outputFile = readParameter(arguments, PARAMETER_OUT_FILE);
            urlEndPoint = readParameter(arguments, PARAMETER_URL);
            
            /* If the list is not empty means that user has put at least one "unknown" parameter. Show only first. */
            if (!arguments.isEmpty()) {
                throw new GetOperationException(EnumErrorCatalog.ERR_GET_012, arguments.get(0)); 
            }

            /* Creates a request with all the parameters. Do not make any validation here. */
            Map<String, String> msgOptions = new HashMap<>();

            if (messageId != null) {
                msgOptions.put(EnumFilterElement.MESSAGE_IDENTIFICATION.toString(), messageId);
            }

            if (messageVersion != null) {
                msgOptions.put(EnumFilterElement.MESSAGE_VERSION.toString(), messageVersion.toString());
            }

            if (code != null) {
                msgOptions.put(EnumFilterElement.CODE.toString(), code);
            }

            if (queue != null) {
                msgOptions.put(EnumFilterElement.QUEUE.toString(), queue);
            }

            /* Sets the url, if no url is provided by arguments, use the one configured. */
            urlEndPoint = setConfig(urlEndPoint);

            /* Creates and set up a get object. */
            GetMessage get = new GetMessage();
            get.setEndPoint(urlEndPoint);

            long init = System.currentTimeMillis();

            /* Send the request (get operation will validate at this point the parameters) */
            RetrievedMessage response = get.get(msgOptions);

            /*
             * Prints retrieved messages on screen if the user didn't specified an output file
             * Note that binary messages are always saved to disk: if no file name was specified the one provided by the server will be used
             * other wise, the file name specified by the user will overrride the one provided by the server. 
             */
            if (outputFile == null) {
                if (response.isBinary()) {
                    String fileName = response.getFileName();
                    FileUtil.write(fileName, response.getBinaryPayload());
                } else {
                    LOGGER.info(response.getPrettyPayload());
                }
            } else {
                if (response.isBinary()) {
                    FileUtil.write(outputFile, response.getBinaryPayload());
                } else {
                    FileUtil.writeUTF8(outputFile, response.getPrettyPayload());
                }
            }

            /* Writes performance values on screen if the user set an output file or if the retrieved message is binary. */
            if (outputFile != null || response.isBinary()) {
                LOGGER.info(Messages.getString("EXECUTION_TIME", getPerformance(init, System.currentTimeMillis()))); //$NON-NLS-1$
            }

        } catch (GetOperationException e) {
            
            String code = e.getCode();
            
            if (code.equals(EnumErrorCatalog.ERR_HAND_010.getCode())) {
                
                LOGGER.log(Level.SEVERE, e.getCode() + ": " + e.getMessage() + " " + e.getCause().getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
                
            } else {

                LOGGER.log(Level.SEVERE, e.getCode() + ": " + e.getMessage(), e.getCause()); //$NON-NLS-1$
            
                /* Bad parameters? show usage! */
                if (code.equals(EnumErrorCatalog.ERR_GET_004.getCode()) || code.equals(EnumErrorCatalog.ERR_GET_012.getCode())) {
                    LOGGER.info(Messages.getString("GET_USAGE", PARAMETER_MSG_ID, GET_PARAMETER_MSG_VER, PARAMETER_CODE, //$NON-NLS-1$
                            GET_PARAMETER_QUEUE, PARAMETER_OUT_FILE, PARAMETER_URL, new Date()));
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

        } catch (IOException e) {

            LOGGER.severe(Messages.getString("UNABLE_TO_WRITE", outputFile)); //$NON-NLS-1$
            
        }
    }
}

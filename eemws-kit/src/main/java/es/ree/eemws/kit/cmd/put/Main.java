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
package es.ree.eemws.kit.cmd.put;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.ree.eemws.client.put.PutMessage;
import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.core.utils.error.EnumErrorCatalog;
import es.ree.eemws.core.utils.file.FileUtil;
import es.ree.eemws.core.utils.iec61968100.EnumMessageFormat;
import es.ree.eemws.core.utils.operations.put.PutOperationException;
import es.ree.eemws.kit.cmd.ParentMain;
import es.ree.eemws.kit.common.Messages;

/**
 * Puts a message using the command line.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.1 13/02/2016
 */
public final class Main extends ParentMain {

    /** Log messages. */
    private static final Logger LOGGER = Logger.getLogger("put"); //$NON-NLS-1$

    /** Sets text for parameter <code>in</code>. */
    private static final String PUT_PARAMETER_IN = Messages.getString("PUT_PARAMETER_IN"); //$NON-NLS-1$

    /** Sets text for parameter <code>attachment</code>. */
    private static final String PUT_PARAMETER_ATTACHMENT = Messages.getString("PUT_PARAMETER_ATTACHMENT"); //$NON-NLS-1$

    /** Sets text for parameter <code>url</code>. */
    private static final String PARAMETER_URL = Messages.getString("PARAMETER_URL"); //$NON-NLS-1$

    /** Sets text for parameter <code>out</code>. */
    private static final String PARAMETER_OUT_FILE = Messages.getString("PARAMETER_OUT_FILE"); //$NON-NLS-1$

    /**
     * Main. Executes the put action.  
     * @param args command line arguments.     
     */
    public static void main(final String[] args) {

        String urlEndPoint = null;
        String fileIn = null;
        String attachment = null;
        String outputFile = null;
        PutMessage put = null;
        long init = -1;
        
        try {

            /* Reads command line parameters, store its values. */
            List<String> arguments = new ArrayList<>(Arrays.asList(args));
            
            /* If the list has duplicates must stop the execution. */
            String dup = findDuplicates(arguments, PUT_PARAMETER_IN, PUT_PARAMETER_ATTACHMENT, PARAMETER_OUT_FILE, PARAMETER_URL);
            if (dup != null) {
                throw new PutOperationException(EnumErrorCatalog.ERR_PUT_017, Messages.getString("PARAMETER_REPEATED",  dup)); //$NON-NLS-1$
            }  

            fileIn = readParameter(arguments, PUT_PARAMETER_IN);
            attachment = readParameter(arguments, PUT_PARAMETER_ATTACHMENT);
            outputFile = readParameter(arguments, PARAMETER_OUT_FILE);
            urlEndPoint = readParameter(arguments, PARAMETER_URL);
                      

            /* If the list is not empty means that user has put at least one "unknown" parameter. Show only first. */
            if (!arguments.isEmpty()) {
                throw new PutOperationException(EnumErrorCatalog.ERR_PUT_017, arguments.get(0));
            }

            /* Basic parameter validation. */
            if (fileIn == null && attachment == null) {
                throw new PutOperationException(EnumErrorCatalog.ERR_PUT_017, Messages.getString("PUT_PARAMETER_NO_INPUT_FILE")); //$NON-NLS-1$
            }

            if (fileIn != null && attachment != null) {
                throw new PutOperationException(EnumErrorCatalog.ERR_PUT_017, 
                        Messages.getString("PUT_PARAMETER_NO_TWO_KINDS_INPUT_FILE", PUT_PARAMETER_IN, PUT_PARAMETER_ATTACHMENT)); //$NON-NLS-1$
            }

            /* Sets the url, if no url is provided by arguments, use the one configured. */
            urlEndPoint = setConfig(urlEndPoint);

            /* Creates and set up a put object. */
            put = new PutMessage();
            put.setEndPoint(urlEndPoint);

            init = System.currentTimeMillis();
            String response;

            /* Sends the message. */
            if (fileIn == null) {
                File f = new File(attachment);
                String fileName = f.getName();
                response = put.put(fileName, FileUtil.readBinary(attachment), EnumMessageFormat.BINARY);

            } else {
                response = put.put(new StringBuilder(FileUtil.readUTF8(fileIn)));
            }

            /* Prints the response on screen if the user didn't specified an output file. */
            if (response == null) {
                LOGGER.fine("Response was empty"); //$NON-NLS-1$
            } else {
                writeResponse(outputFile, init, response);
            }

        } catch (PutOperationException e) {
            
            String code = e.getCode();
            
            if (code.equals(EnumErrorCatalog.ERR_HAND_010.getCode())) {
                
                LOGGER.log(Level.SEVERE, e.getCode() + ": " + e.getMessage() + " " + e.getCause().getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
                
                /* Server returns fault and user wants a response file? Write the fault as server response. */
                if (put != null && outputFile != null) {
                    String faultStr = put.getMessageMetaData().getRejectText();
                    writeResponse(outputFile, init, faultStr);
                }

            } else {

                LOGGER.log(Level.SEVERE, e.getCode() + ": " + e.getMessage(), e.getCause()); //$NON-NLS-1$

                /* Bad parameters? show usage! */
                if (code.equals(EnumErrorCatalog.ERR_PUT_017.getCode())) {
                    LOGGER.info(Messages.getString("PUT_USAGE", PUT_PARAMETER_IN, PUT_PARAMETER_ATTACHMENT, PARAMETER_OUT_FILE, PARAMETER_URL, new Date())); //$NON-NLS-1$
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

            if (fileIn == null) {
                LOGGER.severe(Messages.getString("UNABLE_TO_READ", attachment)); //$NON-NLS-1$
            } else {
                LOGGER.severe(Messages.getString("UNABLE_TO_READ", fileIn)); //$NON-NLS-1$
            }
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

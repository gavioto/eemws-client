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

import es.ree.eemws.client.common.ClientException;
import es.ree.eemws.client.put.PutMessage;
import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.core.utils.file.FileUtil;
import es.ree.eemws.kit.cmd.ParentMain;
import es.ree.eemws.kit.common.Messages;


/**
 * Main class to put messages.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class Main extends ParentMain {

    /** Name of the command. */
    private static final String COMMAND_NAME = "put"; //$NON-NLS-1$
	
	/** Sets text for parameter <code>in</code>. */
	private static final String PUT_PARAMETER_IN = Messages.getString("PUT_PARAMETER_IN"); //$NON-NLS-1$

	/** Sets text for parameter <code>attachment</code>. */
	private static final String PUT_PARAMETER_ATTACHMENT = Messages.getString("PUT_PARAMETER_ATTACHMENT"); //$NON-NLS-1$
	
	/** Sets text for parameter <code>url</code>. */
	private static final String PARAMETER_URL = Messages.getString("PARAMETER_URL"); //$NON-NLS-1$

	/** Sets text for parameter <code>out</code>. */
	private static final String PARAMETER_OUT_FILE = Messages.getString("PARAMETER_OUT_FILE"); //$NON-NLS-1$
	
    /** Log messages. */
    private static final Logger LOGGER = Logger.getLogger(COMMAND_NAME);

    /**
     * Main. Execute the put action.  
	 * @param args command line arguments.     
	 */
    public static void main(final String[] args) {

    	String urlEndPoint = null;
    	String fileIn = null;
    	String attachment = null;
    	String outputFile = null;
    	
    	boolean isReading = true;
    	
        try {
        	List<String> arguments = new ArrayList<>(Arrays.asList(args));
			
        	fileIn = readParameter(arguments, PUT_PARAMETER_IN);
        	attachment = readParameter(arguments, PUT_PARAMETER_ATTACHMENT);
        	outputFile = readParameter(arguments, PARAMETER_OUT_FILE);
        	urlEndPoint = readParameter(arguments, PARAMETER_URL);
        	
        	if (!arguments.isEmpty()) {
				throw new IllegalArgumentException(Messages.getString("UNKNOWN_PARAMETERS", arguments.toString())); //$NON-NLS-1$
			}
        	
        	String response;
 
        	if (fileIn == null && attachment == null) {
        		throw new IllegalArgumentException(Messages.getString("PUT_PARAMETER_NO_INPUT_FILE")); //$NON-NLS-1$
			}
        	
        	if (fileIn != null && attachment != null) {
        		throw new IllegalArgumentException(Messages.getString("PUT_PARAMETER_NO_TWO_KINDS_INPUT_FILE", PUT_PARAMETER_IN, PUT_PARAMETER_ATTACHMENT)); //$NON-NLS-1$
			}
        	
        	if (outputFile != null) {
				File f = new File(outputFile);
				if (f.isDirectory()) {
					throw new IllegalArgumentException(Messages.getString("UNABLE_TO_WRITE", outputFile)); //$NON-NLS-1$
				}
			}

            urlEndPoint = setConfig(urlEndPoint);

            PutMessage put = new PutMessage();
            put.setEndPoint(urlEndPoint);
            
            long init = System.currentTimeMillis();
            
            if (fileIn == null) {
            	
            	if (!FileUtil.exists(attachment)) {
                 	throw new IllegalArgumentException(Messages.getString("UNABLE_TO_READ", attachment)); //$NON-NLS-1$
                }
            	File f = new File(attachment);
            	String fileName = f.getName();
            	response = put.put("binary", fileName, FileUtil.readBinary(attachment)); //$NON-NLS-1$
            
            } else {
            
            	if (!FileUtil.exists(fileIn)) {
                 	throw new IllegalArgumentException(Messages.getString("UNABLE_TO_READ", fileIn)); //$NON-NLS-1$
                }
            	response = put.put(new StringBuilder(FileUtil.readUTF8(fileIn)));
            }

            if (outputFile == null) {
            	
				LOGGER.info(response);

				/* Don't output performance values here, the user could pipe the ouput to other process. */

			} else {
				isReading = false;
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
			LOGGER.info(Messages.getString("PUT_USAGE", PUT_PARAMETER_IN, PUT_PARAMETER_ATTACHMENT, PARAMETER_OUT_FILE, PARAMETER_URL, new Date())); //$NON-NLS-1$
		
		} catch (IOException e) {
			
			if (isReading) {
			
				if (fileIn == null) {
					LOGGER.severe(Messages.getString("UNABLE_TO_READ", attachment)); //$NON-NLS-1$
				} else {
					LOGGER.severe(Messages.getString("UNABLE_TO_READ", fileIn)); //$NON-NLS-1$
				}
				
			} else {
				
				LOGGER.severe(Messages.getString("UNABLE_TO_WRITE", outputFile)); //$NON-NLS-1$
			}
		}
    }
}

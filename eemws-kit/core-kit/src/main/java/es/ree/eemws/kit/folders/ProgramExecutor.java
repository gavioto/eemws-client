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

package es.ree.eemws.kit.folders;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.ree.eemws.kit.common.Messages;

/**
 * Executes an external program.
 * 
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 02/02/2016
 * 
 */
public final class ProgramExecutor {

    /** Log system. */
    private static final Logger LOGGER = Logger.getLogger(ProgramExecutor.class.getName());

    /** Replaces in a command line the abolute file name (path + filename). */
    private static final String ABS_FILE_NAME = "@ABS_FILE_NAME@"; //$NON-NLS-1$

    /** Replaces in a command line the file type. */
    private static final String FILE_TYPE = "@FILE_TYPE@"; //$NON-NLS-1$

    /** Replaces in a command line the file path. */
    private static final String FILE_PATH = "@FILE_PATH@"; //$NON-NLS-1$

    /** Replaces in a command line the file name. */
    private static final String FILE_NAME = "@FILE_NAME@"; //$NON-NLS-1$

    /** Replaces in a command line the message status. */
    private static final String STATUS = "@STATUS@"; //$NON-NLS-1$
    
    /**
     * Executes the given program.
     * @param whatToExecute Command line to be executed.
     * @param fileName File name where the message was stored.
     * @param status Message status, only for acks. Can be <code>null</code>. 
     * @param type Message type, only for get. Can be <code>null</code>.
     */
    public static void execute(final String whatToExecute, final File fileName, final String status, final String type) {
        
        String cmd = ""; //$NON-NLS-1$
                
        try {

            if (whatToExecute != null) {

                cmd = whatToExecute;
                /* On windows platforms paths uses "\" when replaced by other string the character will disapear!. */
                cmd = cmd.replaceAll(FILE_PATH, fileName.getParent().replaceAll("\\\\", "\\\\\\\\")); //$NON-NLS-1$ //$NON-NLS-2$
                cmd = cmd.replaceAll(FILE_NAME, fileName.getName());
                cmd = cmd.replaceAll(ABS_FILE_NAME, fileName.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\")); //$NON-NLS-1$ //$NON-NLS-2$
                if (status != null) {
                    cmd = cmd.replaceAll(STATUS, status);
                }
                
                if (type != null) {
                    cmd = cmd.replaceAll(FILE_TYPE, type);
                }

                LOGGER.info(Messages.getString("MF_RUN_INFO", cmd)); //$NON-NLS-1$
                Runtime rt = Runtime.getRuntime();
                Process proc = rt.exec(cmd);
                StreamConsumer errorGobbler = new StreamConsumer(proc.getErrorStream(), cmd);
                StreamConsumer outputGobbler = new StreamConsumer(proc.getInputStream(), cmd);
                errorGobbler.start();
                outputGobbler.start();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, Messages.getString("MF_RUN_ERROR", cmd), e); //$NON-NLS-1$
        }
    }

    /**
     * Constructor.
     * Utility classes should have a private constructor.
     */
    private ProgramExecutor() {

        /* This constructor must be empty. */
    }
}

/**
 * Reads the given input stream doing nothing with its content.
 */
class StreamConsumer extends Thread {

    /** Input stream to be consumed. */
    private InputStream inputStrm;

    /** Command line that is executing, for logging only. */
    private String whatExecuting;

    /** Log system. */
    private static final Logger LOGGER = Logger.getLogger(ProgramExecutor.class.getName());

    /**
     * Constructor.
     * Creates a new consumer for the given InputStream.
     * @param is InputStream to be consumed.
     * @param whatToExecute 
     */
    StreamConsumer(final InputStream is, final String whatToExecute) {
        inputStrm = is;
        whatExecuting = whatToExecute;
    }

    /**
     * Reads the configured input stream until it is empty.
     */
    public void run() {
        try (InputStreamReader isr = new InputStreamReader(inputStrm); BufferedReader br = new BufferedReader(isr);) {

            String line = null;
            do {
                line = br.readLine();
            } while (line != null);

        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, Messages.getString("MF_RUN_ERROR", whatExecuting), ioe); //$NON-NLS-1$
        }
    }
}

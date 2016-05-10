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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import es.ree.eemws.client.put.PutMessage;
import es.ree.eemws.core.utils.error.EnumErrorCatalog;
import es.ree.eemws.core.utils.file.FileUtil;
import es.ree.eemws.core.utils.iec61968100.EnumMessageStatus;
import es.ree.eemws.core.utils.iec61968100.FaultUtil;
import es.ree.eemws.core.utils.iec61968100.MessageMetaData;
import es.ree.eemws.core.utils.operations.put.PutOperationException;
import es.ree.eemws.core.utils.xml.XMLElementUtil;
import es.ree.eemws.kit.common.Messages;

/**
 * InputTask. Checks for files in the input folder, read them and send to the server.
 * 
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.1 27/04/2016
 */
public final class InputTask implements Runnable {

    /** Prefix added to identify response files. */
    private static final String RESPONSE_ID_PREFIX = "ack_"; //$NON-NLS-1$
    
    /** Object for checking messages sent by another module. */
    private final LockHandler lh;

    /** Put message operation object. */
    private PutMessage putMessage;

    /** This input task configuration set. */
    private InputConfigurationSet ics;
    
    /** This input set index (get it instead of asking ics each time). */
    private int icsIndex;
    
    /** Thread log system. */
    private static final Logger LOGGER = Logger.getLogger(InputTask.class.getName());

    /** Number of millisconds between file size checks. */
    private static final long SLEEP_BETWEEN_READS = 500;
   
    /**
     * Creates a Input task in order to sent the files in the configured folder to a server.
     * @param lockHandler Lock manager to synchronize the work.
     * @param ic This task configuration set.
     * @throws MalformedURLException If cannot obtain an URL to access attachment services.
     */
    public InputTask(final LockHandler lockHandler, final InputConfigurationSet ic) throws MalformedURLException {
     
        lh = lockHandler;
        ics = ic;
        icsIndex = ics.getIndex();
        putMessage = new PutMessage();
        putMessage.setEndPoint(ic.getInputUrlEndPoint());
        LOGGER.info(ic.toString());
    }

    /**
     * Checks whether the given file is complete. Checks file size, waits for a while and checks
     * its size again. If the size remains the same, the file is considered to be complete.
     * @param file Reference to the input file.
     * @return <code>true</code> When complete, <code>false</code> otherwise.
     */
    private boolean isComplete(final File file) {

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
     * Detects files in input folder. 
     * Sends them to the server 
     */
    @Override
    public void run() {
        
        String fileName = null;
        
        try {
            File f = new File(ics.getInputFolder());
            File[] files = f.listFiles();

            if (files != null) {
                boolean lockFile;
                StatusIcon.setBusy();

                for (File file : files) {
                    fileName = file.getName();
                    lockFile = lh.tryLock(fileName);

                    if (lockFile && isComplete(file)) {
                        process(file);
                    }

                    lh.releaseLock(fileName);

                }

                StatusIcon.setIdle();
            }
        } catch (Exception ex) {

            /* Defensive exception, if runnable task ends with exception won't be exectued againg! */
            LOGGER.log(Level.SEVERE, Messages.getString("MF_UNEXPECTED_ERROR_I", icsIndex), ex); //$NON-NLS-1$
        } finally {
            if (fileName != null) {
                lh.releaseLock(fileName);
            }
        }
    }

    /**
     * Process file with name and contents passed as arguments.
     * @param file Reference to the input file.
     */
    private void process(final File file) {

        String fileName = file.getName();
        String fullFileName = file.getAbsolutePath();

        try {

            /* Send. */
            LOGGER.info(Messages.getString("MF_SENDING_MESSAGE", icsIndex, fileName)); //$NON-NLS-1$
            StringBuilder response = new StringBuilder(putMessage.put(new StringBuilder(FileUtil.readUTF8(fullFileName))));
            LOGGER.info(Messages.getString("MF_SENT_MESSAGE", icsIndex, fileName)); //$NON-NLS-1$

            moveOnceProcessed(file);

            saveAndExecuteAck(file, response);

        } catch (PutOperationException ex) {

            /* Set status as failed, this is an exception!. */
            putMessage.getMessageMetaData().setStatus(EnumMessageStatus.FAILED);
            
            String code = ex.getCode();
            
            /* Soap Fault, save the fault message. */
            if (code.equals(EnumErrorCatalog.ERR_HAND_010.getCode())) {

                LOGGER.severe(Messages.getString("MF_SERVER_RETURNS_FAULT", icsIndex, fullFileName, ex.getCause().getMessage())); //$NON-NLS-1$
                saveAndExecuteAck(file, new StringBuilder(putMessage.getMessageMetaData().getRejectText()));

            } else {
                
                /* Magic folder needs a file, build a "fake" fault using the exception. */
                try {
                    String fault = XMLElementUtil.element2String(XMLElementUtil.obj2Element(FaultUtil.getFaultMessageFromException(ex.getMessage(), ex.getCode())));
                    saveAndExecuteAck(file, new StringBuilder(fault));
                    
                } catch (TransformerException |  ParserConfigurationException |  JAXBException e) {
                    LOGGER.log(Level.SEVERE, Messages.getString("MF_CANNOT_CREATE_FAULT_MSG", icsIndex), e); //$NON-NLS-1$
                }
                
                if (code.equals(EnumErrorCatalog.ERR_PUT_014)) {
                    LOGGER.log(Level.SEVERE, Messages.getString("MF_RETURNS_ERROR", icsIndex, fullFileName), ex); //$NON-NLS-1$
                } else {
                    LOGGER.log(Level.SEVERE, Messages.getString("MF_SERVER_RETURNS_ERROR", icsIndex, fullFileName), ex); //$NON-NLS-1$
                }
            }

            moveOnceProcessed(file);

        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, Messages.getString("MF_CANNOT_READ_FILE", icsIndex, fullFileName), ioe); //$NON-NLS-1$
        } 
    }
   
    /**
     * Moves (copy + delete) the input file to the processed folder (if configured) once the file is sent.
     * Depending on the java version, if source and destination are in different filesystems, move is not possible
     * (so that, this is implemented as copy + delete)
     * @param file Input file to be moved.
     */
    private void moveOnceProcessed(final File file) {

        String execContext = ""; //$NON-NLS-1$

        try {
            String fileName = file.getName();
            String fullFileName = file.getAbsolutePath();

            if (ics.getProcessedFolder() != null) {
                String processedFilePath = ics.getProcessedFolder() + File.separator + fileName;
                execContext = Messages.getString("MF_SAVING_PROCESS_FOLDER", icsIndex, fullFileName, processedFilePath); //$NON-NLS-1$
                FileUtil.writeUTF8(processedFilePath, FileUtil.readUTF8(fullFileName));
            }

            execContext = Messages.getString("MF_UNABLE_TO_DELETE_INPUT_FILE", icsIndex, fullFileName); //$NON-NLS-1$
            File f = new File(fullFileName);
            if (!f.delete()) {
                LOGGER.warning(execContext);
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, execContext, ioe);
        }
    }
    
    /**
     * Saves the given ack (could be a fault) into the configured ack folder(s). 
     * Then runs the configured scripts / programs
     * @param file Input file name (for log purposes)
     * @param response Server response as string.
     */
    private void saveAndExecuteAck(final File file, final StringBuilder response) {

        String execContext = ""; //$NON-NLS-1$
        String fileName = file.getName();
        String fullFileName = file.getAbsolutePath();

        try {

            String ackFilePath = null;
            EnumMessageStatus status = EnumMessageStatus.OK;

            /* Response is saved in response folder. */
            if (ics.getAckFolder() != null) {
                ackFilePath = ics.getAckFolder() + File.separator + RESPONSE_ID_PREFIX + fileName;
                execContext = Messages.getString("MF_SAVING_ACK_FOLDER", icsIndex, fullFileName, ackFilePath); //$NON-NLS-1$
                FileUtil.writeUTF8(ackFilePath, response.toString());
            }

            MessageMetaData metaData = putMessage.getMessageMetaData();
            status = metaData.getStatus();
            if (status == null) {
                status = EnumMessageStatus.OK;
            }
            

            if (status.equals(EnumMessageStatus.OK)) {

                /* Response is saved in response OK folder. */
                if (ics.getAckOkFolder() != null) {
                    ackFilePath = ics.getAckFolder() + File.separator + RESPONSE_ID_PREFIX + fileName;
                    execContext = Messages.getString("MF_SAVING_ACK_OK_FOLDER", icsIndex, fullFileName, ackFilePath); //$NON-NLS-1$
                    FileUtil.writeUTF8(ackFilePath, response.toString());
                }
                
            } else {

                /* Response is saved in response FAILED folder. */
                if (ics.getAckFailedFolder() != null) {
                    ackFilePath = ics.getAckFailedFolder() + File.separator + RESPONSE_ID_PREFIX + fileName;
                    execContext = Messages.getString("MF_SAVING_ACK_FAILED_FOLDER", icsIndex, fullFileName, ackFilePath); //$NON-NLS-1$
                    FileUtil.writeUTF8(ackFilePath, response.toString());
                }
            }
            
            /* If ack was saved and there is a program configured, run it...*/
            if (ackFilePath != null) {
                if (status.equals(EnumMessageStatus.OK)) {
                    if (ics.getAckOkProgramCmdLine() != null) {
                        ProgramExecutor.execute(ics.getAckOkProgramCmdLine(), new File(ackFilePath), status.toString(), null);        
                    }
                } else {
                    if (ics.getAckFailedProgramCmdLine() != null) {
                        ProgramExecutor.execute(ics.getAckFailedProgramCmdLine(), new File(ackFilePath), status.toString(), null);
                    }
                }
            }
            
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, execContext, ex);
        }
    }
}

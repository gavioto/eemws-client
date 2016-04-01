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
 * @version 1.0 01/02/2016
 */
public final class InputTask implements Runnable {

    /** Prefix added to identify response files. */
    private static final String RESPONSE_ID_PREFIX = "ack_"; //$NON-NLS-1$

    /** Input folder. */
    private final String inputFolderPath;

    /** Processed folder. */
    private final String processedFolderPath;

    /** Response folder. */
    private final String responseFolderPath;

    /** Response OK folder. */
    private final String responseOkFolderPath;

    /** Response FAILED folder. */
    private final String responseFailedForderPath;

    /** Command line to execute for OK response. */
    private final String cmdOk;

    /** Command line to execute for FAILED response. */
    private final String cmdFailed;

    /** Sending web service module. */
    private final PutMessage putMessage;

    /** Object for checking messages sent by another module. */
    private final LockHandler lh;

    /** Thread log system. */
    private static final Logger LOGGER = Logger.getLogger(InputTask.class.getName());

    /** Number of millisconds between file size checks. */
    private static final long SLEEP_BETWEEN_READS = 500;

    /** Log elements separator. */
    private static final String TAB = "\n    "; //$NON-NLS-1$

    /**
     * Constructor. 
     * @param lockHandler Lock handler.
     * @param index Input task index.
     * @param config Module settings.
     * @throws MalformedURLException If cannot obtain an URL to access attachment services.
     */
    public InputTask(final LockHandler lockHandler, final Integer index, final Configuration config) throws MalformedURLException {

        inputFolderPath = config.getInputFolder(index);
        responseFolderPath = config.getResponseFolder(index);
        responseOkFolderPath = config.getResponseOkFolder(index);
        responseFailedForderPath = config.getResponseFailedFolder(index);
        processedFolderPath = config.getProcessedFolder(index);
        
        String endPoint = config.getUrlEndPoint().toString();

        lh = lockHandler;

        putMessage = new PutMessage();
        putMessage.setEndPoint(endPoint);

        StringBuilder msg = new StringBuilder();
        msg.append("\n"); //$NON-NLS-1$
        msg.append(Messages.getString("MF_SET_NUM", index)); //$NON-NLS-1$
        msg.append(TAB).append(Messages.getString("MF_CONFIG_INPUT_FOLDER", inputFolderPath)); //$NON-NLS-1$

        if (responseFolderPath != null) {
            msg.append(TAB).append(Messages.getString("MF_CONFIG_ACK_FOLDER", responseFolderPath)); //$NON-NLS-1$
        }

        if (responseOkFolderPath != null) {
            msg.append(TAB).append(Messages.getString("MF_CONFIG_ACK_OK_FOLDER", responseOkFolderPath)); //$NON-NLS-1$
        }

        if (responseFailedForderPath != null) {
            msg.append(TAB).append(Messages.getString("MF_CONFIG_ACK_FAILED_FOLDER", responseFailedForderPath)); //$NON-NLS-1$
        }

        String configCmdOk = config.getAckOkProgramCmdLIne(index);
        if (configCmdOk == null) {
            cmdOk = null;
        } else {
            if (responseFolderPath != null || responseOkFolderPath != null) {
                msg.append(TAB).append(Messages.getString("MF_CONFIG_CMD_ACK_OK", configCmdOk)); //$NON-NLS-1$
                cmdOk = configCmdOk;
            } else {
                
                /* User has set a program for responses, but did not set any folder for responses. Ignoring parameter. */
                cmdOk = null;
            }
        }

        String configCmdFailed = config.getAckFailedProgramCmdLine(index);
        if (configCmdFailed == null) {
            cmdFailed = null;
        } else {
            if (responseFolderPath != null || responseFailedForderPath != null) {
                msg.append(TAB).append(Messages.getString("MF_CONFIG_CMD_ACK_FAILED", configCmdFailed)); //$NON-NLS-1$
                cmdFailed = configCmdFailed;
            } else {
                
                /* User has set a program for responses, but did not set any folder for responses. Ignoring parameter. */
                cmdFailed = null;
            }
        }
        
        if (processedFolderPath != null) {
            msg.append(TAB).append(Messages.getString("MF_CONFIG_PROCESSED_FOLDER", processedFolderPath)); //$NON-NLS-1$
        }

        msg.append(TAB).append(Messages.getString("MF_CONFIG_DELAY_TIME_I", config.getSleepTimeInput(index))); //$NON-NLS-1$
        msg.append(TAB).append(Messages.getString("MF_CONFIG_URL_I", endPoint.toString())); //$NON-NLS-1$

        LOGGER.info(msg.toString());
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
            File f = new File(inputFolderPath);
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
            LOGGER.log(Level.SEVERE, Messages.getString("MF_UNEXPECTED_ERROR"), ex); //$NON-NLS-1$
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

        String execContext = ""; //$NON-NLS-1$

        String fileName = file.getName();
        String fullFileName = file.getAbsolutePath();

        try {

            /* Send. */
            LOGGER.info(Messages.getString("MF_SENDING_MESSAGE", fileName)); //$NON-NLS-1$
            StringBuilder response = new StringBuilder(putMessage.put(new StringBuilder(FileUtil.readUTF8(fullFileName))));
            LOGGER.info(Messages.getString("MF_SENT_MESSAGE", fileName)); //$NON-NLS-1$

            moveOnceProcessed(file);

            saveAndExecuteAck(file, response);

        } catch (PutOperationException ex) {

            /* Set status as failed, this is an exception!. */
            putMessage.getMessageMetaData().setStatus(EnumMessageStatus.FAILED);
            
            String code = ex.getCode();
            
            /* Soap Fault, save the fault message. */
            if (code.equals(EnumErrorCatalog.ERR_HAND_010.getCode())) {

                LOGGER.severe(Messages.getString("MF_SERVER_RETURNS_FAULT", fullFileName, ex.getCause().getMessage())); //$NON-NLS-1$
                saveAndExecuteAck(file, new StringBuilder(putMessage.getMessageMetaData().getRejectText()));

            } else {
                
                /* Magic folder needs a file, build a "fake" fault using the exception. */
                try {
                    String fault = XMLElementUtil.element2String(XMLElementUtil.obj2Element(FaultUtil.getFaultMessageFromException(ex.getMessage(), ex.getCode())));
                    saveAndExecuteAck(file, new StringBuilder(fault));
                    
                } catch (TransformerException |  ParserConfigurationException |  JAXBException e) {
                    LOGGER.log(Level.SEVERE, Messages.getString("MF_CANNOT_CREATE_FAULT_MSG"), e); //$NON-NLS-1$
                }
                
                if (code.equals(EnumErrorCatalog.ERR_PUT_014)) {
                    LOGGER.log(Level.SEVERE, Messages.getString("MF_RETURNS_ERROR", fullFileName), ex); //$NON-NLS-1$
                } else {
                    LOGGER.log(Level.SEVERE, Messages.getString("MF_SERVER_RETURNS_ERROR", fullFileName), ex); //$NON-NLS-1$
                }
            }

            moveOnceProcessed(file);

        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, execContext, ioe);
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

            if (processedFolderPath != null) {
                String processedFilePath = processedFolderPath + File.separator + fileName;
                execContext = Messages.getString("MF_SAVING_PROCESS_FOLDER", fullFileName, processedFilePath); //$NON-NLS-1$
                FileUtil.writeUTF8(processedFilePath, FileUtil.readUTF8(fullFileName));
            }

            execContext = Messages.getString("MF_UNABLE_TO_DELETE_INPUT_FILE", fullFileName); //$NON-NLS-1$
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
            if (responseFolderPath != null) {
                ackFilePath = responseFolderPath + File.separator + RESPONSE_ID_PREFIX + fileName;
                execContext = Messages.getString("MF_SAVING_ACK_FOLDER", fullFileName, ackFilePath); //$NON-NLS-1$
                FileUtil.writeUTF8(ackFilePath, response.toString());
            }

            if (responseOkFolderPath != null || responseFailedForderPath != null) {

                MessageMetaData metaData = putMessage.getMessageMetaData();
                status = metaData.getStatus();
                if (status == null) {
                    status = EnumMessageStatus.OK;
                }

                if (status.equals(EnumMessageStatus.OK)) {

                    /* Response is saved in response OK folder. */
                    if (responseOkFolderPath != null) {
                        ackFilePath = responseOkFolderPath + File.separator + RESPONSE_ID_PREFIX + fileName;
                        execContext = Messages.getString("MF_SAVING_ACK_OK_FOLDER", fullFileName, ackFilePath); //$NON-NLS-1$
                        FileUtil.writeUTF8(ackFilePath, response.toString());
                    }

                    if (cmdOk != null) {
                        ProgramExecutor.execute(cmdOk, new File(ackFilePath), EnumMessageStatus.OK.toString(), null);
                    }

                } else {

                    /* Response is saved in response FAILED folder. */
                    if (responseFailedForderPath != null) {
                        ackFilePath = responseFailedForderPath + File.separator + RESPONSE_ID_PREFIX + fileName;
                        execContext = Messages.getString("MF_SAVING_ACK_FAILED_FOLDER", fullFileName, ackFilePath); //$NON-NLS-1$
                        FileUtil.writeUTF8(ackFilePath, response.toString());
                    }

                    if (cmdFailed != null) {
                        ProgramExecutor.execute(cmdFailed, new File(ackFilePath), EnumMessageStatus.FAILED.toString(), null);
                    }
                }
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, execContext, ex);
        }
    }
}

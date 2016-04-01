/*
 * Copyright 2015 Red Eléctrica de España, S.A.U.
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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.client.get.GetMessage;
import es.ree.eemws.client.get.RetrievedMessage;
import es.ree.eemws.client.list.ListMessages;
import es.ree.eemws.client.list.MessageListEntry;
import es.ree.eemws.core.utils.file.FileUtil;
import es.ree.eemws.core.utils.operations.get.GetOperationException;
import es.ree.eemws.core.utils.operations.list.ListOperationException;

/**
 * Execute a list + get loop to retrieve messages.
 * 
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.1 02/11/2015
 * 
 */
public final class OutputTask implements Runnable {

    /** Output folder. */
    private Map<Integer, String> outputFolder = new HashMap<>();

    /** List of message types to retrieve. */
    private Map<Integer, List<String>> typesToRetrieveList = new HashMap<>();

    /** Program to be executed when a file is saved. */
    private Map<Integer, String> programCmdLine = new HashMap<>();
    
    /** Total list of messages to retrieve (sum of the typesToRetrieveList). */
    private List<String> totalTypesToRetrieve = new ArrayList<>();

    /** File name extension to be used. */
    private Map<Integer, String> fileNameExtension = new HashMap<>();

    /** Code for the last listed message. */
    private long lastListCode;

    /** Object which locks message for group working. */
    private LockHandler lh;

    /** List messages. */
    private ListMessages list;

    /** Retrieve messages. */
    private GetMessage get;
    
    /** Log elements separator. */
    private static final String TAB = "\n    "; //$NON-NLS-1$
    
    /** Log system. */
    private static final Logger LOGGER = Logger.getLogger(OutputTask.class.getName());

    /** Temporary file prefix. */
    private static final String TMP_PREFIX = "_tmp_out_"; //$NON-NLS-1$

    /** File name extension for "xml". */
    private static final String FILE_NAME_EXTENSION_XML = "xml"; //$NON-NLS-1$
    
    /** File name extension separator. */
    private static final String FILE_ELEMENTS_SEPARTOR = "."; //$NON-NLS-1$

    /** Headers values that identifies the file type. */
    private static final String[] HEADER_VALUES = { "BZh91AY", "7z", "PDF", "PK", "PNG", "JFIF" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

    /** File extensions according to the header value. */
    private static final String[] EXTENSION_VALUES = { "bz2", "7z", "pdf", "zip", "png", "jpg" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

    /** Reads up to 20 bytes in order to guess the proper file extension. */
    private static final int FIRST_BYTES_OF_MESSAGE = 20;

    /**
     * Constructor. Initializes parameters for detection thread.
     * @param lockHandler Lock Manager.
     * @param config Module configuration.
     */
    public OutputTask(final LockHandler lockHandler, final Configuration config) {

        StringBuilder msg = new StringBuilder();
        
        boolean listAll = false;
        for (int k = 0; config.getOutputFolder(k) != null; k++) {
            
            List<String> lstT = config.getMessagesTypeList(k);
            if (lstT == null) {
                listAll = true;
            } else {
                typesToRetrieveList.put(k, lstT);
                totalTypesToRetrieve.addAll(lstT);
            }
            
            String outF = config.getOutputFolder(k);
            outputFolder.put(k, outF);     
            msg.append("\n"); //$NON-NLS-1$
            msg.append(Messages.getString("MF_SET_NUM", k)); //$NON-NLS-1$
            msg.append(TAB).append(Messages.getString("MF_CONFIG_OUTPUT_FOLDER", outF)); //$NON-NLS-1$ 
            
            msg.append(TAB);
            if (lstT == null) {
                msg.append(Messages.getString("MF_CONFIG_LST_MESSAGES_TYPE_ALL")); //$NON-NLS-1$ 
            } else {                
                msg.append(Messages.getString("MF_CONFIG_LST_MESSAGES_TYPE", lstT.toString())); //$NON-NLS-1$ 
            }
            
            String filE = config.getFileNameExtension(k);
            fileNameExtension.put(k, filE);
            msg.append(TAB).append(Messages.getString("MF_FILE_NAME_EXTENSION", filE)); //$NON-NLS-1$ 

            
            String program = config.getProgramCmdLine(k);
            if (program == null) {
                msg.append(TAB).append(Messages.getString("MF_NO_PROGRAM")); //$NON-NLS-1$ 

            } else {
                msg.append(TAB).append(Messages.getString("MF_PROGRAM", program)); //$NON-NLS-1$ 
                programCmdLine.put(k, program);
            }            
        }
        msg.append("\n").append(Messages.getString("MF_CONFIG_DELAY_TIME_O", config.getSleepTimeOutput())); //$NON-NLS-1$//$NON-NLS-2$
        
        if (listAll) {
            totalTypesToRetrieve = null;
        }
        
        URL endPoint = config.getUrlEndPoint();
        lastListCode = 0;
        lh = lockHandler;

        list = new ListMessages();
        list.setEndPoint(endPoint);

        get = new GetMessage();
        get.setEndPoint(endPoint);

        
        msg.append("\n").append(Messages.getString("MF_CONFIG_URL_O", endPoint.toString())); //$NON-NLS-1$ //$NON-NLS-2$
         
        LOGGER.info(msg.toString());
    }

    /**
     * Retrieves and stores a message.
     * @param mle List element retrieved in detection process.
     */
    private void retrieveAndStore(final MessageListEntry mle) {

        long code = mle.getCode().longValue();
        String codeStr = String.valueOf(code);

        boolean lockFile = lh.tryLock(codeStr);

        if (lockFile) {

            try {
                if (mle.getVersion() == null) {
                    LOGGER.info(Messages.getString("MF_RETRIEVING_MESSAGE_WO_VERSION", codeStr, mle.getMessageIdentification())); //$NON-NLS-1$
                } else {
                    LOGGER.info(Messages.getString("MF_RETRIEVING_MESSAGE", codeStr, mle.getMessageIdentification(), mle.getVersion())); //$NON-NLS-1$
                }

                RetrievedMessage response = get.get(code);

                if (mle.getVersion() == null) {
                    LOGGER.info(Messages.getString("MF_RETRIEVED_MESSAGE_WO_VERSION", codeStr, mle.getMessageIdentification())); //$NON-NLS-1$
                } else {
                    LOGGER.info(Messages.getString("MF_RETRIEVED_MESSAGE", codeStr, mle.getMessageIdentification(), mle.getVersion())); //$NON-NLS-1$
                }

                for (Integer k : outputFolder.keySet()) {
                    List<String> type = typesToRetrieveList.get(k);
                    if (type == null || type.contains(mle.getType())) {
                        saveFile(mle, response, outputFolder.get(k), fileNameExtension.get(k), programCmdLine.get(k));
                    }                    
                }
                
            } catch (GetOperationException e) {
                if (mle.getVersion() == null) {
                    LOGGER.log(Level.SEVERE, Messages.getString("MF_UNABLE_TO_GET_WO_VERSION", String.valueOf(code), mle.getMessageIdentification()), e); //$NON-NLS-1$
                } else {
                    LOGGER.log(Level.SEVERE, Messages.getString("MF_UNABLE_TO_GET", String.valueOf(code), mle.getMessageIdentification(), mle.getVersion()), e); //$NON-NLS-1$
                }
            } catch (IOException e) {
                if (mle.getVersion() == null) {
                    LOGGER.log(Level.SEVERE, Messages.getString("MF_UNABLE_TO_SAVE_WO_VERSION", String.valueOf(code), mle.getMessageIdentification()), e); //$NON-NLS-1$
                } else {
                    LOGGER.log(Level.SEVERE, Messages.getString("MF_UNABLE_SAVE_GET", String.valueOf(code), mle.getMessageIdentification(), mle.getVersion()), e); //$NON-NLS-1$
                }
            } finally {
                lh.releaseLock(codeStr);
            }
        }
    }

    /**
     * Saves the retrieved message and (optionally) executes a program.
     * @param mle Retrieved message information.
     * @param response Retrieved message.
     * @param outPath Path to save.
     * @param fExtension File extension to be added to the file name.
     * @param cmdLine Program to execute. <code>null</code> if no program have to be executed.
     * @throws IOException If the message cannot be saved or if the provided command line produces error.
     */
    private void saveFile(final MessageListEntry mle, final RetrievedMessage response, final String outPath, final String fExtension, final String cmdLine) throws IOException {
        String fileName = getFileName(mle, response, fExtension);
        String abosoluteFileName = outPath + File.separator + fileName;

        if (FileUtil.exists(abosoluteFileName)) {

            LOGGER.info(Messages.getString("MF_RETRIEVED_MESSAGE_ALREADY_EXISTS", abosoluteFileName)); //$NON-NLS-1$

        } else {

            /*
             * Avoid "broken files" in case of anormal program termination. First write into a temporaly file
             * then rename it.
             */
            File tmpFile;
            tmpFile = File.createTempFile(TMP_PREFIX, null, new File(outPath));
            if (response.isBinary()) {
                FileUtil.write(tmpFile.getAbsolutePath(), response.getBinaryPayload());
            } else {
                FileUtil.writeUTF8(tmpFile.getAbsolutePath(), response.getPrettyPayload());
            }

            File file = new File(abosoluteFileName);
            tmpFile.renameTo(file);
            
            ProgramExecutor.execute(cmdLine, file,  null,  mle.getType());            
        }
    }

    /**
     * Returns the file name  according to the configuration and the retrieved message.
     * @param mle message entry which file name will be returned.
     * @param response Retrieved message.
     * @param fExtension File extension to be added.
     * @return Proper file name. 
     */
    private String getFileName(final MessageListEntry mle, final RetrievedMessage response, final String fExtension) {

        StringBuilder retValue = new StringBuilder();

        if (response.isBinary()) {
            retValue.append(response.getFileName());
        } else {
            retValue.append(mle.getMessageIdentification());

            if (mle.getVersion() != null) {
                retValue.append(FILE_ELEMENTS_SEPARTOR);
                retValue.append(mle.getVersion());
            }
        }

        if (fExtension.equalsIgnoreCase(Configuration.FILE_NAME_EXTENSION_AUTO)) {
            if (response.isBinary()) {
                byte[] b = response.getBinaryPayload();
                if (b.length > FIRST_BYTES_OF_MESSAGE) {
                    String headerValue = new String(response.getBinaryPayload(), 0, FIRST_BYTES_OF_MESSAGE);
                    boolean found = false;
                    for (int cont = 0; cont < HEADER_VALUES.length && !found; cont++) {
                        if (headerValue.indexOf(HEADER_VALUES[cont]) != -1) {
                            retValue.append(FILE_ELEMENTS_SEPARTOR);
                            retValue.append(EXTENSION_VALUES[cont]);
                            found = true;
                        }
                    }
                }

            } else {
                retValue.append(FILE_ELEMENTS_SEPARTOR);
                retValue.append(FILE_NAME_EXTENSION_XML);
            }
        } else if (!fExtension.equalsIgnoreCase(Configuration.FILE_NAME_EXTENSION_NONE)) {
            retValue.append(FILE_ELEMENTS_SEPARTOR);
            retValue.append(fExtension);
        }

        return retValue.toString();
    }

    /**
     * Gets a message list using the last list code.
     * @return A message list. <code>null</code> if the client cannot connect with the server.
     */
    private List<MessageListEntry> listMessages() {
        List<MessageListEntry> messageList = null;

        try {
            messageList = list.list(lastListCode);
        } catch (ListOperationException ex) {
            LOGGER.log(Level.SEVERE, Messages.getString("MF_UNABLE_TO_LIST"), ex); //$NON-NLS-1$
        }

        return messageList;
    }

    /**
     * Detection cycle.
     */
    public void run() {
        try {
            List<MessageListEntry> messageList = listMessages();

            if (messageList != null) {
                int len = messageList.size();

                if (len > 1) {
                    StatusIcon.setBusy();

                    for (final MessageListEntry message : messageList) {

                        if (totalTypesToRetrieve == null || totalTypesToRetrieve.contains(message.getType())) {
                            retrieveAndStore(message);
                        }

                        /* Take the highest message code to start listing form this one. */
                        int msgCode = message.getCode().intValue();
                        if (msgCode > lastListCode) {
                            lastListCode = msgCode;
                        }
                    }

                    StatusIcon.setIdle();
                }
            }
        } catch (Exception ex) {

            // Defensive exception, if runnable task ends with exception won't be exectued againg!
            LOGGER.log(Level.SEVERE, Messages.getString("MF_UNEXPECTED_ERROR"), ex); //$NON-NLS-1$
        }
    }

}

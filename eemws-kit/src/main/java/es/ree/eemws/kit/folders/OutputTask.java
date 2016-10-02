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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import es.ree.eemws.client.get.GetMessage;
import es.ree.eemws.client.get.RetrievedMessage;
import es.ree.eemws.client.list.ListMessages;
import es.ree.eemws.client.list.MessageListEntry;
import es.ree.eemws.core.utils.file.FileUtil;
import es.ree.eemws.core.utils.operations.get.GetOperationException;
import es.ree.eemws.core.utils.operations.list.ListOperationException;
import es.ree.eemws.kit.common.Messages;

/**
 * Executes a list + get loop to retrieve messages.
 * 
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.2 15/09/2016
 * 
 */
public final class OutputTask implements Runnable {

    /** Object which locks message for group working. */
    private LockHandler lh;

    /** Log system. */
    private static final Logger LOGGER = Logger.getLogger(OutputTask.class.getName());

    /** Temporary file prefix. */
    private static final String TMP_PREFIX = "_tmp_out_"; //$NON-NLS-1$

    /** File name extension for "xml". */
    private static final String FILE_NAME_EXTENSION_XML = "xml"; //$NON-NLS-1$

    /** File name extension separator. */
    private static final String FILE_ELEMENTS_SEPARTOR = "."; //$NON-NLS-1$

    /** Headers values that identifies the file type. */
    @SuppressWarnings("nls")
    private static final String[] HEADER_VALUES = { "BZh91AY", "7z", "PDF", "PK", "PNG", "JFIF" };

    /** File extensions according to the header value. */
    @SuppressWarnings("nls")
    private static final String[] EXTENSION_VALUES = { "bz2", "7z", "pdf", "zip", "png", "jpg" };

    /** Reads up to 20 bytes in order to guess the proper file extension. */
    private static final int FIRST_BYTES_OF_MESSAGE = 20;

    /** This task configuration set values. */
    private List<OutputConfigurationSet> ocs;

    /** Last retrieved code. */
    private long lastListCode;

    /** Full list of files to retrieve. */
    private List<String> totalTypesToRetrieve;

    /** Message List object. */
    private ListMessages list;

    /** Message get object. */
    private GetMessage get;

    /** This output task set of ids. */
    private String setIds;

    /** Last retrieved file. */
    private RetrievedMessage lastRetrievedFile = null;
    
    /** Preference object in order to keep the lastest list's code value. */
    private Preferences preferences = Preferences.userNodeForPackage(getClass()); 

    /**
     * Constructor. Initializes parameters for detection thread.
     * @param lockHandler Lock Manager.
     * @param oc List of output configuration sets that shares the same url.
     * @param setIdss This output task set of ids.
     */
    public OutputTask(final LockHandler lockHandler, final List<OutputConfigurationSet> oc, final String setIdss) {

        totalTypesToRetrieve = new ArrayList<>();
        boolean retrieveAllMessages = false;
        for (OutputConfigurationSet o : oc) {
            LOGGER.info(o.toString());

            if (!retrieveAllMessages) {
                List<String> lstRetr = o.getMessagesTypesList();
                if (lstRetr == null) {
                    retrieveAllMessages = true;
                    totalTypesToRetrieve = null;
                } else {
                    totalTypesToRetrieve.addAll(lstRetr);
                }
            }
        }

        URL endPoint = oc.get(0).getOutputUrlEndPoint(); 
        
        lastListCode = preferences.getLong(oc.get(0).getOutputUrlEndPoint().toString(), 0);
        
        if (lastListCode != 0) {
            LOGGER.info(Messages.getString("MF_CONFIG_LST_CODE", setIdss, String.valueOf(lastListCode)));   //$NON-NLS-1$
        }
                
        list = new ListMessages();
        list.setEndPoint(endPoint);

        get = new GetMessage();
        get.setEndPoint(endPoint);

        setIds = setIdss;
        lh = lockHandler;
        ocs = oc;

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
                
                lastRetrievedFile = null;

                for (OutputConfigurationSet oc : ocs) {
                    List<String> type = oc.getMessagesTypesList();
                    if (type == null || type.contains(mle.getType())) {
                        saveFile(mle, oc);
                    }
                }

            } catch (GetOperationException e) {
                if (mle.getVersion() == null) {
                    LOGGER.log(Level.SEVERE, Messages.getString("MF_UNABLE_TO_GET_WO_VERSION", setIds, String.valueOf(code), mle.getMessageIdentification()), e); //$NON-NLS-1$
                } else {
                    LOGGER.log(Level.SEVERE, Messages.getString("MF_UNABLE_TO_GET", setIds, String.valueOf(code), //$NON-NLS-1$
                            mle.getMessageIdentification(), mle.getVersion()), e);
                }
            } catch (IOException e) {
                if (mle.getVersion() == null) {
                    LOGGER.log(Level.SEVERE, Messages.getString("MF_UNABLE_TO_SAVE_WO_VERSION", setIds, String.valueOf(code), mle.getMessageIdentification()), e); //$NON-NLS-1$
                } else {
                    LOGGER.log(Level.SEVERE, Messages.getString("MF_UNABLE_TO_SAVE", setIds, String.valueOf(code), //$NON-NLS-1$
                            mle.getMessageIdentification(), mle.getVersion()), e);
                }
            } finally {
                lh.releaseLock(codeStr);
                
                /* Avoid holding this (probably) huge value */
                lastRetrievedFile = null;
            }
        }
    }

    /**
     * Gets the message given its <code>MessageListEntry</code>.
     * The method wont call the get operation if the file was already retrieved.
     * @param mle MessageListEntry with the information about the file to be retrieved.
     * @return The message retrieved according the given MessageListEntry parameter. <code>null</code> is never returned.
     * @throws GetOperationException If the message cannot be retrieved.
     */
    private RetrievedMessage retrieveFile(final MessageListEntry mle) throws GetOperationException {

        if (lastRetrievedFile == null) {

            long code = mle.getCode().longValue();
            String codeStr = String.valueOf(code);

            if (mle.getVersion() == null) {
                LOGGER.info(Messages.getString("MF_RETRIEVING_MESSAGE_WO_VERSION", setIds, codeStr, mle.getMessageIdentification())); //$NON-NLS-1$
            } else {
                LOGGER.info(Messages.getString("MF_RETRIEVING_MESSAGE", setIds, codeStr, mle.getMessageIdentification(), mle.getVersion())); //$NON-NLS-1$
            }

            lastRetrievedFile = get.get(code);

            if (mle.getVersion() == null) {
                LOGGER.info(Messages.getString("MF_RETRIEVED_MESSAGE_WO_VERSION", setIds, codeStr, mle.getMessageIdentification())); //$NON-NLS-1$
            } else {
                LOGGER.info(Messages.getString("MF_RETRIEVED_MESSAGE", setIds, codeStr, mle.getMessageIdentification(), mle.getVersion())); //$NON-NLS-1$
            }
        }
        
        return lastRetrievedFile;

    }

    /**
     * Saves the current message and (optionally) executes a program.
     * @param mle Retrieved message information.
     * @param oc Configuration to be used in order to save the file. 
     * @throws IOException If the message cannot be saved or if the provided command line produces error.
     * @throws GetOperationException  If the message cannot be retrieved.
     */
    private void saveFile(final MessageListEntry mle, final OutputConfigurationSet oc) throws IOException, GetOperationException {

        String fileName = calculateFileName(mle, oc.getFileNameExtension());

        String abosoluteFileName= oc.getOutputFolder() + File.separator + fileName;

        if (FileUtil.exists(abosoluteFileName)) {

            LOGGER.info(Messages.getString("MF_RETRIEVED_MESSAGE_ALREADY_EXISTS", oc.getIndex(), abosoluteFileName)); //$NON-NLS-1$

        } else {
 
            RetrievedMessage message = retrieveFile(mle);
  
            /*
             * Avoid "broken files" in case of anormal program termination. First write into a temporaly file then
             * rename it.
             */
            File tmpFile;
            tmpFile = File.createTempFile(TMP_PREFIX, null, new File(oc.getOutputFolder()));
            if (message.isBinary()) {
                FileUtil.write(tmpFile.getAbsolutePath(), message.getBinaryPayload());
            } else {
                FileUtil.writeUTF8(tmpFile.getAbsolutePath(), message.getStringPayload()); 
            }

            File file = new File(abosoluteFileName);
            tmpFile.renameTo(file);

            ProgramExecutor.execute(oc.getProgramCmdLine(), file, null, mle.getType());
        }

    } 

    /**
     *  Gets the file name extension according to the given extension configuration value.
     *  The extension is obtained as following:
     *  <li> AUTO (for binary): fileName + ["." + calculatedExtension"] </li>
     *  <li> AUTO (non binary): fileName + ["." + version] + ".xml" </li>
     *  <li> NONE: messageId + ["." + version] </li>
     *  <li> XXXX: messageId + ["." + version] + ".XXXX </li>
     *  
     *  NOTE: If the retrieved binary file has an unsupported extension the calculated file name won't have extension.
     *  NOTE: AUTO forces file retrieving in order to know the proper extension according to the file's content.
     *  
     *  @param mle Current message list entry.
     *  @param fExtension Extension configuration value.
     *  @throws GetOperationException if file cannot be retrieved.
     */
    private String calculateFileName(final MessageListEntry mle, final String fExtension) throws GetOperationException {

        StringBuilder extStr = new StringBuilder();
        
        
        if (fExtension.equalsIgnoreCase(OutputConfigurationSet.FILE_NAME_EXTENSION_AUTO)) {

            /* Extension == AUTO and the file was not yet retrieved -> get the file */
            RetrievedMessage message = retrieveFile(mle);

            if (message.isBinary()) {
                            
                extStr.append(message.getFileName());
                               
                byte[] b = message.getBinaryPayload();
                if (b.length > FIRST_BYTES_OF_MESSAGE) {
                    String headerValue = new String(message.getBinaryPayload(), 0, FIRST_BYTES_OF_MESSAGE);
                    boolean found = false;
                    for (int cont = 0; cont < HEADER_VALUES.length && !found; cont++) {
                        if (headerValue.indexOf(HEADER_VALUES[cont]) != -1) {
                            extStr.append(FILE_ELEMENTS_SEPARTOR);
                            extStr.append(EXTENSION_VALUES[cont]);
                            found = true;
                        }
                    }
                }

            } else {
            
                extStr.append(mle.getMessageIdentification());
                
                if (mle.getVersion() != null) {
                    extStr.append(FILE_ELEMENTS_SEPARTOR);
                    extStr.append(mle.getVersion());
                    extStr.append(FILE_ELEMENTS_SEPARTOR);
                    extStr.append(FILE_NAME_EXTENSION_XML);
                }
            }

        } else {
            
            extStr.append(mle.getMessageIdentification());
                        
            if (mle.getVersion() != null) {
                extStr.append(FILE_ELEMENTS_SEPARTOR);
                extStr.append(mle.getVersion());
            }

            if (!fExtension.equalsIgnoreCase(OutputConfigurationSet.FILE_NAME_EXTENSION_NONE)) {
                extStr.append(FILE_ELEMENTS_SEPARTOR);
                extStr.append(fExtension);
             }
        }
        
        
        return extStr.toString();
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
            LOGGER.log(Level.SEVERE, Messages.getString("MF_UNABLE_TO_LIST", setIds), ex); //$NON-NLS-1$
        }

        return messageList;
    }

    /**
     * Detection cycle.
     */
    @Override
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
                            preferences.putLong(ocs.get(0).getOutputUrlEndPoint().toString(), lastListCode);
                        }
                    }

                    StatusIcon.setIdle();
                }
            }
        } catch (Exception ex) {

            // Defensive exception, if runnable task ends with exception won't be exectued againg!
            LOGGER.log(Level.SEVERE, Messages.getString("MF_UNEXPECTED_ERROR_O", setIds), ex); //$NON-NLS-1$
        }
    }

}

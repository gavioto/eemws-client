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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.core.utils.config.ConfigManager;
import es.ree.eemws.core.utils.file.FileUtil;
import es.ree.eemws.kit.common.Messages;

/**
 * Magic folder configuration settings.
 * 
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 01/02/2016
 */
public final class Configuration extends es.ree.eemws.kit.config.Configuration {

    /** File name extension "auto". */
    public static final String FILE_NAME_EXTENSION_AUTO = "AUTO"; //$NON-NLS-1$

    /** File name extension "none". */
    public static final String FILE_NAME_EXTENSION_NONE = "NONE"; //$NON-NLS-1$

    /** Settings file for Folder manager. */
    private static final String FOLDER_CONFIG_FILE = "magic-folder.properties"; //$NON-NLS-1$

    /** Name of the service to run / create . */
    private static final String SERVICE_NAME = "magic-folder"; //$NON-NLS-1$

    /** RMI url protocol prefix. */
    private static final String RMI_URL_PROTOCOL = "rmi://"; //$NON-NLS-1$

    /**
     * Mapping key for the value of server ID.
     * @see {@link #thisServiceID}
     */
    private static final String ID_KEY = "THIS_ID"; //$NON-NLS-1$

    /**
     * Prefix uses for the mapping key to host values for group working.
     * @see {@link #membersRmiUrls}.
     */
    private static final String HOST_PREFIX_KEY = "HOST"; //$NON-NLS-1$

    /** Mapping key to {@link #inputFolder}. */
    private static final String INPUT_FOLDER_KEY = "INPUT_FOLDER"; //$NON-NLS-1$

    /** Mapping key to {@link #sleepTimeInput}. */
    private static final String INPUT_DELAYTIME_KEY = "INPUT_FOLDER_DELAY_TIME_MS"; //$NON-NLS-1$

    /** Mapping key to {@link #ackFolder}. */
    private static final String RESPONSE_FOLDER_KEY = "ACK_FOLDER"; //$NON-NLS-1$

    /** Mapping key to {@link #ackFolderOk}. */
    private static final String RESPONSE_FOLDER_OK_KEY = "ACK_FOLDER_OK"; //$NON-NLS-1$

    /** Mapping key to {@link #ackFolderFailed}. */
    private static final String RESPONSE_FOLDER_FAILED_KEY = "ACK_FOLDER_FAILED"; //$NON-NLS-1$

    /** Mapping key to {@link #ackOkCmd}. */
    private static final String ACK_FOLDER_OK_PROGRAM_CMD_LINE_KEY = "ACK_FOLDER_OK_PROGRAM_CMD_LINE"; //$NON-NLS-1$

    /** Mapping key to {@link #ackFailedCmd}. */
    private static final String ACK_FOLDER_FAILED_PROGRAM_CMD_LINE_KEY = "ACK_FOLDER_FAILED_PROGRAM_CMD_LINE"; //$NON-NLS-1$

    /** Mapping key to {@link #processedFolder}. */
    private static final String PROCESSED_FOLDER_KEY = "PROCESSED_FOLDER"; //$NON-NLS-1$

    /** Mapping key to {@link #outputFolder}. */
    private static final String OUTPUT_FOLDER_KEY = "OUTPUT_FOLDER"; //$NON-NLS-1$

    /** Mapping key to {@link #backupFolder}. */
    private static final String BACKUP_FOLDER_KEY = "BACKUP_FOLDER"; //$NON-NLS-1$

    /** Mapping key to {@link #instanceID}. */
    private static final String INSTANCE_ID_KEY = "INSTANCE_ID"; //$NON-NLS-1$

    /** Mapping key to {@link #sleepTimeOutput}. */
    private static final String OUTPUT_DELAYTIME_KEY = "OUTPUT_FOLDER_DELAY_TIME_MS"; //$NON-NLS-1$

    /** Mapping key to {@link #numOfDaysKept}. */
    private static final String MAX_FILE_AGE_IN_DAYS = "MAX_FILE_AGE_IN_DAYS"; //$NON-NLS-1$

    /** Mapping key to {@link #messageTypesList}. */
    private static final String MENSSAGE_TYPES_KEY = "OUTPUT_FOLDER_MESSAGE_TYPES"; //$NON-NLS-1$

    /** Mapping key to {@link #fileNameExtension}. */
    private static final String FILE_NAME_EXTENSION_KEY = "OUTPUT_FILE_NAME_EXTENSION"; //$NON-NLS1$ //$NON-NLS-1$

    /** Mapping key to {@link #programCmdLine}. */
    private static final String COMMAND_LINE_KEY = "OUTPUT_PROGRAM_CMD_LINE"; //$NON-NLS-1$

    /** Max. length for {@link #instanceID}. */
    private static final int INSTANCE_ID_MAX_LENGTH = 20;

    /** Mapping key to {@link #maxNumThreads}. */
    private static final String MAX_NUM_THREADS_KEY = "MAX_NUM_THREADS"; //$NON-NLS-1$

    /** Default maxinum number of threads. */
    private static final String DEFAULT_MAX_NUM_THREADS = "5"; //$NON-NLS-1$

    /** Max number of threads. */
    private static final String MAX_NUM_THREADS = "25"; //$NON-NLS-1$
    
    /**
     * Min delay time for loops (input / output).
     * @see {@link #sleepTimeInput}, {@link #sleepTimeOutput}.
     */
    private static final long MIN_SLEEP_TIME = 60000;

    /**
     * Default delay time for loops (input / output) in milliseconds.
     * @see {@link #sleepTimeInput}, {@link #sleepTimeOutput}.
     */
    private static final String DEFAULT_DELAY = "180000"; //$NON-NLS-1$

    /** Default value for {@link #numOfDaysKept}. */
    private static final String DEFAULT_MAX_FILE_AGE_IN_DAYS = "7"; //$NON-NLS-1$

    /** Default file name extension.*/
    private static final String DEFAULT_FILE_NAME_EXTENSION = FILE_NAME_EXTENSION_AUTO;

    /** Default value index. */
    private static final Integer DEFAULT_INDEX_VALUE = new Integer(0);

    /** ID string for this service into farm. */
    private String thisServiceID;

    /** List containing all hosts and ports of service Farm. */
    private final List<String> membersRmiUrls = new ArrayList<>();

    /** Input folder. */
    private Map<Integer, String> inputFolder = new HashMap<>();

    /** Output folder. */
    private Map<Integer, String> outputFolder = new HashMap<>();

    /** Command line for output. */
    private Map<Integer, String> programCmdLine = new HashMap<>();

    /** Processed messages folder. */
    private Map<Integer, String> processedFolder = new HashMap<>();

    /** Folder containing ack responses. */
    private Map<Integer, String> ackFolder = new HashMap<>();

    /** Folder containing OK ack responses. */
    private Map<Integer, String> ackFolderOk = new HashMap<>();

    /** Folder containing FAILED ack responses. */
    private Map<Integer, String> ackFolderFailed = new HashMap<>();

    /** Command line for ack ok. */
    private Map<Integer, String> ackOkCmd = new HashMap<>();

    /** Command line for ack failed. */
    private Map<Integer, String> ackFailedCmd = new HashMap<>();

    /** Backup folder. */
    private String backupFolder = null;

    /** This instance identification. */
    private String instanceID = null;

    /** Sleep time between two input detection cycles. */
    private Map<Integer, String> sleepTimeInput = new HashMap<>();

    /** Sleep time between two output detection cycles. */
    private String sleepTimeOutput;

    /** Number of days that a file is kept in file system before a backup is done. */
    private String numOfDaysKept;

    /** List containing types of messages to be retrieved. */
    private Map<Integer, List<String>> messageTypesList = new HashMap<>();

    /** File name extension to be used when retrieving files. */
    private Map<Integer, String> fileNameExtension = new HashMap<>();

    /** Max number of concurrent threads. */
    private String maxNumThreads;

    /**
     * Validates settings. Checks that paths exist and the timeout period entered is numeric.
     * @throws ConfigException If any validation fails.
     */
    public void validateConfiguration() throws ConfigException {

        StringBuilder msgErr = new StringBuilder();

        validateFolders(msgErr);

        validateDuplications(msgErr);

        validateSleeps(msgErr);

        /* No input, no output: Magic folder has nothing to do!. */
        if (inputFolder.get(DEFAULT_INDEX_VALUE) == null && outputFolder.get(DEFAULT_INDEX_VALUE) == null) {
            msgErr.append("\n").append(Messages.getString("MF_UNABLE_TO_START", INPUT_FOLDER_KEY, OUTPUT_FOLDER_KEY)); //$NON-NLS-1$ //$NON-NLS-2$
        }

        for (String memberRmiUrl : membersRmiUrls) {
            String hostPort = memberRmiUrl.substring(RMI_URL_PROTOCOL.length(), memberRmiUrl.indexOf(SERVICE_NAME) - 1);

            int colonPosition = hostPort.indexOf(":"); //$NON-NLS-1$
            if (colonPosition == -1) {
                msgErr.append("\n").append(Messages.getString("MF_MF_INVALID_MEMBER_URL", hostPort)); //$NON-NLS-1$//$NON-NLS-2$
            } else {
                String port = ""; //$NON-NLS-1$
                try {
                    port = hostPort.substring(colonPosition + 1);
                    Integer.parseInt(port);
                } catch (NumberFormatException ex) {
                    msgErr.append("\n").append(Messages.getString("MF_INVALID_MEMBER_PORT", port)); //$NON-NLS-1$//$NON-NLS-2$
                }
            }
        }

        if (numOfDaysKept != null) {
            try {
                Integer.parseInt(numOfDaysKept);
            } catch (NumberFormatException ex) {
                msgErr.append("\n").append(Messages.getString("MF_INVALID_NUMBER", MAX_FILE_AGE_IN_DAYS, numOfDaysKept)); //$NON-NLS-1$//$NON-NLS-2$
            }
        }

        if (isNotNullAndNotEmpty(maxNumThreads)) {
            try {
                int numThread = Integer.parseInt(maxNumThreads);
                if (numThread < Integer.parseInt(DEFAULT_MAX_NUM_THREADS)) {
                    maxNumThreads = DEFAULT_MAX_NUM_THREADS;
                } 
                if (numThread > Integer.parseInt(MAX_NUM_THREADS)) {
                    maxNumThreads = MAX_NUM_THREADS;
                }
            } catch (NumberFormatException ex) {
                msgErr.append("\n").append(Messages.getString("MF_INVALID_NUMBER", MAX_NUM_THREADS_KEY, maxNumThreads)); //$NON-NLS-1$//$NON-NLS-2$
            }
        }
        
        
        if (thisServiceID != null) {
            try {
                Integer.parseInt(thisServiceID);
            } catch (NumberFormatException ex) {
                msgErr.append("\n").append(Messages.getString("MF_INVALID_NUMBER", ID_KEY, thisServiceID)); //$NON-NLS-1$//$NON-NLS-2$
            }
        }

        if (instanceID != null) {
            try {
                File f = new File(instanceID);
                f.getCanonicalPath();
            } catch (IOException e) {
                msgErr.append("\n").append(Messages.getString("MF_INVALID_ID", INSTANCE_ID_KEY, instanceID)); //$NON-NLS-1$//$NON-NLS-2$
            }

            if (instanceID.length() > INSTANCE_ID_MAX_LENGTH) {
                msgErr.append("\n").append(Messages.getString("MF_INVALID_ID_LENGTH", INSTANCE_ID_KEY, instanceID, INSTANCE_ID_MAX_LENGTH)); //$NON-NLS-1$//$NON-NLS-2$
            }
        }

        if (msgErr.length() > 0) {
            throw new ConfigException(msgErr.toString());
        }
    }

    /** 
     * Validates the realitionship between folders.
     * <li>Input folder cannot be set with the same value of any other response folders.
     * <li>Input folder cannot be set with the same value of any other output folders.
     * <li>Input folder cannot be set with the same value of any other processed folders.
     * <li>Input folder cannot be repeated in other set.
     * @param msgErr String buffer error message.
     */
    private void validateDuplications(final StringBuilder msgErr) {

        Collection<String> inputF;

        /* Input folders and response folders must be different, otherwise would create a loop. */
        inputF = new ArrayList<>(inputFolder.values());
        inputF.retainAll(ackFolder.values());
        if (!inputF.isEmpty()) {
            msgErr.append("\n").append(Messages.getString("MF_INVALID_FOLDER_INPUT_ACK", inputF.toString())); //$NON-NLS-1$ //$NON-NLS-2$
        }

        inputF = new ArrayList<>(inputFolder.values());
        inputF.retainAll(ackFolderOk.values());
        if (!inputF.isEmpty()) {
            msgErr.append("\n").append(Messages.getString("MF_INVALID_FOLDER_INPUT_ACK", inputF.toString())); //$NON-NLS-1$ //$NON-NLS-2$
        }

        inputF = new ArrayList<>(inputFolder.values());
        inputF.retainAll(ackFolderFailed.values());
        if (!inputF.isEmpty()) {
            msgErr.append("\n").append(Messages.getString("MF_INVALID_FOLDER_INPUT_ACK", inputF.toString())); //$NON-NLS-1$ //$NON-NLS-2$
        }

        /* Input folders and output folders must be different, otherwise would create a loop. */
        inputF = new ArrayList<>(inputFolder.values());
        inputF.retainAll(outputFolder.values());
        if (!inputF.isEmpty()) {
            msgErr.append("\n").append(Messages.getString("MF_INVALID_FOLDER_INPUT_OUTPUT", inputF.toString())); //$NON-NLS-1$ //$NON-NLS-2$
        }

        /* Input folders and processed folders must be different, otherwise would create a loop. */
        inputF = new ArrayList<>(inputFolder.values());
        inputF.retainAll(processedFolder.values());
        if (!inputF.isEmpty()) {
            msgErr.append("\n").append(Messages.getString("MF_INVALID_FOLDER_INPUT_PROCESSED", inputF.toString())); //$NON-NLS-1$ //$NON-NLS-2$
        }

        int inputFolderSize = inputFolder.values().size();
        int inputFolderSizeNoDuplicates = new HashSet<>(inputFolder.values()).size();
        if (inputFolderSize != inputFolderSizeNoDuplicates) {
            msgErr.append("\n").append("No se puede especificar la misma carpeta de entrada en varios conjuntos"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Validates sleeps values.
     * @param msgErr String buffer error message.
     */
    private void validateSleeps(final StringBuilder msgErr) {

        for (Integer k : inputFolder.keySet()) {
            String slep = sleepTimeInput.get(k);
            if (slep != null) {
                try {
                    long lo = Long.parseLong(slep);
                    if (lo < MIN_SLEEP_TIME) {
                        if (k == 0) {
                            msgErr.append("\n").append(Messages.getString("MF_VALUE_TOO_SMALL", //$NON-NLS-1$ //$NON-NLS-2$
                                    INPUT_DELAYTIME_KEY, sleepTimeInput, MIN_SLEEP_TIME));
                        } else {
                            msgErr.append("\n").append(Messages.getString("MF_VALUE_TOO_SMALL", //$NON-NLS-1$ //$NON-NLS-2$
                                    INPUT_DELAYTIME_KEY + "_" + k, sleepTimeInput, MIN_SLEEP_TIME)); //$NON-NLS-1$ 
                        }
                    }
                } catch (NumberFormatException e) {
                    if (k == 0) {
                        msgErr.append("\n").append(Messages.getString("MF_INVALID_NUMBER", //$NON-NLS-1$//$NON-NLS-2$
                                INPUT_DELAYTIME_KEY, sleepTimeInput));
                    } else {
                        msgErr.append("\n").append(Messages.getString("MF_INVALID_NUMBER", //$NON-NLS-1$//$NON-NLS-2$
                                INPUT_DELAYTIME_KEY + "_" + k, sleepTimeInput)); //$NON-NLS-1$ 
                    }
                }
            }
        }

        if (sleepTimeOutput != null) {
            try {
                long slep = Long.parseLong(sleepTimeOutput);
                if (slep < MIN_SLEEP_TIME) {
                    msgErr.append("\n").append(Messages.getString("MF_VALUE_TOO_SMALL", OUTPUT_DELAYTIME_KEY, sleepTimeOutput, MIN_SLEEP_TIME)); //$NON-NLS-1$//$NON-NLS-2$
                }
            } catch (NumberFormatException ex) {
                msgErr.append("\n").append(Messages.getString("MF_INVALID_NUMBER", OUTPUT_DELAYTIME_KEY, sleepTimeOutput)); //$NON-NLS-1$//$NON-NLS-2$
            }
        }

    }

    /**
     * Validates all the configured folders.
     * @param msgErr Error message buffer.
     */
    private void validateFolders(final StringBuilder msgErr) {
        validateFolder(msgErr, inputFolder, INPUT_FOLDER_KEY);
        validateFolder(msgErr, outputFolder, OUTPUT_FOLDER_KEY);
        validateFolder(msgErr, processedFolder, PROCESSED_FOLDER_KEY);
        validateFolder(msgErr, ackFolder, RESPONSE_FOLDER_KEY);
        validateFolder(msgErr, ackFolderOk, RESPONSE_FOLDER_OK_KEY);
        validateFolder(msgErr, ackFolderFailed, RESPONSE_FOLDER_FAILED_KEY);
        
        if (isNotNullAndNotEmpty(backupFolder)) {
            Map<Integer, String> temp = new HashMap<>();
            temp.put(DEFAULT_INDEX_VALUE, backupFolder);
            validateFolder(msgErr, temp, BACKUP_FOLDER_KEY);
            backupFolder = temp.get(DEFAULT_INDEX_VALUE);
        }
    }

    /**
     * Checks the given set of paths are existent directories and not a file.
     * @param msgErr Error message container to be modified in case of entered path is a nonexistent path or a file.
     * @param folders Absolute path to the folder.
     * @param folderID Type of folder to be validated.
     */
    private void validateFolder(final StringBuilder msgErr, final Map<Integer, String> folders, final String folderID) {

        for (Integer k : folders.keySet()) {
            String folderPath = folders.get(k);
            if (folderPath != null) {
                File f = new File(folderPath);
                if (f.isDirectory()) {
                    folderPath = folderPath.trim().replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
                    if (folderPath.endsWith("/")) { //$NON-NLS-1$
                        folderPath = folderPath.substring(0, folderPath.length() - 1);
                    }
                    folders.put(k, folderPath);
                } else {
                    if (k.equals(DEFAULT_INDEX_VALUE)) {
                        msgErr.append("\n").append(Messages.getString("MF_INVALID_FOLDER", folderID, folderPath)); //$NON-NLS-1$//$NON-NLS-2$
                    } else {
                        msgErr.append("\n").append(Messages.getString("MF_INVALID_FOLDER", folderID + "_" + k.toString(), folderPath)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                }
            }
        }
    }

    /**
     * Gets maximun number of threads.
     * @return Maximun number of threads.
     */
    public int getMaxNumThreads() {
        return Integer.parseInt(maxNumThreads);
    }

    /**
     * Return the input folder for the given index.
     * @param pos Index.
     * @return Input folder for the given index.
     */
    public String getInputFolder(final Integer pos) {
        return inputFolder.get(pos);
    }

    /**
     * Sets input folder for 0 index..
     * @param folder Input folder for 0 index.
     */
    public void setInputFolder(final String folder) {
        if (folder != null) {
            inputFolder.put(DEFAULT_INDEX_VALUE, folder);
        }
    }

    /**
     * Return instance ID.
     * @return Instance ID <code>null</code> if undefined.
     */
    public String getInstanceID() {
        return instanceID;
    }

    /**
     * Return response folder for given index.
     * @param pos Index.
     * @return Response folder for given index.
     */
    public String getResponseFolder(final Integer pos) {
        return ackFolder.get(pos);
    }

    /**
     * Return response "OK" folder for given index.
     * @param pos Index.
     * @return Response "OK" folder for given index.
     */
    public String getResponseOkFolder(final Integer pos) {
        return ackFolderOk.get(pos);
    }

    /**
     * Return response "FAILED" folder for given index.
     * @param pos Index.
     * @return Response "FAILED" folder for given index.
     */
    public String getResponseFailedFolder(final Integer pos) {
        return ackFolderFailed.get(pos);
    }

    /**
     * Set response folder for 0 index.
     * @param folder Response folder for 0 index.
     */
    public void setResponseFolder(final String folder) {
        if (folder != null) {
            ackFolder.put(DEFAULT_INDEX_VALUE, folder);
        }
    }

    /**
     * Return output folder for given index.
     * @param pos Index.
     * @return Output folder for given index.
     */
    public String getOutputFolder(final Integer pos) {
        return outputFolder.get(pos);
    }
    
    /**
     * Sets default (index=0) output folder.
     * @param folder Folder to be set.
     */
    public void setOutputFolder(final String folder) {
        if (folder != null) {
            outputFolder.put(DEFAULT_INDEX_VALUE, folder);
        }
    }

    /**
     * Return processed folder for given index.
     * @param pos Index.
     * @return Processed folder for given index.
     */
    public String getProcessedFolder(final Integer pos) {
        return processedFolder.get(pos);
    }

    /**
     * Sets processed folder for index 0.
     * @param folder Processed folder for index 0.
     */
    public void setProcessedFolder(final String folder) {
        if (folder != null) {
            processedFolder.put(DEFAULT_INDEX_VALUE, folder);
        }
    }

    /**
     * Gets backup folder.
     * @return Backup folder.
     */
    public String getBackupFolder() {
        String retValue = null;
        if (isNotNullAndNotEmpty(backupFolder)) {
            retValue = backupFolder;
        }
        
        return retValue;
    }
    
    /**
     * Returns the configured backup folder or <code>null</code> if there is no backup folder.
     * @param folder Returns the configured backup folder or <code>null</code> if there is no backup folder.
     */
    public void setBackupFolder(final String folder) {
        backupFolder = folder;
    }

    /**
     * Gets delay time between two input message detection cycles for given index.
     * @param pos Configuration position index.
     * @return Delay time between two input detection cycles in milliseconds.
     */
    public long getSleepTimeInput(final Integer pos) {

        String str = sleepTimeInput.get(pos);
        if (str == null) {
            str = DEFAULT_DELAY;
        }

        return Long.parseLong(str);
    }

    /**
     * Returns delay time between two output message detection cycles.
     * @return Delay time between two output message detection cycles.
     */
    public long getSleepTimeOutput() {
        return Long.parseLong(sleepTimeOutput);
    }

    /**
     * Returns number of days that a file is kept in file system.
     * @return Number of days that a file is kept in file system.
     */
    public int getNumOfDaysKept() {
        return Integer.parseInt(numOfDaysKept);
    }

    /**
     * Returns ID string for this service.
     * @return ID string for this service.
     */
    public String getServiceID() {
        return thisServiceID;
    }

    /**
     * Returns an array containing info about all hosts and ports.
     * @return ArrayList containing info about all members rmi urls.
     */
    public List<String> getMembersRmiUrls() {
        return membersRmiUrls;
    }

    /**
     * Returns a list of message types to retrieve for given index. If returned value is <code>null</code> all available messages will be
     * retrieved.
     * @param pos Index.
     * @return List of message types to retrieve. <code>null</code> to retrieve all available messages.
     */
    public List<String> getMessagesTypeList(final Integer pos) {
        return messageTypesList.get(pos);
    }

    /**
     * Returns the filename extension to be used when retrieving files.
     * Possible values are:
     * NONE: no file name extension will be used.
     * AUTO: create a filename extesion according to the content.
     * XXXX: use "XXXX" filename extension.
     * @param pos Index.
     * @return file name extension for the given index.
     */
    public String getFileNameExtension(final Integer pos) {
        String str = fileNameExtension.get(pos);
        if (str == null) {
            str = DEFAULT_FILE_NAME_EXTENSION;
        }
        return str;
    }

    /**
     * Sets the list of desired messages to retrieve.
     * @param cm ConfigManager with all the configuration information.
     * @param keys Keys to search in the configuration.
     */
    private void setMessagesTypeList(final ConfigManager cm, final Set<Integer> keys) {
        Map<Integer, String> map = new HashMap<>();
        readMappedValue(MENSSAGE_TYPES_KEY, map, cm, keys);

        for (Integer k : map.keySet()) {
            String str = map.get(k);
            messageTypesList.put(k, Arrays.asList(str.split(";"))); //$NON-NLS-1$
        }
    }

    /**
     * Reads a configuration value and store its value in the given map.
     * First configuration name has no index:  "CONFIG_NAME" 
     * If there are other values, these must have an index starting from 1: "CONFIG_NAME_1", "CONFIG_NAME_2", etc.
     * @param key Configuration name key.
     * @param map Map to store the configuration values.
     * @param cm Configuration set.
     */
    private void readMappedValue(final String key, final Map<Integer, String> map, final ConfigManager cm) {
        String value = cm.getValue(key);
        if (isNotNullAndNotEmpty(value)) {
            map.put(DEFAULT_INDEX_VALUE, value);

            int cont = 1;
            do {
                value = cm.getValue(key + "_" + cont); //$NON-NLS-1$
                if (isNotNullAndNotEmpty(value)) {
                    map.put(new Integer(cont), value);
                }
                cont++;
            } while (value != null);
        }
    }

    /**
     * Reads configuration values for no index element and for those elements that has the same index as keys has.
     * @param key Configuration name key.
     * @param map Map to store the configuration values.
     * @param cm Configuration set.
     * @param keys Index set to be read.
     */
    private void readMappedValue(final String key, final Map<Integer, String> map, final ConfigManager cm, final Set<Integer> keys) {
        String value = cm.getValue(key);
        if (isNotNullAndNotEmpty(value)) {
            map.put(DEFAULT_INDEX_VALUE, value);
        }

        for (int k : keys) {
            value = cm.getValue(key + "_" + k); //$NON-NLS-1$
            if (isNotNullAndNotEmpty(value)) {
                map.put(k, value);
            }
        }
    }
    
    /**
     * Returns <code>true</code> if the provided value is not null
     * and not empty.
     * @param value Value to be checked.
     * @return <code>true</code> if the provided value is not null
     * and not empty.
     */
    private boolean isNotNullAndNotEmpty(final String value) {
        return value != null && !value.trim().isEmpty();
    }
 
    /**
     * Reads configuration file values.
     * @throws ConfigException If any of the required files cannot be read.
     */
    @Override
    public void readConfiguration() throws ConfigException {

        super.readConfiguration();

        ConfigManager cm = new ConfigManager();
        cm.readConfigFile(FOLDER_CONFIG_FILE);

        instanceID = cm.getValue(INSTANCE_ID_KEY);
        if (instanceID != null && instanceID.trim().isEmpty()) {
            instanceID = null;
        } else {
            StatusIcon.setIdentification(instanceID);
        }

        readMappedValue(INPUT_FOLDER_KEY, inputFolder, cm);
        Set<Integer> inputSet = inputFolder.keySet();
        readMappedValue(RESPONSE_FOLDER_KEY, ackFolder, cm, inputSet);
        readMappedValue(PROCESSED_FOLDER_KEY, processedFolder, cm, inputSet);
        readMappedValue(INPUT_DELAYTIME_KEY, sleepTimeInput, cm, inputSet);
        readMappedValue(RESPONSE_FOLDER_OK_KEY, ackFolderOk, cm, inputSet);
        readMappedValue(RESPONSE_FOLDER_FAILED_KEY, ackFolderFailed, cm, inputSet);
        readMappedValue(ACK_FOLDER_OK_PROGRAM_CMD_LINE_KEY, ackOkCmd, cm, inputSet);
        readMappedValue(ACK_FOLDER_FAILED_PROGRAM_CMD_LINE_KEY, ackFailedCmd, cm, inputSet);

        readMappedValue(OUTPUT_FOLDER_KEY, outputFolder, cm);
        Set<Integer> outputSet = outputFolder.keySet();
        readMappedValue(FILE_NAME_EXTENSION_KEY, fileNameExtension, cm, outputSet);
        readMappedValue(COMMAND_LINE_KEY, programCmdLine, cm, outputSet);
        setMessagesTypeList(cm, outputSet);

        backupFolder = cm.getValue(BACKUP_FOLDER_KEY);
        sleepTimeOutput = cm.getValue(OUTPUT_DELAYTIME_KEY, DEFAULT_DELAY);
        numOfDaysKept = cm.getValue(MAX_FILE_AGE_IN_DAYS, DEFAULT_MAX_FILE_AGE_IN_DAYS);

        maxNumThreads = cm.getValue(MAX_NUM_THREADS_KEY, DEFAULT_MAX_NUM_THREADS);

        boolean loop = true;
        for (int count = 1; loop; count++) {
            String hostAndPort = cm.getValue(HOST_PREFIX_KEY + "_" + count); //$NON-NLS-1$
            if (hostAndPort != null) {
                membersRmiUrls.add(RMI_URL_PROTOCOL + hostAndPort + File.separator + SERVICE_NAME);
            } else {
                loop = false;
            }
        }

        thisServiceID = cm.getValue(ID_KEY);
    }

    /**
     * Updates configuration file.
     * @throws ConfigException If cannot update file (File access error).
     */
    @Override
    public void writeConfiguration() throws ConfigException {

        ConfigManager cm = new ConfigManager();
        cm.readConfigFile(FOLDER_CONFIG_FILE);

        try {
            String fullPathConfig = FileUtil.getFullPathOfResoruce(FOLDER_CONFIG_FILE);

            String fileContent = FileUtil.read(fullPathConfig);
            fileContent = writeValue(inputFolder.get(DEFAULT_INDEX_VALUE), cm.getValue(INPUT_FOLDER_KEY), INPUT_FOLDER_KEY, fileContent);
            fileContent = writeValue(outputFolder.get(DEFAULT_INDEX_VALUE), cm.getValue(OUTPUT_FOLDER_KEY), OUTPUT_FOLDER_KEY, fileContent);
            fileContent = writeValue(backupFolder, cm.getValue(BACKUP_FOLDER_KEY), BACKUP_FOLDER_KEY, fileContent);
            fileContent = writeValue(processedFolder.get(DEFAULT_INDEX_VALUE), cm.getValue(PROCESSED_FOLDER_KEY), PROCESSED_FOLDER_KEY, fileContent);
            fileContent = writeValue(ackFolder.get(DEFAULT_INDEX_VALUE), cm.getValue(RESPONSE_FOLDER_KEY), RESPONSE_FOLDER_KEY, fileContent);

            FileUtil.createBackup(fullPathConfig);
            FileUtil.write(fullPathConfig, fileContent);
        } catch (IOException ex) {
            throw new ConfigException(ex);
        }
    }

    /**
     * Gets the command line to be executed for the given index.
     * @param index Index value to be retrieved
     * @return Command line to be executed by provided index. <code>null</code> 
     * if no command line was specified.
     */
    public String getProgramCmdLine(final int index) {
        return programCmdLine.get(index);
    }

    /**
     * Gets the command line to be executed for ack ok for the given index.
     * @param index Index value to be retrieved.
     * @return Command line to be executed for ack ok for the given index.
     * <code>null</code> if no command line was specified.
     */
    public String getAckOkProgramCmdLIne(final int index) {
        return ackOkCmd.get(index);
    }

    /**
     * Gets the command line to be executed for ack failed for the given index.
     * @param index Index value to be retrieved.
     * @return Command line to be executed for ack failed for the given index.
     * <code>null</code> if no command line was specified.
     */
    public String getAckFailedProgramCmdLine(final int index) {
        return ackFailedCmd.get(index);
    }

}

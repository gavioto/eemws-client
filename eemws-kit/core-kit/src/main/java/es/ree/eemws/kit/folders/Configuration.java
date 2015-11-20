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
package es.ree.eemws.kit.folders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.core.utils.config.ConfigManager;
import es.ree.eemws.core.utils.file.FileUtil;
import es.ree.eemws.kit.common.Messages;

/**
 * Magic folder configuration settings.
 * 
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class Configuration extends es.ree.eemws.kit.config.Configuration {

    /** File name extension "auto" */
    public static final String FILE_NAME_EXTENSION_AUTO = "AUTO";

    /** File name extension "none" */
    public static final String FILE_NAME_EXTENSION_NONE = "NONE";

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
    private static final String HOST_PREFIX_KEY = "HOST_"; //$NON-NLS-1$

    /** Mapping key to {@link #inputFolder}. */
    private static final String INPUT_FOLDER_KEY = "INPUT_FOLDER"; //$NON-NLS-1$

    /** Mapping key to {@link #sleepTimeInput}. */
    private static final String INPUT_DELAYTIME_KEY = "INPUT_FOLDER_DELAY_TIME_MS"; //$NON-NLS-1$

    /** Mapping key to {@link #ackFolder}. */
    private static final String RESPONSE_FOLDER_KEY = "ACK_FOLDER"; //$NON-NLS-1$

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
    private static final String MENSSAGE_TYPES_KEY = "OUTPUT_FOLDER_MENSSAGE_TYPES"; //$NON-NLS-1$

    /** Mapping key to {@link #fileNameExtension}. */
    private static final String FILE_NAME_EXTENSION_KEY = "OUTPUT_FILE_NAME_EXTENSION"; //$NON-NLS1$

    /** Max. length for {@link #instanceID}. */
    private static final int INSTANCE_ID_MAX_LENGTH = 20;

    /**
     * Min delay time for loops (input / output).
     * @see {@link #sleepTimeInput}, {@link #sleepTimeOutput}.
     */
    private static final long MIN_SLEEP_TIME = 30000;

    /**
     * Default delay time for loops (input / output).
     * @see {@link #sleepTimeInput}, {@link #sleepTimeOutput}.
     */
    private static final String DEFAULT_DELAY = "60000"; //$NON-NLS-1$

    /** Default value for {@link #numOfDaysKept}. */
    private static final String DEFAULT_MAX_FILE_AGE_IN_DAYS = "7"; //$NON-NLS-1$

    /** Default file name extension.*/
    private static final String DEFAULT_FILE_NAME_EXTENSION = FILE_NAME_EXTENSION_AUTO;

    /** ID string for this service into farm. */
    private String thisServiceID;

    /** List containing all hosts and ports of service Farm. */
    private final List<String> membersRmiUrls = new ArrayList<String>();

    /** Input folder. */
    private String inputFolder = null;

    /** Output folder. */
    private String outputFolder = null;

    /** Processed items folder. */
    private String processedFolder = null;

    /** Folder containing ack responses. */
    private String ackFolder = null;

    /** Backup folder. */
    private String backupFolder = null;

    /** Identification of this instance. */
    private String instanceID = null;

    /** Sleep time between two input detection cycles. */
    private String sleepTimeInput;

    /** Sleep time between two output detection cycles. */
    private String sleepTimeOutput;

    /** Number of days that a file is kept in file system before a backup is done. */
    private String numOfDaysKept;

    /** List containing types of messages to be retrieved. */
    private List<String> messageTypesList = null;

    /** File name extension to be used when retrieving files. */
    private String fileNameExtension = null;

    /**
     * Validates detection settings without considering common settings. Check that paths to entered folders exist and
     * the timeout period entered is numeric.
     * @throws ConfigException If any validation fails.
     */
    public void validateConfiguration() throws ConfigException {

        StringBuilder msgErr = new StringBuilder();

        inputFolder = validateFolder(msgErr, inputFolder, INPUT_FOLDER_KEY);
        outputFolder = validateFolder(msgErr, outputFolder, OUTPUT_FOLDER_KEY);
        processedFolder = validateFolder(msgErr, processedFolder, PROCESSED_FOLDER_KEY);
        ackFolder = validateFolder(msgErr, ackFolder, RESPONSE_FOLDER_KEY);
        backupFolder = validateFolder(msgErr, backupFolder, BACKUP_FOLDER_KEY);

        if (inputFolder == null && (processedFolder != null || ackFolder != null)) {
            msgErr.append("\n").append(Messages.getString("MF_ACK_FOLDER_WITHOUT_INPUT", PROCESSED_FOLDER_KEY, RESPONSE_FOLDER_KEY, INPUT_FOLDER_KEY)); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (inputFolder != null && processedFolder != null && inputFolder.equals(processedFolder)) {
            msgErr.append("\n").append(Messages.getString("MF_INVALID_FOLDER_PATH", INPUT_FOLDER_KEY, PROCESSED_FOLDER_KEY)); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (inputFolder != null && processedFolder != null && inputFolder.equals(processedFolder)) {
            msgErr.append("\n").append(Messages.getString("MF_INVALID_FOLDER_PATH", INPUT_FOLDER_KEY, RESPONSE_FOLDER_KEY)); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (inputFolder != null && outputFolder != null && inputFolder.equals(outputFolder)) {
            msgErr.append("\n").append(Messages.getString("MF_INVALID_FOLDER_PATH", INPUT_FOLDER_KEY, OUTPUT_FOLDER_KEY)); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (inputFolder == null && outputFolder == null) {
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

        if (sleepTimeInput != null) {
            try {
                long slep = Long.parseLong(sleepTimeInput);
                if (slep < MIN_SLEEP_TIME) {
                    msgErr.append("\n").append(Messages.getString("MF_VALUE_TOO_SMALL", INPUT_DELAYTIME_KEY, sleepTimeInput, MIN_SLEEP_TIME)); //$NON-NLS-1$//$NON-NLS-2$
                }
            } catch (NumberFormatException ex) {
                msgErr.append("\n").append(Messages.getString("MF_INVALID_NUMBER", INPUT_DELAYTIME_KEY, sleepTimeInput)); //$NON-NLS-1$//$NON-NLS-2$
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

        if (numOfDaysKept != null) {
            try {
                Integer.parseInt(numOfDaysKept);
            } catch (NumberFormatException ex) {
                msgErr.append("\n").append(Messages.getString("MF_INVALID_NUMBER", MAX_FILE_AGE_IN_DAYS, numOfDaysKept)); //$NON-NLS-1$//$NON-NLS-2$
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
     * Checks the given path is an existent directory and not a file.
     * @param msgErr Error message container to be modified in case of entered path is a nonexistent path or a file.
     * @param folderPath Absolute path to the folder.
     * @param folderID Type of folder to be validated.
     * @return Path without spaces, replacing "\" by "/" and removing the last occurrence of "\" if necessary.
     */
    private String validateFolder(final StringBuilder msgErr, final String folderPath, final String folderID) {

        String newFolderID = folderPath;

        if (folderPath != null) {
            File f = new File(folderPath);
            if (f.isDirectory()) {
                newFolderID = newFolderID.trim().replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
                if (newFolderID.endsWith("/")) { //$NON-NLS-1$
                    newFolderID = newFolderID.substring(0, newFolderID.length() - 1);
                }
            } else {
                msgErr.append("\n").append(Messages.getString("MF_INVALID_FOLDER", folderID, folderPath)); //$NON-NLS-1$//$NON-NLS-2$
            }
        }

        return newFolderID;
    }

    /**
     * Return the input folder.
     * @return Entered input folder.
     */
    public String getInputFolder() {
        return inputFolder;
    }

    /**
     * Set input folder.
     * @param folder Input folder.
     */
    public void setInputFolder(final String folder) {
        inputFolder = folder;
    }

    /**
     * Return instance ID.
     * @return Instance ID <code>null</code> if undefined.
     */
    public String getInstanceID() {
        return instanceID;
    }

    /**
     * Return response folder.
     * @return Response folder.
     */
    public String getResponseFolder() {
        return ackFolder;
    }

    /**
     * Set response folder.
     * @param folder Response folder.
     */
    public void setResponseFolder(final String folder) {
        ackFolder = folder;
    }

    /**
     * Return output folder.
     * @return Output folder.
     */
    public String getOutputFolder() {
        return outputFolder;
    }

    /**
     * Set output folder.
     * @param folder Output folder.
     */
    public void setOutputFolder(final String folder) {
        outputFolder = folder;
    }

    /**
     * Return processed folder.
     * @return Processed folder.
     */
    public String getProcessedFolder() {
        return processedFolder;
    }

    /**
     * Set processed folder.
     * @param folder processed folder.
     */
    public void setProcessedFolder(final String folder) {
        processedFolder = folder;
    }

    /**
     * Return backup folder.
     * @return Backup folder.
     */
    public String getBackupFolder() {
        return backupFolder;
    }

    /**
     * Set backup folder.
     * @param folder Backup folder.
     */
    public void setBackupFolder(final String folder) {
        backupFolder = folder;
    }

    /**
     * Return delay time between two input message detection cycles.
     * @return Delay time between two input detection cycles in milliseconds.
     */
    public long getSleepTimeInput() {
        return Long.parseLong(sleepTimeInput);
    }

    /**
     * Return delay time between two output message detection cycles.
     * @return Delay time between two output message detection cycles.
     */
    public long getSleepTimeOutput() {
        return Long.parseLong(sleepTimeOutput);
    }

    /**
     * Return number of days that a file is kept in file system.
     * @return Number of days that a file is kept in file system.
     */
    public int getNumOfDaysKept() {
        return Integer.parseInt(numOfDaysKept);
    }

    /**
     * Return ID string for this service.
     * @return ID string for this service.
     */
    public String getServiceID() {
        return thisServiceID;
    }

    /**
     * Return array containing info about all hosts and ports.
     * @return ArrayList containing info about all members rmi urls.
     */
    public List<String> getMembersRmiUrls() {
        return membersRmiUrls;
    }

    /**
     * Return list of message types to retrieve. If returned value is <code>null</code> all available messages will be
     * retrieved.
     * @return List of message types to retrieve. <code>null</code> to retrieve all available messages.
     */
    public List<String> getMessagesTypeList() {
        return messageTypesList;
    }

    /**
     * Set the list of desired messages to retrieve.
     * @param typeList list of desired messages to retrieve. Message types are separated by semicolon ";".
     */
    public void setMessagesTypeList(final String typeList) {
        String strAux = typeList.replaceAll(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$
        if (!strAux.trim().isEmpty()) {
            messageTypesList = Arrays.asList(strAux.split(";")); //$NON-NLS-1$
        }
    }

    /**
     * Read configuration file values.
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

        inputFolder = cm.getValue(INPUT_FOLDER_KEY);
        if (inputFolder != null && inputFolder.trim().isEmpty()) {
            inputFolder = null;
        }

        outputFolder = cm.getValue(OUTPUT_FOLDER_KEY);
        if (outputFolder != null && outputFolder.trim().isEmpty()) {
            outputFolder = null;
        }

        ackFolder = cm.getValue(RESPONSE_FOLDER_KEY);
        if (ackFolder != null && ackFolder.trim().isEmpty()) {
            ackFolder = null;
        }

        processedFolder = cm.getValue(PROCESSED_FOLDER_KEY);
        if (processedFolder != null && processedFolder.trim().isEmpty()) {
            processedFolder = null;
        }

        backupFolder = cm.getValue(BACKUP_FOLDER_KEY);
        if (backupFolder != null && backupFolder.trim().isEmpty()) {
            backupFolder = null;
        }

        fileNameExtension = cm.getValue(FILE_NAME_EXTENSION_KEY, DEFAULT_FILE_NAME_EXTENSION);
        sleepTimeInput = cm.getValue(INPUT_DELAYTIME_KEY, DEFAULT_DELAY);
        sleepTimeOutput = cm.getValue(OUTPUT_DELAYTIME_KEY, DEFAULT_DELAY);
        numOfDaysKept = cm.getValue(MAX_FILE_AGE_IN_DAYS, DEFAULT_MAX_FILE_AGE_IN_DAYS);

        setMessagesTypeList(cm.getValue(MENSSAGE_TYPES_KEY, "")); //$NON-NLS-1$

        boolean loop = true;
        for (int count = 1; loop; count++) {
            String hostAndPort = cm.getValue(HOST_PREFIX_KEY + count);
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
            fileContent = writeValue(inputFolder, cm.getValue(INPUT_FOLDER_KEY), INPUT_FOLDER_KEY, fileContent);
            fileContent = writeValue(outputFolder, cm.getValue(OUTPUT_FOLDER_KEY), OUTPUT_FOLDER_KEY, fileContent);
            fileContent = writeValue(backupFolder, cm.getValue(BACKUP_FOLDER_KEY), BACKUP_FOLDER_KEY, fileContent);
            fileContent = writeValue(processedFolder, cm.getValue(PROCESSED_FOLDER_KEY), PROCESSED_FOLDER_KEY, fileContent);
            fileContent = writeValue(ackFolder, cm.getValue(RESPONSE_FOLDER_KEY), RESPONSE_FOLDER_KEY, fileContent);

            FileUtil.createBackup(fullPathConfig);
            FileUtil.write(fullPathConfig, fileContent);
        } catch (IOException ex) {
            throw new ConfigException(ex);
        }
    }

    /**
     * Returns the filename extension to be used when retrieving files.
     * Possible values are:
     * NONE: no file name extension will be used.
     * AUTO: create a filename extesion according to the content.
     * XXXX: use "XXXX" filename extension
     */
    public String getFileNameExtension() {
        return fileNameExtension;
    }
}

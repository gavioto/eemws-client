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
import java.text.MessageFormat;
import java.util.ArrayList;

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.core.utils.config.ConfigManager;
import es.ree.eemws.core.utils.file.FileUtil;
import es.ree.eemws.kit.common.Messages;


/**
 * Read and validate folder settings.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class Configuration extends es.ree.eemws.kit.config.Configuration {

    /** Settings file for Folder manager. */
    private static final String FOLDER_CONFIG_FILE = "folders.properties";

    /**
     * Mapping key for the value of server ID.
     * @see {@link #thisServiceID}
     */
    private static final String ID_KEY = "THIS_ID";

    /**
     * Prefix uses for the mapping key to host values for group working.
     * @see {@link #serviceFarm}.
     */
    private static final String HOST_PREFIX_KEY = "HOST_";

    /** Mapping key to {@link #inputFolder}. */
    private static final String INPUT_FOLDER_KEY = "INPUT_FOLDER";

    /** Mapping key to {@link #responseFolder}. */
    private static final String RESPONSE_FOLDER_KEY = "RESPONSE_FOLDER";

    /** Mapping key to {@link #processedFolder}. */
    private static final String PROCESSED_FOLDER_KEY = "PROCESSED_FOLDER";

    /** Mapping key to {@link #outputFolder}. */
    private static final String OUTPUT_FOLDER_KEY = "OUTPUT_FOLDER";

    /** Mapping key to {@link #backupFolder}. */
    private static final String BACKUP_FOLDER_KEY = "BACKUP_FOLDER";

    /** Mapping key to {@link #instanceID}. */
    private static final String INSTANCE_ID_KEY = "INSTANCE_ID";

    /** Mapping key to {@link #sleepTimeInput}. */
    private static final String INPUT_DELAYTIME_KEY = "INPUT.DELAYTIME";

    /** Mapping key to {@link #sleepTimeOutput}. */
    private static final String OUTPUT_DELAYTIME_KEY = "OUTPUT.DELAYTIME";

    /** Mapping key to {@link #numOfDaysKept}. */
    private static final String NUM_OF_DAYS_KEEP_KEPT = "NUM_OF_DAYS_KEEP";

    /** Mapping key to {@link #messageTypesList}. */
    private static final String MENSSAGE_TYPES_KEY = "MENSSAGE_TYPES";

    /** Max. length for {@link #instanceID}. */
    private static final int INSTANCE_ID_MAX_LENGTH = 20;

    /**
     * Default delay time for detection cycles (input / output).
     * @see {@link #sleepTimeInput}, {@link #sleepTimeOutput}.
     */
    private static final String DEFAULT_DELAY = "60000";

    /** Default value for {@link #numOfDaysKept}. */
    private static final String DEFAULT_NUM_OF_DAYS_KEEP = "7";

    /** ID string for this service into farm. */
    private String thisServiceID;

    /** Vector containing all hosts and ports of service Farm. */
    private final ArrayList<String> serviceFarm = new ArrayList<String>();

    /** Input folder for system. */
    private String inputFolder = null;

    /** Folder containing input responses and some publications. */
    private String outputFolder = null;

    /** Processed items folder. */
    private String processedFolder = null;

    /** Folder containing send responses. */
    private String responseFolder = null;

    /** backup folder. */
    private String backupFolder = null;

    /** ID string for the current instance. */
    private String instanceID = null;

    /** Waiting time between two input detection cycles. */
    private String sleepTimeInput;

    /** Waiting time between two output detection cycles. */
    private String sleepTimeOutput;

    /** Number of days that a file is kept in file system before a backup is done. */
    private String numOfDaysKept;

    /** List containing types of messages to be retrieved. */
    private String messageTypesList;

    /**
     * Validate folder setting values.
     * @throws ConfigException If settings values are incorrect.
     */
    public void validateConfiguration() throws ConfigException {
        validateFolderSettings();
    }

    /**
     * Validates detection settings without considering common settings. Check
     * that paths to entered folders exist and the timeout period entered is numeric.
     * @throws ConfigException If any validation fails.
     */
    public void validateFolderSettings() throws ConfigException {

        StringBuffer msgErr = new StringBuffer();

        inputFolder = validateFolder(msgErr, inputFolder, "input");
        outputFolder = validateFolder(msgErr, outputFolder, "output");
        processedFolder = validateFolder(msgErr, processedFolder, "processed");
        responseFolder = validateFolder(msgErr, responseFolder, "response");
        backupFolder = validateFolder(msgErr, backupFolder, "backup");

        if (inputFolder == null
                && (processedFolder != null || responseFolder != null)) {
            msgErr.append("\n" + Messages.getString("kit.folder.0"));
        }

        if (inputFolder != null && processedFolder != null && inputFolder.equals(processedFolder)) {
            msgErr.append("\n" + Messages.getString("kit.folder.1"));
        }

        if (inputFolder != null && responseFolder != null && inputFolder.equals(responseFolder)) {
            msgErr.append("\n" + Messages.getString("kit.folder.2"));
        }

        if (inputFolder != null && outputFolder != null && inputFolder.equals(outputFolder)) {
            msgErr.append("\n" + Messages.getString("kit.folder.3"));
        }

        if (inputFolder == null && outputFolder == null) {
            msgErr.append("\n" + Messages.getString("kit.folder.4"));
        }

        for (String serverURL : serviceFarm) {
            String port = "";
            int colonPosition = serverURL.indexOf(":");
            if (colonPosition == -1) {
                Object[] paramsText = {serverURL};
                String msg = MessageFormat.format(Messages.getString("kit.folder.5"), paramsText);
                msgErr.append("\n" + msg);
            } else {
                try {
                    port = serverURL.substring(colonPosition + 1);
                    Integer.parseInt(port);
                } catch (NumberFormatException ex) {
                    Object[] paramsText = {port};
                    String msg = MessageFormat.format(Messages.getString("kit.folder.6"), paramsText);
                    msgErr.append("\n" + msg);
                }
            }
        }

        try {
            Long.parseLong(sleepTimeInput);
        } catch (NumberFormatException ex) {
            msgErr.append("\n" + Messages.getString("kit.folder.7"));
        }

        try {
            Long.parseLong(sleepTimeOutput);
        } catch (NumberFormatException ex) {
            msgErr.append("\n" + Messages.getString("kit.folder.8"));
        }

        try {
            Integer.parseInt(numOfDaysKept);
        } catch (NumberFormatException ex) {
            msgErr.append("\n" + Messages.getString("kit.folder.9"));
        }

        if (thisServiceID != null) {
            try {
                Integer.parseInt(thisServiceID);
            } catch (NumberFormatException ex) {
                msgErr.append("\n" + Messages.getString("kit.folder.10"));
            }
        }

        if (instanceID != null) {
            if (instanceID.indexOf('*') != -1 || instanceID.indexOf('?') != -1) {
                msgErr.append("\n" + Messages.getString("kit.folder.11"));
            }

            if (instanceID.length() > INSTANCE_ID_MAX_LENGTH) {
                Object[] paramsText = {INSTANCE_ID_MAX_LENGTH};
                String msg = MessageFormat.format(Messages.getString("kit.folder.12"), paramsText);
                msgErr.append(msg);
            }
        }

        if (msgErr.length() > 0) {
            throw new ConfigException(msgErr.toString());
        }
    }

    /**
     * Check that the entered path the is a directory and not a file.
     * @param msgErr
     *            Error message container to be modified in case of entered path
     *            is a nonexistent path or a file.
     * @param folderPath
     *            Absolute path to the folder.
     * @param folderID
     *            Type of folder to be validated.
     * @return Path without spaces, replacing "\" by "/" and removing the last
     *         occurrence of "\" if necessary.
     */
    private String validateFolder(final StringBuffer msgErr, final String folderPath, final String folderID) {

        String newFolderID = folderPath;

        if (folderPath != null) {
            File f = new File(folderPath);
            if (!f.isDirectory()) {
                Object[] paramsText = {folderPath, folderID};
                String msg = MessageFormat.format(Messages.getString("kit.folder.13"), paramsText);
                msgErr.append(msg);
            } else {
                newFolderID = newFolderID.trim().replaceAll("\\\\", "/");
                if (newFolderID.endsWith("/")) {
                    newFolderID = newFolderID.substring(0, newFolderID.length() - 1);
                }
            }
        }

        return newFolderID;
    }

    /**
     * Return the entered input folder.
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
        return responseFolder;
    }

    /**
     * Set response folder.
     * @param folder Response folder.
     */
    public void setResponseFolder(final String folder) {
        responseFolder = folder;
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
     * @return ArrayList containing info about all hosts and ports.
     */
    public ArrayList<String> getServiceFarm() {
        return serviceFarm;
    }

    /**
     * Return list of desired message types to retrieve. If returned value is
     * <code>null</code> all permitted messages will be retrieved.
     * @return List of message types to retrieve separated by semicolon ";".
     *         <code>null</code> to retrieve all messages to which user has
     *         permission.
     */
    public String getMessagesTypeList() {
        return messageTypesList;
    }

    /**
     * Set the list of desired messages to retrieve.
     * @param typeList
     *            list of desired messages to retrieve. Message types are
     *            separated by semicolon ";". To separate all accessible
     *            messages, value must be set to <code>null</code>
     */
    public void setMessagesTypeList(final String typeList) {
        messageTypesList = typeList.replaceAll(" ", "");
        if (messageTypesList.trim().isEmpty()) {
            messageTypesList = null;
        } else {
            messageTypesList = ";" + messageTypesList + ";";
        }
    }

    /**
     * Read setting file setting values as system properties.
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
        }

        inputFolder = cm.getValue(INPUT_FOLDER_KEY);
        if (inputFolder != null && inputFolder.trim().isEmpty()) {
            inputFolder = null;
        }

        outputFolder = cm.getValue(OUTPUT_FOLDER_KEY);
        if (outputFolder != null && outputFolder.trim().isEmpty()) {
            outputFolder = null;
        }

        responseFolder = cm.getValue(RESPONSE_FOLDER_KEY);
        if (responseFolder != null && responseFolder.trim().isEmpty()) {
            responseFolder = null;
        }

        processedFolder = cm.getValue(PROCESSED_FOLDER_KEY);
        if (processedFolder != null && processedFolder.trim().isEmpty()) {
            processedFolder = null;
        }

        backupFolder = cm.getValue(BACKUP_FOLDER_KEY);
        if (backupFolder != null && backupFolder.trim().isEmpty()) {
            backupFolder = null;
        }

        sleepTimeInput = cm.getValue(INPUT_DELAYTIME_KEY, DEFAULT_DELAY);
        sleepTimeOutput = cm.getValue(OUTPUT_DELAYTIME_KEY, DEFAULT_DELAY);
        numOfDaysKept = cm.getValue(NUM_OF_DAYS_KEEP_KEPT, DEFAULT_NUM_OF_DAYS_KEEP);

        setMessagesTypeList(cm.getValue(MENSSAGE_TYPES_KEY, ""));

        boolean loop = true;
        for (int count = 1; loop; count++) {
            String host = cm.getValue(HOST_PREFIX_KEY + count);
            if (host != null) {
                serviceFarm.add(host);
            } else {
                loop = false;
            }
        }

        thisServiceID = cm.getValue(ID_KEY);
    }

    /**
     * Updates settings file with entered values.
     * @throws ConfigException If cannot update file (File access error).
     */
    @Override
    public void writeConfiguration() throws ConfigException {

        ConfigManager cm = new ConfigManager();
        cm.readConfigFile(FOLDER_CONFIG_FILE);

        try {
            createBackup(FOLDER_CONFIG_FILE);
            String fileContent = FileUtil.read(FileUtil.getFullPathOfResoruce(FOLDER_CONFIG_FILE));
            fileContent = writeValue(inputFolder, cm.getValue(INPUT_FOLDER_KEY), INPUT_FOLDER_KEY, fileContent);
            fileContent = writeValue(outputFolder, cm.getValue(OUTPUT_FOLDER_KEY), OUTPUT_FOLDER_KEY, fileContent);
            fileContent = writeValue(backupFolder, cm.getValue(BACKUP_FOLDER_KEY), BACKUP_FOLDER_KEY, fileContent);
            fileContent = writeValue(processedFolder, cm.getValue(PROCESSED_FOLDER_KEY), PROCESSED_FOLDER_KEY, fileContent);
            fileContent = writeValue(responseFolder, cm.getValue(RESPONSE_FOLDER_KEY), RESPONSE_FOLDER_KEY, fileContent);

            FileUtil.write(FileUtil.getFullPathOfResoruce(FOLDER_CONFIG_FILE), fileContent);
        } catch (IOException ex) {
            throw new ConfigException(ex);
        }
    }
}

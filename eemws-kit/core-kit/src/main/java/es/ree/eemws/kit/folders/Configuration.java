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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.core.utils.config.ConfigManager;
import es.ree.eemws.kit.common.Messages;

/**
 * Magic folder configuration settings.
 * 
 * @author Red Eléctrica de España, S.A.U.
 * @version 2.0 27/04/2016
 */
public class Configuration extends es.ree.eemws.kit.config.Configuration {

    /** Name of the service to run / create . */
    private static final String SERVICE_NAME = "magic-folder"; //$NON-NLS-1$

    /** RMI url protocol prefix. */
    private static final String RMI_URL_PROTOCOL = "rmi://"; //$NON-NLS-1$

    /** Configuration key for service RMI number. */
    private static final String ID_KEY = "THIS_ID"; //$NON-NLS-1$

    /** Configuration key for HOST (or IP) RMI value. */
    private static final String HOST_PREFIX_KEY = "HOST"; //$NON-NLS-1$

    /** Configuration key for input sleep time between run loops. */
    private static final String INPUT_DELAYTIME_KEY = "INPUT_FOLDER_DELAY_TIME_MS"; //$NON-NLS-1$

    /** Configuration key for ack folder. */
    protected static final String RESPONSE_FOLDER_KEY = "ACK_FOLDER"; //$NON-NLS-1$

    /** Configuration key for ack OK folder. */
    private static final String RESPONSE_FOLDER_OK_KEY = "ACK_FOLDER_OK"; //$NON-NLS-1$

    /** Configuration key for ack FAILED folder. */
    private static final String RESPONSE_FOLDER_FAILED_KEY = "ACK_FOLDER_FAILED"; //$NON-NLS-1$

    /** Configuration key for script / program to be executed for ack ok messages. */
    private static final String ACK_FOLDER_OK_PROGRAM_CMD_LINE_KEY = "ACK_FOLDER_OK_PROGRAM_CMD_LINE"; //$NON-NLS-1$

    /** Configuration key for script / program to be executed for ack failed messages. */
    private static final String ACK_FOLDER_FAILED_PROGRAM_CMD_LINE_KEY = "ACK_FOLDER_FAILED_PROGRAM_CMD_LINE"; //$NON-NLS-1$

    /** Configuration key for input url. */
    private static final String INPUT_URL_KEY = "INPUT_WEBSERVICES_URL"; //$NON-NLS-1$

    /** Configuration key for output url. */
    private static final String OUTPUT_URL_KEY = "OUTPUT_WEBSERVICES_URL"; //$NON-NLS-1$

    /** Configuration key for instance identification. */
    private static final String INSTANCE_ID_KEY = "INSTANCE_ID"; //$NON-NLS-1$

    /** Configuration key for output sleep time between run loops. */
    private static final String OUTPUT_DELAYTIME_KEY = "OUTPUT_FOLDER_DELAY_TIME_MS"; //$NON-NLS-1$

    /** Configuration key for the messages types to be retrieved. */
    private static final String MENSSAGE_TYPES_KEY = "OUTPUT_FOLDER_MESSAGE_TYPES"; //$NON-NLS-1$

    /** Configuration key for files extension. */
    private static final String FILE_NAME_EXTENSION_KEY = "OUTPUT_FILE_NAME_EXTENSION"; //$NON-NLS1$ //$NON-NLS-1$

    /** Configuration key for command line / program to be executed after retrieving a message. */
    private static final String COMMAND_LINE_KEY = "OUTPUT_PROGRAM_CMD_LINE"; //$NON-NLS-1$

    /** Maximum instance identification lenght. */
    private static final int INSTANCE_ID_MAX_LENGTH = 20;

    /** Configuration key for the maximun number of threads. */
    private static final String MAX_NUM_THREADS_KEY = "MAX_NUM_THREADS"; //$NON-NLS-1$

    /** Default number of thrads. */
    private static final int DEFAULT_MAX_NUM_THREADS = 5;

    /** Max number of threads. */
    private static final int MAX_NUM_THREADS = 25;

    /** Configuration key for the number of days that a file is kept in the system. */
    private static final String MAX_FILE_AGE_IN_DAYS = "MAX_FILE_AGE_IN_DAYS"; //$NON-NLS-1$

    /** Default value for the numbers of days that a file is kept in the system. */
    private static final int DEFAULT_MAX_FILE_AGE_IN_DAYS = 7;

    /** ID string for this service into farm. */
    private String rmiServiceNumber;

    /** List containing all hosts and ports of service Farm. */
    private final List<String> membersRmiUrls = new ArrayList<>();

    /** This instance identification. */
    private String instanceID = null;

    /** Number of days that a file is kept in file system before a backup is done. */
    private int numOfDaysKept;

    /** Max number of concurrent threads. */
    private int maxNumThreads;

    /** Backup folder. */
    protected String backupFolder = null;

    /** List of input configuration sets. */
    protected List<InputConfigurationSet> inputSetLst = new ArrayList<>();

    /** List of output configuration sets by URL. */
    protected List<List<OutputConfigurationSet>> outputSetLst = new ArrayList<>();

    /** Configuration key for input folder. */
    protected static final String INPUT_FOLDER_KEY = "INPUT_FOLDER"; //$NON-NLS-1$

    /** Configuration key for processed folder. */
    protected static final String PROCESSED_FOLDER_KEY = "PROCESSED_FOLDER"; //$NON-NLS-1$

    /** Configuration key for output folder. */
    protected static final String OUTPUT_FOLDER_KEY = "OUTPUT_FOLDER"; //$NON-NLS-1$

    /** Configuration key backup folder. */
    protected static final String BACKUP_FOLDER_KEY = "BACKUP_FOLDER"; //$NON-NLS-1$

    /** Settings file for Folder manager. */
    protected static final String FOLDER_CONFIG_FILE = "magic-folder.properties"; //$NON-NLS-1$

    /** Min delay time for loops. */
    protected static final long MIN_SLEEP_TIME = 60000;

    /** Log elements separator. */
    protected static final String TAB = "\n    "; //$NON-NLS-1$

    /**
     * Checks that the given folder (if not null) exists.
     * @param folderPath Absolute path to the folder.
     * @param folderID Identification of the folder (for error details).
     * @return The folderPath in linux format (/ instead of \)
     * @throws ConfigException If the given folder is not an existent directory.
     */
    protected String validateFolder(final String folderPath, final String folderID) throws ConfigException {

        String retValue = null;

        if (isNotNullAndNotEmpty(folderPath)) {
            File f = new File(folderPath);
            if (f.isDirectory()) {
                retValue = folderPath.trim().replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
                if (retValue.endsWith("/")) { //$NON-NLS-1$
                    retValue = folderPath.substring(0, folderPath.length() - 1);
                }
            } else {
                throw new ConfigException(Messages.getString("MF_INVALID_FOLDER", folderID, folderPath)); //$NON-NLS-1$
            }
        }

        return retValue;
    }

    /**
     * Gets maximun number of threads.
     * @return Maximun number of threads.
     */
    public int getMaxNumThreads() {
        return maxNumThreads;
    }

    /**
     * Sets the maximun number of threads.
     * @param value Configured number of threads.
     * @throws ConfigException If the configured value is not a number.
     */
    private void setMaxNumThreads(final String value) throws ConfigException {
        if (isNotNullAndNotEmpty(value)) {
            try {
                maxNumThreads = Integer.parseInt(value);
                if (maxNumThreads < DEFAULT_MAX_NUM_THREADS) {
                    maxNumThreads = DEFAULT_MAX_NUM_THREADS;
                }
                if (maxNumThreads > MAX_NUM_THREADS) {
                    maxNumThreads = MAX_NUM_THREADS;
                }
            } catch (NumberFormatException ex) {
                throw new ConfigException(Messages.getString("MF_INVALID_NUMBER", MAX_NUM_THREADS_KEY, maxNumThreads)); //$NON-NLS-1$
            }
        } else {
            maxNumThreads = DEFAULT_MAX_NUM_THREADS;
        }
    }

    /**
     * Sets this MF instance id.
     * @param value Configured identification.
     * @throws ConfigException If the given value is incorrect.
     */
    private void setInstanceId(final String value) throws ConfigException {

        if (isNotNullAndNotEmpty(value)) {

            instanceID = value;

            try {
                File f = new File(instanceID);
                f.getCanonicalPath();
            } catch (IOException e) {
                throw new ConfigException(Messages.getString("MF_INVALID_ID", INSTANCE_ID_KEY, instanceID)); //$NON-NLS-1$
            }

            if (instanceID.length() > INSTANCE_ID_MAX_LENGTH) {
                throw new ConfigException(Messages.getString("MF_INVALID_ID_LENGTH", INSTANCE_ID_KEY, instanceID, INSTANCE_ID_MAX_LENGTH)); //$NON-NLS-1$
            }

            StatusIcon.setIdentification(value);
        } else {
            instanceID = null;
        }
    }

    /**
     * Returns this MF instance ID.
     * @return Instance ID <code>null</code> if undefined.
     */
    public String getInstanceID() {
        return instanceID;
    }

    /**
     * Gets backup folder.
     * @return Configured backup folder or <code>null</code> if there is no backup folder.
     */
    public String getBackupFolder() {
        String retValue = null;
        if (isNotNullAndNotEmpty(backupFolder)) {
            retValue = backupFolder;
        }

        return retValue;
    }

    /**
     * Sets the backup folder or <code>null</code> if there is no backup folder.
     * @param folder Configured backup folder or <code>null</code> if there is no backup folder.
     */
    public void setBackupFolder(final String folder) {
        backupFolder = folder;
    }

    /**
     * Gets the number of days that a generated file is keep in the file system.
     * @return Number of days that a generated file is keep in the file system.
     */
    public Integer getNumOfDaysKept() {
        return numOfDaysKept;
    }

    /**
     * Sets the number of days that a generated file is keep in the file system.
     * @param value Configured number of days that a generated file is keep in the file system.
     * @throws ConfigException If the configured value is not a number.
     */
    private void setNumOfDaysKept(final String value) throws ConfigException {
        if (isNotNullAndNotEmpty(value)) {
            try {
                numOfDaysKept = Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                throw new ConfigException(Messages.getString("MF_INVALID_NUMBER", MAX_FILE_AGE_IN_DAYS, numOfDaysKept)); //$NON-NLS-1$
            }
        } else {
            numOfDaysKept = DEFAULT_MAX_FILE_AGE_IN_DAYS;
        }
    }

    /**
     * Sets this server rmi service number.
     * @param value Configured rmi service number.
     * @throws ConfigException If the configured number is not valid.
     */
    private void rmiServiceNumber(final String value) throws ConfigException {
        rmiServiceNumber = value;

        if (rmiServiceNumber != null) {
            try {
                int k = Integer.parseInt(rmiServiceNumber);
                if (k < 1) {
                    throw new ConfigException(Messages.getString("MF_INVALID_NUMBER", ID_KEY, rmiServiceNumber)); //$NON-NLS-1$
                }
            } catch (NumberFormatException ex) {
                throw new ConfigException(Messages.getString("MF_INVALID_NUMBER", ID_KEY, rmiServiceNumber)); //$NON-NLS-1$
            }
        }
    }

    /**
     * Returns this server rmi service number.
     * @return Configured rmi service number.
     */
    public String getRmiServiceNumber() {
        return rmiServiceNumber;
    }

    /**
     * Returns a list containing info about all hosts and ports.
     * @return List containing info about all members rmi urls.
     */
    public List<String> getMembersRmiUrls() {
        return membersRmiUrls;
    }

  

    /**
     * Reads and validates the configuration file.
     * @throws ConfigException If the configuration file cannot be read of if it is incorrect.
     */
    @Override
    public void readConfiguration() throws ConfigException {

        super.readConfiguration();

        ConfigManager cm = new ConfigManager();
        cm.readConfigFile(FOLDER_CONFIG_FILE);

        setInstanceId(cm.getValue(INSTANCE_ID_KEY));
        rmiServiceNumber(cm.getValue(ID_KEY));
        setNumOfDaysKept(cm.getValue(MAX_FILE_AGE_IN_DAYS));
        setMaxNumThreads(cm.getValue(MAX_NUM_THREADS_KEY));

        backupFolder = validateFolder(cm.getValue(BACKUP_FOLDER_KEY), BACKUP_FOLDER_KEY);

        boolean atLeastOneIFolder = readInputSet(cm);
        boolean atLeastOneOFolder = readOutputSet(cm);

        if (!atLeastOneIFolder && !atLeastOneOFolder) {
            throw new ConfigException(Messages.getString("MF_UNABLE_TO_START", INPUT_FOLDER_KEY, OUTPUT_FOLDER_KEY)); //$NON-NLS-1$ 
        }

        readRmiUrls(cm);

    }

    /**
     * Reads the RMI configuration.
     * @param cm Configuration Manager in order to read configured values.
     * @throws ConfigException If the RMI configuration is invalid.
     */
    private void readRmiUrls(final ConfigManager cm) throws ConfigException {
        boolean loop = true;
        for (int count = 1; loop; count++) {
            String hostAndPort = cm.getValue(HOST_PREFIX_KEY + "_" + count); //$NON-NLS-1$
            if (hostAndPort != null) {
                String rmiUrl = RMI_URL_PROTOCOL + hostAndPort + File.separator + SERVICE_NAME;

                String hostPort = rmiUrl.substring(RMI_URL_PROTOCOL.length(), rmiUrl.indexOf(SERVICE_NAME) - 1);
                int colonPosition = hostPort.indexOf(":"); //$NON-NLS-1$
                if (colonPosition == -1) {
                    throw new ConfigException(Messages.getString("MF_MF_INVALID_MEMBER_URL", hostPort)); //$NON-NLS-1$
                }

                String port = ""; //$NON-NLS-1$
                try {
                    port = hostPort.substring(colonPosition + 1);
                    Integer.parseInt(port);
                } catch (NumberFormatException ex) {
                    throw new ConfigException(Messages.getString("MF_INVALID_MEMBER_PORT", port)); //$NON-NLS-1$
                }

                membersRmiUrls.add(rmiUrl);

            } else {
                loop = false;
            }
        }
    }

    /**
     * Reads input set configuration.
     * @param cm Configuration Manager in order to read configured values.
     * @return <code>true</code> if at least one input folder is configured.
     * @throws ConfigException If the configured set of input values are invalid.
     */
    private boolean readInputSet(final ConfigManager cm) throws ConfigException {
        boolean retValue = false;
        int cont = 0;
        String sufix = ""; //$NON-NLS-1$
        String value;
        String key;

        do {
            key = INPUT_FOLDER_KEY + sufix;
            value = cm.getValue(key);

            if (isNotNullAndNotEmpty(value)) {
                retValue = true;
                InputConfigurationSet is = new InputConfigurationSet(cont);
                is.setInputFolder(validateFolder(value, key));

                key = RESPONSE_FOLDER_KEY + sufix;
                is.setAckFolder(validateFolder(cm.getValue(key), key));

                key = PROCESSED_FOLDER_KEY + sufix;
                is.setProcessedFolder(validateFolder(cm.getValue(key), key));

                key = RESPONSE_FOLDER_OK_KEY + sufix;
                is.setAckOkFolder(validateFolder(cm.getValue(key), key));

                key = RESPONSE_FOLDER_FAILED_KEY + sufix;
                is.setAckFailedFolder(validateFolder(cm.getValue(key), key));

                key = INPUT_DELAYTIME_KEY + sufix;
                is.setSleepTime(cm.getValue(key), key);

                is.setOkCmd(cm.getValue(ACK_FOLDER_OK_PROGRAM_CMD_LINE_KEY + sufix));
                is.setFailedCmd(cm.getValue(ACK_FOLDER_FAILED_PROGRAM_CMD_LINE_KEY + sufix));

                key = INPUT_URL_KEY + sufix;
                is.setInputUrlEndPoint(cm.getValue(key, super.getUrlEndPoint().toString()), key);

                inputSetLst.add(is);
            }

            cont++;
            sufix = "_" + cont; //$NON-NLS-1$

        } while (value != null);

        return retValue;
    }

    /**
     * Reads output set configuration.
     * @param cm Configuration Manager in order to read configured values.
     * @return <code>true</code> if at least one output folder is configured.
     * @throws ConfigException If the configured set of output values are invalid.
     */
    private boolean readOutputSet(final ConfigManager cm) throws ConfigException {
        boolean retValue = false;
        int cont = 0;
        String sufix = ""; //$NON-NLS-1$
        String value;
        String key;
        Map<URL, List<OutputConfigurationSet>> listByUrl = new HashMap<>();

        do {
            key = OUTPUT_FOLDER_KEY + sufix;
            value = cm.getValue(key);

            if (isNotNullAndNotEmpty(value)) {
                retValue = true;
                OutputConfigurationSet os = new OutputConfigurationSet(cont);
                os.setOutputFolder(validateFolder(value, key));
                os.setFileNameExtension(cm.getValue(FILE_NAME_EXTENSION_KEY + sufix));
                os.setProgramCmdLine(cm.getValue(COMMAND_LINE_KEY + sufix));
                os.setMessagesTypesList(cm.getValue(MENSSAGE_TYPES_KEY + sufix));

                key = OUTPUT_DELAYTIME_KEY + sufix;
                os.setSleepTime(cm.getValue(key), key);

                key = OUTPUT_URL_KEY + sufix;
                os.setOutputUrlEndPoint(cm.getValue(key, super.getUrlEndPoint().toString()), key);

                URL url = os.getOutputUrlEndPoint();
                List<OutputConfigurationSet> lst = listByUrl.get(url);
                if (lst == null) {
                    lst = new ArrayList<>();
                    listByUrl.put(url,  lst);
                }

                lst.add(os);
            }

            cont++;
            sufix = "_" + cont; //$NON-NLS-1$

        } while (value != null);

        outputSetLst.addAll(listByUrl.values());

        return retValue;
    }

    /**
     * Returns the current input configuration set values.
     * @return Current input configuration set values.
     */
    public List<InputConfigurationSet> getInputConfigurationSet() {
        return inputSetLst;
    }

    /**
     * Returns the current output configuration set values by URL.
     * @return Current output configuration set values.
     */
    public List<List<OutputConfigurationSet>> getOutputConfigurationSet() {
        return outputSetLst;
    }

}

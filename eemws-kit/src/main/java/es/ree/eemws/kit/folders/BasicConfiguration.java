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

/**
 * Magic folder basic configuration settings.
 * These settings are available thought config GUI application.
 * 
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 27/04/2016
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.core.utils.config.ConfigManager;
import es.ree.eemws.core.utils.file.FileUtil;

/**
 * Magic folder basic configuration settings.
 * 
 * @author Red Eléctrica de España, S.A.U.
 * @version 2.0 27/04/2016
 */

public final class BasicConfiguration extends Configuration {

    /** Input configuration set. */
    private InputConfigurationSet is = new InputConfigurationSet(0);

    /** Output configuration set. */
    private OutputConfigurationSet os = new OutputConfigurationSet(0);

    /**
     * Reads the basic set of configuration folders:
     * <ul>
     * <li>Input</li>
     * <li>Ack</li>
     * <li>Processed</li>
     * <li>Output</li>
     * <li>Backup</li>
     * </ul>
     * @throws ConfigException  If it is not possible to read the configuration file.
     */
    @Override
    public void readConfiguration() throws ConfigException {

        String value;

        ConfigManager cm = new ConfigManager();
        cm.readConfigFile(FOLDER_CONFIG_FILE);

        value = cm.getValue(INPUT_FOLDER_KEY);
        if (isNotNullAndNotEmpty(value)) {
            is.setInputFolder(value);
        }

        value = cm.getValue(RESPONSE_FOLDER_KEY);
        if (isNotNullAndNotEmpty(value)) {
            is.setAckFolder(value);
        }

        value = cm.getValue(PROCESSED_FOLDER_KEY);
        if (isNotNullAndNotEmpty(value)) {
            is.setProcessedFolder(value);
        }

        value = cm.getValue(OUTPUT_FOLDER_KEY);
        if (isNotNullAndNotEmpty(value)) {
            os.setOutputFolder(value);
        }

        backupFolder = cm.getValue(BACKUP_FOLDER_KEY);
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

            StringBuilder fileContent = new StringBuilder(FileUtil.read(fullPathConfig));

            writeValue(is.getInputFolder(), cm.getValue(INPUT_FOLDER_KEY), INPUT_FOLDER_KEY, fileContent);
            writeValue(is.getProcessedFolder(), cm.getValue(PROCESSED_FOLDER_KEY), PROCESSED_FOLDER_KEY, fileContent);
            writeValue(is.getAckFolder(), cm.getValue(RESPONSE_FOLDER_KEY), RESPONSE_FOLDER_KEY, fileContent);
            writeValue(os.getOutputFolder(), cm.getValue(OUTPUT_FOLDER_KEY), OUTPUT_FOLDER_KEY, fileContent);
            writeValue(backupFolder, cm.getValue(BACKUP_FOLDER_KEY), BACKUP_FOLDER_KEY, fileContent);

            FileUtil.createBackup(fullPathConfig);
            FileUtil.write(fullPathConfig, fileContent.toString());

        } catch (IOException ex) {
            throw new ConfigException(ex);
        }
    }

    /**
     * Validates the current set of basic configuration.
     * @throws ConfigException If the current set is not valid.
     */
    public void validateConfiguration() throws ConfigException {

        backupFolder = validateFolder(backupFolder, BACKUP_FOLDER_KEY);

        is.setInputFolder(validateFolder(is.getInputFolder(), INPUT_FOLDER_KEY));
        is.setAckFolder(validateFolder(is.getAckFolder(), RESPONSE_FOLDER_KEY));
        is.setProcessedFolder(validateFolder(is.getProcessedFolder(), PROCESSED_FOLDER_KEY));

        inputSetLst.clear();
        inputSetLst.add(is);

        os.setOutputFolder(validateFolder(os.getOutputFolder(), OUTPUT_FOLDER_KEY));

        outputSetLst.clear();

        List<OutputConfigurationSet> tmpConf = new ArrayList<>();
        tmpConf.add(os);

        outputSetLst.add(tmpConf);
    }

    /** 
     * Gets input folder.
     * @return Input folder.
     */
    public String getInputFolder() {
        return is.getInputFolder();
    }

    /**
     * Sets input folder.
     * @param value input folder.
     */
    public void setInputFolder(final String value) {
        is.setInputFolder(value);
    }

    /**
     * Gets response (ack) folder.
     * @return Response (ack) folder.
     */
    public String getResponseFolder() {
        return is.getAckFolder();
    }

    /**
     * Sets response (ack) folder.
     * @param value Response (ack) folder.
     */
    public void setResponseFolder(final String value) {
        is.setAckFolder(value);
    }

    /**
     * Gets processed folder.
     * @return Processed folder.
     */
    public String getProcessedFolder() {
        return is.getProcessedFolder();
    }

    /**
     * Sets processed folder.
     * @param value Processed folder.
     */
    public void setProcessedFolder(final String value) {
        is.setProcessedFolder(value);
    }

    /**
     * Gets output folder.
     * @return Output folder.
     */
    public String getOutputFolder() {
        return os.getOutputFolder();
    }

    /** 
     * Sets output folder.
     * @param value Output folder.
     */
    public void setOutputFolder(final String value) {
        os.setOutputFolder(value);
    }

}

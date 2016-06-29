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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.kit.common.Messages;

/**
 * Output  Configuration Set.
 * Stores information about a set of output values for MF. 
 * There could be several output sets.
 * 
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 27/04/2016
 */
public final class OutputConfigurationSet extends Configuration {

    /** File name extension "auto". */
    public static final String FILE_NAME_EXTENSION_AUTO = "AUTO"; //$NON-NLS-1$

    /** File name extension "none". */
    public static final String FILE_NAME_EXTENSION_NONE = "NONE"; //$NON-NLS-1$

    /** Default delay time for loops in milliseconds. */
    private static final long DEFAULT_OUTPUT_DELAY = 180000L;
 
    /** Output folder. */
    private String outputFolder;

    /** List of message types to retrieve. */
    private List<String> typesToRetrieveList;

    /** Program to be executed when a file is saved. */
    private String programCmdLine;

    /** File name extension to be used. */
    private String fileNameExtension;

    /** Sleep time between run loops. */
    private long sleepTimeOutput;

    /** Url end point. */
    private URL endPoint;
    
    /** This set index. */
    private int index;

    /** 
     * Constructor.
     * Creates a new Output configuration set with the given index.
     * @param i This set index.
     */
    public OutputConfigurationSet(final int i) {
        index = i;
    }

    /**
     * Returs this set index.
     * @return This set index.
     */
    public int getIndex() {
        return index;
    }


    /**
     * Sets this set output folder.
     * @param folder This set output folder.
     */
    public void setOutputFolder(final String folder) {
        if (isNotNullAndNotEmpty(folder)) {
            outputFolder = folder;
        }
    }

    /**
     * Returns this set outuput folder.
     * @return This set output folder.
     */
    public String getOutputFolder() {
        return outputFolder;
    }

    /**
     * Sets the set of message types to be retrieved. 
     * @param list List of messages types to be retrieved (using ";" as separator)
     */
    public void setMessagesTypesList(final String list) {

        if (isNotNullAndNotEmpty(list)) {
            typesToRetrieveList = Arrays.asList(list.split(";")); //$NON-NLS-1$
        } else {
            typesToRetrieveList = null;
        }
    }

    /**
     * Gets the set of message types to be retrieved. 
     * @return List of messages types to be retrieved.
     */
    public List<String> getMessagesTypesList() {
        return typesToRetrieveList;
    }

    /**
     * Sets this set file name extension.
     * @param ext This set file name extension.
     */
    public void setFileNameExtension(final String ext) {
        if (isNotNullAndNotEmpty(ext)) {
            fileNameExtension = ext;
        } else {
            fileNameExtension = FILE_NAME_EXTENSION_AUTO;
        }
    }

    /**
     * Gets this set file name extension.
     * @return This set file name extension.
     */
    public String getFileNameExtension() {
        return fileNameExtension;
    }

    /**
     * Sets this set program / script.
     * @param cmd Program / script to be executed.
     */
    public void setProgramCmdLine(final String cmd) {
        if (isNotNullAndNotEmpty(cmd)) {
            programCmdLine = cmd;
        } else {
            programCmdLine = null;
        }
    }

    /**
     * Returns this set program / script.
     * @return This set program / script. 
     */
    public String getProgramCmdLine() {
        return programCmdLine;
    }
    
    /**
     * Sets this set sleep time between loops.
     * @param value Value retrieved from the configuration.
     * @param label The value name in the configuration set.
     * @throws ConfigException If the given value is not a number of if it is too small.
     */
    public void setSleepTime(final String value, final String label) throws ConfigException {
        if (isNotNullAndNotEmpty(value)) {
            try {
                sleepTimeOutput = Long.parseLong(value);
                if (sleepTimeOutput < MIN_SLEEP_TIME) {
                    throw new ConfigException(Messages.getString("MF_VALUE_TOO_SMALL", label, sleepTimeOutput, MIN_SLEEP_TIME)); //$NON-NLS-1$
                }
            } catch (NumberFormatException ex) {
                throw new ConfigException(Messages.getString("MF_INVALID_NUMBER", label, sleepTimeOutput)); //$NON-NLS-1$
            }
        } else { 
            sleepTimeOutput = DEFAULT_OUTPUT_DELAY;
        }
    }
    
    /**
     * Gets this set sleep time between loops.
     * @return This set sleep time between loops.
     */
    public long getSleepTime() {
        return sleepTimeOutput;
    }

    /**
     * Sets this set url endpoint.
     * @param urlEndPoint This set url endpoint.
     * @param label Configuration label for this url.
     * @throws ConfigException If the provided URL is not valid.
     */
    public void setOutputUrlEndPoint(final String urlEndPoint, final String label) throws ConfigException {
        try {
            endPoint = new URL(urlEndPoint);
        } catch (MalformedURLException e) {
            throw new ConfigException(Messages.getString("MF_INVALID_SET_URL", label, urlEndPoint)); //$NON-NLS-1$
        }
    }

    /**
     * Returns this output set URL.
     * @return This output set URL.
     */
    public URL getOutputUrlEndPoint() {
        return endPoint;
    }
    
    /**
     * Returns a string representation of this set.
     * @return String representation of this set.
     */
    @Override
    public String toString() {
        StringBuffer msg = new StringBuffer();

        msg.append(TAB).append(Messages.getString("MF_CONFIG_OUTPUT_FOLDER", index, outputFolder)); //$NON-NLS-1$ 

        msg.append(TAB);
        if (typesToRetrieveList == null) {
            msg.append(Messages.getString("MF_CONFIG_LST_MESSAGES_TYPE_ALL", index)); //$NON-NLS-1$ 
        } else {
            msg.append(Messages.getString("MF_CONFIG_LST_MESSAGES_TYPE", index, typesToRetrieveList.toString())); //$NON-NLS-1$ 
        }

        msg.append(TAB).append(Messages.getString("MF_FILE_NAME_EXTENSION", index, fileNameExtension)); //$NON-NLS-1$ 

        msg.append(TAB);
        if (programCmdLine == null) {
            msg.append(Messages.getString("MF_NO_PROGRAM", index)); //$NON-NLS-1$ 
        } else {
            msg.append(Messages.getString("MF_PROGRAM", index, programCmdLine)); //$NON-NLS-1$ 
        }

        msg.append(TAB).append(Messages.getString("MF_CONFIG_URL_O", index, endPoint.toString())); //$NON-NLS-1$  

        return msg.toString();
    }

   
}

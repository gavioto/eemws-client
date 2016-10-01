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

import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.kit.common.Messages;

/**
 * Input Configuration Set.
 * Stores information about a set of input values for MF. 
 * There could be several input sets.
 * 
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 27/04/2016
 */
public final class InputConfigurationSet extends Configuration {

    /** Default delay time for loops in milliseconds. */
    private static final long DEFAULT_INPUT_DELAY = 180000L; 
   
    /** Input folder. */
    private String inputFolder;

    /** Processed folder. */
    private String processedFolder;

    /** Response folder. */
    private String ackFolder;

    /** Response OK folder. */
    private String ackOkFolder;

    /** Response FAILED folder. */
    private String ackFailedForder;

    /** Command line to execute for OK response. */
    private String ackOkCmd;

    /** Command line to execute for FAILED response. */
    private String ackFailedCmd;

    /** Sleep between run loops. */
    private long sleepTimeInput;

    /** Url end point. */
    private URL endPoint;

    /** This set index. */
    private int index;

    /** 
     * Constructor.
     * Creates a new Input configuration set with the given index.
     * @param i This set index.
     */
    public InputConfigurationSet(final int i) {
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
     * Returns this set input folder.
     * @return This set input folder.
     */
    public String getInputFolder() {
        return inputFolder;
    }

    /**
     * Sets this set input folder.
     * @param folder Input folder.
     */
    public void setInputFolder(final String folder) {
        if (isNotNullAndNotEmpty(folder)) {
            inputFolder = folder.trim();
        }
    }

    /**
     * Gets the command line to be executed for ack ok.
     * @return Command line to be executed for ack ok.
     * <code>null</code> if no command line was specified.
     */
    public String getAckOkProgramCmdLine() {
        return ackOkCmd;
    }

    /**
     * Gets the command line to be executed for ack failed.
     * @return Command line to be executed for ack failed.
     * <code>null</code> if no command line was specified.
     */
    public String getAckFailedProgramCmdLine() {
        return ackFailedCmd;
    }

    /**
     * Sets this set ack folder.
     * @param folder This set ack folder.
     */
    public void setAckFolder(final String folder) {
        if (isNotNullAndNotEmpty(folder)) {
            ackFolder = folder.trim();    
        }        
    }

    /**
     * Returns this set ack Folder.
     * @return This set ack folder.
     */
    public String getAckFolder() {
        return ackFolder;
    }

    /**
     * Sets this ack ok folder.
     * @param folder Ack ok folder.
     */
    public void setAckOkFolder(final String folder) {
        if (isNotNullAndNotEmpty(folder)) {
            ackOkFolder = folder.trim();
        }
    }

    /**
     * Returns this set ack ok folder.
     * @return This set ack ok folder.
     */
    public String getAckOkFolder() {
        return ackOkFolder;
    }

    /**
     * Set this set processed folder.
     * @param folder This set processed folder.
     */
    public void setProcessedFolder(final String folder) {
        if (isNotNullAndNotEmpty(folder)) {
            processedFolder = folder.trim();
        }
    }

    /**
     * Returns this set processed folder.
     * @return This set processed folder.
     */
    public String getProcessedFolder() {
        return processedFolder;
    }

    /**
     * Sets this set ack failed folder.
     * @param folder This set ack failed folder.
     */
    public void setAckFailedFolder(final String folder) {
        if (isNotNullAndNotEmpty(folder)) {
            ackFailedForder = folder.trim();
        }
    }

    /**
     * Gets this set ack failed folder.
     * @return This set ack failded folder.
     */
    public String getAckFailedFolder() {
        return ackFailedForder;
    }
    
    /**
     * Sets this set command line for ack ok.
     * @param cmd Command line for ack ok.
     */
    public void setOkCmd(final String cmd) {
        if (isNotNullAndNotEmpty(cmd)) {
            ackOkCmd = cmd;
        }
    }

    /**
     * Sets this set command line for ack failed.
     * @param cmd Command line for ack failed.
     */
    public void setFailedCmd(final String cmd) {
        if (isNotNullAndNotEmpty(cmd)) {
            ackFailedCmd = cmd;
        }
    }

    /**
     * Returns this set sleep time between run loops.
     * @return This set sleep time between run loops.
     */    
    public long getSleepTime() {
        return sleepTimeInput;
    }

    /**
     * Sets this set sleep time between loops.
     * @param value Configured sleep value.
     * @param label Current sleep configuration label.
     * @throws ConfigException If the given value is not a number or if it is too small.
     */
    public void setSleepTime(final String value, final String label) throws ConfigException {
        if (isNotNullAndNotEmpty(value)) {
            try {
                sleepTimeInput = Long.parseLong(value);
                if (sleepTimeInput < MIN_SLEEP_TIME) {
                    throw new ConfigException(Messages.getString("MF_VALUE_TOO_SMALL", label, sleepTimeInput, MIN_SLEEP_TIME)); //$NON-NLS-1$
                }
            } catch (NumberFormatException ex) {
                throw new ConfigException(Messages.getString("MF_INVALID_NUMBER", label, sleepTimeInput)); //$NON-NLS-1$
            }
        } else { 
            sleepTimeInput = DEFAULT_INPUT_DELAY;
        }
    }

    /**
     * Sets this set url endpoint.
     * @param urlEndPoint This set url endpoint.
     * @param label This element configuration name.
     * @throws ConfigException If the provided URL is not valid.
     */
    public void setInputUrlEndPoint(final String urlEndPoint, final String label) throws ConfigException {
        try {
            endPoint = new URL(urlEndPoint);
        } catch (MalformedURLException e) {
            throw new ConfigException(Messages.getString("MF_INVALID_SET_URL", label, urlEndPoint)); //$NON-NLS-1$
        }
    }

    
    /**
     * Gets this input set URL.
     * @return This input set URL.
     */
    public URL getInputUrlEndPoint() {
        return endPoint;
    }

    /**
     * Retruns a string representation of the input set.
     * @return A string representation of the input set.
     */
    @Override
    public String toString() {

        StringBuffer msg = new StringBuffer();

        msg.append(TAB).append(Messages.getString("MF_CONFIG_INPUT_FOLDER", index, inputFolder)); //$NON-NLS-1$

        if (ackFolder != null) {
            msg.append(TAB).append(Messages.getString("MF_CONFIG_ACK_FOLDER", index, ackFolder)); //$NON-NLS-1$
        }

        if (ackOkFolder != null) {
            msg.append(TAB).append(Messages.getString("MF_CONFIG_ACK_OK_FOLDER", index, ackOkFolder)); //$NON-NLS-1$
        }

        if (ackFailedForder != null) {
            msg.append(TAB).append(Messages.getString("MF_CONFIG_ACK_FAILED_FOLDER", index, ackFailedForder)); //$NON-NLS-1$
        }

        if (ackOkCmd != null && (ackFolder != null || ackOkFolder != null)) {
            msg.append(TAB).append(Messages.getString("MF_CONFIG_CMD_ACK_OK", index, ackOkCmd)); //$NON-NLS-1$
        }

        if (ackFailedCmd != null && (ackFolder != null || ackFailedForder != null)) {
            msg.append(TAB).append(Messages.getString("MF_CONFIG_CMD_ACK_FAILED", index, ackFailedCmd)); //$NON-NLS-1$
        }
        
        if (processedFolder != null) {
            msg.append(TAB).append(Messages.getString("MF_CONFIG_PROCESSED_FOLDER", index, processedFolder)); //$NON-NLS-1$
        }

        msg.append(TAB).append(Messages.getString("MF_CONFIG_DELAY_TIME_I", index, sleepTimeInput)); //$NON-NLS-1$
        msg.append(TAB).append(Messages.getString("MF_CONFIG_URL_I", index, endPoint.toString())); //$NON-NLS-1$
        
        return msg.toString();
    }

   

}

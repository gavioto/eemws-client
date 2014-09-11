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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.SignatureException;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.ree.eemws.client.exception.ClientException;
import es.ree.eemws.client.putmessage.PutMessage;
import es.ree.eemws.core.utils.file.FileUtil;

/**
 * El <code>DetectorEntradaThread</code> implements a loop that
 * searches and processes files in input folder.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 29/05/2014
 */
public final class InputDetectionThread extends Thread {

    /** Time passed (expressed in Milliseconds) between file size checks. */
    private static final long SLEEP_BETWEEN_READS = 500;

    /** Extension appended to generated files. */
    private static final String XML_FILE_EXTENSION = ".xml";

    /** Prefix added to identify response files. */
    private static final String RESPONSE_ID_PREFIX = "response_";

    /** Input folder. */
    private final transient String inputFolderPath;

    /** Processed folder. */
    private final transient String processedFolderPath;

    /** Response folder. */
    private final transient String responseFolderPath;

    /** Sending web service module. */
    private final transient PutMessage putMessage;

    /** Set as <code>true</code> if thread can be killed. */
    private transient boolean killable;

    /** Object for checking messages sent by another module. */
    private final transient LockHandler lh;

    /** Thread log system. */
    private static Logger log = Logger.getLogger(Thread.currentThread().getStackTrace()[0].getClassName());

    /** Set as <code>true</code> if the thread must stop. */
    private transient boolean stop;

    /** Set as <code>true</code> if the thread finished its cycle. */
    private transient boolean stopped;

    /** time during thread remains stopped until next cycle. */
    private final transient long sleepTime;

    /**
     * Constructor. Initializes parameters for stopping thread.
     * @param lockHandler Lock handler.
     * @param config Module settings.
     * @throws MalformedURLException If cannot obtain an URL to access attachment services.
     */
    public InputDetectionThread(final LockHandler lockHandler, final Configuration config)
            throws MalformedURLException {

        inputFolderPath = config.getInputFolder();
        responseFolderPath = config.getResponseFolder();
        processedFolderPath = config.getProcessedFolder();
        String endPoint = config.getUrlEndPoint().toString();
        sleepTime = config.getSleepTimeInput();

        stop = false;
        stopped = true;
        killable = true;

        lh = lockHandler;

        putMessage = new PutMessage();

        putMessage.setEndPoint(endPoint);
        putMessage.setSignRequest(config.isSignResquest());
        putMessage.setVerifyResponse(config.isVerifySignResponse());

        if (log.isLoggable(Level.CONFIG)) {
            StringBuffer msg = new StringBuffer();
            msg.append("\n[INPUT] Input folder: ");
            msg.append(inputFolderPath);
            if (responseFolderPath != null) {
                msg.append("\n[INPUT] Response folder: ");
                msg.append(responseFolderPath);
            }
            if (processedFolderPath != null) {
                msg.append("\n[INPUT] Processed folder: ");
                msg.append(processedFolderPath);
            }
            msg.append("\n[INPUT] Delay time between detections: ");
            msg.append(sleepTime);
            msg.append("\n[INPUT] Send URL: ");
            msg.append(endPoint.toString());

            log.config(msg.toString());
        }
    }


    /**
     * Stopping cycle process.
     */
    @Override
    public void run() {
        stopped = false;

        long sleep = 0;
        long start;
        long end;

        killable = false;

        while (!stop) {
            try {
                sleep = sleepTime;
                start = System.currentTimeMillis();
                detect();
                end = System.currentTimeMillis();
                sleep -= (start - end);
            } catch (RuntimeException ex) {
                log.severe("[INPUT] Error in message detection cycle.");
            }

            if (sleep > 0) {
                try {
                    killable = true;
                    Thread.sleep(sleep);
                    killable = false;
                } catch (InterruptedException ex) {
                    killable = false;
                }
            }
        }

        stopped = true;
    }

    /**
     * Check whether the current file has finished writing.
     * To do that, checks file size, waits for a while and checks
     * its size again. If the size remains the same, the file
     * is taken for complete.
     * @param filePath Absolute path to to file.
     * @return <code>true</code> When complete, <code>
     * false</code> otherwise.
     */
    private boolean isComplete(final String filePath) {
        File file = new File(filePath);
        long initialSize = file.length();
        long finalSize = initialSize;
        boolean complete = false;

        if (initialSize > 0) {
            try {
                Thread.sleep(SLEEP_BETWEEN_READS);
            } catch (InterruptedException e) {
                log.finer("Wait interrupted");
            }
            finalSize = file.length();

            complete = (finalSize == initialSize);
        }

        return complete;
    }

    /**
     * Detect files in input folder. If files are processable (name ends with *)
     * are added into the processable file list, otherwise are added into a
     * non-processable files list. A file is considered as non-processable if is still
     * being written by FTP system (has not an 'end-of-file' yet).
     */
    private void detect() {
        File f = new File(inputFolderPath);
        File[] files = f.listFiles();

        if (files != null) {
            String filePath = "";
            String fileName = "";
            boolean lockFile;
            int len = files.length;

            StatusIconFacade.busy();
            for (int cont = 0; cont < len; cont++) {
                filePath = files[cont].getAbsolutePath();
                fileName = files[cont].getName();
                lockFile = lh.tryLock(fileName);

                if (lockFile) {
                    try {
                        if (isComplete(filePath)) {
                            process(filePath, fileName);
                        }
                    } catch (FileNotFoundException ex) {
                        log.finer("[INPUT] [" + fileName + "] File has been deleted by other module.");
                    } catch (IOException ex) {
                        log.severe("[INPUT] [" + fileName + "] Cannot read file.");
                    } catch (SignatureException ex) {
                        log.severe("[INPUT] [" + fileName + "] Cannot sign file.");
                    } finally {
                        lh.releaseLock(fileName);
                    }
                }
            }

            StatusIconFacade.idle();
        }
    }

    /**
     * Process file with name and contents passed as arguments.
     * @param fullFileName Absolute path to file.
     * @param fileName Name of file.
     * @throws IOException If cannot read the file.
     * @throws SignatureException If cannot sign the file.
     */
    private void process(final String fullFileName, final String fileName) throws SignatureException, IOException {

        String execContext = "";
        String response = "";
        boolean isText = isTextFile(fullFileName);

        try {
            /* Sending message to system. */
            execContext = "Sending";
            response = sendMessage(fullFileName, isText);
        } catch (ClientException cE) {
            log.severe("[INPUT] [" + fullFileName + "] Cannot send message. [Context="
                    + execContext + "]");
        } catch (IllegalArgumentException ex) {
            log.severe("[INPUT] [" + fullFileName + "] Cannot send message. [Context="
                    + execContext + "]");
        }

        /* Regardless of whether the message has been put or not, file is deleted. */
        try {

            /* Incoming message is saved in Processed folder. */
            execContext = "Saving message in processed folder.";
            saveInputMessageAsProcessed(fullFileName, fileName, isText);

            /* Once processed, the original file is deleted. */
            execContext = "Deleting incominmg message.";
            deleteProcessedEntry(fileName);

            /* Response is saved in response folder. */
            execContext = "Saving server response.";
            saveResponse(fileName, isText, response);

        } catch (IOException ex) {
            log.severe("[INPUT] [" + fullFileName
                    + "] Cannot access filesystem. [Context=" + execContext + "]");
        }
    }

    /**
     * Check whether the file is text or binary
     * A file is taken for text-type if:
     * <li> Has not extension.
     * <li> Has an XML extension.
     * <li> Extension is a numeric value (version number).
     * @param fullPath Absolute path to file.
     * @return <code>true</code> If is a text file. <code>false</code>
     * otherwise.
     */
    private boolean isTextFile(final String fullPath) {
        boolean retValue = false;

        if (!fullPath.toLowerCase().endsWith(XML_FILE_EXTENSION)) {

            int lastPointPosition = fullPath.lastIndexOf('.');
            if (lastPointPosition == -1 || lastPointPosition + 1 == fullPath.length()) {

                /* Has not extension or file ends with "." .*/
                retValue = true;
            } else {
                String extension = fullPath.substring(lastPointPosition + 1);
                try {
                    Integer.parseInt(extension);
                    retValue = true;
                } catch (NumberFormatException ex) {

                    /* Extension is non-numeric. */
                    retValue = false;
                }
            }
        } else {

            /* Ends with .xml */
            retValue = true;
        }

        return retValue;
    }

    /**
     * Save the system response in the processed folder. There may be the following cases:<br>
     * <li> Fault: Server returns an Fault Exception. Outgoing message is saved with "fault" extension.
     * <li> NOOK: Message has been processed with errors, message is saved with NOOK extension.
     * <li> OK:  Message has been processed without errors. Extension is "out".
     * @param fileName processed file name.
     * @param isText Indicate text file.
     * @param response Response.
     * @throws IOException In cannot save the processed file.
     */
    private void saveResponse(final String fileName, final boolean isText, final String response) throws IOException {
        if (responseFolderPath != null) {
            FileUtil.writeUTF8(responseFolderPath + "/" + RESPONSE_ID_PREFIX + fileName, response);
        }
    }

    /**
     * Save incoming message in processed folder. Saved file has always "xml" extension.
     * @param fullFileName Full path to file.
     * @param fileName name of the file to send.
     * @param ficheroTexto Indicates whether the file is text or binary.
     * @throws IOException If cannot save file in folder.
     */
    private void saveInputMessageAsProcessed(final String fullFileName, final String fileName,
            final boolean ficheroTexto) throws IOException {
        if (processedFolderPath != null) {
            String fullProcessedName = processedFolderPath + "/" + fileName;
            try {
                FileUtil.writeUTF8(fullProcessedName, FileUtil.readUTF8(fullFileName));
            } catch (IOException ioe) {

                /*
                 * If file is not encoded as UTF-8 this will result in a
                 * <code>sun.io.MalformedInputException</code>.
                 * Since <code>sun.*</code> packages must not be used,
                 * generic IOException will be caught instead.
                 *
                 * File is read again using the default system encoding.
                 */
                FileUtil.writeUTF8(fullProcessedName, FileUtil.read(fullFileName));
            }
        }
    }

    /**
     * Delete incoming file once has been processed.
     * @param fileName Name of the file to delete.
     */
    private void deleteProcessedEntry(final String fileName) {

        File f = new File(inputFolderPath + "/" + fileName);

        if (!f.delete()) {
            log.warning("[INPUT] [" + fileName + "] Cannot delete Input folder file.");
        }
    }

    /**
     * Sends the message whose name and content are passed as arguments.
     * @param fullFileName Full path to file.
     * @param textFile Indicates whether the file is text or binary.
     * @return Response of the server.
     * @throws ClientException Exception with the error.
     * @throws IOException If cannot read the file.
     */
    private String sendMessage(final String fullFileName, final boolean textFile) throws ClientException, IOException {

        String response = "";
        try {

            response = putMessage.put(FileUtil.readUTF8(fullFileName));

        } catch (IOException ioe) {

            /*
             * If file is not encoded as UTF-8 this will result in a
             * <code>sun.io.MalformedInputException</code>.
             * Since <code>sun.*</code> packages must not be used,
             * generic IOException will be caught instead.
             *
             * File is read again using the default system encoding.
             */
            response = putMessage.put(FileUtil.read(fullFileName));
        }

        return response;
    }

    /**
     * Notifies to the thread cycle that must stop.
     */
    public void stopThread() {
        stop = true;
    }

    /**
     * Indicates whether the thread can be killed because is not performing any action (sleeping).
     * @return <code>true</code> If the thread can be killed <code>false</code> otherwise.
     */
    public boolean isKillable() {
        return killable;
    }

    /**
     * Return activity status of the thread.
     * @return <code>true</code> If thread cycle is stopped.
     * <code>false</code> if thread cycle is active.
     */
    public boolean isStopped() {
        return stopped;
    }
}

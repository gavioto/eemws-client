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
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.ree.eemws.client.exception.ClientException;
import es.ree.eemws.client.getmessage.GetMessage;
import es.ree.eemws.client.listmessages.ListMessages;
import es.ree.eemws.client.listmessages.MessageListEntry;
import es.ree.eemws.core.utils.file.FileUtil;

/**
 * <code>OutputDetectionThread</code> implements a loop that finds
 * and processes publication files.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 29/05/2014
 *
 */
public final class OutputDetectionThread extends Thread {

    /** Extension to be appended to generated files. */
    private static final String XML_FILE_EXTENSION = "xml";

    /** Output folder. */
    private String outputFolder;

    /** True when thread cannot be killed. */
    private boolean killable;

    /** Code for the last listed message. */
    private long lastListCode;

    /** Object which locks message for group working. */
    private LockHandler lh;

    /** Object for retrieve Lists of XML publications. */
    private ListMessages list;

    /** Thread log system. */
    private static Logger log = Logger.getLogger(Thread.currentThread().getStackTrace()[0].getClassName());

    /** Object for retrieve XML publications. */
    private GetMessage get;

    /** True if thread must finish execution. */
    private boolean stop;

    /** True if thread must has finished cycle. */
    private boolean stopped;

    /** Sleep time for thread between cycles. */
    private long sleepTime;

    /** List of message types to retrieve. */
    private String typesToRetrieveList = null;

    /**
     * Constructor. Initializes parameters for detection thread.
     * @param lockHandler Lock Manager.
     * @param config Module configuration.
     */
    public OutputDetectionThread(final LockHandler lockHandler, final Configuration config) {

        outputFolder = config.getOutputFolder();
        sleepTime = config.getSleepTimeOutput();
        URL endPoint = config.getUrlEndPoint();
        typesToRetrieveList = config.getMessagesTypeList();
        stop = false;
        stopped = true;
        killable = true;
        lastListCode = 0;
        lh = lockHandler;


        list = new ListMessages();
        list.setEndPoint(endPoint);
        list.setVerifyResponse(config.isVerifySignResponse());
        list.setSignRequest(config.isSignResquest());

        get = new GetMessage();
        get.setEndPoint(endPoint);
        get.setVerifyResponse(config.isVerifySignResponse());
        get.setSignRequest(config.isSignResquest());

        if (log.isLoggable(Level.CONFIG)) {
            StringBuffer msg = new StringBuffer();
            msg.append("\n[OUTPUT] Output folder: ");
            msg.append(outputFolder);
            msg.append("\n[OUTPUT] URL: ");
            msg.append(endPoint.toString());
            msg.append("\n[OUTPUT] Delay between detections: ");
            msg.append(sleepTime);
            log.config(msg.toString());
        }
    }

    /**
     * Create Publication File.
     *
     * @param messageListElement List element retrieved in detection process.
     */
    private void createMessage(final MessageListEntry messageListElement) {

        String fileName = messageListElement.getMessageIdentification() + "." + messageListElement.getVersion() + ".";

        get.setSignRequest(System.getProperty(Configuration.SIGN_RESQUEST, "TRUE").toUpperCase().trim().equalsIgnoreCase("TRUE"));
        get.setVerifyResponse(System.getProperty(Configuration.VERIFY_SIGN_RESPONSE, "FALSE").toUpperCase().trim().equalsIgnoreCase("TRUE"));

        String response = null;

        fileName += XML_FILE_EXTENSION;
        boolean lockFile = lh.tryLock(fileName);
        if (lockFile && !FileUtil.exists(outputFolder + "/" + fileName)) {

            /*
             * File is created as temporary, this ensures a half-done file will no be stored as a definitive one
             * in case of system stop. Once the file is completely generated, is renamed as definitive.
             */

            String type = messageListElement.getType();
            String identification = messageListElement.getMessageIdentification();
            Integer version = messageListElement.getVersion().intValue();

            try {
                response = get.get(type, identification, version);
                File tmpFile;
                tmpFile = File.createTempFile("output_", null, new File(outputFolder));
                FileUtil.write(tmpFile.getAbsolutePath(), response);
                tmpFile.renameTo(new File(outputFolder + "/" + fileName));

            } catch (ClientException e) {
                log.severe("[OUTPUT] [" + fileName + "] Cannot request message");
            } catch (IOException e) {
                log.severe("[OUTPUT] [" + fileName + "] Cannot access file system.");
            } finally {
                lh.releaseLock(fileName);
            }
        }
    }

    /**
     * Processes the retrieved message Array.
     * For each received message a publication creation method
     * is called depending of the type of message.
     * @param messageList List of messages for processing (create file if are publications).
     */
    private void procesa(final List<MessageListEntry> messageList) {
        int len = messageList.size();

        if (len > 1) {
            StatusIconFacade.busy();
        }

        for (final MessageListEntry message : messageList) {
            String typeToCompareWith = ";" + message.getType() + ";";
            int msgCode = message.getCode().intValue();
            if (typesToRetrieveList == null
                    || (typesToRetrieveList != null && typesToRetrieveList.indexOf(typeToCompareWith) != -1)) {

                createMessage(message);
            }

            /* Take the highest message code to start listing form this one. */
            if (msgCode > lastListCode) {
                lastListCode = msgCode;
            }
        }

        if (len > 1) {
            StatusIconFacade.idle();
        }
    }

    /**
     * Method obtained out of a list of files. List is obtained from
     * the highest code of the last listed item.
     * @return Array containing listed items.
     */
    private List<MessageListEntry> getFileList() {
        List<MessageListEntry> response = null;
        try {
            response = list.list(lastListCode);
        } catch (ClientException e) {
            log.severe("Cannot retrieve message list.");
        }
        return response;
    }

    /**
     * Detection cycle.
     */
    public void run() {
        stopped = false;
        long sleep;
        long start;
        long fin;

        killable = false;

        while (!stop) {
            sleep = sleepTime;
            start = System.currentTimeMillis();

            try {
                List<MessageListEntry> messageList = getFileList();
                procesa(messageList);
            } catch (RuntimeException ex) {
                log.severe("[OUTPUT] Error in detection cycle");
            }

            fin = System.currentTimeMillis();
            sleep -= (start - fin);

            if (sleep > 0) {
                try {
                    killable = true;
                    Thread.sleep(sleep);
                    killable = false;
                } catch (InterruptedException ex) {

                    /* Ignore interuptedException. */
                    killable = false;
                }
            }
        }

        stopped = true;
    }

    /**
     * Notifies the thread must stop.
     */
    public void stopThread() {
        stop = true;
    }

    /**
     * Indicate whether the thread can be killed because is not
     * performing any action (sleeping).
     * @return <code>true</code> If thread can be killed.
     * <code>false</code> otherwise.
     */
    public boolean isKillable() {
        return killable;
    }

    /**
     * Return the thread status.
     * @return <code>true</code> If thread cycle is stopped.
     * <code>false</code> If thread cycle is running.
     */
    public boolean isStopped() {
        return stopped;
    }

}

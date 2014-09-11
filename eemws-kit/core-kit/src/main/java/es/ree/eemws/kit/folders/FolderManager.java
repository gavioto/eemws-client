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
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JOptionPane;

import es.ree.eemws.core.utils.config.ConfigException;

/**
 * Application to manage message folders using folders.
 * Messages are saved / retrieved in folders to be
 * generated / sent by e·trans.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 09/05/2014
 *
 */
public final class FolderManager {

    /**
     * Number of milliseconds that the system is waiting for
     * the expiration of the associated threads to exit.
     */
    private static final int THREAD_EXPIRATION_WAIT = 2000;
    /** logging. */
    private static Logger log = Logger.getLogger(Thread.currentThread().getStackTrace()[0].getClassName());

    /**
     * Create all the folder managers and starts application.
     */
    public void execApplication() {
        try {
            startLogging();
            Configuration config = new Configuration();
            config.readConfiguration();
            config.validateConfiguration();

            ExecutionControl ce = new ExecutionControl(config);
            if (ce.alreadyRunning()) {
                String errMsg = "Other Magic Folder insance already running.";

                if (System.getProperty("folder.intercativo") != null) {
                    JOptionPane.showMessageDialog(null, errMsg, "Error", JOptionPane.ERROR_MESSAGE);
                }
                log.info("[FOLDER]" + errMsg);
            } else {

                /* Status images Initialization. */
                StatusIconFacade.initializeStatusIconFacade(config);

                /* Create lock handler. */
                LockHandler lh;
                lh = createLockHandler(config);

                /* Create input detector only if an input folder is set. */
                if (config.getInputFolder() != null) {
                    createInputThread(lh, config);
                }

                /* Create output detector only if an output folder is set. */
                if (config.getOutputFolder() != null) {
                    createOutputThread(lh, config);
                }

                /* Create deletion / backup folder. */
                createDeletingThread(lh, config);

                log.info("Application started. Detecting...");
            }

        } catch (ConfigException ex) {
            log.severe("Configuration error. " + ex.getMessage());
        } catch (RemoteException ex) {
            log.severe("Cannot retrieve / register remote references.");
        } catch (MalformedURLException ex) {
            log.severe("Cannot retrieve URL for attachment send.");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }

    /**
     * Creates a thread that will delete all files (output, processed,
     * temporary, common) which had been created by application when has
     * passed an interval configurable through the key <code>NUM_DIAS_MANTENER</code>
     * (default value = 7).
     *
     * @param lh Lock manager to synchronize the threads
     * @param conf Module settings.
     */
    private void createDeletingThread(final LockHandler lh, final Configuration conf) {
        DeleteProcessedFilesThread dPFT = new DeleteProcessedFilesThread(lh, conf);
        dPFT.start();
    }

    /**
     * Create thread for detection / input messages processing.
     * @param lh Lock manager to synchronize the threads.
     * @param conf Module settings.
     * @throws MalformedURLException If cannot obtain an access URL for attachment services.
     */
    private void createInputThread(final LockHandler lh, final Configuration conf)  throws MalformedURLException {

        final InputDetectionThread dt = new InputDetectionThread(lh, conf);
        dt.start();

        Runnable shutdownHook = new Runnable() {
            public void run() {
                dt.stopThread();

                while (!dt.isStopped() && !dt.isKillable()) {
                    try {
                        Thread.sleep(THREAD_EXPIRATION_WAIT);
                    } catch (InterruptedException ex) {
                        log.finest("Input thread interrupted.");
                    }
                }
            }
        };

        Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));
    }

    /**
     * Create thread for detection / output messages processing.
     * @param lh Lock manager to synchronize the threads.
     * @param conf Module settings.
     */
    private void createOutputThread(final LockHandler lh, final Configuration conf) {
        final OutputDetectionThread dt = new OutputDetectionThread(lh, conf);
        dt.start();

        Runnable shutdownHook = new Runnable() {
            public void run() {
                dt.stopThread();

                while (!dt.isStopped() && !dt.isKillable()) {
                    try {
                        Thread.sleep(THREAD_EXPIRATION_WAIT);
                    } catch (InterruptedException ex) {
                        log.finest("Output thread interrupted.");
                    }
                }
            }
        };

        Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));
    }

    /**
     * Create lock Handler.
     * @param conf Module configuration.
     * @return Controller object to synchronize the threads.
     * @throws RemoteException if cannot create lock manager (cannot get RMI communication).
     */
    private LockHandler createLockHandler(final Configuration conf) throws RemoteException {
        return new LockHandler(conf);
    }

    /**
     * Main program. Create an instance of detector.
     * @param args Commandline arguments -ignored-
     */
    public static void main(final String[] args) {
        Locale.setDefault(Locale.ENGLISH);
        FolderManager soporte = new FolderManager();
        soporte.execApplication();
    }

    /**
     * Initialize file logging for application.
     * @throws IOException Exception with the error.
     */
    private void startLogging() throws IOException {

        String fileNamePattern = "../logs/magic_folder_%g.txt";

        File f = new File(fileNamePattern);
        f.getParentFile().mkdirs();

        FileHandler fileHandler = new FileHandler(fileNamePattern, 1000000, 99);
        fileHandler.setFormatter(new SimpleFormatter());

        log.addHandler(fileHandler);
        log.setLevel(Level.INFO);

    }
}


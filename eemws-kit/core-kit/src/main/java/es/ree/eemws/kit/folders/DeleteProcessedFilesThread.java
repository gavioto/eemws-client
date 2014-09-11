/*
 * Copyright 2014 Red El�ctrica de Espa�a, S.A.U.
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
 * reference to Red El�ctrica de Espa�a, S.A.U. as the copyright owner of
 * the program.
 */

package es.ree.eemws.kit.folders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import es.ree.eemws.core.utils.file.FileUtil;

/**
 * <code>EliminaFicherosProcesadosThread</code>
 * deletes files stored in system that were generated over
 * {@link #numberOfDays} days ago.
 *

 * @author Red El�ctrica de Espa�a, S.A.U.
 * @version 1.0 29/05/2014
 *
 */
public final class DeleteProcessedFilesThread extends Thread {

    /** Milliseconds that the thread remains inactive before next execution. */
    private static final long TIME_SLEEP = 24 * 60 * 60 * 1000;

    /** Output folder ID. */
    private static final int OUTPUT_FOLDER_ID = 0;

    /** Processed folder ID. */
    private static final int PROCESSED_FOLDER_ID = 1;

    /** Temporal folder ID. */
    private static final int RESPONSE_FOLDER_ID = 2;

    /** Number of folders. */
    private static final int NUMBER_OF_FOLDERS = 3;

    /** Backup folder. */
    private String backupFolder;

    /**  Folders containing files that will be deleted. */
    private String[] foldersToEmpty;

    /** Object for message checking generated by other node. */
    private LockHandler lh;

    /** Thread log system. */
    private static Logger log = Logger.getLogger(Thread.currentThread().getStackTrace()[0].getClassName());


    /** Number of days during a generated file will exist. */
    private int numberOfDays;

    /**
     * Constructor. Receives work parameters from thread.
     * @param lockHandler Lock manager. Prevents multiple nodes
     * from create backup at once.
     * @param config Module settings.
     */
    public DeleteProcessedFilesThread(final LockHandler lockHandler, final Configuration config) {
        foldersToEmpty = new String[NUMBER_OF_FOLDERS];
        foldersToEmpty[OUTPUT_FOLDER_ID] = config.getOutputFolder();
        foldersToEmpty[PROCESSED_FOLDER_ID] = config.getProcessedFolder();
        foldersToEmpty[RESPONSE_FOLDER_ID] = config.getResponseFolder();
        numberOfDays = config.getNumOfDaysKept();
        backupFolder = config.getBackupFolder();
        lh = lockHandler;
    }

    /**
     * Delete files in specified folders which modification date is
     * previous or equal to the deletion date.
     */
    private void remove() {

        /* Deletion date. */
        GregorianCalendar gc = new GregorianCalendar();
        gc.add(Calendar.DATE, -numberOfDays);

        long deleteDate = gc.getTimeInMillis();

        /* File utils to be used in case of backup. */
        ZipOutputStream zosTmp = null;
        File tmpZip = null;
        File bakZip = null;
        SimpleDateFormat sdf = new SimpleDateFormat("'backup_'ddMMyyyy'.zip'");

        String bakZipName = sdf.format(new Date(deleteDate));

        if (backupFolder != null) {

            /*
             * If backup folder is set but this node cannot
             * generate file (is already being generated by
             * another one) then must exit.
             * Neither files can be created nor backup file
             * which is generating can be modified.
             */
            if (!lh.tryLock(bakZipName)) {
                return;
            }

            tmpZip = new File(backupFolder + "/tmp.zip");
            bakZip = new File(backupFolder + "/" + bakZipName);
        }

        int len = foldersToEmpty.length;

        for (int count = 0; count < len; count++) {
            if (foldersToEmpty[count] != null) {

                /* For each received folder. */
                File folder = new File(foldersToEmpty[count]);

                /* Retrieve list of contained files. */
                File[] fileList = folder.listFiles();

                if (fileList != null) {

                    /* If files found... */
                    int numberOfFiles = fileList.length;

                    for (int count2 = 0; count2 < numberOfFiles; count2++) {

                        /* Retrieve modification date from file, if this
                         * date precedes the date set for deletion
                         * the file is deleted. */

                        if (fileList[count2].lastModified() <= deleteDate) {
                            try {

                                /* If there is a folder set for backups (zip). */
                                if (backupFolder != null) {
                                    if (zosTmp == null) { // Zip not created yet.
                                        zosTmp = new ZipOutputStream(new FileOutputStream(tmpZip));

                                        if (bakZip.exists()) { // Existent zip file. Will add entries on it
                                            ZipInputStream zis = new ZipInputStream(new FileInputStream(bakZip));
                                            ZipEntry ze;
                                            byte[] buffer = new byte[1];

                                            while ((ze = zis.getNextEntry()) != null) {
                                                zosTmp.putNextEntry(ze);

                                                while (zis.available() != 0) {
                                                    zis.read(buffer, 0, 1);
                                                    zosTmp.write(buffer, 0, 1);
                                                }

                                                zis.closeEntry();
                                                zosTmp.closeEntry();
                                            }

                                            zis.close();
                                        }
                                    }

                                    zosTmp.putNextEntry(new ZipEntry(fileList[count2].getName()));

                                    String content = FileUtil.read(fileList[count2].getAbsolutePath());

                                    zosTmp.write(content.getBytes());
                                    content = null;
                                    zosTmp.closeEntry();
                                }
                            } catch (IOException ex) {
                                log.severe("[BACKUP] Error creating backup file.");
                            }

                            /* In any case, file is deleted. */
                            fileList[count2].delete();
                        }
                    }
                }
            }
        }
        if (zosTmp != null) {
            try {
                zosTmp.close();
                bakZip.delete();
                tmpZip.renameTo(bakZip);
            } catch (IOException ex) {
                log.severe("[BACKUP] Error deleting temporary backup file.");
            }
        }

        lh.releaseLock(bakZipName);
    }

    /**
     * Thread cycle. Just consists on sleep / delete.
     */
    public void run() {
        while (true) {
            try {
                remove();
            } catch (RuntimeException ex) {
                log.severe("[BACKUP] Error creating temporary backup file.");
            }

            try {
                Thread.sleep(TIME_SLEEP);
            } catch (InterruptedException ex) {
                log.finest("[BACKUP] Waiting lapse for backup creation cycle has been interrupted.");
            }
        }
    }
}
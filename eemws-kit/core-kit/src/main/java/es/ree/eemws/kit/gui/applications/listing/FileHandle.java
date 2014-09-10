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
package es.ree.eemws.kit.gui.applications.listing;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import es.ree.eemws.core.utils.file.FileUtil;
import es.ree.eemws.kit.gui.applications.Logger;
import es.ree.eemws.kit.gui.common.Constants;


/**
 * File management for save retrieved messages.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 02/06/2014
 */
public final class FileHandle {

    /** Reference to main window. */
    private Lists mainWindow;

    /** Logging object. */
    private Logger logger;

    /** Preset saving folder, prevents asking user every time. */
    private String storeFolder = "";

    /** Save As... dialogue */
    private JFileChooser fcFileChooser = new JFileChooser();

    /** Set active to create backup files are automatically. */
    private JCheckBoxMenuItem cbmiBackupAuto = new JCheckBoxMenuItem();

    /** Set active to save without asking. */
    private JCheckBoxMenuItem cbmiStoreAuto = new JCheckBoxMenuItem();

    /**
     * Create a new instance of File settings manager.
     * @param window Reference to the main window.
     */
    public FileHandle(final Lists window) {
        mainWindow = window;
        logger = mainWindow.getLogHandle().getLog();
    }

    /**
     * Get File menu.
     * @return 'File' menu option.
     */
    public JMenu getMenu() {

        /* Create backup option. */
        cbmiBackupAuto.setText("Automatically create backup");
        cbmiBackupAuto.setSelected(false);
        cbmiBackupAuto.setMnemonic('b');
        cbmiBackupAuto.setToolTipText("If a retieved file already exists in the system, a backup file will be created");

        /* 'save automatically' option. */
        cbmiStoreAuto = new JCheckBoxMenuItem("Automatically save in default folder",
                new ImageIcon(getClass().getResource(Constants.ICON_SAVE)));
        cbmiStoreAuto.setSelected(false);
        cbmiStoreAuto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                selectFolder();
            }
        });
        cbmiStoreAuto.setMnemonic('G');
        cbmiStoreAuto.setToolTipText("Prevents asking for destination folder every time time a message is retrieved.");

        /* 'exit' option. */
        JMenuItem miExit = new JMenuItem();
        miExit.setText("Exit");
        miExit.setMnemonic('S');
        miExit.setToolTipText("Exits application");
        miExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                exitApplication();
            }
        });

        /* 'file' menu. */
        JMenu mnFileMenu = new JMenu();
        mnFileMenu.setText("File");
        mnFileMenu.setMnemonic('r');
        mnFileMenu.add(cbmiBackupAuto);
        mnFileMenu.add(cbmiStoreAuto);
        mnFileMenu.addSeparator();
        mnFileMenu.add(miExit);

        return mnFileMenu;
    }

    /**
     * Open a dialog window to set a folder for Retrieved messages,
     * thus user will not be asked every time a message is retrieved.
     */
    private void selectFolder() {
        if (cbmiStoreAuto.isSelected()) {
            fcFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int returnVal = fcFileChooser.showSaveDialog(mainWindow);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fcFileChooser.getSelectedFile();

                storeFolder = file.getAbsolutePath();
            } else {
                cbmiStoreAuto.setSelected(false);
            }
        }
    }

    /**
     * Saves retrieved message using the ID passed as parameter.
     * @param idMensaje ID for the message to save.
     * @param response Request object containing requested message data.
     * @throws IOException If cannot write file.
     */
    public void saveFile(final String idMensaje, final String response) throws IOException {

        String fileName = idMensaje;
        fileName += ".xml";

        int returnVal;
        if (cbmiStoreAuto.isSelected()) {
            returnVal = JFileChooser.APPROVE_OPTION;
        } else {
            fcFileChooser = new JFileChooser();
            fcFileChooser.setSelectedFile(new File(fileName));
            returnVal = fcFileChooser.showSaveDialog(mainWindow);
        }


        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file;

            if (cbmiStoreAuto.isSelected()) {
                file = new File(storeFolder + "/" + fileName);
            } else {
                file = fcFileChooser.getSelectedFile();
            }

            boolean save = true;
            if (file.exists()) {
                if (cbmiBackupAuto.isSelected()) {
                    String nomBackup = FileUtil.createBackup(file.getAbsolutePath());
                    logger.logMessage("Backup created " + nomBackup);
                } else {
                    int answer = JOptionPane.showConfirmDialog(mainWindow,
                            "File " + file.getName() + " already exist. \n¿Replace?",
                            "Replace existing file",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (answer != JOptionPane.OK_OPTION) {
                        logger.logMessage("Cannot replace "
                                + file.getName() + ". Mesage not saved.");
                        save = false;
                    }
                }
            }

            if (save) {
                FileUtil.writeUTF8(file.getAbsolutePath(), response);
                logger.logMessage("Saving message " + file.getName());
            }
        }
    }

    /**
     * Ask for user confirmation before exiting application.
     */
    public void exitApplication() {
        int answer = JOptionPane.showConfirmDialog(mainWindow,
                "¿Exit application?",
                "Confirm:",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (answer == JOptionPane.OK_OPTION) {
            System.exit(0);
        }
    }

}

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
package es.ree.eemws.kit.gui.applications.editor;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import es.ree.eemws.core.utils.file.FileUtil;
import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.kit.gui.common.Logger;


/**
 * Processing of File actions.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class FileHandler extends DropTargetAdapter {

    /**
     * String to notify on title that the current
     * document has not been saved yet.
     */
    public static final String NEW_FILE = Messages.getString("EDITOR_NEW_FILE_TITLE"); //$NON-NLS-1$

    /** Character to notify in the application title that document has been modified. */
    public static final String MODIFIED_FILE_CHAR = "*"; //$NON-NLS-1$

    /** File name start character token. */ 
    private static final String TITLE_START = "["; //$NON-NLS-1$
    
    /** File name end character token. */
    private static final String TITLE_END = "]"; //$NON-NLS-1$
    
    /** Editable text handler. */
    private DocumentHandle documentHandle = null;

    /** File menu. */
    private JMenu fileMenu = new JMenu();

    /** Button bar. */
    private JToolBar toolBar = new JToolBar();

    /** Log window. */
    private Logger log;

    /** Main window. */
    private Editor mainWindow = null;

    /** File currently edited. */
    private File currentFile = null;

    /**
     * Creates a new instance of the File handler.
     * @param window Reference to main window.
     */
    public FileHandler(final Editor window) {

        mainWindow = window;
        documentHandle = mainWindow.getDocumentHandle();
        log = mainWindow.getLogHandle().getLog();

        /* Enables the editor open files by dropping items. */
        new DropTarget(documentHandle.getDocument(), this);
        newFile();
    }

    /**
     * Gets file menu.
     * @return File Menu items for the main menu.
     */
    public JMenu getMenu() {

        JMenuItem newFileMenuItem = new JMenuItem(Messages.getString("EDITOR_MENU_ITEM_NEW"), new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_NEW))); //$NON-NLS-1$
        newFileMenuItem.setMnemonic(Messages.getString("EDITOR_MENU_ITEM_NEW_HK").charAt(0)); //$NON-NLS-1$
        newFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {  // NOSONAR event is not used.
                newFile();
            }
        });

        JMenuItem openFileMenuItem = new JMenuItem(Messages.getString("EDITOR_MENU_ITEM_OPEN"), new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_OPEN))); //$NON-NLS-1$
        openFileMenuItem.setMnemonic(Messages.getString("EDITOR_MENU_ITEM_OPEN_HK").charAt(0)); //$NON-NLS-1$
        openFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) { // NOSONAR event is not used.
                openFile();
            }
        });

        JMenuItem saveMenuItem = new JMenuItem(Messages.getString("EDITOR_MENU_ITEM_SAVE"), new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_SAVE))); //$NON-NLS-1$
        saveMenuItem.setMnemonic(Messages.getString("EDITOR_MENU_ITEM_SAVE_HK").charAt(0)); //$NON-NLS-1$
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke('S', InputEvent.CTRL_MASK));
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) { // NOSONAR event is not used.
                saveFile();
            }
        });

        JMenuItem saveAsMenuItem = new JMenuItem(Messages.getString("EDITOR_MENU_ITEM_SAVE_AS"), new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_SAVE_AS))); //$NON-NLS-1$
        saveAsMenuItem.setMnemonic(Messages.getString("EDITOR_MENU_ITEM_SAVE_AS_HK").charAt(0)); //$NON-NLS-1$
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) { // NOSONAR event is not used.
                saveFileAs();
            }
        });

        JMenuItem menuItemExit = new JMenuItem();
        menuItemExit.setText(Messages.getString("EDITOR_MENU_ITEM_EXIT")); //$NON-NLS-1$
        menuItemExit.setMnemonic(Messages.getString("EDITOR_MENU_ITEM_EXIT").charAt(0)); //$NON-NLS-1$
        menuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) { // NOSONAR event is not used.
                exitProgram();
            }
        });

        fileMenu.setText(Messages.getString("EDITOR_MENU_ITEM_FILE")); //$NON-NLS-1$
        fileMenu.setMnemonic(Messages.getString("EDITOR_MENU_ITEM_FILE").charAt(0)); //$NON-NLS-1$
        fileMenu.add(newFileMenuItem);
        fileMenu.add(openFileMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(menuItemExit);

        return fileMenu;
    }

    /**
     * Returns button bar related to File options.
     * @return Option bar related to File options.
     */
    public JToolBar getButtonBar() {

        toolBar.setFloatable(true);

        JButton newDocumentButton = new JButton();
        newDocumentButton.setIcon(new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_NEW)));
        newDocumentButton.setToolTipText(Messages.getString("EDITOR_MENU_ITEM_NEW")); //$NON-NLS-1$
        newDocumentButton.setBorderPainted(false);
        newDocumentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {  // NOSONAR event is not used.
                newFile();
            }
        });

        JButton openFileButton = new JButton();
        openFileButton.setToolTipText(Messages.getString("EDITOR_MENU_ITEM_OPEN")); //$NON-NLS-1$
        openFileButton.setBorderPainted(false);
        openFileButton.setIcon(new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_OPEN)));
        openFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {  // NOSONAR event is not used.
                openFile();
            }
        });

        JButton saveFileButton = new JButton();
        saveFileButton.setIcon(new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_SAVE)));
        saveFileButton.setToolTipText(Messages.getString("EDITOR_MENU_ITEM_SAVE")); //$NON-NLS-1$
        saveFileButton.setBorderPainted(false);
        saveFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {  // NOSONAR event is not used.
                saveFile();
            }
        });

        toolBar.add(newDocumentButton, null);
        toolBar.add(openFileButton, null);
        toolBar.add(saveFileButton, null);
        return toolBar;
    }

    /**
     * Opens a file in edit area.
     * @param file File to be opened.
     */
    private void openFile(final File file) {

        if (file.exists()) {

            try {

                documentHandle.openIrreversible(new StringBuilder(FileUtil.readUTF8(file.getAbsolutePath())));
                log.logMessage(Messages.getString("EDITOR_OPENING_FILE", file.getAbsolutePath())); //$NON-NLS-1$
                mainWindow.setTitle(TITLE_START + file.getName() + TITLE_END); 
                currentFile = file;

            } catch (IOException ioe) {
                String errMsg = Messages.getString("EDITOR_CANNOT_OPEN_FILE", file.getAbsolutePath(), ioe.getMessage()); //$NON-NLS-1$
                JOptionPane.showMessageDialog(mainWindow, errMsg, Messages.getString("MSG_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
                log.logMessage(errMsg);
            }
        }
    }

    /**
     * Opens a file in editor.
     */
    private void openFile() {

        if (hasUserPermission()) {

            JFileChooser fileChooser = new JFileChooser();
            int returnVal = fileChooser.showOpenDialog(mainWindow);
            if (returnVal == JFileChooser.APPROVE_OPTION) {

                openFile(fileChooser.getSelectedFile());
            }
        }
    }

    /**
     * Saves content area in file, using the already existent file name.
     */
    private void saveFile() {

        if (currentFile == null) {
            saveFileAs();

        } else {
            saveFile(currentFile);
        }
    }

    /**
     * Saves Text area content in file.
     */
    private void saveFileAs() {

        if (documentHandle.isEmpty()) {

            log.logMessage(Messages.getString("EDITOR_NOTHING_TO_SAVE")); //$NON-NLS-1$
            JOptionPane.showMessageDialog(mainWindow, Messages.getString("EDITOR_NOTHING_TO_SAVE"), Messages.getString("MSG_WARNING_TITLE"), JOptionPane.INFORMATION_MESSAGE);  //$NON-NLS-1$//$NON-NLS-2$

        } else {

            String fileNameFromTitle = mainWindow.getTitle();
            fileNameFromTitle = fileNameFromTitle.substring(fileNameFromTitle.indexOf(TITLE_START) + 1, fileNameFromTitle.indexOf(TITLE_END));  
 
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(fileNameFromTitle));
            int returnVal = fileChooser.showSaveDialog(mainWindow);

            if (returnVal == JFileChooser.APPROVE_OPTION) {

                File file = fileChooser.getSelectedFile();
                if (file.exists()) {

                    int resp = JOptionPane.showConfirmDialog(mainWindow, 
                            Messages.getString("EDITOR_SAVE_FILE_ALREADY_EXISTS", file.getName()), //$NON-NLS-1$
                            Messages.getString("MSG_WARNING_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$

                    if (resp != JOptionPane.OK_OPTION) {

                        return;
                    }

                    log.logMessage(Messages.getString("EDITOR_SAVE_FILE_OVERWRITTEN", file.getAbsolutePath())); //$NON-NLS-1$
                }

                saveFile(file);
            }
        }
    }

    /**
     * Saves a file.
     * @param file File.
     */
    private void saveFile(final File file) {

        try {

            String contenido = documentHandle.getPlainText();
            FileUtil.writeUTF8(file.getAbsolutePath(), contenido);
            log.logMessage(Messages.getString("EDITOR_SAVE_FILE_SAVED", file.getAbsolutePath())); //$NON-NLS-1$
            currentFile = file;
            mainWindow.setTitle(TITLE_START + file.getName() + TITLE_END);  
            
        } catch (IOException ioe) {

            String errMsg = Messages.getString("EDITOR_UNABLE_TO_SAVE", file.getAbsolutePath());  //$NON-NLS-1$
            
            JOptionPane.showMessageDialog(mainWindow, errMsg, Messages.getString("MSG_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
                        
            log.logException(errMsg, ioe);
        }
    }

    /**
     * CreateS a new file.
     */
    private void newFile() {

        if (hasUserPermission()) {
            documentHandle.openIrreversible(new StringBuilder("")); //$NON-NLS-1$
            mainWindow.setTitle(TITLE_START + NEW_FILE + TITLE_END);
            log.logMessage(NEW_FILE);
            currentFile = null;
        }
    }

    /**
     * In case the message has been edited, application will ask user for confirmation before
     * overwriting content (open, new, drop).
     * @return <code>true</code> If changes are ignored. <code>false</code> otherwise.
     */
    private boolean hasUserPermission() {

        boolean permited = true;

        String title = mainWindow.getTitle();

        if (title.indexOf(MODIFIED_FILE_CHAR) != -1) {

            String fileName = title.substring(0, title.indexOf(MODIFIED_FILE_CHAR));
            fileName = fileName.substring(fileName.indexOf(TITLE_START) + 1, fileName.indexOf(TITLE_END));

            int answer = JOptionPane.showConfirmDialog(mainWindow, 
                    Messages.getString("EDITOR_LOSE_CHANGES", fileName),  //$NON-NLS-1$
                    Messages.getString("MSG_WARNING_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); //$NON-NLS-1$
            
            
            permited = (answer == JOptionPane.OK_OPTION);
            
        }

        return permited;
    }

    /**
     * Method invoked before exit.
     */
    public void exitProgram() {

        if (hasUserPermission()) {
            int res = JOptionPane.showConfirmDialog(mainWindow, Messages.getString("EDITOR_EXIT_APPLICATION"),  //$NON-NLS-1$
                    Messages.getString("MSG_QUESTION_TITLE"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE); //$NON-NLS-1$

            if (res == JOptionPane.OK_OPTION) {
                System.exit(0); //NOSONAR We want to force application to exit.
            }
        }
    }

    /**
     * Triggered when a link to a file (or any object else) is dropped on editor.
     * @param eventoDrop Event triggered containing data related to dropped object.
     */
    @SuppressWarnings({ "rawtypes" })
    public void drop(final DropTargetDropEvent eventoDrop) {

        if (eventoDrop.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {

            try {

                eventoDrop.acceptDrop(DnDConstants.ACTION_LINK);
                List obj = (List) eventoDrop.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                File file = (File) obj.get(0);
                if (!file.isDirectory()) {

                    if (hasUserPermission()) {
                        openFile(file);
                    }

                } else {

                    JOptionPane.showMessageDialog(mainWindow, Messages.getString("EDITOR_CANNOT_LOAD_FOLDER"),  //$NON-NLS-1$
                            Messages.getString("MSG_ERROR_TITLE"), //$NON-NLS-1$ 
                            JOptionPane.ERROR_MESSAGE); 
                    eventoDrop.dropComplete(false);
                }

            } catch (UnsupportedFlavorException | IOException e) {

                eventoDrop.dropComplete(false);
            }

        } else {

            eventoDrop.rejectDrop();
        }

    }

    /**
     * Enables / disables graphic values.
     * @param activeValue <code>true</code> Enable.
     * <code>false</code> Disable.
     */
    public void enable(final boolean activeValue) {

        for (Component comp : toolBar.getComponents()) {
            comp.setEnabled(activeValue);
        }
        
        for (Component comp : fileMenu.getMenuComponents()) {
            comp.setEnabled(activeValue);
        }
    }
}

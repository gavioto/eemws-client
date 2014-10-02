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
import java.text.MessageFormat;
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
import es.ree.eemws.kit.gui.applications.Logger;


/**
 * Processing of File actions.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class FileHandle extends DropTargetAdapter {

    /**
     * String to notify on title that the current
     * document has not been saved yet.
     */
    public static final String NEW_FILE = Messages.getString("kit.gui.editor.38");

    /** Character to notify on title that document has been modified. */
    private static final String MODIFIED_FILE_CHAR = "*";

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
    public FileHandle(final Editor window) {

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

        JMenuItem newFileMenuItem = new JMenuItem(Messages.getString("kit.gui.editor.39"), new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_NEW)));
        newFileMenuItem.setMnemonic('N');
        newFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {

                newFile();
            }
        });

        JMenuItem openFileMenuItem = new JMenuItem(Messages.getString("kit.gui.editor.40"), new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_OPEN)));
        openFileMenuItem.setMnemonic('O');
        openFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                openFile();
            }
        });

        JMenuItem saveMenuItem = new JMenuItem(Messages.getString("kit.gui.editor.41"), new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_SAVE)));
        saveMenuItem.setMnemonic('S');
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke('S', InputEvent.CTRL_MASK));
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                saveFile();
            }
        });

        JMenuItem saveAsMenuItem = new JMenuItem(Messages.getString("kit.gui.editor.42"), new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_SAVE_AS)));
        saveAsMenuItem.setMnemonic('A');
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                saveFileAs();
            }
        });

        JMenuItem menuItemExit = new JMenuItem();
        menuItemExit.setText(Messages.getString("kit.gui.editor.43"));
        menuItemExit.setMnemonic('x');
        menuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                exitProgram();
            }
        });

        fileMenu.setText(Messages.getString("kit.gui.editor.44"));
        fileMenu.setMnemonic('F');
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
        newDocumentButton.setToolTipText(Messages.getString("kit.gui.editor.38"));
        newDocumentButton.setBorderPainted(false);
        newDocumentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                newFile();
            }
        });

        JButton openFileButton = new JButton();
        openFileButton.setToolTipText(Messages.getString("kit.gui.editor.45"));
        openFileButton.setBorderPainted(false);
        openFileButton.setIcon(new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_OPEN)));
        openFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                openFile();
            }
        });

        JButton saveFileButton = new JButton();
        saveFileButton.setIcon(new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_SAVE)));
        saveFileButton.setToolTipText(Messages.getString("kit.gui.editor.46"));
        saveFileButton.setBorderPainted(false);
        saveFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                saveFile();
            }
        });

        toolBar.add(newDocumentButton, null);
        toolBar.add(openFileButton, null);
        toolBar.add(saveFileButton, null);
        return toolBar;
    }

    /**
     * Open file in Edit area.
     * @param file File to be opened.
     */
    private void openFile(final File file) {

        if (file.exists()) {

            try {

                documentHandle.openIrreversible(new StringBuilder(FileUtil.readUTF8(file.getAbsolutePath())));
                log.logMessage(Messages.getString("kit.gui.editor.47") + " " + file.getAbsolutePath());
                mainWindow.setTitle("[" + file.getName() + "]");
                currentFile = file;

            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(mainWindow, Messages.getString("kit.gui.editor.48") + ioe.getMessage(), Messages.getString("kit.gui.editor.49"), JOptionPane.ERROR_MESSAGE);
                log.logMessage(Messages.getString("kit.gui.editor.50") + " " + file.getAbsolutePath() + " " + Messages.getString("kit.gui.editor.51") + ioe.getMessage());
            }
        }
    }

    /**
     * Opens file in editor.
     */
    private void openFile() {

        if (hasUserPermission()) {

            JFileChooser manejarFichero = new JFileChooser();
            int returnVal = manejarFichero.showOpenDialog(mainWindow);
            if (returnVal == JFileChooser.APPROVE_OPTION) {

                openFile(manejarFichero.getSelectedFile());
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

            log.logMessage(Messages.getString("kit.gui.editor.52"));
            JOptionPane.showMessageDialog(mainWindow, Messages.getString("kit.gui.editor.53"), Messages.getString("kit.gui.editor.54"), JOptionPane.INFORMATION_MESSAGE);

        } else {

            String title = mainWindow.getTitle();
            title = title.substring(title.indexOf("[") + 1, title.indexOf("]"));

            JFileChooser manejarFichero = new JFileChooser();
            manejarFichero.setSelectedFile(new File(title));
            int returnVal = manejarFichero.showSaveDialog(mainWindow);

            if (returnVal == JFileChooser.APPROVE_OPTION) {

                File file = manejarFichero.getSelectedFile();
                if (file.exists()) {

                    int resp = JOptionPane.showConfirmDialog(mainWindow, Messages.getString("kit.gui.editor.55") + file.getName() + Messages.getString("kit.gui.editor.56"),
                            Messages.getString("kit.gui.editor.57"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                    if (resp != JOptionPane.OK_OPTION) {

                        return;
                    }

                    log.logMessage(Messages.getString("kit.gui.editor.58") + " " + file.getAbsolutePath());
                }

                saveFile(file);
            }
        }
    }

    /**
     * Save a file.
     * @param file File.
     */
    private void saveFile(final File file) {

        try {

            String contenido = documentHandle.getPlainText();
            FileUtil.writeUTF8(file.getAbsolutePath(), contenido);
            mainWindow.setTitle("[" + file.getName() + "]");

            Object[] paramsText = {file.getAbsolutePath()};
            String msg = MessageFormat.format(Messages.getString("kit.gui.editor.59"), paramsText);
            log.logMessage(msg);
            currentFile = file;

        } catch (IOException ioe) {

            JOptionPane.showMessageDialog(mainWindow, Messages.getString("kit.gui.editor.60") + " " + ioe.getMessage(), Messages.getString("kit.gui.editor.61"), JOptionPane.ERROR_MESSAGE);
            log.logMessage(Messages.getString("kit.gui.editor.62") + " " + file.getAbsolutePath() + " " + Messages.getString("kit.gui.editor.51") + ioe.getMessage());
        }
    }

    /**
     * Create a new file.
     */
    private void newFile() {

        if (hasUserPermission()) {

            documentHandle.openIrreversible(new StringBuilder(""));
            mainWindow.setTitle("[" + NEW_FILE + "]");
            log.logMessage(NEW_FILE + ".");
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
            fileName = fileName.substring(fileName.indexOf("[") + 1, fileName.indexOf("]"));

            Object[] paramsText = {fileName};
            String msg = MessageFormat.format(Messages.getString("kit.gui.editor.63"), paramsText);

            int answer = JOptionPane.showConfirmDialog(mainWindow, msg, Messages.getString("kit.gui.editor.64"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {

                permited = false;
            }
        }

        return permited;
    }

    /**
     * Method invoked before exit.
     */
    public void exitProgram() {

        if (hasUserPermission()) {

            int respuesta = JOptionPane.showConfirmDialog(mainWindow, Messages.getString("kit.gui.editor.65"), Messages.getString("kit.gui.editor.66"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (respuesta == JOptionPane.OK_OPTION) {

                System.exit(0);
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

                    JOptionPane.showMessageDialog(mainWindow, Messages.getString("kit.gui.editor.67"), Messages.getString("kit.gui.editor.68"), JOptionPane.ERROR_MESSAGE);
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

        Component[] buttons = toolBar.getComponents();
        for (int count = 0; count < buttons.length; count++) {

            buttons[count].setEnabled(activeValue);
        }

        Component[] subMenu = fileMenu.getMenuComponents();
        for (int cont = 0; cont < subMenu.length; cont++) {

            subMenu[cont].setEnabled(activeValue);
        }
    }
}

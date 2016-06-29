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
package es.ree.eemws.kit.gui.applications.sender;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import es.ree.eemws.client.put.PutMessage;
import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.core.utils.error.EnumErrorCatalog;
import es.ree.eemws.core.utils.file.FileUtil;
import es.ree.eemws.core.utils.iec61968100.EnumMessageFormat;
import es.ree.eemws.core.utils.iec61968100.EnumMessageStatus;
import es.ree.eemws.core.utils.iec61968100.FaultUtil;
import es.ree.eemws.core.utils.operations.put.PutOperationException;
import es.ree.eemws.core.utils.xml.XMLElementUtil;
import es.ree.eemws.core.utils.xml.XMLUtil;
import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.kit.config.Configuration;
import es.ree.eemws.kit.gui.common.LogHandle;
import es.ree.eemws.kit.gui.common.Logger;

/**
 * File Handler utility class for sender application.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 01/03/2016
 */
public final class FileHandler extends DropTargetAdapter {

    /** Key to store the whether use of binary as preference. */
    private static final String USE_BINARY_FILE = "USE_BINARY_FILE"; //$NON-NLS-1$

    /** Value to setup save mode to "auto-save". */
    private static final String USE_AUTO_SAVE = "USE_AUTO_SAVE"; //$NON-NLS-1$

    /** Value to setup save mode to "auto-save as". */
    private static final String USE_SAVE_AS = "USE_SAVE_AS"; //$NON-NLS-1$

    /** Value to setup save mode to "no save". */
    private static final String USE_NO_SAVE = "USE_NO_SAVE"; //$NON-NLS-1$

    /** Key to store, in the preference set, the selected save mode. */
    private static final String SAVE_MODE = "SAVE_MODE"; //$NON-NLS-1$

    /** Prefix added to identify response files. */
    private static final String RESPONSE_ID_PREFIX = "ack_"; //$NON-NLS-1$

    /** File type checkbox. */
    private JCheckBoxMenuItem fileType;

    /** File menu. */
    private JMenu fileMenu = new JMenu();

    /** Reference to the main window. */
    private Sender mainWindow;

    /** Log handler. */
    private LogHandle logHandle;

    /** Logger. */
    private Logger logger;

    /** Menu option for auto save ack. */
    private JRadioButtonMenuItem autoSave;

    /** Menu option for save as ack. */
    private JRadioButtonMenuItem saveAs;

    /** Menu option for no save ack. */
    private JRadioButtonMenuItem noSave;

    /**
     * Creates a new instance of the File handler.
     * @param window Reference to main window.
     * @param logHandl LogHandle reference.
     */
    public FileHandler(final Sender window, final LogHandle logHandl) {

        mainWindow = window;
        logHandle = logHandl;
        logger = logHandle.getLog();

        /* Enables file open by dropping items. */
        new DropTarget(window, this);
    }

    /**
     * Gets file menu.
     * @return File Menu items for the main menu.
     */
    public JMenu getMenu() {

        Preferences preferences = Preferences.userNodeForPackage(getClass());

        JMenuItem openFileMenuItem = new JMenuItem(Messages.getString("SENDER_MENU_ITEM_OPEN"), //$NON-NLS-1$
                new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_OPEN)));
        openFileMenuItem.setMnemonic(Messages.getString("SENDER_MENU_ITEM_OPEN_HK").charAt(0)); //$NON-NLS-1$
        openFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) { // NOSONAR event is not used.
                openFile();
            }
        });

        JMenu saveMenu = new JMenu(Messages.getString("SENDER_MENU_ITEM_SAVE")); //$NON-NLS-1$
        saveMenu.setMnemonic(Messages.getString("SENDER_MENU_ITEM_SAVE_HK").charAt(0)); //$NON-NLS-1$
        saveMenu.setIcon(new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_SAVE_AS)));

        String saveMode = preferences.get(SAVE_MODE, USE_AUTO_SAVE);

        autoSave = new JRadioButtonMenuItem(Messages.getString("SENDER_SAVE_AUTO")); //$NON-NLS-1$
        autoSave.setMnemonic(Messages.getString("SENDER_SAVE_AUTO_HK").charAt(0)); //$NON-NLS-1$
        autoSave.setSelected(saveMode.equals(USE_AUTO_SAVE));
        saveMenu.add(autoSave);
        autoSave.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) { // NOSONAR event is not used.
                changeSaveType();
            }
        });

        saveAs = new JRadioButtonMenuItem(Messages.getString("SENDER_ASK_SAVE")); //$NON-NLS-1$
        saveAs.setMnemonic(Messages.getString("SENDER_ASK_SAVE_HK").charAt(0)); //$NON-NLS-1$
        saveAs.setSelected(saveMode.equals(USE_SAVE_AS));
        saveMenu.add(saveAs);
        saveAs.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) { // NOSONAR event is not used.
                changeSaveType();
            }
        });

        noSave = new JRadioButtonMenuItem(Messages.getString("SENDER_NO_SAVE")); //$NON-NLS-1$
        noSave.setMnemonic(Messages.getString("SENDER_NO_SAVE_HK").charAt(0)); //$NON-NLS-1$
        noSave.setSelected(saveMode.equals(USE_NO_SAVE));
        saveMenu.add(noSave);
        noSave.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) { // NOSONAR event is not used.
                changeSaveType();
            }
        });

        ButtonGroup bg = new ButtonGroup();
        bg.add(autoSave);
        bg.add(saveAs);
        bg.add(noSave);

        fileType = new JCheckBoxMenuItem(Messages.getString("SENDER_SEND_AS_BINARY")); //$NON-NLS-1$
        fileType.setMnemonic(Messages.getString("SENDER_SEND_AS_BINARY_HK").charAt(0)); //$NON-NLS-1$

        boolean useBinary = preferences.getBoolean(USE_BINARY_FILE, false);
        fileType.setSelected(useBinary);
        fileType.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) { // NOSONAR event is not used.
                changeFileType();
            }
        });

        JMenuItem menuItemExit = new JMenuItem();
        menuItemExit.setText(Messages.getString("SENDER_MENU_ITEM_EXIT")); //$NON-NLS-1$
        menuItemExit.setMnemonic(Messages.getString("SENDER_MENU_ITEM_EXIT").charAt(0)); //$NON-NLS-1$
        menuItemExit.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) { // NOSONAR event is not used.
                System.exit(0);
            }
        });

        fileMenu.setText(Messages.getString("SENDER_MENU_ITEM_FILE")); //$NON-NLS-1$
        fileMenu.setMnemonic(Messages.getString("SENDER_MENU_ITEM_FILE").charAt(0)); //$NON-NLS-1$
        fileMenu.add(openFileMenuItem);
        fileMenu.add(saveMenu);
        fileMenu.add(fileType);
        fileMenu.addSeparator();
        fileMenu.add(menuItemExit);

        return fileMenu;
    }

    /**
     * Stores file type as preferences when changed.
     */
    private void changeFileType() {
        Preferences preferences = Preferences.userNodeForPackage(getClass());
        preferences.putBoolean(USE_BINARY_FILE, fileType.isSelected());
    }

    /**
     * Stores save mode as preferences when changed.
     */
    private void changeSaveType() {
        Preferences preferences = Preferences.userNodeForPackage(getClass());
        if (noSave.isSelected()) {
            preferences.put(SAVE_MODE, USE_NO_SAVE);
        } else if (saveAs.isSelected()) {
            preferences.put(SAVE_MODE, USE_SAVE_AS);
        } else {
            preferences.put(SAVE_MODE, USE_AUTO_SAVE);
        }
    }

    /**
     * Opens a file.
     */
    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showOpenDialog(mainWindow);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            openFile(fileChooser.getSelectedFile());
        }
    }

    /**
     * Enables / disables graphic elements.
     * @param enableValue <code>true</code> enable  <code>false</code> disable.
     */
    public void enable(final boolean enableValue) {

        for (Component comp : fileMenu.getMenuComponents()) {
            comp.setEnabled(enableValue);
        }

        logHandle.enable(enableValue);

        if (enableValue) {
            mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else {
            mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        mainWindow.update(mainWindow.getGraphics());
    }

    /**
     * Sends a file to the server.
     * @param file File to be sent.
     */
    private void sendDataAfterDisableComponents(final File file) {

        PutMessage put = new PutMessage();
        
        try {
            
            Configuration config = new Configuration();
            config.readConfiguration();
            put.setEndPoint(config.getUrlEndPoint());

            String response;
            long l = System.currentTimeMillis();
            if (fileType.isSelected()) {
                response = put.put(file.getName(), FileUtil.readBinary(file.getAbsolutePath()), EnumMessageFormat.BINARY);
            } else {
                response = put.put(new StringBuilder(FileUtil.readUTF8(file.getAbsolutePath())));
            }
            l = (System.currentTimeMillis() - l) / 1000;

            if (response == null) {
                logger.logMessage(Messages.getString("SENDER_NO_RESPONSE")); //$NON-NLS-1$
            } else {
                logger.logMessage(XMLUtil.prettyPrint(response).toString());
            }

            EnumMessageStatus status = put.getMessageMetaData().getStatus();

            if (status == null) {
                mainWindow.setSentFailed(file.getName());
                JOptionPane.showMessageDialog(mainWindow, Messages.getString("SENDER_NO_IEC_MESSAGE"), //$NON-NLS-1$
                        Messages.getString("MSG_ERROR_TITLE"), //$NON-NLS-1$
                        JOptionPane.INFORMATION_MESSAGE);

            } else {
                if (status.equals(EnumMessageStatus.OK)) {
                    mainWindow.setSentOk(file.getName());
                    JOptionPane.showMessageDialog(mainWindow, Messages.getString("SENDER_ACK_OK", l), //$NON-NLS-1$
                            Messages.getString("MSG_INFO_TITLE"), //$NON-NLS-1$
                            JOptionPane.INFORMATION_MESSAGE);

                } else {
                    mainWindow.setSentFailed(file.getName());
                    JOptionPane.showMessageDialog(mainWindow, Messages.getString("SENDER_ACK_NOOK"), //$NON-NLS-1$
                            Messages.getString("MSG_WARNING_TITLE"), //$NON-NLS-1$
                            JOptionPane.INFORMATION_MESSAGE);
                }
                
                if (response != null) {
                    save(new StringBuilder(response), file);
                }
            }

        } catch (PutOperationException ex) {

            String msg = Messages.getString("SENDER_UNABLE_TO_SEND"); //$NON-NLS-1$
            JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("MSG_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
            logger.logException(msg, ex);

            mainWindow.setSentFailed(file.getName());
            
            /* Soap Fault, save the fault message. */
            if (ex.getCode().equals(EnumErrorCatalog.ERR_HAND_010.getCode())) {
                
                save(new StringBuilder(put.getMessageMetaData().getRejectText()), file);

            } else {
                
                /* Creates a "fake" fault using the exception. */
                try {
                    String fault = XMLElementUtil.element2String(XMLElementUtil.obj2Element(FaultUtil.getFaultMessageFromException(ex.getMessage(), ex.getCode())));
                    save(new StringBuilder(fault), file);
                    
                } catch (TransformerException |  ParserConfigurationException |  JAXBException e) {
                    logger.logException(Messages.getString("SENDER_CANNOT_CREATE_FAULT_MSG"), e); //$NON-NLS-1$
                }                
            }            
            
        } catch (ConfigException ex) {

            String msg = Messages.getString("INVALID_CONFIGURATION" + ex.getMessage()); //$NON-NLS-1$
            JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("MSG_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$

        } catch (IOException ex) {
            String msg = Messages.getString("SENDER_CANNOT_OPEN_FILE", file.getName()); //$NON-NLS-1$

            logger.logException(msg, ex);
            JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("MSG_ERROR_TITLE"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$

            mainWindow.setSentFailed(file.getName());
            
        } finally {
            enable(true);
        }
    }
    
    /**
     * Saves the response message according to the user preferences.
     * @param response Response content.
     * @param file File Send file information.
     */
    private void save(final StringBuilder response, final File file) {
        File ackFile = new File(file.getParent() + File.separator + RESPONSE_ID_PREFIX + file.getName());        
        
        if (autoSave.isSelected()) {
            saveFile(response, ackFile);
        } else if (saveAs.isSelected()) {
            saveFileAs(response, ackFile);
        }
    }
    
    /**
     * Saves the response using a dialog.
     * @param response Response content.
     * @param file File Send file information.
     */
    private void saveFileAs(final StringBuilder response, final File file) {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(file);
        int returnVal;
        boolean loop = false;
        do {
            loop = false;
            returnVal = fileChooser.showSaveDialog(mainWindow);

            if (returnVal == JFileChooser.APPROVE_OPTION) {

                File choosedF = fileChooser.getSelectedFile();
                if (choosedF.exists()) {

                    int resp = JOptionPane.showConfirmDialog(mainWindow, Messages.getString("SENDER_SAVE_FILE_ALREADY_EXISTS", choosedF.getName()), //$NON-NLS-1$
                            Messages.getString("MSG_WARNING_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$

                    if (resp == JOptionPane.OK_OPTION) {
                        saveFile(response, choosedF);                          
                    } else {
                        
                        /* User does not want to overwrite, ask againg. */
                        loop = true;
                    }
                } else {
                    saveFile(response, choosedF);
                }
            }  
        } while (loop);
         
    }
  
    /**
     * Saves the response in a file.
     * The response is created in the same path as the send file has using "ack" as file name prefix.
     * @param response Response content.
     * @param file File Send file information.
     */
    private void saveFile(final StringBuilder response, final File file) {

        try {
            FileUtil.createBackup(file.getAbsolutePath());
            FileUtil.writeUTF8(file.getAbsolutePath(), response.toString());
            logger.logMessage(Messages.getString("SENDER_SAVE_FILE_SAVED", file.getAbsolutePath())); //$NON-NLS-1$

        } catch (IOException ioe) {
            String errMsg = Messages.getString("SENDER_UNABLE_TO_SAVE", file.getAbsolutePath()); //$NON-NLS-1$
            JOptionPane.showMessageDialog(mainWindow, errMsg, Messages.getString("MSG_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
            logger.logException(errMsg, ioe);
            saveFileAs(response, file);
        }
    }

    /**
     * Opens a file and sends it.
     * @param file File to be opened.
     */
    private void openFile(final File file) {

        if (file.exists()) {

            mainWindow.setSending(file.getName());
            enable(false);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    sendDataAfterDisableComponents(file);
                }
            });

        } else {

            // El fichero no existe.
            String msg = Messages.getString("SENDER_CANNOT_OPEN_FILE", file.getAbsoluteFile()); //$NON-NLS-1$
            logger.logMessage(msg);
            JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("MSG_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
        }
    }

    /**
     * Triggered when a link to a file (or any object else) is dropped on application.
     * @param eventoDrop Event triggered containing data related to dropped object.
     */
    @Override
    @SuppressWarnings({ "rawtypes" })
    public void drop(final DropTargetDropEvent eventoDrop) {

        if (eventoDrop.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {

            try {

                eventoDrop.acceptDrop(DnDConstants.ACTION_LINK);
                List obj = (List) eventoDrop.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                File file = (File) obj.get(0);

                if (!file.isDirectory()) {

                    openFile(file);

                } else {
                    JOptionPane.showMessageDialog(mainWindow, Messages.getString("SENDER_CANNOT_LOAD_FOLDER"), //$NON-NLS-1$
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
}

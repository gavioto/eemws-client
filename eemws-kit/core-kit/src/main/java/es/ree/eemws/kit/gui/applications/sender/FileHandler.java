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

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

 







import es.ree.eemws.client.put.PutMessage;
import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.core.utils.file.FileUtil;
import es.ree.eemws.core.utils.iec61968100.EnumMessageFormat;
import es.ree.eemws.core.utils.iec61968100.EnumMessageStatus;
import es.ree.eemws.core.utils.operations.put.PutOperationException;
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
    
    /**
     * Creates a new instance of the File handler.
     * @param window Reference to main window.
     * @param logHandl LogHandle reference.
     */
    public FileHandler(final Sender window, final LogHandle logHandl) {

        mainWindow = window;
        logHandle = logHandl; 
        logger = logHandle.getLog();
                
        /* Enables the editor open files by dropping items. */
        new DropTarget(window, this);
    }

    /**
     * Gets file menu.
     * @return File Menu items for the main menu.
     */
    public JMenu getMenu() {

        JMenuItem openFileMenuItem = new JMenuItem(Messages.getString("SENDER_MENU_ITEM_OPEN"),  //$NON-NLS-1$
                new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_OPEN)));
        openFileMenuItem.setMnemonic(Messages.getString("SENDER_MENU_ITEM_OPEN_HK").charAt(0)); //$NON-NLS-1$
        openFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) { // NOSONAR event is not used.
                openFile();
            }
        });

        fileType = new JCheckBoxMenuItem(Messages.getString("SENDER_SEND_AS_BINARY")); //$NON-NLS-1$
        fileType.setMnemonic(Messages.getString("SENDER_SEND_AS_BINARY_HK").charAt(0)); //$NON-NLS-1$

        Preferences preferences = Preferences.userNodeForPackage(getClass());
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
        fileMenu.add(fileType);
        fileMenu.addSeparator();
        fileMenu.add(menuItemExit);

        return fileMenu;
    }
    
    /**
     * Stores file type preference when changed.
     */
    private void changeFileType() {
        Preferences preferences = Preferences.userNodeForPackage(getClass());
        preferences.putBoolean(USE_BINARY_FILE, fileType.isSelected());
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

        try {
            PutMessage put = new PutMessage();
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

            logger.logMessage(response);
            
            EnumMessageStatus status = put.getMessageMetaData().getStatus();

            if (status == null) {
                mainWindow.setSentFailed(file.getName());
                JOptionPane.showMessageDialog(mainWindow, 
                        Messages.getString("SENDER_NO_IEC_MESSAGE"),  //$NON-NLS-1$
                        Messages.getString("MSG_ERROR_TITLE"),  //$NON-NLS-1$
                        JOptionPane.INFORMATION_MESSAGE);
                
            } else if (status.equals(EnumMessageStatus.OK)) {
                mainWindow.setSentOk(file.getName());
                JOptionPane.showMessageDialog(mainWindow, 
                        Messages.getString("SENDER_ACK_OK", l),  //$NON-NLS-1$
                        Messages.getString("MSG_INFO_TITLE"),  //$NON-NLS-1$
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                mainWindow.setSentFailed(file.getName());
                JOptionPane.showMessageDialog(mainWindow, 
                        Messages.getString("SENDER_ACK_NOOK"),  //$NON-NLS-1$
                        Messages.getString("MSG_WARNING_TITLE"),  //$NON-NLS-1$
                        JOptionPane.INFORMATION_MESSAGE);
            }
                       
        } catch (PutOperationException ex) {
            
            String msg = Messages.getString("SENDER_UNABLE_TO_SEND"); //$NON-NLS-1$
            JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("MSG_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
            logger.logException(msg, ex);

            mainWindow.setSentFailed(file.getName());
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
     * Triggered when a link to a file (or any object else) is dropped on editor.
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

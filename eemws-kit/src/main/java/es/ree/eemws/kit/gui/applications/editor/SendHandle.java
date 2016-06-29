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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
 
import es.ree.eemws.client.put.PutMessage;
import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.core.utils.iec61968100.EnumMessageStatus;
import es.ree.eemws.core.utils.operations.put.PutOperationException;
import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.kit.config.Configuration;
import es.ree.eemws.kit.gui.common.Logger;
import es.ree.eemws.kit.gui.common.ServiceMenuListener;


/**
 * Processes related to message send.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014007
 */
public final class SendHandle implements ServiceMenuListener {

    /** Access to log window. */
    private Logger log;

    /** Button bar. */
    private JToolBar buttonBar = new JToolBar();

    /** Send menu. */
    private JMenu serviceMenu = new JMenu();

    /** Main Window. */
    private Editor mainWindow = null;

    /** Editable text manager. */
    private DocumentHandle documentHandle = null;

    /**
     * Constructor. Creates a new private instance of the
     * Send handler class.
     * @param window Main window.
     * @throws ConfigException If module settings are incorrect.
     */
    public SendHandle(final Editor window) throws ConfigException {

        Configuration cf = new Configuration();
        cf.readConfiguration();

        mainWindow = window;
        log = mainWindow.getLogHandle().getLog();
        documentHandle = mainWindow.getDocumentHandle();
    }
 
    /**
     * Obtains the service menu items.
     * @return Menu entry containing 'Settings' menu items.
     */
    public JMenu getMenu() {

        JMenuItem sendMenuItem = new JMenuItem(Messages.getString("EDITOR_MENU_ITEM_SEND"), new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_SEND))); //$NON-NLS-1$
        sendMenuItem.setMnemonic(Messages.getString("EDITOR_MENU_ITEM_SEND_HK").charAt(0)); //$NON-NLS-1$
        sendMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
        sendMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {  // NOSONAR event is not used.
                send();
            }
        });

        serviceMenu.setText(Messages.getString("EDITOR_MENU_ITEM_SERVICE")); //$NON-NLS-1$
        serviceMenu.setMnemonic(Messages.getString("EDITOR_MENU_ITEM_SERVICE_HK").charAt(0)); //$NON-NLS-1$
        serviceMenu.add(sendMenuItem);
        serviceMenu.addSeparator();

        return serviceMenu;
    }

    /**
     * Returns the sending option bar.
     * @return Sending option bar.
     */
    public JToolBar getButtonBar() {

        buttonBar = new JToolBar();
        buttonBar.setFloatable(true);

        JButton enviarDocBtn = new JButton();
        enviarDocBtn.setToolTipText(Messages.getString("EDITOR_MENU_ITEM_SEND")); //$NON-NLS-1$
        enviarDocBtn.setIcon(new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_SEND)));
        enviarDocBtn.setBorderPainted(false);
        enviarDocBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {  // NOSONAR event is not used.
                send();
            }
        });

        buttonBar.add(enviarDocBtn, null);
        return buttonBar;
    }

    /**
     * Enables / disables graphic values.
     * @param activeValue <code>true</code> enable. <code>false</code> disable.
     */
    public void enable(final boolean activeValue) {

        for (Component comp : buttonBar.getComponents()) {
            comp.setEnabled(activeValue);
        }

        for (Component comp : serviceMenu.getMenuComponents()) {
            comp.setEnabled(activeValue);
        }
    }

    /**
     * Sends XML files.
     */
    private void send() {

        if (documentHandle.isEmpty()) {

            log.logMessage(Messages.getString("EDITOR_SEND_DOCUMENT_IS_EMPTY")); //$NON-NLS-1$
            JOptionPane.showMessageDialog(mainWindow, 
                    Messages.getString("EDITOR_SEND_DOCUMENT_IS_EMPTY"),   //$NON-NLS-1$
                    Messages.getString("MSG_INFO_TITLE"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$

        } else {

            mainWindow.enableScreen(false);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    sendDataAfterDisableComponents();
                }
            });
        }
    }

    /**
     * Sends the current message.
     */
    private void sendDataAfterDisableComponents() {
      
        try {
            
            PutMessage put = new PutMessage();
            Configuration config = new Configuration();
            config.readConfiguration();
            put.setEndPoint(config.getUrlEndPoint());
 
            log.logMessage(Messages.getString("EDITOR_SENDING")); //$NON-NLS-1$
            long l = System.currentTimeMillis();

            String response = put.put(new StringBuilder(documentHandle.getPlainText()));
            
            l = (System.currentTimeMillis() - l) / 1000;

            log.logMessage(Messages.getString("EDITOR_ACK_RECEIVED")); //$NON-NLS-1$
            if (response != null) {
                log.logMessage(response);
            }

            EnumMessageStatus status = put.getMessageMetaData().getStatus();
            
            if (status == null) {
                JOptionPane.showMessageDialog(mainWindow, 
                        Messages.getString("EDITOR_NO_IEC_MESSAGE"),  //$NON-NLS-1$
                        Messages.getString("MSG_ERROR_TITLE"),  //$NON-NLS-1$
                        JOptionPane.INFORMATION_MESSAGE);
                
            } else if (status.equals(EnumMessageStatus.OK)) {
                JOptionPane.showMessageDialog(mainWindow, 
                        Messages.getString("EDITOR_ACK_OK", l),  //$NON-NLS-1$
                        Messages.getString("MSG_INFO_TITLE"),  //$NON-NLS-1$
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(mainWindow, 
                        Messages.getString("EDITOR_ACK_NOOK"),  //$NON-NLS-1$
                        Messages.getString("MSG_WARNING_TITLE"),  //$NON-NLS-1$
                        JOptionPane.INFORMATION_MESSAGE);
            }
         

        } catch (PutOperationException ex) {
            
            String msg = Messages.getString("EDITOR_UNABLE_TO_SEND"); //$NON-NLS-1$
            JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("MSG_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
            log.logException(msg, ex);
                
        } catch (ConfigException ex) {
            
            String msg = Messages.getString("INVALID_CONFIGURATION" + ex.getMessage()); //$NON-NLS-1$
            JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("MSG_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
            log.logException(msg, ex);
            
        } finally {

            mainWindow.enableScreen(true);
        }
    }
 

    /**
     * Sets end point.
     * @param endp End point to messages will be sent.
     */
    @Override
    public void setEndPoint(final String endp) {

        /* This method should not be implemented. */
    }
}

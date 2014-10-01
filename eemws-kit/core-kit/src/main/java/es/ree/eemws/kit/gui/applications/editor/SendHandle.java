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
import java.text.MessageFormat;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.xml.ws.WebServiceException;

import org.w3c.dom.Element;

import es.ree.eemws.client.exception.ClientException;
import es.ree.eemws.client.putmessage.PutMessage;
import es.ree.eemws.core.utils.config.ConfigException;
import es.ree.eemws.core.utils.xml.XMLElementUtil;
import es.ree.eemws.core.utils.xml.XMLUtil;
import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.kit.config.Configuration;
import es.ree.eemws.kit.gui.applications.Logger;
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

    /** Status sent by service when successful. */
    private static final String STATUS_OK = "A01";

    /** Type of the acknowledgement document. */
    private static final String ACK_DOCUMENT_TYPE = "Acknowledgement_MarketDocument";

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
     * Retrieves a message send module, it sends messages to end point
     * using current certificate or the one set to signature.
     * @return Send module.
     */
    private PutMessage getPutMessage() {

        PutMessage put = new PutMessage();
        Configuration config = new Configuration();

        try {

            config.readConfiguration();

            String urlEndPoint = config.getUrlEndPoint().toURI().toString();

            put.setEndPoint(urlEndPoint);

        } catch (Exception e) {

            log.logMessage(e.getMessage());
        }

        return put;
    }

    /**
     * Obtains the service menu items.
     * @return Menu entry containing 'Settings' menu items.
     */
    public JMenu getMenu() {

        JMenuItem sendMenuItem = new JMenuItem(Messages.getString("kit.gui.editor.73"), new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_SEND)));
        sendMenuItem.setMnemonic('S');
        sendMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
        sendMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                send();
            }
        });

        serviceMenu.setText(Messages.getString("kit.gui.editor.74"));
        serviceMenu.setMnemonic('e');
        serviceMenu.add(sendMenuItem);
        serviceMenu.addSeparator();

        ServiceMenu ms = new ServiceMenu(this);
        ms.addServiceElements(serviceMenu);

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
        enviarDocBtn.setToolTipText(Messages.getString("kit.gui.editor.75"));
        enviarDocBtn.setIcon(new ImageIcon(getClass().getResource(es.ree.eemws.kit.gui.common.Constants.ICON_SEND)));
        enviarDocBtn.setBorderPainted(false);
        enviarDocBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
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

        Component[] buttons = buttonBar.getComponents();
        for (int cont = 0; cont < buttons.length; cont++) {

            buttons[cont].setEnabled(activeValue);
        }

        Component[] subMenu = serviceMenu.getMenuComponents();
        for (int cont = 0; cont < subMenu.length; cont++) {

            subMenu[cont].setEnabled(activeValue);
        }
    }

    /**
     * Sends XML files.
     */
    private void send() {

        if (documentHandle.isEmpty()) {

            log.logMessage(Messages.getString("kit.gui.editor.76"));
            JOptionPane.showMessageDialog(mainWindow, Messages.getString("kit.gui.editor.76"),  Messages.getString("kit.gui.editor.54"), JOptionPane.INFORMATION_MESSAGE);

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
     * Starts sending tasks once screen elements are disabled and the wait
     * cursor is shown.
     */
    private void sendDataAfterDisableComponents() {

        PutMessage putMEssage = getPutMessage();

        try {

            log.logMessage(Messages.getString("kit.gui.editor.77"));
            long l = System.currentTimeMillis();

            String response = putMEssage.put(documentHandle.getPlainText());


            l = (System.currentTimeMillis() - l) / 1000;

            log.logMessage(Messages.getString("kit.gui.editor.78"));
            log.logMessage(response);

            if (!"".equals(response) && response != null) {
                if (isSuccess(response)) {

                    Object[] paramsText = {l};
                    String msg = MessageFormat.format(Messages.getString("kit.gui.editor.79"), paramsText);
                    log.logMessage(msg);
                    JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("kit.gui.editor.80"), JOptionPane.INFORMATION_MESSAGE);

                } else {

                    String rejectionReason = "";
                    rejectionReason = getReasonText(response);
                    Object[] paramsText = {rejectionReason, l};
                    String msg = MessageFormat.format(Messages.getString("kit.gui.editor.81"), paramsText);
                    String msg2 = MessageFormat.format(Messages.getString("kit.gui.editor.82"), paramsText);
                    log.logMessage(msg);
                    JOptionPane.showMessageDialog(mainWindow, msg2, Messages.getString("kit.gui.editor.22"), JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                Element xml = XMLElementUtil.string2Element(documentHandle.getPlainText());
                if (ACK_DOCUMENT_TYPE.equals(xml.getLocalName())) {
                     Object[] paramsText = {l};
                     String msg = MessageFormat.format(Messages.getString("kit.gui.editor.79"), paramsText);
                     log.logMessage(msg);
                     JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("kit.gui.editor.80"), JOptionPane.INFORMATION_MESSAGE);
                }

            }


        } catch (ClientException cE) {

            String msg = cE.getMessage();
            JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("kit.gui.configuration.12"), JOptionPane.ERROR_MESSAGE);
            log.logMessage(cE.getMessage());

        } catch (WebServiceException wsE) {

            String msg = Messages.getString("kit.gui.editor.84");
            JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("kit.gui.configuration.12"), JOptionPane.ERROR_MESSAGE);
            log.logMessage(wsE.getMessage());

        } catch (Exception e) {

            String msg = Messages.getString("kit.gui.editor.85");
            JOptionPane.showMessageDialog(mainWindow, msg, Messages.getString("kit.gui.configuration.12"), JOptionPane.ERROR_MESSAGE);
            log.logMessage(e.getMessage());

        } finally {

            mainWindow.enableScreen(true);
        }
    }

    /**
     * Checks if message was sent successfully.
     * @param document Content of the response
     * @return <code>true</code> if the response is marked as successful.
     * <code> false</code> otherwise.
     */
    private boolean isSuccess(final String document) {

        String code = getCode(document);
        return (STATUS_OK.indexOf(code) != -1);
    }

    /** Retrieves the Status code.
     * @param document content of the Document as text.
     * @return The Reason node as text.
     */
    private String getCode(final String document) {

        String status = XMLUtil.getNodeValue("code", document);
        return status;
    }

    /**
     * Retrieves the description of Send status.
     * @param document String containing the response
     * @return description of status
     */
    private String getReasonText(final String document) {

        String text = XMLUtil.getNodeValue("text", document);
        return text;
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

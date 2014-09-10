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

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

/**
 *
 * Manages folder processing status by showing an icon on taskbar notification area.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 29/05/2014
 *
 */
public final class StatusIcon {

    /** Text to be shown on tooltip when files are being processed. */
    private static final String TEXT_BUSY = "Processing files...";

    /** Text to be shown on tooltip when no tasks are being performed. */
    private static final String TEXT_IDLE = "Magic Folder.";

    /** Path to image that notifies "normal" status. */
    private static final String IMAGE_IDLE = "/images/idle.gif";

    /** Path to image that notifies "busy" status. */
    private static final String IMAGE_BUSY = "/images/busy.gif";

    /** Path to image displayed when no tasks are being performed. */
    private static Image imgIdle = null;

    /** Path to image displayed when files are being processed. */
    private static Image imageBusy = null;

    /** Task bar icon on notification area.*/
    private TrayIcon trayIcon = null;

    /**
     * Constructor. Sets a new icon on notification area (if supported by OS) and creates two
     * menu items: one to indicate the connection environment, and the other to exit the application.
     * @param config Connection kit settings.
     */
    public StatusIcon(final Configuration config) {
        if (SystemTray.isSupported()) {

            imgIdle = Toolkit.getDefaultToolkit().getImage(getClass().getResource(IMAGE_IDLE));
            imageBusy = Toolkit.getDefaultToolkit().getImage(getClass().getResource(IMAGE_BUSY));

            MenuItem exitItem = new MenuItem("Exit");
            exitItem.setShortcut(new MenuShortcut('S'));
            exitItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent arg0) {
                    exit();
                }
            });
            PopupMenu iconMenu = new PopupMenu();
            iconMenu.add(exitItem);

            trayIcon = new TrayIcon(imgIdle, TEXT_IDLE, iconMenu);

            trayIcon.setImageAutoSize(true);
            try {
                SystemTray tray = SystemTray.getSystemTray();
                tray.add(trayIcon);
            } catch (AWTException e) {
                trayIcon = null;
            }
        }
    }

    /**
     * Activate the the icon notifying the "Busy" status on notification area.
     */
    public void setBusy() {
        if (trayIcon != null) {
            trayIcon.setToolTip(TEXT_BUSY);
            trayIcon.setImage(imageBusy);
        }
    }

    /**
     * Activate the the icon notifying the "Idle" status on notification area.
     */
    public void setIdle() {
        if (trayIcon != null) {
            trayIcon.setToolTip(TEXT_IDLE);
            trayIcon.setImage(imgIdle);
        }
    }

    /**
     * Ask for confirmation to exit the application.
     */
    private void exit() {
        int answer = JOptionPane.showConfirmDialog(null, "Exit application? Please confirm.", "Confirm:",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (answer == JOptionPane.OK_OPTION) {
            System.exit(0);
        }
    }
}

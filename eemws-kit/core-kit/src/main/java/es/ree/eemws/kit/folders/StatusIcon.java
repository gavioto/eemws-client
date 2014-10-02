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

import es.ree.eemws.kit.common.Messages;

/**
 * Status Icon, changes the imagen when the application is processing messages. 
 * Handles a basic menu to exit the application. Not available in all the platforms.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 29/05/2014
 *
 */
public final class StatusIcon {

	/** System property key. If set, the application is in "interactive" mode and will display a Windows icon. */
	private static final String WINDOWS_SYSTEM_INTERACTIVE = "interactive"; //$NON-NLS-1$
	
    /** Path to image that notifies "normal" status. */
    private static final String IMAGE_IDLE = "/images/idle.gif"; //$NON-NLS-1$

    /** Path to image that notifies "busy" status. */
    private static final String IMAGE_BUSY = "/images/busy.gif"; //$NON-NLS-1$

    /** Path to image displayed when no tasks are being performed. */
    private static Image imgIdle = null;

    /** Path to image displayed when files are being processed. */
    private static Image imageBusy = null;

    /** Task bar icon on notification area.*/
    private static TrayIcon trayIcon = null;

	/** 
	 * Number of invocations to the <code>setBusy</code> method. 
	 * Avoids to show the icon as "idle" if two threads invokes the "busy" method and then one of them calls the "idle" method
	 * and there is still one thread busy. Idle = 0
	 */
    private static Integer numBusy = 0;

    /**
     * Initializes the class.
     */
    static {
    	
        if (isInteractive() && SystemTray.isSupported()) {

            imgIdle = Toolkit.getDefaultToolkit().getImage(Thread.currentThread().getClass().getResource(IMAGE_IDLE));
            imageBusy = Toolkit.getDefaultToolkit().getImage(Thread.currentThread().getClass().getResource(IMAGE_BUSY));

            MenuItem exitItem = new MenuItem(Messages.getString("MF_MENU_ITEM_EXIT")); //$NON-NLS-1$
            exitItem.setShortcut(new MenuShortcut(Messages.getString("MF_MENU_ITEM_EXIT_HOT_KEY").charAt(0))); //$NON-NLS-1$
            exitItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent arg0) {
                    exit();
                }
            });
            PopupMenu iconMenu = new PopupMenu();
            iconMenu.add(exitItem);

            trayIcon = new TrayIcon(imgIdle, Messages.getString("MF_STATUS_IDLE"), iconMenu); //$NON-NLS-1$

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
     * Removes the icon from the system tray.
     */
	public static void removeIcon() {
		if (trayIcon != null) {
			SystemTray.getSystemTray().remove(trayIcon);
		}
	}    	
    

    /**
     * Checks if the system key that set up the instance as interactive (not service) is set.
     * @return  <code>true</code> if the application is running in interactive mode. 
     * <code>false</code> if running in daemon mode (without gui)
     */
    public static boolean isInteractive() {
    	return System.getProperty(WINDOWS_SYSTEM_INTERACTIVE) != null; 
    }
    
    /**
     * Activate the the icon notifying the "Busy" status on notification area.
     */
    public static void setBusy() {
        if (trayIcon != null) {
        	synchronized (numBusy) {
                numBusy ++;
                trayIcon.setToolTip(Messages.getString("MF_STATUS_BUSY")); //$NON-NLS-1$
                trayIcon.setImage(imageBusy);
        	}
        }
    }

    /**
     * Activate the the icon notifying the "Idle" status on notification area.
     */
    public static void setIdle() {
        if (trayIcon != null) {
        	synchronized (numBusy) {
                numBusy --;
                if (numBusy <0) {
                	numBusy = 0;
                }
                if (numBusy == 0) {
                	trayIcon.setToolTip(Messages.getString("MF_STATUS_IDLE")); //$NON-NLS-1$
                	trayIcon.setImage(imgIdle);
                }
        	}
        }
    }

    /**
     * Ask for confirmation to exit the application.
     */
    private static void exit() {
        int answer = JOptionPane.showConfirmDialog(null, Messages.getString("MF_EXIT_APPLICATION"), Messages.getString("MF_EXIT_APPLICATION_TITLE"), //$NON-NLS-1$ //$NON-NLS-2$
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (answer == JOptionPane.OK_OPTION) {
            System.exit(0);
        }
    }
}

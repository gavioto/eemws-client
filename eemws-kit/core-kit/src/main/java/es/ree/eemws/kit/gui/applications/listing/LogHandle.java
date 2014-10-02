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

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import es.ree.eemws.kit.gui.applications.Logger;
import es.ree.eemws.kit.gui.applications.LoggerListener;


/**
 * Log Window management.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class LogHandle implements LoggerListener {

    /** Log menu item, is modified depending on status Log status. */
    private JMenuItem miShowLog = new JMenuItem();

    /** Log window. */
    private Logger log;

    /** Log menu. */
    private JMenu mnLogMenu = new JMenu();

    /**
     * Create new instance of Log window.
     */
    public LogHandle() {
        log = new Logger(this);
        log.setVisible(false);
    }

    /**
     * Retrieve "Actions" menu.
     * @return "Actions" Menu for main Options bar.
     */
    public JMenu getMenu() {

        JMenuItem miDeleteLog = new JMenuItem();

        miDeleteLog.setText("Delete Log");
        miDeleteLog.setMnemonic('D');
        miDeleteLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                deleteLog();
            }
        });
        miShowLog.setText("Show Log");
        miShowLog.setMnemonic('S');
        miShowLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                toggleLogMenuItemText();
            }
        });

        mnLogMenu.setText("Log");
        mnLogMenu.setMnemonic('L');
        mnLogMenu.add(miShowLog);
        mnLogMenu.add(miDeleteLog);

        return mnLogMenu;
    }

    /**
     * Display / hide Log window depending on whether log
     * window is active or not.
     * Change Text on Menu item: Show Log / Hide Log.
     */
    private void toggleLogMenuItemText() {
        if (log.isVisible()) {
            log.setVisible(false);
            miShowLog.setText("Show Log");
        } else {
            log.setVisible(true);
            miShowLog.setText("Hide Log");
        }
    }

    /**
     * Delete log messages.
     */
    private void deleteLog() {
        log.deleteLog();
    }

    /**
     * Retrieve reference to log Window.
     * @return Reference to log Window.
     */
    public Logger getLog() {
        return log;
    }

    /**
     * Invoked by window when closing, swaps items
     * Hide log / Show log.
     */
    public void logWindowIsClosing() {
        miShowLog.setText("Show Log");
    }

    /**
     * Enable / disable graphic values.
     * @param activeValue <code>true</code> Enable.
     * <code>false</code> disable.
     */
    public void enable(final boolean activeValue) {
        Component[] subMenu = mnLogMenu.getMenuComponents();
        for (int cont = 0; cont < subMenu.length; cont++) {
            subMenu[cont].setEnabled(activeValue);
        }
    }
}

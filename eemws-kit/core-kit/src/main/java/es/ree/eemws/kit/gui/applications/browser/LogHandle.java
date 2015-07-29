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
package es.ree.eemws.kit.gui.applications.browser;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import es.ree.eemws.kit.common.Messages;
import es.ree.eemws.kit.gui.common.Logger;
import es.ree.eemws.kit.gui.common.LoggerListener;

/**
 * Log Window management.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class LogHandle implements LoggerListener {

    /** Log menu item, is modified depending on status Log status. */
    private JCheckBoxMenuItem miShowLog = new JCheckBoxMenuItem();

    /** Log window. */
    private Logger log;

    /** Log menu. */
    private JMenu mnLogMenu = new JMenu();

    /**
     * Creates new instance of Log window.
     */
    public LogHandle() {
        log = new Logger(this);
        log.setVisible(false);
    }

    /**
     * Retrieves "Log" menu.
     * @return "Log" menu.
     */
    public JMenu getMenu() {

        JMenuItem miDeleteLog = new JMenuItem();

        miDeleteLog.setText(Messages.getString("BROWSER_LOG_CLEAR_MENU_ITEM")); //$NON-NLS-1$
        miDeleteLog.setMnemonic(Messages.getString("BROWSER_LOG_CLEAR_MENU_ITEM_HK").charAt(0)); //$NON-NLS-1$
        miDeleteLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {   // NOSONAR event is not used.
                log.deleteLog();
            }
        });
        miShowLog.setText(Messages.getString("BROWSER_LOG_SHOW_MENU_ITEM")); //$NON-NLS-1$
        miShowLog.setMnemonic(Messages.getString("BROWSER_LOG_SHOW_MENU_ITEM_HK").charAt(0)); //$NON-NLS-1$
        miShowLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {  // NOSONAR event is not used.
            	log.setVisible(miShowLog.isSelected());
            }
        });

        mnLogMenu.setText(Messages.getString("BROWSER_LOG_MENU_ITEM")); //$NON-NLS-1$
        mnLogMenu.setMnemonic(Messages.getString("BROWSER_LOG_MENU_ITEM_HK").charAt(0)); //$NON-NLS-1$
        mnLogMenu.add(miShowLog);
        mnLogMenu.add(miDeleteLog);

        return mnLogMenu;
    }

   
    /**
     * Retrieves reference to log Window.
     * @return Reference to log Window.
     */
    public Logger getLog() {
        return log;
    }

    /**
     * Invoked by window when closing.
     */
    @Override
    public void logWindowIsClosing() {
        miShowLog.setSelected(false);
    }

    /**
     * Enables / disables graphic values.
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

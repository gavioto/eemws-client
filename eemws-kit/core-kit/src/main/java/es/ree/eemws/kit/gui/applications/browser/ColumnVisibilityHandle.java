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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import es.ree.eemws.kit.common.Messages;


/**
 * Handles column visibility status.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 02/06/2014
 */
public final class ColumnVisibilityHandle {

    /** Column visibility in "simple" display. */
    private static final boolean[] SIMPLE_VIEW = {true, true, true, true, false, false, false, false, false};

    /** Menu element array containing column visibility status.*/
    private JCheckBoxMenuItem[] arColumnsMenuItem;

    /** Reference to table model.*/
    private ListTableModel tableModel = null;

    /** Preferences object to save visibility status. */
    private Preferences preferences;

    /**
     * Constructor. Retrieve visualization preferences.
     * @param pTableModel Table model on which show / hide columns.
     */
    public ColumnVisibilityHandle(final ListTableModel pTableModel) {
        tableModel = pTableModel;
        preferences = Preferences.userNodeForPackage(getClass());
    }

    /**
     * Add visualization options to the menu passed as parameter.
     * @param menuVer Visualization option main menu.
     */
    public void getMenu(final JMenu menuVer) {

        JMenu mnColumnMenu = new JMenu(Messages.getString("BROWSER_COLUMN_MENU_ENTRY")); //$NON-NLS-1$
        mnColumnMenu.setMnemonic(Messages.getString("BROWSER_COLUMN_MENU_ENTRY_HK").charAt(0)); //$NON-NLS-1$
        menuVer.add(mnColumnMenu);


        JMenuItem simpleView = new JMenuItem(Messages.getString("BROWSER_SIMPLE_VIEW")); //$NON-NLS-1$
        simpleView.setMnemonic(Messages.getString("BROWSER_SIMPLE_VIEW_HK").charAt(0)); //$NON-NLS-1$
        simpleView.setSelected(true);
        simpleView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                simpleView();
            }
        });


        JMenuItem fullView = new JMenuItem(Messages.getString("BROWSER_FULL_VIEW")); //$NON-NLS-1$
        fullView.setMnemonic(Messages.getString("BROWSER_FULL_VIEW_HK").charAt(0)); //$NON-NLS-1$
        fullView.setSelected(false);
        fullView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                fullView();
            }
        });

        mnColumnMenu.add(simpleView);
        mnColumnMenu.add(fullView);
        mnColumnMenu.addSeparator();

        int len = ColumnsId.values().length;
        arColumnsMenuItem = new JCheckBoxMenuItem[len];
        
        /* 
         * Creates menu entry for each column name. Sets the hot key for each menu entry
         * avoiding the use of any previous used hot key.
         */
        ArrayList<Character> alMnemonics  = new ArrayList<Character>();
        alMnemonics.add(Messages.getString("BROWSER_SIMPLE_VIEW_HK").charAt(0)); //$NON-NLS-1$
        alMnemonics.add(Messages.getString("BROWSER_FULL_VIEW_HK").charAt(0)); //$NON-NLS-1$
        for (int cont = 0; cont < len; cont++) {
            arColumnsMenuItem[cont] = new JCheckBoxMenuItem();
            String text = ColumnsId.values()[cont].getText();
            arColumnsMenuItem[cont].setText(text);
            text = text.replaceAll(" ", "").replaceAll("/.", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            int nameLength = text.length();
            boolean visible = preferences.getBoolean(ColumnsId.values()[cont].name(), SIMPLE_VIEW[cont]);
            arColumnsMenuItem[cont].setSelected(visible);
            boolean found = false;
            for (int cha = 0; !found && cha < nameLength; cha++) {
                char caracter = text.charAt(cha);
                if (!alMnemonics.contains(caracter)) {
                    found = true;
                    alMnemonics.add(caracter);
                    arColumnsMenuItem[cont].setMnemonic(caracter);
                }
            }
            arColumnsMenuItem[cont].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    updateColumnVisibility();
                }
            });
            mnColumnMenu.add(arColumnsMenuItem[cont]);
        }

        updateColumnVisibility();
    }


    /**
     * Modify visualization status using the "simple view".
     */
    private void simpleView() {

        for (int cont = 0; cont < SIMPLE_VIEW.length; cont++) {
            arColumnsMenuItem[cont].setSelected(SIMPLE_VIEW[cont]);
        }

        updateColumnVisibility();
    }

    /**
     * Modify visualization status showing all available columns.
     */
    private void fullView() {
        int len = arColumnsMenuItem.length;
        for (int cont = 0; cont < len; cont++) {
            arColumnsMenuItem[cont].setSelected(true);
        }

        updateColumnVisibility();
    }

    /**
     * Update column visibility status according to options marked on menu.
     */
    private void updateColumnVisibility() {
        int len = arColumnsMenuItem.length;
        boolean[] arVisibility = new boolean[len];
        for (int cont = 0; cont < len; cont++) {
            boolean visible = arColumnsMenuItem[cont].isSelected();
            arVisibility[cont] = visible;
            preferences.putBoolean(ColumnsId.values()[cont].name(), visible);
        }

        tableModel.setVisible(arVisibility);
    }
}

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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


/**
 * Handles column visibility status.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 02/06/2014
 */
public final class ColumnVisibilityHandle {

    /** Column visibility in classic display. */
    private static final boolean[] CLASSIC_VIEW = {true, true, true, true, false, false, false, false, false};

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

        JMenu mnColumnMenu = new JMenu("Columns");
        mnColumnMenu.setMnemonic('m');
        menuVer.add(mnColumnMenu);


        JMenuItem miClassicView = new JMenuItem("Classic");
        miClassicView.setMnemonic('C');
        miClassicView.setSelected(true);
        miClassicView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                classicView();
            }
        });


        JMenuItem miCompleteView = new JMenuItem("Complete");
        miCompleteView.setMnemonic('o');
        miCompleteView.setSelected(false);
        miCompleteView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                completeView();
            }
        });

        mnColumnMenu.add(miClassicView);
        mnColumnMenu.add(miCompleteView);
        mnColumnMenu.addSeparator();


        int len = ColumnsId.values().length;
        arColumnsMenuItem = new JCheckBoxMenuItem[len];
        ArrayList<Character> alMnemonics  = new ArrayList<Character>();
        alMnemonics.add('C');
        alMnemonics.add('o');
        for (int cont = 0; cont < len; cont++) {
            arColumnsMenuItem[cont] = new JCheckBoxMenuItem();
            String name = ColumnsId.values()[cont].getName();
            arColumnsMenuItem[cont].setText(name);
            name = name.replaceAll(" ", "").replaceAll("/.", "");
            int nameLength = name.length();
            boolean visible = preferences.getBoolean(ColumnsId.values()[cont].name(), CLASSIC_VIEW[cont]);
            arColumnsMenuItem[cont].setSelected(visible);
            boolean found = false;
            for (int cha = 0; !found && cha < nameLength; cha++) {
                char caracter = name.charAt(cha);
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
     * Modify visualization status using the "classic view".
     * (columns which were shown on previous program versions).
     */
    private void classicView() {

        for (int cont = 0; cont < CLASSIC_VIEW.length; cont++) {
            arColumnsMenuItem[cont].setSelected(CLASSIC_VIEW[cont]);
        }

        updateColumnVisibility();
    }

    /**
     * Modify visualization status showing all available columnS.
     */
    private void completeView() {
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

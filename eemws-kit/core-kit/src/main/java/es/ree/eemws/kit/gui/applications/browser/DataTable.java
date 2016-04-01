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
import java.awt.event.MouseEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import es.ree.eemws.kit.common.Messages;


/**
 * Table containing data to show (view model).
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 02/06/2014
 */
public final class DataTable {

    /** Constant referring to 'Select All'. */
    public static final int SELECTION_ALL = 1;

    /** Constant referring to 'Clear selection'.  */
    public static final int SELECTION_NONE = 2;

    /** Constant referring to 'Invert Selection'. */
    public static final int SELECTION_INVERT = 3;

    /** Scrollable container for table. */
    private JScrollPane pnlScrollableTableArea;

    /** List table. */
    private JTable tblListTable;

    /** Table model. */
    private ListTableModel tableModel;

    /** Reference to main window. */
    private Browser mainWindow;

    /** Status bar. */
    private StatusBar statusBar;

    /** Object for message request. */
    private GetMessageSender requestSend;

    /**
     * Constructor. Initialize graphical elements on screen
     * @param window Reference to main window.
     */
    public DataTable(final Browser window) {

        mainWindow = window;
        statusBar = mainWindow.getStatusBar();
        requestSend = mainWindow.getRequestSend();

        tableModel = new ListTableModel();
        tblListTable = new JTable(tableModel);
        tblListTable.setAutoCreateRowSorter(true);
        tblListTable.setDefaultRenderer(Object.class, new TableStrippedCellRender());
        tblListTable.setDefaultRenderer(java.math.BigInteger.class, new TableStrippedCellRender());
        tblListTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tblListTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(final MouseEvent e) {
                /* Double click. */
                if (e.getClickCount() == 2) {
                    requestSend.retrieve();
                }
            }
        });
        tblListTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(final ListSelectionEvent e) { // NOSONAR event is not used.
                changeSelectionStatus();
            }

        });

        pnlScrollableTableArea = new JScrollPane();
        pnlScrollableTableArea.getViewport().add(tblListTable);

    }

    /**
     * Enables / disables data table.
     *
     * @param value <code>true</code> Enable table. <code>false</code> disable.
     */
    public void setEnabled(final boolean value) {
        tblListTable.setEnabled(value);
        pnlScrollableTableArea.setEnabled(value);
    }

    /**
     * Returns model associated to table.
     * @return Model associated to table.
     */
    public ListTableModel getModel() {
        return tableModel;
    }

    /**
     * Returns date selection menu.
     * @return Actions menu.
     */
    public JMenu getSelectionMenu() {

        /* Select All. */
        JMenuItem miSelectAll = new JMenuItem();
        miSelectAll.setText(Messages.getString("BROWSER_SELECT_ALL_MENU_ENTRY")); //$NON-NLS-1$
        miSelectAll.setMnemonic(Messages.getString("BROWSER_SELECT_ALL_MENU_ENTRY_HK").charAt(0)); //$NON-NLS-1$
        miSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) { // NOSONAR event is not used.
                tableSelection(DataTable.SELECTION_ALL);
            }
        });

        /* Remove selecion. */
        JMenuItem miSelectNone = new JMenuItem();
        miSelectNone.setText(Messages.getString("BROWSER_SELECT_NONE_MENU_ENTRY")); //$NON-NLS-1$
        miSelectNone.setMnemonic(Messages.getString("BROWSER_SELECT_NONE_MENU_ENTRY_HK").charAt(0)); //$NON-NLS-1$
        miSelectNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) { // NOSONAR event is not used.
                tableSelection(DataTable.SELECTION_NONE);
            }
        });

        /* Invert selection. */
        JMenuItem miSelectInvert = new JMenuItem();
        miSelectInvert.setText(Messages.getString("BROWSER_SELECT_INVERT_MENU_ENTRY")); //$NON-NLS-1$
        miSelectInvert.setMnemonic(Messages.getString("BROWSER_SELECT_INVERT_MENU_ENTRY_HK").charAt(0)); //$NON-NLS-1$
        miSelectInvert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) { // NOSONAR event is not used.
                tableSelection(DataTable.SELECTION_INVERT);
            }
        });

        /* Select option. */
        JMenu mnSelectionMenu = new JMenu();
        mnSelectionMenu.setText(Messages.getString("BROWSER_SELECT_MENU_ENTRY")); //$NON-NLS-1$
        mnSelectionMenu.setMnemonic(Messages.getString("BROWSER_SELECT_MENU_ENTRY_HK").charAt(0)); //$NON-NLS-1$
        mnSelectionMenu.add(miSelectAll);
        mnSelectionMenu.add(miSelectNone);
        mnSelectionMenu.add(miSelectInvert);

        return mnSelectionMenu;
    }

    /**
     * Changes status bar text according to table's status (num rows, num of selected rows).
     */
    private void changeSelectionStatus() {
        int totalRowNum = tableModel.getRowCount();
        int numSelectedRows = tblListTable.getSelectedRowCount();
               
        if (totalRowNum == 1) {
        	if (numSelectedRows == 0) {
        		statusBar.setStatus(Messages.getString("BROWSER_STATUS_MESSAGE")); //$NON-NLS-1$
         	} else {
         		statusBar.setStatus(Messages.getString("BROWSER_STATUS_MESSAGE_SELECTED")); //$NON-NLS-1$
         	}
        } else {
        	if (numSelectedRows == 0) {
        		statusBar.setStatus(Messages.getString("BROWSER_STATUS_MESSAGES", totalRowNum)); //$NON-NLS-1$
        	} else {
        		statusBar.setStatus(Messages.getString("BROWSER_STATUS_MESSAGES_SELECTED", totalRowNum, numSelectedRows)); //$NON-NLS-1$
        	}
        }
    }


    /**
     * Selects table elements according to the entered mode
     * <li>mode=1: Select all elements in table.
     * <li>mode=2: Clear current selection.
     * <li>mode=3: Invert selection.
     * @param mode Selection mode.
     */
    private void tableSelection(final int mode) {
        if (mode == SELECTION_ALL) {
            tblListTable.selectAll();
        }

        if (mode == SELECTION_NONE) {
            tblListTable.clearSelection();
        }

        if (mode == SELECTION_INVERT) {
            int len = tblListTable.getRowCount();

            for (int cont = 0; cont < len; cont++) {
                if (tblListTable.isRowSelected(cont)) {
                    tblListTable.removeRowSelectionInterval(cont, cont);
                } else {
                    tblListTable.addRowSelectionInterval(cont, cont);
                }
            }
        }
    }

    /**
     * Returns number of selected rows in table.
     *
     * Is necessary to perform a conversion from view to model to prevent problems related to order.
     * @return Number of files selected on table.
     */
    public int[] getSelectedRows() {
        int[] selectedRows = tblListTable.getSelectedRows();
        int len = selectedRows.length;
        int[] orderedSelectedRows = new int[len];
        for (int cont = 0; cont < len; cont++) {
            orderedSelectedRows[cont] = tblListTable.convertRowIndexToModel(selectedRows[cont]);
        }

        return orderedSelectedRows;
    }

    /**
     * Gets index of the selected file.
     * Is necessary to perform a conversion from view to model to prevent problems related to order.
     * @return index of the selected file on table.
     */
    public int getSelectedRow() {
        return tblListTable.convertRowIndexToModel(tblListTable.getSelectedRow());
    }

    /**
     * Sets data to be shown on table.
     * @param data Data to be shown on table.
     */
    public void setData(final Object[][] data) {
        tableModel.setValues(data);
    }

    /**
     * Indicates whether the table is empty.
     * @return <code>true</code> If table is empty. <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return tableModel.getRowCount() != 0;
    }

    /**
     * Return scrollable panel containing message table.
     * @return Scrollable panel containing message table.
     */
    public Component getPanelScroll() {
        return pnlScrollableTableArea;
    }

    /**
     * Adjusts the table size according to the values passed as parameters.
     * @param width Width of the main window.
     * @param height Height  of the main window.
     * @param isFilterVisible Indicates whether the data filter is visible.
     */
    public void adjustTableSize(final int width, final int height, final boolean isFilterVisible) {
        if (isFilterVisible) {
            pnlScrollableTableArea.setBounds(2, 166, width - 30, height - 250);
        } else {
            pnlScrollableTableArea.setBounds(2, 6, width - 30, height - 90);
        }
        tblListTable.repaint();
    }

}

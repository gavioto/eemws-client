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
import java.awt.event.MouseEvent;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


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

    /** Mapping key to 'display striped rows' preference setting. */
    private static final String ZEBRA_STRIP_KEY = "ZEBRA_STRIP_KEY";

    /** Default value for 'display striped rows' preference [true]. */
    private static final boolean ZEBRA_STRIP_VALUE = true;

    /** Scrollable container for table. */
    private JScrollPane pnlScrollableTableArea;

    /** List table itself. */
    private JTable tblListTable;

    /** Table model. */
    private ListTableModel tableModel;

    /** Reference to main window. */
    private Lists mainWindow;

    /** Status bar. */
    private StatusBar statusBar;

    /** Object for message request. */
    private RequestSend requestSend;

    /** Object for saving status references. */
    private Preferences preferences;

    /**
     * Constructor. Initialize graphical elements on screen
     * @param window Reference to main window.
     */
    public DataTable(final Lists window) {

        mainWindow = window;
        preferences = Preferences.userNodeForPackage(getClass());
        statusBar = mainWindow.getStatusBar();
        requestSend = mainWindow.getRequestSend();

        tableModel = new ListTableModel();
        tblListTable = new JTable(tableModel);
        tblListTable.setAutoCreateRowSorter(true);

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
            public void valueChanged(final ListSelectionEvent e) {
                changeSelectionStatus();
            }

        });

        boolean verPijama = preferences.getBoolean(ZEBRA_STRIP_KEY, ZEBRA_STRIP_VALUE);
        setZebraStrip(verPijama);

        pnlScrollableTableArea = new JScrollPane();
        pnlScrollableTableArea.getViewport().add(tblListTable);

    }

    /**
     * Enable / disable data table.
     *
     * @param value <code>true</code> Enable table. <code>false</code> disable.
     */
    public void setEnabled(final boolean value) {
        tblListTable.setEnabled(value);
        pnlScrollableTableArea.setEnabled(value);
    }

    /**
     * Return model associated to table.
     * @return Model associated to table.
     */
    public ListTableModel getModel() {
        return tableModel;
    }

    /**
     * Add visualization options to the menu passed as parameter.
     * @param menuVer Visualization options menu.
     */
    public void getVisualizationMenu(final JMenu menuVer) {
        JMenu menuZebraStrip = new JMenu("Zebra-strip table");

        menuVer.add(menuZebraStrip);

        JRadioButtonMenuItem miZebraStrip = new JRadioButtonMenuItem("Zebra-strip rows");
        miZebraStrip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                setZebraStrip(true);
            }
        });
        miZebraStrip.setSelected(true);

        JRadioButtonMenuItem miSameColor = new JRadioButtonMenuItem("Same color rows");
        miSameColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                setZebraStrip(false);
            }
        });

        ButtonGroup bg = new ButtonGroup();
        bg.add(miZebraStrip);
        bg.add(miSameColor);


        boolean zebraStripViewStatus = preferences.getBoolean(ZEBRA_STRIP_KEY, ZEBRA_STRIP_VALUE);

        miZebraStrip.setSelected(zebraStripViewStatus);
        miSameColor.setSelected(!zebraStripViewStatus);

        menuZebraStrip.add(miZebraStrip);
        menuZebraStrip.add(miSameColor);
    }


    /**
     * Return date selection menu.
     * @return Actions menu.
     */
    public JMenu getSelectionMenu() {

        /* Select All. */
        JMenuItem miSelectAll = new JMenuItem();
        miSelectAll.setText("Select All");
        miSelectAll.setMnemonic('A');
        miSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                tableSelection(DataTable.SELECTION_ALL);
            }
        });

        /* Remove selecion. */
        JMenuItem miSelectNone = new JMenuItem();
        miSelectNone.setText("Clear selection");
        miSelectNone.setMnemonic('e');
        miSelectNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                tableSelection(DataTable.SELECTION_NONE);
            }
        });

        /* Invert selection. */
        JMenuItem miSelectInvert = new JMenuItem();
        miSelectInvert.setText("Invert selection");
        miSelectInvert.setMnemonic('I');
        miSelectInvert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                tableSelection(DataTable.SELECTION_INVERT);
            }
        });

        /* Select option. */
        JMenu mnSelectionMenu = new JMenu();
        mnSelectionMenu.setText("Select");
        mnSelectionMenu.setMnemonic('S');
        mnSelectionMenu.add(miSelectAll);
        mnSelectionMenu.add(miSelectNone);
        mnSelectionMenu.add(miSelectInvert);

        return mnSelectionMenu;
    }

    /**
     * Show / hide 'striped rows' visualization (zebra striped).
     * @param ver <code>true</code> Enable 'striped rows'
     * visualization. <code>false</code> otherwise.
     */
    private void setZebraStrip(final boolean ver) {
        if (ver) {
            tblListTable.setDefaultRenderer(Object.class, new TableStrippedCellRender());
            tblListTable.setDefaultRenderer(java.math.BigInteger.class, new TableStrippedCellRender());

        } else {
            tblListTable.setDefaultRenderer(Object.class, new TableCellRender());
            tblListTable.setDefaultRenderer(java.math.BigInteger.class, new TableCellRender());
        }

        tblListTable.repaint();
        preferences.putBoolean(ZEBRA_STRIP_KEY, ver);
    }

    /**
     * Change status bar text according to
     * the selection performed on table.
     */
    private void changeSelectionStatus() {
        int totalRowNum = tableModel.getRowCount();

        String sTotal = "";
        if (totalRowNum > 1) {
            sTotal = "s";
        }

        StringBuilder msg = new StringBuilder();
        msg.append(totalRowNum);
        msg.append(" Message");
        msg.append(sTotal);
        msg.append(" listed");

        int numSelectedRows = tblListTable.getSelectedRowCount();
        if (numSelectedRows > 0) {
            String stPluralSuffix = "";
            if (numSelectedRows > 1) {
                stPluralSuffix = "s";
            }
            msg.append(" (");
            msg.append(numSelectedRows);
            msg.append(" message");
            msg.append(stPluralSuffix);
            msg.append(" selected");
            msg.append(").");
        }

        statusBar.setStatus(msg.toString());
    }


    /**
     * Select table elements according to the entered mode
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
     * Return number of selected rows in table.
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
     * Get index of the selected file.
     * Is necessary to perform a conversion from view to model to prevent problems related to order.
     * @return index of the selected file on table.
     */
    public int getSelectedRow() {
        return tblListTable.convertRowIndexToModel(tblListTable.getSelectedRow());
    }

    /**
     * Set data to be shown on table.
     * @param data Data to be shown on table.
     */
    public void setData(final Object[][] data) {
        tableModel.setValues(data);
    }

    /**
     * Indicate whether the table is empty.
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
     * Adjust the table size according to the values passed as parameters.
     * @param width Width of the main window.
     * @param height Height  of the main window.
     * @param isFilterVisible Indicates whether the data filter is visible.
     */
    public void adjustTableSize(final int width, final int height, final boolean isFilterVisible) {
        if (isFilterVisible) {
            pnlScrollableTableArea.setBounds(2, 166, width - 24, height - 250);
        } else {
            pnlScrollableTableArea.setBounds(2, 6, width - 24, height - 90);
        }
        tblListTable.repaint();
    }

}

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

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;


/**
 * Data model for Table.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 02/06/2014
 */
public final class ListTableModel extends AbstractTableModel implements TableModelListener {

    /** Class ID. */
    private static final long serialVersionUID = 6177656234370478043L;

    /** Column visibility status. */
    private boolean[] visible = new boolean[ColumnsId.values().length];

    /** Shown data. */
    private Object[][] data = new Object[0][0];

    /**
     * Constructor. Create a new instance of model setting all columns visible.
     */
    public ListTableModel() {
        for (int cont = 0; cont < visible.length; cont++) {
            visible[cont] = true;
        }
    }

    /**
     * Set column visibility, according to an array passed as parameter.
     *
     * @param visibilidad
     *            Array containing visibility statuses <code>true</code> visible
     *            <code>false</code> hidden.
     */
    public void setVisible(final boolean[] visibilidad) {
        for (int cont = 0; cont < visible.length; cont++) {
            visible[cont] = visibilidad[cont];
        }
        fireTableStructureChanged();
    }

    /**
     * Return number of visible columns.
     *
     * @return Number of visible columns.
     */
    public int getColumnCount() {
        int numVisible = 0;
        for (boolean b : visible) {
            if (b) {
                numVisible++;
            }
        }

        return numVisible;
    }

    /**
     * Return number of data in model.
     *
     * @return number of date in model.
     */
    public int getRowCount() {
        return data.length;
    }

    /**
     * Return Name of the column which index is passed as parameter. Visibility
     * status is taken into account.
     *
     * @param col
     *            column index.
     * @return Column name.
     */
    public String getColumnName(final int col) {
        int numVisible = -1;
        int numCol = -1;

        while (numVisible != col) {
            numCol++;
            if (visible[numCol]) {
                numVisible++;
            }
        }

        return ColumnsId.values()[numCol].getName();
    }

    /**
     * Return the value shown in the cell which coordinates are passed as
     * parameters.
     *
     * @param row
     *            Row to which requested cell belongs.
     * @param col
     *            Column to which requested cell belongs.
     * @return Cell value (row, column).
     */
    public Object getValueAt(final int row, final int col) {
        int numVisible = -1;
        int numCol = -1;

        while (numVisible != col) {
            numCol++;
            if (visible[numCol]) {
                numVisible++;
            }
        }

        return data[row][numCol];
    }

    /**
     * Return the value shown in the cell which coordinates are passed as
     * parameters, without considering the visibility status of columns.
     *
     * @param row
     *            Row to which requested cell belongs.
     * @param col
     *            Column to which requested cell belongs.
     * @return Cell value (row, column).
     */
    public Object getAbsoluteValueAt(final int row, final int col) {
        return data[row][col];
    }

    /**
     * Set values to be shown on table.
     *
     * @param values
     *            Values to be shown on table.
     */
    public void setValues(final Object[][] values) {
        data = values;
        fireTableDataChanged();
    }

    /**
     * Notify change on table.
     *
     * @param e
     *            Table model event.
     */
    @Override
    public void tableChanged(final TableModelEvent e) {
        fireTableChanged(e);
    }


    /**
     * Returns <code>Object.class</code> regardless of columnIndex<br>
     * Must be overriden for the correct behavior of table
     * autosorter.
     * @param col the column being queried
     * @return The <code>Object.class</code>
     */
    @Override
    public Class<?> getColumnClass(final int col) {
        if (data.length > 0) {
            return data[0][col].getClass();
        } else {
            return super.getColumnClass(col);
        }
    }
}

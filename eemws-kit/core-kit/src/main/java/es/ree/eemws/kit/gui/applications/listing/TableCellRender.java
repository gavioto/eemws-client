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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.table.DefaultTableCellRenderer;


/**
 * Class to format table cells.
 * Table is is shown in striped table format, status values are shown in green / red.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 02/06/2014
 */
public class TableCellRender extends DefaultTableCellRenderer {

    /** Class ID. */
    private static final long serialVersionUID = 1637294076635324355L;

    /** Output format for Date Objects. */
    private SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    /**
     * Format date values preventing being taken as strings by Table comparator.
     * @param value value to be shown on cell.
     */
    public final void setValue(final Object value) {
        String defaultValue = "-";
        if (value != null) {
            if (value instanceof Calendar) {
                String fomattedDate = "";
                Date fecha = ((Calendar) value).getTime();
                fomattedDate = sdfDateTime.format(fecha);
                setText(fomattedDate);
            } else {
                super.setValue(value);
            }
        } else {
            setText(defaultValue);
        }

    }
}

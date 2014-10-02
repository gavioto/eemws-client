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

/**
 * Enumeration containing column names on list.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 02/06/2014
 *
 */
public enum ColumnsId {

    /** Column Code. */
    CODE("Code"),

    /** Column Id. */
    ID("ID"),

    /** Column Version. */
    VERSION("Version"),

    /** Column Status. */
    STATUS("Status"),

    /** Column Start Time. */
    APPLICATION_ST_TIME("Appl. Start Time"),

    /** Column End Time. */
    APPLICATION_END_TIME("Appl. End Time"),

    /** Column Server Timestamp. */
    SERVER_TIMESTAMP("Server Timestamp"),

    /** Column Type. */
    TYPE("Type"),

    /** Column Owner. */
    OWNER("Owner");

    /** Column name (visible). */
    private final String columnName;

    /**
     * Constructor. Sets the visible name of column.
     * @param name Visible name of column.
     */
    private ColumnsId(final String name) {
        columnName = name;
    }

    /**
     * Return the visible name of column.
     * @return Visible name of column.
     */
    public String getName() {
        return columnName;
    }
}

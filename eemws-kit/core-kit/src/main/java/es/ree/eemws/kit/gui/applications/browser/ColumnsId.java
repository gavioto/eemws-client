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

import es.ree.eemws.kit.common.Messages;

/**
 * Enumeration with the message list table view columns.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 02/06/2014
 *
 */
public enum ColumnsId {

    /** Column Code. */
    CODE(Messages.getString("BROWSER_COLUMN_CODE")), //$NON-NLS-1$

    /** Column Id. */
    ID(Messages.getString("BROWSER_COLUMN_ID")), //$NON-NLS-1$

    /** Column Version. */
    VERSION(Messages.getString("BROWSER_COLUMN_VERSION")), //$NON-NLS-1$

    /** Column Status. */
    STATUS(Messages.getString("BROWSER_COLUMN_STATUS")), //$NON-NLS-1$

    /** Column Start Time. */
    APPLICATION_ST_TIME(Messages.getString("BROWSER_COLUMN_APPLICATION_ST_TIME")), //$NON-NLS-1$

    /** Column End Time. */
    APPLICATION_END_TIME(Messages.getString("BROWSER_COLUMN_APPLICATION_END_TIME")), //$NON-NLS-1$

    /** Column Server Timestamp. */
    SERVER_TIMESTAMP(Messages.getString("BROWSER_COLUMN_SERVER_TIMESTAMP")), //$NON-NLS-1$

    /** Column Type. */
    TYPE(Messages.getString("BROWSER_COLUMN_MSG_TYPE")), //$NON-NLS-1$

    /** Column Owner. */
    OWNER(Messages.getString("BROWSER_COLUMN_OWNER")); //$NON-NLS-1$

    /** Column text (visible text). */
    private final String columnText;

    /**
     * Constructor. Sets the visible text column.
     * @param text Visible text column.
     */
    private ColumnsId(final String text) {
        columnText = text;
    }

    /**
     * Return the visible text column.
     * @return Visible text column.
     */
    public String getText() {
        return columnText;
    }
}

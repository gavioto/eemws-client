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
 * Exception for problems relative to filter settings.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 02/06/2014
 */
public class FilterException extends RuntimeException {

    /** Class ID. */
    private static final long serialVersionUID = 5346790433959607361L;

    /**
     * Constructor. Create a new Filter Exception containing the text
     * passed as argument.
     * @param text Explanatory text about error.
     */
    public FilterException(final String text) {

        super(text);
    }
}

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
package es.ree.eemws.client.exception;


/**
 * General exception used by the client of the web service.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class ClientException extends Exception {

    /** Serial version UID. */
    private static final long serialVersionUID = 1738083255122012738L;

    /**
     * Constructor.
     */
    public ClientException() {

        super();
    }

    /**
     * Constructor.
     * @param cause Cause of the exception.
     */
    public ClientException(final String cause) {

        super(cause);
    }

    /**
     * Constructor.
     * @param cause Exception.
     */
    public ClientException(final Throwable cause) {

        super(cause);
    }

    /**
     * Constructor.
     * @param errorMessage Error message.
     * @param cause Exception.
     */
    public ClientException(final String errorMessage, final Throwable cause) {

        super(errorMessage, cause);
    }
}

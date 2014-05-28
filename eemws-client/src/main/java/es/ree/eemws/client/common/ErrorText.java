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
package es.ree.eemws.client.common;


/**
 * Class with the text of the error in the client application.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class ErrorText {

    /**
     * Constructor.
     */
    private ErrorText() {

        /* This method should not be implemented. */
    }

    /** Error text. */
    public static final String ERROR_TEXT_001 = "Header response incorrect. Reply Verb[{0}] Noun[{1}] must be Verb[{2}] Noun[{3}]";

    /** Error text. */
    public static final String ERROR_TEXT_002 = "The service response with error. Reply Result[{0}]";

    /** Error text. */
    public static final String ERROR_TEXT_003 = "Unable to get the Payload. Error[{0}]";

    /** Error text. */
    public static final String ERROR_TEXT_004 = "Unable to create the Payload. Error[{0}]";

    /** Error text. */
    public static final String ERROR_TEXT_005 = "Unable to get the parameter [{0}]";

    /** Error text. */
    public static final String ERROR_TEXT_006 = "Configuration incorrect. Error [{0}]";

    /** Error text. */
    public static final String ERROR_TEXT_007 = "URL is empty";

    /** Error text. */
    public static final String ERROR_TEXT_008 = "Key store file is empty";

    /** Error text. */
    public static final String ERROR_TEXT_009 = "Unable to get the user certificate. Error [{0}]";
}

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
 * Model for message status (correct / incorrect).
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 02/06/2014
 */
public final class MessageStatus  {

    /** String which states correct status. */
    private static final String STATUS_OK = "OK";

    /** String which states incorrect status. */
    private static final String STATUS_NO_OK = "FAILED";

    /** Message status. */
    private boolean status;

    /**
     * Constructor. Creates a new instance of the object containing the status.
     * @param isOK Message status (<code>true</code> if correct,
     * <code>false</code> if incorrect).
     */
    public MessageStatus(final boolean isOK) {

        status = isOK;
    }

    /**
     * Constructor. Creates a new instance of the object containing the status.
     * @param pStatus String containing value ({@link #STATUS_OK} or {@link #STATUS_NO_OK})
     */
    public MessageStatus(final String pStatus) {

        status = STATUS_OK.equals(pStatus);
    }


    /**
     * Indicate whether the message is correct or not.
     * @return <code>true</code> If correct. <code>false</code> otherwise.
     */
    public boolean isOk() {

        return status;
    }

    /**
     * Return message status as a String.
     * @return Message status as a String.
     */
    public String toString() {

        String retValue = STATUS_OK;
        if (!status) {
            retValue = STATUS_NO_OK;
        }

        return retValue;
    }
}

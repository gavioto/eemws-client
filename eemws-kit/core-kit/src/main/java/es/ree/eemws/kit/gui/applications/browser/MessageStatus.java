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
 * Model for message status (correct / incorrect).
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 02/06/2014
 */
public final class MessageStatus  {

    /** String which states correct status. */
    private static final String STATUS_OK = "OK"; //$NON-NLS-1$

    /** Text to be shown for status "ok". */
    private static final String STATUS_OK_TEXT = Messages.getString("BROWSER_STATUS_OK"); //$NON-NLS-1$
 
    /** Text to be shown for status "failed". */
    private static final String STATUS_FAILED_TEXT = Messages.getString("BROWSER_STATUS_FAILED"); //$NON-NLS-1$
    
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
     * Indicates whether the message is correct or not.
     * @return <code>true</code> If correct. <code>false</code> otherwise.
     */
    public boolean isOk() {

        return status;
    }

    /**
     * Returns message status as a String. Note that the returned
     * text is a human readable text. For a "Ok" message the returned
     * text could be "Correct". The returned text dependes on the
     * <code>messages.properties</code> file (see project resource's)
     * @return Message status as a String.
     */
    @Override
    public String toString() {

        String retValue;
        if (status) {
            retValue = STATUS_OK_TEXT;
        } else {
        	retValue = STATUS_FAILED_TEXT;
        }

        return retValue;
    }
}

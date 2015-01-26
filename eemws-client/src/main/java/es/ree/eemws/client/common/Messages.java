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

import java.util.ResourceBundle;

import es.ree.eemws.core.utils.messages.AbstractMessages;


/**
 * Class to manage the messages of the application.
 *
 * @author Red Eléctrica de España S.A.U.
 * @version 1.0 13/06/2014
 */
public final class Messages extends AbstractMessages {

    /** Base name of the bundle. */
    private static final String BUNDLE_NAME = "properties.client_messages"; //$NON-NLS-1$

    /** Resource bundle. */
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	
    /**
     * Constructor.
     */
    private Messages() {

        /* This method should not be implemented. */
    }
    
    /**
     * This method gets the message given its key.
     * If there is no text for the given key the string <code>???KEY???</code> will be returned.
     * @param key Key of the message.
     * @param parameters parameters that will be replaced in the message.
     * @return Message of the key.
     */
    public static String getString(final String key, final Object... parameters) {
        return AbstractMessages.getString(RESOURCE_BUNDLE, key, parameters);
    }
}

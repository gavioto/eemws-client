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
package es.ree.eemws.kit.gui.common;

import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import es.ree.eemws.kit.common.Messages;


/**
 * The <code>Log</code> class read the log configuration and
 * returns a Log object.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 09/05/2014
 */
public final class Log {

    /** Returned log object. */
    private static Log logger = null;

    /** Log configuration file. */
    private static final String CONFIG_FILE = "config/logging.properties";

    /**
     * Private constructor, in order to use the class as a singleton.
     * Configuration file must be read only once.
     */
    private Log() {

        try {

            InputStream isProps = this.getClass().getClassLoader().getResourceAsStream(CONFIG_FILE);
            LogManager.getLogManager().readConfiguration(isProps);

        } catch (Exception ex) {

            System.out.println(Messages.getString("kit.gui.editor.90") + " " + ex);
        }
    }

    /**
     * Returns a Logger instance, avoid read the configuration file
     * for each instance.
     * @return A Logger object in order to log.
     */
    public static synchronized Logger getInstance() {

        if (logger == null) {

            logger = new Log();
        }

        return (Logger.getLogger("."));
    }
}

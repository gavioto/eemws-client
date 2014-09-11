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

package es.ree.eemws.kit.folders;

import java.util.logging.Logger;

import javax.swing.UIManager;

/**
 * Facade for class {@link StatusIcon}. Prevents errors during execution
 * if Runtime version is 1.5.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 29/05/2014
 */

public final class StatusIconFacade {

    /** Minimum version to invoke Status change Icon. */
    private static final float COMPARE_VERSION = 1.6F;

    /** Reference to the class which controls status icon. */
    private static StatusIcon statusIcon = null;

    /** Thread log system. */
    private static Logger log = Logger.getLogger(Thread.currentThread().getStackTrace()[0].getClassName());


    /** Static classes must not contain public constructor. */
    private StatusIconFacade() {
        /** THis constructor must not be implemented. */
    }

    /**
     * Initialize class for Status icon if system is set to interactive mode
     * and Runtime version is equal to 1.5 or greater.
     * @param config Connection kit settings.
     */
    public static void initializeStatusIconFacade(final Configuration config) {
        String versionJava = System.getProperty("java.version");
        versionJava = versionJava.substring(0, versionJava.lastIndexOf("."));
        float versionF = Float.parseFloat(versionJava);

        if (versionF >= COMPARE_VERSION  && System.getProperty("folder.intercativo") != null) {
            statusIcon = new StatusIcon(config);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                /*
                 * ClassNotFoundException, InstantiationException,
                 * IllegalAccessException,
                 * UnsupportedLookAndFeelException
                 */
                log.info("[FOLDER] " + ex.getMessage());
            }
        }
    }

    /**
     * Invoke control class for Icon status to set the 'idle' icon.
     */
    public static void idle() {
        if (statusIcon != null) {
            statusIcon.setIdle();
        }
    }

    /**
     * Invoke control class for Icon status to set the 'busy' icon.
     */
    public static void busy() {
        if (statusIcon != null) {
            statusIcon.setBusy();
        }
    }


}

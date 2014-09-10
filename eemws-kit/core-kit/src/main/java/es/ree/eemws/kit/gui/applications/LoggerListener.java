/*
 * Copyright 2014 Red El�ctrica de Espa�a, S.A.U.
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
 * reference to Red El�ctrica de Espa�a, S.A.U. as the copyright owner of
 * the program.
 */
package es.ree.eemws.kit.gui.applications;


/**
 * LoggerListener allows graphical applications to be aware
 * of changes on log window.
 *
 * @author Red El�ctrica de Espa�a S.A.U.
 * @version 1.0 13/06/2014
 */
public interface LoggerListener {

    /** Notifies closing of Log window. */
    void logWindowIsClosing();
}

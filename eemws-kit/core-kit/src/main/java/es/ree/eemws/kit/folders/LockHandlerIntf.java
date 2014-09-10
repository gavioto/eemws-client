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

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Processes that must synchronize to service farm
 * must implement this interface.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 29/05/2014
 *
 */
public interface LockHandlerIntf extends Remote {

    /**
     * Notifies to another service the existence of a new member.
     * @param url Full address to the new active service.
     * @throws RemoteException If remote invocation failed..
     */
    void suscribe(String url) throws RemoteException;

    /**
     * Check with another service whether the file name passed as
     * argument is locked.
     * @param fileName Name of the file to check.
     * @param remoteId ID for the remote service to check with
     * @return <code>true</code> IF the file is locked by the
     * service<code>false</code> otherwise.
     * @throws RemoteException If remote call fails.
     */
    boolean isLocked(String fileName, int remoteId) throws RemoteException;
}

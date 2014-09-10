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

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * <code>LockHandler</code> class implements the remote interface
 * to request message lock.
 *
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 29/05/2014
 *
 */
public final class LockHandler extends UnicastRemoteObject implements LockHandlerIntf {

    /** Class ID. */
    private static final long serialVersionUID = 189508672518820218L;

    /** Indicate that based on the number of that there can be only be a server. */
    private static final int ONLY_ONE_SERVER = 2;

    /** Name of the service to run / create . */
    private static final String SERVICE_NAME = "magic-folder";

    /** Number of invalid attempts before giving up. */
    private static final int NUM_RETRIES = 3;

    /** Number of milliseconds to wait before retry after a failed attempt. */
    private static final long SLEEP_BEFORE_RETRY = 5000;

    /** Indicate whether is there an only server or multiple servers. */
    private boolean isSingle = false;

    /** Mutual exclusion Lock. */
    private Object lock = new Object();

    /** Mutual exclusion lock for the Management of group members. */
    private Object neighborLock = new Object();

    /** Names of locked messages. */
    private Vector<String> lockFiles;

    /** Names of messages to be locked. */
    private Vector<String> tryLockFiles;

    /** Denotes the number of active neighbors. */
    private int numberOfNeighbors;

    /** Unique ID for this service into farm. */
    private int thisServiceID;

    /** Contains full addresses of all members. */
    private Vector<String> neighborURLs;

    /** Contains references (remote stubs) to all members. */
    private Vector<LockHandlerIntf> neighbors;

    /** Thread log system. */
    private static Logger log = Logger.getLogger(Thread.currentThread().getStackTrace()[0].getClassName());


    /**
     * Constructor. Creates a new lock manager with the values passed as arguments.
     * The manager will find the remote references to all members and will subscribe
     * to all of them.
     * @param config System settings.
     * @throws RemoteException If cannot create an RMI register and subscribe to it.
     */
    public LockHandler(final Configuration config) throws RemoteException {
        String serviceID = config.getServiceID();
        ArrayList<String> serviceFarm = config.getServiceFarm();

        if (serviceID == null || serviceFarm == null || (serviceFarm != null && serviceFarm.size() < ONLY_ONE_SERVER)) {
            isSingle = true;
        } else {
            isSingle = false;
            lockFiles = new Vector<String>();
            tryLockFiles = new Vector<String>();
            neighborURLs = new Vector<String>();
            neighbors = new Vector<LockHandlerIntf>();

            /* Retrieving IP and port values for this service. */
            thisServiceID = Integer.parseInt(serviceID);

            String url = "rmi://" + (String) serviceFarm.get(thisServiceID - 1) + "/" + SERVICE_NAME;

            createRMIRegistry(url);

            /*  Retrieving full URLs for the rest of services
             *  URL must always match the pattern "rmi://<host>:<port>/SERVICE_NAME". */
            String[] urls = getRemoteURLs(serviceFarm);

            /*
             *  Retrieving remote references to the rest of servers.
             *  If a server is not available, another attempt will be made
             *  after the time set by SLEEP_BEFORE_RETRY expressed in milliseconds.
             *  Once the maximum number of retries set by NUM_RETRIES is reached,
             *  the server will be taken for unavailable and ignored.
             */
            getRemoteReferences(urls);

            /* Notifying the rest of neighbors our existence. */
            neighborSubscription(url);
        }
    }

    /**
     * Retrieve the URLs of the rest of neighbors.
     * Return array containing info about all hosts and ports
     * @param serviceFarm Addresses of Host and port for neighbor services..
     * @return Array containing URLs of neighbor settings.
     */
    private String[] getRemoteURLs(final ArrayList<String> serviceFarm) {
        ArrayList<String> servers = new ArrayList<String>();
        int numberOfServers = serviceFarm.size();
        for (int count = 0; count < numberOfServers; count++) {
            if (thisServiceID != (count + 1)) {
                servers.add("rmi://" + serviceFarm.get(count) + "/" + SERVICE_NAME);
            }
        }

        return (String[]) servers.toArray(new String[]{});
    }

    /**
     * Creates an RMI registry on port passed as argument.
     * Service will subscribe to this registry, thus the class registers
     * as Request server.
     * @param url URL where service subscribes.
     * @throws RemoteException If cannot connect to RMI server.
     */
    private void createRMIRegistry(final String url) throws RemoteException {

        try {
            try {
                /* Search port in URL expression: rmi://<host>:<port>/service_name */
                int colonPosition = url.indexOf(":");
                colonPosition = url.indexOf(":", colonPosition + 1);
                String port = url.substring(colonPosition + 1, url.indexOf("/", colonPosition));
                LocateRegistry.createRegistry(Integer.parseInt(port));
            } catch (RemoteException ex) {
                log.warning("[LOCK] Cannot create a registry, probably already exists one.");
            }

            /* Subscribe to service. */
            Naming.rebind(url, this);
        } catch (MalformedURLException e) {
            throw new RemoteException("Incorrect URL " + url);
        }
    }

    /**
     * Retrieve remote references to neighbors.
     * @param urls URLs of the services to inspect.
     * @throws RemoteException If the settings of any server are incorrect.
     */
    private void getRemoteReferences(final String[] urls) throws RemoteException {
        LockHandlerIntf interfaceN;
        boolean gotReference;

        for (int count = 0; count < urls.length; count++) {
            gotReference = false;

            for (int times = 0; !gotReference && times < NUM_RETRIES; times++) {
                try {
                    interfaceN = (LockHandlerIntf) Naming.lookup(urls[count]);

                    synchronized (neighborLock) {
                        if (!neighborURLs.contains(urls[count])) {
                            neighbors.add(interfaceN);
                            neighborURLs.add(urls[count]);
                        }
                    }

                    gotReference = true;
                } catch (MalformedURLException e) {
                    throw new RemoteException("incorrect URL " + urls[count]);
                } catch (RemoteException ex) {
                    log.severe("[LOCK] Server " + urls[count] + " Not available yet...");
                } catch (NotBoundException ex) {
                    log.severe("[LOCK] Server " + urls[count] + " Not available yet...");
                }

                try {
                    Thread.sleep(SLEEP_BEFORE_RETRY * (times + 1));
                } catch (InterruptedException ex) {
                    log.finest("[LOCK] The wait for remote references retrieval has been interrupted.");
                }
            }

            if (!gotReference) {
                log.severe("Service " + urls[count] + " IS NOT ACTIVE ");
            }
        }

        synchronized (neighborLock) {
            numberOfNeighbors = neighbors.size();
        }
    }

    /**
     * Notifies the rest of nodes our existence. This notification is necessary
     * when a service dies (the other nodes must remove from their list) and
     * when is restarted (is necessary add to the list of the other nodes).
     * @param url Listening request URL for this node.
     */
    private void neighborSubscription(final String url) {
        LockHandlerIntf interfazN;
        for (int cont = 0; cont < numberOfNeighbors; cont++) {
            interfazN = (LockHandlerIntf) neighbors.elementAt(cont);

            try {
                interfazN.suscribe(url);
            } catch (RemoteException ex) {
                log.severe("[LOCK] CAnnot subscribe to [" + neighborURLs.elementAt(cont) + "]");
            }
        }
    }

    /**
     * Release the message passed as argument in order to can be retrieved by other members.
     * @param fileName ID of the message to be released.
     */
    public void releaseLock(final String fileName) {
        if (!isSingle) {
            synchronized (lock) {
                lockFiles.remove(fileName);
            }
        }
    }

    /**
     * Indicate to this server the existence of a new member into the farm.
     * @param remoteURL remote URL (rmi://host:port/service) of the new member.
     */
    public void suscribe(final String remoteURL) {

        int pos = neighborURLs.indexOf(remoteURL);

        try {
            synchronized (neighborLock) {
                if (pos == -1) {    // If reference is nonexistent is added,
                    neighborURLs.add(remoteURL);
                    neighbors.add((LockHandlerIntf) Naming.lookup(remoteURL));
                    numberOfNeighbors++;
                } else { // If the reference already exists, replace the previous one
                    neighbors.setElementAt((LockHandlerIntf) Naming.lookup(remoteURL), pos);
                }
            }
        } catch (MalformedURLException ex) {
            /* Block theoretically unreachable, URL previously checked.  */
            log.finest("[LOCK] Unable to get reference to the neighbor which is requesting subscription: " + remoteURL);
        } catch (RemoteException ex) {
            log.severe("[LOCK] Unable to get reference to the neighbor which is requesting subscription: " + remoteURL);
        } catch (NotBoundException ex) {
            log.severe("[LOCK] neighbor which is requesting subscription is not accepting any request or is nonexistent: " + remoteURL);
        }
    }

    /**
     * Try to lock the message passed as argument.
     * @param fileName Name of the message to lock.
     * @return <code>true</code> If the file could be locked
     * <code>false</code> otherwise.
     */
    public boolean tryLock(final String fileName) {
        boolean canLock = true;

        if (!isSingle) {
            synchronized (lock) {
                tryLockFiles.add(fileName);
            }

            boolean lockedByNeighbor = isLockedByNeighbor(fileName);

            synchronized (lock) {
                if (!lockedByNeighbor) {
                    if (tryLockFiles.contains(fileName)) {
                        canLock = true;
                        lockFiles.add(fileName);
                    } else {
                        canLock = false;
                    }
                } else {
                    canLock = false;
                }

                tryLockFiles.remove(fileName);
            }
        }

        return canLock;
    }

    /**
     * Ask neighbors (other group members) whether the file is locked.
     * If any communication problem is found (eg. crashed), is removed from group.
     * @param fileName file to query.
     * @return <code>true</code> If the file is locked by another member.
     * <code>false</code> Otherwise.
     */
    private boolean isLockedByNeighbor(final String fileName) {
        boolean lockedByNeighbor = false;

        int count = 0;

        while (!lockedByNeighbor && (count < numberOfNeighbors)) {
            LockHandlerIntf interfaceN = (LockHandlerIntf) neighbors.elementAt(count);
            boolean success = false;

            for (int attempt = 0; !success && (attempt < NUM_RETRIES); attempt++) {
                try {
                    lockedByNeighbor = interfaceN.isLocked(fileName, thisServiceID);
                    count++;
                    success = true;
                } catch (Exception ex) {
                    log.severe("\n\n\n[LOCK] Server " + neighborURLs.elementAt(count) + " is not available\n\n\n");
                    try {
                        Thread.sleep(SLEEP_BEFORE_RETRY);
                    } catch (InterruptedException ex1) {
                        log.severe("\n\n\n[LOCK] Server " + neighborURLs.elementAt(count) + " has been removed from group\n\n\n");
                    }
                }
            }

            /*
             * If after multiple attempts node does not response
             * is tagged as 'dead' and will no be communicated
             * any longer until it is not be subscribed again.
             */
            if (!success) {
                synchronized (neighborLock) {

                    log.warning("\n\n\n[LOCK] Server " + neighborURLs.elementAt(count) + " has been removed from group\n\n\n");

                    neighborURLs.removeElementAt(count);
                    neighbors.removeElementAt(count);
                    numberOfNeighbors--;
                }
            }
        }

        return lockedByNeighbor;
    }

    /**
     * Check whether the message passed as argument is locked by the
     * member into farm.
     * A message is locked if:
     * - Is in the list of Locked messages.
     * - Is in the list of desired messages and this server has bigger priority (lower remote ID).
     *
     * @param fileName Name of the message to retrieve.
     * @param remoteID Remote ID of the asking server.
     * @return <code>true</code> if the message is locked by
     * this member <code>false</code> otherwise.
     * @throws RemoteException If there is no response from member.
     */
    public boolean isLocked(final String fileName, final int remoteID) throws RemoteException {
        synchronized (lock) {

            /* If message is locked and so will be marked. */
            if (lockFiles.contains(fileName)) {
                return true;
            }

            /*
             * If message is requested by node 1 and node 2 asks for it, priority
             * will be given to node 1, indicating this way to node 2 that is locked.
             */
            if (tryLockFiles.contains(fileName) && thisServiceID < remoteID) {
                return true;
            }

            /*
             * If message is requested by node 1 and node 2 asks for it, is removed from
             * the request list of node 2 and is processed by node 1.
             */
            if (tryLockFiles.contains(fileName) && thisServiceID > remoteID) {
                tryLockFiles.remove(fileName);
            }
        }

        return false;
    }
}

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
import java.util.List;
import java.util.logging.Logger;

import es.ree.eemws.kit.common.Messages;

/**
 * <code>LockHandler</code> class implements the remote interface to communicate working group.
 * 
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 29/05/2014
 * 
 */
public final class LockHandler extends UnicastRemoteObject implements LockHandlerIntf {

	/** Class ID. */
	private static final long serialVersionUID = 9069481093242188767L;

	/** Constant for "only one server in the group. */
	private static final int ONLY_ONE_SERVER = 1;

	/** Number of invalid attempts before give up. */
	private static final int NUM_RETRIES = 3;

	/** Number of milliseconds to wait before retry after a failed attempt. */
	private static final long SLEEP_BEFORE_RETRY = 5000;

	/** Indicate whether is there an only server or multiple servers. */
	private boolean isSingle = false;

	/** Names of locked files. */
	private List<String> lockFiles;

	/** Names of messages to be locked. */
	private List<String> tryLockFiles;

	/** Unique ID for this service in the group. */
	private int thisServiceID;

	/** Members of the group. */
	private ArrayList<Member> members = new ArrayList<>();

	/** Logging system. */
	private static final Logger LOGGER = Logger.getLogger(LockHandler.class.getName());

	/**
	 * Constructor. Creates a new lock manager according to the configuration. The manager will find the remote
	 * references to all members and will subscribe to all of them.
	 * @param config System settings.
	 * @throws RemoteException If cannot create an RMI register and subscribe to it.
	 */
	public LockHandler(final Configuration config) throws RemoteException {
		String serviceID = config.getServiceID();
		List<String> membersRmiUrls = config.getMembersRmiUrls();

		if (serviceID == null || membersRmiUrls == null || membersRmiUrls.size() == ONLY_ONE_SERVER) {
			isSingle = true;
			LOGGER.info(Messages.getString("MF_STAND_ALONE")); //$NON-NLS-1$
		} else {
			isSingle = false;
			lockFiles = new ArrayList<>();
			tryLockFiles = new ArrayList<>();

			thisServiceID = Integer.parseInt(serviceID);
			String thisServerURL = membersRmiUrls.get(thisServiceID - 1);
			createRMIRegistry(thisServerURL);
			membersRmiUrls.remove(thisServerURL);
			
			LOGGER.info(Messages.getString("MF_SEARCHING_MEMBERS", String.valueOf(membersRmiUrls.size()))); //$NON-NLS-1$
			
			getRemoteReferences(membersRmiUrls, thisServerURL);
			
			LOGGER.info(Messages.getString("MF_GROUP_LISTEINGN_URL", thisServerURL)); //$NON-NLS-1$
		}
	}

	/**
	 * Creates an RMI registry on port passed as argument.
	 * @param url URL where service subscribes.
	 * @throws RemoteException If cannot connect to RMI server.
	 */
	private void createRMIRegistry(final String url) throws RemoteException {

		try {
			try {
				/* Search port in URL expression: rmi://<host>:<port>/service_name */
				int colonPosition = url.indexOf(":"); //$NON-NLS-1$
				colonPosition = url.indexOf(":", colonPosition + 1); //$NON-NLS-1$
				String port = url.substring(colonPosition + 1, url.indexOf("/", colonPosition)); //$NON-NLS-1$
				LocateRegistry.createRegistry(Integer.parseInt(port));
			} catch (RemoteException ex) {
				LOGGER.warning(Messages.getString("MF_UNABLE_TO_CREATE_REGISTRY")); //$NON-NLS-1$
			}

			/* Subscribe to service. */
			Naming.rebind(url, this);
		} catch (MalformedURLException e) {
			throw new RemoteException(Messages.getString("MF_INVALID_HOST_PORT", url)); //$NON-NLS-1$
		}
	}

	/**
	 * Retrieve remote references to neighbors.
	 * @param membersRmiUrls List of all members' URL.
	 * @param thisMemberUrl This member URL.
	 * @throws RemoteException If the settings of any server are incorrect.
	 */
	private void getRemoteReferences(final List<String> membersRmiUrls, final String thisMemberUrl) throws RemoteException {

		boolean gotReference;

		for (String memberUrl : membersRmiUrls) {

			gotReference = false;

			LOGGER.info(Messages.getString("MF_CONNECTING_WITH_MEMBER", memberUrl)); //$NON-NLS-1$
			
			for (int times = 0; !gotReference && times < NUM_RETRIES; times++) {
				
				try {
					LockHandlerIntf interfaceN = (LockHandlerIntf) Naming.lookup(memberUrl);
					
					interfaceN.suscribe(thisMemberUrl);
					
					Member mem = new Member(memberUrl, interfaceN);
					
					synchronized (members) {
						if (!members.contains(mem)) {
							members.add(mem);
						}
					}
										
					gotReference = true;
				} catch (MalformedURLException e) {
					throw new RemoteException(Messages.getString("MF_INVALID_MEMBER_CONFIGURATION", memberUrl)); //$NON-NLS-1$
				} catch (RemoteException | NotBoundException ex) {
					LOGGER.info(Messages.getString("MF_MEMBER_NOT_AVAILABLE_YET", memberUrl)); //$NON-NLS-1$
				}

				try {
					Thread.sleep(SLEEP_BEFORE_RETRY * (times + 1));
				} catch (InterruptedException ex) {
					LOGGER.finest("Interrupted!"); // Don't mind! //$NON-NLS-1$
				}
			}

			if (!gotReference) {
				LOGGER.warning(Messages.getString("MF_MEMBER_NOT_AVAILABLE", memberUrl)); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Release the message passed as argument in order to can be retrieved by other members.
	 * @param fileName ID of the message to be released.
	 */
	public void releaseLock(final String fileName) {
		if (!isSingle) {
			synchronized (lockFiles) {
				lockFiles.remove(fileName);
			}
		}
	}

	/**
	 * Indicate to this server the existence of a new member in the group.
	 * @param remoteURL remote URL (rmi://host:port/service) of the new member.
	 */
	public void suscribe(final String remoteURL) {

		try {
			LockHandlerIntf remoteReference = (LockHandlerIntf) Naming.lookup(remoteURL);

			synchronized (members) {
				boolean updated = false;
				for (Member member : members) {
					if (remoteURL.equals(member.getUrl())) {
						LOGGER.info(Messages.getString("MF_UPDATE_MEMBER", remoteURL)); //$NON-NLS-1$
						member.setRemoteReference(remoteReference);
						updated = true;
					}
				}

				/* New member. */
				if (!updated) {
					LOGGER.info(Messages.getString("MF_NEW_MEMBER", remoteURL)); //$NON-NLS-1$
					members.add(new Member(remoteURL, remoteReference));
				}
			}

		} catch (MalformedURLException e) {
			LOGGER.warning(Messages.getString("MF_INVALID_URL_RECEIVED", remoteURL)); //$NON-NLS-1$

		} catch (RemoteException | NotBoundException e) {
			LOGGER.warning(Messages.getString("MF_INVALID_URL_RECEIVED", remoteURL)); //$NON-NLS-1$
		}
	}

	/**
	 * Try to lock the message passed as argument.
	 * @param fileName Name of the message to lock.
	 * @return <code>true</code> If the file could be locked <code>false</code> otherwise.
	 */
	public boolean tryLock(final String fileName) {
		boolean canLock = true;

		if (!isSingle) {
			synchronized (lockFiles) {
				tryLockFiles.add(fileName);
			}

			boolean lockedByNeighbor = isLockedByNeighbor(fileName);

			synchronized (lockFiles) {
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
	 * Ask neighbors (other group members) whether the file is locked. If any communication problem is found (eg.
	 * crashed), is removed from group.
	 * @param fileName file to query.
	 * @return <code>true</code> If the file is locked by another member. <code>false</code> Otherwise.
	 */
	private boolean isLockedByNeighbor(final String fileName) {
		boolean lockedByNeighbor = false;

		int count = 0;

		int numberOfMembers = members.size();

		while (!lockedByNeighbor && (count < numberOfMembers)) {
			LockHandlerIntf interfaceN = members.get(count).getRemoteReference();
			boolean success = false;

			for (int attempt = 0; !success && (attempt < NUM_RETRIES); attempt++) {
				try {
					lockedByNeighbor = interfaceN.isLocked(fileName, thisServiceID);
					count++;
					success = true;
				} catch (RemoteException ex) {
					LOGGER.warning(Messages.getString("MF_MEMBER_NOT_AVAILABLE", members.get(count).getUrl())); //$NON-NLS-1$
					try {
						Thread.sleep(SLEEP_BEFORE_RETRY);
					} catch (InterruptedException ex1) {
						LOGGER.fine("Interrupted!"); //$NON-NLS-1$
					}
				}
			}

			/*
			 * If after multiple attempts node does not response is tagged as 'dead' and will no be communicated any
			 * longer until it is not be subscribed again.
			 */
			if (!success) {
				synchronized (members) {
					LOGGER.severe(Messages.getString("MF_MEMBER_GONE", members.get(count).getUrl())); //$NON-NLS-1$
					members.remove(count);
					numberOfMembers--;
				}
			}
		}

		return lockedByNeighbor;
	}

	/**
	 * Check whether the message passed as argument is locked by this member. A message is locked if: - Is in the list
	 * of Locked messages. - Is in the list of desired messages and this server has bigger priority (lower remote ID).
	 * @param fileName Name of the message to retrieve.
	 * @param remoteID Remote ID of the asking server.
	 * @return <code>true</code> if the message is locked by this member <code>false</code> otherwise.
	 * @throws RemoteException If there is no response from member.
	 */
	public boolean isLocked(final String fileName, final int remoteID) throws RemoteException {
		boolean retValue = false;

		synchronized (lockFiles) {

			retValue = (lockFiles.contains(fileName) || (tryLockFiles.contains(fileName) && thisServiceID < remoteID));

			if (tryLockFiles.contains(fileName) && thisServiceID > remoteID) {
				tryLockFiles.remove(fileName);
			}
		}

		return retValue;
	}

	
	/**
	 * Stores information about working group members. 
	 */
	private class Member {

		/** RMI URL to access to a member. */
		private String rmiUrl;

		/** Remote RMI reference. */	
		private LockHandlerIntf remoteReference;

		
		/**
		 * Creates a new member given its URL and remote interface.
		 * @param url URL of the member.
		 * @param remIterf Remote interface.
		 */
		Member(final String url, final LockHandlerIntf remIterf) {
			rmiUrl = url;
			remoteReference = remIterf;
		}

		/** 
		 * Sets the remote interface of a member (update).
		 * @param remoteRef Remote interface of a member.
		 */
		public void setRemoteReference(final LockHandlerIntf remoteRef) {
			remoteReference = remoteRef;
		}

		/** 
		 * Get the remote interface of a member. 
		 * @return RMI url of the member.
		 */
		public String getUrl() {
			return rmiUrl;
		}

		/**
		 * Return the remote interface of the member. 
		 * @return Remote interface.
		 */
		public LockHandlerIntf getRemoteReference() {
			return remoteReference;
		}		
	}
}

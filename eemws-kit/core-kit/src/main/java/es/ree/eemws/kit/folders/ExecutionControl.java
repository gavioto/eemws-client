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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Prevents the execution of several application instances.
 * @author Red Eléctrica de España, S.A.U.
 * @version 1.0 29/05/2014
 * 
 */
public final class ExecutionControl {

	/** Name of the file to be used to prevent two simultaneous executions. */
	private static final String DEFAULT_LOCK_FILE_NAME = ".magicfolder.lck"; //$NON-NLS-1$

	/** Path to locking file. Will use a temporary path, if not possible "user dir" will be used instead. */
	private static String lockFileFolder = System.getProperty("java.io.tmpdir", System.getProperty("user.home", File.separator)); //$NON-NLS-1$ //$NON-NLS-2$

	private static FileLock fl; 
	
	/**
	 * Check if is there a an existent locking.
	 * Do no close the resources, lock them until program termination.
	 * @return <code>true</code> If locking exists. <code>false</code> otherwise.
	 */
	@SuppressWarnings("resource")
	public static boolean isRunning(final String id) {

		boolean isRunning = false;
		String lockFileName = id;
		if (lockFileName == null) {
			lockFileName = DEFAULT_LOCK_FILE_NAME;
		}

		try {
			FileOutputStream lockStream = new FileOutputStream(lockFileFolder + File.separator + lockFileName); 
			FileChannel fc = lockStream.getChannel(); 

			fl = fc.tryLock();
			if (fl == null) {
				isRunning = true;
			}
		} catch (IOException ix) {
			isRunning = true;
		}

		return isRunning; 
	}
}
